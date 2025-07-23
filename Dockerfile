FROM azul/zulu-openjdk-alpine:21-jre-headless-latest@sha256:3310c6029290da76c09c9be6e73181d062700c5d82c4ee2a37030e8e6269f234

RUN apk add --update --no-cache bind-tools
RUN set -eux; \
    adduser -S app

RUN mkdir -p -m 777 /tmp/heapdump
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80.0 -XX:InitialRAMPercentage=25.0 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump"

EXPOSE 8080

USER app
WORKDIR /

COPY ./target/app.jar /app.jar
CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
