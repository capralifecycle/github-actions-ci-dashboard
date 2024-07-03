#!/bin/bash

set -e

SERVICE_NAME="<Service>"
GIT_COMMIT_HASH="$(git rev-parse HEAD)"

if test ! -d "./target"; then
  mvn package -DskipTests
fi

docker build \
  --platform linux/arm64/v8 \
  --build-arg service_version=$GIT_COMMIT_HASH \
  -t $SERVICE_NAME .

docker run -it --rm -p 8080:8080 $SERVICE_NAME
