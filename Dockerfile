FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sh mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src
RUN sh mvnw --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S aplicacao && adduser -S aplicacao -G aplicacao
WORKDIR /aplicacao
COPY --from=build --chown=aplicacao:aplicacao /workspace/target/service-scheduling-api-*.jar aplicacao.jar

USER aplicacao
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/aplicacao/aplicacao.jar"]
