package it.unibs;

public class Configurazione {

    public int righe;
    public int colonne;
    public long seed = System.currentTimeMillis();
    public int numSemplici = 5;//15;
    public int numAgglomerati = 3;//10;
    public int numDiagonali = 1;//5;
    public int numBarre = 1;//5;
    public int numAnelli = 1;//3;
    public double density;
}