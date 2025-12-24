package it.unibs;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Cella {
    private int riga, colonna;
    private TipoCella tipo;
    private boolean isFrontiera;
    private boolean isLandMark;
    //private int tipo = 1;
    //private boolean isAttraversabile = true;

    // Costruttore con tipo di default
    
    public Cella(int riga, int colonna){
         this.riga = riga;
        this.colonna = colonna;
        this.tipo=TipoCella.LIBERA;
        this.isFrontiera=false;
        this.isLandMark=false;
    }
    @JsonCreator
    public Cella(
        @JsonProperty("riga") int riga,
        @JsonProperty("colonna") int colonna,
        @JsonProperty("tipo") TipoCella tipo
    ) {
        this.riga = riga;
        this.colonna = colonna;
        this.tipo=tipo;
        this.isFrontiera=false;
        this.isLandMark=false;
        
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

    // Setters and Getters
    public int getRiga() { return riga; }
    public int getColonna() { return colonna; }
    public void setRiga(int riga) { this.riga = riga; }
    public void setColonna(int colonna) { this.colonna = colonna; }
    public void setCoordinate(int riga, int colonna){this.riga=riga; this.colonna=colonna;}

    public TipoCella getTipo() { return tipo; }
    public void setTipo(TipoCella tipo){this.tipo=tipo;}

    public boolean isLandMark(){return this.isLandMark;}
    public boolean isFrontiera(){return this.isFrontiera;}
    public void setIsFrontiera(boolean isFrontiera){this.isFrontiera=isFrontiera;}
    public void setIsLandmark(boolean isLandMark){this.isLandMark=isLandMark;}
 //   public boolean isAttraversabile() { return isAttraversabile; }

    // Setters
/*    public void setAttraversabile(boolean isAttraversabile) {
        this.isAttraversabile = isAttraversabile;
    }*/
    
    /*public void setTipo(int tipo) {
		this.tipo = tipo;
	}*/

	@Override
    public String toString() {
        return "Cella(riga=" + riga + ", colonna=" + colonna + ")";// tipo=" + tipo + ")";
    }
}