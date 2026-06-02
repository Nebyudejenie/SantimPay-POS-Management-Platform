# posctl-api

Spring Boot 3.x **modular monolith** (Spring Modulith). Reference module: `merchant`.

## First-time setup
This repo ships without the Maven wrapper binary. Generate it once:
```bash
cd apps/api
mvn -N wrapper:wrapper -Dmaven=3.9.9   # creates mvnw + .mvn/wrapper
```

## Run locally
```bash
make up          # postgres + redis + minio + keycloak (from repo root)
make api         # ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# API:     http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# Health:  http://localhost:8080/actuator/health
```

## Test (incl. architecture fitness)
```bash
./mvnw verify    # runs ModularityTests (Spring Modulith verify) + unit/integration tests
```

## Layout (every module follows this)
```
<module>/
  domain/          aggregates, value objects, domain events (JPA-annotated, no Spring)
  application/     use cases (@Service), ports, commands/queries
  infrastructure/  Spring Data adapters, MapStruct mappers, external clients
  web/             @RestController, request/response DTOs
  events/          published integration events (the module's public contract)
  package-info.java  @ApplicationModule
```
Boundaries are enforced by `ModularityTests`. Cross-module calls go through published events only.

## Adding a module
Copy `merchant` as the template, change the package, add a Flyway migration `V1_00NN__<module>.sql`,
register permissions in `V1_0001__identity.sql` + `shared.security.Permissions`, write the module test.
