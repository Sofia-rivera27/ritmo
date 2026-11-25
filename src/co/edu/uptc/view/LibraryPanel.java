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
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import co.edu.uptc.model.SearchCriteria;

/**
 * Sección Biblioteca. Muestra las canciones importadas (archivos MP3) y
 * permite buscarlas por canción, artista o género mientras se escribe.
 * <p>
 * Solo vista: avisa las acciones del usuario mediante listeners. El comando de
 * acción de los botones de cada fila es el índice de esa fila (0, 1, 2...)
 * dentro de la lista que se está mostrando, que puede estar filtrada.
 * @author programacion 2
 */
public class LibraryPanel extends MyPanel {

    private static final Color ICON_BOX = new Color(40, 40, 40);
    private static final String SEARCH_PLACEHOLDER = "Buscar en tu biblioteca...";
    private static final int ROW_HEIGHT = 46;
    private static final int SCROLL_SPEED = 16;

    // Posición de cada dato en las filas que envía el Presenter
    private static final int TITLE = 0;
    private static final int ARTIST = 1;
    private static final int ALBUM = 2;
    private static final int GENRE = 3;
    private static final int DURATION = 4;
    private static final int FAVORITE = 5;

    private final JLabel titleLabel;
    private final JTextField searchField;
    private final MyPanel criteriaRow;
    private final MyPanel songList;
    private final JLabel statusLabel;
    private final MyButton playAllButton;

    private SearchCriteria criteria = SearchCriteria.TITLE;
    /** true mientras el buscador muestra el texto de ayuda y no algo escrito. */
    private boolean showingPlaceholder = true;

    // Listeners que registra el Presenter. Los botones los buscan al hacer
    // clic, así funcionan aunque las filas se vuelvan a crear.
    private ActionListener searchListener;
    private ActionListener importListener;
    private ActionListener playAllListener;
    private ActionListener playListener;
    private ActionListener favoriteListener;
    private ActionListener playlistListener;
    private ActionListener removeListener;
    private ActionListener editListener;
    private ActionListener addSongListener;
    
    /**
     * Construye el panel, vacío.
     */
    public LibraryPanel() {
        setBackgroundColor(Palette.BACKGROUND);
        applyLayout(new BorderLayout());
        setPreferredSize(null);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        titleLabel = createLabel("Biblioteca · 0", Palette.TEXT, Font.BOLD, 20);
        searchField = new JTextField();
        criteriaRow = createTransparentPanel();
        playAllButton = createButton("▶ Reproducir todo", Palette.ACCENT, Palette.TEXT_DARK, null, 150);
        songList = createTransparentPanel();
        statusLabel = createLabel(" ", Palette.ACCENT, Font.PLAIN, 11);

        MyPanel top = createTransparentPanel();
        top.applyLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createHeader());
        top.add(Box.createVerticalStrut(14));
        top.add(createSearchBar());
        top.add(Box.createVerticalStrut(14));

        songList.applyLayout(new BoxLayout(songList, BoxLayout.Y_AXIS));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        add(top, BorderLayout.NORTH);
        add(createScroll(songList), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        showSongs(List.of(), 0);
    }

    // ---------------------------------------------------------------
    // Encabezado y buscador
    // ---------------------------------------------------------------

    private MyPanel createHeader() {
        MyPanel texts = createTransparentPanel();
        texts.applyLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(titleLabel);
        texts.add(Box.createVerticalStrut(4));
        texts.add(createLabel("Tus canciones importadas. Los archivos no se mueven de su carpeta.",
                Palette.TEXT_MUTED, Font.PLAIN, 11));

        
        MyButton addSongButton = createButton("+ Agregar canciones", Palette.TRANSPARENT, Palette.ACCENT,
                Palette.ACCENT, 145);
        addSongButton.addActionListener(event -> forward(addSongListener, event));
        
        
        MyButton importButton = createButton("Importar MP3", Palette.TRANSPARENT, Palette.TEXT,
                Palette.TEXT_MUTED, 120);
        importButton.addActionListener(event -> forward(importListener, event));

        playAllButton.setFontStyle(Font.BOLD, 11);
        playAllButton.addActionListener(event -> forward(playAllListener, event));

        MyPanel buttons = createTransparentPanel();
        buttons.applyLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(addSongButton);
        buttons.add(importButton);
        buttons.add(playAllButton);

        MyPanel header = createTransparentPanel();
        header.applyLayout(new BorderLayout());
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        header.add(texts, BorderLayout.CENTER);
        header.add(centerVertically(buttons), BorderLayout.EAST);
        return header;
    }

