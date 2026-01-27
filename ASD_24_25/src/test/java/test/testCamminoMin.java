package test.java.test;

import it.unibs.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnalizCamTest {

    private AnalizCam analizCam;
    private Griglia griglia;
    private GeneratoreGriglieOstacoli genGriglie;
    private Configurazione config;

    private static final double EPSILON = 1e-6;

    @BeforeEach
    void setUp() {

        // Griglia semplice e deterministica
        griglia = new Griglia(10, 10, TipoPattern.SCACCHI);
        boolean[][] ostacoli = new boolean[griglia.getRighe()][griglia.getColonne()];
        config = new Configurazione(0);
        config.tipoPattern = TipoPattern.SCACCHI;
        genGriglie = new GeneratoreGriglieOstacoli(config, ostacoli);
        genGriglie.generaTutto();

        griglia.setOstacoli(ostacoli);

        griglia.setDestinazioneDefault(new Cella(9, 0, TipoCella.ATTRAVERSABILE));
        griglia.setOrigineDefault(new Cella(0, 1, TipoCella.ATTRAVERSABILE));
        analizCam = new AnalizCam(griglia);

        // Configurazione standard (adatta ai tuoi default)
        ConfigurazioneAnalizzatore config = new ConfigurazioneAnalizzatore();
        config.setUsaMemoriaSequenze(0);
        config.setLimiteTempoMs(0);
        config.setCondForte(1);
        config.setRiordinaFrontiera(false);
        config.setUsaLunghezzaGlob(0);
        analizCam.setConfig(config);
        analizCam.resetContatori(griglia.getRighe(), griglia.getColonne());
    }

    @Test
    void testCamminoMinOttimizzato_LunghezzaSimmetrica() {

        Cella origine = griglia.getOrigineDefault();
        Cella destinazione = griglia.getDestinazioneDefault();
        Cella[][] celle = griglia.getCelle();

        Supplier<Boolean> stopCheck = () -> false;

        // Prima esecuzione: origine -> destinazione
        RisultatoCammino risultato1 = analizCam.camminoMinOttimizzato(
                origine,
                destinazione,
                celle,
                0.0,
                stopCheck);

        assertNotNull(risultato1);
        assertTrue(risultato1.getLunghezza() < Double.POSITIVE_INFINITY);

        // Seconda esecuzione: destinazione -> origine
        analizCam.resetContatori(griglia.getRighe(), griglia.getColonne());

        RisultatoCammino risultato2 = analizCam.camminoMinOttimizzato(
                destinazione,
                origine,
                celle,
                0.0,
                stopCheck);

        assertNotNull(risultato2);
        assertTrue(risultato2.getLunghezza() < Double.POSITIVE_INFINITY);

        // Confronto con epsilon
        assertEquals(
                risultato1.getLunghezza(),
                risultato2.getLunghezza(),
                EPSILON,
                "La lunghezza del cammino dovrebbe essere simmetrica");
    }
}
