# Liflig REST baseline

Replace any sample values inside `< >` in these files:

- README.md
- [.github/workflows/ci.yaml.template](.github/workflows/ci.yaml.template), and rename to `ci.yaml`
- [.ldp.json](.ldp.json)
- [test-docker.sh](test-docker.sh)
- [Dockerfile](Dockerfile)
- [pom.xml](pom.xml): `<groupId>` `<artifactId>`, `<name>`, `<sonar.projectKey>`
- [src/main/resources/logback.xml](src/main/resources/logback.xml) `<logger name="no.liflig" level="DEBUG"/>`
- [src/main/resources/logback-container.xml](src/main/resources/logback-container.xml) `<logger name="no.liflig" level="DEBUG"/>`
- [src/main/kotlin/no/liflig/baseline/support/observability/OpenTelemetryConfig.kt](src/main/kotlin/no/liflig/baseline/common/observability/OpenTelemetryConfig.kt)
- [src/main/resources-filtered/application.properties](src/main/resources-filtered/application.properties) `service.name`

You might have success with this script: https://gist.github.com/stekern/23e4804c0801520b50c0c3e5b3822138
to replace placeholders.

Refactor the package name to suit your needs.
Then update the [pom.xml](pom.xml)'s package for `MainKt` (`maven-shade-plugin`)

---

# \<Title of the Project>

<!--
Using this README template:
Replace any text <inside tags> with something that suits your project.
Remove any sections that do not fit.
Remove or modify the Badges with the correct links and artifact urls.
Update any visible text or links to Confluence etc. with your details.
Write the appropriate dependencies and steps for getting started.
-->

<!-- Keep/add/modify the badges you want -->
![Java Badge](https://img.shields.io/badge/java-17-blue?logo=java)
![Kotlin Badge](https://img.shields.io/badge/kotlin--blue?logo=kotlin)
[![Build Status](https://github.com/capralifecycle/<repo-name>/actions/workflows/ci.yaml/badge.svg)](https://github.com/capralifecycle/<repo-name>/actions/workflows/ci.yaml)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=capralifecycle_<repo-name>&metric=sqale_index&token=c4c5c941805bfa8cd296947dd001e37c853e4e86)](https://sonarcloud.io/summary/new_code?id=capralifecycle_<repo-name>)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=capralifecycle_<repo-name>&metric=code_smells&token=c4c5c941805bfa8cd296947dd001e37c853e4e86)](https://sonarcloud.io/summary/new_code?id=capralifecycle_<repo-name>)

<!-- Pick a badge that matches how you release your application/lib -->
[![Docker Badge](https://img.shields.io/docker/v/azul/zulu-openjdk-alpine/17-jre-headless)](https://hub.docker.com/layers/azul/zulu-openjdk-alpine/17-jre-headless/images/sha256-fc9db671e88f11569c49f0665506a460a576340d480e5604779a1c404890603d?context=explore)
[![Central Repository](https://img.shields.io/maven-central/v/no.capraconsulting/siren-util?label=release)](https://search.maven.org/search?q=g:no.capraconsulting%20AND%20a:siren-util)
[![NPM Badge](https://img.shields.io/npm/v/@liflig/cdk)](https://www.npmjs.com/package/@liflig/cdk)

Responsible for &lt;transforming source data into a domain model, persisting the data, publish updates to SNS topic for
subscribers and providing APIs for lookup of these entities.>

## Documentation

<!-- Optional links to other pages -->
More information is found here:

<!-- Add links that suits your project. These are just exammples: -->

- [Main confluence page](https://confluence.capraconsulting.no/display/<Customer>/<Service>)
- [API Docs]()
- [Javadocs]()

## Contributing

<!-- If this section gets long, you can use a CONTRIBUTING.md file and link to it here instead. -->

### Getting started

#### Tool dependencies

You need to install:

- Docker
- docker-compose (optional)
- Maven (or run maven through IntelliJ)
- JDK 17
    - `brew tap homebrew/cask-versions` and then`brew install --cask temurin17`

#### Developer machine setup

0. [Authenticate to Github Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) for internal maven
   repos.
1. Create an `overrides.properties` by running
    ```shell
    ./init-local-env.sh
    ```
3. Install ktfmt plugin to IntelliJ

### Running the application

1. Build the jar: `mvn package`
2. Build the docker image with `./test-docker.sh`.
3. Run the app
   - Start `docker-compose`:
      ```shell
      docker-compose -f docker-compose.yml up -d --build
      ```
   - Or run `no.liflig.baseline.Main.main()`
   - Or `cd docker && ./test-docker.sh`

You can test the API with [src/test/http/health.http](src/test/http/health.http)

### Running tests

```shell
mvn verify
```

Add `-DskipTests` to `mvn` to disable all tests.
Add `-DskipITs` to only disable integration tests.

### Linting

Only check lint: `mvn spotless:check`

Fix: `mvn spotless:apply`

### Deploying

Push the master branch.
You can track the progress in [GitHub Actions](https://github.com/capralifecycle/<repo-name>/actions/workflows/ci.yaml)
and in
the [AWS CodePipeline](https://eu-west-1.console.aws.amazon.com/codesuite/codepipeline/pipelines/<CUSTOMER>-apps-prod/view?region=eu-west-1) (`<customer>-build-admin`)
.

## Open Telemetry

You can disable the java agent in ECS by setting the environment parameter `OTEL_JAVAAGENT_ENABLED` to `false`.

You can collect data by attaching a sidecar in ECS with the AWS Distro of Otel Collector: https://aws-otel.github.io/docs/setup/ecs.

## License

&lt;Private project. No reuse.>
<!-- Or -->
Apache 2.0, see [LICENSE](./LICENSE).
<!-- Or this, where you update year, and perhaps add any customer that wanted this proejct to Copyright holder -->

```text
Copyright 2022 Liflig By Capra AS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
