package it.unibs;

import java.util.List;

public class RisultatoCammino {
	
	private double lunghezza;
	private List<Cella> seq;
	
	public RisultatoCammino(double lunghezza, List<Cella> seq) {
		this.lunghezza = lunghezza;
		this.seq = seq;
	}

	public double getLunghezza() {
		return lunghezza;
	}

	public List<Cella> getSeq() {
		return seq;
	}
	
	public void stampa() {
	    System.out.println("Lunghezza cammino: " + lunghezza);
	    System.out.println("Sequenza dei landmark:");
	    for (Cella cella : seq) {
	        System.out.println(" - " + cella);
	    }
	}
}