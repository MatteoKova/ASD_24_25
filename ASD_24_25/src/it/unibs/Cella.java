package it.unibs;

import java.util.Objects;

public class Cella {
    private int riga, colonna;
    private int tipo = 1;
    private boolean isAttraversabile = true;

    // Costruttore con tipo di default
    public Cella(int riga, int colonna) {
        this.riga = riga;
        this.colonna = colonna;
    }
    /*
    // Costruttore con tipo personalizzato
    public Cella(int riga, int colonna, int tipo) {
        this.riga = riga;
        this.colonna = colonna;
        this.tipo = tipo;
    }
    */
    @Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof Cella)) return false;
	    Cella other = (Cella) o;
	    return this.riga == other.riga && this.colonna == other.colonna;
	}

	@Override
	public int hashCode() {
	    return Objects.hash(riga, colonna);
	}

    // Getters
    public int getRiga() { return riga; }
    public int getColonna() { return colonna; }
    public int getTipo() { return tipo; }
    public boolean isAttraversabile() { return isAttraversabile; }

    // Setters
    public void setAttraversabile(boolean isAttraversabile) {
        this.isAttraversabile = isAttraversabile;
    }
    
    public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	@Override
    public String toString() {
        return "Cella(riga=" + riga + ", colonna=" + colonna + ", tipo=" + tipo + ")";
    }
}