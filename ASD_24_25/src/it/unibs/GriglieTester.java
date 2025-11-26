package it.unibs;

public class GriglieTester {
    private final java.util.List<Griglia> griglie;

    public GriglieTester(java.util.List<Griglia> griglie) {
        this.griglie = griglie;
    }

    public void runTests() {
        for (Griglia g : griglie) {
            boolean risultato = testGriglia(g);
            /*
            
            */
        }
    }

    private boolean testGriglia(Griglia g) {
       return true;
    }

    private void generateReport() {
        // Implementa la generazione di un report dettagliato dei test
    }


}