# Multi-stage Dockerfile for Spring Boot 3.5.5 + Java 21 + Maven

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

# Copy Maven wrapper and pom.xml first (for dependency caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this layer is cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Extract JAR layers for optimized Docker layer caching
RUN mkdir -p target/extracted && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring

WORKDIR /app

# Copy extracted layers from build stage (order matters for caching)
COPY --from=build --chown=spring:spring /build/target/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /build/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /build/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /build/target/extracted/application/ ./

# Switch to non-root user
USER spring:spring

# Expose port 8080 (standard internal container port)
EXPOSE 8080

# JVM settings optimized for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Health check using actuator endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entrypoint to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]