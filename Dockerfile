# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Build Tesseract and create the final Docker image
FROM openjdk:17.0.1-jdk-slim

# Install dependencies for building Tesseract
RUN apt-get update && apt-get install -y \
    autoconf \
    automake \
    build-essential \
    ca-certificates \
    g++ \
    git \
    libtool \
    pkg-config \
    wget \
    libleptonica-dev \
    && apt-get clean

# Download and build Tesseract 4.0.0
RUN wget https://github.com/tesseract-ocr/tesseract/archive/refs/tags/4.0.0.tar.gz \
    && tar -xzvf 4.0.0.tar.gz \
    && cd tesseract-4.0.0 \
    && ./autogen.sh \
    && ./configure \
    && make \
    && make install \
    && ldconfig

# Download tessdata
RUN mkdir -p /app/tessdata \
    && wget https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata -P /app/tessdata

# Copy the built application
WORKDIR /app
COPY --from=build /app/target/verification_system-0.0.1-SNAPSHOT.jar verification_system.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "verification_system.jar"]
