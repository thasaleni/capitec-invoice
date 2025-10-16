# Multi-stage Dockerfile to build and run the Capitec Invoice App

# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy the entire project (simple + reliable for CI); Gradle wrapper included
COPY . .

# Ensure wrapper is executable
RUN chmod +x ./gradlew

# Build the Spring Boot jar (skip tests for faster image builds)
RUN ./gradlew --no-daemon bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/invoice-0.0.1-SNAPSHOT.jar app.jar

# App listens on 8080 by default
EXPOSE 8080

# Allow passing extra JVM args via JAVA_OPTS (e.g., -Xms256m -Xmx512m)
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
