# Use Eclipse Temurin JDK 17 as base (adjust if pom.xml uses a different version)
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build the application using Maven wrapper
RUN ./mvnw clean package -DskipTests

# Use a smaller JDK image for running
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]