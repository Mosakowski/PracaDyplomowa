# Scenariusze Przypadków Użycia #

## Przypadek użycia: Zarejestruj się

**Aktorzy:** Klient, Właściciel Boiska  
**Opis:** Użytkownik się Zarejestrować.  
**Rezultat**: Użytkownik Zarejestrowany.
**Warunki wstępne:** Użytkownik jest niezalogowany.
**Warunki końcowe:** Użytkownik zarejestrowany.

### Główny scenariusz:
1. System wyświetla panel rejestracji.
2. Użytkownik podaje dane i potwierdza.
3. System wyświetla potwierdzenie rejestracji.

### Scenariusze alternatywne:
**A1 – Użytkownik podał nieprawidłowy login**  
W kroku 2 Użytkownik podał nieprawidłowy login, system wyświetla informacje i wraca do kroku 1.

**A2 – Użytkownik podał nieprawidłowe hasło**  
W kroku 2 Użytkownik podał nieprawidłowe hasło, system wyświetla informacje i wraca do kroku 1.

**A3 – Użytkownik podał nieprawidłowy adres email**  
W kroku 2 Użytkownik podał nieprawidłowy adres email, system wyświetla informacje i wraca do kroku 1.


## Przypadek użycia: Zaloguj się

**Aktorzy:** Klient, Właściciel Boiska  
**Opis:** Użytkownik się zalogować.  
**Rezultat**: Użytkownik zalogowany.
**Warunki wstępne:** Użytkownik jest niezalogowany.
**Warunki końcowe:** Użytkownik Zalogowany.

### Główny scenariusz:
1. System wyświetla panel logowania.
2. Użytkownik podaje dane i potwierdza.

### Scenariusze alternatywne:
**A1 – Użytkownik podał nieprawidłowy login**  
W kroku 2 Użytkownik podał nieprawidłowy login, system wyświetla informacje i wraca do kroku 1.

**A2 – Użytkownik podał nieprawidłowe hasło**  
W kroku 2 Użytkownik podał nieprawidłowe hasło, system wyświetla informacje i wraca do kroku 1.

**A3 – Użytkownik podał nieprawidłowy adres email**  
W kroku 2 Użytkownik podał nieprawidłowy adres email, system wyświetla informacje i wraca do kroku 1.


## Przypadek użycia: Sprawdź dostępnośc Boiska

**Aktorzy:** Klient, Właściciel Boiska  
**Opis:** Użytkownik chce przejrzeć dostępność Boiska.
**Rezultat**: System wyświetla Użytkownikowi dostępność boisk.  
**Warunki wstępne:** Brak.    
**Warunki końcowe:** Dostępność boisk wyświetlona.

### Główny scenariusz:
1. Użytkownik wybiera Obiekt Sportowy.
2. System wyświetla dostępne terminy i boiska dla Obiektu Sportowego.


## Przypadek użycia: Zarezerwuj Boisko Extends Sprawdź dostępność Boiska

**Aktorzy:** Klient, System Płatności, Właściciel Boiska  
**Opis:** Użytkownik chce zarezerwować boisko.  
**Rezultat**: Użytkownik zarezerwował boisko.
**Warunki wstępne:** Użytkownik jest zalogowany.  
**Warunki końcowe:** Rezerwacja utworzona.

### Główny scenariusz:
1. Użytkownik wybiera Obiekt Sportowy.
2. System wyświetla dostępne terminy i boiska dla Obiektu Sportowego.
3. Użytkownik wybiera boisko i termin, oraz potwierdza rezerwację.
4. System rozpoczyna proces płatności.
5. Użytkownik płaci w Systemie Płatności.
6. System zapisuje rezerwację i pokazuje potwierdzenie.

### Scenariusze alternatywne:
**A1 – Termin zajęty**  
W kroku 3 system informuje o braku dostępności i wraca do wyboru terminu.

**A2 – Błąd płatności**  
W kroku 5 system informuje o błędzie i wraca do kroku 4.


## Przypadek użycia: Sprawdź historię rezerwacji

**Aktorzy:** Klient, Właściciel Boiska  
**Opis:** Użytkownik chce zobaczyć swoje rezerwację.
**Warunki wstępne:** Użytkownik jest zalogowany.  
**Warunki końcowe:** Historia rezerwacji wyświetlona.

### Główny scenariusz:
1. System wyświetla historię rezerwacji.


## Przypadek użycia: Anuluj Rezerwację Extends Sprawdź historię rezerwacji

**Aktorzy:** Klient, Właściciel Boiska  
**Opis:** Użytkownik chce anulować rezerwację.  
**Rezultat**: Użytkownik anulował rezerwację.
**Warunki wstępne:** Użytkownik jest zalogowany.  
**Warunki końcowe:** Rezerwacja jest anulowana.

### Główny scenariusz:
1. System wyświetla historię rezerwacji.
2. Użytkownik anuluje rezerwację.
3. System wyświetla informacje czy Użytkownik na pewno chce anulować.
4. Użytkownik wybiera tak.
5. System anuluje rezerwacje i wyświetla potwierdzenie.

