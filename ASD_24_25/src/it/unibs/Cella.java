package it.unibs;

import java.util.Objects;

//Classe che rappresenta una singola cella
public class Cella {
	private int x, y;
	private String tipo = "attraversabile";
	
	// Costruttore con tipo di default
	public Cella(int x, int y) {
	    this.x = x;
	    this.y = y;
	}
	/*
	// Costruttore con tipo personalizzato
	public Cella(int x, int y, String tipo) {
	    this.x = x;
	    this.y = y;
	    this.tipo = tipo;
	}
	*/
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof Cella)) return false;
	    Cella other = (Cella) o;
	    return this.x == other.x && this.y == other.y;
	}

	@Override
	public int hashCode() {
	    return Objects.hash(x, y);
	}
	
	// Getter e Setter
	public int getX() { return x; }
	public int getY() { return y; }
	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }
	/*
	@Override
	public String toString() {
		return "Cella(x=" + x + ", y=" + y + ", tipo=" + tipo + ")";
    }
    */
}