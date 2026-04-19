# Stage 1: Build (Maven + Node via frontend-maven-plugin)
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache Maven dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
COPY frontend ./frontend
RUN mvn package -DskipTests -q

# Stage 2: Runtime (JRE only)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /build/target/tcpforwarder-*.jar app.jar

# Rules and logs live on volumes, persisted across container restarts
VOLUME /app/data
VOLUME /app/logs

ENV FORWARDER_API_KEY=changeme
ENV FORWARDER_RULES_FILE=/app/data/rules.yml
ENV FORWARDER_AUDIT_LOG_FILE=/app/logs/audit.log

EXPOSE 8080
# NOTE: TCP forwarding ports (e.g. -p 5433:5433) must be added at docker run time
# since they are defined by runtime rules, not statically known at build time.

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
