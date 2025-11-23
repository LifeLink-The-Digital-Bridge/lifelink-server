# Stage 1: Build stage using Maven image
FROM maven:3.9-eclipse-temurin-21-jammy AS build

WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /build/target/*.jar app.jar

# Create non-root user for security
RUN useradd -m -u 1001 -s /bin/bash appuser && \
    chown -R appuser:appuser /app

USER appuser

# Expose port for REST API and WebSocket
EXPOSE 8086

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
