# Backend Dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the jar (make sure you build it first)
COPY target/*.jar app.jar

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
