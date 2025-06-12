FROM openjdk:21-slim-buster
WORKDIR /app
COPY target/t1project-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]