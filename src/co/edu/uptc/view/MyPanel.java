package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Panel con esquinas redondeadas y borde de 1 píxel opcionales. El fondo lo
 * pinta esta clase para poder redondear las esquinas.
 */
public class MyPanel extends JPanel {

    /** Radio de las esquinas en píxeles. 0 = esquinas rectas. */
    private int cornerRadius = 0;
    /** Color del borde. null = sin borde. */
    private Color borderColor = null;

    public MyPanel() {
        setPreferredSize(new Dimension(300, 200));
        setBackground(new Color(220, 220, 220));
        setLayout(null);
        setOpaque(false);
    }

    /**
     * Cambia el tamaño preferido del panel.
     *
     * @param width  ancho en píxeles
     * @param height alto en píxeles
     */
    public void setPreferredSize(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        revalidate();
    }

    /**
     * Cambia el color de fondo y vuelve a pintar el panel.
     *
     * @param color nuevo color de fondo
     */
    public void setBackgroundColor(Color color) {
        setBackground(color);
        repaint();
    }

    /**
     * Agrega un componente y vuelve a pintar el panel.
     *
     * @param component componente que se desea agregar
     */
    public void addComponent(Component component) {
        add(component);
        repaint();
    }

    /**
     * Cambia el layout y recalcula la posición de los componentes.
     *
     * @param layout nuevo layout
     */
    public void applyLayout(LayoutManager layout) {
        setLayout(layout);
        revalidate();
    }

    /**
     * Redondea las esquinas del panel.
     *
     * @param cornerRadius radio en píxeles, 0 para esquinas rectas
     */
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    /**
     * Dibuja un borde de 1 píxel alrededor del panel.
     *
     * @param borderColor color del borde, o null para quitarlo
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = cornerRadius * 2;
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
    }
}