-- ============================================================================
-- V1_0011 notification: notifications table. (The outbox lives in V1_0000.)
-- Columns align with com.santimpay.posctl.notification.domain.Notification.
-- ============================================================================

CREATE TABLE notification.notifications (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  recipient_id uuid,
  channel      text NOT NULL CHECK (channel IN ('IN_APP','EMAIL','SMS','PUSH')),
  template     text NOT NULL,
  payload      jsonb NOT NULL DEFAULT '{}',
  status       text NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SENT','FAILED','READ')),
  related_type text,
  related_id   uuid,
  sent_at      timestamptz,
  read_at      timestamptz,
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  created_by   uuid, updated_by uuid,
  version      integer NOT NULL DEFAULT 0,
  deleted_at   timestamptz
);
CREATE INDEX ix_notif_recipient ON notification.notifications(recipient_id, status);
CREATE INDEX ix_notif_pending ON notification.notifications(status) WHERE status = 'PENDING';
