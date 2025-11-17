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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalizzatoreCammini {

    // Calcolo della distanza libera dlib(O, D)
    public static double dlib(Cella o, Cella d, Set<Cella> contesto, Set<Cella> complemento) {
    	//if (contesto.contains(d) || complemento.contains(d)) {
    		int dx = Math.abs(d.getColonna() - o.getColonna());
            int dy = Math.abs(d.getRiga() - o.getRiga());
            int deltaMin = Math.min(dx, dy);
            int deltaMax = Math.max(dx, dy);
            return Math.sqrt(2) * deltaMin + (deltaMax - deltaMin);
    	//}
    	//else return 0;
    }

    // Permette di compiere un cammino libero di tio 1 o 2
    public static boolean camminoLibero(Griglia g, Cella o, Cella d, int tipo, Set<Cella> ostacoli) {

        int dx = d.getColonna() - o.getColonna();
        int dy = d.getRiga() - o.getRiga();

        if (dx == 0 && dy == 0) return true;

        // sorgente o destinazione bloccate?
        if (!g.isLibera(o.getRiga(), o.getColonna())
            || !g.isLibera(d.getRiga(), d.getColonna())
            || ostacoli.contains(d)) return false;

        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        int diagSteps = Math.min(absDx, absDy);
        int remSteps = Math.abs(absDx - absDy);

        if (tipo == 1) {
            // diagonale
            if (!percorri(g, o, stepX, stepY, diagSteps, ostacoli)) return false;

            Cella mid = g.getCella(
                o.getRiga() + stepY * diagSteps,
                o.getColonna() + stepX * diagSteps
            );

            // poi orizz/vert
            if (absDx > absDy)
                return percorri(g, mid, stepX, 0, remSteps, ostacoli);
            else
                return percorri(g, mid, 0, stepY, remSteps, ostacoli);

        } else { // tipo 2

            Cella mid;

            // orizz/vert
            if (absDx > absDy) {
                if (!percorri(g, o, stepX, 0, remSteps, ostacoli)) return false;
                mid = g.getCella(o.getRiga(), o.getColonna() + stepX * remSteps);
            } else {
                if (!percorri(g, o, 0, stepY, remSteps, ostacoli)) return false;
                mid = g.getCella(o.getRiga() + stepY * remSteps, o.getColonna());
            }

            // poi diagonale
            return percorri(g, mid, stepX, stepY, diagSteps, ostacoli);
        }
    }

    // Percorre la griglia lungo una direzione
    private static boolean percorri(Griglia g, Cella start, int stepX, int stepY, int steps, Set<Cella> ostacoli) {
        int r = start.getRiga();
        int c = start.getColonna();

        for (int i = 0; i < steps; i++) {
            r += stepY;
            c += stepX;

            // Cella corrente
            Cella attuale = g.getCella(r, c);

            // Se fuori griglia → bloccato
            if (attuale == null) return false;

            // Se non libera nella griglia → bloccato
            if (!attuale.isAttraversabile()) return false;

            // SESTA COSA IMPORTANTE: se è negli ostacoli dinamici -> bloccato
            if (ostacoli.contains(attuale)) return false;
        }

        return true;
    }

    // Calcola il contesto di O (punti raggiungibili con cammini di tipo 1)
    public static Set<Cella> calcolaContesto(Griglia g, Cella o, Set<Cella> ostacoli) {
    	Set<Cella> contesto = new HashSet<>();

    	for (int r = 0; r < g.getRighe(); r++) {
    		for (int c = 0; c < g.getColonne(); c++) {

    			Cella cella = g.getCella(r, c);

	            // Se la cella è bloccata o appartiene agli ostacoli dinamici -> salta
	            if (!cella.isAttraversabile() || ostacoli.contains(cella)) {
	                continue;
	            }
	
	            // Se esiste un cammino libero di tipo 1, allora la cella appartiene al contesto
	            if (camminoLibero(g, o, cella, 1, ostacoli)) {
	                contesto.add(cella);
	            }
	        }
	    }
	
	    return contesto;
	}

    // Calcola il complemento di O (punti raggiungibili SOLO con cammini di tipo 2)
    public static Set<Cella> calcolaComplemento(Griglia g, Cella o, Set<Cella> ostacoli) {
        Set<Cella> complemento = new HashSet<>();

        for (int r = 0; r < g.getRighe(); r++) {
            for (int c = 0; c < g.getColonne(); c++) {

                Cella cella = g.getCella(r, c);

                if (!cella.isAttraversabile() || ostacoli.contains(cella)) continue;

                if (camminoLibero(g, o, cella, 2, ostacoli)
                    && !camminoLibero(g, o, cella, 1, ostacoli)) {

                    complemento.add(cella);
                }
            }
        }

        return complemento;
    }
    
    // Controlla che il punto appartenga alla frontiera
    public static boolean controllaVicini(Cella cella, Griglia griglia, Set<Cella> chiusura) {
        int[] dx = {-1, 0, 1};
        int[] dy = {-1, 0, 1};

        for (int fx : dx) {
            for (int fy : dy) {

                if (fx == 0 && fy == 0) continue;

                int nx = cella.getColonna() + fx;
                int ny = cella.getRiga() + fy;

                //if (!griglia.inBounds(nx, ny)) continue;

                if (griglia.isLibera(ny, nx) && !chiusura.contains(griglia.getCella(ny, nx))) {
                    return true;
                }
            }
        }
        return false;
    }

    
    // Calcola la frontiera
    public static Set<Cella> calcolaFrontiera(Set<Cella> contesto, Set<Cella> complemento, Griglia griglia) {
    	Set<Cella> frontiera = new HashSet<Cella>();
    	
    	// Calcola la chiusura
    	Set<Cella> chiusura = new HashSet<>(contesto);
    	chiusura.addAll(complemento);

    	for (Cella p : chiusura) {
    		/*
    		if (controllaVicini(p, griglia, chiusura)) {
    			frontiera.add(p);
    		}
    		*/
    		if (controllaVicini(p, griglia, chiusura)) {
    		    frontiera.add(new Cella(p.getRiga(), p.getColonna(), p.getTipo()));
    		}
    		
    	}
    	return frontiera;
    }
    
    // Restituisce le celle che compongono gli ostacoli della griglia
    public static Set<Cella> trovaOstacoli(Griglia griglia) {
    	Set<Cella> ostacoli = new HashSet<Cella>();
    	for (int i=0; i < griglia.getRighe(); i++) {
    		for (int j=0; j < griglia.getColonne(); j++) {
    			Cella cella = griglia.getCella(i, j);
    			if (!cella.isAttraversabile()) {
    				ostacoli.add(cella);
    			}
    		}
    	}
    	return ostacoli;
    }
    
    // Unisce 3 set di celle
    public static Set<Cella> unisci(Set<Cella> seq1, Set<Cella> seq2, Set<Cella> seq3) {
        Set<Cella> chiusura = new HashSet<>(seq1);
        chiusura.addAll(seq2);
        chiusura.addAll(seq3);
        return chiusura;
    }
    
    // Unisce 2 set di celle rimuovendo il primo elemento della seconda
    public static List<Cella> compatta(List<Cella> seq1, List<Cella> seq2) {
        List<Cella> risultato = new ArrayList<>(seq1);
        // aggiungi tutti gli elementi di seq2 tranne il primo
        risultato.addAll(seq2.subList(1, seq2.size()));
        return risultato;
    }

    // Calcola il cammino minimo da O a un D che non appartiene alla sua chiusura
    public static RisultatoCammino camminoMin(Cella o, Cella d, Griglia griglia, Set<Cella> ostacoli) {
        // Calcola contesto e complemento rispetto agli ostacoli correnti
        Set<Cella> contesto = calcolaContesto(griglia, o, ostacoli);
        Set<Cella> complemento = calcolaComplemento(griglia, o, ostacoli);

        // Chiusura attuale
        Set<Cella> chiusura = new HashSet<>(contesto);
        chiusura.addAll(complemento);

        // Se D è già nella chiusura, ritorna soluzione diretta
        if (contesto.contains(d)) {
            return new RisultatoCammino(dlib(o, d, contesto, complemento),
                List.of(new Cella(o.getColonna(), o.getRiga(),0), new Cella(d.getColonna(), d.getRiga(),1)));
        }
        if (complemento.contains(d)) {
            return new RisultatoCammino(dlib(o, d, contesto, complemento),
                List.of(new Cella(o.getColonna(), o.getRiga(),0), new Cella(d.getColonna(), d.getRiga(),2)));
        }

        // Calcola frontiera secondo definizione corretta
        Set<Cella> frontiera = calcolaFrontiera(contesto, complemento, griglia);

        // CASO BASE: se frontiera è vuota => non si può espandere -> impossibile
        if (frontiera.isEmpty()) {
            return new RisultatoCammino(Double.POSITIVE_INFINITY, List.of());
        }

        // Per evitare cicli: costruisci nuovo set di ostacoli per le chiamate ricorsive
        //    questo è il significato di unisci(ostacoli, contesto, complemento)
        Set<Cella> nuoviOstacoliBase = new HashSet<>(ostacoli);
        nuoviOstacoliBase.addAll(chiusura);

        double lunghezzaMin = Double.POSITIVE_INFINITY;
        List<Cella> seqMin = new ArrayList<>();

        for (Cella e : frontiera) {
           
            int tipo = e.getTipo();

            double lF = dlib(o, e, contesto, complemento);
            if (lF >= lunghezzaMin) continue; // pruning

            // Crea una copia per la chiamata ricorsiva (modifica lo stato)
            Set<Cella> ostacoliRic = new HashSet<>(nuoviOstacoliBase);

            RisultatoCammino parziale = camminoMin(e, d, griglia, ostacoliRic);

            double lFD = parziale.getLunghezza();
            if (Double.isInfinite(lFD)) continue;

            double lTot = lF + lFD;
            if (lTot < lunghezzaMin) {
                lunghezzaMin = lTot;
                seqMin = compatta(
                    List.of(new Cella(o.getColonna(), o.getRiga(),0), new Cella(e.getColonna(), e.getRiga(), tipo)),
                    parziale.getSeq()
                );
            }
        }

        return new RisultatoCammino(lunghezzaMin, seqMin);
    }
}