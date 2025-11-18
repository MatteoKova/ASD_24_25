package it.unibs;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
    
        SwingUtilities.invokeLater(() -> {

            GrillCreator creator= new GrillCreator();
            creator.setVisible(true);
         
        });
    }

}