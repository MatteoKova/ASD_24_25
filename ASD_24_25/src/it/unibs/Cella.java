package it.unibs;

//Classe che rappresenta una singola cella
public class Cella {
	private int riga;
	private int colonna;
	private String tipo = "attraversabile";
	
	// Costruttore con tipo di default
	public Cella(int riga, int colonna) {
	    this.riga = riga;
	    this.colonna = colonna;
	}
	
	// Costruttore con tipo personalizzato
	public Cella(int riga, int colonna, String tipo) {
	    this.riga = riga;
	    this.colonna = colonna;
	    this.tipo = tipo;
	}
	
	// Getter e Setter
	public int getX() { return riga; }
	public int getY() { return colonna; }
	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }
	
	@Override
	public String toString() {
		return "Cella(x=" + riga + ", y=" + colonna + ", tipo=" + tipo + ")";
    }
}