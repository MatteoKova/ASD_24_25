package it.unibs;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ItemEvent;

public class CreatoreGriglia extends JFrame {
    private static final long serialVersionUID = 1L;
	private JTextField campoRighe, campoColonne, campoSeed;
    private JButton pulsanteAnteprima, pulsanteConferma, pulsanteAnnulla;
    private JComboBox<String> boxDensita;
    private JPanel pannelloGriglia;
    private PannelloGrigliaManuale pannelloManuale;
    private boolean[][] grigliaBackup;

    private int righe, colonne;
    private long seed = 0;
    
    public CreatoreGriglia() {
        setTitle("Creazione Griglia");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Pannello form
        JPanel pannelloForm = new JPanel(new GridBagLayout());
        pannelloForm.setBorder(new TitledBorder("Parametri griglia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Numero righe
        gbc.gridx = 0; gbc.gridy = 0;
        pannelloForm.add(new JLabel("Numero di righe:"), gbc);
        campoRighe = new JTextField("10", 10);
        gbc.gridx = 1;
        pannelloForm.add(campoRighe, gbc);

        // Numero colonne
        gbc.gridx = 0; gbc.gridy = 1;
        pannelloForm.add(new JLabel("Numero di colonne:"), gbc);
        campoColonne = new JTextField("10", 10);
        gbc.gridx = 1;
        pannelloForm.add(campoColonne, gbc);

        // Densità ostacoli
        gbc.gridx = 0; gbc.gridy = 2;
        pannelloForm.add(new JLabel("Densità ostacoli:"), gbc);
        boxDensita = new JComboBox<>(new String[]{"Bassa", "Media", "Alta", "Personalizzata"});
        gbc.gridx = 1;
        pannelloForm.add(boxDensita, gbc);

        // Seed
        gbc.gridx = 0; gbc.gridy = 3;
        pannelloForm.add(new JLabel("Seed (0 = casuale):"), gbc);
        campoSeed = new JTextField("0", 10);
        gbc.gridx = 1;
        pannelloForm.add(campoSeed, gbc);

        // Pulsanti
        JPanel pannelloPulsanti = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pulsanteAnteprima = new JButton("Aggiorna anteprima");
        pulsanteConferma = new JButton("Conferma");
        pannelloPulsanti.add(pulsanteAnteprima);
        pannelloPulsanti.add(pulsanteConferma);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        pannelloForm.add(pannelloPulsanti, gbc);

        add(pannelloForm, BorderLayout.WEST);

        pannelloGriglia = new JPanel();
        pannelloGriglia.setBorder(new TitledBorder("Anteprima"));
        pannelloGriglia.setPreferredSize(new Dimension(400, 400)); // quadrato
        add(pannelloGriglia, BorderLayout.CENTER);

        // Azioni
        pulsanteAnteprima.addActionListener(e -> {
        	mostraAnteprima();
        	pulsanteConferma.setEnabled(true);
        });
        
        pulsanteConferma.addActionListener(e -> {
        	if (leggiParametri())
        		creaGriglia();
        	}
        );
        
        disattivaConferma();
        
        // Mostra anteprima iniziale con parametri di default
        mostraAnteprima();
    }
    
    // Se l'utente modifica un input, il pulsante di conferma si disattiva finché non aggiorna l'anteprima
    public void disattivaConferma() {
    	// --- LISTENER PER I CAMPI ---
        DocumentListener listenerCampi = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { pulsanteConferma.setEnabled(false); }
            @Override
            public void removeUpdate(DocumentEvent e) { pulsanteConferma.setEnabled(false); }
            @Override
            public void changedUpdate(DocumentEvent e) { pulsanteConferma.setEnabled(false); }
        };

        // Aggiungo il listener ai JTextField
        campoRighe.getDocument().addDocumentListener(listenerCampi);
        campoColonne.getDocument().addDocumentListener(listenerCampi);
        campoSeed.getDocument().addDocumentListener(listenerCampi);
        
        // Se hai anche una JComboBox per la densità:
        boxDensita.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pulsanteConferma.setEnabled(false);
            }
        });
    }
    
    // Metodo che recupera i dati inseriti dall'utente
    private boolean leggiParametri() {
        try {
            righe = Integer.parseInt(campoRighe.getText().trim());
            colonne = Integer.parseInt(campoColonne.getText().trim());
            seed = Long.parseLong(campoSeed.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Inserisci valori numerici validi per righe, colonne e seed",
                "Errore di input",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Metodo che chiama il generatore degli ostacoli
    private boolean[][] generaOstacoli() {
        leggiParametri();

        if (righe <= 0 || colonne <= 0) {
            JOptionPane.showMessageDialog(this, "La matrice deve avere un numero positivo di righe e e di colonne", "Errore input",
            		JOptionPane.ERROR_MESSAGE);
            return null;
        } else if (righe > 100 || colonne > 100) {
            JOptionPane.showMessageDialog(this, "La matrice non può avere più di 100 righe e 100 colonne", "Errore input",
            		JOptionPane.ERROR_MESSAGE);
            return null;
        }

        double densita = 0.1;
        String scelta = (String) boxDensita.getSelectedItem();
        if ("Media".equals(scelta)) densita = 0.25;
        if ("Alta".equals(scelta)) densita = 0.45;

        boolean[][] ostacoli = new boolean[righe][colonne];
        Configurazione cfg = new Configurazione();
        cfg.righe = righe;
        cfg.colonne = colonne;
        cfg.density = densita;
        if (seed != 0) cfg.seed = seed;

        GeneratoreGriglieOstacoli generatore = new GeneratoreGriglieOstacoli(cfg, ostacoli);
        generatore.generaTutto();
        return ostacoli;
    }
    
    // Metodo che si occupa della griglia visualizzabile in anteprima
    private void mostraAnteprima() {
        String scelta = (String) boxDensita.getSelectedItem();
        pannelloGriglia.removeAll();

        if ("Personalizzata".equals(scelta)) {
            pannelloGriglia.setLayout(new BorderLayout());
            pannelloGriglia.add(new JLabel("Anteprima non disponibile in modalità personalizzata",
            		SwingConstants.CENTER), BorderLayout.CENTER);
            grigliaBackup = null; // nessuna anteprima disponibile
        } else {
            boolean[][] ostacoli = generaOstacoli();
            if (ostacoli == null) return;
            
            // Salvo la griglia generata
            grigliaBackup = ostacoli;

            PannelloAnteprima anteprima = new PannelloAnteprima(ostacoli);
            pannelloGriglia.setLayout(new BorderLayout());
            pannelloGriglia.add(anteprima, BorderLayout.CENTER);
        }

        pannelloGriglia.revalidate();
        pannelloGriglia.repaint();
    }

    // Metodo che chiama il generatore di griglie
    private void creaGriglia() {
        String scelta = (String) boxDensita.getSelectedItem();
        if ("Personalizzata".equals(scelta)) {
            boolean[][] ostacoli = new boolean[righe][colonne];
            personalizzaOstacoli(ostacoli);
        } else {
        	// Se ho già un'anteprima, la riuso
            if (grigliaBackup != null) {
                new FinestraGriglia(grigliaBackup);
            } else {
	            boolean[][] ostacoli = generaOstacoli();
	            if (ostacoli != null) {
	                new FinestraGriglia(ostacoli);
	            }
            }
        }
    }

    // Metodo che si occupa delle griglie con ostacoli inseriti a mano dall'utente
    public void personalizzaOstacoli(boolean[][] ostacoli) {
        pannelloGriglia.removeAll();
        pannelloGriglia.setLayout(new BorderLayout());

        JPanel wrapperCentro = new JPanel(new GridBagLayout());
        pannelloManuale = new PannelloGrigliaManuale(righe, colonne);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;

        wrapperCentro.add(pannelloManuale, gbc);
        pannelloGriglia.add(wrapperCentro, BorderLayout.CENTER);

        JPanel pannelloBasso = new JPanel();
        pulsanteAnnulla = new JButton("Annulla");
        pulsanteConferma = new JButton("Conferma");

        pannelloBasso.add(pulsanteAnnulla);
        pannelloBasso.add(pulsanteConferma);
        pannelloGriglia.add(pannelloBasso, BorderLayout.SOUTH);

        grigliaBackup = new boolean[righe][colonne];

        pulsanteAnnulla.addActionListener(e -> pannelloManuale.setOstacoli(grigliaBackup));

        pulsanteConferma.addActionListener(e -> {
            boolean[][] obs = pannelloManuale.getOstacoli();
            for (int r = 0; r < righe; r++)
                for (int c = 0; c < colonne; c++)
                    if (obs[r][c]) ostacoli[r][c] = true;

            new FinestraGriglia(ostacoli);
        });

        pannelloGriglia.revalidate();
        pannelloGriglia.repaint();
    }
    
    // Classe interna che rappresenta il pannello dell'anteprima
    class PannelloAnteprima extends JPanel {
        private static final long serialVersionUID = 1L;
		private boolean[][] ostacoli;

        public PannelloAnteprima(boolean[][] ostacoli) {
            this.ostacoli = ostacoli;
        }

        public void setOstacoli(boolean[][] ostacoli) {
            this.ostacoli = ostacoli;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (ostacoli == null) return;

            int righe = ostacoli.length;
            int colonne = ostacoli[0].length;

            // Calcola lato cella quadrata
            int cellSize = Math.min(getWidth() / colonne, getHeight() / righe);

            // Calcola offset per centrare la griglia
            int offsetX = (getWidth() - (colonne * cellSize)) / 2;
            int offsetY = (getHeight() - (righe * cellSize)) / 2;

            for (int r = 0; r < righe; r++) {
                for (int c = 0; c < colonne; c++) {
                    int x = offsetX + c * cellSize;
                    int y = offsetY + r * cellSize;
                    g.setColor(ostacoli[r][c] ? Color.BLUE.darker() : Color.WHITE);
                    g.fillRect(x, y, cellSize, cellSize);
                    g.setColor(Color.GRAY);
                    g.drawRect(x, y, cellSize, cellSize);
                }
            }
        }
    }
}