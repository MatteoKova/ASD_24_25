package it.unibs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class FinestraGriglia extends JFrame {
    private static final long serialVersionUID = 1L;

    // Dati della griglia
    private boolean[][] matriceOstacoli;
    private Griglia griglia;
    private Cella origine = null;
    private Cella destinazione = null;
    private int[][] matriceContesto = null;
    private int[][] matriceComplemento = null;
    private Set<Landmark> frontiera = null;

    // Componenti interfaccia utente
    private JButton pulsanteAvvio;
    private JButton pulsanteStop;
    private JLabel etichettaInfo;
    private PannelloGriglia pannelloGriglia;

    // Worker per calcolo percorsi
    private LavoratorePercorso lavoratore;
    private volatile boolean stopRichiesto = false;

    public FinestraGriglia(boolean[][] matriceOstacoli) {
        // TEMPORANEO: View e Controller insieme
        this.matriceOstacoli = matriceOstacoli;

        int numeroRighe = matriceOstacoli.length;
        int numeroColonne = matriceOstacoli[0].length;
        this.griglia = new Griglia(numeroRighe, numeroColonne);

        Stack<Cella> pilaOstacoli = new Stack<>();
        int[][] matriceVuota = new int[numeroRighe][numeroColonne]; 
        Stack<int[][]> pilaTipiCammini = new Stack<>();
        pilaTipiCammini.push(matriceVuota);

        AnalizzatoreCammini analizzatoreCammini = new AnalizzatoreCammini(matriceOstacoli, pilaOstacoli, pilaTipiCammini);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        matriceContesto = new int[numeroRighe][numeroColonne];
        matriceComplemento = new int[numeroRighe][numeroColonne];

        pannelloGriglia = new PannelloGriglia(griglia, matriceOstacoli, matriceContesto, matriceComplemento, frontiera);
        add(pannelloGriglia, BorderLayout.CENTER);
        
        // Pannello legenda laterale
        JPanel pannelloLegenda = new JPanel();
        pannelloLegenda.setLayout(new BoxLayout(pannelloLegenda, BoxLayout.Y_AXIS));
        pannelloLegenda.setBorder(BorderFactory.createTitledBorder("Legenda"));

        // Spaziatura interna
        pannelloLegenda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Legenda"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Voci legenda
        pannelloLegenda.add(creaVoceLegenda(Color.WHITE, "Cella libera"));
        pannelloLegenda.add(creaVoceLegenda(Color.BLUE.darker(), "Ostacolo"));
        pannelloLegenda.add(creaVoceLegenda(Color.GREEN.darker(), "Contesto"));
        pannelloLegenda.add(creaVoceLegenda(Color.YELLOW, "Asse"));
        pannelloLegenda.add(creaVoceLegenda(Color.ORANGE, "Complemento"));
        pannelloLegenda.add(creaVoceLegenda(Color.RED, "Origine (O)"));
        pannelloLegenda.add(creaVoceLegenda(Color.RED, "Destinazione (D)"));

        add(pannelloLegenda, BorderLayout.EAST);

        // Pannello controlli inferiori
        JPanel pannelloControlli = new JPanel(new FlowLayout());
        pulsanteAvvio = new JButton("Avvia valutazione");
        pulsanteStop = new JButton("STOP");
        pulsanteStop.setEnabled(false);

        pannelloControlli.add(pulsanteAvvio);
        pannelloControlli.add(pulsanteStop);

        add(pannelloControlli, BorderLayout.SOUTH);

        // Etichetta superiore
        etichettaInfo = new JLabel("Clicca per selezionare Origine (O) e Destinazione (D)");
        etichettaInfo.setFont(new Font("Arial", Font.BOLD, 18));
        etichettaInfo.setForeground(Color.BLACK);
        etichettaInfo.setOpaque(true);
        etichettaInfo.setBackground(Color.LIGHT_GRAY);
        etichettaInfo.setHorizontalAlignment(SwingConstants.CENTER);
        add(etichettaInfo, BorderLayout.NORTH);

        setSize(900, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        // Listener per click del mouse sulla griglia
        pannelloGriglia.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            		
            	// Conversione pixel → cella usando il metodo del pannello
                Point p = pannelloGriglia.cellaDaCoordinatePixel(e.getX(), e.getY());
                if (p == null) return; // click fuori dalla griglia

                int riga = p.y;
                int colonna = p.x;
            	
                if (!griglia.isNeiMargini(riga, colonna) || matriceOstacoli[riga][colonna]) return;

                if (origine == null) {
                    analizzatoreCammini.ripristinaCoerenzaOstacoli();
                    analizzatoreCammini.ripristinaCoerenzaTipi();
                    origine = new Cella(riga, colonna);
                    etichettaInfo.setText("Origine selezionata: " + origine + ". Ora scegli Destinazione");
                    int[][] matriceTipi = new int[numeroRighe][numeroColonne];
                    matriceContesto = analizzatoreCammini.calcolaContesto(griglia, origine, matriceTipi);
                    matriceComplemento = analizzatoreCammini.calcolaComplemento(griglia, origine, matriceTipi);
                    frontiera = analizzatoreCammini.calcolaFrontiera(matriceTipi, griglia);

                } else if (destinazione == null) {
                    destinazione = new Cella(riga, colonna);
                    etichettaInfo.setText("Destinazione selezionata: " + destinazione);

                } else {
                    // Reset: nuova origine
                    origine = new Cella(riga, colonna);
                    destinazione = null;
                    matriceContesto = null;
                    matriceComplemento = null;
                    analizzatoreCammini.ripristinaCoerenzaOstacoli();
                    analizzatoreCammini.ripristinaCoerenzaTipi();
                    etichettaInfo.setText("Origine aggiornata: " + origine + ". Ora scegli Destinazione");
                    int[][] matriceTipi = new int[numeroRighe][numeroColonne];
                    matriceContesto = analizzatoreCammini.calcolaContesto(griglia, origine, matriceTipi);
                    matriceComplemento = analizzatoreCammini.calcolaComplemento(griglia, origine, matriceTipi);
                    frontiera = analizzatoreCammini.calcolaFrontiera(matriceTipi, griglia);
                }

                // Aggiorna il pannello e ridisegna
                pannelloGriglia.aggiornaDati(origine, destinazione, matriceContesto, matriceComplemento, frontiera);
            }
        });

        pulsanteAvvio.addActionListener(e -> {
            // Controllo: origine e destinazione devono essere selezionate
            if (origine == null || destinazione == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Seleziona prima Origine e Destinazione",
                    "Errore di input",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Ripristina la coerenza dei dati prima di avviare l’algoritmo
            analizzatoreCammini.ripristinaCoerenzaOstacoli();
            analizzatoreCammini.ripristinaCoerenzaTipi();

            // Pannello di dialogo per conferma e inserimento limite di tempo
            JPanel pannelloDialogo = new JPanel(new BorderLayout(5, 5));
            JPanel pannelloInput = new JPanel(new GridLayout(2, 2, 5, 5));

            pannelloInput.add(new JLabel("Confermi l’avvio dell’algoritmo?"));
            pannelloInput.add(new JLabel()); // spazio vuoto

            pannelloInput.add(new JLabel("Limite di tempo (ms):"));
            JTextField campoTempo = new JTextField("1000000"); // valore predefinito: 1.000.000
            pannelloInput.add(campoTempo);

            pannelloDialogo.add(pannelloInput, BorderLayout.CENTER);

            int risultato = JOptionPane.showConfirmDialog(
                this,
                pannelloDialogo,
                "Conferma avvio",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (risultato == JOptionPane.OK_OPTION) {
                long limiteTempo = 1000000;
                try {
                    limiteTempo = Long.parseLong(campoTempo.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Limite di tempo non valido, uso valore predefinito: 1.000.000 ms."
                    );
                }

                // Reset del flag di stop
                stopRichiesto = false;

                // Avvio del worker per il calcolo del percorso
                lavoratore = new LavoratorePercorso(analizzatoreCammini, griglia, limiteTempo);
                pulsanteAvvio.setEnabled(false);
                pulsanteStop.setEnabled(true);
                etichettaInfo.setText("Algoritmo in esecuzione…");
                lavoratore.execute();
            }
        });

        pulsanteStop.addActionListener(e -> {
            if (lavoratore != null) {
                // Ripristina la coerenza dei dati prima di fermare l’algoritmo
                analizzatoreCammini.ripristinaCoerenzaOstacoli();
                analizzatoreCammini.ripristinaCoerenzaTipi();

                // Imposta il flag di stop e interrompe il worker
                stopRichiesto = true;
                //lavoratore.cancel(true);

                // Aggiorna l’etichetta informativa
                etichettaInfo.setText("Interruzione in corso…");
            }
        });
	}

    // Metodo di utilità per creare una voce della legenda
    private JPanel creaVoceLegenda(Color colore, String testo) {
        JLabel box = new JLabel();
        box.setOpaque(true);
        box.setBackground(colore);
        box.setPreferredSize(new Dimension(20, 20));
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel voce = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voce.add(box);
        voce.add(new JLabel(testo));
        return voce;
    }
    
    // Worker Swing per l’esecuzione del calcolo del cammino in background (=> l'utente può fermare il processo senza problemi)
    private class LavoratorePercorso extends SwingWorker<RisultatoCammino, String> {
        private Griglia griglia;
        private long limiteTempo; // in millisecondi
        private long tempoInizio;
        private AnalizzatoreCammini analizzatoreCammini;
        RisultatoCammino risultato;

        public LavoratorePercorso(AnalizzatoreCammini analizzatoreCammini, Griglia griglia, long limiteTempo) {
            this.griglia = griglia;
            this.limiteTempo = limiteTempo;
            this.analizzatoreCammini = analizzatoreCammini;
            this.risultato = null;
            System.out.println("Limite di tempo impostato a " + limiteTempo + " ms");
        }

        @Override
        protected RisultatoCammino doInBackground() throws Exception {
        	// Inizializzazione timer
        	analizzatoreCammini.calls.set(0); // Inizializzazione ricorsioni
        	analizzatoreCammini.resetContatori();
        	tempoInizio = System.currentTimeMillis();
        	long t1 = System.nanoTime(); // Inizializzazione timer

        	// Esecuzione del calcolo del cammino minimo
        	try{
        		this.risultato = analizzatoreCammini.camminoMin(
        				origine,
        				destinazione,
        				griglia,
        				() -> {
        					// Stop if user requested it or time limit elapsed
        					return stopRichiesto || (System.currentTimeMillis() - tempoInizio) >= limiteTempo;
        				}
        				);
        	} catch (StopException e){
        		this.risultato = e.partialResult;
        	}
        	
        	long t2 = System.nanoTime();
        	
        	// snapshot per thread-safety
        	boolean[][] ostacoliSnapshot = matriceOstacoli;

        	// lista finale
        	List<Cella> camminoCompleto = new ArrayList<>();
        	List<Landmark> seq = risultato.getSeq();

        	for (int i = 0; i < seq.size() - 1; i++) {
        	    Cella a = seq.get(i).getCella();
        	    Cella b = seq.get(i+1).getCella();
     	  
        	    List<Cella> parte = analizzatoreCammini.ricostruisciCammino(a, b, ostacoliSnapshot, true);
        	    
        	    if (parte == null || parte.isEmpty()) {
        	        // non esiste sottocammino fra questi due landmark → gestisci:
        	        // opzioni: abort, saltare, usare segmento rettilineo ignorando ostacoli, ecc.
        	        continue; // qui scegliamo di saltare
        	    }

        	    // unisci evitando duplicati consecutivi
        	    if (!camminoCompleto.isEmpty() && camminoCompleto.get(camminoCompleto.size()-1).equals(parte.get(0))) {
        	        camminoCompleto.addAll(parte.subList(1, parte.size()));
        	    } else {
        	        camminoCompleto.addAll(parte);
        	    }
        	}

        	pannelloGriglia.aggiornaCammino(camminoCompleto);
			
        	risultato.setTempo(t2 - t1);
        	System.out.println("Tempo: " + (t2 - t1) + " ns");

        	return risultato;
        }

        @Override
        protected void done() {
            // Riabilita i pulsanti
            pulsanteAvvio.setEnabled(true);
            pulsanteStop.setEnabled(false);

            RisultatoCammino risultato = null;
            boolean annullato = false;

            try {
                risultato = get();
            } catch (Exception ex) {
                annullato = true;
            }

            // Controllo stato finale
            if (annullato || stopRichiesto) {
                etichettaInfo.setText("Esecuzione interrotta da utente");
                mostraRiepilogo(risultato, 0);
            } else if ((System.currentTimeMillis() - tempoInizio) >= limiteTempo) {
                etichettaInfo.setText("Esecuzione interrotta da limite di tempo");
                mostraRiepilogo(risultato, 1);
            } else {
                etichettaInfo.setText("Esecuzione completata");
                mostraRiepilogo(risultato, 2);
            }
        }
    }
	
    // Mostra un dialogo riepilogativo al termine o all’interruzione dell’algoritmo
    private void mostraRiepilogo(RisultatoCammino risultato, int stato) {
        JDialog dialogo = new JDialog(this, "Finestra riepilogo", true);
        dialogo.setSize(400, 400);
        dialogo.setLayout(new BorderLayout());

        JTextArea areaTesto = new JTextArea();
        areaTesto.setEditable(false);

        if (stato == 0) {
            areaTesto.setText("PROCESSO INTERROTTO (INTERVENTO DELL’UTENTE)\n\nRIEPILOGO:\n"); 
        } else if (stato == 1) {
            areaTesto.setText("PROCESSO INTERROTTO (LIMITE DI TEMPO SCADUTO)\n\nRIEPILOGO:\n"); 
        } else if (stato == 2) {
            areaTesto.setText("PROCESSO COMPLETATO\n\nRIEPILOGO:\n");
        }
        if (risultato != null) {
            areaTesto.append(risultato.stampa());
        }

        dialogo.add(new JScrollPane(areaTesto), BorderLayout.CENTER);

        // Pulsante SALVA
        JButton pulsanteSalva = new JButton("Salva in .txt");
        pulsanteSalva.addActionListener(ev -> {
            JFileChooser selettoreFile = new JFileChooser();
            if (selettoreFile.showSaveDialog(dialogo) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter out = new PrintWriter(selettoreFile.getSelectedFile())) {
                    out.print(areaTesto.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogo, "Errore durante il salvataggio: " + ex.getMessage());
                }
            }
        });

        JPanel pannelloInferiore = new JPanel();
        pannelloInferiore.add(pulsanteSalva);

        dialogo.add(pannelloInferiore, BorderLayout.SOUTH);

        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
}