package it.unibs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GeneratoreGriglieOstacoli {
    public static class Config {
        public int rows = 20;
        public int cols = 20;
        public long seed = System.currentTimeMillis();
        public int numSemplici = 15;
        public int numAgglomerati = 10;
        public int numDiagonali = 5;
        public int numBarre = 5;
        public int numAnelli = 3;
    }

    public static class Grid {
        private final char[][] cells;
        public final int rows, cols;

        public Grid(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            this.cells = new char[rows][cols];
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    cells[r][c] = '.'; // '.' = cella libera
        }

        public boolean inBounds(int r, int c) {
            return r >= 0 && r < rows && c >= 0 && c < cols;
        }

        public boolean isFree(int r, int c) {
            return inBounds(r, c) && cells[r][c] == '.';
        }

        public void setObstacle(int r, int c) {
            if (inBounds(r, c)) cells[r][c] = '#';
        }

        public char get(int r, int c) {
            return cells[r][c];
        }

        public void saveToFile(String path) {
            try (FileWriter fw = new FileWriter(path)) {
                for (int r = 0; r < rows; r++) {
                    fw.write(cells[r]);
                    fw.write("\n");
                }
            } catch (IOException e) {
                System.err.println("Errore nel salvataggio griglia: " + e.getMessage());
            }
        }

        public void printToConsole() {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++)
                    System.out.print(cells[r][c]);
                System.out.println();
            }
        }
    }

    // --- Classe Generatore ---
    private final Config cfg;
    private final Random rnd;
    public final Grid grid;

    public GeneratoreGriglieOstacoli(Config cfg) {
        this.cfg = cfg;
        this.rnd = new Random(cfg.seed);
        this.grid = new Grid(cfg.rows, cfg.cols);
    }

    public void generateAll() {
        generaSemplici(cfg.numSemplici);
        generaAgglomerati(cfg.numAgglomerati);
        generaDiagonali(cfg.numDiagonali);
        generaBarre(cfg.numBarre);
        generaAnelli(cfg.numAnelli);
    }

    // --- Tipi di ostacoli ---
    private void generaSemplici(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(grid.rows);
            int c = rnd.nextInt(grid.cols);
            grid.setObstacle(r, c);
        }
    }

    private void generaAgglomerati(int n) {
        for (int i = 0; i < n; i++) {
            int baseR = rnd.nextInt(grid.rows);
            int baseC = rnd.nextInt(grid.cols);
            int dim = 2 + rnd.nextInt(3); // da 2 a 4 celle
            for (int j = 0; j < dim; j++) {
                int r = baseR + rnd.nextInt(2) - 1;
                int c = baseC + rnd.nextInt(2) - 1;
                if (grid.inBounds(r, c)) grid.setObstacle(r, c);
            }
        }
    }

    private void generaDiagonali(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(grid.rows - 3);
            int c = rnd.nextInt(grid.cols - 3);
            int len = 2 + rnd.nextInt(3);
            for (int k = 0; k < len; k++) {
                if (rnd.nextBoolean())
                    grid.setObstacle(r + k, c + k);
                else
                    grid.setObstacle(r + k, c - k >= 0 ? c - k : c + k);
            }
        }
    }

    private void generaBarre(int n) {
        for (int i = 0; i < n; i++) {
            boolean orizzontale = rnd.nextBoolean();
            int lung = 3 + rnd.nextInt(5);
            int spess = 1 + rnd.nextInt(2);
            int r = rnd.nextInt(grid.rows - (orizzontale ? spess : lung));
            int c = rnd.nextInt(grid.cols - (orizzontale ? lung : spess));
            for (int dr = 0; dr < (orizzontale ? spess : lung); dr++)
                for (int dc = 0; dc < (orizzontale ? lung : spess); dc++)
                    grid.setObstacle(r + dr, c + dc);
        }
    }

    private void generaAnelli(int n) {
        for (int i = 0; i < n; i++) {
            int r = rnd.nextInt(grid.rows - 6);
            int c = rnd.nextInt(grid.cols - 6);
            int size = 3 + rnd.nextInt(4);
            for (int k = 0; k < size; k++) {
                grid.setObstacle(r, c + k);
                grid.setObstacle(r + size - 1, c + k);
                grid.setObstacle(r + k, c);
                grid.setObstacle(r + k, c + size - 1);
            }
        }
    }
}