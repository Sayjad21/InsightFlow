# =========================
# Stage 1: Build the JAR
# =========================
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the JAR (skip tests for faster build)
RUN ./mvnw package -DskipTests

# =========================
# Stage 2: Runtime
# =========================
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/insightflow-0.0.1-SNAPSHOT.jar app.jar

# Expose the port used by Spring Boot
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
