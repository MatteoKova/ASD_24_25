package it.unibs;

/* PSEUDOCODICE

INPUT: rows, cols, seed, config (percentuali/numeri per ogni ostacolo)
CREA grid[rows][cols] inizializzata a 0 (libera)
INIZIALIZZA Random con seed
PER ogni tipologia di ostacolo in una lista configurata:
    calcola quante istanze/probabilità usare in base a config e dimensione griglia
    per i=1..numero_istanze:
        prova a generare un'istanza valida (più tentativi)
        --- per ogni tipo: costruisci la forma (lista di celle)
        --- controlla che tutte le celle siano dentro griglia e libere
        --- applica la forma (setta a 1)
se richiesto, esegui postprocessing (es. assicurare connettività della area libera) -- opzionale
STAMPA/SALVA grid

STRATEGIE DI POSIZIONAMENTO:
 - SingleCell: scegli cella libera a caso
 - Agglomerato: scegli cella seed, espandi ortogonalmente fino alla dimensione scelta
 - Diagonale: scegli direzione diagonale e lunghezza, piazza celle in (r+i, c+i) o (r+i, c-i)
 - Barra: scegli lunghezza L e spessore S, scegli orientamento orizz/vert e posiziona rectangle
 - Anello: scegli centro e raggio (o half-width), scrivi perimetro di un rettangolo/cerchio
 - Blob (random walk): partire da seed e camminare aggiungendo nuove celle

VERIFICHE:
 - Controlla collisioni con celle già occupate
 - Evita di posizionare anelli se nasconderebbero completamente la griglia o non lascerebbero spazio interno
 - Limita tentativi -> se fallisce skip o riduci dimensione
*/

import java.util.Random;

public class GeneratoreGriglieOstacoli {
    private final Configurazione cfg;
    private final Random rnd;
    private final Griglia griglia;

    public GeneratoreGriglieOstacoli(Configurazione cfg) {
        this.cfg = cfg;
        this.rnd = new Random(cfg.seed);
        this.griglia = new Griglia(cfg.righe, cfg.colonne);
    }

    // Genera tutte le tipologie di ostacoli rispettando la densità specificata
    public void generateAll() {
        if (cfg.density <= 0) return;
    double densita;
        do{
        generaSemplici(cfg.numSemplici);
        generaAgglomerati(cfg.numAgglomerati);
        generaDiagonali(cfg.numDiagonali);
        generaBarre(cfg.numBarre);
        generaAnelli(cfg.numAnelli);
        densita = griglia.calcolaDensita();
        System.out.println("density: " + densita + ", target: " + cfg.density);
    } while (densita < cfg.density );

     griglia.calculateOstacoli();
      rimuoviOstacoliEccesso(densita);
    

    }

    public void pulisciGriglia() {
        for (int r = 0; r < cfg.righe; r++) {
            for (int c = 0; c < cfg.colonne; c++) {
                griglia.getCella(r, c).setAttraversabile(true);
            }
        }
    }

    private void rimuoviOstacoliEccesso(double densitaAttuale) {
        double densitaTarget = cfg.density;
        int righe = cfg.righe;
        int colonne = cfg.colonne;
        int totaleCelle = righe * colonne;
        int ostacoliDaRimuovere = (int) ((densitaAttuale - densitaTarget) * totaleCelle);

        while (ostacoliDaRimuovere > 0) {
            int index=rnd.nextInt(griglia.getOstacoli().size());
            Cella ostacolo = griglia.getOstacoli().get(index);
            int r = ostacolo.getRiga();
            int c = ostacolo.getColonna();
            griglia.rimuoviOstacolo(r, c);;
            ostacoliDaRimuovere--;
            
        }
    }

    // TIPOLOGIE DI OSTACOLI
    
    // Genera singole celle
    private void generaSemplici(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(griglia.getRighe());
            int c = rnd.nextInt(griglia.getColonne());
            griglia.inserisciOstacolo(r, c);
        }
    }

    // Genera insiemi con 2, 3 o 4 celle
    private void generaAgglomerati(int n) {
        for (int i = 0; i < n; i++) {
            int baseR = rnd.nextInt(griglia.getRighe());
            int baseC = rnd.nextInt(griglia.getColonne());
            int dim = 2 + rnd.nextInt(3); // da 2 a 4 celle
            for (int j = 0; j < dim; j++) {
                int r = baseR + rnd.nextInt(2);
                int c = baseC + rnd.nextInt(2);
                griglia.inserisciOstacolo(r, c); // se le coordinate non sono valide, non stampa cella
            }
        }
    }

    // Genera insiemi con 2, 3 o 4 celle in diagonale
    private void generaDiagonali(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(griglia.getRighe());
            int c = rnd.nextInt(griglia.getColonne());
            boolean verso = rnd.nextBoolean();
            int len = 2 + rnd.nextInt(3);
            for (int k = 0; k < len; k++) {
                if (verso) // diagonale creata verso dx
                    griglia.inserisciOstacolo(r + k, c + k); // se supera i limiti, inBounds() evita che venga stampato
                else // diagonale creata verso sx
                    griglia.inserisciOstacolo(r + len - k, c - k); // se supera i limiti, inBounds() evita che venga stampato
            }
        }
    }

    // Genera rettangoli con 3, 4, 5, 6 o 7 celle in lunghezza e con 1 o 2 celle in spessore
    private void generaBarre(int n) {
        for (int i = 0; i < n; i++) {
            boolean orizzontale = rnd.nextBoolean();
            int lung = 3 + rnd.nextInt(5);
            int spess = 1 + rnd.nextInt(2);
            int r = rnd.nextInt(griglia.getRighe());
            int c = rnd.nextInt(griglia.getColonne());
            for (int dr = 0; dr < (orizzontale ? spess : lung); dr++)
                for (int dc = 0; dc < (orizzontale ? lung : spess); dc++)
                    griglia.inserisciOstacolo(r + dr, c + dc);
        }
    }

    // Genera anelli non attraversabili che racchiudono celle attraversabili
    private void generaAnelli(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(griglia.getRighe());
            int c = rnd.nextInt(griglia.getColonne());
            int size = 3 + rnd.nextInt(4);
            for (int k = 0; k < size; k++) {
                griglia.inserisciOstacolo(r, c + k);
                griglia.inserisciOstacolo(r + size - 1, c + k);
                griglia.inserisciOstacolo(r + k, c);
                griglia.inserisciOstacolo(r + k, c + size - 1);
            }
        }
    }

    

	public Griglia getGrid() {
		return griglia;
	}
}