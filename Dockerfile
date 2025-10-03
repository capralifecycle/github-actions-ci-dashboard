FROM azul/zulu-openjdk-alpine:25-jre-headless-latest@sha256:e28e2e7631c5e12651af2208b3070b9067564be8842b1750527f57bcde995ac7

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
