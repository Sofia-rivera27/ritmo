package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * Sección Historial. Muestra las canciones reproducidas, de la más reciente a
 * la más antigua.
 * <p>
 * Solo vista: avisa las acciones del usuario mediante listeners, pero no tiene
 * la lógica para reproducir canciones ni para borrar el historial.
 * @author programacion 2
 */
public class HistoryPanel extends MyPanel {

    private static final Color ICON_BOX = new Color(40, 40, 40);

    private static final String TITLE_PREFIX = "Historial · ";
    private static final int ROW_HEIGHT = 46;
    private static final int SCROLL_SPEED = 16;

    // Posición de cada dato dentro del arreglo de la fila
    private static final int TITLE = 0;
    private static final int ARTIST = 1;
    private static final int ALBUM = 2;
    private static final int PLAYED_AT = 3;

    private JLabel titleLabel;
    private MyButton clearButton;
    private MyPanel entryList;
    private JLabel emptyLabel;

    private final List<MyButton> playButtons = new ArrayList<>();
    private ActionListener playListener;

    /**
     * Construye el panel del historial, vacío al principio.
     */
    public HistoryPanel() {
        setBackgroundColor(Palette.BACKGROUND);
        applyLayout(new BorderLayout());
        setPreferredSize(null);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        showHistory(new ArrayList<>());
    }

    /**
     * Crea el encabezado con el título, el subtítulo y el botón para borrar.
     *
     * @return panel del encabezado
     */
    private MyPanel createHeader() {
        MyPanel header = new MyPanel();
        header.applyLayout(new BorderLayout());
        header.setBackgroundColor(Palette.BACKGROUND);
        header.setPreferredSize(new Dimension(0, 60));

        titleLabel = createLabel(TITLE_PREFIX + 0, Palette.TEXT, Font.BOLD, 20);

        JLabel subtitle = createLabel("Las canciones que has escuchado, de la más reciente a la más antigua.",
                Palette.TEXT_MUTED, Font.PLAIN, 11);

        MyPanel texts = new MyPanel();
        texts.applyLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setBackgroundColor(Palette.BACKGROUND);
        texts.setPreferredSize(null);
        texts.add(titleLabel);
        texts.add(Box.createVerticalStrut(4));
        texts.add(subtitle);

        clearButton = new MyButton("Borrar historial", Palette.TRANSPARENT, Palette.TEXT, Palette.TEXT_MUTED);
        clearButton.setFixedSize(130, 30);
        clearButton.setCornerRadius(15);
        clearButton.setFontStyle(Font.PLAIN, 11);

        // Centra el botón verticalmente respecto al título
        MyPanel buttonBox = new MyPanel();
        buttonBox.applyLayout(new GridBagLayout());
        buttonBox.setBackgroundColor(Palette.BACKGROUND);
        buttonBox.setPreferredSize(null);
        buttonBox.add(clearButton);

        header.add(texts, BorderLayout.CENTER);
        header.add(buttonBox, BorderLayout.EAST);
        return header;
    }

