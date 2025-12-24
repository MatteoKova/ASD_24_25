package it.unibs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class AnalizCam {

    //private Griglia griglia;

	private boolean[][] mat_ostacoli; //matrice booleana che indica quali celle sono ostacoli (true) e quali libere (false)
	private Stack<Cella> stack_ostacoli; //stack che memorizza gli ostacoli aggiunti dinamicamente, per poterli rimuovere quando si risale di un livello nella ricorsione
	private Stack<int[][]> stack_griglia_tipi; //stack che memorizza  contesto complemento dei tipi di cammino, per poterle riprendere quando si risale di un livello nella ricorsione

    private Map<Integer, RisultatoCammino> celleVisitate ; // mappa per tenere traccia delle celle visitate e del loro conteggio
                                                            // come chiave ha l'hash delle coordinate della cella origine da cui è stato calcolato il cammino
                                                            
    private List<Landmark> salvataggioIntermedio; //tiene in memoria il salvattaggio migliore trovato in modo che anche un interruzzione improvvisa del algo
                                                    // permette di acceder a un percorso completo

    private List<Landmark> currentFrontiera;                                                
	int nr, nc;
    int istr16Contatore = 0;  //contatore esiti falsi di istruzione 16 (if < lunghezzaMin) 
    int contatoreFrontiera = 0; //contatore celle di frontiera analizzate 
   double lunghezzaMin = Double.POSITIVE_INFINITY;          



	public AnalizCam(Griglia griglia) {
        //inizializza l'analizzatore con una griglia dummy, che verrà sostituita in seguito
        //this.griglia = griglia;
		this.mat_ostacoli = griglia.getOstacoli();
		this.stack_ostacoli = new Stack<>();
		this.stack_griglia_tipi = new Stack<>();
		this.nr = mat_ostacoli.length;
		this.nc = mat_ostacoli[0].length;
        this.celleVisitate = null;
        this.salvataggioIntermedio=new ArrayList<>();
	}

    // Calcolo della distanza libera dlib(O, D)
    public static double dlib(Cella o, Cella d) {
		int dx = Math.abs(d.getColonna() - o.getColonna());
        int dy = Math.abs(d.getRiga() - o.getRiga());
        int deltaMin = Math.min(dx, dy);
        int deltaMax = Math.max(dx, dy);
        double risultato = 1.41 * deltaMin + (deltaMax - deltaMin);
        risultato = Math.round(risultato * 100.0) / 100.0;
        return risultato;
    }
    

    
    // Permette di compiere un cammino libero di tipo 1 o 2
    public boolean camminoLibero(Cella[][] mat_tipi, Cella origine, Cella destinazione, int tipo) {
        
        int righeGriglia=mat_tipi.length;
        int colonneGriglia=mat_tipi[0].length;
        int dx = destinazione.getColonna() - origine.getColonna();
        int dy = destinazione.getRiga() - origine.getRiga();

        if (dx == 0 && dy == 0) return true;

        // sorgente o destinazione bloccate?
        int ro = origine.getRiga();
        int co = origine.getColonna();
        int rd = destinazione.getRiga();
        int cd = destinazione.getColonna();
        
        if (ro > righeGriglia || co > colonneGriglia ||
        		rd > righeGriglia || cd > colonneGriglia ||
        			mat_tipi[ro][co].getTipo()==TipoCella.OSTACOLO || mat_tipi[rd][cd].getTipo() == TipoCella.OSTACOLO)
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
            if (!percorri(mat_tipi, origine, stepX, stepY, diagSteps)) return false;

            //Calcolo il punto su cui mi trovo dopo la diagonale
            mid = new Cella(
                ro + stepY * diagSteps,
                co + stepX * diagSteps,
            );

            // poi verifico percorribilità in orizz/vert
            if (absDx > absDy)
                return percorri(mat_tipi, mid, stepX, 0, remSteps);
            else
                return percorri(mat_tipi, mid, 0, stepY, remSteps);

        } else { // tipo 2

            // orizz/vert
            if (absDx > absDy) {
                if (!percorri(mat_tipi, origine, stepX, 0, remSteps)) return false;
                mid = new Cella(ro, co + stepX * remSteps);
            } else {
                if (!percorri(mat_tipi, origine, 0, stepY, remSteps)) return false;
                mid = new Cella(ro + stepY * remSteps, co);
            }

            // poi diagonale
            return percorri(mat_tipi, mid, stepX, stepY, diagSteps);
        }
    }

     // Percorre la griglia lungo una direzione
    private boolean percorri(Cella[][] mat_tipi, Cella start, int stepX, int stepY, int steps) {
        int r = start.getRiga();
        int c = start.getColonna();

        for (int i = 0; i < steps; i++) { //percorre ma non controlla che per ogni cella attraversata, non passi sopra ad ostacoli
            r += stepY;
            c += stepX;

            // Se fuori griglia → bloccato
            if (!griglia.isNeiMargini(r, c)) return false;

            // Se non libera nella griglia → bloccato
            // SESTA COSA IMPORTANTE: se è negli ostacoli dinamici -> bloccato
            if (mat_tipi[r][c].getTipo() == TipoCella.OSTACOLO) return false;
        }
        return true;
    }


    // Calcola il contesto di O (punti raggiungibili con cammini di tipo 1 diag->ort)
    // assegna 1 alle celle del contesto nella matrice dei tipi
    public void calcolaContesto(Cella[][] mat_tipi, Cella origine) {
    	Cella cella = new Cella(0, 0, TipoCella.LIBERA);//inizializzo a un valore di default
    	for (int r = 0; r < mat_tipi.length; r++) {
    		for (int c = 0; c < mat_tipi[0].length; c++) {
                
    			

	            // Se la cella è bloccata o appartiene agli ostacoli dinamici -> salta
	            if (mat_tipi[r][c].getTipo()==TipoCella.OSTACOLO) {
	                continue;
	            }
	
	            // Se esiste un cammino libero di tipo 1, allora la cella appartiene al contesto
	            if (camminoLibero(mat_tipi, origine, cella, 1)) {
	                mat_tipi[r][c].setTipo(TipoCella.CONTESTO); 
	            }
	        }
	    }
    }


    /**
     * Calcola il complemento di O (punti raggiungibili con cammini di tipo 2 ort->diag)
       assegna 2 alle celle del complemento nella matrice dei tipi
        ATTENZIONE: per funzionare presume che il contesto sia gia calcolato sulla griglia passata
         */ 
    
    public void calcolaComplemento(Cella[][] mat_tipi, Cella origine) {
       Cella cella = new Cella(0, 0, TipoCella.LIBERA);//inizializzo a un valore di default
    	for (int r = 0; r < mat_tipi.length; r++) {
    		for (int c = 0; c < mat_tipi[0].length; c++) {

                // Se la cella è bloccata o appartiene agli ostacoli dinamici -> salta
	            if (mat_tipi[r][c].getTipo()==TipoCella.OSTACOLO ||
                 mat_tipi[r][c].getTipo()==TipoCella.CONTESTO) 
	                continue;
	            
                cella.setCoordinate(r, c);
                //cella analizzata deve essere di tipo 2, e non deve appartenere al conteesto di origine
                if (camminoLibero(mat_tipi, origine,cella, 2)) {
                 mat_tipi[r][c].setTipo(TipoCella.COMPLEMENTO);  
                }
            }
        }
    }


       public List <Cella> calcolaCCF(Cella[][] mat_tipi, Cella origine) {
         Cella cella = new Cella(0, 0, TipoCella.LIBERA);
         List<Cella> frontiera = new ArrayList<>();
    	for (int r = 0; r < mat_tipi.length; r++) {
    		for (int c = 0; c < mat_tipi[0].length; c++) {

                if (mat_tipi[r][c].getTipo()==TipoCella.OSTACOLO) continue;
                else{ //se la cella su cui sono non è attraversabile(perchè ostacolo o anche chiusura di punto precedente del cammino), salta
                    cella.setRiga(r);
                    cella.setColonna(c);

                    if (camminoLibero(mat_tipi, origine, cella, 1)) {
                        if(controllaVicini(cella, mat_tipi))  //Se la cella appartiene alla chiusura dell'origine
    					 {  Cella cellaFrontiera= new Cella(r, c, TipoCella.COMPLEMENTO);
                            cellaFrontiera.setIsFrontiera(true);
                            frontiera.add(cellaFrontiera); //SISTEMO: non trova punti di frontiera!!!
                            mat_tipi[r][c].setIsFrontiera(true);  
                         }
                         mat_tipi[r][c].setTipo(TipoCella.COMPLEMENTO); 
                         
                         
                        
                    }
                    //cella analizzata deve essere di tipo 2, e non deve appartenere al conteesto di origine
                    else  if (camminoLibero(mat_tipi, origine, cella, 2)) {
                        if(controllaVicini(cella, mat_tipi))  //Se la cella appartiene alla chiusura dell'origine
    					 {  Cella cellaFrontiera= new Cella(r, c, TipoCella.CONTESTO);
                            cellaFrontiera.setIsFrontiera(true);
                            frontiera.add(cellaFrontiera); //SISTEMO: non trova punti di frontiera!!!
                           mat_tipi[r][c].setIsFrontiera(true);  
                         }
                         mat_tipi[r][c].setTipo(TipoCella.CONTESTO); 
                         
                         
                        
                    }
                    //cella libera
                    else {
                      mat_tipi[r][c].setTipo(TipoCella.COMPLEMENTO); 
                    }
                };

                
            }
        }
        return frontiera;
    }

        /* metodo per valutare se una cella è sulla frontiera della chiusura della cella
           ATTENZIONE: presume che Contesto e Complemento siano gia' stai calcolati
            se trova una cella attorno alla cella passata che è libera (no ost no chiusura)
            ritorna vero
        */
       public boolean controllaVicini(Cella cella, Cella[][] griglia_tipi) {
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
                if (griglia_tipi[ny][nx].getTipo() == TipoCella.OSTACOLO && 
                griglia_tipi[ny][nx].getTipo() != TipoCella.COMPLEMENTO && 
                griglia_tipi[ny][nx].getTipo() != TipoCella.CONTESTO) {
                    return true; 
                }
            }
        }
        return false;
    }

     
    public List<Cella> calcolaFrontiera(Cella[][] mat_tipi) {
    	List<Cella> frontiera = new ArrayList<>();
    	Cella cella=new Cella(0,0);

    	for (int r = 0; r < mat_tipi.length; r++) {
    		for (int c = 0; c < mat_tipi[0].length; c++) {

    			if(mat_tipi[r][c].getTipo()==TipoCella.COMPLEMENTO || mat_tipi[r][c].getTipo() ==TipoCella.CONTESTO){
    				cella.setCoordinate(r, c);
    				if(controllaVicini(cella, mat_tipi)) { //Se la cella appartiene alla chiusura dell'origine
    					Cella cellaFrontiera= new Cella(r, c, TipoCella.CONTESTO);
                            cellaFrontiera.setIsFrontiera(true);
                            frontiera.add(cellaFrontiera); //SISTEMO: non trova punti di frontiera!!!
                            mat_tipi[r][c].setIsFrontiera(true);
    				}
    			}
    		}
    	}
    	return frontiera;
    }

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

    public RisultatoCammino camminoMin(Cella origine, Cella destinazione, Cella[][] mat_tipi, Supplier<Boolean> stopCheck) {
        
        if (stopCheck.get()) 
           throw new StopException(new RisultatoCammino(Double.POSITIVE_INFINITY, List.of(new Landmark(origine, 0)), 
                                            origine, destinazione, istr16Contatore, contatoreFrontiera, griglia));


    	Cella[][] griglia_tipi = creaNuovaGrigliaTipi(mat_tipi); //crea una nuova matrice per memorizzare i tipi relativi ad O, copiando la matrice precedente a 0
    	calcolaContesto(griglia_tipi, origine); //è inutile che restituisca qualcosa, visto che sta modificando griglia_tipi
    	calcolaComplemento(griglia_tipi, origine);
    	
        
        if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()].getTipo()==TipoCella.CONTESTO) {
        	//origine.setTipo(0);
        	//destinazione.setTipo(1);
        	return new RisultatoCammino(dlib(origine, destinazione), List.of(new Landmark(origine,0), new Landmark(destinazione,1)));
        }
        else if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()].getTipo()==TipoCella.COMPLEMENTO) {
        	//origine.setTipo(0);
        	//destinazione.setTipo(2);
        	return new RisultatoCammino(dlib(origine, destinazione), List.of(new Landmark(origine,0), new Landmark(destinazione,2)));
        }
        
        //Set<Landmark> frontiera = calolaFrontiera(...,griglia_tipi);
        List<Cella> frontiera = calcolaFrontiera(griglia_tipi); //Sarebbe da passare a list
        contatoreFrontiera += frontiera.size();

        // CASO BASE: se frontiera è vuota => non si può espandere -> impossibile
        if (frontiera.isEmpty()) {
            return new RisultatoCammino(Double.POSITIVE_INFINITY, List.of());
        }
        
        int num_ostacoli_temporanei = aggiornaOstacoli(griglia_tipi);
        double lunghezzaMin = Double.POSITIVE_INFINITY;
        List<Landmark> seqMin = new ArrayList<>();
        double lF, lFD, lTot;
        
        for (Cella cella_frontiera : frontiera) {
            int rc=cella_frontiera.getRiga();
            int cc=cella_frontiera.getColonna();
        	griglia_tipi[rc][cc].setTipo(mat_tipi[rc][cc].getTipo()); //imposta temporaneamente la cella di forntiera che sto considerando, come libera
        	lF = dlib(origine, cella_frontiera);
        	//if (lF >= lunghezzaMin) continue; //proning
        	if (lF + dlib(cella_frontiera, destinazione) >= lunghezzaMin) continue; // pruning
            istr16Contatore++;
        	System.out.println("istruzione 16 eseguita: confronto " +istr16Contatore);
            RisultatoCammino sotto_cammino = camminoMin(cella_frontiera, destinazione, griglia_tipi,stopCheck);
        	
        	griglia_tipi[rc][cc].setTipo(TipoCella.OSTACOLO);
        	//stack_griglia_tipi.pop(); //rimuove la griglia dei tipi del sotto_cammino dallo stack
        	
        	lFD = sotto_cammino.getLunghezza();
        	if(Double.isInfinite(lFD)) continue;
        	
        	lTot = lF + lFD;
        	if (lTot < lunghezzaMin) {
                lunghezzaMin = lTot;
                 seqMin = compatta(List.of(new Landmark(origine, 0), cella_frontiera), sotto_cammino.getSeq());
            }
        } 
        return new RisultatoCammino(lunghezzaMin, seqMin, origine, destinazione, istr16Contatore, contatoreFrontiera, griglia); //Restituisce il percorso con distanza minore tra origine e destinazione
    }


    private Cella[][] creaNuovaGrigliaTipi(Cella[][] oldG) {
    int rows = oldG.length;
    int cols = oldG[0].length;

    Cella[][] newG = new Cella[rows][cols];

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            newG[i][j] = new Cella(i, j,  newG[i][j].getTipo()); // deep copy
        }
    }

    return newG;
}
    /*
     * Funzione per aggiornare la matrice contenente gli ostacoli, aggiungedogli le celle della chiusura dell'origine.
     * Viene memorizzato anche l'elenco delle celle rese ostacoli, in modo da poterle rimuovere quando terminarà un ierazione di camminoMin
     */
    private int aggiornaOstacoli(Cella[][] griglia_tipi) {
    	int num_ostacoli_aggiunti = 0;
    	
    	for(int r = 0; r < griglia_tipi.length; r++) {
    		for(int c = 0; c < griglia_tipi[0].length; c++) {
    			if(griglia_tipi[r][c].getTipo()==TipoCella.COMPLEMENTO || griglia_tipi[r][c].getTipo()==TipoCella.CONTESTO) { //se sono in una cella della chiusura della mia origine
    				griglia_tipi[r][c].setTipo(TipoCella.OSTACOLO); //segna la cella come ostacolo (temporaneo)
    				num_ostacoli_aggiunti++; //serve per aver modo di rimuovere il giusto numero di ostacoli, ogni volta che risalgo un livello
    			}
    		}
    	}
    	return num_ostacoli_aggiunti;
    }

    

    
}