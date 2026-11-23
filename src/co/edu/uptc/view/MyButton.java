package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/**
 * Botón con esquinas redondeadas y efecto al pasar el mouse y al presionarlo.
 * El fondo lo pinta esta clase en lugar del estilo por defecto de Swing.
 * @author programacion 2
 */
public class MyButton extends JButton {

    private static final int HOVER_LIGHTEN = 20;
    private static final int PRESSED_DARKEN = -25;
    private static final Color HOVER_OVERLAY = new Color(255, 255, 255, 20);
    private static final Color PRESSED_OVERLAY = new Color(255, 255, 255, 40);

    private Color backgroundColor;
    /** Color del borde. null = sin borde. */
    private Color borderColor;
    private int cornerRadius = 15;
    private boolean hovered = false;

    public MyButton() {
        this("", Palette.TRANSPARENT, Palette.TEXT, null);
    }

    /**
     * Crea un botón.
     *
     * @param text            texto del botón
     * @param backgroundColor color de fondo
     * @param textColor       color del texto
     * @param borderColor     color del borde, o null para no tener borde
     */
    public MyButton(String text, Color backgroundColor, Color textColor, Color borderColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;

        setForeground(textColor);
        setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Se quita el dibujo por defecto de Swing: el botón lo dibuja paintComponent
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        // El botón no toma el foco del teclado. Si lo tomara, la barra
        // espaciadora volvería a presionar el último botón usado en vez de
        // pausar o reanudar la música.
        setFocusable(false);
        setOpaque(false);
        setMargin(new Insets(0, 0, 0, 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    /**
     * Cambia los colores del botón.
     *
     * @param background color de fondo
     * @param text       color del texto
     * @param border     color del borde, o null para no tener borde
     */
    public void setColors(Color background, Color text, Color border) {
        this.backgroundColor = background;
        this.borderColor = border;
        setForeground(text);
        repaint();
    }

    /**
     * Redondea las esquinas del botón.
     *
     * @param cornerRadius radio en píxeles
     */
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    /**
     * Cambia el estilo y el tamaño de la fuente.
     *
     * @param style estilo de la fuente, por ejemplo Font.BOLD
     * @param size  tamaño de la fuente en puntos
     */
    public void setFontStyle(int style, int size) {
        setFont(new Font(Palette.FONT_FAMILY, style, size));
    }

    /**
     * Le da al botón un tamaño fijo (preferido, mínimo y máximo).
     *
     * @param width  ancho en píxeles
     * @param height alto en píxeles
     */
    public void setFixedSize(int width, int height) {
        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        revalidate();
    }

    /** Obtiene el color de fondo según el estado (normal, mouse encima, presionado). */
    private Color currentBackground() {
        boolean transparent = backgroundColor.getAlpha() == 0;

        if (getModel().isPressed()) {
            return transparent ? PRESSED_OVERLAY : shiftBrightness(backgroundColor, PRESSED_DARKEN);
        }
        if (hovered) {
            return transparent ? HOVER_OVERLAY : shiftBrightness(backgroundColor, HOVER_LIGHTEN);
        }
        return backgroundColor;
    }

    /** Aclara (cantidad positiva) u oscurece (cantidad negativa) un color. */
    private Color shiftBrightness(Color color, int amount) {
        int red = clamp(color.getRed() + amount);
        int green = clamp(color.getGreen() + amount);
        int blue = clamp(color.getBlue() + amount);
        return new Color(red, green, blue);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int arc = cornerRadius * 2;
        g2.setColor(currentBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }

        super.paintComponent(g2); // dibuja el texto encima
        g2.dispose();
    }
    
    /**
     * Actualiza los colores del botón al cambiar
     * el tema de la aplicación.
     */
    public void applyTheme(boolean light) {

        Color bg = backgroundColor;
        Color fg = getForeground();
        Color border = borderColor;

        if (light) {

            if (bg.equals(new Color(28, 28, 28)))
                bg = Color.WHITE;

            else if (bg.equals(new Color(38, 38, 38)))
                bg = new Color(238, 238, 238);

            else if (bg.equals(new Color(48, 48, 48)))
                bg = new Color(220, 220, 220);

            // Círculos de artistas y álbumes de la pantalla Home
            else if (bg.equals(new Color(41, 41, 41)))
                bg = new Color(230, 230, 230);

        } else {

            if (bg.equals(Color.WHITE))
                bg = new Color(28, 28, 28);

            else if (bg.equals(new Color(238, 238, 238)))
                bg = new Color(38, 38, 38);

            else if (bg.equals(new Color(220, 220, 220)))
                bg = new Color(48, 48, 48);

            else if (bg.equals(new Color(230, 230, 230)))
                bg = new Color(41, 41, 41);
        }

        // El texto claro sobre un fondo transparente o blanco no se vería en el
        // tema claro, así que se oscurece (y al revés al volver al oscuro).
        // Los botones amarillos no cambian porque ya usan texto oscuro.
        boolean lightBackground = bg.getAlpha() == 0 || bg.equals(Color.WHITE)
                || bg.equals(new Color(238, 238, 238)) || bg.equals(new Color(220, 220, 220));
        if (light && lightBackground && fg.equals(LIGHT_THEME_TEXT_SOURCE)) {
            fg = LIGHT_THEME_TEXT;
        } else if (!light && fg.equals(LIGHT_THEME_TEXT)) {
            fg = LIGHT_THEME_TEXT_SOURCE;
        }

        setColors(bg, fg, border);
    }

    /** Color de texto del tema oscuro que se reemplaza en el tema claro. */
    private static final Color LIGHT_THEME_TEXT_SOURCE = new Color(235, 235, 235);
    /** Color de texto que usan los botones en el tema claro. */
    private static final Color LIGHT_THEME_TEXT = new Color(35, 35, 35);
}