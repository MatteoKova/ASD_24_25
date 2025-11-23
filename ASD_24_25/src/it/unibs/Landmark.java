package it.unibs;

public class Landmark {
	private Cella cella;
	private int tipo;
	
	public Landmark(Cella cella, int tipo) {
		this.cella = cella;
		this.tipo = tipo;
	}
	
    public Cella getCella() {
		return cella;
	}

	public String toString() {
        return "(" + cella + ", " + tipo + ")";
    }
}
