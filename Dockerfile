# ETAP 1: Budowanie aplikacji (Java + Gradle)
FROM gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Dajemy Gradle'owi więcej pamięci, żeby kompilator WASM nie padł
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx3g -Dkotlin.daemon.jvm.options=-Xmx3g -Dkotlin.compiler.execution.strategy=in-process"

# Nadajemy uprawnienia (to już masz, ale zostawiamy)
RUN chmod +x ./gradlew

# 1. Budujemy Backend
RUN ./gradlew :server:installDist --no-daemon

# 2. Budujemy Frontend (Wasm)
RUN ./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon

# ETAP 2: Uruchomienie (Eclipse Temurin)
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /home/gradle/src/server/build/install/server /app/server
COPY --from=build /home/gradle/src/composeApp/build/dist/wasmJs/productionExecutable /app/static

EXPOSE 8080

CMD ["/app/server/bin/server"]