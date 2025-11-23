package it.unibs;

import java.util.List;

public class Griglia {
    //private Cella[][] celle;
    private final int righe, colonne;
    //private  List <Cella> ostacoli;
    // Costruttore
    public Griglia(int righe, int colonne) {
        this.righe = righe;
        this.colonne = colonne;
        //this.celle = new Cella[righe][colonne];
        //this.ostacoli = new java.util.ArrayList<>();
        /*for (int r = 0; r < righe; r++) {
            for (int c = 0; c < colonne; c++) {
                celle[r][c] = new Cella(r, c); // riga, colonna
            }
        }*/
    }
    
    // Controlla se la cella è all'interno della griglia
    public boolean isNeiMargini(int r, int c) {
        return r >= 0 && r < righe && c >= 0 && c < colonne;
    }

    // Riconosce le celle libere
 /*   public boolean isLibera(int r, int c) {
        return isNeiMargini(r, c) && celle[r][c].isAttraversabile();
    }


    // Trasforma una cella in ostacolo
    public void inserisciOstacolo(int r, int c) {
        if (isNeiMargini(r, c) && isLibera(r, c)) {			// && isLibera(r, c)
            celle[r][c].setAttraversabile(false);
            ostacoli.add(celle[r][c]);
        }
    }

    // Trasforma una cella in libera
    public void rimuoviOstacolo(int r, int c) {
        if (isNeiMargini(r, c) && !isLibera(r, c)) {
            celle[r][c].setAttraversabile(true);
            ostacoli.remove(celle[r][c]);
        }
    }

    // Calcola la densità di ostacoli nella griglia
    public double calcolaDensita() {
        double sum = 0;;
        for (int r = 0; r < getRighe(); r++) {
            for (int c = 0; c < getColonne(); c++) {
                if (!isLibera(r, c)) {
                    sum += 1;
                }
            }
        }
        return sum/(getRighe() * getColonne());
    }

    // Inserisce gli ostacoli
    public void setObstacle(int r, int c) {
        if (isNeiMargini(r, c)) inserisciOstacolo(r, c);

    }

    //calcola ostacoli
    public List<Cella> calculateOstacoli() {
        ostacoli.clear();
        for (int r = 0; r < getRighe(); r++) {
            for (int c = 0; c < getColonne(); c++) {
                if (!isLibera(r, c)) {
                    ostacoli.add(celle[r][c]);
                }
            }
        }
        return ostacoli;
    }*/

    /*public List<Cella> getOstacoli() {
        return ostacoli;
    }


    // Restituisce una cella della griglia
    public Cella getCella(int r, int c) {
        return celle[r][c];
    }*/

    // Restituisce il numero di righe della griglia
    public int getRighe() {
        return righe;
    }

    // Restituisce il numero di colonne della griglia
    public int getColonne() {
        return colonne;
    }
}