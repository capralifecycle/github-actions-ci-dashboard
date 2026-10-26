FROM azul-zulu:25.38-25.0.5.0.3-jre-headless-alpine3.23@sha256:80968ad76f60a44859a045c66ddd726a2a9f6b928015ad726cb1119323d338aa

RUN apk add --update --no-cache bind-tools
RUN set -eux; \
    adduser -S app

RUN mkdir -p -m 777 /tmp/heapdump
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80.0 -XX:InitialRAMPercentage=25.0 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump"

EXPOSE 8080

WORKDIR /

COPY ./target/app.jar /app.jar

USER app

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
