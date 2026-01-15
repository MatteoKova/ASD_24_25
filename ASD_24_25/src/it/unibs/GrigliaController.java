package it.unibs;

import javax.swing.SwingUtilities;

public class GrigliaController {
	
	

	public GrigliaController(int modalita, java.util.List<Griglia> griglie ) {
		if (modalita == 1) // grafica
		{		SwingUtilities.invokeLater(() -> {

			CreatoreGriglia creator= new CreatoreGriglia();
			creator.setVisible(true);

		});}
		else if (modalita == 0) // batch
		{
			GriglieTester tester = new GriglieTester(griglie);
			tester.askParameters();
			tester.runTests();
		}
		//this.analizzatore_cammini = new AnalizzatoreCammini();

	}
}