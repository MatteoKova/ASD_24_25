package it.unibs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PannelloGrigliaManuale extends JPanel {

    private static final long serialVersionUID = 1L;

    private boolean[][] obstacles;
    private int rows, cols;

    private boolean painting = false;   // left-drag painting ON
    private boolean erasing = false;    // right-drag erasing ON

    public PannelloGrigliaManuale(int r, int c) {
        this.rows = r;
        this.cols = c;
        obstacles = new boolean[rows][cols];

        setBackground(Color.WHITE);

        MouseAdapter listener = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int row = getRow(e);
                int col = getCol(e);

                if (!valid(row, col)) return;

                // LEFT CLICK = toggle or begin painting
                if (SwingUtilities.isLeftMouseButton(e)) {

                    // TOGGLE ON CLICK
                    obstacles[row][col] = !obstacles[row][col];

                    // START DRAG PAINTING (paint = force obstacle ON)
                    painting = true;
                } 

                // RIGHT CLICK = toggle or begin erasing
                else if (SwingUtilities.isRightMouseButton(e)) {

                    // TOGGLE ON CLICK
                    obstacles[row][col] = !obstacles[row][col];

                    // START DRAG ERASING (erase = force obstacle OFF)
                    erasing = true;
                }

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                painting = false;
                erasing = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int row = getRow(e);
                int col = getCol(e);

                if (!valid(row, col)) return;

                if (painting) {
                    obstacles[row][col] = true;   // draw
                }
                else if (erasing) {
                    obstacles[row][col] = false;  // erase
                }

                repaint();
            }
        };

        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    // ----------------- Helpers -----------------------

    private int getCellSize() {
        if (cols == 0 || rows == 0) return 0;
        return Math.min(getWidth() / cols, getHeight() / rows);
    }

    private int getRow(MouseEvent e) { return e.getY() / getCellSize(); }

    private int getCol(MouseEvent e) { return e.getX() / getCellSize(); }

    private boolean valid(int r, int c) {
        return (r >= 0 && r < rows && c >= 0 && c < cols);
    }

    public boolean[][] getOstacoli() {
        return obstacles;
    }

    public void setOstacoli(boolean[][] grid) {
        this.obstacles = grid;
        repaint();
    }

    // ----------------- Painting -----------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cell = getCellSize();
        if (cell <= 0) return;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                int x = c * cell;
                int y = r * cell;

                if (obstacles[r][c])
                    g.setColor(Color.BLUE.darker());
                else
                    g.setColor(Color.WHITE);

                g.fillRect(x, y, cell, cell);

                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, cell, cell);
            }
        }
    }
}
