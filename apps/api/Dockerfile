# syntax=docker/dockerfile:1.7
# ---- Build stage ----
# Maven base image (no committed wrapper required). Switch to ./mvnw after `mvn -N wrapper:wrapper`.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests clean package \
    && cp target/*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# ---- Runtime stage (distroless, non-root) ----
FROM gcr.io/distroless/java21-debian12:nonroot AS runtime
WORKDIR /app
# Layered copy for better cache reuse
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./
USER nonroot
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseZGC -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
