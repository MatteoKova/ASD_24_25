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
import java.util.Map;
import java.util.Set;

public class AnalizzatoreCammini {

    // Calcolo della distanza libera dlib(O, D)
    public static double dlib(Cella o, Cella d, Set<Cella> contesto, Set<Cella> complemento) {
    	if (contesto.contains(d) || complemento.contains(d)) {
    		int dx = Math.abs(d.getX() - o.getX());
            int dy = Math.abs(d.getY() - o.getY());
            int deltaMin = Math.min(dx, dy);
            int deltaMax = Math.max(dx, dy);
            return Math.sqrt(2) * deltaMin + (deltaMax - deltaMin);
    	}
    	else return 0;
    }

    // Calcola se esiste un cammino libero di tipo 1 o 2
    public static boolean camminoLibero(Griglia g, Cella o, Cella d, int tipo) {
        int dx = d.getX() - o.getX();
        int dy = d.getY() - o.getY();

        if (dx == 0 && dy == 0) return true;
        if (!g.isFree(o.getY(), o.getX()) || !g.isFree(d.getY(), d.getX())) return false;

        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int diagSteps = Math.min(absDx, absDy);
        int remSteps = Math.abs(absDx - absDy);

        // tipo 1: diagonale poi orizz/vert
        if (tipo == 1) {
            if (!percorri(g, o, stepX, stepY, diagSteps)) return false;
            Cella mid = new Cella(o.getX() + stepX * diagSteps, o.getY() + stepY * diagSteps);
            if (absDx > absDy)
                return percorri(g, mid, stepX, 0, remSteps);
            else
                return percorri(g, mid, 0, stepY, remSteps);
        } else { // tipo 2: orizz/vert poi diagonale
            Cella mid;
            if (absDx > absDy) {
                if (!percorri(g, o, stepX, 0, remSteps)) return false;
                mid = new Cella(o.getX() + stepX * remSteps, o.getY());
            } else {
                if (!percorri(g, o, 0, stepY, remSteps)) return false;
                mid = new Cella(o.getX(), o.getY() + stepY * remSteps);
            }
            return percorri(g, mid, stepX, stepY, diagSteps);
        }
    }

    // Percorre un certo numero di passi in una direzione
    private static boolean percorri(Griglia g, Cella start, int dx, int dy, int passi) {
        int x = start.getX();
        int y = start.getY();
        for (int i = 0; i < passi; i++) {
            x += dx;
            y += dy;
            if (!g.inBounds(y, x) || !g.isFree(y, x)) return false;
        }
        return true;
    }

    // Calcola il contesto (celle raggiungibili con cammini tipo 1)
    public static Set<Cella> calcolaContesto(Griglia g, Cella o) {
        Set<Cella> contesto = new HashSet<>();
        for (int r = 0; r < g.getRighe(); r++) {
            for (int c = 0; c < g.getColonne(); c++) {
                Cella D = new Cella(c, r);
                if (camminoLibero(g, o, D, 1))
                    contesto.add(D);
            }
        }
        return contesto;
    }

    // Calcola il complemento (celle raggiungibili SOLO con cammini tipo 2)
    public static Set<Cella> calcolaComplemento(Griglia g, Cella o) {
        Set<Cella> complemento = new HashSet<>();
        for (int r = 0; r < g.getRighe(); r++) {
            for (int c = 0; c < g.getColonne(); c++) {
                Cella D = new Cella(c, r);
                if (!camminoLibero(g, o, D, 1) && camminoLibero(g, o, D, 2))
                    complemento.add(D);
            }
        }
        return complemento;
    }
    
