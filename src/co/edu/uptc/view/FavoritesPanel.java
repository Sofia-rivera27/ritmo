package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Sección de canciones favoritas.
 * <p>
 * Muestra la información y avisa las acciones del usuario mediante listeners,
 * pero no tiene la lógica para eliminar ni reproducir canciones.
 *  @author programacion 2
 * 
 */
public class FavoritesPanel extends MyPanel {

    private static final Color ICON_BOX = new Color(40, 40, 40);

    private static final String SEARCH_PLACEHOLDER = "Buscar por artista, género o canción...";
    private static final String TITLE_PREFIX = "Favoritos · ";
    private static final int ROW_HEIGHT = 46;

    // Posición de cada dato dentro del arreglo de la canción
    private static final int TITLE = 0;
    private static final int ARTIST = 1;
    private static final int ALBUM = 2;
    private static final int GENRE = 3;
    private static final int DURATION = 4;

    private JTextField searchField;
    private JLabel titleLabel;
    private MyButton playAllButton;
    private MyPanel songList;

    private final List<MyButton> removeButtons = new ArrayList<>();
    /** Círculo con la inicial del usuario que inició sesión. */
    private MyButton avatar;
    private ActionListener removeListener;
    private ActionListener searchListener;
    /** true mientras el buscador muestra el texto de ayuda y no algo escrito. */
    private boolean showingPlaceholder = true;

