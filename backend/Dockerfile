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
    freetype-dev \
    chromium \
    chromium-chromedriver \
    wget \
    unzip \
    libxrender \
    libxtst


# Set Chrome binary path for Selenium
ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROME_DRIVER=/usr/bin/chromedriver

COPY --from=build /app/target/insightflow-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
