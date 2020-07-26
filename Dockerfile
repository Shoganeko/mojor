FROM gradle:jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:8-jre-slim

EXPOSE 8077

RUN mkdir /mojor

COPY --from=build /home/gradle/src/build/libs/*.jar /mojor/mojor.jar

ENTRYPOINT ["java", "-jar", "/mojor/mojor.jar"]