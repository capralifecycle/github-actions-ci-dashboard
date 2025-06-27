# app
APP_IMAGE_NAME = github-actions-ci-dashboard
APP_IMAGE_TAG  = latest

# db
DB_IMAGE_NAME = postgres
DB_IMAGE_TAG  = latest
DB_PORTS = "5432:5432"
DB_USER = user
DB_PASSWORD = password

.PHONY: all
all: build

.PHONY: build
build: maven-build docker-build

.PHONY: maven-build
maven-build:
	mvn clean verify

.PHONY: docker-build
docker-build:
	docker build -t $(APP_IMAGE_NAME):$(APP_IMAGE_TAG) .

.PHONY: clean
clean:
	mvn clean

.PHONY: run-jar
run-jar:
	java -jar target/app.jar

.PHONY: start-postgres
start-postgres:
	docker run --name some-postgres \
	-e POSTGRES_PASSWORD=$(DB_PASSWORD) \
	-e POSTGRES_USER=$(DB_USER) \
		-e POSTGRES_DB=app \
		-p $(DB_PORTS) \
		-d $(DB_IMAGE_NAME):$(DB_IMAGE_TAG)

.PHONY: stop-postgres
stop-postgres:
	docker rm --force some-postgres
