package it.unibs;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Set;

public class PannelloGriglia extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final int OFFSET = 40;
    
    // Dati della griglia
    private Griglia griglia;
    private Cella origine, destinazione;
    private int[][] matriceContesto, matriceComplemento;
    private Set<Landmark> frontiera;
    private boolean[][] matriceOstacoli;

    public PannelloGriglia(Griglia griglia, boolean[][] matriceOstacoli, int[][] matriceContesto,
    						int[][] matriceComplemento, Set<Landmark> frontiera) {
        this.griglia = griglia;
        this.matriceOstacoli = matriceOstacoli;
        this.matriceContesto = matriceContesto;
        this.matriceComplemento = matriceComplemento;
        this.frontiera = frontiera;
        
        setBackground(Color.WHITE);
        // Dimensione preferita: almeno 400x400 oppure proporzionata alla griglia
        setPreferredSize(new Dimension(Math.max(400, griglia.getColonne() * 20 + 50), Math.max(400, griglia.getRighe() * 20 + 50)
        ));
    }

    // Aggiorna i dati e ridisegna il pannello
    public void aggiornaDati(Cella origine, Cella destinazione, int[][] matriceContesto,
    						int[][] matriceComplemento, Set<Landmark> frontiera) {
        this.origine = origine;
        this.destinazione = destinazione;
        this.matriceContesto = matriceContesto;
        this.matriceComplemento = matriceComplemento;
        this.frontiera = frontiera;
        repaint();
    }

    // Calcola la dimensione di una cella in base allo spazio disponibile
    public int getDimensioneCella() {
        int larghezza = getWidth() - OFFSET;
        int altezza = getHeight() - OFFSET; 
        if (griglia.getColonne() == 0 || griglia.getRighe() == 0) return 0;
        return Math.max(4, Math.min(larghezza / griglia.getColonne(), altezza / griglia.getRighe()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int dimCella = getDimensioneCella();
        if (dimCella <= 0) return;

        // Font per etichette assi
        Font fontAssi = new Font("SansSerif", Font.BOLD, Math.max(12, dimCella / 2));
        g.setFont(fontAssi);
        FontMetrics fm = g.getFontMetrics();
        
        // Etichette colonne (solo multipli di 5)
        for (int x = 0; x < griglia.getColonne(); x++) {
        	if(griglia.getColonne() > 25)
        		if (x % 5 != 0)
        			continue;

            String etichetta = String.valueOf(x);
            int larghezzaTesto = fm.stringWidth(etichetta);

            int posX = OFFSET + x * dimCella + (dimCella - larghezzaTesto) / 2;
            int posY = OFFSET - 10;

            g.setColor(Color.BLACK);
            g.drawString(etichetta, posX, posY);

            // Tacche verticali
            int barX = OFFSET + x * dimCella + dimCella / 2;
            g.drawLine(barX, OFFSET - 8, barX, OFFSET - 2);
        }

        // Etichette righe (solo multipli di 5)
        int spazioEtichetta = 6; 
        int lunghezzaTacca = Math.max(6, dimCella / 3);

        for (int y = 0; y < griglia.getRighe(); y++) {
        	if(griglia.getRighe() > 25)
        		if (y % 5 != 0)
        			continue;

            String etichetta = String.valueOf(y);
            int altezzaTesto = fm.getAscent();

            int posX = OFFSET - spazioEtichetta - fm.stringWidth(etichetta);
            int posY = OFFSET + y * dimCella + (dimCella + altezzaTesto) / 2 - fm.getDescent();

            g.setColor(Color.BLACK);
            g.drawString(etichetta, posX, posY);

            // Tacche orizzontali
            int centroY = OFFSET + y * dimCella + dimCella / 2;
            int barXStart = OFFSET - spazioEtichetta;
            int barXEnd = Math.min(OFFSET - spazioEtichetta + lunghezzaTacca, OFFSET - 2);
            g.drawLine(barXStart, centroY, barXEnd, centroY);
        }

        // Disegno celle
        for (int x = 0; x < griglia.getColonne(); x++) {
            for (int y = 0; y < griglia.getRighe(); y++) {
                int posX = OFFSET + x * dimCella;
                int posY = OFFSET + y * dimCella;

                // Sfondo ostacoli o celle libere
                if (matriceOstacoli[y][x]) {
                    g.setColor(Color.BLUE.darker());
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(posX, posY, dimCella, dimCella);

                // Evidenzia contesto o complemento
                if (matriceContesto[y][x] == 1) {
                    g.setColor(Color.GREEN.darker());
                    g.fillRect(posX, posY, dimCella, dimCella);
                } else if (matriceComplemento[y][x] == 2) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(posX, posY, dimCella, dimCella);
                }

                // Evidenzia assi rispetto all'origine
                if (origine != null && destinazione == null &&
                    (y == origine.getRiga() || x == origine.getColonna()) &&
                    !matriceOstacoli[y][x]) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(posX, posY, dimCella, dimCella);
                }
                
                // Origine
                if (origine != null && x == origine.getColonna() && y == origine.getRiga()) {
                    g.fillRect(posX, posY, dimCella, dimCella);
                    disegnaLetteraCella(g, "O", posX, posY, dimCella);
                }

                // Frontiera
                if (frontiera != null) {
                    for (Landmark punto : frontiera) {
                        if (punto.getCella().equals(new Cella(y, x))) {
                            g.setColor(Color.BLACK);
                            int padding = 2;
                            g.drawOval(posX + padding, posY + padding,
                                       dimCella - 2 * padding, dimCella - 2 * padding);
                        }
                    }
                }
                
                // Destinazione
                if (destinazione != null && x == destinazione.getColonna() && y == destinazione.getRiga()) {
                    g.fillRect(posX, posY, dimCella, dimCella);
                    disegnaLetteraCella(g, "D", posX, posY, dimCella);
                }

                // Bordo cella
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(posX, posY, dimCella, dimCella);
            }
        }
    }

    // Disegna una lettera centrata all’interno di una cella
    private void disegnaLetteraCella(Graphics g, String lettera,
                                     int posX, int posY, int dimCella) {
        int dimensioneFont = Math.max(8, (int)(dimCella * 0.6));
        Font fontScalato = g.getFont().deriveFont((float) dimensioneFont);
        g.setFont(fontScalato);

        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(lettera, g);

        int larghezzaTesto = (int) rect.getWidth();
        int altezzaTesto = (int) rect.getHeight();

        int xTesto = posX + (dimCella - larghezzaTesto) / 2;
        int yTesto = posY + (dimCella - altezzaTesto) / 2 + fm.getAscent();

        g.setColor(Color.RED);
        g.drawString(lettera, xTesto, yTesto);
    }
}
