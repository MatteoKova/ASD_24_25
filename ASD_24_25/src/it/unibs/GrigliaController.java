package it.unibs;

import javax.swing.SwingUtilities;

public class GrigliaController {
	
	AnalizzatoreCammini analizzatore_cammini ;

	public GrigliaController() {
		
		
		SwingUtilities.invokeLater(() -> {

			GrillCreator creator= new GrillCreator();
			creator.setVisible(true);

		});
		
		
		
		//this.analizzatore_cammini = new AnalizzatoreCammini();

	}
	

}
