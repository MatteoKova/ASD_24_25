package it.unibs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AnalizCam {

    

	private boolean[][] mat_ostacoli; //matrice booleana che indica quali celle sono ostacoli (true) e quali libere (false)
                                                     
    private RisultatoCammino risultatoIntermedio; //tiene in memoria il salvattaggio migliore trovato in modo che anche un interruzzione improvvisa del algo
                                                    // permette di acceder a un percorso completo
    private List<Cella> migliorCamminoTemp; //tiene in memoria il miglior cammino trovato durante l'esecuzione dell'algoritmo                                                
    private List<Cella> camminoCorrente;   //tiene in memoria il cammino attualmente in costruzione che altrimenti si creerebbe solo al
                                            // termine della ricorsione
    private ConfigurazioneAnalizzatore cfg;

                                 
	int nr, nc;
    int istr16Contatore = 0;  //contatore esiti falsi di istruzione 16 (if < lunghezzaMin) 
    int contatoreFrontiera = 0; //contatore celle di frontiera analizzate 
    int contatorePercorsiSalvati=0; //contatore dei percorsi salvati in memoria
    double lunghezzaMinGlobale = Double.POSITIVE_INFINITY;     
    private static final double EPSILON = 1e-9;
     /// Matrice per memorizzare i cammini calcolati
    private RisultatoCammino[][] celleVisitateMatrice;     
    boolean abilitaDebug = false; //se true abilita stampe di debug


	public AnalizCam(Griglia griglia) {
        //inizializza l'analizzatore con una griglia dummy, che verrà sostituita in seguito
        //this.griglia = griglia;
		this.mat_ostacoli = griglia.getOstacoli();
		this.nr = mat_ostacoli.length;
		this.nc = mat_ostacoli[0].length;
        
        this.cfg = new ConfigurazioneAnalizzatore();
              
              this.camminoCorrente = new ArrayList<>();
              this.migliorCamminoTemp = new ArrayList<>();
        this.risultatoIntermedio=new RisultatoCammino( Double.POSITIVE_INFINITY, migliorCamminoTemp);
       
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
                co + stepX * diagSteps
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
            if (!(r >= 0 && r < mat_tipi.length && c >= 0 && c < mat_tipi[0].length)) return false;

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
                cella=mat_tipi[r][c];
    			

	            // Se la cella è bloccata o appartiene agli ostacoli dinamici -> salta
	            if (cella.getTipo()==TipoCella.OSTACOLO) {
	                continue;
	            }
	
	            // Se esiste un cammino libero di tipo 1, allora la cella appartiene al contesto
	            if (camminoLibero(mat_tipi, origine, cella, 1)) {
	                cella.setTipo(TipoCella.CONTESTO); 
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
                     
                         mat_tipi[r][c].setTipo(TipoCella.CONTESTO); 
                         
                         mat_tipi[r][c].setIsFrontiera(false);
                        
                    }
                    //cella analizzata deve essere di tipo 2, e non deve appartenere al conteesto di origine
                    else  if (camminoLibero(mat_tipi, origine, cella, 2)) {
                         mat_tipi[r][c].setTipo(TipoCella.COMPLEMENTO); 
                            mat_tipi[r][c].setIsFrontiera(false);
                         
                         
                        
                    }
                    //cella libera
                    else {
                      mat_tipi[r][c].setTipo(TipoCella.LIBERA); 
                        mat_tipi[r][c].setIsFrontiera(false);
                    }
                };
                
                
            }
        }
        for (int r = 0; r < mat_tipi.length; r++) {
    		for (int c = 0; c < mat_tipi[0].length; c++) {
             cella =mat_tipi[r][c];
           if((cella.getTipo()==TipoCella.COMPLEMENTO ||  cella.getTipo()==TipoCella.CONTESTO ) && controllaVicini(cella, mat_tipi))  //Se la cella appartiene alla chiusura dell'origine
    					 {  Cella cellaFrontiera= new Cella(r, c,cella.getTipo());
                            cellaFrontiera.setIsFrontiera(true);
                            frontiera.add(cellaFrontiera); //SISTEMO: non trova punti di frontiera!!!
                            mat_tipi[r][c].setIsFrontiera(true);  
                         }

}}
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
        
        int righe_tot = griglia_tipi.length;
        int col_tot =  griglia_tipi[0].length;
        
        for (int fx : dx) {
            for (int fy : dy) {

                if (fx == 0 && fy == 0) continue;

                int nx = c + fx;
                int ny = r + fy;
                
                if (nx < 0 || ny < 0) continue;

                if (nx >= col_tot || ny >= righe_tot) continue;

                //se la cella limitrofa, non è un ostacolo, e non appartiene alla chiusura dell'origine, 
                if (griglia_tipi[ny][nx].getTipo() == TipoCella.LIBERA ) {
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
                cella=mat_tipi[r][c];
    			if(cella.getTipo()==TipoCella.COMPLEMENTO || cella.getTipo() ==TipoCella.CONTESTO){
    				if(controllaVicini(cella, mat_tipi)) { //Se la cella appartiene alla chiusura dell'origine
    					Cella cellaFrontiera= new Cella(r, c, cella.getTipo());
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


    public RisultatoCammino camminoMinOttimizzato(Cella origine, Cella destinazione, Cella[][] mat_tipi, double lunghezzaCorrente,  Supplier<Boolean> stopCheck) {
       //terminazione anticipata
        if (stopCheck.get()) 
           throw new StopException(risultatoIntermedio);
    
        


         if (cfg.usaMemoriaSequenze==1){
             RisultatoCammino r=checkMemoria(origine);
                if (r!=null){
                    camminoCorrente.addAll(r.getSeq());
                    salvaRisutltatoIntermedio();
                    return r;
                }
            }
        
         
        camminoCorrente.add(origine);

        
    
    	Cella[][] griglia_tipi = creaNuovaGrigliaTipi(mat_tipi); //crea una nuova matrice per memorizzare i tipi relativi ad O, copiando la matrice precedente a 0
    	

        List<Cella> frontiera = calcolaCCF(griglia_tipi,origine); //Sarebbe da passare a list
            

            
    	  if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()].getTipo()==TipoCella.CONTESTO) {
            camminoCorrente.add(destinazione);
            salvaRisutltatoIntermedio();
            if (cfg.getUsaLunghezzaGlob()==1)aggiornaLunghezzaGlob(lunghezzaCorrente+dlib(origine, destinazione));
        	return new RisultatoCammino(dlib(origine, destinazione), List.of(new Cella(origine.getRiga(), origine.getColonna(), origine.getTipo()), new Cella(destinazione.getRiga(), destinazione.getColonna(), TipoCella.CONTESTO)));
        }
        else if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()].getTipo()==TipoCella.COMPLEMENTO) {
            camminoCorrente.add(destinazione);
            salvaRisutltatoIntermedio();
             if (cfg.getUsaLunghezzaGlob()==1)aggiornaLunghezzaGlob(lunghezzaCorrente+dlib(origine, destinazione));
        	return new RisultatoCammino(dlib(origine, destinazione),  List.of(new Cella(origine.getRiga(), origine.getColonna(), origine.getTipo()), new Cella(destinazione.getRiga(), destinazione.getColonna(), TipoCella.COMPLEMENTO)));
        }
        
        ;
        
        contatoreFrontiera += frontiera.size();

        // CASO BASE: se frontiera è vuota => non si può espandere -> impossibile
        if (frontiera.isEmpty()) {
            return new RisultatoCammino(Double.POSITIVE_INFINITY, List.of());
        }
        
        int num_ostacoli_temporanei = aggiornaOstacoli(griglia_tipi);
       double lunghezzaMin = Double.POSITIVE_INFINITY;
        List<Cella> seqMin = new ArrayList<>();
        double lF, lFD, lTot;

        if (cfg.isRiordinaFrontiera()) {
            
        riordinaFrontiera(frontiera, destinazione);
          
        	
        }
       
        for (Cella cella_frontiera : frontiera) {
            int rc=cella_frontiera.getRiga();
            int cc=cella_frontiera.getColonna();
        	griglia_tipi[rc][cc].setTipo(mat_tipi[rc][cc].getTipo()); //imposta temporaneamente la cella di forntiera che sto considerando, come libera
        	
            lF = dlib(origine, cella_frontiera);

        	//if (lF >= lunghezzaMin) continue; //proning
        	if ( (cfg.getUsaMemoriaSequenze()==1 && lunghezzaCorrente+lF + dlib(cella_frontiera, destinazione) >= lunghezzaMinGlobale-EPSILON) ||
           (cfg.getUsaMemoriaSequenze()==0 && lF + dlib(cella_frontiera, destinazione) >= lunghezzaMin-EPSILON) ) continue; // pruning
            
            istr16Contatore++;
        	
            RisultatoCammino sotto_cammino = camminoMinOttimizzato(cella_frontiera, destinazione, griglia_tipi,
                 lunghezzaCorrente+lF, stopCheck);
        	
        	griglia_tipi[rc][cc].setTipo(TipoCella.OSTACOLO);
        	//stack_griglia_tipi.pop(); //rimuove la griglia dei tipi del sotto_cammino dallo stack
        	
        	lFD = sotto_cammino.getLunghezza();

            


        	
        	lTot = lF + lFD;
        	if (lTot < lunghezzaMin && !Double.isInfinite(lTot)) {
                lunghezzaMin = lTot;
                seqMin = compatta(List.of(new Cella(origine.getRiga(), origine.getColonna(), origine.getTipo()), cella_frontiera), sotto_cammino.getSeq());
             
            }

             //memorizzo il cammino calcolato  
            if (cfg.usaMemoriaSequenze==1 && checkMemoria(cella_frontiera)==null){
                aggiungiAMemoria(cella_frontiera, sotto_cammino);
            }

            
            //aggiorna cammino corrente

            
            if (!camminoCorrente.isEmpty())  {
            while(!(cella_frontiera.getRiga() == camminoCorrente.getLast().getRiga() 
  && cella_frontiera.getColonna() == camminoCorrente.getLast().getColonna())
        ) camminoCorrente.removeLast();
     camminoCorrente.removeLast();   }
  
       
    
    } 
     
        return new RisultatoCammino(lunghezzaMin, seqMin); //Restituisce il percorso con distanza minore tra origine e destinazione
    }

//================================================================================================
//================================================================================================
//========================================================================================================================================================
//======
public RisultatoCammino camminoMin(Cella origine, Cella destinazione, Cella[][] mat_tipi,  Supplier<Boolean> stopCheck) {
       
        if (stopCheck.get()) 
           throw new StopException(risultatoIntermedio);


    	Cella[][] griglia_tipi = creaNuovaGrigliaTipi(mat_tipi); //crea una nuova matrice per memorizzare i tipi relativi ad O, copiando la matrice precedente a 0
    	calcolaContesto(griglia_tipi, origine); //è inutile che restituisca qualcosa, visto che sta modificando griglia_tipi
    	calcolaComplemento(griglia_tipi, origine);
    	 
       
        
        if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()].getTipo()==TipoCella.CONTESTO) {
        	//origine.setTipo(0);
        	//destinazione.setTipo(1);
        	return new RisultatoCammino(dlib(origine, destinazione), List.of(new Cella(origine.getRiga(), origine.getColonna(), origine.getTipo()), new Cella(destinazione.getRiga(), destinazione.getColonna(), TipoCella.CONTESTO)));
        }
        else if(griglia_tipi[destinazione.getRiga()][destinazione.getColonna()].getTipo()==TipoCella.COMPLEMENTO) {
        	//origine.setTipo(0);
        	//destinazione.setTipo(2);
        	return new RisultatoCammino(dlib(origine, destinazione),  List.of(new Cella(origine.getRiga(), origine.getColonna(), origine.getTipo()), new Cella(destinazione.getRiga(), destinazione.getColonna(), TipoCella.COMPLEMENTO)));
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
        List<Cella> seqMin = new ArrayList<>();
        double lF, lFD, lTot;
        
        for (Cella cella_frontiera : frontiera) {
            int rc=cella_frontiera.getRiga();
            int cc=cella_frontiera.getColonna();
        	griglia_tipi[rc][cc].setTipo(mat_tipi[rc][cc].getTipo()); //imposta temporaneamente la cella di forntiera che sto considerando, come libera
        	lF = dlib(origine, cella_frontiera);
        	//if (lF >= lunghezzaMin) continue; //proning
        	if (lF + dlib(cella_frontiera, destinazione) >= lunghezzaMin) continue; // pruning
           
            istr16Contatore++;
        	//System.out.println("istruzione 16 eseguita: confronto " +istr16Contatore);
            RisultatoCammino sotto_cammino = camminoMin(cella_frontiera, destinazione, griglia_tipi,stopCheck);
        	
        	griglia_tipi[rc][cc].setTipo(TipoCella.OSTACOLO);
        	//stack_griglia_tipi.pop(); //rimuove la griglia dei tipi del sotto_cammino dallo stack
        	
        	lFD = sotto_cammino.getLunghezza();
        	if(Double.isInfinite(lFD)) continue;
        	
        	lTot = lF + lFD;
        	if (lTot < lunghezzaMin) {
                lunghezzaMin = lTot;
                seqMin = compatta(List.of(new Cella(origine.getRiga(), origine.getColonna(), origine.getTipo()), cella_frontiera), sotto_cammino.getSeq());
                risultatoIntermedio.setLunghezza(lTot); //aggiorna il salvataggio intermedio
                risultatoIntermedio.setSeq(seqMin);
            }
        } 
        return new RisultatoCammino(lunghezzaMin, seqMin); //Restituisce il percorso con distanza minore tra origine e destinazione
    }



    private Cella[][] creaNuovaGrigliaTipi(Cella[][] oldG) {
    int rows = oldG.length;
    int cols = oldG[0].length;

    Cella[][] newG = new Cella[rows][cols];

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            newG[i][j] = new Cella(i, j,  oldG[i][j].getTipo()); // deep copy
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


     // Unisce 2 liste di landmark, rimuovendo il primo elemento della seconda
    public static List<Cella> compatta (List<Cella> seq1, List<Cella> seq2) {
    	List<Cella> result = new ArrayList<>(seq1);
        // aggiungi tutti gli elementi di seq2 tranne il primo
        result.addAll(seq2.subList(1, seq2.size()));
        return result;
    }

//==metodi per ottimizzare la ricerca del cammino minimo==//

 // Riordina la frontiera in base alla distanza dlib dalla destinazione
    // possbibile miglioramento: usare una struct dati più efficiente(set non puo essere ordinato)
    //possibile ottimizzazione: usare una priority piu complessa 
    public List<Cella> riordinaFrontiera(List<Cella> frontiera, Cella destinazione) {
 frontiera.sort(
    Comparator.comparingDouble(l -> dlib(l, destinazione))
);
return frontiera;
}

    



// Inizializza la matrice
public void inizializzaMemoriaSequenze(int nr, int nc) {
    this.nr = nr;
    this.nc = nc;
    celleVisitateMatrice = new RisultatoCammino[nr][nc];
}

// Aggiunge un cammino alla matrice della memoria
public void aggiungiAMemoria(Cella cella, RisultatoCammino risultatoCammino) {
    List<Cella> nuovaSeq = new ArrayList<>();
    if (risultatoCammino.getSeq().isEmpty()) {
        nuovaSeq.add(cella);
    } else {
        nuovaSeq.addAll(risultatoCammino.getSeq());
    }

    RisultatoCammino newR = new RisultatoCammino(
        risultatoCammino.getLunghezza(),
        nuovaSeq
    );

    int r = cella.getRiga();     // metodo che ritorna la riga della cella
    int c = cella.getColonna();  // metodo che ritorna la colonna della cella
    contatorePercorsiSalvati++;
    celleVisitateMatrice[r][c] = newR;
     
      
}

// Controlla se esiste un cammino già memorizzato
public RisultatoCammino checkMemoria(Cella cella) {
     
    int r = cella.getRiga();
    int c = cella.getColonna();
    if (celleVisitateMatrice != null) {
        return celleVisitateMatrice[r][c];
    }
    return null;
}


//utilizzare lunghezza globale dell'albero 
public void aggiornaLunghezzaGlob(double lunghezzaCorrente)
{
    if (lunghezzaCorrente<lunghezzaMinGlobale- EPSILON)
        lunghezzaMinGlobale=lunghezzaCorrente;
}
//=======================================

//===metodi per salvataggio intermedio===//
   private void salvaRisutltatoIntermedio() {
     int somma=0;
     for (int i =0; i<camminoCorrente.size()-1; i++) {
       somma+= dlib(camminoCorrente.get(i), camminoCorrente.get(i+1));
       } 
       if (somma<risultatoIntermedio.getLunghezza()) {
         risultatoIntermedio.setLunghezza(somma);
         copyList(camminoCorrente,risultatoIntermedio.getSeq());
       }
       

       
    }
    public static <T> void copyList(List<T> source, List<T> target) {
    target.clear();
    target.addAll(source);
}
//=====================================// 



public int getContatoreFrontiera() {
        return contatoreFrontiera;
    }
    public int getIstr16Contatore() {
        return istr16Contatore;
    }

    public void setConfig(ConfigurazioneAnalizzatore cfg) {
        this.cfg = cfg;
    }

    public void resetContatori(int nr, int nc) {
        this.istr16Contatore = 0;
        this.contatoreFrontiera = 0;
        this.contatorePercorsiSalvati=0;
        this.lunghezzaMinGlobale = Double.POSITIVE_INFINITY;
       risultatoIntermedio.setLunghezza(Double.POSITIVE_INFINITY);
         risultatoIntermedio.getSeq().clear();
        camminoCorrente.clear();
        this.nr = nr;
        this.nc = nc;
        this.abilitaDebug=false;
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

   public int getContatorePercorsiSalvati() {
        return contatorePercorsiSalvati;
    }

 public static void printGridDebug(Cella[][] grid) {
    int rows = grid.length;
    int cols = grid[0].length;

    // Asse X (colonne)
    System.out.print("    "); // spazio per asse Y
    for (int j = 0; j < cols; j++) {
        System.out.printf("%3d", j);
    }
    System.out.println();

    // Separatore
    System.out.print("    ");
    for (int j = 0; j < cols; j++) {
        System.out.print("---");
    }
    System.out.println();

    // Griglia con asse Y
    for (int i = 0; i < rows; i++) {
        System.out.printf("%3d|", i); // asse Y

        for (int j = 0; j < cols; j++) {
            Cella c = grid[i][j];
            char ch;

            if (c.isLandMark()) ch = 'L';
            else if (c.getTipo() == TipoCella.OSTACOLO) ch = 'X';
            else if (c.isFrontiera()) ch = 'F';
            else if (c.getTipo() == TipoCella.LIBERA) ch = '*';
             else if (c.getTipo() == TipoCella.COMPLEMENTO) ch = 'M';
            else if (c.getTipo() == TipoCella.CONTESTO) ch = 'C';
           
            else ch = '?';

            System.out.printf("%3c", ch);
        }
        System.out.println();
    }
}



}