    /**
     * Construye el panel de favoritos: barra superior y cuerpo. Las canciones
     * las pone el Presenter con showFavorites.
     */
    public FavoritesPanel() {
        setBackgroundColor(Palette.BACKGROUND);
        applyLayout(new BorderLayout());
        setPreferredSize(null);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        add(createTopBar(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

    }

    /**
     * Crea la barra superior con el campo de búsqueda y el avatar.
     *
     * @return panel con los componentes de la barra superior
     */
    private MyPanel createTopBar() {
        MyPanel bar = new MyPanel();
        bar.applyLayout(new BorderLayout(12, 0));
        bar.setBackgroundColor(Palette.BACKGROUND);
        bar.setPreferredSize(new Dimension(0, 62));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        MyPanel searchBox = new MyPanel();
        searchBox.applyLayout(new BorderLayout());
        searchBox.setBackgroundColor(Palette.CARD);
        searchBox.setCornerRadius(22);
        searchBox.setBorderColor(Palette.CARD_BORDER);
        searchBox.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        searchField = new JTextField(SEARCH_PLACEHOLDER);
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setForeground(Palette.TEXT_MUTED);
        searchField.setCaretColor(Palette.TEXT);
        searchField.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 13));
        // Comportamiento del texto de ayuda (placeholder)
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder) {
                    showingPlaceholder = false;
                    searchField.setText("");
                    searchField.setForeground(Palette.TEXT);
                    ThemeManager.refresh(searchField);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    showingPlaceholder = true;
                    searchField.setText(SEARCH_PLACEHOLDER);
                    searchField.setForeground(Palette.TEXT_MUTED);
                    ThemeManager.refresh(searchField);
                }
            }
        });
        // Filtra mientras se escribe, sin tener que presionar Enter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchChanged();
            }
        });
        searchBox.add(searchField, BorderLayout.CENTER);

        avatar = new MyButton(" ", Palette.ACCENT, Palette.TEXT_DARK, null);
        avatar.setFixedSize(40, 40);
        avatar.setCornerRadius(20); // la mitad del tamaño forma un círculo
        avatar.setFontStyle(Font.BOLD, 14);

        bar.add(searchBox, BorderLayout.CENTER);
        bar.add(avatar, BorderLayout.EAST);
        return bar;
    }

    /**
     * Construye el contenido principal: encabezado, subtítulo, lista de canciones
     * y tarjeta inferior, en una columna vertical.
     *
     * @return panel con el cuerpo de la vista
     */
    private MyPanel createBody() {
        MyPanel body = new MyPanel();
        body.applyLayout(new BorderLayout());
        body.setBackgroundColor(Palette.BACKGROUND);
        body.setPreferredSize(null);

        // Columna vertical pegada arriba
        MyPanel column = new MyPanel();
        column.applyLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBackgroundColor(Palette.BACKGROUND);
        column.setPreferredSize(null);

        column.add(createHeader());

        JLabel subtitle = new JLabel(
                "Solo las canciones marcadas con ♥. Quitarlas aquí no las borra de la biblioteca.");
        subtitle.setForeground(Palette.TEXT_MUTED);
        subtitle.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 14, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(subtitle);

        songList = new MyPanel();
        songList.applyLayout(new BoxLayout(songList, BoxLayout.Y_AXIS));
        songList.setBackgroundColor(Palette.BACKGROUND);
        songList.setPreferredSize(null);
        songList.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(songList);

        column.add(Box.createVerticalStrut(10));
        column.add(createBottomCard());

        body.add(column, BorderLayout.NORTH);
        return body;
    }

    /**
     * Crea el encabezado con el título de favoritos y el botón para reproducir
     * todo.
     *
     * @return panel del encabezado
     */
    private MyPanel createHeader() {
        MyPanel header = new MyPanel();
        header.applyLayout(new BorderLayout());
        header.setBackgroundColor(Palette.BACKGROUND);
        header.setPreferredSize(new Dimension(0, 34));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleLabel = new JLabel(TITLE_PREFIX + 0);
        titleLabel.setForeground(Palette.TEXT);
        titleLabel.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 20));

        playAllButton = new MyButton("Reproducir todo", Palette.ACCENT, Palette.TEXT_DARK, null);
        playAllButton.setFixedSize(130, 30);
        playAllButton.setCornerRadius(15);
        playAllButton.setFontStyle(Font.BOLD, 11);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(playAllButton, BorderLayout.EAST);
        return header;
    }

    /**
     * Crea la tarjeta informativa que va debajo de la lista de canciones.
     *
     * @return panel con el mensaje para agregar canciones a favoritos
     */
    private MyPanel createBottomCard() {
        MyPanel card = new MyPanel();
        card.applyLayout(new BorderLayout());
        card.setBackgroundColor(Palette.CARD);
        card.setCornerRadius(10);
        card.setBorderColor(Palette.CARD_BORDER);
        card.setPreferredSize(new Dimension(0, 46));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel message = new JLabel("Para agregar más, ve a Biblioteca y toca ♡ en la canción.");
        message.setForeground(Palette.TEXT_MUTED);
        message.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));
        card.add(message, BorderLayout.CENTER);
        return card;
    }

    /**
     * Construye la fila que muestra una canción favorita.
     *
     * @param song  datos de la canción, en orden: título, artista, álbum, género y
     *              duración
     * @param index posición de la canción en la lista; se usa para mostrar el
     *              número
     *              y como comando de acción del botón quitar
     * @return panel que representa la canción
     */
    private MyPanel createRow(String[] song, int index) {
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

        // 2. Icono de la canción
        addToRow(row, createIcon(), 1, 0);

        // 3. Título, artista y álbum (se reparten el espacio sobrante)
        addToRow(row, createLabel(song[TITLE], Palette.TEXT, Font.BOLD, 13), 2, 3);
        addToRow(row, createLabel(song[ARTIST], Palette.TEXT_MUTED, Font.PLAIN, 12), 3, 2);
        addToRow(row, createLabel(song[ALBUM], Palette.TEXT_MUTED, Font.PLAIN, 12), 4, 2);

        // 4. Etiqueta del género
        addToRow(row, createGenreTag(song[GENRE]), 5, 0);

        // 5. Corazón y duración
        addToRow(row, createLabel("♥", Palette.ACCENT, Font.BOLD, 14), 6, 0);
        addToRow(row, createLabel(song[DURATION], Palette.TEXT_MUTED, Font.PLAIN, 11), 7, 0);

        // 6. Botón quitar
        MyButton removeButton = new MyButton("Quitar de favoritos", Palette.TRANSPARENT, Palette.TEXT,
                Palette.TEXT_MUTED);
        removeButton.setFixedSize(140, 28);
        removeButton.setCornerRadius(14);
        removeButton.setFontStyle(Font.PLAIN, 11);
        removeButton.setActionCommand(String.valueOf(index)); // así el Presenter sabe cuál fila fue
        removeButtons.add(removeButton);
        addToRow(row, removeButton, 8, 0);

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

    /**
     * Crea una etiqueta con el color, estilo y tamaño indicados.
     *
     * @param text  texto de la etiqueta
     * @param color color del texto
     * @param style estilo de la fuente, por ejemplo Font.BOLD
     * @param size  tamaño de la fuente en puntos
     * @return etiqueta configurada
     */
    private JLabel createLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    /**
     * Crea el recuadro decorativo con la nota musical.
     *
     * @return panel que representa el icono de la canción
     */
    private MyPanel createIcon() {
        MyPanel icon = new MyPanel();
        icon.applyLayout(new GridBagLayout()); // centra el símbolo
        icon.setBackgroundColor(ICON_BOX);
        icon.setCornerRadius(8);
        icon.setPreferredSize(new Dimension(34, 34));
        icon.add(createLabel("♪", Palette.ACCENT, Font.BOLD, 14));
        return icon;
    }

    /**
     * Crea la etiqueta que muestra el género musical.
     *
     * @param genre nombre del género
     * @return panel con el género
     */
    private MyPanel createGenreTag(String genre) {
        MyPanel tag = new MyPanel();
        tag.applyLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
        tag.setBackgroundColor(Palette.TRANSPARENT);
        tag.setCornerRadius(9);
        tag.setBorderColor(Palette.ACCENT);
        tag.setPreferredSize(null); // se ajusta al texto
        tag.add(createLabel(genre, Palette.ACCENT, Font.PLAIN, 10));
        return tag;
    }

    /**
     * Actualiza la lista con las canciones favoritas recibidas, sin filtro.
     *
     * @param songs lista de canciones; cada elemento es un arreglo con título,
     *              artista, álbum, género y duración
     */
    public void showFavorites(List<String[]> songs) {
        showFavorites(songs, songs.size());
    }

    /**
     * Actualiza la lista con las canciones favoritas recibidas.
     * <p>
     * Borra las filas anteriores, crea una fila por canción, actualiza el
     * contador y le pone el listener de quitar a los botones nuevos, si ya se
     * había registrado. Si no hay nada que mostrar, muestra un mensaje.
     *
     * @param songs          canciones que se ven (pueden estar filtradas por el
     *                       buscador); cada elemento es un arreglo con título,
     *                       artista, álbum, género y duración
     * @param totalFavorites cantidad total de favoritas, sin filtrar
     */
    public void showFavorites(List<String[]> songs, int totalFavorites) {
        songList.removeAll();
        removeButtons.clear();

        if (totalFavorites == 0) {
            songList.add(createEmptyMessage("Todavía no tienes favoritas. Toca ♡ en una canción para agregarla."));
        } else if (songs.isEmpty()) {
            songList.add(createEmptyMessage("Ninguna favorita coincide con \"" + getQuery() + "\"."));
        }
        for (int i = 0; i < songs.size(); i++) {
            songList.add(createRow(songs.get(i), i));
            songList.add(Box.createVerticalStrut(6));
        }

        titleLabel.setText(songs.size() == totalFavorites ? TITLE_PREFIX + totalFavorites
                : TITLE_PREFIX + songs.size() + " de " + totalFavorites);
        playAllButton.setEnabled(!songs.isEmpty());

        // Los botones nuevos también deben escuchar
        if (removeListener != null) {
            for (MyButton button : removeButtons) {
                button.addActionListener(removeListener);
            }
        }

        // Las filas nuevas se crean con colores del tema oscuro
        ThemeManager.refresh(songList);
        songList.revalidate();
        songList.repaint();
    }

    private MyPanel createEmptyMessage(String text) {
        MyPanel box = new MyPanel();
        box.applyLayout(new FlowLayout(FlowLayout.LEFT, 0, 10));
        box.setBackgroundColor(Palette.BACKGROUND);
        box.setPreferredSize(null);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(createLabel(text, Palette.TEXT_MUTED, Font.PLAIN, 12));
        return box;
    }

    private void searchChanged() {
        if (!showingPlaceholder && searchListener != null) {
            searchListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getQuery()));
        }
    }

    /**
     * Texto escrito en el buscador, sin espacios al inicio ni al final. Si solo
     * se ve el texto de ayuda, devuelve una cadena vacía.
     *
     * @return texto que se busca
     */
    public String getQuery() {
        return showingPlaceholder ? "" : searchField.getText().trim();
    }

    /**
     * Registra lo que pasa cuando se escribe en el buscador.
     *
     * @param listener listener que atiende la búsqueda
     */
    public void addSearchListener(ActionListener listener) {
        searchListener = listener;
    }

    /**
     * Registra el listener de los botones "Quitar de favoritos". El comando de
     * acción de cada botón es el índice de su fila (0, 1, 2...).
     *
     * @param listener listener que atiende los eventos de quitar; también se
     *                 agrega a los botones que ya existen
     */
    public void addRemoveFavoriteListener(ActionListener listener) {
        removeListener = listener;
        for (MyButton button : removeButtons) {
            button.addActionListener(listener);
        }
    }

    /**
     * Registra el listener del botón "Reproducir todo".
     *
     * @param listener listener que atiende el evento
     */
    public void addPlayAllListener(ActionListener listener) {
        playAllButton.addActionListener(listener);
    }

    /**
     * Muestra en el círculo de arriba la inicial del usuario.
     *
     * @param userName nombre del usuario que inició sesión
     */
    public void setUserName(String userName) {
        avatar.setText(initialOf(userName));
        avatar.setToolTipText(userName);
    }

    static String initialOf(String userName) {
        return userName == null || userName.isBlank() ? " " : userName.trim().substring(0, 1).toUpperCase();
    }
}