package it.unibs;

import java.io.FileWriter;
import java.io.IOException;

public class Griglia {
    private final char[][] cells;
    public final int rows, cols;

    // Creazione griglia formato command line che verra' convertito in GUI
    public Griglia(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new char[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c] = '.'; // '.' = cella libera
    }

    // 
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // Riconosce le celle libere
    public boolean isFree(int r, int c) {
        return inBounds(r, c) && cells[r][c] == '.';
    }

    // Riconosce gli ostacoli
    public void setObstacle(int r, int c) {
        if (inBounds(r, c)) cells[r][c] = '#';
    }

    // Restituisce la cella usando le coordinate
    public char get(int r, int c) {
        return cells[r][c];
    }
    /*
    // Salvataggio della griglia su file esterno
    public void saveToFile(String path) {
        try (FileWriter fw = new FileWriter(path)) {
            for (int r = 0; r < rows; r++) {
                fw.write(cells[r]);
                fw.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio griglia: " + e.getMessage());
        }
    }*/
}