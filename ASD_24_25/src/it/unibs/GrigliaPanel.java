package it.unibs;

import javax.swing.*;

import java.awt.*;
import java.util.Set;

public class GrigliaPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private Griglia griglia;
    private Cella origine, destinazione;
    private Set<Cella> contesto, complemento;
    //private Set<Map.Entry<Cella, Integer>> frontiera;
    private Set<Cella> frontiera;


    public GrigliaPanel(Griglia grid) {
        this.griglia = grid;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(Math.max(400, grid.getColonne() * 20), Math.max(400, grid.getRighe() * 20)));
    }

    // Aggiornamento dati e repaint
    public void aggiornaDati(Cella origine, Cella destinazione, Set<Cella> contesto, Set<Cella> complemento, Set<Cella> frontiera) {
        this.origine = origine;
        this.destinazione = destinazione;
        this.contesto = contesto;
        this.complemento = complemento;
        this.frontiera = frontiera;
        repaint();
    }

    // Restituisce le dimensioni di una cella
    public int getCellSize() {
        int larghezza = getWidth();
        int altezza = getHeight();
        if (griglia.getColonne() == 0 || griglia.getRighe() == 0) return 0;
        return Math.max(4, Math.min(larghezza / griglia.getColonne(), altezza / griglia.getRighe()));
    }

	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int dimCella = getCellSize();
        if (dimCella <= 0) return;

        // Preparazione font per eventuali lettere dentro le celle
        Font cellFont = new Font("SansSerif", Font.BOLD, Math.max(12, dimCella / 2));
        g.setFont(cellFont);
        FontMetrics fm = g.getFontMetrics();

        for (int x = 0; x < griglia.getColonne(); x++) {
        	for (int y = 0; y < griglia.getRighe(); y++) {
                int xGUI = x * dimCella;
                int yGUI = y * dimCella;

                // Sfondo base
                if (!griglia.isLibera(y, x)) {
                    g.setColor(Color.BLUE.darker());
                    g.fillRect(xGUI, yGUI, dimCella, dimCella);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(xGUI, yGUI, dimCella, dimCella);
                }

                // Overlay contesto / complemento
                if (contesto != null && contesto.contains(griglia.getCella(y, x))) {
                    g.setColor(Color.GREEN.darker());
                    g.fillRect(xGUI, yGUI, dimCella, dimCella);
                } else if (complemento != null && complemento.contains(griglia.getCella(y, x))) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(xGUI, yGUI, dimCella, dimCella);
                }

                // Assi rispetto all'origine
                if (origine != null && destinazione == null && (y == origine.getRiga() || x == origine.getColonna()) && griglia.isLibera(y, x)) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(xGUI, yGUI, dimCella, dimCella);
                }

                // Origine
                if (origine != null && x == origine.getColonna() && y == origine.getRiga()) {
                    g.fillRect(xGUI, yGUI, dimCella, dimCella);
                    String oTesto = "O";
                    int tw = fm.stringWidth(oTesto);
                    int th = fm.getAscent();
                    g.setColor(Color.RED);
                    g.drawString(oTesto, xGUI + (dimCella - tw) / 2, yGUI + (dimCella + th) / 2 - fm.getDescent());
                }

                // Frontiera
                if (frontiera != null) {
                	//Set<Cella>frontieraCelle = frontiera.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
	                //if (frontieraCelle != null && frontieraCelle.contains(griglia.getCella(c, r))) {
                	if (frontiera.contains(griglia.getCella(y, x))) {
	                    g.setColor(Color.BLACK);
	                    int padding = 1;
	                    g.drawOval(xGUI + padding, yGUI + padding, dimCella - 2 * padding, dimCella - 2 * padding);
	                }
                }

                // Destinazione
                if (destinazione != null && x == destinazione.getColonna() && y == destinazione.getRiga()) {
                    g.fillRect(xGUI, yGUI, dimCella, dimCella);
                    String dTesto = "D";
                    int tw = fm.stringWidth(dTesto);
                    int th = fm.getAscent();
                    g.setColor(Color.RED);
                    g.drawString(dTesto, xGUI + (dimCella - tw) / 2, yGUI + (dimCella + th) / 2 - fm.getDescent());
                }

                // Bordo cella
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(xGUI, yGUI, dimCella, dimCella);
            }
        }
    }
}