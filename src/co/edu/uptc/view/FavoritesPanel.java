package co.edu.uptc.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FavoritesPanel extends JPanel {
    private JButton btnAddFavorite;
    private JButton btnRemoveFavorite;
    private JButton btnShowFavorites;
    private JLabel lblIndicator;

    public FavoritesPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        initComponents();
    }

    private void initComponents() {
        btnAddFavorite = new JButton("Añadir a favoritos");
        btnRemoveFavorite = new JButton("Quitar de favoritos");
        btnShowFavorites = new JButton("Ver favoritos");

        lblIndicator = new JLabel("No está en favoritos");
        lblIndicator.setFont(new Font("Dialog", Font.BOLD, 14));
        lblIndicator.setForeground(Color.GRAY);

        add(lblIndicator);
        add(btnAddFavorite);
        add(btnRemoveFavorite);
        add(btnShowFavorites);
    }

    public void setFavoriteIndicator(boolean isFavorite) {
        if (isFavorite) {
            lblIndicator.setText("En favoritos");
            lblIndicator.setForeground(new Color(0, 128, 0));
        } else {
            lblIndicator.setText("No está en favoritos");
            lblIndicator.setForeground(Color.GRAY);
        }
    }

    public void addAddFavoriteListener(ActionListener listener) {
        btnAddFavorite.addActionListener(listener);
    }

    public void addRemoveFavoriteListener(ActionListener listener) {
        btnRemoveFavorite.addActionListener(listener);
    }

    public void addShowFavoritesListener(ActionListener listener) {
        btnShowFavorites.addActionListener(listener);
    }
}