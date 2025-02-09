FROM amazoncorretto:17-alpine

WORKDIR /spring

COPY target/Todo-api-0.0.1-SNAPSHOT.jar ./todo-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "todo-api.jar" , "--spring.profiles.active=prod" ]