    /**
     * Crea el cuerpo con la lista de reproducciones dentro de un scroll, porque
     * el historial puede tener muchas canciones.
     *
     * @return panel con la lista del historial
     */
    private Component createBody() {
        entryList = new MyPanel();
        entryList.applyLayout(new BoxLayout(entryList, BoxLayout.Y_AXIS));
        entryList.setBackgroundColor(Palette.BACKGROUND);
        entryList.setPreferredSize(null);

        // Contenedor que mantiene la lista pegada arriba aunque tenga pocas filas
        MyPanel wrapper = new MyPanel();
        wrapper.applyLayout(new BorderLayout());
        wrapper.setBackgroundColor(Palette.BACKGROUND);
        wrapper.setPreferredSize(null);
        wrapper.add(entryList, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(Palette.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        emptyLabel = createLabel("Todavía no has reproducido canciones. Ve a Explorar y dale play a alguna.",
                Palette.TEXT_MUTED, Font.PLAIN, 12);
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return scroll;
    }

    /**
     * Construye la fila de una reproducción.
     *
     * @param entry datos de la fila, en orden: título, artista, álbum y hora
     * @param index posición en la lista; se usa para mostrar el número y como
     *              comando de acción del botón reproducir
     * @return panel que representa la reproducción
     */
    private MyPanel createRow(String[] entry, int index) {
        MyPanel row = new MyPanel();
        row.applyLayout(new GridBagLayout());
        row.setBackgroundColor(Palette.CARD);
        row.setCornerRadius(10);
        row.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 1. Número
        JLabel number = createLabel(String.valueOf(index + 1), Palette.TEXT_MUTED, Font.PLAIN, 11);
        number.setPreferredSize(new Dimension(24, 20));
        addToRow(row, number, 0, 0);

        // 2. Icono
        addToRow(row, createIcon(), 1, 0);

        // 3. Título, artista y álbum (se reparten el espacio sobrante)
        addToRow(row, createLabel(entry[TITLE], Palette.TEXT, Font.BOLD, 13), 2, 3);
        addToRow(row, createLabel(entry[ARTIST], Palette.TEXT_MUTED, Font.PLAIN, 12), 3, 2);
        addToRow(row, createLabel(entry[ALBUM], Palette.TEXT_MUTED, Font.PLAIN, 12), 4, 2);

        // 4. Hora de reproducción
        addToRow(row, createLabel(entry[PLAYED_AT], Palette.TEXT_MUTED, Font.PLAIN, 11), 5, 0);

        // 5. Botón reproducir
        MyButton playButton = new MyButton("▶ Reproducir", Palette.TRANSPARENT, Palette.ACCENT, Palette.ACCENT);
        playButton.setFixedSize(110, 28);
        playButton.setCornerRadius(14);
        playButton.setFontStyle(Font.PLAIN, 11);
        playButton.setActionCommand(String.valueOf(index)); // así el Presenter sabe cuál fila fue
        playButtons.add(playButton);
        addToRow(row, playButton, 6, 0);

        return row;
    }

    /**
     * Agrega un componente a una fila en la columna indicada.
     *
     * @param row       panel al que se agrega el componente
     * @param component componente que se desea agregar
     * @param column    posición horizontal del componente
     * @param weight    parte del espacio horizontal que puede ocupar; si es
     *                  mayor que cero, el componente se estira a lo ancho
     */
    private void addToRow(MyPanel row, Component component, int column, double weight) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = 0;
        constraints.weightx = weight;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = weight > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 8, 0, 8);
        row.add(component, constraints);
    }

    private JLabel createLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    /**
     * Crea el recuadro decorativo con el símbolo de historial.
     *
     * @return panel que representa el icono de la fila
     */
    private MyPanel createIcon() {
        MyPanel icon = new MyPanel();
        icon.applyLayout(new GridBagLayout()); // centra el símbolo
        icon.setBackgroundColor(ICON_BOX);
        icon.setCornerRadius(8);
        icon.setPreferredSize(new Dimension(34, 34));
        icon.add(createLabel("↺", Palette.ACCENT, Font.BOLD, 14));
        return icon;
    }

    /**
     * Actualiza la lista con las reproducciones recibidas.
     * <p>
     * Borra las filas anteriores, crea una fila por reproducción, actualiza el
     * contador y le pone el listener de reproducir a los botones nuevos. Si la
     * lista está vacía, muestra un mensaje y desactiva el botón de borrar.
     *
     * @param entries lista de reproducciones; cada elemento es un arreglo con
     *                título, artista, álbum y hora de reproducción
     */
    public void showHistory(List<String[]> entries) {
        entryList.removeAll();
        playButtons.clear();

        if (entries.isEmpty()) {
            entryList.add(emptyLabel);
        }
        for (int i = 0; i < entries.size(); i++) {
            entryList.add(createRow(entries.get(i), i));
            entryList.add(Box.createVerticalStrut(6));
        }

        titleLabel.setText(TITLE_PREFIX + entries.size());
        clearButton.setEnabled(!entries.isEmpty());

        // Los botones nuevos también deben escuchar
        if (playListener != null) {
            for (MyButton button : playButtons) {
                button.addActionListener(playListener);
            }
        }

        entryList.revalidate();
        entryList.repaint();
    }

    /**
     * Registra el listener de los botones "Reproducir". El comando de acción de
     * cada botón es el índice de su fila (0, 1, 2...).
     *
     * @param listener listener que atiende los eventos de reproducir; también
     *                 se agrega a los botones que ya existen
     */
    public void addPlayListener(ActionListener listener) {
        playListener = listener;
        for (MyButton button : playButtons) {
            button.addActionListener(listener);
        }
    }

    /**
     * Registra el listener del botón "Borrar historial".
     *
     * @param listener listener que atiende el evento
     */
    public void addClearListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }
}