    private MyPanel createSearchBar() {
        MyPanel searchBox = new MyPanel();
        searchBox.applyLayout(new BorderLayout());
        searchBox.setBackgroundColor(Palette.CARD);
        searchBox.setCornerRadius(18);
        searchBox.setBorderColor(Palette.CARD_BORDER);
        searchBox.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        searchBox.setPreferredSize(new Dimension(0, 36));

        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setForeground(Palette.TEXT_MUTED);
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setCaretColor(Palette.ACCENT);
        searchField.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 13));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (showingPlaceholder) {
                    showingPlaceholder = false;
                    searchField.setText("");
                    searchField.setForeground(Palette.TEXT);
                    ThemeManager.refresh(searchField);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (searchField.getText().isEmpty()) {
                    showingPlaceholder = true;
                    searchField.setText(SEARCH_PLACEHOLDER);
                    searchField.setForeground(Palette.TEXT_MUTED);
                    ThemeManager.refresh(searchField);
                }
            }
        });
        // Busca mientras se escribe, sin tener que presionar Enter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                searchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                searchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                searchChanged();
            }
        });
        searchBox.add(searchField, BorderLayout.CENTER);

        // Botones para elegir en qué campo buscar
        criteriaRow.applyLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (SearchCriteria option : SearchCriteria.values()) {
            MyButton chip = createButton(option.getLabel(), Palette.CARD, Palette.TEXT, null, 84);
            chip.setActionCommand(option.name());
            chip.addActionListener(event -> {
                criteria = option;
                paintCriteria();
                searchChanged();
            });
            criteriaRow.add(chip);
        }
        paintCriteria();

        MyPanel bar = createTransparentPanel();
        bar.applyLayout(new BorderLayout(12, 0));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        bar.add(searchBox, BorderLayout.CENTER);
        bar.add(centerVertically(criteriaRow), BorderLayout.EAST);
        return bar;
    }

    /** Resalta el botón del criterio elegido. */
    private void paintCriteria() {
        for (Component component : criteriaRow.getComponents()) {
            MyButton chip = (MyButton) component;
            boolean selected = chip.getActionCommand().equals(criteria.name());
            chip.setColors(selected ? Palette.ACCENT : Palette.CARD, selected ? Palette.TEXT_DARK : Palette.TEXT, null);
            chip.setFontStyle(selected ? Font.BOLD : Font.PLAIN, 11);
        }
        // Los colores de arriba son del tema oscuro; se ajustan al tema activo
        ThemeManager.refresh(criteriaRow);
    }

    private void searchChanged() {
        if (!showingPlaceholder) {
            forward(searchListener, new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getQuery()));
        }
    }

    // ---------------------------------------------------------------
    // Lista de canciones
    // ---------------------------------------------------------------

    /**
     * Muestra las canciones de la biblioteca (todas o las que coinciden con la
     * búsqueda).
     *
     * @param songs          cada elemento es un arreglo con título, artista,
     *                       álbum, género, duración y "1" si es favorita
     * @param librarySize    cantidad total de canciones en la biblioteca, para
     *                       el título y para elegir el mensaje cuando no hay
     *                       nada que mostrar
     */
    public void showSongs(List<String[]> songs, int librarySize) {
        titleLabel.setText(songs.size() == librarySize ? "Biblioteca · " + librarySize
                : "Biblioteca · " + songs.size() + " de " + librarySize);
        playAllButton.setEnabled(!songs.isEmpty());

        songList.removeAll();
        if (librarySize == 0) {
            songList.add(createEmptyMessage(
                    "Tu biblioteca está vacía. Usa \"Importar MP3\" para agregar canciones de tu computador."));
        } else if (songs.isEmpty()) {
            songList.add(createEmptyMessage("Ninguna canción coincide con \"" + getQuery() + "\" en "
                    + criteria.getLabel().toLowerCase() + "."));
        }
        for (int i = 0; i < songs.size(); i++) {
            songList.add(createRow(songs.get(i), i));
            songList.add(Box.createVerticalStrut(6));
        }
        // Las filas nuevas se crean con colores del tema oscuro
        ThemeManager.refresh(songList);
        songList.revalidate();
        songList.repaint();
    }

    private MyPanel createRow(String[] song, int index) {
        MyPanel row = new MyPanel();
        row.applyLayout(new GridBagLayout());
        row.setBackgroundColor(Palette.CARD);
        row.setCornerRadius(10);
        row.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel number = createLabel(String.valueOf(index + 1), Palette.TEXT_MUTED, Font.PLAIN, 11);
        number.setPreferredSize(new Dimension(24, 20));
        addToRow(row, number, 0, 0);
        addToRow(row, createIcon(), 1, 0);
        addToRow(row, createLabel(song[TITLE], Palette.TEXT, Font.BOLD, 13), 2, 3);
        addToRow(row, createLabel(song[ARTIST], Palette.TEXT_MUTED, Font.PLAIN, 12), 3, 2);
        addToRow(row, createLabel(song[ALBUM], Palette.TEXT_MUTED, Font.PLAIN, 12), 4, 2);
        addToRow(row, createGenreTag(song[GENRE]), 5, 0);

        boolean favorite = "1".equals(song[FAVORITE]);
        MyButton heart = new MyButton(favorite ? "♥" : "♡", Palette.TRANSPARENT,
                favorite ? Palette.ACCENT : Palette.TEXT_MUTED, null);
        heart.setFixedSize(30, 30);
        heart.setFontStyle(Font.BOLD, 14);
        heart.setToolTipText(favorite ? "Quitar de favoritos" : "Agregar a favoritos");
        connect(heart, index, () -> favoriteListener);
        addToRow(row, heart, 6, 0);

        addToRow(row, createLabel(song[DURATION], Palette.TEXT_MUTED, Font.PLAIN, 11), 7, 0);

        MyButton play = createButton("▶", Palette.TRANSPARENT, Palette.ACCENT, Palette.ACCENT, 40);
        play.setToolTipText("Reproducir");
        connect(play, index, () -> playListener);
        addToRow(row, play, 8, 0);

        MyButton playlist = createButton("+ Playlist", Palette.TRANSPARENT, Palette.TEXT, Palette.TEXT_MUTED, 82);
        connect(playlist, index, () -> playlistListener);
        addToRow(row, playlist, 9, 0);

        MyButton edit = createButton("Editar", Palette.TRANSPARENT, Palette.TEXT, Palette.TEXT_MUTED, 62);
        edit.setToolTipText("Editar el título o la imagen de portada");
        connect(edit, index, () -> editListener);
        addToRow(row, edit, 10, 0);

        MyButton remove = createButton("Quitar", Palette.TRANSPARENT, Palette.TEXT, Palette.TEXT_MUTED, 70);
        remove.setToolTipText("Quitar de la biblioteca (el archivo no se borra)");
        connect(remove, index, () -> removeListener);
        addToRow(row, remove, 11, 0);
        return row;
    }

    /**
     * Pone el índice de la fila como comando de acción del botón y, al hacer
     * clic, avisa al listener que esté registrado en ese momento.
     */
    private void connect(MyButton button, int index, Supplier<ActionListener> listener) {
        button.setActionCommand(String.valueOf(index));
        button.addActionListener(event -> forward(listener.get(), event));
    }

    private void addToRow(MyPanel row, Component component, int column, double weight) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = 0;
        constraints.weightx = weight;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = weight > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 6, 0, 6);
        row.add(component, constraints);
    }

    // ---------------------------------------------------------------
    // Piezas comunes
    // ---------------------------------------------------------------

    private MyPanel createTransparentPanel() {
        MyPanel panel = new MyPanel();
        panel.setBackgroundColor(Palette.BACKGROUND);
        panel.setPreferredSize(null);
        return panel;
    }

    private MyPanel centerVertically(Component component) {
        MyPanel box = createTransparentPanel();
        box.applyLayout(new GridBagLayout());
        box.add(component);
        return box;
    }

    private JScrollPane createScroll(MyPanel list) {
        // Contenedor que mantiene la lista pegada arriba aunque tenga pocas filas
        MyPanel wrapper = createTransparentPanel();
        wrapper.applyLayout(new BorderLayout());
        wrapper.add(list, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(Palette.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private MyButton createButton(String text, Color background, Color textColor, Color border, int width) {
        MyButton button = new MyButton(text, background, textColor, border);
        button.setFixedSize(width, 28);
        button.setCornerRadius(14);
        button.setFontStyle(Font.PLAIN, 11);
        return button;
    }

    private JLabel createLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    private MyPanel createIcon() {
        MyPanel icon = new MyPanel();
        icon.applyLayout(new GridBagLayout()); // centra el símbolo
        icon.setBackgroundColor(ICON_BOX);
        icon.setCornerRadius(8);
        icon.setPreferredSize(new Dimension(34, 34));
        icon.add(createLabel("♪", Palette.ACCENT, Font.BOLD, 14));
        return icon;
    }

    private MyPanel createGenreTag(String genre) {
        MyPanel tag = new MyPanel();
        tag.applyLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
        tag.setBackgroundColor(Palette.TRANSPARENT);
        tag.setCornerRadius(9);
        tag.setBorderColor(AdminComponents.genreColor(genre));
        tag.setPreferredSize(null); // se ajusta al texto
        tag.add(createLabel(genre, AdminComponents.genreColor(genre), Font.PLAIN, 10));
        return tag;
    }

    private MyPanel createEmptyMessage(String text) {
        MyPanel box = createTransparentPanel();
        box.applyLayout(new FlowLayout(FlowLayout.LEFT, 0, 10));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(createLabel(text, Palette.TEXT_MUTED, Font.PLAIN, 12));
        return box;
    }

    private void forward(ActionListener listener, ActionEvent event) {
        if (listener != null) {
            listener.actionPerformed(event);
        }
    }
    
    
    /**
     * Registra el listener del botón "+ Agregar canciones".
     *
     * @param listener listener que abre la pantalla para agregar canciones
     * @author progra 2
     */
    public void addAddSongListener(ActionListener listener) {
        addSongListener = listener;
    }
    
    
    

    // ---------------------------------------------------------------
    // Métodos que usa el Presenter
    // ---------------------------------------------------------------

    /**
     * Texto escrito en el buscador, sin espacios al inicio ni al final. Si
     * solo se ve el texto de ayuda, devuelve una cadena vacía.
     *
     * @return texto que se busca
     */
    public String getQuery() {
        return showingPlaceholder ? "" : searchField.getText().trim();
    }

    /**
     * Campo en el que se busca: canción, artista o género.
     *
     * @return criterio elegido
     */
    public SearchCriteria getCriteria() {
        return criteria;
    }

    /**
     * Muestra un mensaje corto debajo de la lista.
     *
     * @param message texto que ve el usuario
     */
    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    /** Se escribe en el buscador o se cambia el criterio. */
    public void addSearchListener(ActionListener listener) {
        searchListener = listener;
    }

    /** Botón "Importar MP3". */
    public void addImportListener(ActionListener listener) {
        importListener = listener;
    }

    /** Botón "Reproducir todo" (reproduce las canciones que se están viendo). */
    public void addPlayAllListener(ActionListener listener) {
        playAllListener = listener;
    }

    /** Botón "▶" de cada canción. */
    public void addPlayListener(ActionListener listener) {
        playListener = listener;
    }

    /** Corazón de cada canción. */
    public void addFavoriteListener(ActionListener listener) {
        favoriteListener = listener;
    }

    /** Botón "+ Playlist" de cada canción. */
    public void addPlaylistListener(ActionListener listener) {
        playlistListener = listener;
    }

    /** Botón "Quitar" de cada canción. */
    public void addRemoveListener(ActionListener listener) {
        removeListener = listener;
    }

    /** Botón "Editar" de cada canción. */
    public void addEditListener(ActionListener listener) {
        editListener = listener;
    }
}