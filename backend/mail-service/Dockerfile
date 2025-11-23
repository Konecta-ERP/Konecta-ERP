# === STAGE 1: Build the Application ===
# Use a Maven & Java 17 image
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set the working directory
WORKDIR /build

# Copy the entire multi-module project.
# This requires a .dockerignore file in the project ROOT.
COPY . .

# Build *only* the mail-service and its dependencies (-am)
# We use 'package' (not 'install') and skip tests for a faster build.
RUN mvn clean package -pl mail-service -am -DskipTests

# === STAGE 2: Extract the Layered JAR ===
# Use a slim JRE image to extract the Spring Boot layers
FROM eclipse-temurin:17-jre-focal AS extractor

WORKDIR /app

# Copy the built JAR from the builder stage
# The artifactId is 'mail-service' and version is '0.0.1-SNAPSHOT'
COPY --from=builder /build/mail-service/target/mail-service-0.0.1-SNAPSHOT.jar app.jar

# Extract the layers
RUN java -Djarmode=layertools -jar app.jar extract

# === STAGE 3: Build the Final Production Image ===
# Use the slim, official JRE image
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Set the active Spring profile to 'docker'
# This will make Spring Boot use your 'application-docker.properties'
ENV SPRING_PROFILES_ACTIVE=docker

# Expose the port defined in your docker properties
EXPOSE 8080

# JVM settings optimized for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Copy the extracted layers from the extractor stage
# This order is important for Docker's layer caching
COPY --from=extractor /app/dependencies/ .
COPY --from=extractor /app/spring-boot-loader/ .
COPY --from=extractor /app/snapshot-dependencies/ .
COPY --from=extractor /app/application/ .

# The entrypoint is different when using layers
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]