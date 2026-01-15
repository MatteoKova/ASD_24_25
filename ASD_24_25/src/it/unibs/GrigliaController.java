package it.unibs;

import java.util.Scanner;

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
    Scanner scanner = new Scanner(System.in);

    boolean continua = true;

    while (continua)
    {
        tester.askParameters();
        tester.runTests();

        System.out.print("Do you want to run another batch? (y/n): ");
        String risposta = scanner.nextLine().trim().toLowerCase();

        if (!risposta.equals("y"))
        {
            continua = false;
        }
    }

    tester.writeCsvReport();
}

	}
}