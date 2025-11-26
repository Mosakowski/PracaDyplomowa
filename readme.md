# System rezerwacji boisk sportowych

Multiplatformowa aplikacja do zarządzania i rezerwowania boisk sportowych. Projekt został zbudowany w Kotlin Multiplatform, dzięki czemu umożliwia współdzielenie logiki między Androidem, iOS, Webem oraz serwerem backendowym.

---

## 🎯 Funkcje

- Przegląd dostępnych boisk
- Rezerwacja wybranego terminu
- Obsługa użytkowników i administratorów
- Działający backend REST API w Ktor
- Interfejs na Androida, iOS i Web

---

## 🛠️ Technologie

| Obszar        | Technologia               |
|---------------|----------------------------|
| Język         | Kotlin Multiplatform       |
| Backend       | Ktor, Exposed ORM          |
| Baza danych   | PostgreSQL                 |
| Frontend Web  | Compose Multiplatform JS   |
| Android       | Jetpack Compose            |
| iOS           | UIKit (via iosApp)         |
| Build system  | Gradle                     |
| IDE           | IntelliJ IDEA, Android Studio |
| JSON          | kotlinx.serialization      |
| Inne          | HikariCP, CORS, REST API   |

---

## 🚀 Uruchomienie lokalne

1. Skonfiguruj JDK (zalecany OpenJDK 21 lub wyżej)
2. Uruchom serwer Ktor:  
   ```bash
   ./gradlew run
