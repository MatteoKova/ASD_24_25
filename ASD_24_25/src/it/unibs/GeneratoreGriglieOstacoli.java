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
    private final Griglia grid;

    public GeneratoreGriglieOstacoli(Configurazione cfg) {
        this.cfg = cfg;
        this.rnd = new Random(cfg.seed);
        this.grid = new Griglia(cfg.rows, cfg.cols);
    }

    // Genera tutte le tipologie di ostacoli
    public void generateAll() {
        generaSemplici(cfg.numSemplici);
        generaAgglomerati(cfg.numAgglomerati);
        generaDiagonali(cfg.numDiagonali);
        generaBarre(cfg.numBarre);
        generaAnelli(cfg.numAnelli);
    }

    // TIPOLOGIE DI OSTACOLI
    
    // Genera singole celle
    private void generaSemplici(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(grid.getRighe());
            int c = rnd.nextInt(grid.getColonne());
            grid.setObstacle(r, c);
        }
    }

    // Genera insiemi con 2, 3 o 4 celle
    private void generaAgglomerati(int n) {
        for (int i = 0; i < n; i++) {
            int baseR = rnd.nextInt(grid.getRighe());
            int baseC = rnd.nextInt(grid.getColonne());
            int dim = 2 + rnd.nextInt(3); // da 2 a 4 celle
            for (int j = 0; j < dim; j++) {
                int r = baseR + rnd.nextInt(2);
                int c = baseC + rnd.nextInt(2);
                grid.setObstacle(r, c); //se le coordinate non sono valide, non stampa cella
            }
        }
    }

    // Genera insiemi con 2, 3 o 4 celle in diagonale
    private void generaDiagonali(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(grid.getRighe());
            int c = rnd.nextInt(grid.getColonne());
            boolean verso = rnd.nextBoolean();
            int len = 2 + rnd.nextInt(3);
            for (int k = 0; k < len; k++) {
                if (verso) //diagonale creata verso dx
                    grid.setObstacle(r + k, c + k); //se supera i limiti, inBounds() evita che venga stampato
                else //diagonale creata verso sx
                    grid.setObstacle(r + len - k, c - k); //se supera i limiti, inBounds() evita che venga stampato
            }
        }
    }

    // Genera rettangoli con 3, 4, 5, 6 o 7 celle in lunghezza e con 1 o 2 celle in spessore
    private void generaBarre(int n) {
        for (int i = 0; i < n; i++) {
            boolean orizzontale = rnd.nextBoolean();
            int lung = 3 + rnd.nextInt(5);
            int spess = 1 + rnd.nextInt(2);
            int r = rnd.nextInt(grid.getRighe());
            int c = rnd.nextInt(grid.getColonne());
            for (int dr = 0; dr < (orizzontale ? spess : lung); dr++)
                for (int dc = 0; dc < (orizzontale ? lung : spess); dc++)
                    grid.setObstacle(r + dr, c + dc);
        }
    }

    // Genera anelli non attraversabili che delimitano celle attraversabili
    private void generaAnelli(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(grid.getRighe());
            int c = rnd.nextInt(grid.getColonne());
            int size = 3 + rnd.nextInt(4);
            for (int k = 0; k < size; k++) {
                grid.setObstacle(r, c + k);
                grid.setObstacle(r + size - 1, c + k);
                grid.setObstacle(r + k, c);
                grid.setObstacle(r + k, c + size - 1);
            }
        }
    }

	public Griglia getGrid() {
		return grid;
	}
}