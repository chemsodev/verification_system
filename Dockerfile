# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image
FROM openjdk:17.0.1-jdk-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    libleptonica-dev \
    locales \
    wget \
    && apt-get clean

# Generate locales
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8  

# Create the tessdata directory and download the English trained data
RUN mkdir -p /usr/share/tessdata \
    && wget https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata -P /usr/share/tessdata

# Set the TESSDATA_PREFIX environment variable
ENV TESSDATA_PREFIX /usr/share/tessdata/

# Copy the built application
WORKDIR /app
COPY --from=build /app/target/verification_system-0.0.1-SNAPSHOT.jar verification_system.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "verification_system.jar"]
