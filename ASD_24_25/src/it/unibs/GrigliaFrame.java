package it.unibs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

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

public class GrigliaFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    //private Griglia griglia;
    private Cella origine = null;
    private Cella destinazione = null;
    private Set<Cella> contesto = null;
    private Set<Cella> complemento = null;
    private Set<Cella> frontiera = null;
        private JButton startButton;
private JButton stopButton;
private PathWorker worker;
private volatile boolean stopRequested = false;
    //private Set<Map.Entry<Cella, Integer>> frontiera = null;
    private JLabel infoLabel;
    private GrigliaPanel panel;

    public GrigliaFrame(Griglia griglia) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new GrigliaPanel(griglia);
        add(panel, BorderLayout.CENTER);

        // Legenda laterale
        JPanel legendaPanel = new JPanel();
        legendaPanel.setLayout(new BoxLayout(legendaPanel, BoxLayout.Y_AXIS));

                // --- Bottom control panel (Start / Stop) ---
        JPanel controls = new JPanel(new FlowLayout());
        startButton = new JButton("Start Eval");
        stopButton = new JButton("STOP");
        stopButton.setEnabled(false);

        controls.add(startButton);
        controls.add(stopButton);

        add(controls, BorderLayout.SOUTH);


        legendaPanel.add(creaVoceLegenda(Color.BLUE.darker(), "Ostacolo"));
        legendaPanel.add(creaVoceLegenda(Color.GREEN.darker(), "Contesto"));
        legendaPanel.add(creaVoceLegenda(Color.YELLOW, "Asse"));
        legendaPanel.add(creaVoceLegenda(Color.ORANGE, "Complemento"));
        legendaPanel.add(creaVoceLegenda(Color.RED, "Origine (O)"));
        legendaPanel.add(creaVoceLegenda(Color.RED, "Destinazione (D)"));
        legendaPanel.add(creaVoceLegenda(Color.WHITE, "Cella libera"));

        add(legendaPanel, BorderLayout.EAST);

        // Etichetta sopra
        infoLabel = new JLabel("Clicca per selezionare Origine (O) e Destinazione (D)");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setForeground(Color.BLACK);
        infoLabel.setOpaque(true);
        infoLabel.setBackground(Color.LIGHT_GRAY);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        setSize(900, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        // Permette di registrare gli eventi del mouse
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	int cellSize = panel.getCellSize();
            	if (cellSize <= 0) return;

            	int offsetX = 40; // stesso valore usato in paintComponent
            	int offsetY = 40;

            	int colonna = (e.getX() - offsetX) / cellSize;
            	int riga = (e.getY() - offsetY) / cellSize;
            	if (!griglia.isNeiMargini(riga, colonna) || !griglia.isLibera(riga, colonna)) return;
            	
                if (origine == null) {
                	origine = griglia.getCella(riga, colonna);
                    infoLabel.setText("Origine selezionata: Cella[riga=" + origine.getRiga() + ", colonna=" + origine.getColonna() + "]. Ora scegli Destinazione.");
                    Set<Cella> ostacoli = AnalizzatoreCammini.trovaOstacoli(griglia);
                    contesto = AnalizzatoreCammini.calcolaContesto(griglia, origine, ostacoli);
                    complemento = AnalizzatoreCammini.calcolaComplemento(griglia, origine, ostacoli);
                    frontiera = AnalizzatoreCammini.calcolaFrontiera(contesto, complemento, griglia);    
                    
                } else if (destinazione == null) {
                    destinazione = griglia.getCella(riga, colonna);
                    infoLabel.setText("Destinazione: " + destinazione);
                    //questo va inserito nel worker
                    /*
                    Set<Cella> ostacoli = AnalizzatoreCammini.trovaOstacoli(griglia);
                    RisultatoCammino cammino = AnalizzatoreCammini.camminoMin(origine, destinazione, griglia, ostacoli);
                    double dist = cammino.getLunghezza();
                    infoLabel.setText("dlib(O,D) = " + String.format("%.3f", dist));
                    cammino.stampa();
                    */
                } else {
                    // Reset: nuova origine
                    origine = griglia.getCella(riga, colonna);
                    destinazione = null;
                    contesto = null;
                    complemento = null;
                    infoLabel.setText("Origine aggiornata: Cella[riga=" + origine.getRiga() + ", colonna=" + origine.getColonna() + "]. Ora scegli Destinazione.");
                    Set<Cella> ostacoli = AnalizzatoreCammini.trovaOstacoli(griglia);
                    contesto = AnalizzatoreCammini.calcolaContesto(griglia, origine, ostacoli);
                    complemento = AnalizzatoreCammini.calcolaComplemento(griglia, origine, ostacoli);
                    frontiera = AnalizzatoreCammini.calcolaFrontiera(contesto, complemento, griglia);
                }

                // Comunica i dati aggiornati al pannello e chiedi repaint
                panel.aggiornaDati(origine, destinazione, contesto, complemento, frontiera);
            }
        });
        
        startButton.addActionListener(e -> {



    if (origine == null || destinazione == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima Origine e Destinazione.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

    // Create a dialog to confirm and enter time limit
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));

    inputPanel.add(new JLabel("Confirm start algorithm?"));
    inputPanel.add(new JLabel()); // spacer

    inputPanel.add(new JLabel("Time limit (ms):"));
    JTextField timeField = new JTextField("1000000"); // default 1,000,000
    inputPanel.add(timeField);

    panel.add(inputPanel, BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(this, panel, "Confirm Start", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
        long timeLimit = 1000000;
        try {
            timeLimit = Long.parseLong(timeField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid time limit, using default 1,000,000 ms.");
        }

      
        stopRequested = false; // reset stop flag

      
            worker = new PathWorker(griglia, timeLimit);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            infoLabel.setText("Esecuzione algoritmo…");
            worker.execute();
    }
});




            

         
     

    stopButton.addActionListener(e -> {
    if (worker != null) {
        stopRequested = true;
        worker.cancel(true);
        infoLabel.setText("Stopping…");
    }
});


    }

    // Definisce il formato della legenda
    private JPanel creaVoceLegenda(Color colore, String testo) {
        JPanel voce = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel box = new JLabel();
        box.setOpaque(true);
        box.setBackground(colore);
        box.setPreferredSize(new Dimension(20, 20));
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        voce.add(box);
        voce.add(new JLabel(testo));
        return voce;
    }

    private class PathWorker extends SwingWorker<RisultatoCammino, String> {
         Griglia griglia;
    long timeLimit; // in milliseconds
    long startTime;

    public PathWorker(Griglia griglia, long timeLimit) {
        this.griglia = griglia;
        this.timeLimit = timeLimit;
    }

    @Override
    protected RisultatoCammino doInBackground() throws Exception {
        Set<Cella> ostacoli = AnalizzatoreCammini.trovaOstacoli(this.griglia);

        startTime = System.currentTimeMillis();

        
        
        AnalizzatoreCammini.calls.set(0); // Inizializzazione ricorsioni
        long t1 = System.nanoTime(); // Inizializzazione timer
        
        RisultatoCammino r1 = AnalizzatoreCammini.camminoMin(origine, destinazione, griglia, ostacoli, () -> {
                // Stop if user requested or time limit elapsed
                return stopRequested || (System.currentTimeMillis() - startTime) >= timeLimit;
            }
        );
        
        long t2 = System.nanoTime();

        System.out.println("Tempo: " + (t2 - t1) + " ns"); // per controllare se meglio la condizione a riga 16 o 17
        System.out.println("Ricorsioni: " + AnalizzatoreCammini.calls.get()); // per controllare se meglio la condizione a riga 16 o 17
        
        return r1;
    }

    @Override
    protected void done() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        RisultatoCammino result = null;
        boolean cancelled = false;

        try {
            result = get();
        } catch (Exception ex) {
            cancelled = true;
        }

        if (cancelled || stopRequested || (System.currentTimeMillis() - startTime) >= timeLimit) {
            infoLabel.setText("Evaluation stopped.");
            showSummaryDialog(result, true);
        } else {
            infoLabel.setText("Evaluation finished.");
            showSummaryDialog(result, false);
        }
    }
}

private void showSummaryDialog(RisultatoCammino result, boolean stopped) {
    JDialog dialog = new JDialog(this, "Summary", true);
    dialog.setSize(400, 300);
    dialog.setLayout(new BorderLayout());

    JTextArea area = new JTextArea();
    area.setEditable(false);

    if (stopped) {
        area.setText("PROCESS STOPPED BY USER.\n\nPartial summary:\n\n");
       if (result != null) {
            area.append(result.stampa());
        }
    } else {
        area.setText("PROCESS COMPLETED.\n\nSummary:\n\n");
        if (result != null) {
            area.append(result.stampa());
        }
    }

    dialog.add(new JScrollPane(area), BorderLayout.CENTER);

    // SAVE button
    JButton saveBtn = new JButton("Save to .txt");
    saveBtn.addActionListener(ev -> {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter out = new java.io.PrintWriter(chooser.getSelectedFile())) {
                out.print(area.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving file: " + ex.getMessage());
            }
        }
    });

    JPanel bottom = new JPanel();
    bottom.add(saveBtn);

    dialog.add(bottom, BorderLayout.SOUTH);

    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}


}