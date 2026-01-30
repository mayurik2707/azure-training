# Simple runtime image for Spring Boot fat JAR
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY target/azure-sql-java-demo-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]