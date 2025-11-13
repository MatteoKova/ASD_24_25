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

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class AnalizzatoreCammini {

    // Calcolo della distanza libera dlib(O, D)
    public static double dlib(Point O, Point D) {
        int dx = Math.abs(D.x - O.x);
        int dy = Math.abs(D.y - O.y);
        int deltaMin = Math.min(dx, dy);
        int deltaMax = Math.max(dx, dy);
        return Math.sqrt(2) * deltaMin + (deltaMax - deltaMin);
    }

    // Calcola se esiste un cammino libero di tipo 1 o 2
    public static boolean camminoLibero(Griglia g, Point O, Point D, int tipo) {
        int dx = D.x - O.x;
        int dy = D.y - O.y;

        if (dx == 0 && dy == 0) return true;
        if (!g.isFree(O.y, O.x) || !g.isFree(D.y, D.x)) return false;

        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int diagSteps = Math.min(absDx, absDy);
        int remSteps = Math.abs(absDx - absDy);

        // tipo 1: diagonale poi orizz/vert
        if (tipo == 1) {
            if (!percorri(g, O, stepX, stepY, diagSteps)) return false;
            Point mid = new Point(O.x + stepX * diagSteps, O.y + stepY * diagSteps);
            if (absDx > absDy)
                return percorri(g, mid, stepX, 0, remSteps);
            else
                return percorri(g, mid, 0, stepY, remSteps);
        } else { // tipo 2: orizz/vert poi diagonale
            Point mid;
            if (absDx > absDy) {
                if (!percorri(g, O, stepX, 0, remSteps)) return false;
                mid = new Point(O.x + stepX * remSteps, O.y);
            } else {
                if (!percorri(g, O, 0, stepY, remSteps)) return false;
                mid = new Point(O.x, O.y + stepY * remSteps);
            }
            return percorri(g, mid, stepX, stepY, diagSteps);
        }
    }

    // Percorre un certo numero di passi in una direzione
    private static boolean percorri(Griglia g, Point start, int dx, int dy, int passi) {
        int x = start.x;
        int y = start.y;
        for (int i = 0; i < passi; i++) {
            x += dx;
            y += dy;
            if (!g.inBounds(y, x) || !g.isFree(y, x)) return false;
        }
        return true;
    }

    // Calcola il contesto (celle raggiungibili con cammini tipo 1)
    public static Set<Point> calcolaContesto(Griglia g, Point O) {
        Set<Point> contesto = new HashSet<>();
        for (int r = 0; r < g.rows; r++) {
            for (int c = 0; c < g.cols; c++) {
                Point D = new Point(c, r);
                if (camminoLibero(g, O, D, 1))
                    contesto.add(D);
            }
        }
        return contesto;
    }

    // Calcola il complemento (celle raggiungibili SOLO con cammini tipo 2)
    public static Set<Point> calcolaComplemento(Griglia g, Point O) {
        Set<Point> complemento = new HashSet<>();
        for (int r = 0; r < g.rows; r++) {
            for (int c = 0; c < g.cols; c++) {
                Point D = new Point(c, r);
                if (!camminoLibero(g, O, D, 1) && camminoLibero(g, O, D, 2))
                    complemento.add(D);
            }
        }
        return complemento;
    }
}