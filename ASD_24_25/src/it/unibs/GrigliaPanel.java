package it.unibs;

import javax.swing.*;

import java.awt.*;
import java.util.Set;

public class GrigliaPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private Griglia grid;
    private Point origine, destinazione;
    private Set<Point> contesto, complemento;

    public GrigliaPanel(Griglia grid) {
        this.grid = grid;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(Math.max(400, grid.cols * 20), Math.max(400, grid.rows * 20)));
    }

    // Aggiornamento dati e repaint
    public void aggiornaDati(Point origine, Point destinazione, Set<Point> contesto, Set<Point> complemento) {
        this.origine = origine;
        this.destinazione = destinazione;
        this.contesto = contesto;
        this.complemento = complemento;
        repaint();
    }

    // Restituisce le dimensioni di una cella
    public int getCellSize() {
        int w = getWidth();
        int h = getHeight();
        if (grid.cols == 0 || grid.rows == 0) return 0;
        return Math.max(4, Math.min(w / grid.cols, h / grid.rows));
    }

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellSize = getCellSize();
        if (cellSize <= 0) return;

        // Preparazione font per eventuali lettere dentro le celle
        Font cellFont = new Font("SansSerif", Font.BOLD, Math.max(12, cellSize / 2));
        g.setFont(cellFont);
        FontMetrics fm = g.getFontMetrics();

        for (int r = 0; r < grid.rows; r++) {
            for (int c = 0; c < grid.cols; c++) {
                int x = c * cellSize;
                int y = r * cellSize;

                // Sfondo base
                if (!grid.isFree(r, c)) {
                    g.setColor(Color.BLUE.darker());
                    g.fillRect(x, y, cellSize, cellSize);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, cellSize, cellSize);
                }

                // Overlay contesto / complemento
                if (contesto != null && contesto.contains(new Point(c, r))) {
                    g.setColor(Color.GREEN.darker());
                    g.fillRect(x, y, cellSize, cellSize);
                } else if (complemento != null && complemento.contains(new Point(c, r))) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(x, y, cellSize, cellSize);
                }

                // Assi rispetto all'origine
                if (origine != null && (r == origine.y || c == origine.x) && grid.isFree(r, c)) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(x, y, cellSize, cellSize);
                }

                // Origine
                if (origine != null && c == origine.x && r == origine.y) {
                    g.setColor(Color.RED);
                    g.fillRect(x, y, cellSize, cellSize);
                    String oText = "O";
                    int tw = fm.stringWidth(oText);
                    int th = fm.getAscent();
                    g.setColor(Color.WHITE);
                    g.drawString(oText, x + (cellSize - tw) / 2, y + (cellSize + th) / 2 - fm.getDescent());
                }

                // Destinazione
                if (destinazione != null && c == destinazione.x && r == destinazione.y) {
                    g.setColor(Color.RED);
                    g.fillRect(x, y, cellSize, cellSize);
                    String dText = "D";
                    int tw = fm.stringWidth(dText);
                    int th = fm.getAscent();
                    g.setColor(Color.WHITE);
                    g.drawString(dText, x + (cellSize - tw) / 2, y + (cellSize + th) / 2 - fm.getDescent());
                }

                // Bordo cella
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
    }
}