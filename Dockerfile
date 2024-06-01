# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image
FROM openjdk:17.0.1-jdk-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    locales \
    wget \
    && apt-get clean

# Generate locales
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8  

# Copy the Spring Boot application jar file from the build stage
WORKDIR /app
COPY --from=build /app/target/verification_system-0.0.1-SNAPSHOT.jar .

# Copy the Tesseract OCR English trained data file
COPY src/main/resources/tessdata/eng.traineddata /app/tessdata/eng.traineddata

# Set the TESSDATA_PREFIX environment variable
ENV TESSDATA_PREFIX=/app/tessdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "verification_system-0.0.1-SNAPSHOT.jar"]
