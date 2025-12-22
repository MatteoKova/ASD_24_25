package it.unibs;

import java.util.List;

public class RisultatoCammino {
	
	private double lunghezza;
	private List<Landmark> seq;
	private Cella origine;
	private Cella destinazione;
	private long tempoCalcolo;
	private int istr17Contatore;
	private Griglia griglia;
	private int contatoreFrontiera;
	private int convergenza;// 1 se converge(trova destinazione), 0 se non-converge
	private long crescitaMemoriaBytes;
	
	public RisultatoCammino(double lunghezza, List<Landmark> seq, Cella origine, Cella destinazione, int istr16Contatore, int contatoreFrontiera, Griglia griglia) {
		this.lunghezza = lunghezza;
		this.seq = seq;
		this.origine = origine;
		this.destinazione = destinazione;
		this.tempoCalcolo = 0;
		this.istr17Contatore = istr16Contatore;
		this.griglia = griglia;
		this.contatoreFrontiera = contatoreFrontiera;
		this.convergenza = 1; // di default converge
		this.crescitaMemoriaBytes=0;

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
		sb.append("- Dimensioni griglia: ").append(griglia.getRighe()).append(" x ").append(griglia.getColonne()).append("\n");
		sb.append("- Tipologia griglia: ").append(griglia.getTipologia().toString()).append("\n");
		//sb.append("densità ostacoli: ").append(String.format("%.2f", griglia.calcolaDensita())).append("\n");
		sb.append(convergenza == 1 ? "Destinazione raggiunta\n" : "Destinazione non raggiunta\n");
	    sb.append("- Lunghezza cammino: ").append(lunghezza).append("\n");
		sb.append("- Tempo di calcolo: ").append(tempoCalcolo).append(" ns\n");
		sb.append("- Numero totale di nodi nella frontiera analizzati: ").append(contatoreFrontiera).append("\n");
		sb.append("- Istruzioni di tipo 17 eseguite: ").append(istr17Contatore).append("\n\n");
		sb.append("- Crescita memoria heap: ").append(crescitaMemoriaBytes).append(" bytes\n\n");
		sb.append("DETTAGLIO CAMMINO:\n");
	    sb.append("ORIGINE:\n").append(" - ").append("(" + origine + ", " + 0 + ")");
	    
	    String tipo = "?";
	    
	    sb.append("\n\nSEQUENZA DEI LANDMARK:\n");
	    if (seq.size() != 0) { // seq.size() == 0 quando non esiste un cammino valido
		    
		    for (int i = 1; i < seq.size() - 1; i++) {
		    	sb.append(" - ").append(seq.get(i)).append("\n");
		    }
		    
		    if (seq.size() != 1) { // seq.size() == 1 quando il calcolo è interrotto
		    	tipo = Integer.toString(seq.get(seq.size() - 1).getTipo());
		    }
	    } else {
	    	sb.append("Non è stato trovato alcun cammino \nPerciò non è stato attraversato alcun landmark\n");
	    }
	
	    sb.append("\nDESTINAZIONE:\n").append(" - ").append("(" + destinazione + ", " + tipo + ")");
		
	    return sb.toString();
	}	

	public long getTempoCalcolo() {
	    return tempoCalcolo;
	}
	
	public int getIstr17Contatore() {
	    return istr17Contatore;
	}
	
	public Griglia getGriglia() {
	    return griglia;
	}
	
	public int getContatoreFrontiera() {
	    return contatoreFrontiera;
	}

	public int getConvergenza() {
	    return convergenza;
	}

	public void setConvergenza(int convergenza) {
	    this.convergenza = convergenza;
	}

	public void setCrescitaMemoriaBytes(long crescitaMemoriaBytes) {
    this.crescitaMemoriaBytes = crescitaMemoriaBytes;
}

public long getCrescitaMemoriaBytes() {
    return crescitaMemoriaBytes;
}
}