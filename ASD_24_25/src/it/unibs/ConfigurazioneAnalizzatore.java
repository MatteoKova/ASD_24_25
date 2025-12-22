package it.unibs;

public class ConfigurazioneAnalizzatore {
   
    long limiteTempoMs;
    boolean riordinaFrontiera;// false = no, true = si.  Ordina frontiera prima del ciclo in modo da analizzare celle di frontiera
                                //  migliori prima, e poter fare piu pruning dopo
    int condForte; // 0 = no, 1 = si. Attiva condizione forte dell istr 16 dell analizzatore
    int usaMemoriaSequenze; // 0 = no, 1 = si. Usa memoria per sequenze di celle gia analizzate

    //boolean usaBitMap; // false = no, true = si. Usa BitMap per memorizzare ostacoli e tipi di cammino(da implementare)

    public void configuraAnalizzatore() {
        this.limiteTempoMs = 3000;// Valore di default
        this.riordinaFrontiera = false; // Valore di default
        this.condForte = 0; // Valore di default
        this.usaMemoriaSequenze = 0; // Valore di default
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