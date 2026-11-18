package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {

    private int radio = 0;              // 0 = esquinas rectas
    private Color colorBorde = null;    // null = sin borde

    public MyPanel() {
        setPreferredSize(new Dimension(300, 200));
        setBackground(new Color(220, 220, 220));
        setLayout(null);
        setOpaque(false); // el fondo lo pintamos nosotros (para poder redondear)
    }

    // Cambiar tamaño
    public void cambiarTamaño(int ancho, int alto) {
        setPreferredSize(new Dimension(ancho, alto));
        revalidate();
    }

    // Cambiar color
    public void cambiarColor(Color color) {
        setBackground(color);
        repaint();
    }

    // Cambiar posición y tamaño
    public void cambiarPosicion(int x, int y, int ancho, int alto) {
        setBounds(x, y, ancho, alto);
    }

    // Agregar componentes
    public void agregarComponente(Component componente) {
        add(componente);
        repaint();
    }

    // Cambiar diseño
    public void cambiarLayout(LayoutManager layout) {
        setLayout(layout);
        revalidate();
    }

    // NUEVO: esquinas redondeadas (radio en pixeles)
    public void cambiarRedondeo(int radio) {
        this.radio = radio;
        repaint();
    }

    // NUEVO: borde de 1 pixel (null para quitarlo)
    public void cambiarBorde(Color color) {
        this.colorBorde = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arco = radio * 2;
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);

        if (colorBorde != null) {
            g2.setColor(colorBorde);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        }
        g2.dispose();
    }
}
/*
    private static final Color TARJETA = new Color(28, 28, 28);
    private static final Color BORDE_TARJETA = new Color(48, 48, 48);
    private static final Color CAMPO = new Color(38, 38, 38);
    private static final Color AMARILLO = new Color(255, 193, 7);
    private static final Color TEXTO = new Color(235, 235, 235);
    private static final Color TEXTO_SUAVE = new Color(150, 150, 150);
    private static final Color TEXTO_OSCURO = new Color(20, 20, 20);
    private static final Color TRANSPARENTE = new Color(0, 0, 0, 0);
    private static final int ANCHO = 274;


    private MyPanel options;
    private MyPanel footer;

    private MyButton Home;
    private MyButton Explorar;
    private MyButton Biblioteca;
    private MyButton Playlists;
    private MyButton Historial;
    private MyButton Configuracion;

    private MyButton pausa;
    private MyButton avanzar;
    private MyButton retroceder;

    private JFrame frameProvisional;


*/