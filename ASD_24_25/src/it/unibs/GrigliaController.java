package it.unibs;

import javax.swing.SwingUtilities;

public class GrigliaController {
	
	AnalizzatoreCammini analizzatore_cammini ;

	public GrigliaController() {
		
		
		SwingUtilities.invokeLater(() -> {

			CreatoreGriglia creator= new CreatoreGriglia();
			creator.setVisible(true);

		});
		
		
		
		//this.analizzatore_cammini = new AnalizzatoreCammini();

	}
	

}
