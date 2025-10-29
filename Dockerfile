FROM azul/zulu-openjdk-alpine:25-jre-headless-latest@sha256:abe5afdb569c34e02e75546266b2d03c703f19697f15753c7f17b776e6661b18

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
