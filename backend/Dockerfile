# Stage 1: Build the JAR using system Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Install font libraries and system dependencies needed for chart generation and Selenium
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    freetype \
    chromium \
    chromium-chromedriver \
    nss \
    ca-certificates

# Create a non-root user for running Chrome (security best practice)
RUN adduser -D -s /bin/sh chromium-user

# Set Chrome binary path for Selenium
ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROME_DRIVER=/usr/bin/chromedriver
ENV DISPLAY=:99

# Make sure ChromeDriver is executable
RUN chmod +x /usr/bin/chromedriver

# Copy application
COPY --from=build /app/target/insightflow-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000

# Set JVM memory limits for container environment
ENV JAVA_OPTS="-Xms256m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
