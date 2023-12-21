FROM eclipse-temurin:21-jre-jammy
LABEL maintainer="mslxl <i@mslxl.com>"

ADD build/libs/provlegisto-tracker-0.0.1-SNAPSHOT.jar provlegisto.jar
ENTRYPOINT java -jar provlegisto
EXPOSE 8080