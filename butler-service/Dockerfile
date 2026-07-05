FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle
COPY src src
# Rename to a fixed filename so this Dockerfile doesn't need editing every time
# build.gradle's `version` changes — a stale hardcoded jar name here (0.3.0 vs
# the 0.4.0 the build actually produced) broke every deploy since that bump.
RUN ./gradlew test bootJar --no-daemon && mv build/libs/*.jar build/libs/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV PORT=8080
COPY --from=build /workspace/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
