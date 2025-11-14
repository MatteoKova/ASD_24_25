package it.unibs;

public class Griglia {
	private Cella[][] celle;
	private final int righe, colonne;

    // Costruttore
    public Griglia(int righe, int colonne) {
    	this.righe = righe;
    	this.colonne = colonne;
        this.celle = new Cella[righe][colonne];
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                celle[i][j] = new Cella(i, j); // tipo di default "attraversabile"
            }
        }
    }
    
    // 
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < righe && c >= 0 && c < colonne;
    }

    // Riconosce le celle libere
    public boolean isFree(int r, int c) {
        return inBounds(r, c) && celle[r][c].getTipo().equals("attraversabile");
    }

    // Inserisce gli ostacoli
    public void setObstacle(int r, int c) {
        if (inBounds(r, c)) celle[r][c].setTipo("ostacolo");
    }

    // Restituisce una cella della griglia
    public Cella getCella(int riga, int colonna) {
        return celle[riga][colonna];
    }

    // Modifica del tipo di una cella
    public void setTipo(int riga, int colonna, String tipo) {
        celle[riga][colonna].setTipo(tipo);
    }

    // Restituisce il numero di righe della griglia
    public int getRighe() {
		return righe;
	}

    // Restituisce il numero di colonne della griglia
	public int getColonne() {
		return colonne;
	}

	// Stampa della griglia
    public void stampaGriglia() {
        for (int i = 0; i < celle.length; i++) {
            for (int j = 0; j < celle[i].length; j++) {
                System.out.print(celle[i][j].getTipo().charAt(0) + " "); 
            }
            System.out.println();
        }
    }
}