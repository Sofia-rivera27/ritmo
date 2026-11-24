package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Sección Playlists, con dos tarjetas:
 * <ul>
 * <li><b>Izquierda:</b> campo para crear una playlist y la lista de playlists.
 * Al hacer clic en una, se selecciona.</li>
 * <li><b>Derecha:</b> las canciones de la playlist seleccionada, con botones
 * para reproducir, renombrar, eliminar y agregar canciones desde la
 * biblioteca.</li>
 * </ul>
 * Solo vista: avisa las acciones del usuario mediante listeners. El comando de
 * acción de los botones de cada fila es el índice de esa fila (0, 1, 2...).
 * <p>
 * Basado en el diseño original de la pantalla de playlists del equipo.
 *
 * @author programacion 2
 */
public class PlaylistPanel extends MyPanel {

    /** Fondo de la pantalla (el mismo de Home en el diseño original). */
    private static final Color SCREEN_BACKGROUND = new Color(17, 17, 17);
    /** Fondo de las filas de la lista (un poco más claro que la tarjeta). */
    private static final Color ROW = new Color(33, 33, 33);
    /** Fondo de los íconos (disco y nota musical). */
    private static final Color ICON_BOX = new Color(41, 41, 41);
    private static final Color ERROR_BACKGROUND = new Color(62, 26, 26);
    private static final Color ERROR_BORDER = new Color(225, 90, 90);
    private static final Color ERROR_TEXT = new Color(255, 170, 170);
    private static final Color SECONDARY_BORDER = new Color(85, 85, 85);

    /** Ancho de la tarjeta izquierda: un tercio del espacio, entre estos límites. */
    private static final int LEFT_CARD_MAX_WIDTH = 400;
    private static final int LEFT_CARD_MIN_WIDTH = 280;
    /** Si la tarjeta de canciones es más angosta que esto, se oculta el álbum. */
    private static final int COMPACT_SONGS_WIDTH = 720;
    private static final int PLAYLIST_ROW_HEIGHT = 70;
    private static final int SONG_ROW_HEIGHT = 62;
    /**
     * Ancho base de título, artista y álbum. Es pequeño a propósito: el
     * GridBagLayout reparte el resto del espacio entre las tres columnas, y si
     * el ancho base no cupiera las encogería todas al mínimo.
     */
    private static final int TEXT_COLUMN_WIDTH = 40;

    // Posición de cada dato en las filas de playlists
    private static final int PLAYLIST_NAME = 0;
    private static final int PLAYLIST_COUNT = 1;

    // Posición de cada dato en las filas de canciones
    private static final int TITLE = 0;
    private static final int ARTIST = 1;
    private static final int ALBUM = 2;
    private static final int GENRE = 3;
    private static final int DURATION = 4;
    private static final int FAVORITE = 5;

    // Tarjeta izquierda
    private MyTextField nameField;
    private MyButton createButton;
    private MyPanel errorBox;
    private JLabel errorLabel;
    private JComponent errorSpacer;
    private MyPanel playlistList;

    // Tarjeta derecha
    private JLabel playlistTitle;
    private JLabel playlistCount;
    private MyButton addFromLibraryButton;
    private MyButton playAllButton;
    private MyButton renameButton;
    private MyButton deleteButton;
    private MyPanel songList;
    private MyPanel createCard;
    /** Etiquetas de álbum de las filas actuales, para ocultarlas si no caben. */
    private final List<JLabel> albumLabels = new ArrayList<>();
    /** Botones "Quitar de playlist" de las filas actuales, para acortarlos. */
    private final List<MyButton> removeButtons = new ArrayList<>();
    private boolean compact;

    private final JLabel statusLabel;

    // Listeners que registra el Presenter. Los botones los buscan al hacer
    // clic, así funcionan aunque las filas se vuelvan a crear.
    private ActionListener createListener;
    private ActionListener selectListener;
    private ActionListener renameListener;
    private ActionListener deleteListener;
    private ActionListener playAllListener;
    private ActionListener addFromLibraryListener;
    private ActionListener playSongListener;
    private ActionListener removeSongListener;

