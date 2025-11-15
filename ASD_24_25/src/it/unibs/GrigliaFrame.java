package it.unibs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class GrigliaFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private Griglia grid;
    private Cella origine = null;
    private Cella destinazione = null;
    private Set<Cella> contesto = null;
    private Set<Cella> complemento = null;
    private JLabel infoLabel;
    private GrigliaPanel panel;

    public GrigliaFrame(Griglia griglia) {
        this.grid = griglia;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new GrigliaPanel(griglia);
        add(panel, BorderLayout.CENTER);

        // Legenda laterale
        JPanel legendaPanel = new JPanel();
        legendaPanel.setLayout(new BoxLayout(legendaPanel, BoxLayout.Y_AXIS));

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
                int colonna = e.getX() / cellSize;
                int riga = e.getY() / cellSize;
                if (!griglia.inBounds(riga, colonna) || !griglia.isFree(riga, colonna)) return;

                if (origine == null) {
                    origine = new Cella(colonna, riga);
                    infoLabel.setText("Origine selezionata: Cella[x=" + origine.getX() + ", y=" + origine.getY() + "]. Ora scegli Destinazione.");
                    contesto = AnalizzatoreCammini.calcolaContesto(griglia, origine);
                    complemento = AnalizzatoreCammini.calcolaComplemento(griglia, origine);
                } else if (destinazione == null) {
                    destinazione = new Cella(colonna, riga);
                    infoLabel.setText("Destinazione: " + destinazione);
                    double dist = AnalizzatoreCammini.dlib(origine, destinazione, contesto, complemento);
                    infoLabel.setText("dlib(O,D) = " + String.format("%.3f", dist));
                } else {
                    // Reset: nuova origine
                    origine = new Cella(colonna, riga);
                    destinazione = null;
                    contesto = null;
                    complemento = null;
                    infoLabel.setText("Origine aggiornata: Cella[x=" + origine.getX() + ", y=" + origine.getY() + "]. Ora scegli Destinazione.");
                    contesto = AnalizzatoreCammini.calcolaContesto(griglia, origine);
                    complemento = AnalizzatoreCammini.calcolaComplemento(griglia, origine);
                }

                // Comunica i dati aggiornati al pannello e chiedi repaint
                panel.aggiornaDati(origine, destinazione, contesto, complemento);
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
}