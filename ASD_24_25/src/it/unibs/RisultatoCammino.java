package it.unibs;

import java.util.List;

public class RisultatoCammino {
	
	private double lunghezza;
	private List<Landmark> seq;
	private  long tempoCalcolo;
	private int istr16Contatore;
	private Griglia griglia;
	private int  contatoreFrontiera;
	
	public RisultatoCammino(double lunghezza, List<Landmark> seq, int istr16Contatore, int contatoreFrontiera, Griglia griglia	) {
		this.lunghezza = lunghezza;
		this.seq = seq;
		this.tempoCalcolo = 0;
		this.istr16Contatore = istr16Contatore;
		this.griglia = griglia;
		this.contatoreFrontiera = contatoreFrontiera;	
	}

	public double getLunghezza() {
		return lunghezza;
	}

	public List<Landmark> getSeq() {
		return seq;
	}
	 
	public void setTempo(long tempoCalcolo) {
		this.tempoCalcolo = tempoCalcolo;
		}
	public String stampa() {
    StringBuilder sb = new StringBuilder();
	sb.append("Dimnensioni griglia: ").append(griglia.getRighe()).append(" x ").append(griglia.getColonne()).append("\n");
	//sb.append("densità ostacoli: ").append(String.format("%.2f", griglia.calcolaDensita())).append("\n");
    sb.append("Lunghezza cammino: ").append(lunghezza).append("\n");
  
	sb.append("Tempo di calcolo: ").append(tempoCalcolo).append(" ns\n");
	sb.append("Istruzioni di tipo 16 eseguite: ").append(istr16Contatore).append("\n");
	sb.append("Numero totale di nodi nella frontiera analizzati: ").append(contatoreFrontiera).append("\n");
	  sb.append("Sequenza dei landmark:\n");

    for (Landmark landmark : seq) {
        sb.append(" - ").append(landmark.toString()).append("\n");
    }

    return sb.toString();
}

}