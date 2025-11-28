package it.unibs;

/* PSEUDOCODICE

Input: griglia G (con celle attraversabili/non), cella O=(rO, cO)
Output: Contesto(O), Complemento(O)

1. Inizializza insieme CONT = {}, COMP = {}
2. Per ogni cella D attraversabile in G:
      - Se esiste un cammino libero tipo1(O,D): aggiungi D a CONT
      - Altrimenti se esiste cammino libero tipo2(O,D): aggiungi D a COMP

3. Restituisci CONT e COMP

-------------------------------------------------------------------------------

FUNZIONE cammino_libero(O, D, tipo)

Input: O, D, tipo ∈ {1,2}, G
Output: true/false

1. Determina quadrante q di D rispetto a O
2. Calcola dx = |cD - cO|, dy = |rD - rO|
3. Se dx==0 e dy==0 → ritorna true (stessa cella)

4. Determina le direzioni ammesse in q:
    quadrante I: obliquo = NE, orizz/vert = E o N
    quadrante II: obliquo = NW, orizz/vert = W o N
    quadrante III: obliquo = SW, orizz/vert = W o S
    quadrante IV: obliquo = SE, orizz/vert = E o S

5. Se tipo==1:
       - prova cammino (a)(b): prima diagonale, poi orizz/vert
   Se tipo==2:
       - prova cammino (b)(a): prima orizz/vert, poi diagonale

6. Ogni mossa deve attraversare solo celle attraversabili

   Implementazione concreta:
   - Per (a)(b):
        passi_diagonali = min(dx, dy)
        percorri passi_diagonali in direzione diagonale
        se ostacolo → return false
        poi percorri (dx-dy) passi orizzontali o (dy-dx) verticali
        se ostacolo → return false
        se arrivi a D → return true
   - Viceversa per tipo 2

7. return false se qualsiasi cella è bloccata

-------------------------------------------------------------------------------

FUNZIONE dlib(O, D)

dx = |cD - cO|
dy = |rD - rO|
delta_min = min(dx, dy)
delta_max = max(dx, dy)
dlib = sqrt(2)*delta_min + (delta_max - delta_min)
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AnalizzatoreCammini {
	public AtomicLong calls = new AtomicLong(0);

	private boolean[][] mat_ostacoli;
	private Stack<Cella> stack_ostacoli;
	private Stack<int[][]> stack_griglia_tipi;
	int nr, nc;
    int istr16Contatore = 0;  //contatore esiti falsi di istruzione 16 (if < lunghezzaMin) 
    int contatoreFrontiera = 0; //contatore celle di frontiera analizzate

	public AnalizzatoreCammini(boolean[][] mat_ostacoli, Stack<Cella> stack_ostacoli, Stack<int[][]> stack_griglia_tipi) {
		this.mat_ostacoli = mat_ostacoli;
		this.stack_ostacoli = stack_ostacoli;
		this.stack_griglia_tipi = stack_griglia_tipi;
		this.nr = mat_ostacoli.length;
		this.nc = mat_ostacoli[0].length;
	}

	// Calcolo della distanza libera dlib(O, D)
    public static double dlib(Cella o, Cella d) {
		int dx = Math.abs(d.getColonna() - o.getColonna());
        int dy = Math.abs(d.getRiga() - o.getRiga());
        int deltaMin = Math.min(dx, dy);
        int deltaMax = Math.max(dx, dy);
        return Math.sqrt(2) * deltaMin + (deltaMax - deltaMin);
    }

    // Permette di compiere un cammino libero di tipo 1 o 2
    public boolean camminoLibero(Griglia griglia, Cella origine, Cella destinazione, int tipo) {

        int dx = destinazione.getColonna() - origine.getColonna();
        int dy = destinazione.getRiga() - origine.getRiga();

        if (dx == 0 && dy == 0) return true;

        // sorgente o destinazione bloccate?
        int ro = origine.getRiga();
        int co = origine.getColonna();
        int rd = destinazione.getRiga();
        int cd = destinazione.getColonna();
        
        if (ro > griglia.getRighe() || co > griglia.getColonne() ||
        		rd > griglia.getRighe() || cd > griglia.getColonne() ||
        			mat_ostacoli[ro][co] == true || mat_ostacoli[rd][cd] == true)
        	return false;
       
        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        int diagSteps = Math.min(absDx, absDy);
        int remSteps = Math.abs(absDx - absDy);
        
        Cella mid;

        if (tipo == 1) {
            // verifico di poter percorrere la diagonale
            if (!percorri(griglia, origine, stepX, stepY, diagSteps)) return false;

            //Calcolo il punto su cui mi trovo dopo la diagonale
            mid = new Cella(
                ro + stepY * diagSteps,
                co + stepX * diagSteps
            );

            // poi verifico percorribilità in orizz/vert
            if (absDx > absDy)
                return percorri(griglia, mid, stepX, 0, remSteps);
            else
                return percorri(griglia, mid, 0, stepY, remSteps);

        } else { // tipo 2

            // orizz/vert
            if (absDx > absDy) {
                if (!percorri(griglia, origine, stepX, 0, remSteps)) return false;
                mid = new Cella(ro, co + stepX * remSteps);
            } else {
                if (!percorri(griglia, origine, 0, stepY, remSteps)) return false;
                mid = new Cella(ro + stepY * remSteps, co);
            }

            // poi diagonale
            return percorri(griglia, mid, stepX, stepY, diagSteps);
        }
    }

    // Percorre la griglia lungo una direzione
    private boolean percorri(Griglia griglia, Cella start, int stepX, int stepY, int steps) {
        int r = start.getRiga();
        int c = start.getColonna();

        for (int i = 0; i < steps; i++) { //percorre ma non controlla che per ogni cella attraversata, non passi sopra ad ostacoli
            r += stepY;
            c += stepX;

            // Se fuori griglia → bloccato
            if (!griglia.isNeiMargini(r, c)) return false;

            // Se non libera nella griglia → bloccato
            // SESTA COSA IMPORTANTE: se è negli ostacoli dinamici -> bloccato
            if (mat_ostacoli[r][c] == true) return false;
        }
        return true;
    }

    public int[][] calcolaContesto(Griglia griglia, Cella origine, int[][] griglia_tipi) {
    	
    	for (int r = 0; r < griglia.getRighe(); r++) {
    		for (int c = 0; c < griglia.getColonne(); c++) {

    			Cella cella = new Cella(r, c);

	            // Se la cella è bloccata o appartiene agli ostacoli dinamici -> salta
	            if (mat_ostacoli[r][c] == true) {
	                continue;
	            }
	
	            // Se esiste un cammino libero di tipo 1, allora la cella appartiene al contesto
	            if (camminoLibero(griglia, origine, cella, 1)) {
	                griglia_tipi[r][c]=1; 
	            }
	        }
	    }
    	return griglia_tipi;
    }
    
    public int[][] calcolaComplemento(Griglia griglia, Cella origine, int[][] griglia_tipi) {
    	for (int r = 0; r < griglia.getRighe(); r++) {
            for (int c = 0; c < griglia.getColonne(); c++) {

                if (mat_ostacoli[r][c]) continue; //se la cella su cui sono non è attraversabile(perchè ostacolo o anche chiusura di punto precedente del cammino), salta

                //cella analizzata deve essere di tipo 2, e non deve appartenere al conteesto di origine
                if (camminoLibero(griglia, origine, new Cella(r,c), 2) && griglia_tipi[r][c]==0) {
                    griglia_tipi[r][c]=2;
                }
            }
        }
    	return griglia_tipi;
    }

    // Calcola il complemento di O (punti raggiungibili SOLO con cammini di tipo 2)
    /*public static Set<Cella> calcolaComplemento(Griglia g, Cella o, Set<Cella> ostacoli) {
        Set<Cella> complemento = new HashSet<>();

        for (int r = 0; r < g.getRighe(); r++) {
            for (int c = 0; c < g.getColonne(); c++) {

                Cella cella = g.getCella(r, c);

                if (!cella.isAttraversabile() || ostacoli.contains(cella)) continue;

                if (camminoLibero(g, o, cella, 2, ostacoli) && !camminoLibero(g, o, cella, 1, ostacoli)) {
                    complemento.add(cella);
                }
            }
        }

        return complemento;
    }*/
    
    public boolean controllaVicini(Cella cella, Griglia griglia, int[][] griglia_tipi) {
    	int[] dx = {-1, 0, 1};
        int[] dy = {-1, 0, 1};
        
        int r = cella.getRiga();
        int c = cella.getColonna();
        
        int righe_tot = griglia.getRighe();
        int col_tot = griglia.getColonne();
        
        for (int fx : dx) {
            for (int fy : dy) {

                if (fx == 0 && fy == 0) continue;

                int nx = c + fx;
                int ny = r + fy;
                
                if (nx < 0 || ny < 0) continue;

                if (nx >= col_tot || ny >= righe_tot) continue;

                //se la cella limitrofa, non è un ostacolo, e non appartiene alla chiusura dell'origine, 
                if (mat_ostacoli[ny][nx] == false && griglia_tipi[ny][nx] == 0) {
                    return true; 
                }
            }
        }
        return false;
    }
    
    
    // Controlla che il punto appartenga alla frontiera
  /*  public static boolean controllaVicini(Cella cella, Griglia griglia, Set<Cella> chiusura) {
        int[] dx = {-1, 0, 1};
        int[] dy = {-1, 0, 1};

        for (int fx : dx) {
            for (int fy : dy) {

                if (fx == 0 && fy == 0) continue;

                int nx = cella.getColonna() + fx;
                int ny = cella.getRiga() + fy;

                if (!griglia.isNeiMargini(nx, ny)) continue;

                if (griglia.isLibera(ny, nx) && !chiusura.contains(griglia.getCella(ny, nx))) {
                    return true;
                }
            }
        }
        return false;
    }*/

    //TODO?: potrei unire calcolaFrontiera/Contesto/Complemento, in modo da ridurre le operazioni totali
    public Set<Landmark> calcolaFrontiera(int[][] griglia_tipi, Griglia griglia) {
    	Set<Landmark> frontiera = new HashSet<Landmark>();
    	Cella cella;

    	for(int r = 0; r < griglia.getRighe(); r++) {
    		for(int c = 0; c < griglia.getColonne(); c++) {
    			if(griglia_tipi[r][c] == 1 || griglia_tipi[r][c] == 2){
    				cella = new Cella(r,c);
    				if(controllaVicini(cella, griglia, griglia_tipi)) { //Se la cella appartiene alla chiusura dell'origine
    					frontiera.add(new Landmark(cella,griglia_tipi[r][c])); //SISTEMO: non trova punti di frontiera!!!
    				}
    			}
    		}
    	}
    	return frontiera;
    }
    
    // Calcola la frontiera
    /*public Set<Cella> calcolaFrontiera(Set<Cella> contesto, Set<Cella> complemento, Griglia griglia) {
    	Set<Cella> frontiera = new HashSet<Cella>();
    	
    	// Calcola la chiusura
    	Set<Cella> chiusura = new HashSet<>(contesto);
    	chiusura.addAll(complemento);

    	for (Cella cella : chiusura) {
    		if (controllaVicini(cella, griglia, chiusura)) {
    			frontiera.add(cella);
    		}
    	}
    	return frontiera;
    }*/
    
    // Restituisce le celle che compongono gli ostacoli della griglia
