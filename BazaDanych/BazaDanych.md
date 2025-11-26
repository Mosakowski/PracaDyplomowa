# 🏟️ Baza Danych Systemu

Projekt relacyjnej bazy danych zaprojektowany do kompleksowej obsługi systemu rezerwacji boisk i obiektów sportowych. System obsługuje hierarchię obiektów, różnorodne role użytkowników, elastyczne harmonogramy oraz pełny proces płatności i zwrotów.

## 🖼️ Diagram ERD

## 💾 Struktura Bazy Danych

### 1. Zarządzanie Użytkownikami (`User`)
Centralna tabela uwierzytelniania. System rozróżnia trzy poziomy dostępu za pomocą typu wyliczeniowego:
* **Admin:** Zarządza całym systemem.
* **Field_Owner:** Dodaje obiekty i zarządza rezerwacjami.
* **Client:** Rezerwuje i opłaca terminy.
> **Kluczowe pola:** `email` (Unique), `user_type` (Enum).

### 2. Hierarchia Obiektów (`Facility` & `Field`)
Model zastosowuje relację *Jeden-do-Wielu* (One-to-Many). Jeden fizyczny obiekt (np. "Kompleks Orlik") może posiadać wiele boisk (np. "Boisko do piłki nożnej", "Kort A").

* **Facility:** Przechowuje dane adresowe i opisowe głównego obiektu.
* **Field:** Definiuje konkretne zasoby do wynajęcia.
    * Wykorzystuje **JSONB** (`hours`, `exceptions`) do elastycznego definiowania godzin otwarcia (np. różne godziny w weekendy).
    * Cennik zdefiniowany jako `price_per_slot`.

### 3. Proces Rezerwacji (`Booking`)
Tabela łącząca użytkownika z konkretnym boiskiem w danym przedziale czasowym.
* Obsługuje pełny cykl życia rezerwacji: `waiting` ➝ `active` ➝ `completed` (lub `cancelled`).
* Zawiera walidację dat (`start` i `end`).

### 4. Finanse (`Payment` & `Cancellation`)
Moduł finansowy został wydzielony dla zachowania czystości danych księgowych.
* **Payment:** Rejestruje każdą transakcję, jej metodę oraz status (np. `paid`, `failed`).
* **Cancellation:** Tabela dedykowana do obsługi anulacji, przechowująca status oraz – co kluczowe – `refund_amount` (kwotę zwrotu), co pozwala na obsługę częściowych zwrotów.

## ✨ Kluczowe Funkcjonalności i Decyzje Projektowe

| Funkcja | Opis Techniczny | Korzyść Biznesowa |
| :--- | :--- | :--- |
| **Elastyczny Grafik** | Kolumna `hours` typu `JSONB` w tabeli `Field`. | Możliwość definiowania niestandardowych godzin otwarcia bez tworzenia skomplikowanych tabel pomocniczych. |
| **Bezpieczeństwo Finansowe** | Typ `NUMERIC` dla `price_per_slot`, `amount` i `refund_amount`. | Uniknięcie błędów zaokrągleń (floating point errors) typowych dla obliczeń walutowych. |
| **Audytowalność** | Timestampy `created_at` w każdej tabeli. | Pełna historia utworzenia każdego rekordu w systemie. |
| **Typy Wyliczeniowe** | Szerokie zastosowanie `ENUM` (np. dla statusów). | Zapewnienie spójności danych i ochrona przed wprowadzeniem błędnych statusów. |

## 🛠️ Technologie
Projekt jest zoptymalizowany pod silniki SQL wspierające typy JSON oraz ENUM, w szczególności:
* **PostgreSQL** (Rekomendowany ze względu na wydajność JSONB)
