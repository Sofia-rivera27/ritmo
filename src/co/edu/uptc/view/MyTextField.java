package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Campo de texto personalizado: fondo redondeado y texto de ayuda (placeholder).
 * Si esClave es true oculta lo que se escribe (como una contraseña).
 */
public class MyTextField extends JPasswordField {

    private String placeholder;
    private Color colorFondo = new Color(38, 38, 38);
    private Color colorBorde = new Color(52, 52, 52);
    private Color colorBordeFoco = new Color(255, 193, 7);
    private Color colorPlaceholder = new Color(150, 150, 150);
    private int radio = 8;

    public MyTextField(String placeholder, boolean esClave) {
        this.placeholder = placeholder;

        if (!esClave) {
            setEchoChar((char) 0); // 0 = mostrar el texto normal
        }

        setOpaque(false);
        setForeground(new Color(235, 235, 235));
        setCaretColor(new Color(235, 235, 235));
        setFont(new Font("SansSerif", Font.PLAIN, 12));
        setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12)); // margen interno

        // Para que el borde cambie de color al entrar/salir del campo
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

    // Devuelve lo escrito (sirve para campo normal y de clave)
    public String getTexto() {
        return new String(getPassword());
    }

    public void limpiar() {
        setText("");
    }

    public void cambiarColores(Color fondo, Color borde, Color bordeFoco) {
        this.colorFondo = fondo;
        this.colorBorde = borde;
        this.colorBordeFoco = bordeFoco;
        repaint();
    }

    public void cambiarRedondeo(int radio) {
        this.radio = radio;
        repaint();
    }

    public void cambiarTamano(int ancho, int alto) {
        Dimension d = new Dimension(ancho, alto);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int arco = radio * 2;
        g2.setColor(colorFondo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);

        if (hasFocus()) {
            g2.setColor(colorBordeFoco);
        } else {
            g2.setColor(colorBorde);
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);

        super.paintComponent(g2); // dibuja el texto escrito

        // Placeholder: solo si el campo esta vacio
        if (getPassword().length == 0) {
            g2.setColor(colorPlaceholder);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, getInsets().left, y);
        }
        g2.dispose();
    }
}
