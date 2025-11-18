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
	
	public String stampa() {
    StringBuilder sb = new StringBuilder();
    sb.append("Lunghezza cammino: ").append(lunghezza).append("\n");
    sb.append("Sequenza dei landmark:\n");

    for (Cella cella : seq) {
        sb.append(" - ").append(cella).append("\n");
    }

    return sb.toString();
}

}