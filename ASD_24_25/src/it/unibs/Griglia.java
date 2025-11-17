package it.unibs;

public class Griglia {
    private Cella[][] celle;
    private final int righe, colonne;

    // Costruttore
    public Griglia(int righe, int colonne) {
        this.righe = righe;
        this.colonne = colonne;
        this.celle = new Cella[righe][colonne];
        for (int r = 0; r < righe; r++) {
            for (int c = 0; c < colonne; c++) {
                celle[r][c] = new Cella(r, c); // riga, colonna
            }
        }
    }

    // Controlla se la cella è all'interno della griglia
    public boolean isNeiMargini(int r, int c) {
        return r >= 0 && r < righe && c >= 0 && c < colonne;
    }

    // Riconosce le celle libere
    public boolean isLibera(int r, int c) {
        return isNeiMargini(r, c) && celle[r][c].isAttraversabile();
    }

    // Trasforma una cella in ostacolo
    public void inserisciOstacolo(int r, int c) {
        if (isNeiMargini(r, c)) {			// && isLibera(r, c)
            celle[r][c].setAttraversabile(false);
        }
    }

    // Restituisce una cella della griglia
    public Cella getCella(int r, int c) {
        return celle[r][c];
    }

    // Restituisce il numero di righe della griglia
    public int getRighe() {
        return righe;
    }

    // Restituisce il numero di colonne della griglia
    public int getColonne() {
        return colonne;
    }
}