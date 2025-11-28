package it.unibs;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class PannelloGriglia extends JPanel {
    private static final long serialVersionUID = 1L;
  
    private Griglia griglia;

    private boolean[][] matriceOstacoli;
    private int[][] matriceContesto;
    private int[][] matriceComplemento;
    private Set<Landmark> frontiera;

    private Cella origine;
    private Cella destinazione;

    private List<Cella> camminoOrdinato = new ArrayList<>();
    
    public PannelloGriglia(Griglia griglia, boolean[][] matriceOstacoli, int[][] matriceContesto,
			int[][] matriceComplemento, Set<Landmark> frontiera) {
		this.griglia = griglia;
		this.matriceOstacoli = matriceOstacoli;
		this.matriceContesto = matriceContesto;
		this.matriceComplemento = matriceComplemento;
		this.frontiera = frontiera;
		
		setBackground(Color.WHITE);
	}

    
    /* ============================================================
       === 1) METODI PUBBLICI PER AGGIORNAMENTO DATI
       ============================================================ */

    public void aggiornaDati(Cella origine,
                             Cella destinazione,
                             int[][] contesto,
                             int[][] complemento,
                             Set<Landmark> frontiera)
    {
        this.origine = origine;
        this.destinazione = destinazione;
        this.matriceContesto = contesto;
        this.matriceComplemento = complemento;
        this.frontiera = frontiera;
        repaint();
    }

    public void aggiornaCammino(List<Cella> cammino) {
        this.camminoOrdinato = cammino;
        repaint();
    }
    
    public Point cellaDaCoordinatePixel(int px, int py) {
        Graphics g = getGraphics();
        if (g == null) return null;

        InfoDisposizione infoProv = InfoDisposizione.creaProvvisorio(getWidth(), getHeight(), griglia, g);
        if (infoProv.dimCella <= 0) return null;

        Font fontAssi = getFontAssi(infoProv.dimCella);
        FontMetrics fmAssi = g.getFontMetrics(fontAssi);

        InfoDisposizione info = new InfoDisposizione(getWidth(), getHeight(), griglia, fmAssi);

        int col = (px - info.offsetX) / info.dimCella;
        int row = (py - info.offsetY) / info.dimCella;

        if (col < 0 || row < 0 ||
            col >= griglia.getColonne() ||
            row >= griglia.getRighe())
            return null;

        return new Point(col, row);
    }
    
    private Font getFontAssi(int dimCella) {
        // proporzionale alla cella, con soglia minima per leggibilità
        int size = Math.max(14, (int)(dimCella * 0.5));
        return new Font("SansSerif", Font.BOLD, size);
    }
    

    /* ============================================================
       === 3) PAINT COMPONENT
       ============================================================ */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // layout provvisorio
        InfoDisposizione infoProv = InfoDisposizione.creaProvvisorio(getWidth(), getHeight(), griglia, g);
        if (infoProv.dimCella <= 0) return;

        // font coerente con cella provvisoria
        Font fontAssi = getFontAssi(infoProv.dimCella);
        g.setFont(fontAssi);
        FontMetrics fmAssi = g.getFontMetrics(fontAssi);

        // layout finale con font corretto
        InfoDisposizione info = new InfoDisposizione(getWidth(), getHeight(), griglia, fmAssi);
        int dimCella = info.dimCella;
        int offsetX = info.offsetX;
        int offsetY = info.offsetY;

        // disegno etichette
        drawEtichetteColonne(g, fmAssi, info);
        drawEtichetteRighe(g, fmAssi, info);
    
        for (int y = 0; y < griglia.getRighe(); y++) {
            for (int x = 0; x < griglia.getColonne(); x++) {

                int px = offsetX + x * dimCella;
                int py = offsetY + y * dimCella;

                // sfondo (ostacolo o libero)
                if (matriceOstacoli[y][x]) {
                    g.setColor(Color.BLUE.darker());
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(px, py, dimCella, dimCella);

                // contesto / complemento
                if (matriceContesto[y][x] == 1) {
                    g.setColor(Color.GREEN.darker());
                    g.fillRect(px, py, dimCella, dimCella);
                } else if (matriceComplemento[y][x] == 2) {
                    g.setColor(Color.ORANGE);
                    g.fillRect(px, py, dimCella, dimCella);
                }

                // assi rispetto all'origine
                if (origine != null && destinazione == null &&
                    (y == origine.getRiga() || x == origine.getColonna()) &&
                    !matriceOstacoli[y][x]) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(px, py, dimCella, dimCella);
                }

                // origine
                if (origine != null && x == origine.getColonna() && y == origine.getRiga()) {
                    g.setColor(Color.RED);
                    g.fillRect(px, py, dimCella, dimCella);
                    drawLetteraCella(g, "O", px, py, dimCella);
                }

                // destinazione
                if (destinazione != null && x == destinazione.getColonna() && y == destinazione.getRiga()) {
                    g.setColor(Color.RED);
                    g.fillRect(px, py, dimCella, dimCella);
                    drawLetteraCella(g, "D", px, py, dimCella);
                }

                // frontiera
                if (frontiera != null) {
                    for (Landmark L : frontiera) {
                        Cella c = L.getCella();
                        if (c.getColonna() == x && c.getRiga() == y) {
                            g.setColor(Color.BLACK);
                            int pad = 2;
                            g.drawOval(px + pad, py + pad, dimCella - 2*pad, dimCella - 2*pad);
                        }
                    }
                }

                // bordo cella
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(px, py, dimCella, dimCella);
            }
        }

        /* ------------------------------
           3. Disegno cammino UNA SOLA VOLTA
           ------------------------------ */

        Graphics2D g2 = (Graphics2D) g;
        drawCammino(g2, dimCella, offsetX, offsetY);
    }


    /* ============================================================
       === 4) METODI DI DISEGNO
       ============================================================ */
    
    private void drawEtichetteColonne(Graphics g, FontMetrics fm, InfoDisposizione info) {
        int dimCella = info.dimCella;
        
        // parametri estetici
        final int gapNumeroTacchetta = 2; // distanza tra numero e tacchetta
        final int taccaLen = 6;           // lunghezza tacchetta

        for (int x = 0; x < griglia.getColonne(); x++) {
            if (griglia.getColonne() > 25 && x % 5 != 0) continue;

            String txt = String.valueOf(x);
            int w = fm.stringWidth(txt);

            // numero: sopra la tacchetta
            int px = info.offsetX + x * dimCella + (dimCella - w) / 2;
            int py = info.offsetY - fm.getDescent() - (taccaLen + gapNumeroTacchetta);
            g.drawString(txt, px, py);

            // tacchetta: subito sopra la griglia
            int cx = info.offsetX + x * dimCella + dimCella / 2;
            int yTaccaTop = info.offsetY - taccaLen;
            int yTaccaBottom = info.offsetY - 1;
            g.drawLine(cx, yTaccaTop, cx, yTaccaBottom);
        }
    }
    
    private void drawEtichetteRighe(Graphics g, FontMetrics fm, InfoDisposizione info) {
        int dimCella = info.dimCella;

        // parametri estetici
        final int gapNumeroTacchetta = 4; // distanza tra numero e tacchetta
        final int taccaLen = 6;           // lunghezza tacchetta

        for (int y = 0; y < griglia.getRighe(); y++) {
            if (griglia.getRighe() > 25 && y % 5 != 0) continue;

            String txt = String.valueOf(y);

            // posizione verticale centrata nella cella
            int cy = info.offsetY + y * dimCella + dimCella / 2;
            int py = cy + (fm.getAscent() - fm.getDescent()) / 2;

            // numero: più a sinistra
            int pxNumero = info.offsetX - taccaLen - gapNumeroTacchetta - fm.stringWidth(txt);
            g.drawString(txt, pxNumero, py);

            // tacchetta: subito a destra del numero, prima della griglia
            int xTaccaLeft = info.offsetX - taccaLen;
            int xTaccaRight = info.offsetX - 2;
            g.drawLine(xTaccaLeft, cy, xTaccaRight, cy);
        }
    }

    private void drawLetteraCella(Graphics g, String lettera, int px, int py, int dim) {
        int size = Math.max(8, (int)(dim * 0.6));
        Font f = g.getFont().deriveFont((float) size);
        g.setFont(f);

        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(lettera);
        int h = fm.getAscent();

        int tx = px + (dim - w)/2;
        int ty = py + (dim - h)/2 + fm.getAscent();

        g.setColor(Color.WHITE);
        g.drawString(lettera, tx, ty);
    }

    private void drawCammino(Graphics2D g2, int dim, int offsetX, int offsetY) {
        if (camminoOrdinato == null || camminoOrdinato.size() < 2) return;

        g2.setStroke(new BasicStroke(Math.max(2, dim/5),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.BLACK);

        for (int i = 0; i < camminoOrdinato.size() - 1; i++) {

            Cella a = camminoOrdinato.get(i);
            Cella b = camminoOrdinato.get(i + 1);

            int ax = offsetX + a.getColonna() * dim + dim/2;
            int ay = offsetY + a.getRiga() * dim + dim/2;

            int bx = offsetX + b.getColonna() * dim + dim/2;
            int by = offsetY + b.getRiga() * dim + dim/2;

            g2.drawLine(ax, ay, bx, by);
        }
    }
}