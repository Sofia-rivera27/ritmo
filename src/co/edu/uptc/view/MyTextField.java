package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JPasswordField;

/**
 * Campo de texto con fondo redondeado y texto de ayuda (placeholder). Si
 * isPassword es true, oculta lo que se escribe.
 */
public class MyTextField extends JPasswordField {

    private String placeholder;
    private Color backgroundColor = Palette.FIELD;
    private Color borderColor = Palette.FIELD_BORDER;
    private Color focusBorderColor = Palette.ACCENT;
    private Color placeholderColor = Palette.TEXT_MUTED;
    private int cornerRadius = 8;

    /**
     * Crea un campo de texto.
     *
     * @param placeholder texto que se ve mientras el campo está vacío
     * @param isPassword  true para ocultar lo que se escribe
     */
    public MyTextField(String placeholder, boolean isPassword) {
        this.placeholder = placeholder;

        if (!isPassword) {
            setEchoChar((char) 0); // 0 = mostrar el texto normal
        }

        setOpaque(false);
        setForeground(Palette.TEXT);
        setCaretColor(Palette.TEXT);
        setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 12));
        setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        // Se vuelve a pintar para que el borde cambie de color al entrar o salir del campo
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    /**
     * Obtiene lo escrito. Sirve para campos normales y de contraseña.
     *
     * @return texto escrito
     */
    public String getValue() {
        return new String(getPassword());
    }

    public void clear() {
        setText("");
    }

    /**
     * Cambia los colores del campo.
     *
     * @param background  color de fondo
     * @param border      color del borde
     * @param focusBorder color del borde mientras el campo está seleccionado
     */
    public void setColors(Color background, Color border, Color focusBorder) {
        this.backgroundColor = background;
        this.borderColor = border;
        this.focusBorderColor = focusBorder;
        repaint();
    }

    /**
     * Cambia los colores del campo según el tema. Este campo pinta su propio
     * fondo, por eso ThemeManager no lo puede cambiar con setBackground.
     *
     * @param light true para el tema claro
     */
    public void applyTheme(boolean light) {
        backgroundColor = swap(backgroundColor, Palette.FIELD, LIGHT_FIELD, light);
        borderColor = swap(borderColor, Palette.FIELD_BORDER, LIGHT_FIELD_BORDER, light);
        placeholderColor = swap(placeholderColor, Palette.TEXT_MUTED, LIGHT_PLACEHOLDER, light);
        setForeground(swap(getForeground(), Palette.TEXT, LIGHT_TEXT, light));
        setCaretColor(light ? LIGHT_TEXT : Palette.TEXT);
        repaint();
    }

    /** Devuelve el color del tema pedido si el actual es uno de los dos conocidos. */
    private Color swap(Color current, Color dark, Color lightColor, boolean light) {
        if (light && dark.equals(current)) {
            return lightColor;
        }
        if (!light && lightColor.equals(current)) {
            return dark;
        }
        return current;
    }

    private static final Color LIGHT_FIELD = new Color(238, 238, 238);
    private static final Color LIGHT_FIELD_BORDER = new Color(210, 210, 210);
    private static final Color LIGHT_PLACEHOLDER = new Color(105, 105, 105);
    private static final Color LIGHT_TEXT = new Color(35, 35, 35);

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    /**
     * Le da al campo un tamaño fijo (preferido, mínimo y máximo).
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int arc = cornerRadius * 2;
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        g2.setColor(hasFocus() ? focusBorderColor : borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        super.paintComponent(g2); // dibuja el texto escrito

        // Placeholder: solo si el campo está vacío
        if (getPassword().length == 0) {
            g2.setColor(placeholderColor);
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }
        g2.dispose();
    }
}