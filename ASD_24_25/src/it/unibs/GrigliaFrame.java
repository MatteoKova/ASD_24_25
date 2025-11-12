package it.unibs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
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

    private Grid grid;
    private Point origine = null;
    private Point destinazione = null;
    private Set<Point> contesto = null;
    private Set<Point> complemento = null;
    private JLabel infoLabel;
    private GrigliaPanel panel;

    public GrigliaFrame(Grid grid) {
        this.grid = grid;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new GrigliaPanel(grid);
        add(panel, BorderLayout.CENTER);

        // Legenda laterale
        JPanel legendaPanel = new JPanel();
        legendaPanel.setLayout(new BoxLayout(legendaPanel, BoxLayout.Y_AXIS));

        legendaPanel.add(creaVoceLegenda(Color.BLUE.darker(), "Ostacolo"));
        legendaPanel.add(creaVoceLegenda(Color.GREEN.darker(), "Contesto"));
        legendaPanel.add(creaVoceLegenda(Color.YELLOW, "Asse"));
        legendaPanel.add(creaVoceLegenda(Color.ORANGE, "Complemento"));
        legendaPanel.add(creaVoceLegenda(Color.RED, "Origine (O)"));
        legendaPanel.add(creaVoceLegenda(Color.BLACK, "Destinazione (D)"));
        legendaPanel.add(creaVoceLegenda(Color.WHITE, "Cella libera"));

        add(legendaPanel, BorderLayout.EAST);

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

        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int cellSize = panel.getCellSize();
                if (cellSize <= 0) return; // difesa
                int c = e.getX() / cellSize;
                int r = e.getY() / cellSize;
                if (!grid.inBounds(r, c) || !grid.isFree(r, c)) return;

                if (origine == null) {
                    origine = new Point(c, r);
                    infoLabel.setText("Origine selezionata: " + origine + ". Ora scegli Destinazione.");
                } else if (destinazione == null) {
                    destinazione = new Point(c, r);
                    infoLabel.setText("Destinazione: " + destinazione);
                    contesto = AnalizzatoreCammini.calcolaContesto(grid, origine);
                    complemento = AnalizzatoreCammini.calcolaComplemento(grid, origine);
                    double dist = AnalizzatoreCammini.dlib(origine, destinazione);
                    infoLabel.setText("dlib(O,D) = " + String.format("%.3f", dist));
                } else {
                    // reset: nuova origine
                    origine = new Point(c, r);
                    destinazione = null;
                    contesto = null;
                    complemento = null;
                    infoLabel.setText("Origine aggiornata: " + origine + ". Ora scegli Destinazione.");
                }

                // Comunica i dati aggiornati al pannello e chiedi repaint
                panel.aggiornaDati(origine, destinazione, contesto, complemento);
            }
        });
    }

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