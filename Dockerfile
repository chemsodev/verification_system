# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the Docker image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Install Tesseract OCR
RUN apt-get update && apt-get install -y tesseract-ocr && apt-get clean

# Copy the built application JAR from the build stage
COPY --from=build /app/target/verification_system-0.0.1-SNAPSHOT.jar verification_system.jar

# Copy Tesseract data files if needed
COPY src/main/resources/tessdata /app/tessdata

# Set environment variables for Tesseract
ENV TESSDATA_PREFIX=/app/tessdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "verification_system.jar"]
