package it.unibs;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Chiedi parametri all'utente
            int rows = chiediIntero("Numero di righe della griglia:", 20);
            int cols = chiediIntero("Numero di colonne della griglia:", 20);
            long seed = chiediLong("Seed per la generazione (0 = casuale):", 0);

            Config cfg = new Config();
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