FROM azul/zulu-openjdk-alpine:17.0.10-jre-headless@sha256:74ea999488bd46602b29191c73c238cbdf6925700d30755f077372aca858923b

RUN apk add --update --no-cache bind-tools
RUN set -eux; \
    adduser -S app

RUN mkdir -p -m 777 /tmp/heapdump
ENV JAVA_TOOL_OPTIONS="-javaagent:/aws-opentelemetry-agent.jar -XX:MaxRAMPercentage=80.0 -XX:InitialRAMPercentage=25.0 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump"

EXPOSE 8080

# Adds openTelemetry metrics and tracing: https://aws-otel.github.io/docs/getting-started/java-sdk/trace-auto-instr
# REQUIRES YOU TO ADD AN OTEL COLLECTOR SIDECAR IN AWS CDK!
ENV OTEL_TRACES_SAMPLER parentbased_traceidratio
ENV OTEL_TRACES_SAMPLER_ARG 0.1
# Change if you are not using xray tracing and let xray generate Trace IDs
ENV OTEL_PROPAGATORS xray
ENV OTEL_METRICS_EXPORTER otlp
ENV OTEL_EXPORTER_OTLP_ENDPOINT http://127.0.0.1:4317
ENV OTEL_EXPORTER_OTLP_COMPRESSION gzip
ENV OTEL_METRIC_EXPORT_INTERVAL 15000
ENV OTEL_LOGS_EXPORTER none
# Disables the entire agent. Use your AWS ECS config to enable the agent by overriding this to true
ENV OTEL_JAVAAGENT_ENABLED false

ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/download/v1.31.1/aws-opentelemetry-agent.jar /aws-opentelemetry-agent.jar
RUN chmod 777 /aws-opentelemetry-agent.jar

ARG service_name=<ServiceName>
ARG service_namespace=<customer-or-service-namespace>
ARG service_version=git_commit_hash
ENV OTEL_RESOURCE_ATTRIBUTES service.name=$service_name,service.namespace=$service_namespace,service.version=$service_version,service=$service_name,version=$service_version

USER app
WORKDIR /

COPY ./target/app.jar /app.jar
CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
