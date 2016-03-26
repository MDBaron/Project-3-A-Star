package astar;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class Driver {

	 public static JFrame projectFrame;
	public static StarPanel globalSp; 
	 
	    
	    //Driver for board to run instance of GUI in it's own thread
	    public static void main(String[] args) {
	    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				    public void run() {
				
	        int width  = 720;
	        int height = 512;
	        projectFrame = new JFrame("AStar and Friends");
	        projectFrame.setContentPane(new StarPanel(width,height));
	        projectFrame.pack();
	        projectFrame.setResizable(false);

	        // the form is located in the center of the screen
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        double screenWidth = screenSize.getWidth();
	        double ScreenHeight = screenSize.getHeight();
	        int x = ((int)screenWidth-width)/2;
	        int y = ((int)ScreenHeight-height)/2;

	        projectFrame.setLocation(x,y);
	        projectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        projectFrame.setVisible(true);
				}//run
			}); // while
	    } // end main()
}
