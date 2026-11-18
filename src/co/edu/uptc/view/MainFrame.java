package co.edu.uptc.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class MainFrame extends JFrame {
	private FavoritesPanel favoritesPanel;

	public MainFrame() {
		setTitle("Ritmo");
		setSize(600, 600);
		setResizable(false);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		favoritesPanel = new FavoritesPanel();
		add(favoritesPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	public FavoritesPanel getFavoritesPanel() {
		return favoritesPanel;
	}
}