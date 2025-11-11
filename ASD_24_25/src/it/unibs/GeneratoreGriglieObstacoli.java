package it.unibs;

/*
GeneratoreGriglieObstacoli.java

Contenuto:
 - Pseudocodice (in alto)
 - Implementazione Java di un generatore (single-file) che produce griglie NxM
   con celle non attraversabili secondo varie tipologie di ostacoli:
     * singole (1 cella)
     * agglomerati (cluster ortogonali: 2-4 celle o più)
     * diagonali (celle disposte in diagonale; si toccano solo agli spigoli)
     * anelli (delimitatori di aree chiuse: zona attraversabile racchiusa da un anello)
     * barre (rettangoli di lunghezza/spessore casuali)
     * blob casuali (walk-based)

 - L'utente può configurare dimensioni griglia, seed, e parametri per ogni tipo di ostacolo
 - Output su console ('.' = libero, '#' = ostacolo). Possibilità di salvare su file.

Uso (esempio):
  java GeneratoreGriglieObstacoli [rows] [cols] [seed]
  Esempio: java GeneratoreGriglieObstacoli 40 60 12345

Nota: questo file è pensato come punto di partenza per sperimentazioni e 
permette facilmente di variare parametri nel main o tramite la classe Config.
*/

/* PSEUDOCODICE

INPUT: rows, cols, seed, config (percentuali/numeri per ogni ostacolo)
CREA grid[rows][cols] inizializzata a 0 (libera)
INIZIALIZZA Random con seed
PER ogni tipologia di ostacolo in una lista configurata:
    calcola quante istanze/probabilità usare in base a config e dimensione griglia
    per i=1..numero_istanze:
        prova a generare un'istanza valida (più tentativi)
        --- per ogni tipo: costruisci la forma (lista di celle)
        --- controlla che tutte le celle siano dentro griglia e libere
        --- applica la forma (setta a 1)
se richiesto, esegui postprocessing (es. assicurare connettività della area libera) -- opzionale
STAMPA/SALVA grid

STRATEGIE DI POSIZIONAMENTO:
 - SingleCell: scegli cella libera a caso
 - Agglomerato: scegli cella seed, espandi ortogonalmente fino alla dimensione scelta
 - Diagonale: scegli direzione diagonale e lunghezza, piazza celle in (r+i, c+i) o (r+i, c-i)
 - Barra: scegli lunghezza L e spessore S, scegli orientamento orizz/vert e posiziona rectangle
 - Anello: scegli centro e raggio (o half-width), scrivi perimetro di un rettangolo/cerchio
 - Blob (random walk): partire da seed e camminare aggiungendo nuove celle

VERIFICHE:
 - Controlla collisioni con celle già occupate
 - Evita di posizionare anelli se nasconderebbero completamente la griglia o non lascerebbero spazio interno
 - Limita tentativi -> se fallisce skip o riduci dimensione
*/

public class GeneratoreGriglieObstacoli {
    // RAPPRESENTAZIONE: 0 = libero, 1 = ostacolo
    static class Config {
        int rows = 30;
        int cols = 50;
        long seed = System.currentTimeMillis();

        // Percentuali o numeri assoluti stimati (adattabili)
        int singleCount = 40;          // numero stimato di celle singole
        int agglomerateCount = 12;     // numero di agglomerati
        int agglomerateMaxSize = 5;    // max celle per agglomerato
        int diagonalCount = 8;         // numero di diagonali
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
        void setFree(int r,int c){ if(inBounds(r,c)) g[r][c]=0; }
        int countFree(){ int s=0; for(int i=0;i<rows;i++) for(int j=0;j<cols;j++) if(g[i][j]==0) s++; return s; }
        void print(){
            for(int i=0;i<rows;i++){
                StringBuilder sb = new StringBuilder();
                for(int j=0;j<cols;j++) sb.append(g[i][j]==0?'.':'#');
                System.out.println(sb.toString());
            }
        }
    }

    java.util.Random rnd;
    Config cfg;
    Grid grid;

    public GeneratoreGriglieObstacoli(Config cfg){
        this.cfg = cfg;
        this.grid = new Grid(cfg.rows,cfg.cols);
        this.rnd = (cfg.seed==0)? new java.util.Random(): new java.util.Random(cfg.seed);
    }

    // Helper: prova ad applicare una forma (lista di celle). Ritorna true se applicata.
    boolean tryApply(java.util.List<int[]> cells){
        for(int[] p: cells){ int r=p[0], c=p[1]; if(!grid.inBounds(r,c) || !grid.isFree(r,c)) return false; }
        for(int[] p: cells) grid.setBlocked(p[0],p[1]);
        return true;
    }

    // 1) single cell
    void placeSingles(int count){
        int attempts=0; int placed=0;
        while(placed<count && attempts < cfg.maxAttemptsPerPlacement*count){
            int r = rnd.nextInt(grid.rows);
            int c = rnd.nextInt(grid.cols);
            attempts++;
            if(grid.isFree(r,c)){ grid.setBlocked(r,c); placed++; }
        }
    }

