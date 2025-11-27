package it.unibs;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class GriglieTester {
    private final java.util.List<Griglia> griglie;
    private AnalizzatoreCammini analizzatore;
    private Stack<Cella> pilaOstacoli;
    private  int[][] matriceVuota ; 
    private  Stack<int[][]> pilaTipiCammini;
    private List<RisultatoCammino> risultati;    
 
    public GriglieTester(java.util.List<Griglia> griglie) {
        this.griglie = griglie;
        this.pilaOstacoli = new Stack<>();
        this.matriceVuota = new int[griglie.get(0).getRighe()][griglie.get(0).getColonne()]; 
        this.pilaTipiCammini = new Stack<>();
        this.risultati= new java.util.ArrayList<>();
        pilaTipiCammini.push(matriceVuota);
        this.analizzatore = new AnalizzatoreCammini(
            griglie.get(0).getOstacoli(), pilaOstacoli, pilaTipiCammini);
    }

    public void runTests() {
        RisultatoCammino risultato;
        for (Griglia g : griglie) {
            if (matriceVuota.length != g.getRighe() || matriceVuota[0].length != g.getColonne()) {
                matriceVuota = new int[g.getRighe()][g.getColonne()];
                pilaTipiCammini.clear();
                pilaTipiCammini.push(matriceVuota);
            }
            pilaOstacoli.clear();
            pilaTipiCammini.clear();
            pilaTipiCammini.push(matriceVuota);

            risultato = testGriglia(g);
            risultati.add(risultato);
            
        }
        generateReport(risultati);
    }

    private RisultatoCammino testGriglia(Griglia g) {
        long limiteTempo = 3000; // ms
        return eseguiCammino(g, limiteTempo);
    }

    private RisultatoCammino eseguiCammino(Griglia g, long limiteTempoMs) {

        analizzatore.resetContatori();
        analizzatore.calls.set(0);

        long inizioMs = System.currentTimeMillis();
        long inizioNs = System.nanoTime();

        RisultatoCammino risultato;

        try {
            risultato = analizzatore.camminoMin(
                    g.getOrigineDefault(),
                    g.getDestinazioneDefault(),
                    g,
                    () -> (System.currentTimeMillis() - inizioMs) >= limiteTempoMs
            );

        } catch (StopException e) {
            risultato = e.partialResult;
        }

        long fineNs = System.nanoTime();
         //non credo seerva questa sezione
        /* 
        List<Cella> camminoCompleto = new java.util.ArrayList<>();
       
        List<Landmark> seq = risultato.getSeq();

        for (int i = 0; i < seq.size() - 1; i++) {
            camminoCompleto.addAll(
                    analizzatore.ricostruisciCammino(
                            seq.get(i).getCella(), 
                            seq.get(i + 1).getCella()
                    )
            );
        }

        risultato.setCamminoCompleto(camminoCompleto);*/
        risultato.setTempo(fineNs - inizioNs);

        return risultato;
    }


    

    private void generateReport(List<RisultatoCammino> risultati) {
    String fileName = "report.csv";

    try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {

        // CSV HEADER
        pw.println("righe,colonne,lunghezza,tempo_ns,istr16,frontiera,landmarks");

        for (RisultatoCammino r : risultati) {

            String landmarks = r.getSeq().stream()
                    .map(Landmark::toString)
                    .collect(Collectors.joining("|")); 
            // Use | to avoid commas breaking the CSV format

            pw.printf("%d,%d, %d,%.3f,%d,%d,%d,%s%n",
                    r.getGriglia().getRighe(),
                    r.getGriglia().getColonne(),
                    r.getGriglia().getTipologia().toString(),
                    r.getLunghezza(),
                    r.getTempoCalcolo(),
                    r.getIstr16Contatore(),
                    r.getContatoreFrontiera(),
                    landmarks
            );
        }

        System.out.println("CSV report generated: " + fileName);

    } catch (IOException e) {
        e.printStackTrace();
    }
}


}