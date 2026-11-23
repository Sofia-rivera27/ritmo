package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * Barra deslizante con el estilo de la aplicación: una línea redondeada, la
 * parte recorrida en amarillo y un círculo pequeño para arrastrar.
 * <p>
 * Al hacer clic en cualquier punto de la barra salta directo a ese punto (el
 * JSlider normal solo avanza de a poquitos). Mientras el usuario arrastra,
 * {@link #getValueIsAdjusting()} es true; al soltar pasa a false.
 * @author programacion 2
 */
public class MySlider extends JSlider {

    private static final int TRACK_HEIGHT = 4;
    private static final int THUMB_SIZE = 12;

    private static final Color DARK_TRACK = new Color(70, 70, 70);
    private static final Color LIGHT_TRACK = new Color(210, 210, 210);

    /**
     * Crea la barra.
     *
     * @param min   valor mínimo
     * @param max   valor máximo
     * @param value valor inicial
     */
    public MySlider(int min, int max, int value) {
        super(min, max, value);
        setOpaque(false);
        // No toma el foco del teclado, para no quitarle la barra espaciadora
        // ni las flechas a los atajos de reproducción.
        setFocusable(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void updateUI() {
        // Usa el dibujo propio en vez del aspecto por defecto de Swing.
        // JSlider llama a este método al crearse y si cambia el aspecto.
        setUI(new FlatSliderUI(this));
    }

    /**
     * Dibujo de la barra y comportamiento del mouse.
     */
    private static class FlatSliderUI extends BasicSliderUI {

        FlatSliderUI(JSlider slider) {
            super(slider);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(THUMB_SIZE, THUMB_SIZE);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle track = trackRect;
            int y = track.y + (track.height - TRACK_HEIGHT) / 2;

            // Fondo de la barra, según el tema activo
            g2.setColor(ThemeManager.isLightTheme() ? LIGHT_TRACK : DARK_TRACK);
            g2.fillRoundRect(track.x, y, track.width, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);

            // Parte recorrida, en amarillo
            int filled = thumbRect.x + thumbRect.width / 2 - track.x;
            if (slider.isEnabled() && filled > 0) {
                g2.setColor(Palette.ACCENT);
                g2.fillRoundRect(track.x, y, filled, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT);
            }
            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            if (!slider.isEnabled()) {
                return; // sin canción no hay nada que arrastrar
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Palette.ACCENT);
            g2.fillOval(thumbRect.x, thumbRect.y + (thumbRect.height - THUMB_SIZE) / 2, THUMB_SIZE, THUMB_SIZE);
            g2.dispose();
        }

        @Override
        public void paintFocus(Graphics g) {
            // Sin recuadro de foco
        }

        @Override
        protected TrackListener createTrackListener(JSlider slider) {
            return new TrackListener() {
                @Override
                public void mousePressed(MouseEvent event) {
                    if (!slider.isEnabled()) {
                        return;
                    }
                    slider.setValueIsAdjusting(true);
                    moveTo(event);
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    if (slider.getValueIsAdjusting()) {
                        moveTo(event);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (slider.getValueIsAdjusting()) {
                        moveTo(event);
                        slider.setValueIsAdjusting(false);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent event) {
                    // Nada: solo importa cuando se presiona el mouse
                }

                private void moveTo(MouseEvent event) {
                    slider.setValue(valueForXPosition(event.getX()));
                }
            };
        }
    }
}