    // 2) agglomerate (orthogonal connected cluster)
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
                // expand orthogonally
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
                        // try other existing frontier items
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

    // 3) diagonal (touch only at corners) -> place along direction (1,1) or (1,-1)
    void placeDiagonals(int count, int maxLen){
        for(int k=0;k<count;k++){
            int attempts=0; boolean placed=false;
            while(!placed && attempts < cfg.maxAttemptsPerPlacement){
                attempts++;
                int len = 2 + rnd.nextInt(Math.max(1, maxLen-1));
                int dir = rnd.nextBoolean()?1:-1; // (1,1) or (1,-1)
                int r = rnd.nextInt(grid.rows);
                int c = rnd.nextInt(grid.cols);
                java.util.List<int[]> cells = new java.util.ArrayList<>();
                for(int i=0;i<len;i++){
                    int nr = r + i;
                    int nc = c + i*dir;
                    cells.add(new int[]{nr,nc});
                }
                // check they are within and free
                if(tryApply(cells)) placed=true;
            }
        }
    }

    // 4) bar (rectangle with orientation, length, thickness)
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

    // 5) ring (delimitatore che crea area interna libera) - drawn as rectangular ring for simplicity
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
                // ensure ring fits
                if(top<1 || left<1 || bottom>=grid.rows-1 || right>=grid.cols-1) continue;
                java.util.List<int[]> cells = new java.util.ArrayList<>();
                // top/bottom edges
                for(int cc=left; cc<=right; cc++){ cells.add(new int[]{top,cc}); cells.add(new int[]{bottom,cc}); }
                // left/right edges
                for(int rr=top+1; rr<=bottom-1; rr++){ cells.add(new int[]{rr,left}); cells.add(new int[]{rr,right}); }
                // avoid overlapping internal area too small
                int innerW = (right-left-1); int innerH=(bottom-top-1);
                if(innerW<2 || innerH<2) continue;
                if(tryApply(cells)) placed=true;
            }
        }
    }

    // 6) blob (random walk)
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
                        // random jump
                        cr = r + rnd.nextInt(Math.max(1, Math.min(3, grid.rows-1)))-1;
                        cc = c + rnd.nextInt(Math.max(1, Math.min(3, grid.cols-1)))-1;
                        if(!grid.inBounds(cr,cc) || !grid.isFree(cr,cc)) break;
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

    // Funzione che coordina tutto
    public void generateAll(){
        // Order matters: place big shapes first to avoid fragmentation
        placeRings(cfg.ringCount, cfg.ringMaxRadius);
        placeBars(cfg.barCount, cfg.barMaxLen, cfg.barMaxThickness);
        placeAgglomerates(cfg.agglomerateCount, cfg.agglomerateMaxSize);
        placeBlobs(cfg.blobCount, cfg.blobMaxSize);
        placeDiagonals(cfg.diagonalCount, cfg.diagonalMaxLen);
        placeSingles(cfg.singleCount);
    }

    // Esempio: esporta come matrice di 0/1 string
    public String[] exportLines(){
        String[] lines = new String[grid.rows];
        for(int i=0;i<grid.rows;i++){
            StringBuilder sb=new StringBuilder();
            for(int j=0;j<grid.cols;j++) sb.append(grid.g[i][j]==0?'.':'#');
            lines[i]=sb.toString();
        }
        return lines;
    }

    // MAIN: esempio di uso e possibilità di cambiare parametri
    public static void main(String[] args){
        Config cfg = new Config();
        if(args.length>=2){ cfg.rows = Integer.parseInt(args[0]); cfg.cols = Integer.parseInt(args[1]); }
        if(args.length>=3){ cfg.seed = Long.parseLong(args[2]); }
        // Qui si possono personalizzare altri parametri prima di creare il generatore
        // Esempio rapido: adattare numero ostacoli alla dimensione della griglia
        double area = cfg.rows * cfg.cols;
        cfg.singleCount = Math.max(10, (int)(area * 0.03));
        cfg.agglomerateCount = Math.max(3, (int)(area * 0.005));
        cfg.diagonalCount = Math.max(2, (int)(area * 0.002));
        cfg.barCount = Math.max(3, (int)(area * 0.004));
        cfg.ringCount = Math.max(1, (int)(area * 0.0008));
        cfg.blobCount = Math.max(2, (int)(area * 0.0025));

        GeneratoreGriglieObstacoli gen = new GeneratoreGriglieObstacoli(cfg);
        gen.generateAll();
        gen.grid.print();

        // Esempio salvataggio su file
        try{
            java.nio.file.Path out = java.nio.file.Paths.get("grid_output.txt");
            java.nio.file.Files.write(out, java.util.Arrays.asList(gen.exportLines()));
            System.out.println("Grid salvata in: " + out.toAbsolutePath());
        } catch(Exception ex){ System.err.println("Impossibile salvare file: " + ex.getMessage()); }
    }
}