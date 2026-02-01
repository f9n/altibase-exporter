FROM gradle:9.3-jdk25 AS builder
WORKDIR /build
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle shadowJar --no-daemon -q

FROM eclipse-temurin:25-jre-jammy AS runtime
WORKDIR /app
COPY --from=builder /build/build/libs/altibase-exporter.jar /app/
EXPOSE 9399
ENV WEB_LISTEN_PORT=9399
ENTRYPOINT ["java", "-jar", "/app/altibase-exporter.jar"]
