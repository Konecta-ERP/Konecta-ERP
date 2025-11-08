# === STAGE 1: Build the Application ===
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -pl recruitment-service -am -DskipTests

# === STAGE 2: Extract the Layered JAR ===
FROM eclipse-temurin:17-jre-focal AS extractor
WORKDIR /app
COPY --from=builder /build/recruitment-service/target/recruitment-service-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# === STAGE 3: Build the Final Production Image ===
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

COPY --from=extractor /app/dependencies/ .
COPY --from=extractor /app/spring-boot-loader/ .
COPY --from=extractor /app/snapshot-dependencies/ .
COPY --from=extractor /app/application/ .

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]