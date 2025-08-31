FROM eclipse-temurin:17-jdk-alpine

ENV JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
