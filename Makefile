# posctl — top-level orchestration (Java built by Maven, JS/TS by pnpm/turbo)
.DEFAULT_GOAL := help
SHELL := /bin/bash

API_DIR := apps/api
WEB_DIR := apps/web
APP_DIR := apps/field_app

.PHONY: help
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}'

## ---- Local dev stack ----
.PHONY: up
up: ## Start local infra (postgres, redis, minio, keycloak) via docker compose
	docker compose -f docker-compose.dev.yml up -d

.PHONY: down
down: ## Stop local infra
	docker compose -f docker-compose.dev.yml down

## ---- Backend ----
.PHONY: api
api: ## Run the Spring Boot API (dev profile)
	cd $(API_DIR) && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

.PHONY: api-test
api-test: ## Run backend tests incl. modularity verification
	cd $(API_DIR) && ./mvnw verify

.PHONY: api-build
api-build: ## Build backend jar
	cd $(API_DIR) && ./mvnw -DskipTests package

## ---- Frontend ----
.PHONY: web
web: ## Run the React web console
	pnpm --filter web dev

.PHONY: web-build
web-build: ## Build the web console
	pnpm --filter web build

## ---- Mobile ----
.PHONY: app
app: ## Run the Flutter field app
	cd $(APP_DIR) && flutter run

## ---- Contracts ----
.PHONY: contracts
contracts: ## Regenerate TS + Dart clients from OpenAPI (source of truth: packages/contracts/openapi.yaml)
	pnpm --filter @posctl/contracts contracts:gen

.PHONY: contracts-lint
contracts-lint: ## Lint the OpenAPI contract
	pnpm --filter @posctl/contracts lint

## ---- Quality ----
.PHONY: verify
verify: api-test web-build ## Full local verification gate
