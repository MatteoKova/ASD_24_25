# Algoritmi e Strutture Dati

## Collaboratori

- Matteo Kovacic — [GitHub](https://github.com/MatteoKova)
- Federico Bagnalasta — [GitHub](https://github.com/FedericoBagnalasta)
- Samuele Zambelli — [GitHub](https://github.com/sazmb)

## Descrizione del progetto

Il progetto consiste nello sviluppo di un’applicazione per la gestione di griglie bidimensionali e il calcolo di percorsi minimi, realizzata nell’ambito del corso di **Algoritmi e Strutture Dati (a.a. 2024-2025)**.

L’applicazione consente di generare griglie configurabili, contenenti celle attraversabili e ostacoli di diverse tipologie, e di analizzare la connettività dello spazio per individuare percorsi tra celle di origine e destinazione.

### Funzionalità principali

- **Generazione delle griglie:** creazione di griglie finite con distribuzioni casuali di ostacoli e configurazioni strutturate, tra cui ostacoli semplici, aggregati, diagonali, delimitatori di aree chiuse e conformazioni a barre.
- **Modellazione dello spazio:** rappresentazione delle celle e delle relazioni di adiacenza secondo un modello a otto direzioni, comprendente mosse cardinali e diagonali. I costi associati agli spostamenti sono rispettivamente pari a \(1\) e \(\sqrt{2}\).
- **Analisi della raggiungibilità:** calcolo del contesto e del complemento di una cella di origine, considerando i cammini liberi di tipo 1 e di tipo 2 previsti dalla specifica del progetto.
- **Calcolo delle distanze:** determinazione della distanza libera tra coppie di celle e individuazione dei cammini minimi, anche attraverso procedure ricorsive basate sull’attraversamento delle celle di frontiera.
- **Ricerca dei landmark:** individuazione della sequenza di landmark associata a un cammino minimo tra origine e destinazione, con ricostruzione del percorso completo quando disponibile. La procedura gestisce anche i casi in cui la destinazione non appartiene alla chiusura dell’origine, componendo più cammini minimi intermedi.
- **Ottimizzazione della ricerca:** impiego di strategie algoritmiche e criteri euristici per ordinare le celle di frontiera e migliorare l’efficienza delle operazioni di esplorazione e ricerca dei percorsi.
- **Valutazione sperimentale:** esecuzione di test su tutte le tipologie di griglie generate, con misurazione delle prestazioni dell’algoritmo `camminoMin()` e confronto tra differenti configurazioni e strategie implementative.

### Organizzazione e obiettivi

Il codice è organizzato per separare la rappresentazione della griglia, la gestione delle celle e degli ostacoli, gli algoritmi di analisi e ricerca dei percorsi e le funzionalità di sperimentazione. Questa suddivisione favorisce la manutenibilità, la chiarezza della struttura e la possibilità di confrontare diverse soluzioni algoritmiche.

La componente sperimentale permette di analizzare il comportamento degli algoritmi al variare delle dimensioni della griglia, della distribuzione degli ostacoli e della configurazione dello spazio di ricerca, fornendo una base per valutare l’efficacia delle ottimizzazioni introdotte.

Il progetto integra quindi **modellazione dei dati, algoritmi di ricerca, ricostruzione dei percorsi e analisi delle prestazioni** in un’unica applicazione, con l’obiettivo di studiare e confrontare soluzioni per il problema dei cammini minimi in ambienti bidimensionali con ostacoli.
