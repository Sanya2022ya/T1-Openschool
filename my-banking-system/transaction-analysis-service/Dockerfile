FROM openjdk:21-slim-buster
WORKDIR /app
COPY target/transaction-analysis-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]