FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/verification_system-0.0.1-SNAPSHOT.jar verification_system.jar
COPY src/main/resources/tessdata /app/tessdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "verification_system.jar"]
