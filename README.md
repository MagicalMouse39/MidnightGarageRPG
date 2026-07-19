# Midnight Garage RPG

**Corso:** Metodologie di Programmazione e Modellazione e Gestione della Conoscenza (A.A. 2025/2026)  
**Studente Matricola:** 122830  
**Repository:** public-github-repo  
**Package Principale:** `it.unicam.cs.mpgc.rpg122830`

---

## Descrizione del Progetto

**Midnight Garage** è un Gioco di Ruolo (RPG) gestionale e di simulazione automobilistica sviluppato in Java e JavaFX. Il giocatore veste i panni di un meccanico e pilota di auto sportive d'epoca, con l'obiettivo di restaurare veicoli, acquistare componenti ad alte prestazioni al mercato nero, gestire le finanze, riparare componenti usurati, accumulare reputazione (XP) e gareggiare in corse clandestine notturne.

### Funzionalità Principali
1. **Officina (Garage View):** Gestione in tempo reale dei componenti installati sul veicolo attivo (Motore, Turbocompressore, Freni, Sospensioni), con visualizzazione della salute/usura (Condition) e dei bonus prestazionali. Possibilità di smontare componenti o ripararli pagando il costo relativo (guadagnando XP).
2. **Mercato (Market View):** Compravendita quotidiana di veicoli usati e componenti sfusi (Rusty, OEM o Racing) con prezzi dinamici basati sulla condizione del pezzo e sul livello del giocatore. I giocatori possono anche vendere i propri pezzi o auto per liquidità.
3. **Street Racing (Race View):** Simulazione di gare clandestine con log narrativo in tempo reale basato sulla strategia scelta:
   - **Drag Race (Accelerazione):** Dipende principalmente da potenza del Motore e del Turbocompressore.
   - **Touge Drift Battle (Derapata):** Dipende dall'equilibrio tra Sospensioni, Freni e il livello di abilità del pilota.
   *Le gare applicano usura reale ai componenti utilizzati.*
4. **Salvataggio & Caricamento:** Persistenza locale dello stato del giocatore (cash, XP, garage, inventario) tramite file JSON.

---

## Architettura e Principi SOLID Applicati

Il progetto adotta un'architettura **MVC (Model-View-Controller)** separando nettamente le responsabilità:
* **`core.model`**: Entità pure (`Player`, `Vehicle`, `Component` e i suoi sottotipi concreti `Engine`, `Turbocharger`, `Brakes`, `Suspension`) prive di dipendenze dalla UI.
* **`core.service`**: Logica di business (calcoli economici in `RepairService`, transazioni in `MarketService` e simulazioni gare in `RaceStrategy`).
* **`persistence`**: Gestione del salvataggio tramite il pattern Dependency Inversion (`SaveManager<T>` e l'implementazione concreta `JsonSaveManager`).
* **`view`**: Interfaccia utente interamente JavaFX, arricchita con CSS personalizzato per un'esperienza premium a tema "carbon-and-neon".

### Principi SOLID Rispettati:
* **SRP (Single Responsibility Principle):** Ogni classe ha un unico scopo. Ad esempio, `Vehicle` non calcola i costi delle riparazioni; tale logica è incapsulata in `RepairService`.
* **OCP (Open/Closed Principle):** Nuovi tipi di componenti o nuove strategie di gara possono essere aggiunti estendendo le classi base (`Component`, `RaceStrategy`) senza modificare la logica di calcolo centrale.
* **LSP (Liskov Substitution Principle):** Tutte le sottoclassi di `Component` possono essere utilizzate ovunque sia richiesto un `Component` senza alterare la correttezza del programma.
* **ISP (Interface Segregation Principle):** Le interfacce come `SaveManager` e `RaceStrategy` sono piccole e focalizzate su un'unica responsabilità.
* **DIP (Dependency Inversion Principle):** I controller e i servizi dipendono da astrazioni (es. `SaveManager`) anziché da implementazioni concrete, facilitando in futuro il passaggio a database remoti o SQLite.

---

## Istruzioni per l'Esecuzione

Per compilare ed eseguire l'applicazione sono necessari solo due comandi:

### 1. Compilazione
```bash
./gradlew build
```

### 2. Esecuzione
```bash
./gradlew run
```

*Nota: Il progetto è configurato per utilizzare automaticamente la Toolchain Java 21.*

---

## Strumenti AI

Durante la realizzazione di questo progetto ho utlizzato `Gemini Flash 3.5` tramite l'agente `Antigravity` principalmente per spunti su documentazione e interfaccia grafica.

Alla fine del progetto è stata effettuata una code-review da parte dell'agente AI.
