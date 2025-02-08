FROM maven:3.8.5-openjdk-17 AS build
COPY /src /src
COPY pom.xml /
RUN mvn -f /pom.xml clean package
RUN mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 -Dmapstruct.processor=org.mapstruct.ap.MappingProcessor


FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar application.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "application.jar"]