    public PlaylistPanel() {
        setBackgroundColor(SCREEN_BACKGROUND);
        applyLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setPreferredSize(null);

        statusLabel = createLabel(" ", Palette.ACCENT, Font.PLAIN, 11);

        MyPanel cards = createTransparent();
        cards.applyLayout(new BorderLayout(25, 0));
        createCard = createCreateCard();
        cards.add(createCard, BorderLayout.WEST);
        cards.add(createSongsCard(), BorderLayout.CENTER);
        // Ajusta los anchos cuando cambia el tamaño de la ventana
        cards.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                adjustToWidth(cards.getWidth());
            }
        });

        add(createLabel("Playlists", Palette.TEXT, Font.BOLD, 22), BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        hideError();
        showPlaylists(List.of(), -1);
        showNoPlaylistSelected(false);
    }

    /**
     * Reparte el ancho entre las dos tarjetas. En ventanas angostas la
     * tarjeta izquierda se achica y las filas de canciones ocultan el álbum.
     */
    private void adjustToWidth(int width) {
        int left = Math.max(LEFT_CARD_MIN_WIDTH, Math.min(LEFT_CARD_MAX_WIDTH, width / 3));
        createCard.setPreferredSize(new Dimension(left, 0));
        compact = width - left - 25 < COMPACT_SONGS_WIDTH;
        for (JLabel album : albumLabels) {
            album.setVisible(!compact);
        }
        for (MyButton remove : removeButtons) {
            setRemoveCompact(remove, compact);
        }
        revalidate();
    }

    /** En ventanas angostas el botón dice solo "Quitar" (el resto queda en el tooltip). */
    private void setRemoveCompact(MyButton remove, boolean compactMode) {
        remove.setText(compactMode ? "Quitar" : "Quitar de playlist");
        remove.setFixedSize(compactMode ? 76 : 130, 32);
    }

    // ---------------------------------------------------------------
    // Tarjeta izquierda: crear playlist y lista de playlists
    // ---------------------------------------------------------------
    private MyPanel createCreateCard() {
        MyPanel card = createCard();
        card.setPreferredSize(new Dimension(LEFT_CARD_MAX_WIDTH, 0));

        nameField = new MyTextField("Nombre de la playlist", false);
        nameField.setFixedSize(200, 44);
        // Enter en el campo hace lo mismo que el botón "Crear"
        nameField.addActionListener(event -> forward(createListener, event));

        createButton = new MyButton("Crear", Palette.ACCENT, Palette.TEXT_DARK, null);
        createButton.setFontStyle(Font.BOLD, 12);
        createButton.setFixedSize(70, 44);
        createButton.setCornerRadius(22);
        createButton.addActionListener(event -> forward(createListener, event));

        MyPanel createRow = createTransparent();
        createRow.applyLayout(new BorderLayout(14, 0));
        createRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        createRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        // El campo se estira; por eso se quita su ancho máximo fijo
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        createRow.add(nameField, BorderLayout.CENTER);
        createRow.add(createButton, BorderLayout.EAST);

        errorLabel = createLabel(" ", ERROR_TEXT, Font.PLAIN, 12);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        errorBox = new MyPanel();
        errorBox.setBackgroundColor(ERROR_BACKGROUND);
        errorBox.setBorderColor(ERROR_BORDER);
        errorBox.setCornerRadius(4);
        errorBox.applyLayout(new BorderLayout());
        errorBox.setPreferredSize(new Dimension(100, 36));
        errorBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        errorBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorBox.add(errorLabel, BorderLayout.CENTER);
        errorSpacer = (JComponent) Box.createVerticalStrut(12);

        playlistList = createList();

        card.add(createLeftLabel("Crear playlist", Palette.TEXT, Font.BOLD, 16));
        card.add(Box.createVerticalStrut(14));
        card.add(createRow);
        card.add(Box.createVerticalStrut(12));
        card.add(errorBox);
        card.add(errorSpacer);
        card.add(createScroll(playlistList));
        return card;
    }

    // ---------------------------------------------------------------
    // Tarjeta derecha: canciones de la playlist seleccionada
    // ---------------------------------------------------------------
    private MyPanel createSongsCard() {
        MyPanel card = createCard();

        playlistTitle = createLabel(" ", Palette.TEXT, Font.BOLD, 16);
        playlistCount = createLabel(" ", Palette.TEXT_MUTED, Font.PLAIN, 12);

        addFromLibraryButton = new MyButton("Agregar desde biblioteca", Palette.ACCENT, Palette.TEXT_DARK, null);
        addFromLibraryButton.setFontStyle(Font.BOLD, 12);
        addFromLibraryButton.setFixedSize(190, 30);
        addFromLibraryButton.setCornerRadius(15);
        addFromLibraryButton.addActionListener(event -> forward(addFromLibraryListener, event));

        // Fila 1: nombre de la playlist y "Agregar desde biblioteca"
        MyPanel titleRow = createTransparent();
        titleRow.applyLayout(new BorderLayout(10, 0));
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.add(playlistTitle, BorderLayout.CENTER);
        titleRow.add(addFromLibraryButton, BorderLayout.EAST);

        playAllButton = createSmallButton("▶ Reproducir", Palette.ACCENT, Palette.ACCENT, 104);
        playAllButton.addActionListener(event -> forward(playAllListener, event));
        renameButton = createSmallButton("Renombrar", Palette.TEXT, SECONDARY_BORDER, 92);
        renameButton.addActionListener(event -> forward(renameListener, event));
        deleteButton = createSmallButton("Eliminar", Palette.TEXT, SECONDARY_BORDER, 82);
        deleteButton.addActionListener(event -> forward(deleteListener, event));

        MyPanel actions = createTransparent();
        actions.applyLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(playAllButton);
        actions.add(renameButton);
        actions.add(deleteButton);

        // Fila 2: cantidad de canciones y acciones de la playlist
        MyPanel actionsRow = createTransparent();
        actionsRow.applyLayout(new BorderLayout(10, 0));
        actionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsRow.add(playlistCount, BorderLayout.CENTER);
        actionsRow.add(actions, BorderLayout.EAST);

        songList = createList();

        card.add(titleRow);
        card.add(Box.createVerticalStrut(8));
        card.add(actionsRow);
        card.add(Box.createVerticalStrut(12));
        card.add(createScroll(songList));
        card.add(Box.createVerticalStrut(10));
        card.add(createLeftLabel("Quitar de una playlist no elimina la canción de la biblioteca.",
                Palette.TEXT_MUTED, Font.PLAIN, 12));
        return card;
    }

    // ---------------------------------------------------------------
    // Métodos que usa el Presenter
    // ---------------------------------------------------------------

    /**
     * Muestra la lista de playlists de la tarjeta izquierda.
     *
     * @param playlists     cada elemento tiene el nombre y el texto con la
     *                      cantidad de canciones
     * @param selectedIndex posición de la playlist seleccionada, o -1
     */
    public void showPlaylists(List<String[]> playlists, int selectedIndex) {
        playlistList.removeAll();
        if (playlists.isEmpty()) {
            playlistList.add(createEmptyMessage("Todavía no tienes playlists."));
        }
        for (int i = 0; i < playlists.size(); i++) {
            if (i > 0) {
                playlistList.add(Box.createVerticalStrut(5));
            }
            playlistList.add(createPlaylistItem(playlists.get(i), i, i == selectedIndex));
        }
        refreshList(playlistList);
    }

    /**
     * Muestra en la tarjeta derecha las canciones de la playlist seleccionada.
     *
     * @param name  nombre de la playlist
     * @param count texto con la cantidad de canciones, por ejemplo "5 canciones"
     * @param songs cada elemento tiene título, artista, álbum, género, duración
     *              y "1" si la canción es favorita
     */
    public void showSelectedPlaylist(String name, String count, List<String[]> songs) {
        playlistTitle.setText(name);
        playlistCount.setText(count);
        setPlaylistButtonsEnabled(true);
        playAllButton.setEnabled(!songs.isEmpty());

        songList.removeAll();
        albumLabels.clear();
        removeButtons.clear();
        if (songs.isEmpty()) {
            songList.add(createEmptyMessage("Esta playlist está vacía. Usa \"Agregar desde biblioteca\"."));
        }
        for (int i = 0; i < songs.size(); i++) {
            songList.add(createSongRow(songs.get(i), i));
        }
        refreshList(songList);
    }

    /**
     * Deja la tarjeta derecha vacía cuando no hay ninguna playlist seleccionada.
     *
     * @param hasPlaylists true si existen playlists (para elegir el mensaje)
     */
    public void showNoPlaylistSelected(boolean hasPlaylists) {
        playlistTitle.setText(hasPlaylists ? "Elige una playlist" : "Sin playlists");
        playlistCount.setText(" ");
        setPlaylistButtonsEnabled(false);
        songList.removeAll();
        songList.add(createEmptyMessage(hasPlaylists ? "Haz clic en una playlist de la izquierda."
                : "Crea una playlist a la izquierda para empezar."));
        refreshList(songList);
    }

    /** Texto escrito en el campo "Nombre de la playlist". */
    public String getNewPlaylistName() {
        return nameField.getValue();
    }

    public void clearNewPlaylistName() {
        nameField.clear();
    }

    /**
     * Muestra un error en la caja roja, debajo del campo de nombre.
     *
     * @param message texto que ve el usuario
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorBox.setVisible(true);
        errorSpacer.setVisible(true);
        revalidate();
        repaint();
    }

    public void hideError() {
        errorBox.setVisible(false);
        errorSpacer.setVisible(false);
        revalidate();
        repaint();
    }

    /**
     * Muestra un mensaje corto debajo de las tarjetas.
     *
     * @param message texto que ve el usuario
     */
    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    /** Botón "Crear" y Enter en el campo de nombre. */
    public void addCreateListener(ActionListener listener) {
        createListener = listener;
    }

    /** Clic en una playlist de la izquierda (comando = índice). */
    public void addSelectListener(ActionListener listener) {
        selectListener = listener;
    }

    /** Botón "Renombrar" de la playlist seleccionada. */
    public void addRenameListener(ActionListener listener) {
        renameListener = listener;
    }

    /** Botón "Eliminar" de la playlist seleccionada. */
    public void addDeleteListener(ActionListener listener) {
        deleteListener = listener;
    }

    /** Botón "▶ Reproducir" de la playlist seleccionada. */
    public void addPlayAllListener(ActionListener listener) {
        playAllListener = listener;
    }

    /** Botón "Agregar desde biblioteca". */
    public void addAddFromLibraryListener(ActionListener listener) {
        addFromLibraryListener = listener;
    }

    /** Ícono ♫ de cada canción: la reproduce (comando = índice). */
    public void addPlaySongListener(ActionListener listener) {
        playSongListener = listener;
    }

    /** Botón "Quitar de playlist" de cada canción (comando = índice). */
    public void addRemoveSongListener(ActionListener listener) {
        removeSongListener = listener;
    }

    // ---------------------------------------------------------------
    // Filas
    // ---------------------------------------------------------------

    /** Fila de una playlist: disco a la izquierda, nombre y cantidad. */
    private MyPanel createPlaylistItem(String[] playlist, int index, boolean selected) {
        MyButton icon = new MyButton("◉", ICON_BOX, Palette.ACCENT, null);
        icon.setFixedSize(54, 54);
        icon.setCornerRadius(8);
        icon.setFontStyle(Font.PLAIN, 18);
        connect(icon, index, () -> selectListener);

        MyPanel texts = createTransparent();
        texts.applyLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(Box.createVerticalGlue());
        texts.add(createLeftLabel(playlist[PLAYLIST_NAME], Palette.TEXT, Font.BOLD, 14));
        texts.add(createLeftLabel(playlist[PLAYLIST_COUNT], Palette.TEXT_MUTED, Font.PLAIN, 12));
        texts.add(Box.createVerticalGlue());

        MyPanel item = new MyPanel();
        // Gris más claro = playlist seleccionada
        item.setBackgroundColor(selected ? Palette.FIELD : ROW);
        item.setCornerRadius(8);
        item.applyLayout(new BorderLayout(14, 0));
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        item.setPreferredSize(new Dimension(100, PLAYLIST_ROW_HEIGHT));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, PLAYLIST_ROW_HEIGHT));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.add(icon, BorderLayout.WEST);
        item.add(texts, BorderLayout.CENTER);

        // Clic en cualquier parte de la fila también la selecciona
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                forward(selectListener, new ActionEvent(item, ActionEvent.ACTION_PERFORMED, String.valueOf(index)));
            }
        });
        return item;
    }

    /** Fila de una canción, con las columnas del diseño original. */
    private MyPanel createSongRow(String[] song, int index) {
        MyButton icon = new MyButton("♫", ICON_BOX, Palette.ACCENT, null);
        icon.setFixedSize(46, 46);
        icon.setCornerRadius(8);
        icon.setFontStyle(Font.PLAIN, 16);
        icon.setToolTipText("Reproducir");
        connect(icon, index, () -> playSongListener);

        MyButton genreChip = new MyButton(song[GENRE], Palette.TRANSPARENT, Palette.ACCENT, Palette.ACCENT);
        genreChip.setFixedSize(84, 24);
        genreChip.setCornerRadius(12);
        genreChip.setFontStyle(Font.PLAIN, 11);

        MyButton remove = new MyButton("Quitar de playlist", Palette.TRANSPARENT, Palette.TEXT, SECONDARY_BORDER);
        remove.setToolTipText("Quitar de playlist");
        remove.setCornerRadius(16);
        setRemoveCompact(remove, compact);
        removeButtons.add(remove);
        connect(remove, index, () -> removeSongListener);

        boolean favorite = "1".equals(song[FAVORITE]);

        MyPanel row = new MyPanel();
        // Filas intercaladas: las impares con fondo un poco más claro
        row.setBackgroundColor(index % 2 == 0 ? ROW : Palette.TRANSPARENT);
        row.setCornerRadius(8);
        row.applyLayout(new GridBagLayout());
        row.setPreferredSize(new Dimension(100, SONG_ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, SONG_ROW_HEIGHT));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 6, 0, 6);

        c.gridx = 0;
        row.add(column(String.valueOf(index + 1), Font.PLAIN, 13, Palette.TEXT_MUTED, 24), c);
        c.gridx = 1;
        row.add(icon, c);
        // Título, artista y álbum se reparten el espacio libre (40% / 30% / 30%)
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.weightx = 0.4;
        row.add(column(song[TITLE], Font.BOLD, 14, Palette.TEXT, TEXT_COLUMN_WIDTH), c);
        c.gridx = 3;
        c.weightx = 0.3;
        row.add(column(song[ARTIST], Font.PLAIN, 13, Palette.TEXT_MUTED, TEXT_COLUMN_WIDTH), c);
        c.gridx = 4;
        JLabel album = column(song[ALBUM], Font.PLAIN, 13, Palette.TEXT_MUTED, TEXT_COLUMN_WIDTH);
        album.setVisible(!compact);
        albumLabels.add(album);
        row.add(album, c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 5;
        row.add(genreChip, c);
        c.gridx = 6;
        row.add(column(favorite ? "♥" : "♡", Font.PLAIN, 14, favorite ? Palette.ACCENT : Palette.TEXT_MUTED, 20), c);
        c.gridx = 7;
        row.add(column(song[DURATION], Font.PLAIN, 13, Palette.TEXT_MUTED, 40), c);
        c.gridx = 8;
        row.add(remove, c);
        return row;
    }

    // ---------------------------------------------------------------
    // Piezas comunes
    // ---------------------------------------------------------------

    /** Tarjeta con el estilo común (fondo, borde y margen). */
    private MyPanel createCard() {
        MyPanel card = new MyPanel();
        card.setBackgroundColor(Palette.CARD);
        card.setCornerRadius(12);
        card.setBorderColor(Palette.CARD_BORDER);
        card.applyLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        card.setPreferredSize(null);
        return card;
    }

    private MyPanel createTransparent() {
        MyPanel panel = new MyPanel();
        panel.setBackgroundColor(Palette.TRANSPARENT);
        panel.setPreferredSize(null);
        return panel;
    }

    /** Lista vertical donde se agregan filas; sigue el ancho del scroll. */
    private MyPanel createList() {
        MyPanel list = new ScrollableList();
        list.setBackgroundColor(Palette.TRANSPARENT);
        list.applyLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setPreferredSize(null);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
        return list;
    }

    /** Envuelve una lista en un scroll transparente, sin fondo ni borde. */
    private JScrollPane createScroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBar());
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.setPreferredSize(new Dimension(100, 100)); // el alto real lo da la tarjeta
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private MyButton createSmallButton(String text, Color textColor, Color border, int width) {
        MyButton button = new MyButton(text, Palette.TRANSPARENT, textColor, border);
        button.setFixedSize(width, 28);
        button.setCornerRadius(14);
        button.setFontStyle(Font.PLAIN, 11);
        return button;
    }

    /** Sin playlist seleccionada los botones se desactivan y se ocultan. */
    private void setPlaylistButtonsEnabled(boolean enabled) {
        for (MyButton button : List.of(addFromLibraryButton, playAllButton, renameButton, deleteButton)) {
            button.setEnabled(enabled);
            button.setVisible(enabled);
        }
    }

    private JLabel createLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    /** Etiqueta alineada a la izquierda, para usarla dentro de un BoxLayout. */
    private JLabel createLeftLabel(String text, Color color, int style, int size) {
        JLabel label = createLabel(text, color, style, size);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Texto de una columna de la fila de canciones. Tiene un ancho preferido
     * para que las columnas queden alineadas, pero se puede encoger (con "...")
     * cuando la ventana es angosta.
     */
    private JLabel column(String text, int style, int size, Color color, int width) {
        JLabel label = createLabel(text, color, style, size);
        label.setPreferredSize(new Dimension(width, 20));
        label.setMinimumSize(new Dimension(Math.min(width, 30), 20));
        return label;
    }

    private MyPanel createEmptyMessage(String text) {
        MyPanel box = createTransparent();
        box.applyLayout(new FlowLayout(FlowLayout.LEFT, 0, 10));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        box.add(createLabel(text, Palette.TEXT_MUTED, Font.PLAIN, 12));
        return box;
    }

    /**
     * Pone el índice como comando de acción del botón y, al hacer clic, avisa
     * al listener que esté registrado en ese momento.
     */
    private void connect(MyButton button, int index, Supplier<ActionListener> listener) {
        button.setActionCommand(String.valueOf(index));
        button.addActionListener(event -> forward(listener.get(), event));
    }

    private void refreshList(MyPanel list) {
        // Las filas nuevas se crean con colores del tema oscuro
        ThemeManager.refresh(list);
        list.revalidate();
        list.repaint();
    }

    private void forward(ActionListener listener, ActionEvent event) {
        if (listener != null) {
            listener.actionPerformed(event);
        }
    }

    // ---------------------------------------------------------------
    // Clases internas para el scroll
    // ---------------------------------------------------------------

    /** Lista que sigue el ancho del scroll (las filas se estiran) y crece hacia abajo. */
    private static class ScrollableList extends MyPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Barra de scroll delgada y oscura, a juego con el diseño. */
    private static class DarkScrollBar extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(85, 85, 85);
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return emptyButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return emptyButton();
        }

        private JButton emptyButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // sin fondo
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            g2.dispose();
        }
    }
}