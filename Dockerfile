# ETAP 1: Budowanie aplikacji (Java + Gradle)
FROM gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# (Naprawia Permission denied)
RUN chmod +x ./gradlew

# 1. Budujemy Backend (tworzy skrypty startowe)
RUN ./gradlew :server:installDist --no-daemon

# 2. Budujemy Frontend (Wasm)
RUN ./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon

# ETAP 2: Uruchomienie (Obraz Eclipse Temurin - działa stabilnie)
FROM eclipse-temurin:21-jre

# folder na aplikację
WORKDIR /app

# Kopiujemy zbudowany serwer z Etapu 1
COPY --from=build /home/gradle/src/server/build/install/server /app/server

# kopiujemy zbudowany frontend z Etapu 1 do folderu static
COPY --from=build /home/gradle/src/composeApp/build/dist/wasmJs/productionExecutable /app/static

# Otwieramy port 8080
EXPOSE 8080

# Uruchamiamy serwer
CMD ["/app/server/bin/server"]