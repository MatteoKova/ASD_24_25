package it.unibs;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Inserimento dei parametri da parte dell'utente
            int rows = chiediIntero("Numero di righe della griglia:", 20);
            int cols = chiediIntero("Numero di colonne della griglia:", 20);
            long seed = chiediLong("Seed per la generazione (0 = casuale):", 0);

            Configurazione cfg = new Configurazione();
            cfg.rows = rows;
            cfg.cols = cols;
            if (seed != 0) cfg.seed = seed;

            GeneratoreGriglieOstacoli gen = new GeneratoreGriglieOstacoli(cfg);
            gen.generateAll();
            new GrigliaFrame(gen.grid);
        });
    }

	// Metodo per inserire le dimensioni della griglia
    private static int chiediIntero(String messaggio, int predefinito) {
        String s = JOptionPane.showInputDialog(null, messaggio, predefinito);
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return predefinito;
        }
    }

    // Metodo per inserire il seed per generare la griglia
    private static long chiediLong(String messaggio, long predefinito) {
        String s = JOptionPane.showInputDialog(null, messaggio, predefinito);
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return predefinito;
        }
    }
}