    // Controlla che il punto appartenga alla frontiera
    public static boolean controllaVicini(Cella cella, Griglia griglia, Set<Cella> chiusura) {
    	// Ascissa vicini a sinistra
    	int xsinistra = cella.getX() - 1;
    	// Ascissa vicini a al centro
    	int xcentro = cella.getX();
    	// Ascissa vicini a destra
    	int xdestra = cella.getX() + 1;
    	// Ordinata vicini sopra
    	int ysopra = cella.getY() - 1;
    	// Ordinata vicini al centro
    	int ycentro = cella.getY();
    	// Ordinata vicini sotto
    	int ysotto = cella.getY() + 1;
    	
    	
    	// Punto sopra-sinistra
    	// da aggiungere alla condizione if: && !chiusura.contains(Cella(xsinistra, ysopra))
    	if (griglia.isFree(xsinistra, ysopra)) {
    		return true;
    	}
    	// Punto sopra-centro
    	if (griglia.isFree(xcentro, ysopra)) {
    		return true;
    	}
    	// Punto sopra-destra
    	if (griglia.isFree(xdestra, ysopra)) {
    		return true;
    	}
    	
    	// Punto centro-sinistra
    	if (griglia.isFree(xsinistra, ycentro)) {
    		return true;
    	}
    	// Punto centro-centro
    	if (griglia.isFree(xcentro, ycentro)) {
    		return true;
    	}
    	// Punto sotto-destra
    	if (griglia.isFree(xdestra, ycentro)) {
    		return true;
    	}
    	
    	// Punto sotto-sinistra
    	if (griglia.isFree(xsinistra, ysotto)) {
    		return true;
    	}
    	// Punto sotto-centro
    	if (griglia.isFree(xcentro, ysotto)) {
    		return true;
    	}
    	// Punto sotto-destra
    	if (griglia.isFree(xdestra, ysotto)) {
    		return true;
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
    		if (controllaVicini(p, griglia, chiusura)) {
    			frontiera.add(p);
    		}
    	}
    	return frontiera;
    }

    /*
    // Calcola la lunghezza minima del cammino tra O e D, e la sequenza dei landmark lungo un cammino minimo.
    public static RisultatoCammino camminoMin(Point O, Point D, Dimensioni dim, Set<Point> ostacoli, Griglia2 griglia) {

        // Calcola il contesto (celle raggiungibili da O con cammini di tipo 1)
        Set<Point> contesto = calcolaContesto(O, dim, ostacoli);
        //Set<Point> contesto2 = calcolaContesto(griglia, O);
        
        // Calcola il complemento (celle raggiungibili solo con cammini di tipo 2)
        Set<Point> complemento = calcolaComplemento(O, dim, ostacoli);

        // Se D appartiene al contesto, è raggiungibile direttamente
        if (contesto.contains(D)) {
            return new RisultatoCammino(dlib(O, D, contesto, complemento), List.of(new Landmark(O, 0), new Landmark(D, 1)));
        }

        // Se D appartiene al complemento, anche in questo caso è direttamente raggiungibile
        if (complemento.contains(D)) {
            return new RisultatoCammino(dlib(O, D, contesto, complemento),List.of(new Landmark(O, 0), new Landmark(D, 2)));
        }

        // Calcola la "frontiera" tra contesto e complemento
        Set<FrontieraElemento> frontiera = calcolaFrontiera(contesto, complemento, dim, ostacoli);

        // Se non esiste frontiera, nessun cammino possibile
        if (frontiera.isEmpty()) {
            return new RisultatoCammino(Double.POSITIVE_INFINITY, List.of());
        }

        // Inizializza valori minimi
        double lunghezzaMin = Double.POSITIVE_INFINITY;
        List<Landmark> seqMin = new ArrayList<>();

        // Analizza ciascun punto di frontiera
        for (FrontieraElemento elem : frontiera) {
            Point F = elem.punto;
            int tipo = elem.tipo;

            double lF = dlib(O, F, contesto, complemento);

            // Se la distanza O-F è già troppo grande, salta
            if (lF < lunghezzaMin) {

                // Calcola ricorsivamente il cammino minimo da F a D
                RisultatoCammino parziale = camminoMin(F, D, dim, unisci(ostacoli, contesto, complemento)
                );

                double lFD = parziale.lunghezza;
                double lTot = lF + lFD;

                // Se questo cammino è migliore, aggiornalo
                if (lTot < lunghezzaMin) {
                    lunghezzaMin = lTot;
                    seqMin = compatta(List.of(new Landmark(O, 0), new Landmark(F, tipo)), parziale.seq);
                }
            }
        }

        // Ritorna lunghezza minima e sequenza di landmark
        return new RisultatoCammino(lunghezzaMin, seqMin);
    }
    */
}