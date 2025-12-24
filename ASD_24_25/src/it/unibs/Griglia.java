package it.unibs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Griglia {
    //private Cella[][] celle;
    private final int righe, colonne;
    private boolean[][] ostacoli;
    private TipoPattern tipologia;
    private Cella[][] celle; 
    private Cella origineDefault;
    private Cella destinazioneDefault;
    //private  List <Cella> ostacoli;
    // Costruttore
    public Griglia(int righe, int colonne , TipoPattern tipologia) {
        this.righe = righe;
        this.colonne = colonne;
        this.tipologia = tipologia ;// Valore di default in futuro si potrebbe implementatere
                                            // un metodo per settarlo in base al pattern scelto
                                            //in questo momneto la classe Griglia è stata sostiuita da 
                                            // una matrice di boolean in cui true = ostacolo false = libera
                                            //quindi un po lungo andare a modificare tutto il codice per ora lascio cosi
        this.ostacoli = new boolean[righe][colonne];
        this.origineDefault = null;
        this.destinazioneDefault = null;
        this.celle=new Cella[righe][colonne];
        
        
        for (int r = 0; r < righe; r++) {
            for (int c = 0; c < colonne; c++) {
                ostacoli[r][c] = false; // riga, colonna
            }
        }
    }

    @JsonCreator
    public Griglia(
        @JsonProperty("righe") int righe,
        @JsonProperty("colonne") int colonne,
        @JsonProperty("tipologia") TipoPattern tipologia,
        @JsonProperty("ostacoli") boolean[][] ostacoli,
        @JsonProperty("origineDefault") Cella origineDefault,
        @JsonProperty("destinazioneDefault") Cella destinazioneDefault,
        @JsonProperty("celle") Cella[][] celle
        
    ) {

    this.righe = righe;
    this.colonne = colonne;
    this.ostacoli = ostacoli;
    this.tipologia = tipologia;
    this.origineDefault = origineDefault;
    this.destinazioneDefault = destinazioneDefault;
    this.celle=celle;
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

    */

    // Restituisce una cella della griglia
    public Cella getCella(int r, int c) {
        return celle[r][c];
    }

    public Cella[][] getCelle(){return this.celle;}

    public void setOstacoli(boolean[][] mat_ostacoli) {
        this.ostacoli = mat_ostacoli;
    }

    public boolean[][] getOstacoli() {
        return ostacoli;
    }

    // Restituisce il numero di righe della griglia
    public int getRighe() {
        return righe;
    }

    // Restituisce il numero di colonne della griglia
    public int getColonne() {
        return colonne;
    }

    public TipoPattern getTipologia() {
        return tipologia;
    }

    public void setTipologia(TipoPattern tipologia) {
        this.tipologia = tipologia;
    }

    public Cella getOrigineDefault() {
        return origineDefault;
    }
    
    public void setOrigineDefault(Cella origineDefault) {
        this.origineDefault = origineDefault;
    }
    
    public Cella getDestinazioneDefault() {
        return destinazioneDefault;
    }
    
    public void setDestinazioneDefault(Cella destinazioneDefault) {
        this.destinazioneDefault = destinazioneDefault;
    }

    @Override
public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Griglia {\n");
    sb.append("  righe=").append(righe)
      .append(", colonne=").append(colonne).append("\n");

    sb.append("  tipologia=").append(tipologia).append("\n");

    sb.append("  origineDefault=").append(origineDefault).append("\n");
    sb.append("  destinazioneDefault=").append(destinazioneDefault).append("\n");

    


    return sb.toString();
}

}