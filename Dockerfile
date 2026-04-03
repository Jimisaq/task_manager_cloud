FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "app.jar"]