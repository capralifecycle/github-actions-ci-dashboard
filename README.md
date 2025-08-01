<h1 align="center">

![logo](logo.svg)
<br>
  GitHub Actions CI-Dashboard
<br>
</h1>

![Java Badge](https://img.shields.io/badge/java-17-blue?logo=java)
![Kotlin Badge](https://img.shields.io/badge/kotlin--blue?logo=kotlin)
[![Build Status](https://github.com/capralifecycle/github-actions-ci-dashboard/actions/workflows/ci.yaml/badge.svg)](https://github.com/capralifecycle/github-actions-ci-dashboard/actions/workflows/ci.yaml)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=capralifecycle_github-actions-ci-dashboard&metric=sqale_index&token=c098b4d25bf2f8a05ee55cb9aeb4b84eb1329689)](https://sonarcloud.io/summary/new_code?id=capralifecycle_github-actions-ci-dashboard)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=capralifecycle_github-actions-ci-dashboard&metric=code_smells&token=c098b4d25bf2f8a05ee55cb9aeb4b84eb1329689)](https://sonarcloud.io/summary/new_code?id=capralifecycle_github-actions-ci-dashboard)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=capralifecycle_github-actions-ci-dashboard&metric=coverage&token=c098b4d25bf2f8a05ee55cb9aeb4b84eb1329689)](https://sonarcloud.io/summary/new_code?id=capralifecycle_github-actions-ci-dashboard)



Responsible for collecting GitHub Actions `workflow_run` webhook data and providing a web dashboard with recent CI build
status. Put this on a TV in your office.

![Screenshot](docs/dashboard-screenshot.png)

## Documentation

**Usage:**
- [Webhooks and secrets](docs/webhooks-and-secrets.md)
- [Adding configs to show specific repos on dashboards](docs/admin-config.md)
- [Xbar for local machine](docs/Xbar-plugin.md), useful for remote working employees without dedicated dashboard monitors.
- [Deleting CI statuses](docs/delete-endpoint.md) to clean up statuses you don't want.

**Development:**
- [Requirements (Norwegian)](./docs/requirements.md)
- [Solution specification/architecture (Confluence)](https://liflig.atlassian.net/l/cp/Qc1oFmJF)
- [Overview of the design process (Confluence)](https://liflig.atlassian.net/wiki/x/PgBSDg)

### Architecture

![AWS infra architecture](./docs/infrastructure-architecture.png)

## Contributing

### Getting started

#### Tool dependencies

You need to install:

- Docker
- Maven (or run maven through IntelliJ)
- JDK 17
  - `brew tap homebrew/cask-versions` and then`brew install --cask temurin17`

#### Developer machine setup

0. [Authenticate to Github Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
   for internal maven
   repos.
1. Create an `overrides.properties` by running
    ```shell
    ./init-local-env.sh
    ```
3. Install ktfmt plugin to IntelliJ

### Running the application

1. Start the `db` in [docker-compose.yml](./docker-compose.yml)
2. Run the [main method from intelliJ](./src/main/kotlin/no/liflig/cidashboard/Main.kt)
3. Watch the logs for which port is being used.
4. Visit `http://localhost:PORT/?token=TOKEN_HERE`

You can test the API
with [src/test/http/health.http](src/test/http/health.http), [webhook.http](src/test/http/webhook.http).

### Running tests

```shell
mvn verify
```

- Add `-DskipTests` to `mvn` to disable all tests.
- Add `-DskipITs` to only disable integration tests.
- Add `-DREGENERATE_FAILED_SNAPSHOTS=true` to update snapshot tests.

### Linting

Only check lint: `mvn spotless:check`

Fix: `mvn spotless:apply`

### Writing CSS

1. Make sure you run [init-local-env.sh](./init-local-env.sh) to get hot-reload enabled.
2. There is a "test" called [DevelopmentAid](./src/test/kotlin/acceptancetests/DevelopmentAid.kt) that you can start to
open a website preloaded with data.
3. Then modify the css in [index.hbs](./src/main/resources/handlebars-htmx-templates/index.hbs).
4. Refresh the page.

### Deploying

Push the master branch.
You can track the progress
in [GitHub Actions](https://github.com/capralifecycle/github-actions-ci-dashboard/actions/workflows/ci.yaml)
and in
the [AWS CodePipeline](https://eu-west-1.console.aws.amazon.com/codesuite/codepipeline/pipelines/experiments-apps/view?region=eu-west-1) (`liflig-experiments`).

## License

```text
Copyright 2025 Liflig AS (org.nr. 925906093)

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
