package it.unibs;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class AgenteCamminoMinimo {

    private AnalizzatoreCammini analizzatore;
   // private ConfigurazioneAlgoritmo configurazione; //non usata per ora

    public AgenteCamminoMinimo() {
        Griglia gDummy = new Griglia(1,1,TipoPattern.PERSONALIZZATA);
        this.analizzatore = new AnalizzatoreCammini(
				gDummy);
    }

    /* 
    public void configuraAlgoritmo(ConfigurazioneAlgoritmo config) {
        this.configurazione = config;
        // eventuali configurazioni sull'analizzatore
    }
    */

    public void preparaAnalisi(Griglia g) {
        // Prepara eventuali strutture dati o reset dell'analizzatore
        analizzatore.setGriglia(g);

			 // Ripristina la coerenza dei dati prima di avviare l’algoritmo
            analizzatore.ripristinaCoerenzaOstacoli();
            analizzatore.ripristinaCoerenzaTipi();

			analizzatore.resetContatori();
		    analizzatore.calls.set(0);

    }


    public void calcolaCCF(Griglia g, Cella origine, int[][] ccf) {
        analizzatore.calcolaCCF(g,origine, ccf);
      
        //in questo modo non funziona il reset, e la frontier non distingue tra conetsto e complemento
        /** 
          analizzatore.calcolaContesto(g, origine, ccf);
        analizzatore.calcolaComplemento(g, origine, ccf);
        Set<Landmark> frontiera=analizzatore.calcolaFrontiera(ccf, g);
        for (Landmark lm : frontiera) {
            Cella c= lm.getCella();
            ccf[c.getRiga()][c.getColonna()] = 3; // Segna i landmark nella CCF
        }
        */
       System.out.println("Calcolo CCF completato." + ccf);
       for (int i = 0; i < ccf.length; i++) {
           for (int j = 0; j < ccf[0].length; j++) {
               System.out.print(ccf[i][j] + " ");
           }
           System.out.println();
       }
    }
    
    
    /**
     * Esegue il calcolo del cammino minimo tra origine e destinazione sulla griglia fornita,
     * rispettando un limite di tempo opzionale (in ms). Calcola anche tempo e memoria utilizzata.
     */
    public RisultatoCammino eseguiCammino(Griglia griglia, ConfigurazioneAnalizzatore config) {
        // Snapshot memoria iniziale
        if (config.getUsaMemoriaSequenze() == 1) {
            analizzatore.inizializzaMemoriaSequenze();
        }
        long memoriaPrima = 0;
        analizzatore.calls.set(0);
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
                    griglia.getOrigineDefault(),
                    griglia.getDestinazioneDefault(),
                    griglia,
                    config,
                    () -> (System.currentTimeMillis() - inizioMs) >= config.getLimiteTempoMs()
            );
        } catch (StopException e) {
            System.out.println("Time limit exceeded for grid "
                    + griglia.getRighe() + "x" + griglia.getColonne());
            risultato = e.partialResult;
            risultato.setConvergenza(0); // non converge
        }

        long fineNs = System.nanoTime();

        // Calcolo picco memoria
        long piccoHeap = 0;
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP) {
                long usedPeak = pool.getPeakUsage().getUsed();
                if (usedPeak > piccoHeap) piccoHeap = usedPeak;
            }
        }

        long memoriaUsataBytes = piccoHeap - memoriaPrima;

        risultato.setTempo(fineNs - inizioNs);
        risultato.setCrescitaMemoriaBytes(memoriaUsataBytes / 1024); // KB

        return risultato;
    }

    // Ricostruisce il cammino minimo tra start e goal sulla matrice di ostacoli fornita
    public List<Cella> ricostruisciCammino(
            Cella start, Cella goal,
            boolean[][] matriceOstacoli,
            boolean allowDiagonalCutting) {
        return analizzatore.ricostruisciCammino(
                start, goal, matriceOstacoli, allowDiagonalCutting);}
    
}
