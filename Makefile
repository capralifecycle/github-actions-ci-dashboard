.PHONY: all build
all: build

build:
	mvn -B -U verify
	docker build .
