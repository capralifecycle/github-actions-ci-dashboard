# Development

## Patching

### Updating Java version

When updating the Java version, there are several places that must be updated:

- The base image in the [Dockerfile](./Dockerfile)
- The `<java.version>` and `<kotlin.compiler.jvmTarget>` in the [pom.xml](./pom.xml)
- The java version in the `setup-java`-action in [.github/workflows/ci.yaml](./.github/workflows/ci.yaml)
- The development JDK version in [.tool-versions](./.tool-versions)
