package it.unibs;

import javax.swing.*;
import java.awt.*;


public class GrillCreator extends JFrame {
    private JTextField rowsField, colsField;
    private JButton generateRandomBtn;
    private JComboBox<String> densityBox;
    private JPanel grillPanel;
    private ManualGridPanel manualPanel;
private boolean[][] backupGrid;

 
    private int rows, cols;
    private long seed = 0;

    // For manual mode
    private JButton undoBtn, confirmBtn;

    public GrillCreator() {
        setTitle("Grill Creator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Rows:"));
        rowsField = new JTextField("10", 5);
        topPanel.add(rowsField);

        topPanel.add(new JLabel("Columns:"));
        colsField = new JTextField("10", 5);
        topPanel.add(colsField);

        densityBox = new JComboBox<>(new String[]{"Low", "Medium", "High", "Custom"});
        topPanel.add(new JLabel("Obstacle density:"));
        topPanel.add(densityBox);

        generateRandomBtn = new JButton("Generate Grill");
       

        topPanel.add(generateRandomBtn);
       

        add(topPanel, BorderLayout.NORTH);

        grillPanel = new JPanel();
        add(grillPanel, BorderLayout.CENTER);

        generateRandomBtn.addActionListener(e -> createRandomGrill());
    }

    private void readDimensions() {
        rows = Integer.parseInt(rowsField.getText());
        cols = Integer.parseInt(colsField.getText());
    }

    private void prepareGridPanel() {
        grillPanel.removeAll();
        grillPanel.setLayout(new GridLayout(rows, cols));
    }


    private void createRandomGrill() {
        readDimensions();
        prepareGridPanel();
       

        double density = 0.1;
        String d = (String) densityBox.getSelectedItem();
      
        if ("Medium".equals(d)) density = 0.25;
        if ("High".equals(d)) density = 0.45;
        
          
        Configurazione cfg = new Configurazione();
            cfg.righe = rows;
            cfg.colonne = cols;
            cfg.density = density;
            if (seed != 0) cfg.seed = seed;

            GeneratoreGriglieOstacoli gen = new GeneratoreGriglieOstacoli(cfg);
            
          if ("Custom".equals(d)) {
            custumizeObstacles(gen);
        } else {
            gen.generateAll();
            new GrigliaFrame(gen.getGrid());}

        


    

       
    }

    public void custumizeObstacles(GeneratoreGriglieOstacoli gen){
       
    grillPanel.removeAll();
    grillPanel.setLayout(new BorderLayout());

    manualPanel = new ManualGridPanel(rows, cols);
    grillPanel.add(manualPanel, BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel();
    undoBtn = new JButton("Undo");
    confirmBtn = new JButton("Confirm");

    bottomPanel.add(undoBtn);
    bottomPanel.add(confirmBtn);
    grillPanel.add(bottomPanel, BorderLayout.SOUTH);

    // Save backup for undo
    backupGrid = new boolean[rows][cols];

    undoBtn.addActionListener(e -> {
        manualPanel.setObstacles(backupGrid);
    });

    confirmBtn.addActionListener(e -> {
        boolean[][] obs = manualPanel.getObstacles();
        Griglia g = gen.getGrid();

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (obs[r][c])
                    g.setObstacle(r, c);



        g.calculateOstacoli();    
        new GrigliaFrame(g);  // open analysis frame
    });

    grillPanel.revalidate();
    grillPanel.repaint();
    }
}

