# syntax=docker/dockerfile:1.7
# ---- Build stage ----
# Maven base image (no committed wrapper required). Switch to ./mvnw after `mvn -N wrapper:wrapper`.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests clean package && cp target/*.jar app.jar

# ---- Runtime stage (distroless, non-root) ----
FROM gcr.io/distroless/java21-debian12:nonroot AS runtime
WORKDIR /app
# Run the repackaged fat jar directly. (A layered-jar JarLauncher setup is the optimization for
# image-cache reuse, but it must lay out BOOT-INF correctly; the plain fat jar is simpler + robust.)
COPY --from=build /workspace/app.jar ./app.jar
USER nonroot
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseZGC -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "-jar", "app.jar"]
