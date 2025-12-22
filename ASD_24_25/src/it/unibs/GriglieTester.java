package it.unibs;



import jdk.jfr.*;
import jdk.jfr.consumer.RecordingFile;
import java.nio.file.Path;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.Collectors;

public class GriglieTester {
	private final java.util.List<Griglia> griglie;
	private List<RisultatoCammino> risultati; 
	private long limiteTempo = 3000; // ms
	private int analisiSpazio = 0; // 0 = no, 1 = si   
	private AgenteCamminoMinimo agente;
	private ConfigurazioneAnalizzatore configAnalizzatore;

	public GriglieTester(java.util.List<Griglia> griglie) {
		this.griglie = griglie;
		this.risultati= new java.util.ArrayList<>();
		this.agente = new AgenteCamminoMinimo();
		this.configAnalizzatore = new ConfigurazioneAnalizzatore();
	}



	// Metodo principale per eseguire i test su tutte le griglie
	//prepara l'ambiente e richiama eseguiCammino per ogni griglia
	public void runTests() {
		System.out.println("Running tests on " + griglie.size() + " grids...");
		int contatore=0;
		RisultatoCammino risultato;
		for (Griglia g : griglie) {
			agente.preparaAnalisi(g);
			risultato= (analisiSpazio==1) ? testGrigliaconSpazio(g, contatore) :  testGriglia(g);
			risultati.add(risultato);
			contatore++;

		}
		generateReport(risultati);
	}

	// Metodo eseguiCammino normale (con analisi spazio base)
	private RisultatoCammino testGriglia(Griglia g) {
		System.out.println("Testing grid in normal mode" );
		return eseguiCammino(g, limiteTempo);
	}

	// Metodo eseguiCammino con analisi spazio (JFR) (produce file .jfr (file che mostra andamento memoria)
	// per ogni griglia testata, bisogna aprirli con jvisualvm o jmc)
	private RisultatoCammino testGrigliaconSpazio(Griglia g, int contatore) {
		System.out.println("Testing grid with space analyzer mode" );
		long limiteTempo = 3000; // ms
		 Recording recording = new Recording();
        recording.enable("jdk.ObjectAllocationInNewTLAB");   // captures object allocations
        recording.enable("jdk.ObjectAllocationOutsideTLAB");
        recording.setToDisk(true);
        recording.setName("eseguiCamminoRecording");
        recording.start();

        // Call your method
        RisultatoCammino result = eseguiCammino(g, limiteTempo);

        // Stop recording
        recording.stop();

		// Create a unique file name using StringBuilder
		StringBuilder buffer = new StringBuilder();
		buffer.append("eseguiCammino_").append(contatore).append(".jfr");


        // Save the recording to a file
        
       Path recordingFile = Path.of(buffer.toString());
		try {
			recording.dump(recordingFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			recording.close();
		}

		System.out.println("JFR recording saved to: " + recordingFile);
		
		return result;
	}

// Metodo eseguiCammino con analisi tempo e spazio(base-> prendo il picco di memoria heap usata durante l'esecuzione) 
	private RisultatoCammino eseguiCammino(Griglia g, long limiteTempoMs) {
    return agente.eseguiCammino(g, configAnalizzatore);
}



	
// Metodo per generare il report CSV (aggregato di tutti i risultati in un file report.csv)
	private void generateReport(List<RisultatoCammino> risultati) {
		String fileName = "report.csv";

		try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {

			// CSV HEADER
			pw.println("righe,colonne,tipologiaG,convergenza,lunghezza,tempo_ns,istr16,frontiera,memo_usata,landmarks");

			for (RisultatoCammino r : risultati) {

				String landmarks = r.getSeq().stream()
						.map(Landmark::toString)
						.collect(Collectors.joining("|")); 
				// Use | to avoid commas breaking the CSV format

				pw.printf("%d,%d, %s,%d,%.3f,%d,%d,%d,%d,%s%n",
						r.getGriglia().getRighe(),
						r.getGriglia().getColonne(),
						r.getGriglia().getTipologia().toString(),
						r.getConvergenza(), // 1 if converged, 0 otherwise 
						r.getLunghezza(), // lunghezza cammino
						r.getTempoCalcolo(),
						r.getIstr17Contatore(),
						r.getContatoreFrontiera(),
						r.getCrescitaMemoriaBytes(),
						landmarks //parte abbastanza superflua
						);
			}

			System.out.println("CSV report generated: " + fileName);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// Metodo per chiedere i parametri all'utente(limite tempo e analisi spazio)
	public void askParameters() {
		 Scanner scanner = new Scanner(System.in);

    // Ask time limit
    System.out.print("Insert time limit for a single test (ms): ");
    configAnalizzatore.setLimiteTempoMs( scanner.nextLong());
    scanner.nextLine(); // consume newline

    // Ask space analysis
    System.out.print("Do you want space analysis? (y/n): ");
    String answer = scanner.nextLine().trim().toLowerCase();

    analisiSpazio = (answer.equals("y")) ? 1 : 0;

   

    scanner.close();
	}
}


//vecchhio metodo per l'analisi spazio
/*
	private RisultatoCammino eseguiCammino(Griglia g, long limiteTempoMs) {

    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    // Reset peak before measurement
    // Reset peak usage for all heap pools
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
        if (pool.getType() == MemoryType.HEAP) {
            pool.resetPeakUsage();
        }
    }
    long inizioNs = System.nanoTime();
    long inizioMs = System.currentTimeMillis();

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
        risultato.setConvergenza(0);
    }

    long fineNs = System.nanoTime();

 // Get peak heap usage
    long peakBytes = 0; // in bytes
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
        if (pool.getType() == MemoryType.HEAP) {
            long used = pool.getPeakUsage().getUsed();
            if (used > peakBytes) peakBytes = used;
        }
    }
    risultato.setTempo(fineNs - inizioNs);
    risultato.setCrescitaMemoriaBytes(peakBytes/1024); // in KB

    return risultato;
}
 */