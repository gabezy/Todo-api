FROM maven:3-amazoncorretto-17-alpine AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package

FROM amazoncorretto:17-alpine

WORKDIR /spring

COPY --from=build /app/target/Todo-api-0.0.1-SNAPSHOT.jar ./todo-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "todo-api.jar" ]
