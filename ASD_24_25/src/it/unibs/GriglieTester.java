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
	private AnalizzatoreCammini analizzatore;
	private Stack<Cella> pilaOstacoli;
	private  int[][] matriceVuota ; 
	private  Stack<int[][]> pilaTipiCammini;
	private List<RisultatoCammino> risultati; 
	private long limiteTempo = 3000; // ms
	private int analisiSpazio = 0; // 0 = no, 1 = si   

	public GriglieTester(java.util.List<Griglia> griglie) {
		this.griglie = griglie;
		this.pilaOstacoli = new Stack<>();
		this.matriceVuota = new int[griglie.get(0).getRighe()][griglie.get(0).getColonne()]; 
		this.pilaTipiCammini = new Stack<>();
		this.risultati= new java.util.ArrayList<>();
		pilaTipiCammini.push(matriceVuota);
		this.analizzatore = new AnalizzatoreCammini(
				griglie.get(0), pilaOstacoli, pilaTipiCammini);
	}


	// Metodo principale per eseguire i test su tutte le griglie
	//prepara l'ambiente e richiama eseguiCammino per ogni griglia
	public void runTests() {
		System.out.println("Running tests on " + griglie.size() + " grids...");
		int contatore=0;
		RisultatoCammino risultato;
		for (Griglia g : griglie) {
			
			if (matriceVuota.length != g.getRighe() || matriceVuota[0].length != g.getColonne()) {
				matriceVuota = new int[g.getRighe()][g.getColonne()];
				pilaTipiCammini.clear();
				pilaTipiCammini.push(matriceVuota);
			}
			analizzatore.setGriglia(g);
			pilaOstacoli.clear();
			pilaTipiCammini.clear();
			pilaTipiCammini.push(matriceVuota);

			 // Ripristina la coerenza dei dati prima di avviare l’algoritmo
            analizzatore.ripristinaCoerenzaOstacoli();
            analizzatore.ripristinaCoerenzaTipi();

			analizzatore.resetContatori();
		    analizzatore.calls.set(0);

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

    // Take snapshot of memory before starting and reset peak usage
    long memoriaPrima = 0;
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
        if (pool.getType() == MemoryType.HEAP) {
            memoriaPrima += pool.getUsage().getUsed();
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
        System.out.println("Time limit exceeded for grid "
                + g.getRighe() + "x" + g.getColonne());
        risultato = e.partialResult;
        risultato.setConvergenza(0); // non converge
    }

    long fineNs = System.nanoTime();

    // Get peak heap usage during execution
    long piccoHeap = 0;
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
        if (pool.getType() == MemoryType.HEAP) {
            long usedPeak = pool.getPeakUsage().getUsed();
            if (usedPeak > piccoHeap) piccoHeap = usedPeak;
        }
    }

    // Memory used by this method only (subtract memory before start)
    long memoriaUsataBytes = piccoHeap - memoriaPrima;

    // Store metrics in RisultatoCammino
    risultato.setTempo(fineNs - inizioNs);
    risultato.setCrescitaMemoriaBytes(memoriaUsataBytes / 1024); // in KB

    return risultato;
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
    limiteTempo = scanner.nextLong();
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