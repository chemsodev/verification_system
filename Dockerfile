# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the Docker image
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/target/verification_system-0.0.1-SNAPSHOT.jar verification_system.jar

# Copy the tessdata directory
COPY src/main/resources/tessdata /app/tessdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "verification_system.jar"]