### Scenariusze alternatywne:
**A1 – Użytkownik klika nie**  
W kroku 3 Użytkownik rezygnuje z anulacji, System wraca do kroku 1.

**A2 – Błąd anulacji**  
W kroku 5 system informuje o błędzie, wyświetla powód, i wraca do kroku 1.


## Przypadek użycia: Dodaj Obiekt sportowy

**Aktorzy:** Właściciel Boiska  
**Opis:** Użytkownik chce dodać nowy Obiekt Sportowy.  
**Rezultat**: Użytkownik dodał Obiekt sportowy.
**Warunki wstępne:** Użytkownik jest zalogowany jako Właściciel Boiska.  
**Warunki końcowe:** Obiekt sportowy jest dodany.

### Główny scenariusz:
1. System wyświetla panel dodawania Obiektu Sportowego.
2. Użytkownik podaje dane Obiektu sportowego i potwierdza.
3. System dodaje Obiekt Sportowy do bazy i wyświetla potwierdzenie.

### Scenariusze alternatywne:
**A1 – Użytkownik podał nieprawidłowe dane**  
W kroku 2 Użytkownik podał nieprawidłe dane, System o tym informuje i wraca do kroku numer 1.

## Przypadek użycia: Edytuj Obiekt sportowy

**Aktorzy:** Właściciel Boiska  
**Opis:** Użytkownik chce edytować Obiekt Sportowy.  
**Rezultat**: Użytkownik zedytował Obiekt sportowy.
**Warunki wstępne:** Użytkownik jest zalogowany jako Właściciel Boiska.  
**Warunki końcowe:** Obiekt sportowy jest zedytowany.

### Główny scenariusz:
1. System wyświetla panel edytowania Obiektu Sportowego.
2. Użytkownik zmienia dane Obiektu sportowego i potwierdza.
3. System zmienia dane Obiektu Sportowego w bazie i wyświetla potwierdzenie.

### Scenariusze alternatywne:
**A1 – Użytkownik podał nieprawidłowe dane**  
W kroku 2 Użytkownik podał nieprawidłe dane, System o tym informuje i wraca do kroku numer 1.

## Przypadek użycia: Usuń Obiekt sportowy 

**Aktorzy:** Właściciel Boiska  
**Opis:** Użytkownik chce usunąć Obiekt Sportowy.  
**Rezultat**: Użytkownik usunął Obiekt sportowy.
**Warunki wstępne:** Użytkownik jest zalogowany jako Właściciel Boiska.  
**Warunki końcowe:** Obiekt sportowy jest usunięty.

### Główny scenariusz:
1. System wyświetla panel edytowania Obiektu Sportowego.
2. Użytkownik usuwa Obiekt sportowy.
3. System usuwa Obiekt sportowy i wyświetla potwierdzenie.


## Przypadek użycia: Dodaj Boisko

**Aktorzy:** Właściciel Boiska  
**Opis:** Użytkownik chce dodać nowe boisko.  
**Rezultat**: Użytkownik dodał boisko.
**Warunki wstępne:** Użytkownik jest zalogowany jako Właściciel Boiska oraz posiada Obiekt Sportowy.  
**Warunki końcowe:** Boisko jest dodane.

### Główny scenariusz:
1. System wyświetla panel edytowania Obiektu sportowego.
2. Użytkownik dodaje boisko dla Obiektu sportowego, podaje dane i potwierdza.
3. System dodaje boisko do Obiektu sportowego, i wyświetla potwierdzenie.

### Scenariusze alternatywne:
**A1 – Użytkownik podał nieprawidłowe dane**  
W kroku 2 Użytkownik podał nieprawidłe dane, System o tym informuje i wraca do kroku numer 1.

## Przypadek użycia: Edytuj Boisko

**Aktorzy:** Właściciel Boiska  
**Opis:** Użytkownik chce edytować Boisko.  
**Rezultat**: Użytkownik zedytował Boisko.
**Warunki wstępne:** Użytkownik jest zalogowany jako Właściciel Boiska, oraz ma boisko.  
**Warunki końcowe:** Boisko jest zedytowane.

### Główny scenariusz:
1. System wyświetla panel edytowania boiska.
2. Użytkownik zmienia dane boiska i potwierdza.
3. System zmienia dane boiska w bazie i wyświetla potwierdzenie.

### Scenariusze alternatywne:
**A1 – Użytkownik podał nieprawidłowe dane**  
W kroku 2 Użytkownik podał nieprawidłe dane, System o tym informuje i wraca do kroku numer 1.

## Przypadek użycia: Usuń Boisko 

**Aktorzy:** Właściciel Boiska  
**Opis:** Użytkownik chce usunąć boisko.  
**Rezultat**: Użytkownik usunął boisko.
**Warunki wstępne:** Użytkownik jest zalogowany jako Właściciel Boiska oraz posiada boisko.  
**Warunki końcowe:** boisko jest usunięte.

### Główny scenariusz:
1. System wyświetla panel edytowania boiska.
2. Użytkownik usuwa boisko.
3. System usuwa boisko i wyświetla potwierdzenie.

