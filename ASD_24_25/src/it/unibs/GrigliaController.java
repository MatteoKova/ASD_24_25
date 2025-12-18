package it.unibs;

import javax.swing.SwingUtilities;

public class GrigliaController {
	
	AnalizzatoreCammini analizzatore_cammini ;

	public GrigliaController(int modalita) {
		if (modalita == 1) // grafica
		{		SwingUtilities.invokeLater(() -> {

			CreatoreGriglia creator= new CreatoreGriglia();
			creator.setVisible(true);

		});}
		else if (modalita == 0) // batch
		{
			// codice batch
		}
		//this.analizzatore_cammini = new AnalizzatoreCammini();

	}
}