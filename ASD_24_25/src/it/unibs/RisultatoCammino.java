package it.unibs;

import java.util.List;

public class RisultatoCammino {
	
	private double lunghezza;
	private List<Landmark> seq;
	
	public RisultatoCammino(double lunghezza, List<Landmark> seq) {
		this.lunghezza = lunghezza;
		this.seq = seq;
	}

	public double getLunghezza() {
		return lunghezza;
	}

	public List<Landmark> getSeq() {
		return seq;
	}
	
	public String stampa() {
    StringBuilder sb = new StringBuilder();
    sb.append("Lunghezza cammino: ").append(lunghezza).append("\n");
    sb.append("Sequenza dei landmark:\n");

    for (Landmark landmark : seq) {
        sb.append(" - ").append(landmark.toString()).append("\n");
    }

    return sb.toString();
}

}