FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle
COPY src src
RUN ./gradlew test bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV PORT=8080
COPY --from=build /workspace/build/libs/butler-service-0.3.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
