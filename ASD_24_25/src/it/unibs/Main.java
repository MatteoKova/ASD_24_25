package it.unibs;

import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import javax.swing.*;

public class Main {
	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Chiedi parametri all'utente
            int rows = chiediIntero("Numero di righe della griglia:", 20);
            int cols = chiediIntero("Numero di colonne della griglia:", 20);
            long seed = chiediLong("Seed per la generazione (0 = casuale):", 0);

            GeneratoreGriglieOstacoli.Config cfg = new GeneratoreGriglieOstacoli.Config();
            cfg.rows = rows;
            cfg.cols = cols;
            if (seed != 0) cfg.seed = seed; // se 0, usa un seed casuale

            GeneratoreGriglieOstacoli gen = new GeneratoreGriglieOstacoli(cfg);
            gen.generateAll();
            new GrigliaFrame(gen.grid);
        });
    }

    private static int chiediIntero(String messaggio, int predefinito) {
        String s = JOptionPane.showInputDialog(null, messaggio, predefinito);
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return predefinito;
        }
    }

    private static long chiediLong(String messaggio, long predefinito) {
        String s = JOptionPane.showInputDialog(null, messaggio, predefinito);
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return predefinito;
        }
    }
}

class GrigliaFrame extends JFrame {
	private static final long serialVersionUID = 1L;
    private GeneratoreGriglieOstacoli.Grid grid;
    private Point origine = null;
    private Point destinazione = null;
    private Set<Point> contesto = null;
    private Set<Point> complemento = null;
    private JLabel infoLabel;

    public GrigliaFrame(GeneratoreGriglieOstacoli.Grid grid) {
        this.grid = grid;
        setTitle("Analisi Griglia - Cammini Liberi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        GrigliaPanel panel = new GrigliaPanel();
        add(panel, BorderLayout.CENTER);

        infoLabel = new JLabel("Clicca per selezionare Origine (O) e Destinazione (D)");
        add(infoLabel, BorderLayout.SOUTH);

        setSize(800, 800);
        setVisible(true);

        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int cellSize = panel.getCellSize();
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
                    // reset
                    origine = new Point(c, r);
                    destinazione = null;
                    contesto = null;
                    complemento = null;
                    infoLabel.setText("Origine aggiornata: " + origine + ". Ora scegli Destinazione.");
                }
                panel.repaint();
            }
        });
    }

    class GrigliaPanel extends JPanel {
    	private static final long serialVersionUID = 1L;
        private int cellSize = 20;
        public int getCellSize() { return cellSize; }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int r = 0; r < grid.rows; r++) {
                for (int c = 0; c < grid.cols; c++) {
                    if (!grid.isFree(r, c)) g.setColor(Color.BLUE.darker());
                    else g.setColor(Color.WHITE);
                    g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize);
                }
            }

            if (contesto != null) {
                g.setColor(Color.GREEN);
                for (Point p : contesto)
                    g.fillRect(p.x * cellSize, p.y * cellSize, cellSize, cellSize);
            }

            if (complemento != null) {
                g.setColor(Color.ORANGE);
                for (Point p : complemento)
                    g.fillRect(p.x * cellSize, p.y * cellSize, cellSize, cellSize);
            }
            
            if (origine != null) {
                // assi
                g.setColor(Color.YELLOW);
                for (int c = 0; c < grid.cols; c++)
                    g.fillRect(c * cellSize, origine.y * cellSize, cellSize, cellSize);
                for (int r = 0; r < grid.rows; r++)
                    g.fillRect(origine.x * cellSize, r * cellSize, cellSize, cellSize);
            }
            
            if (origine != null) {
                g.setColor(Color.RED);
                g.fillRect(origine.x * cellSize, origine.y * cellSize, cellSize, cellSize);
            }
            
            if (destinazione != null) {
                g.setColor(Color.BLACK);
                g.fillRect(destinazione.x * cellSize, destinazione.y * cellSize, cellSize, cellSize);
            }
        }
    }
}