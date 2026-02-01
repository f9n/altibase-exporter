# Development

Local build and run (no Gradle installation required; the project includes a wrapper).

## Prerequisites

- **Java 25** — e.g. `brew install openjdk@25` (macOS) or [Eclipse Temurin 25](https://adoptium.net/)
- Set `JAVA_HOME` or `PATH` so `java` is Java 25.

## Build

From the project root:

```bash
./gradlew shadowJar
```

Output: `build/libs/altibase-exporter.jar` (fat JAR with dependencies).

## Test

From the project root:

```bash
./gradlew test
```

Uses JUnit 5 (Jupiter). Test sources: `src/test/java/`. Add test classes under `com.f9n.altibase.exporter` (or mirror `src/main/java`). CI (`.github/workflows/ci.yml`) runs tests on every push and PR.

## Run locally

**Option A — use JAR from Releases:** download `altibase-exporter.jar` from [Releases](https://github.com/f9n/altibase-exporter/releases), then:

```bash
export ALTIBASE_SERVER=<host>
export ALTIBASE_PORT=<port>
export ALTIBASE_USER=<user>
export ALTIBASE_PASSWORD=<password>
java -jar altibase-exporter.jar
```

**Option B — build from source:** after `./gradlew shadowJar`:

```bash
export ALTIBASE_SERVER=<host>
export ALTIBASE_PORT=<port>
export ALTIBASE_USER=<user>
export ALTIBASE_PASSWORD=<password>
java -jar build/libs/altibase-exporter.jar
```

Or with Gradle (no JAR build needed):

```bash
export ALTIBASE_SERVER=<host> ALTIBASE_PORT=<port> ALTIBASE_USER=<user> ALTIBASE_PASSWORD=<password>
./gradlew run
```

Or with command-line flags:

```bash
java -jar build/libs/altibase-exporter.jar \
  -altibase.server=<host> \
  -altibase.port=<port> \
  -altibase.user=<user> \
  -altibase.password=<password>
```

On success you will see a JSON log line: `{"@timestamp":"...","level":"INFO","message":"Altibase exporter listening: port=9399 jdbc=...","logger_name":"...","service":"altibase-exporter"}`.

Optional: set `LOG_LEVEL=DEBUG` (or `WARN`, `ERROR`) for more or less verbosity.

Metrics: `http://localhost:9399/metrics`. Root `http://localhost:9399/` returns a simple HTML page with a link to metrics.

## Releases

When a **tag** is pushed (e.g. `v1.0.0`), GitHub Actions (`.github/workflows/release.yml`) runs:

1. **JAR** — Builds `altibase-exporter.jar` and attaches it to a GitHub Release for that tag.
2. **Docker** — Builds and pushes the image to [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry): `ghcr.io/<owner>/altibase-exporter:<tag>` (e.g. `ghcr.io/f9n/altibase-exporter:v1.0.0`).

To create a release: push a tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`). Then download the JAR from the Releases page or pull the image: `docker pull ghcr.io/f9n/altibase-exporter:v1.0.0`.
