#!/bin/bash

set -e

SERVICE_NAME="github-actions-ci-dashboard"

if test ! -d "./target"; then
  mvn package -DskipTests
fi

docker build \
  --platform linux/arm64/v8 \
  -t $SERVICE_NAME .

docker run -it --rm -p 8080:8080 $SERVICE_NAME
