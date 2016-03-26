package astar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class StarPanel extends JPanel {
    
	private final static int
	INFINITY = Integer.MAX_VALUE, 
	EMPTY    = 0,
	OBST     = 1, 
	path    = 2, 
	END   = 3, 
	OPENSPACES = 4, 
	CLOSEDSPACES   = 5, 
	ROUTE    = 6; 
	
	JLabel results;
    JTextField rowsField, columnsField;
    JRadioButton aStar, dijkstra;
    JSlider speedSlider;
    
    int rows    = 25,    
        columns = 25,         
        blockSize = 500/rows; 
    int arrowSize = blockSize/2; 
    
    public static boolean fileInput = false;
    
	ArrayList<GridBlock> openSet   = new ArrayList();
	ArrayList<GridBlock> ClosedSpacesSet = new ArrayList();
	ArrayList<GridBlock> graph     = new ArrayList();
     
    GridBlock pathStart;
    GridBlock ENDPos; 
    
    static int[][] board;     
    static boolean found;    
    static boolean searching;   
    static boolean endOfSearch; 
    int delay; 
    
    RepaintAction action = new RepaintAction();
    Timer timer;
    
    
    private class MouseHandler implements MouseListener, MouseMotionListener {
        private int cur_row, cur_col, cur_val;
        @Override
        public void mousePressed(MouseEvent evt) {
            int row = (evt.getY() - 10) / blockSize;
            int col = (evt.getX() - 10) / blockSize;
            if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found) {
                cur_row = row;
                cur_col = col;
                cur_val = board[row][col];
                if (cur_val == EMPTY){
                    board[row][col] = OBST;
                }
                if (cur_val == OBST){
                    board[row][col] = EMPTY;
                }
            }
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            int row = (evt.getY() - 10) / blockSize;
            int col = (evt.getX() - 10) / blockSize;
            if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found){
                if ((row*columns+col != cur_row*columns+cur_col) && (cur_val == path || cur_val == END)){
                    int new_val = board[row][col];
                    if (new_val == EMPTY){
                        board[row][col] = cur_val;
                        if (cur_val == path) {
                            pathStart.row = row;
                            pathStart.col = col;
                        } else {
                            ENDPos.row = row;
                            ENDPos.col = col;
                        }
                        board[cur_row][cur_col] = new_val;
                        cur_row = row;
                        cur_col = col;
                        if (cur_val == path) {
                            pathStart.row = cur_row;
                            pathStart.col = cur_col;
                        } else {
                            ENDPos.row = cur_row;
                            ENDPos.col = cur_col;
                        }
                        cur_val = board[row][col];
                    }
                } else if (board[row][col] != path && board[row][col] != END){
                    board[row][col] = OBST;            
                }
            }
            repaint();
        }//mouseDragged

        @Override
        public void mouseReleased(MouseEvent evt) { }
        @Override
        public void mouseEntered(MouseEvent evt) { }
        @Override
        public void mouseExited(MouseEvent evt) { }
        @Override
        public void mouseMoved(MouseEvent evt) { }
        @Override
        public void mouseClicked(MouseEvent evt) { }
        
    }//MouseHandler
    
    private class ActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            String cmd = evt.getActionCommand();
            if (cmd.equals("Reset")) {
                fillboard();
                aStar.setEnabled(true);
                dijkstra.setEnabled(true);
            } else if (cmd.equals("Single Iteration") && !found && !endOfSearch) {
                if (!searching && dijkstra.isSelected()) {
                    initializeDijkstra();
                }
                searching = true;
                aStar.setEnabled(false);
                dijkstra.setEnabled(false);
                timer.stop();
                if ((dijkstra.isSelected() && graph.isEmpty()) ||
                              (!dijkstra.isSelected() && openSet.isEmpty()) ) {
                    endOfSearch = true;
                    board[pathStart.row][pathStart.col]=path;
                } else {
                    expandNode();
                    if (found) {
                        plotRoute();
                    }
                }
                repaint();
            } else if (cmd.equals("Automatic Iteration") && !endOfSearch) {
                if (!searching && dijkstra.isSelected()) {
                    initializeDijkstra();
                }
                searching = true;
                aStar.setEnabled(false);
                dijkstra.setEnabled(false);
                timer.setDelay(delay);
                timer.start();
            }
        }
    }//ActionHandler

    private class RepaintAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            if ((dijkstra.isSelected() && graph.isEmpty()) ||
                          (!dijkstra.isSelected() && openSet.isEmpty()) ) {
                endOfSearch = true;
                board[pathStart.row][pathStart.col]=path;
            } else {
                expandNode();
                if (found) {
                    timer.stop();
                    endOfSearch = true;
                    plotRoute();
                }
            }
            repaint();
        }
    }//RepaintAction
  
  
	
	 
    
    
  
    public StarPanel(int width, int height) {
  
        setLayout(null);
        Driver.globalSp = this;
        MouseHandler listener = new MouseHandler();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        setBackground(Color.black);

        setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.black));

        setPreferredSize( new Dimension(width,height) );

        board = new int[rows][columns];
        
        results = new JLabel("Results: ");
        results.setBackground(Color.lightGray);
        results.setForeground(Color.white);
        results.setFont(new Font("Comic-Sans",Font.PLAIN,10));

        JLabel entryText = new JLabel("Enter row and column value:");
        entryText.setBackground(Color.gray);
        entryText.setForeground(Color.white);
        
        JLabel redrawText = new JLabel("Then press Redraw Board");
        redrawText.setBackground(Color.gray);
        redrawText.setForeground(Color.white);
        
        JLabel rowsLbl = new JLabel("Rows(5-100):", JLabel.RIGHT);
        rowsLbl.setBackground(Color.gray);
        rowsLbl.setForeground(Color.white);
        rowsLbl.setFont(new Font("Comic-Sans",Font.PLAIN,13));

        rowsField = new JTextField();
        rowsField.setBackground(Color.lightGray);
        rowsField.setForeground(Color.white);
        rowsField.setText(Integer.toString(rows));

        JLabel columnsLbl = new JLabel("Columns(5-100):", JLabel.RIGHT);
        columnsLbl.setBackground(Color.gray);
        columnsLbl.setForeground(Color.white);
        columnsLbl.setFont(new Font("Comic-Sans",Font.PLAIN,13));

        columnsField = new JTextField();
        columnsField.setBackground(Color.lightGray);
        columnsField.setForeground(Color.white);
        columnsField.setText(Integer.toString(columns));

        JButton fileButton = new JButton("File Input");
        fileButton.addActionListener(new ActionHandler());
        fileButton.setBackground(Color.gray);
        fileButton.setForeground(Color.white);
        fileButton.addActionListener(new java.awt.event.ActionListener(){
        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent evt){
        		fileButtonActionPerformed(evt);
        	}
        });
        
        JButton redrawButton = new JButton("Redraw board");
        redrawButton.addActionListener(new ActionHandler());
        redrawButton.setBackground(Color.gray);
        redrawButton.setForeground(Color.white);
        redrawButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        JButton mazeButton = new JButton("Random board");
        mazeButton.addActionListener(new ActionHandler());
        mazeButton.setBackground(Color.gray);
        mazeButton.setForeground(Color.white);
        mazeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mazeButtonActionPerformed(evt);
            }
        });

        JButton ResetButton = new JButton("Reset");
        ResetButton.addActionListener(new ActionHandler());
        ResetButton.setBackground(Color.red);
        ResetButton.setForeground(Color.white);

        JButton stepButton = new JButton("Single Iteration");
        stepButton.addActionListener(new ActionHandler());
        stepButton.setBackground(Color.gray);
        stepButton.setForeground(Color.white);

        JButton AutomaticIterationButton = new JButton("Automatic Iteration");
        AutomaticIterationButton.addActionListener(new ActionHandler());
        AutomaticIterationButton.setBackground(Color.gray);
        AutomaticIterationButton.setForeground(Color.white);

        JLabel velocity = new JLabel("Speed", JLabel.CENTER);
        velocity.setBackground(Color.gray);
        velocity.setForeground(Color.white);
        velocity.setFont(new Font("Comic-Sans",Font.PLAIN,10));
        
        speedSlider = new JSlider(0,1000,500); 
        speedSlider.setBackground(Color.black);
        speedSlider.setForeground(Color.white);
        
        delay = 1000-speedSlider.getValue();
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                JSlider source = (JSlider)evt.getSource();
                if (!source.getValueIsAdjusting()) {
                    delay = 1000-source.getValue();
                }
            }
        });
        ButtonGroup algoGroup = new ButtonGroup();

        aStar = new JRadioButton("A*");
        aStar.setToolTipText("A* algorithm");
        aStar.setBackground(Color.black);
        aStar.setForeground(Color.white);
        algoGroup.add(aStar);
        aStar.addActionListener(new ActionHandler());

        dijkstra = new JRadioButton("Dijkstra");
        dijkstra.setBackground(Color.black);
        dijkstra.setForeground(Color.white);
        algoGroup.add(dijkstra);
        dijkstra.addActionListener(new ActionHandler());
        aStar.setSelected(true); 
       
        JLabel path = new JLabel("Start", JLabel.CENTER);
        path.setBackground(Color.black);
        path.setForeground(Color.red);
        path.setFont(new Font("Comic_Sans",Font.PLAIN,14));

        JLabel END = new JLabel("End", JLabel.CENTER);
        END.setBackground(Color.black);
        END.setForeground(Color.GREEN);
        END.setFont(new Font("Comic_Sans",Font.PLAIN,14));
     
        JLabel OPENSPACES = new JLabel("Open", JLabel.CENTER);
        OPENSPACES.setBackground(Color.black);
        OPENSPACES.setForeground(Color.blue);
        OPENSPACES.setFont(new Font("Comic_Sans",Font.PLAIN,14));

        JLabel CLOSEDSPACES = new JLabel("Closed", JLabel.CENTER);
        CLOSEDSPACES.setBackground(Color.black);
        CLOSEDSPACES.setForeground(Color.LIGHT_GRAY);
        CLOSEDSPACES.setFont(new Font("Comic_Sans",Font.PLAIN,14));

        JPanel colorKey = new JPanel();
        colorKey.setBackground(Color.black);
        colorKey.setFont(new Font("Comic_Sans",Font.PLAIN,14));
        colorKey.setBorder(BorderFactory.createMatteBorder(3,3,3,3,Color.black));

        //Add things to panel
        add(results);
        add(aStar);
        add(dijkstra);
        add(entryText);
        add(rowsField);
        add(rowsLbl);
        add(columnsLbl);
        add(columnsField);
        add(redrawText);
        add(redrawButton);
        add(mazeButton);
        add(fileButton);
        add(ResetButton);
        add(stepButton);
        add(AutomaticIterationButton);
        add(velocity);
        add(speedSlider);
        add(colorKey);
        colorKey.add(path);
        colorKey.add(END);
        colorKey.add(OPENSPACES);
        colorKey.add(CLOSEDSPACES);
        
        //Size and position of components
        colorKey.setBounds(520, 450, 170, 35);
        
       
        ResetButton.setBounds(520, 5, 170, 25);
        stepButton.setBounds(520, 35, 170, 25);
        AutomaticIterationButton.setBounds(520, 65, 170, 25);
        velocity.setBounds(520, 95, 170, 10);
        speedSlider.setBounds(520, 110, 170, 25);
        aStar.setBounds(530, 140, 70, 25);
        dijkstra.setBounds(600, 140, 85, 25);
        
        results.setBounds(525,180,170,25);
        entryText.setBounds(525, 215, 170, 25);
        rowsLbl.setBounds(490, 275, 140, 25);
        rowsField.setBounds(645, 275, 25, 25);
        columnsLbl.setBounds(490, 245, 140, 25);
        columnsField.setBounds(645, 245, 25, 25);
        redrawText.setBounds(530, 305, 170, 25);
        redrawButton.setBounds(520, 335, 170, 25);
        mazeButton.setBounds(520, 365, 170, 25);
        fileButton.setBounds(520, 395, 170, 25);
        
        timer = new Timer(delay, action);
        
        fillboard();

    }
    
    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt){
    	fileInput = true;
    	FileInputToBoardAndBlock.LoadFile();
    }
    
    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) { 
    	fileInput = false;
        initializeboard(false);
    }//resetButtonActionPerformed()

    private void mazeButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	fileInput = false;
        initializeboard(true);
    }//mazeButtonActionPerformed()

    public void initializeboard(Boolean makeGrid) {                                           
        int oldRows = rows;
        int oldColumns = columns;
        try {
            if (!rowsField.getText().isEmpty()){
                rows = Integer.parseInt(rowsField.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Enter a row number between 5 and 100",
                        "Problem", JOptionPane.ERROR_MESSAGE);
                rows = oldRows;
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a row number between 5 and 100 ",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            rows = oldRows;
            return;
        }
        if (rows < 5 || rows > 100) {
            JOptionPane.showMessageDialog(this,
                    "The field \"# of rows\" \naccepts values between 5 and 100",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            rows = oldRows;
            return;
        }
        try {
            if (!columnsField.getText().isEmpty()){
                columns = Integer.parseInt(columnsField.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Enter a col number between 5 and 100",
                        "Problem", JOptionPane.ERROR_MESSAGE);
                columns = oldColumns;
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter a col number between 5 and 100",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            columns = oldColumns;
            return;
        }
        if (columns < 5 || columns > 100) {
            JOptionPane.showMessageDialog(this,
                    "Enter a col number between 5 and 100",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            columns = oldColumns;
            return;
        }
        blockSize = 500/(rows > columns ? rows : columns);
        arrowSize = blockSize/2;
        if (makeGrid && rows % 2 == 0) {
            rows -= 1;
        }
        if (makeGrid && columns % 2 == 0) {
            columns -= 1;
        }
        board = new int[rows][columns];
        pathStart = new GridBlock(rows-2,1);
        ENDPos = new GridBlock(1,columns-2);
        aStar.setEnabled(true);
        dijkstra.setEnabled(true);
        speedSlider.setValue(500);
        if (makeGrid) {
            GridGenerator maze = new GridGenerator(rows/2,columns/2,Driver.globalSp);
        } else {
            fillboard();
        }
    }//initializeboard()
    
    public void initializeboardFile(int rowCount, int columnCount) {                                           
        int oldRows = rows;
        int oldColumns = columns;
        try {
            if (!rowsField.getText().isEmpty()){
                rows = Integer.parseInt(rowsField.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Rows number must be between 5 and 100",
                        "Problem", JOptionPane.ERROR_MESSAGE);
                rows = oldRows;
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
            		"Rows number must be between 5 and 100",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            rows = oldRows;
            return;
        }
        if (rows < 5 || rows > 100) {
            JOptionPane.showMessageDialog(this,
                    "The rows accepts values between 5 and 100",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            rows = oldRows;
            return;
        }
        try {
            if (!columnsField.getText().isEmpty()){
                columns = Integer.parseInt(columnsField.getText());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Col number must be between 5 and 100",
                        "Problem", JOptionPane.ERROR_MESSAGE);
                columns = oldColumns;
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
            		"Col number must be between 5 and 100",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            columns = oldColumns;
            return;
        }
        if (columns < 5 || columns > 100) {
            JOptionPane.showMessageDialog(this,
            		"Col number must be between 5 and 100",
                    "Problem", JOptionPane.ERROR_MESSAGE);
            columns = oldColumns;
            return;
        }
        
        int rows = rowCount;
        int columns = columnCount;
        
        blockSize = 500/(rows > columns ? rows : columns);
        arrowSize = blockSize/2;
        
        board = new int[rows][columns];
        //new start based on grid size
        pathStart = new GridBlock(rows-2,1);
        //new end based on grid size
        ENDPos = new GridBlock(1,columns-2);
        
        //reset values for UI inputs
        aStar.setEnabled(true);
        dijkstra.setEnabled(true);
        speedSlider.setValue(500);
      
           // fillboard();
    }//initializeboard()

    private class GridBlockComparatorByF implements Comparator<GridBlock>{
        @Override
        public int compare(GridBlock GridBlock1, GridBlock GridBlock2){
            return GridBlock1.f-GridBlock2.f;
        }
    }//GridBlockComparatorByF
  
    private class GridBlockComparatorByDist implements Comparator<GridBlock>{
        @Override
        public int compare(GridBlock GridBlock1, GridBlock GridBlock2){
            return GridBlock1.dist-GridBlock2.dist;
        }
    }//GridBlockComparatorByDist
  
    
    private void expandNode(){
        if (dijkstra.isSelected()){
            GridBlock u;
            if (graph.isEmpty()){
                return;
            }
            u = graph.remove(0);
            ClosedSpacesSet.add(u);
            if (u.row == ENDPos.row && u.col == ENDPos.col){
                found = true;
                return;
            }
            board[u.row][u.col] = CLOSEDSPACES;
            if (u.dist == INFINITY){
                return;
            } 
            ArrayList<GridBlock> neighbors = createSuccesors(u, false);
            for (GridBlock v: neighbors){
                int alt = u.dist + distBetween(u,v);
                if (alt < v.dist){
                    v.dist = alt;
                    v.prev = u;
                    board[v.row][v.col] = OPENSPACES;
                    Collections.sort(graph, new GridBlockComparatorByF());
                }
            }
        } else {
            GridBlock current;
                Collections.sort(openSet, new GridBlockComparatorByDist());
                current = openSet.remove(0);
            
            ClosedSpacesSet.add(0,current);
            board[current.row][current.col] = CLOSEDSPACES;
            if (current.row == ENDPos.row && current.col == ENDPos.col) {
                GridBlock last = ENDPos;
                last.prev = current.prev;
                ClosedSpacesSet.add(last);
                found = true;
                return;
            }
            ArrayList<GridBlock> succesors;
            succesors = createSuccesors(current, false);
            for (GridBlock GridBlock: succesors){
                 if (aStar.isSelected()){
                    int dxg = current.col-GridBlock.col;
                    int dyg = current.row-GridBlock.row;
                    int dxh = ENDPos.col-GridBlock.col;
                    int dyh = ENDPos.row-GridBlock.row;
                    
                    GridBlock.g = current.g+Math.abs(dxg)+Math.abs(dyg);
                    GridBlock.h = Math.abs(dxh)+Math.abs(dyh);
                    
                    //Print out the F,G,H values per node searched to the console, as the in-block rendering is rather useless
                    GridBlock.f = GridBlock.g+GridBlock.h;
                    System.out.println("GridBlock " +"(" + current.row + "," + current.col + ")" + " "  );
                    System.out.println("F Value: "  + GridBlock.f);
                    System.out.println("G Value: "  + GridBlock.g);
                    System.out.println("H Value: "  + GridBlock.h);
                    int openIndex   = isInList(openSet,GridBlock);
                    int CLOSEDSPACESIndex = isInList(ClosedSpacesSet,GridBlock);
                    if (openIndex == -1 && CLOSEDSPACESIndex == -1) {
                        openSet.add(GridBlock);
                        board[GridBlock.row][GridBlock.col] = OPENSPACES;
                    } else {
                        if (openIndex > -1){ 
                        	if (openSet.get(openIndex).f <= GridBlock.f) {
                            } else {
                                openSet.remove(openIndex);
                                openSet.add(GridBlock);
                                board[GridBlock.row][GridBlock.col] = OPENSPACES;
                            }
                        } else {
                        	if (ClosedSpacesSet.get(CLOSEDSPACESIndex).f <= GridBlock.f) {
                            } else {
                                ClosedSpacesSet.remove(CLOSEDSPACESIndex);
                                openSet.add(GridBlock);
                                board[GridBlock.row][GridBlock.col] = OPENSPACES;
                            }
                        }
                    }
                }
            }
        }
    }//expandNode()
    
    
    private ArrayList<GridBlock> createSuccesors(GridBlock current, boolean makeConnected){
        int r = current.row;
        int c = current.col;
        ArrayList<GridBlock> temp = new ArrayList<>();
        if (r > 0 && board[r-1][c] != OBST &&
                ((aStar.isSelected() || dijkstra.isSelected()) ? true :
                      isInList(openSet,new GridBlock(r-1,c)) == -1 &&
                      isInList(ClosedSpacesSet,new GridBlock(r-1,c)) == -1)) {
            GridBlock GridBlock = new GridBlock(r-1,c);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(GridBlock);
                } else {
                    int graphIndex = isInList(graph,GridBlock);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                GridBlock.prev = current;
                temp.add(GridBlock);
             }
        }
   
        if (c < columns-1 && board[r][c+1] != OBST &&
                ((aStar.isSelected() || dijkstra.isSelected())? true :
                      isInList(openSet,new GridBlock(r,c+1)) == -1 &&
                      isInList(ClosedSpacesSet,new GridBlock(r,c+1)) == -1)) {
            GridBlock GridBlock = new GridBlock(r,c+1);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(GridBlock);
                } else {
                    int graphIndex = isInList(graph,GridBlock);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                GridBlock.prev = current;
                temp.add(GridBlock);
            }
        }
    
        
        if (r < rows-1 && board[r+1][c] != OBST &&
                ((aStar.isSelected() || dijkstra.isSelected()) ? true :
                      isInList(openSet,new GridBlock(r+1,c)) == -1 &&
                      isInList(ClosedSpacesSet,new GridBlock(r+1,c)) == -1)) {
            GridBlock GridBlock = new GridBlock(r+1,c);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(GridBlock);
                } else {
                    int graphIndex = isInList(graph,GridBlock);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                GridBlock.prev = current;
                temp.add(GridBlock);
            }
        }
     
        if (c > 0 && board[r][c-1] != OBST && 
                ((aStar.isSelected() || dijkstra.isSelected()) ? true :
                      isInList(openSet,new GridBlock(r,c-1)) == -1 &&
                      isInList(ClosedSpacesSet,new GridBlock(r,c-1)) == -1)) {
            GridBlock GridBlock = new GridBlock(r,c-1);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(GridBlock);
                } else {
                    int graphIndex = isInList(graph,GridBlock);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                GridBlock.prev = current;
                temp.add(GridBlock);
            }
        }
      
        
        return temp;
    }//createSuccesors()
    
    private int isInList(ArrayList<GridBlock> list, GridBlock current){
        int index = -1;
        for (int i = 0 ; i < list.size(); i++) {
            if (current.row == list.get(i).row && current.col == list.get(i).col) {
                index = i;
                break;
            }
        }
        return index;
    }//isInList()
    
    private GridBlock findPrev(ArrayList<GridBlock> list, GridBlock current){
        int index = isInList(list, current);
        return list.get(index).prev;
    }//findPrev()
    
    private int distBetween(GridBlock u, GridBlock v){
        int dist;
        int dx = u.col-v.col;
        int dy = u.row-v.row;
            dist = (int)((double)1000*Math.sqrt(dx*dx + dy*dy));
        dist = Math.abs(dx)+Math.abs(dy);
        return dist;
    }//distBetween()
    
    private void plotRoute(){
        searching = false;
        endOfSearch = true;
        int steps = 0;
        double distance = 0;
        int index = isInList(ClosedSpacesSet,ENDPos);
        GridBlock cur = ClosedSpacesSet.get(index);
        board[cur.row][cur.col]= END;
        do {
                ++distance;
                ++steps;
            cur = cur.prev;
            board[cur.row][cur.col] = ROUTE;
        } while (!(cur.row == pathStart.row && cur.col == pathStart.col));
        board[pathStart.row][pathStart.col]=path;
        String msg;
        msg = String.format("Steps: %d, Distance: %.3f", steps,distance); 
       Driver.globalSp.results.setText(msg);
      
    }//plotRoute()
    
    public void fillboard() {
        if (searching || endOfSearch){ 
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (board[r][c] == OPENSPACES || board[r][c] == CLOSEDSPACES || board[r][c] == ROUTE) {
                        board[r][c] = EMPTY;
                    }
                    if (board[r][c] == path){
                        pathStart = new GridBlock(r,c);
                    }
                    if (board[r][c] == END){
                        ENDPos = new GridBlock(r,c);
                    }
                }
            }
            searching = false;
        } else {
        	if(!fileInput){
        		for (int r = 0; r < rows; r++) {
        			for (int c = 0; c < columns; c++) {
        				board[r][c] = EMPTY;
        			}
        		}
            }
            pathStart = new GridBlock(rows-2,1);
            ENDPos = new GridBlock(1,columns-2);
        }
        found = false;
        searching = false;
        endOfSearch = false;
        openSet.removeAll(openSet);
        openSet.add(pathStart);
        ClosedSpacesSet.removeAll(ClosedSpacesSet);
     
        board[ENDPos.row][ENDPos.col] = END; 
        board[pathStart.row][pathStart.col] = path;
        timer.stop();
        repaint();
        
    }//fillboard()
  

    private void findConnectedComponent(GridBlock v){
        Stack<GridBlock> stack;
        stack = new Stack();
        ArrayList<GridBlock> succesors;
        stack.push(v);
        graph.add(v);
        while(!stack.isEmpty()){
            v = stack.pop();
            succesors = createSuccesors(v, true);
            for (GridBlock c: succesors) {
                if (isInList(graph, c) == -1){
                    stack.push(c);
                    graph.add(c);
                }
            }
        }
    }//findConnectedComponent()
    
    private void initializeDijkstra() {
        graph.removeAll(graph);
        findConnectedComponent(pathStart);
        for (GridBlock v: graph) {
            v.dist = INFINITY;
            v.prev = null;
        }
        Collections.sort(graph, new GridBlockComparatorByDist());
        ClosedSpacesSet.removeAll(ClosedSpacesSet);
        
        graph.get(isInList(graph,pathStart)).dist = 0;
    }//initializeDijkstra()

    @Override
    public void paintComponent(Graphics gridBuilder) {

        super.paintComponent(gridBuilder);

        gridBuilder.setColor(Color.DARK_GRAY);
        gridBuilder.fillRect(10, 10, columns*blockSize+1, rows*blockSize+1);

        for (int rowX = 0; rowX < rows; rowX++) {
            for (int colY = 0; colY < columns; colY++) {
                if (board[rowX][colY] == EMPTY) {
                    gridBuilder.setColor(Color.WHITE);
                }
                if (board[rowX][colY] == path) {
                    gridBuilder.setColor(Color.RED);
                }
                if (board[rowX][colY] == END) {
                    gridBuilder.setColor(Color.GREEN);
                }
                if (board[rowX][colY] == OBST) {
                    gridBuilder.setColor(Color.BLACK);
                }
                if (board[rowX][colY] == OPENSPACES) {
                    gridBuilder.setColor(Color.BLUE);
                }
                if (board[rowX][colY] == CLOSEDSPACES) {
                    gridBuilder.setColor(Color.LIGHT_GRAY);
                } 
                if (board[rowX][colY] == ROUTE) {
                    gridBuilder.setColor(Color.YELLOW);
                }
                gridBuilder.fillRect(11 + colY*blockSize, 11 + rowX*blockSize, blockSize - 1, blockSize - 1);
                
            }
        }
       
        
        
            for (int rowX = 0; rowX < rows; rowX++) {
                for (int colY = 0; colY < columns; colY++) {
                    if ((board[rowX][colY] == END && found)  || board[rowX][colY] == ROUTE  || 
                            board[rowX][colY] == OPENSPACES || (board[rowX][colY] == CLOSEDSPACES &&
                            !(rowX == pathStart.row && colY == pathStart.col))){
                        GridBlock head;
                        if (board[rowX][colY] == OPENSPACES){
                            if (dijkstra.isSelected()){
                                head = findPrev(graph,new GridBlock(rowX,colY));
                            } else {
                                head = findPrev(openSet,new GridBlock(rowX,colY));
                            }
                        } else {
                            head = findPrev(ClosedSpacesSet,new GridBlock(rowX,colY));
                        }
                        int tailX = 11+colY*blockSize+blockSize/2;
                        int tailY = 11+rowX*blockSize+blockSize/2;
                        int headX = 11+head.col*blockSize+blockSize/2;
                        int headY = 11+head.row*blockSize+blockSize/2;
                        if (board[rowX][colY] == END  || board[rowX][colY] == ROUTE){
                            gridBuilder.setColor(Color.RED);
                            drawArrow(gridBuilder,tailX,tailY,headX,headY);
                        } else {
                            gridBuilder.setColor(Color.BLACK);
                            drawArrow(gridBuilder,headX,headY,tailX,tailY);
                        }
                    }
                }
            }
       
    }//paintComponent()
    
   
    private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);
        g.drawLine(0, 0, len/2, 0);
        g.drawLine(0, 0, (int)(arrowSize*Math.sin(75*Math.PI/180))/2 , (int)(arrowSize*Math.cos(75*Math.PI/180)));
        g.drawLine(0, 0, (int)(arrowSize*Math.sin(75*Math.PI/180))/2 , -(int)(arrowSize*Math.cos(75*Math.PI/180)));
       
    }//drawArrow()
    
    
    public static class FileInputToBoardAndBlock {
    	
    	private int boardWidth;
    	private int boardHeight;
    	static String mapFromFile;
    	private static int rows;
    	private static int cols;
    	protected static char[] nextInputArray;// = new char[100];
    	protected static String[] blockArrayList = new String[100];
    	
    	public FileInputToBoardAndBlock(){
    		
    		
    	}//Constructor
    	
    	public static void LoadFile(){
    		JOptionPane.showMessageDialog(new JFrame(), "Please Select a .txt file with the appropriate map formatting: \n \n             'e' for empty spaces and 'o' for s. \n");
    		callFileChooser();
    		//constructBoard();
    		
    	}//LoadFile
    	
    	public static void callFileChooser()
    	{

    		JFileChooser chooser = new JFileChooser();
    		FileFilter filter = new FileNameExtensionFilter("TXT File", "txt");
    		chooser.addChoosableFileFilter(filter);

    		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    		int result = chooser.showOpenDialog(Driver.globalSp);

    		if (result == JFileChooser.APPROVE_OPTION)
    		{
    			boolean importSuccess = importData(chooser.getSelectedFile().getAbsolutePath());
    			
    		}

    	}//callFileChooser
    	
    	private static boolean importData(String absolutePath)
    	{
    		int x = 0;
    		int y = 0;
    		char nextInput = ' ';
    		int rowCount = 0;
    		int columnCount = 0;
    		Driver.globalSp.rowsField.setText(Integer.toString(100));
    		Driver.globalSp.columnsField.setText(Integer.toString(100));

    		Driver.globalSp.initializeboard(false);
    		
    		try
    		{
    			//Scanner to input selected file - Returns absolute path
    			Scanner scan = new Scanner(new FileReader(absolutePath));
    			
    			String line;
    			//Java 7 allows a system call to find the specific OS new-line designation(s)
    			String sep = System.getProperty("line.separator");
    			//read every line and add it to the blockArrayList.
    			while (scan.hasNext())
    			
    			{
    				line = scan.nextLine();
    				System.out.println(line);
    				
    				nextInputArray = line.toCharArray();
    				columnCount = nextInputArray.length;
    				for(int i = 0; i < nextInputArray.length; i++){
    					
    					nextInput = nextInputArray[x];
    					
    					if (nextInput == 'o'){ 
    						StarPanel.board[y][x] = OBST;
    					}
    					
    					if (nextInput == 'e'){
    						StarPanel.board[y][x] = EMPTY;
    					}
    					x++;
    				}//Iterate x to call the next String from blockArrayList 
    				rowCount++;
    				x = 0;
    				y++;
    			}
    			
    			constructBoard(rowCount, columnCount);
    			scan.close();
    		}
    		catch (FileNotFoundException e)
    		{
    			JOptionPane.showMessageDialog(new JFrame(), "File Not Found!");
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		catch (IOException e)
    		{
    			JOptionPane.showMessageDialog(new JFrame(), "Your Input is broken... Try reformatting.");
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}

    		return true;
    		
    	}
    	
    	public static void constructBoard(int rowC, int colC){
    		Driver.globalSp.rowsField.setText(Integer.toString(rowC));
    		Driver.globalSp.columnsField.setText(Integer.toString(colC));
    		//Driver.globalSp.initializeboard(false);
    		//Driver.globalSp.fillboard();
    		//Driver.globalSp.initializeboardFile(rowC,colC);
    		Driver.globalSp.repaint();
    	}
    	

    	/**
    	 * @return the boardWidth
    	 */
    	public int getboardWidth() {
    		return boardWidth;
    	}

    	/**
    	 * @param boardWidth the boardWidth to set
    	 */
    	public void setboardWidth(int boardWidth) {
    		this.boardWidth = boardWidth;
    	}

    	/**
    	 * @return the boardHeight
    	 */
    	public int getboardHeight() {
    		return boardHeight;
    	}

    	/**
    	 * @param boardHeight the boardHeight to set
    	 */
    	public void setboardHeight(int boardHeight) {
    		this.boardHeight = boardHeight;
    	}
    	
    	
    	/**
    	 * @return the rows
    	 */
    	public static int getRows() {
    		return rows;
    	}

    	/**
    	 * @param rows the rows to set
    	 */
    	public static void setRows(int rows) {
    		FileInputToBoardAndBlock.rows = rows;
    	}

    	/**
    	 * @return the cols
    	 */
    	public static int getCols() {
    		return cols;
    	}

    	/**
    	 * @param cols the cols to set
    	 */
    	public static void setCols(int cols) {
    		FileInputToBoardAndBlock.cols = cols;
    	}
    	
    }//Class Pathfinder

    
    
    
    
    
    /*
     * 
     * Get And Set Domain
     * There Be Dragons Here
     * 
     */
    
    
    /**
	 * @return the path
	 */
	public static int getPath() {
		return path;
	}

	/**
	 * @return the eNDPos
	 */
	public GridBlock getENDPos() {
		return ENDPos;
	}

	/**
	 * @param eNDPos the eNDPos to set
	 */
	public void setENDPos(GridBlock eNDPos) {
		ENDPos = eNDPos;
	}

	/**
	 * @return the infinity
	 */
	public static int getInfinity() {
		return INFINITY;
	}

	/**
	 * @return the empty
	 */
	public static int getEmpty() {
		return EMPTY;
	}

	/**
	 * @return the obst
	 */
	public static int getObst() {
		return OBST;
	}

	/**
	 * @return the end
	 */
	public static int getEnd() {
		return END;
	}

	/**
	 * @return the openspaces
	 */
	public static int getOpenspaces() {
		return OPENSPACES;
	}

	/**
	 * @return the closedspaces
	 */
	public static int getClosedspaces() {
		return CLOSEDSPACES;
	}

	/**
	 * @return the route
	 */
	public static int getRoute() {
		return ROUTE;
	}
    
}//ClassStarPanel

