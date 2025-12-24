package it.unibs;

public class ConfigurazioneAnalizzatore {
   
    long limiteTempoMs;
    boolean riordinaFrontiera;// false = no, true = si.  Ordina frontiera prima del ciclo in modo da analizzare celle di frontiera
                                //  migliori prima, e poter fare piu pruning dopo
    int condForte; // 0 = no, 1 = si. Attiva condizione forte dell istr 16 dell analizzatore
    int usaMemoriaSequenze; // 0 = no, 1 = si. Usa memoria per sequenze di celle gia analizzate
    int usaUnicoCicloCCF; //0 = no, 1 si. Usa ciclo unico per calcolare Complemento-Contesto-Frontiera

    //boolean usaBitMap; // false = no, true = si. Usa BitMap per memorizzare ostacoli e tipi di cammino(da implementare)

    public ConfigurazioneAnalizzatore () {
        this.limiteTempoMs = 300000;// Valore di default
        this.riordinaFrontiera = true; // Valore di default
        this.condForte = 1; // Valore di default
        this.usaMemoriaSequenze =0; // Valore di default
        this.usaUnicoCicloCCF=0;
    }

    public int getUsaUnicoCicloCCF (){
        return this.usaUnicoCicloCCF;
    }

    public void setUsaUnicoCicloCCF (int usaUnicoCicloCCF){
      this.usaUnicoCicloCCF=usaUnicoCicloCCF;
    }

    public int getUsaMemoriaSequenze() {
        return usaMemoriaSequenze;
    }
    public void setUsaMemoriaSequenze(int usaMemoriaSequenze) {
        this.usaMemoriaSequenze = usaMemoriaSequenze;
    }
    public boolean isRiordinaFrontiera() {
        return riordinaFrontiera;
    }
    public void setRiordinaFrontiera(boolean riordinaFrontiera) {
        this.riordinaFrontiera = riordinaFrontiera;
    }
    public int getCondForte() {
        return condForte;
    }
    public void setCondForte(int condForte) {
        this.condForte = condForte;
    }
    public long getLimiteTempoMs() {
        return limiteTempoMs;
    }
    public void setLimiteTempoMs(long limiteTempoMs) {
        this.limiteTempoMs = limiteTempoMs;
    }
}