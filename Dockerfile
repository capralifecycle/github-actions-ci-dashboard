FROM azul/zulu-openjdk-alpine:21-jre-headless-latest@sha256:2c2eee6e43e5aaabb803661cde4c457adcc55878f2be7c80eda91bbdc9286165

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
