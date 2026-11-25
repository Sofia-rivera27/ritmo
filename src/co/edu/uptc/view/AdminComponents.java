package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Piezas que comparten las pantallas del administrador (etiquetas, botones,
 * tarjetas, filas de tablas...). Así cada pantalla no repite el mismo código.
 * @author programacion 2
 */
public final class AdminComponents {

    public static final Color ROW_DARK = new Color(22, 22, 22);
    public static final Color ICON_BOX = new Color(40, 40, 40);
    public static final Color WARNING_BACKGROUND = new Color(60, 50, 0);
    public static final Color ERROR_BACKGROUND = new Color(60, 22, 24);
    public static final Color ERROR_BORDER = new Color(200, 90, 90);
    public static final Color ERROR_TEXT = new Color(255, 170, 170);
    public static final String[] GENRE_COLORS = { "Amarillo", "Gris", "Blanco", "Naranja", "Azul", "Verde" };

    /** Color elegido por el administrador para cada género (nombre en minúsculas -> color). */
    private static final Map<String, String> genreColors = new HashMap<>();

    private AdminComponents() {
    }

    /** El Presenter la llama cada vez que se cargan o cambian los géneros. */
    public static void setGenreColor(String genre, String colorName) {
        genreColors.put(genre.toLowerCase(), colorName);
    }

    public static void clearGenreColors() {
        genreColors.clear();
    }

    /** Color con el que se pinta la etiqueta de un género en cualquier pantalla. */
    public static Color genreColor(String genre) {
        String colorName = genre == null ? null : genreColors.get(genre.toLowerCase());
        return colorFor(colorName);
    }

