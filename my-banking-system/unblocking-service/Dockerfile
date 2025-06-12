FROM openjdk:21-slim-buster
WORKDIR /app
COPY target/unblocking-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]