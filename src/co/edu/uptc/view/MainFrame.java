package co.edu.uptc.view;

import javax.swing.JFrame;

public class MainFrame extends JFrame {

	public MainFrame() {
		setTitle("Ritmo");
		setSize(600, 600);
		setResizable(false);
		 setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
