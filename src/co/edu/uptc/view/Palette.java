package co.edu.uptc.view;

import java.awt.Color;

/**
 * Colores que comparten todas las pantallas de la aplicación. Si cambias un
 * color aquí, cambia en todas partes.
 * @author programacion 2
 */
public final class Palette {

    public static final Color BACKGROUND = new Color(3, 3, 3);
    public static final Color MENU_BACKGROUND = new Color(9, 9, 9);
    public static final Color WINDOW_BACKGROUND = new Color(18, 18, 18);
    public static final Color CARD = new Color(28, 28, 28);
    public static final Color CARD_BORDER = new Color(48, 48, 48);
    public static final Color FIELD = new Color(38, 38, 38);
    public static final Color FIELD_BORDER = new Color(52, 52, 52);
    public static final Color ACCENT = new Color(255, 193, 7);
    public static final Color TEXT = new Color(235, 235, 235);
    public static final Color TEXT_MUTED = new Color(150, 150, 150);
    public static final Color TEXT_DARK = new Color(20, 20, 20);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static final String FONT_FAMILY = "SansSerif";

    private Palette() {
        // Solo tiene constantes, no se debe instanciar
    }
}   