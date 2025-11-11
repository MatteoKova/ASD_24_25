package it.unibs;

/*
GeneratoreGriglieObstacoliGUI.java

Versione con interfaccia grafica (Swing) per configurare e generare griglie con ostacoli.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GeneratoreGriglieObstacoliGUI extends JFrame {
    private Generatore generatore;
    private JPanel gridPanel;
    private JTextField rowsField, colsField, seedField;
    private JButton generateButton;
    private Config cfg = new Config();

    public GeneratoreGriglieObstacoliGUI() {
        setTitle("Generatore Griglie con Ostacoli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        controlPanel.add(new JLabel("Righe:"));
        rowsField = new JTextField(String.valueOf(cfg.rows));
        controlPanel.add(rowsField);

        controlPanel.add(new JLabel("Colonne:"));
        colsField = new JTextField(String.valueOf(cfg.cols));
        controlPanel.add(colsField);

        controlPanel.add(new JLabel("Seed (0=random):"));
        seedField = new JTextField(String.valueOf(cfg.seed));
        controlPanel.add(seedField);

        generateButton = new JButton("Genera");
        controlPanel.add(generateButton);

        add(controlPanel, BorderLayout.NORTH);

        gridPanel = new JPanel();
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        generateButton.addActionListener(e -> generaGriglia());

        setSize(900, 700);
        setVisible(true);
    }

    private void generaGriglia() {
        try {
            cfg.rows = Integer.parseInt(rowsField.getText());
            cfg.cols = Integer.parseInt(colsField.getText());
            cfg.seed = Long.parseLong(seedField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valori non validi.");
            return;
        }

        generatore = new Generatore(cfg);
        generatore.generateAll();

        mostraGriglia(generatore.grid);
    }

    private void mostraGriglia(Grid grid) {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(grid.rows, grid.cols, 1, 1));

        for (int i = 0; i < grid.rows; i++) {
            for (int j = 0; j < grid.cols; j++) {
                JPanel cell = new JPanel();
                if (grid.g[i][j] == 1) {
                    cell.setBackground(Color.DARK_GRAY);
                } else {
                    cell.setBackground(Color.WHITE);
                }
                gridPanel.add(cell);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // --- Classi interne ---

    static class Config {
        int rows = 30;
        int cols = 50;
        long seed = 0;
        int singleCount = 40;
        int agglomerateCount = 12;
        int agglomerateMaxSize = 5;
        int diagonalCount = 8;
        int diagonalMaxLen = 10;
        int barCount = 10;
        int barMaxLen = 15;
        int barMaxThickness = 3;
        int ringCount = 4;
        int ringMaxRadius = 8;
        int blobCount = 6;
        int blobMaxSize = 12;
        int maxAttemptsPerPlacement = 200;
    }

    static class Grid {
        final int rows, cols;
        final int[][] g;
        Grid(int r, int c){ rows=r; cols=c; g=new int[r][c]; }
        boolean inBounds(int r,int c){ return r>=0 && r<rows && c>=0 && c<cols; }
        boolean isFree(int r,int c){ return inBounds(r,c) && g[r][c]==0; }
        void setBlocked(int r,int c){ if(inBounds(r,c)) g[r][c]=1; }
    }

    static class Generatore {
        java.util.Random rnd;
        Config cfg;
        Grid grid;

        Generatore(Config cfg){
            this.cfg = cfg;
            this.grid = new Grid(cfg.rows,cfg.cols);
            this.rnd = (cfg.seed==0)? new java.util.Random(): new java.util.Random(cfg.seed);
        }

        boolean tryApply(java.util.List<int[]> cells){
            for(int[] p: cells){ int r=p[0], c=p[1]; if(!grid.inBounds(r,c) || !grid.isFree(r,c)) return false; }
            for(int[] p: cells) grid.setBlocked(p[0],p[1]);
            return true;
        }

        void placeSingles(int count){
            int attempts=0; int placed=0;
            while(placed<count && attempts < cfg.maxAttemptsPerPlacement*count){
                int r = rnd.nextInt(grid.rows);
                int c = rnd.nextInt(grid.cols);
                attempts++;
                if(grid.isFree(r,c)){ grid.setBlocked(r,c); placed++; }
            }
        }

        void placeAgglomerates(int count, int maxSize){
            for(int k=0;k<count;k++){
                int attempts=0;
                boolean done=false;
                while(!done && attempts < cfg.maxAttemptsPerPlacement){
                    attempts++;
                    int r = rnd.nextInt(grid.rows);
                    int c = rnd.nextInt(grid.cols);
                    if(!grid.isFree(r,c)) continue;
                    int size = 2 + rnd.nextInt(Math.max(1, maxSize-1));
                    java.util.Set<Integer> cells = new java.util.HashSet<>();
                    cells.add(r*grid.cols + c);
                    java.util.ArrayList<int[]> frontier = new java.util.ArrayList<>();
                    frontier.add(new int[]{r,c});
                    while(cells.size() < size && !frontier.isEmpty()){
                        int idx = rnd.nextInt(frontier.size());
                        int[] cur = frontier.get(idx);
                        int cr=cur[0], cc=cur[1];
                        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                        int[] d = dirs[rnd.nextInt(dirs.length)];
                        int nr=cr+d[0], nc=cc+d[1];
                        if(grid.inBounds(nr,nc) && grid.isFree(nr,nc) && !cells.contains(nr*grid.cols+nc)){
                            cells.add(nr*grid.cols+nc);
                            frontier.add(new int[]{nr,nc});
                        } else {
                            if(rnd.nextDouble() < 0.3) frontier.remove(idx);
                        }
                    }
                    if(cells.size()>=1){
                        java.util.List<int[]> toPlace = new java.util.ArrayList<>();
                        for(int v: cells) toPlace.add(new int[]{v/grid.cols, v%grid.cols});
                        if(tryApply(toPlace)) done=true;
                    }
                }
            }
        }

        void placeDiagonals(int count, int maxLen){
            for(int k=0;k<count;k++){
                int attempts=0; boolean placed=false;
                while(!placed && attempts < cfg.maxAttemptsPerPlacement){
                    attempts++;
                    int len = 2 + rnd.nextInt(Math.max(1, maxLen-1));
                    int dir = rnd.nextBoolean()?1:-1;
                    int r = rnd.nextInt(grid.rows);
                    int c = rnd.nextInt(grid.cols);
                    java.util.List<int[]> cells = new java.util.ArrayList<>();
                    for(int i=0;i<len;i++){
                        int nr = r + i;
                        int nc = c + i*dir;
                        cells.add(new int[]{nr,nc});
                    }
                    if(tryApply(cells)) placed=true;
                }
            }
        }

        void placeBars(int count, int maxLen, int maxThickness){
            for(int k=0;k<count;k++){
                int attempts=0; boolean placed=false;
                while(!placed && attempts < cfg.maxAttemptsPerPlacement){
                    attempts++;
                    int len = 2 + rnd.nextInt(Math.max(1, maxLen-1));
                    int thickness = 1 + rnd.nextInt(Math.max(1, maxThickness));
                    boolean horizontal = rnd.nextBoolean();
                    int r = rnd.nextInt(grid.rows);
                    int c = rnd.nextInt(grid.cols);
                    java.util.List<int[]> cells = new java.util.ArrayList<>();
                    for(int i=0;i<len;i++){
                        for(int t=0;t<thickness;t++){
                            int rr = r + (horizontal? t : i);
                            int cc = c + (horizontal? i : t);
                            cells.add(new int[]{rr,cc});
                        }
                    }
                    if(tryApply(cells)) placed=true;
                }
            }
        }

        void placeRings(int count, int maxRadius){
            for(int k=0;k<count;k++){
                int attempts=0; boolean placed=false;
                while(!placed && attempts < cfg.maxAttemptsPerPlacement){
                    attempts++;
                    int halfW = 2 + rnd.nextInt(Math.max(1, Math.min(maxRadius, grid.cols/4)-1));
                    int halfH = 2 + rnd.nextInt(Math.max(1, Math.min(maxRadius, grid.rows/4)-1));
                    int centerR = rnd.nextInt(grid.rows);
                    int centerC = rnd.nextInt(grid.cols);
                    int top = centerR - halfH; int bottom = centerR + halfH;
                    int left = centerC - halfW; int right = centerC + halfW;
                    if(top<1 || left<1 || bottom>=grid.rows-1 || right>=grid.cols-1) continue;
                    java.util.List<int[]> cells = new java.util.ArrayList<>();
                    for(int cc=left; cc<=right; cc++){ cells.add(new int[]{top,cc}); cells.add(new int[]{bottom,cc}); }
                    for(int rr=top+1; rr<=bottom-1; rr++){ cells.add(new int[]{rr,left}); cells.add(new int[]{rr,right}); }
                    if(tryApply(cells)) placed=true;
                }
            }
        }

        void placeBlobs(int count, int maxSize){
            for(int k=0;k<count;k++){
                int attempts=0; boolean placed=false;
                while(!placed && attempts < cfg.maxAttemptsPerPlacement){
                    attempts++;
                    int r = rnd.nextInt(grid.rows);
                    int c = rnd.nextInt(grid.cols);
                    if(!grid.isFree(r,c)) continue;
                    int target = 2 + rnd.nextInt(Math.max(1, maxSize-1));
                    java.util.Set<Integer> cells = new java.util.HashSet<>();
                    cells.add(r*grid.cols + c);
                    int cr=r, cc=c;
                    int steps=0;
                    while(cells.size() < target && steps < target*10){
                        steps++;
                        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                        int[] d = dirs[rnd.nextInt(dirs.length)];
                        int nr=cr+d[0], nc=cc+d[1];
                        if(grid.inBounds(nr,nc) && grid.isFree(nr,nc)){
                            cells.add(nr*grid.cols + nc);
                            cr=nr; cc=nc;
                        } else {
                            break;
                        }
                    }
                    if(cells.size()>=1){
                        java.util.List<int[]> toPlace = new java.util.ArrayList<>();
                        for(int v: cells) toPlace.add(new int[]{v/grid.cols, v%grid.cols});
                        if(tryApply(toPlace)) placed=true;
                    }
                }
            }
        }

        void generateAll(){
            placeRings(cfg.ringCount, cfg.ringMaxRadius);
            placeBars(cfg.barCount, cfg.barMaxLen, cfg.barMaxThickness);
            placeAgglomerates(cfg.agglomerateCount, cfg.agglomerateMaxSize);
            placeBlobs(cfg.blobCount, cfg.blobMaxSize);
            placeDiagonals(cfg.diagonalCount, cfg.diagonalMaxLen);
            placeSingles(cfg.singleCount);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GeneratoreGriglieObstacoliGUI::new);
    }
}