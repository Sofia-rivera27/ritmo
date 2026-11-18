package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyButton extends JButton {

    private Color colorFondo;
    private Color colorBorde; // null = sin borde
    private int radio = 15;
    private boolean encima = false;

    public MyButton(){
        
    }

    public MyButton(String texto, Color colorFondo, Color colorTexto, Color colorBorde) {
        super(texto);
        this.colorFondo = colorFondo;
        this.colorBorde = colorBorde;

        setForeground(colorTexto);
        setFont(new Font("SansSerif", Font.PLAIN, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Quitamos el dibujo por defecto de Swing: lo dibujamos nosotros
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setMargin(new Insets(0, 0, 0, 0)); // sin margen interno para que el texto quepa

        // EFECTO DEL MAUSE
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                encima = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = false;
                repaint();
            }
        });
    }

    public void cambiarColores(Color fondo, Color texto, Color borde) {
        this.colorFondo = fondo;
        this.colorBorde = borde;
        setForeground(texto);
        repaint();
    }

    public void cambiarRedondeo(int radio) {
        this.radio = radio;
        repaint();
    }

    public void cambiarFuente(int estilo, int tamano) {
        setFont(new Font("SansSerif", estilo, tamano));
    }

    public void cambiarTamano(int ancho, int alto) {
        Dimension d = new Dimension(ancho, alto);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        revalidate();
    }

    // Color de fondo segun el estado (normal, mouse encima, presionado)
    private Color fondoActual() {
        boolean transparente = colorFondo.getAlpha() == 0;

        if (getModel().isPressed()) {
            if (transparente) {
                return new Color(255, 255, 255, 40);
            } else {
                return mover(colorFondo, -25);
            }
        }

        if (encima) {
            if (transparente) {
                return new Color(255, 255, 255, 20);
            } else {
                return mover(colorFondo, 20);
            }
        }

        return colorFondo;
    }

    // Aclara (cantidad positiva) u oscurece (negativa) un color
    private Color mover(Color c, int cantidad) {
        int r = Math.max(0, Math.min(255, c.getRed() + cantidad));
        int g = Math.max(0, Math.min(255, c.getGreen() + cantidad));
        int b = Math.max(0, Math.min(255, c.getBlue() + cantidad));
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int arco = radio * 2;
        g2.setColor(fondoActual());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);

        if (colorBorde != null) {
            g2.setColor(colorBorde);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        }

        super.paintComponent(g2); // dibuja el texto encima
        g2.dispose();
    }
}
