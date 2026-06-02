-- ============================================================================
-- V1_0012 ai: scores + feature store + embeddings (pgvector) + RAG conversations.
-- Phase-0 data foundation (docs/08). Populated by offline jobs; read by the app.
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai.scores (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  subject_type  text NOT NULL,                 -- 'merchant' | 'device'
  subject_id    uuid NOT NULL,
  score_type    text NOT NULL,                 -- 'risk' | 'health' | 'sales' | 'failure_prob'
  value         numeric(6,4) NOT NULL,
  band          text,
  model_version text NOT NULL,
  features      jsonb,
  computed_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_scores_subject
  ON ai.scores(subject_type, subject_id, score_type, computed_at DESC);

CREATE TABLE ai.feature_store (
  subject_type text NOT NULL,
  subject_id   uuid NOT NULL,
  feature_set  text NOT NULL,
  features     jsonb NOT NULL,
  as_of        date NOT NULL,
  PRIMARY KEY (subject_type, subject_id, feature_set, as_of)
);

CREATE TABLE ai.embeddings (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  source_type text NOT NULL,                   -- 'doc' | 'followup_note' | 'policy'
  source_id   uuid,
  chunk       text NOT NULL,
  embedding   vector(1024) NOT NULL,
  metadata    jsonb,
  created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_embeddings_hnsw ON ai.embeddings USING hnsw (embedding vector_cosine_ops);

CREATE TABLE ai.conversations (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id    uuid NOT NULL,
  title      text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE ai.messages (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id uuid NOT NULL REFERENCES ai.conversations(id) ON DELETE CASCADE,
  role            text NOT NULL CHECK (role IN ('user','assistant','system','tool')),
  content         text NOT NULL,
  tokens          integer,
  citations       jsonb,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_messages_conversation ON ai.messages(conversation_id, created_at);
