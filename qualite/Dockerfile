# Etape 1: Build l'application b Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# N'skippiw tests bach l'build ydoz bzrba
RUN mvn clean package -DskipTests

# Etape 2: Run l'application b Java 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 7623
ENTRYPOINT ["java", "-jar", "app.jar"]