/*    public static Set<Cella> trovaOstacoli(Griglia griglia) {
    	Set<Cella> ostacoli = new HashSet<Cella>();
    	for (int i=0; i < griglia.getRighe(); i++) {
    		for (int j=0; j < griglia.getColonne(); j++) {
    			Cella cella = new Cella(i, j);
    			if (!cella.isAttraversabile()) {
    				ostacoli.add(cella);
    			}
    		}
    	}
    	return ostacoli;
    }*/
    
    // Unisce 3 set di celle
    public static Set<Cella> unisci(Set<Cella> seq1, Set<Cella> seq2, Set<Cella> seq3) {
        Set<Cella> chiusura = new HashSet<>(seq1);
        chiusura.addAll(seq2);
        chiusura.addAll(seq3);
        return chiusura;
    }
    
    // Unisce 2 liste di landmark, rimuovendo il primo elemento della seconda
    public static List<Landmark> compatta(List<Landmark> seq1, List<Landmark> seq2) {
    	List<Landmark> result = new ArrayList<>(seq1);
        // aggiungi tutti gli elementi di seq2 tranne il primo
        result.addAll(seq2.subList(1, seq2.size()));
        return result;
    }

    public RisultatoCammino camminoMin(Cella origine, Cella destinazione, Griglia griglia, Supplier<Boolean> stopCheck) {
    	calls.incrementAndGet();
        
    	int[][] griglia_tipi = new int[griglia.getRighe()][griglia.getColonne()]; //crea una nuova matrice per memorizzare i tipi relativi ad O, inizializzandola a 0
    	calcolaContesto(griglia, origine, griglia_tipi); //è inutile che restituisca qualcosa, visto che sta modificando griglia_tipi
    	calcolaComplemento(griglia, origine, griglia_tipi);
    	
        if (stopCheck.get()) 
           throw new StopException(new RisultatoCammino(Double.POSITIVE_INFINITY, List.of(new Landmark(origine, 0)), 
                                            istr16Contatore, contatoreFrontiera, griglia));

        if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()]==1) {
        	//origine.setTipo(0);
        	//destinazione.setTipo(1);
        	return new RisultatoCammino(dlib(origine, destinazione), List.of(new Landmark(origine,0), new Landmark(destinazione,1)),
        			istr16Contatore, contatoreFrontiera,griglia);
        }
        else if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()]==2) {
        	//origine.setTipo(0);
        	//destinazione.setTipo(2);
        	return new RisultatoCammino(dlib(origine, destinazione), List.of(new Landmark(origine,0), new Landmark(destinazione,2)),
        			istr16Contatore, contatoreFrontiera,  griglia);
        }
        
        //Set<Landmark> frontiera = calolaFrontiera(...,griglia_tipi);
        Set<Landmark> frontiera = calcolaFrontiera(griglia_tipi, griglia); //Sarebbe da passare a list
        contatoreFrontiera += frontiera.size();

        // CASO BASE: se frontiera è vuota => non si può espandere -> impossibile
        if (frontiera.isEmpty()) {
            return new RisultatoCammino(Double.POSITIVE_INFINITY, List.of(), istr16Contatore, contatoreFrontiera, griglia);
        }
        
        int num_ostacoli_temporanei = aggiornaOstacoli(griglia_tipi);
        //ATTENZIONE: sto duplicando la matrice iniziale
        stack_griglia_tipi.add(griglia_tipi); //memorizza la matrice dei tipi, per poterla riprendere dopo aver trovato il sottocammino cella_frontiera -> destinazione, migliore
        
        double lunghezzaMin = Double.POSITIVE_INFINITY;
        List<Landmark> seqMin = new ArrayList<>();
        double lF, lFD, lTot;
        
        for (Landmark landmark_frontiera : frontiera) {
        	Cella cella_frontiera = landmark_frontiera.getCella();
        	mat_ostacoli[cella_frontiera.getRiga()][cella_frontiera.getColonna()] = false; //imposta temporaneamente la cella di forntiera che sto considerando, come libera
        	lF = dlib(origine, cella_frontiera);
        	//if (lF >= lunghezzaMin) continue; //proning
        	if (lF + dlib(cella_frontiera, destinazione) >= lunghezzaMin) continue; // pruning
            istr16Contatore++;
        	System.out.println("istruzione 16 eseguita: confronto " +istr16Contatore);
            RisultatoCammino sotto_cammino = camminoMin(cella_frontiera, destinazione, griglia,stopCheck);
        	
        	mat_ostacoli[cella_frontiera.getRiga()][cella_frontiera.getColonna()] = true;
        	//stack_griglia_tipi.pop(); //rimuove la griglia dei tipi del sotto_cammino dallo stack
        	
        	lFD = sotto_cammino.getLunghezza();
        	if(Double.isInfinite(lFD)) continue;
        	
        	lTot = lF + lFD;
        	if (lTot < lunghezzaMin) {
                lunghezzaMin = lTot;
                
                seqMin = compatta(List.of(new Landmark(origine, 0), landmark_frontiera), sotto_cammino.getSeq());
            }
        }
        rimuoviOstacoli(num_ostacoli_temporanei); //elimina gli ostacoli relativi a questo livello
        stack_griglia_tipi.pop(); //tolgo la matrice di questo livello 
        return new RisultatoCammino(lunghezzaMin, seqMin, istr16Contatore, contatoreFrontiera, griglia); //Restituisce il percorso con distanza minore tra origine e destinazione
    }
    
    /*
     * Funzione per aggiornare la matrice contenente gli ostacoli, aggiungedogli le celle della chiusura dell'origine.
     * Viene memorizzato anche l'elenco delle celle rese ostacoli, in modo da poterle rimuovere quando terminarà un ierazione di camminoMin
     */
    private int aggiornaOstacoli(int[][]griglia_tipi) {
    	int num_ostacoli_aggiunti = 0;
    	
    	for(int r = 0; r < griglia_tipi.length; r++) {
    		for(int c = 0; c < griglia_tipi[0].length; c++) {
    			if(griglia_tipi[r][c] == 1 || griglia_tipi[r][c] == 2) { //se sono in una cella della chiusura della mia origine
    				mat_ostacoli[r][c] = true; //segna la cella come ostacolo (temporaneo)
    				stack_ostacoli.push(new Cella(r,c)); //e aggiungila allo stack degli ostacoli, in mod oda poterla rimuovere da mat_ostacoli, quando camminoMin tornerà ad un livello superiore
    				num_ostacoli_aggiunti++; //serve per aver modo di rimuovere il giusto numero di ostacoli, ogni volta che risalgo un livello
    			}
    		}
    	}
    	return num_ostacoli_aggiunti;
    }
    
    private void rimuoviOstacoli(int num_ostacoli) {
    	Cella ostacolo;
    	for (int i = 0; i < num_ostacoli; i++) {
    		ostacolo = stack_ostacoli.pop();
    		mat_ostacoli[ostacolo.getRiga()][ostacolo.getColonna()] = false; //toglie l'ostacolo, perchè relativo ad un iterazione di acmminoMin già terminata
    	}
    }

    public int[][] getMatTipi() {
    	return stack_griglia_tipi.peek();
    }

    /*
     * Funzione per rimuovere eventuali matrici di tipi, rimaste a seguito di un interruzione
     * di cammminoMin fatto dall'utente
     */
    public void ripristinaCoerenzaTipi() {
    	stack_griglia_tipi = new Stack<int[][]>();
    }

    /*
     * Funzione per rimuovere tutti gli ostacoli "temporanei", che possono essere rimasti a seguito
     * di un interruzione di camminoMin fatto dall'utente
     */
    public void ripristinaCoerenzaOstacoli() {
    	Cella cella;
    	while(!stack_ostacoli.isEmpty()) {
    		cella = stack_ostacoli.pop();
    		mat_ostacoli[cella.getRiga()][cella.getColonna()] = false;
    	}
    }
    
    /**
     * Ricostruisce il cammino cella-per-cella tra due celle consecutive
     * seguendo il percorso minimo rettilineo/diagonale che dlib assume
     */
    public List<Cella> ricostruisciCammino(
            Cella start, Cella goal,
            boolean[][] matriceOstacoli,
            boolean allowDiagonalCutting) {

        int rows = matriceOstacoli.length;
        int cols = matriceOstacoli[0].length;

        // validazioni rapide
        if (start == null || goal == null) return Collections.emptyList();
        if (start.getRiga() == goal.getRiga() && start.getColonna() == goal.getColonna())
            return List.of(new Cella(start.getRiga(), start.getColonna()));
        if (!inBounds(start.getRiga(), start.getColonna(), rows, cols) ||
            !inBounds(goal.getRiga(), goal.getColonna(), rows, cols)) return Collections.emptyList();
        if (matriceOstacoli[start.getRiga()][start.getColonna()] ||
            matriceOstacoli[goal.getRiga()][goal.getColonna()]) return Collections.emptyList();

        // mosse (dx,dy) e costi
        final int[] dx = { 1, 0, -1, 0, 1, 1, -1, -1 };
        final int[] dy = { 0, 1, 0, -1, 1, -1, -1, 1 };
        final double[] cost = { 1, 1, 1, 1, Math.sqrt(2), Math.sqrt(2), Math.sqrt(2), Math.sqrt(2) };

        class Node {
            int r,c; double g,f; Node parent;
            Node(int r,int c,double g,double f,Node p){this.r=r;this.c=c;this.g=g;this.f=f;this.parent=p;}
        }

        // euristica dlib
        final BiFunction<Integer,Integer,Double> h = (r,c) -> {
            int dxAbs = Math.abs(c - goal.getColonna());
            int dyAbs = Math.abs(r - goal.getRiga());
            int dmin = Math.min(dxAbs, dyAbs), dmax = Math.max(dxAbs, dyAbs);
            return Math.sqrt(2)*dmin + (dmax - dmin);
        };

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n->n.f));
        boolean[][] closed = new boolean[rows][cols];
        double[][] bestG = new double[rows][cols];
        for (int i=0;i<rows;i++) Arrays.fill(bestG[i], Double.POSITIVE_INFINITY);

        Node startNode = new Node(start.getRiga(), start.getColonna(), 0.0, h.apply(start.getRiga(), start.getColonna()), null);
        open.add(startNode);
        bestG[start.getRiga()][start.getColonna()] = 0.0;

        while (!open.isEmpty()) {
            Node cur = open.poll();
            if (closed[cur.r][cur.c]) continue;
            if (cur.r == goal.getRiga() && cur.c == goal.getColonna()) {
                LinkedList<Cella> path = new LinkedList<>();
                for (Node it = cur; it != null; it = it.parent) path.addFirst(new Cella(it.r, it.c));
                return path;
            }
            closed[cur.r][cur.c] = true;

            for (int k=0;k<dx.length;k++) {
                int nr = cur.r + dy[k];
                int nc = cur.c + dx[k];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (matriceOstacoli[nr][nc]) continue;

                // evita corner-cutting se richiesto
                if (!allowDiagonalCutting && k >= 4) {
                    int ortho1r = cur.r, ortho1c = cur.c + dx[k];
                    int ortho2r = cur.r + dy[k], ortho2c = cur.c;
                    if (matriceOstacoli[ortho1r][ortho1c] || matriceOstacoli[ortho2r][ortho2c]) continue;
                }

                double tentativeG = cur.g + cost[k];
                if (tentativeG + 1e-9 < bestG[nr][nc]) {
                    bestG[nr][nc] = tentativeG;
                    double f = tentativeG + h.apply(nr,nc);
                    open.add(new Node(nr,nc,tentativeG,f,cur));
                }
            }
        }

        return Collections.emptyList(); // nessun percorso
    }

    private boolean inBounds(int r, int c, int rows, int cols){
        return r>=0 && r<rows && c>=0 && c<cols;
    }


    public void resetContatori() {
    	this.istr16Contatore = 0;
    	this.contatoreFrontiera = 0;
    }
}