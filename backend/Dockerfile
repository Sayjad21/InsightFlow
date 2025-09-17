# Use official Java runtime image
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file (adjust jar name as per your build)
COPY target\insightflow-0.0.1-SNAPSHOT.jar app.jar

# Expose port, must match spring.server.port
EXPOSE 8000

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
