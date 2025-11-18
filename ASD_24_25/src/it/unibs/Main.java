package it.unibs;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
    
        SwingUtilities.invokeLater(() -> {

            GrillCreator creator= new GrillCreator();
            creator.setVisible(true);
            // Inserimento dei parametri da parte dell'utente
           /*  int rows = chiediIntero("Numero di righe della griglia:", 20);
            int cols = chiediIntero("Numero di colonne della griglia:", 20);
            long seed = chiediLong("Seed per la generazione (0 = casuale):", 0);
            
            Configurazione cfg = new Configurazione();
            cfg.righe = righe;
            cfg.colonne = colonne;
            if (seed != 0) cfg.seed = seed;

            GeneratoreGriglieOstacoli gen = new GeneratoreGriglieOstacoli(cfg);
            gen.generateAll();
            new GrigliaFrame(gen.getGrid());*/
        });
    }

	// Metodo per inserire le dimensioni della griglia
    private static int chiediIntero(String messaggio, int predefinito) {
        int valore = predefinito;
        boolean valido = false;

        while (!valido) {
            String s = JOptionPane.showInputDialog(null, messaggio, predefinito);
            if (s == null) {
                // Se l'utente chiude la finestra, esci con il valore predefinito
                return predefinito;
            }
            try {
                valore = Integer.parseInt(s);
                if (valore > 0 && valore <= 100) {
                    valido = true; // input corretto
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Errore: inserisci un numero tra 1 e 100.",
                            "Valore non valido",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Errore: inserisci un numero intero valido.",
                        "Input non valido",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return valore;
    }

    // Metodo per inserire il seed per generare la griglia
    private static long chiediLong(String messaggio, long predefinito) {
        long valore = predefinito;
        boolean valido = false;

        while (!valido) {
            String s = JOptionPane.showInputDialog(null, messaggio, predefinito);
            if (s == null) {
                // Se l'utente chiude la finestra, ritorna il valore predefinito
                return predefinito;
            }
            try {
                valore = Long.parseLong(s);
                // Se parseLong non lancia eccezione, significa che il numero è nel range di long
                valido = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Errore: inserisci un numero intero valido tra "
                        + Long.MIN_VALUE + " e " + Long.MAX_VALUE + ".",
                        "Input non valido",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return valore;
    }
}