FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY . .
RUN ./gradlew clean build -x check -x test -Pproduction

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "app.jar"]