    public static JLabel label(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    /** Botón pequeño con borde, como "Editar" o "Eliminar". */
    public static MyButton smallButton(String text, int width) {
        MyButton button = new MyButton(text, Palette.TRANSPARENT, Palette.TEXT, Palette.TEXT_MUTED);
        button.setFixedSize(width, 22);
        button.setCornerRadius(11);
        button.setFontStyle(Font.PLAIN, 11);
        return button;
    }

    /** Botón amarillo, como "+ Nuevo usuario" o "Guardar". */
    public static MyButton accentButton(String text, int width) {
        MyButton button = new MyButton(text, Palette.ACCENT, Palette.TEXT_DARK, null);
        button.setFixedSize(width, 24);
        button.setCornerRadius(12);
        button.setFontStyle(Font.BOLD, 11);
        return button;
    }

    /** Panel sin fondo propio (usa el color de fondo de la ventana). */
    public static MyPanel transparentPanel() {
        MyPanel panel = new MyPanel();
        panel.setBackgroundColor(Palette.WINDOW_BACKGROUND);
        panel.setPreferredSize(null);
        return panel;
    }

    /** Tarjeta gris con esquinas redondeadas y borde. */
    public static MyPanel card() {
        MyPanel card = new MyPanel();
        card.setBackgroundColor(Palette.CARD);
        card.setBorderColor(Palette.CARD_BORDER);
        card.setCornerRadius(12);
        card.setPreferredSize(null);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return card;
    }

    public static MyTextField field(String placeholder, boolean password) {
        MyTextField field = new MyTextField(placeholder, password);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setPreferredSize(new Dimension(260, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    public static JComboBox<String> comboBox(String[] options) {
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setBackground(Palette.FIELD);
        combo.setForeground(Palette.TEXT);
        combo.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 12));
        combo.setFocusable(false);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setPreferredSize(new Dimension(260, 36));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return combo;
    }

    /**
     * Caja de mensaje (aviso amarillo o error rojo). Empieza oculta.
     *
     * @param error true para el estilo de error
     */
    public static MyPanel messageBox(JLabel text, boolean error) {
        MyPanel box = new MyPanel();
        box.applyLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 8));
        box.setCornerRadius(8);
        box.setBackgroundColor(error ? ERROR_BACKGROUND : WARNING_BACKGROUND);
        box.setBorderColor(error ? ERROR_BORDER : Palette.ACCENT);
        box.setPreferredSize(null);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.setForeground(error ? ERROR_TEXT : Palette.TEXT);
        text.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));
        box.add(text);
        box.setVisible(false);
        return box;
    }

    /**
     * Fila de una tabla: cada celda tiene un ancho fijo para que las columnas
     * queden alineadas con el encabezado.
     */
    public static MyPanel tableRow(Color background, int height, int[] widths, Component... cells) {
        MyPanel row = new MyPanel();
        row.applyLayout(new GridBagLayout());
        row.setBackgroundColor(background);
        row.setCornerRadius(6);
        row.setPreferredSize(new Dimension(0, height));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < cells.length; i++) {
            MyPanel cell = transparentPanel();
            cell.setBackgroundColor(Palette.TRANSPARENT);
            cell.applyLayout(new GridBagLayout());
            cell.setPreferredSize(new Dimension(widths[i], height - 4));
            GridBagConstraints inner = new GridBagConstraints();
            inner.anchor = GridBagConstraints.WEST;
            inner.weightx = 1;
            cell.add(cells[i], inner);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = i;
            constraints.insets = new Insets(0, i == 0 ? 10 : 0, 0, 0);
            row.add(cell, constraints);
        }
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = cells.length;
        filler.weightx = 1;
        row.add(transparentPanelOf(Palette.TRANSPARENT), filler);
        return row;
    }

    /** Encabezado de una tabla con los nombres de las columnas. */
    public static MyPanel tableHeader(String[] titles, int[] widths) {
        Component[] cells = new Component[titles.length];
        for (int i = 0; i < titles.length; i++) {
            cells[i] = label(titles[i], Palette.TEXT, Font.PLAIN, 12);
        }
        return tableRow(Palette.CARD, 26, widths, cells);
    }

    /** Dos botones uno encima del otro (Editar / Eliminar). */
    public static MyPanel stackedButtons(MyButton top, MyButton bottom) {
        MyPanel box = transparentPanelOf(Palette.TRANSPARENT);
        box.applyLayout(new javax.swing.BoxLayout(box, javax.swing.BoxLayout.Y_AXIS));
        box.add(top);
        box.add(javax.swing.Box.createVerticalStrut(2));
        box.add(bottom);
        return box;
    }

    /** Etiqueta redondeada para el género, con el color del género. */
    public static MyPanel genreTag(String genre, String colorName) {
        Color color = colorFor(colorName);
        MyPanel tag = new MyPanel();
        tag.applyLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 2));
        tag.setBackgroundColor(Palette.TRANSPARENT);
        tag.setCornerRadius(9);
        tag.setBorderColor(color);
        tag.setPreferredSize(null);
        tag.add(label(genre, color, Font.PLAIN, 10));
        return tag;
    }

    /** Pastilla de color que se ve en la tabla de géneros. */
    public static MyPanel colorPill(String colorName) {
        MyPanel pill = new MyPanel();
        pill.setBackgroundColor(colorFor(colorName));
        pill.setCornerRadius(8);
        pill.setPreferredSize(38, 16);
        return pill;
    }

    /** Convierte el nombre de un color del género en el color que se pinta. */
    public static Color colorFor(String colorName) {
        if (colorName == null) {
            return Palette.ACCENT;
        }
        switch (colorName) {
            case "Gris":
                return new Color(130, 130, 130);
            case "Blanco":
                return Palette.TEXT;
            case "Naranja":
                return new Color(255, 140, 50);
            case "Azul":
                return new Color(90, 160, 255);
            case "Verde":
                return new Color(90, 200, 120);
            default:
                return Palette.ACCENT;
        }
    }

    /**
     * Cuadro con la portada (o una nota musical si no hay imagen).
     *
     * @param image imagen que se muestra, o null
     * @param size  lado del cuadro en píxeles
     */
    public static MyPanel coverBox(File image, int size) {
        MyPanel box = new MyPanel();
        box.applyLayout(new GridBagLayout());
        box.setBackgroundColor(ICON_BOX);
        box.setCornerRadius(8);
        box.setPreferredSize(size, size);
        box.setMaximumSize(new Dimension(size, size));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (image != null && image.isFile()) {
            Image scaled = new ImageIcon(image.getAbsolutePath()).getImage()
                    .getScaledInstance(size - 4, size - 4, Image.SCALE_SMOOTH);
            box.add(new JLabel(new ImageIcon(scaled)));
        } else {
            box.add(label("♫", Palette.ACCENT, Font.BOLD, size / 3));
        }
        return box;
    }

    /** Barra de desplazamiento sin borde para las listas largas. */
    public static JScrollPane scroll(MyPanel list) {
        MyPanel wrapper = transparentPanel();
        wrapper.applyLayout(new java.awt.BorderLayout());
        wrapper.add(list, java.awt.BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(Palette.WINDOW_BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private static MyPanel transparentPanelOf(Color color) {
        MyPanel panel = new MyPanel();
        panel.setBackgroundColor(color);
        panel.setPreferredSize(null);
        return panel;
    }
}