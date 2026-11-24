package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Ventana para elegir canciones de la biblioteca y agregarlas a una playlist.
 * Tiene un buscador y, en cada canción, un botón "+ Agregar" que cambia a
 * "✓ En la playlist" cuando ya fue agregada.
 * <p>
 * Solo vista: cuando el usuario presiona "+ Agregar", le pregunta al Presenter
 * (con {@code onAdd}) si la canción se pudo agregar.
 *
 * @author programacion 2
 */
public class SongPickerDialog extends JDialog {

    private static final Color ROW = new Color(33, 33, 33);
    private static final int ROW_HEIGHT = 52;

    // Posición de cada dato en las filas
    private static final int TITLE = 0;
    private static final int ARTIST = 1;
    private static final int ALBUM = 2;
    private static final int GENRE = 3;
    private static final int IN_PLAYLIST = 4;

    private final List<String[]> songs;
    /** true en la posición de cada canción que ya está en la playlist. */
    private final boolean[] added;
    private final IntPredicate onAdd;

    private final MyTextField searchField = new MyTextField("Buscar por canción, artista, álbum o género...", false);
    private final MyPanel songList = new MyPanel();
    private final JLabel statusLabel = new JLabel(" ");
    private final MyPanel root = new MyPanel();

    /**
     * Crea la ventana.
     *
     * @param owner        ventana sobre la que se abre
     * @param playlistName nombre de la playlist, para el título
     * @param songs        canciones de la biblioteca; cada una tiene título,
     *                     artista, álbum, género y "1" si ya está en la playlist
     * @param onAdd        recibe el índice de la canción elegida y devuelve true
     *                     si se pudo agregar
     */
    public SongPickerDialog(Window owner, String playlistName, List<String[]> songs, IntPredicate onAdd) {
        super(owner, "Agregar canciones a " + playlistName, ModalityType.APPLICATION_MODAL);
        this.songs = new ArrayList<>(songs);
        this.onAdd = onAdd;
        this.added = new boolean[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            added[i] = "1".equals(songs.get(i)[IN_PLAYLIST]);
        }

        root.setBackgroundColor(Palette.BACKGROUND);
        root.applyLayout(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.setPreferredSize(new Dimension(620, 520));

        JLabel title = new JLabel("Agregar a \"" + playlistName + "\"");
        title.setForeground(Palette.TEXT);
        title.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 16));

        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchField.setPreferredSize(new Dimension(300, 34));
        // Filtra mientras se escribe
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                showSongs();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                showSongs();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                showSongs();
            }
        });

        MyPanel top = new MyPanel();
        top.setBackgroundColor(Palette.BACKGROUND);
        top.applyLayout(new BorderLayout(0, 12));
        top.setPreferredSize(null);
        top.add(title, BorderLayout.NORTH);
        top.add(searchField, BorderLayout.CENTER);

        songList.setBackgroundColor(Palette.BACKGROUND);
        songList.applyLayout(new BoxLayout(songList, BoxLayout.Y_AXIS));
        songList.setPreferredSize(null);

        // Contenedor que mantiene la lista pegada arriba aunque tenga pocas filas
        MyPanel wrapper = new MyPanel();
        wrapper.setBackgroundColor(Palette.BACKGROUND);
        wrapper.applyLayout(new BorderLayout());
        wrapper.setPreferredSize(null);
        wrapper.add(songList, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Palette.BACKGROUND);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        statusLabel.setForeground(Palette.ACCENT);
        statusLabel.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));

        MyButton doneButton = new MyButton("Listo", Palette.ACCENT, Palette.TEXT_DARK, null);
        doneButton.setFixedSize(90, 30);
        doneButton.setFontStyle(Font.BOLD, 11);
        doneButton.addActionListener(event -> dispose());

        MyPanel footer = new MyPanel();
        footer.setBackgroundColor(Palette.BACKGROUND);
        footer.applyLayout(new BorderLayout(8, 0));
        footer.setPreferredSize(new Dimension(0, 34));
        footer.add(statusLabel, BorderLayout.CENTER);
        footer.add(doneButton, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        showSongs();
        ThemeManager.refresh(root);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Muestra las canciones que coinciden con lo escrito en el buscador. */
    private void showSongs() {
        String query = searchField.getValue().trim().toLowerCase();
        songList.removeAll();

        if (songs.isEmpty()) {
            songList.add(createMessage("Tu biblioteca está vacía. Agrega canciones en Biblioteca → \"+ Agregar canciones\"."));
        }
        int shown = 0;
        for (int i = 0; i < songs.size(); i++) {
            if (query.isEmpty() || matches(songs.get(i), query)) {
                songList.add(createRow(songs.get(i), i, shown));
                songList.add(Box.createVerticalStrut(4));
                shown++;
            }
        }
        if (!songs.isEmpty() && shown == 0) {
            songList.add(createMessage("Ninguna canción coincide con \"" + searchField.getValue().trim() + "\"."));
        }

        // Las filas nuevas se crean con colores del tema oscuro
        ThemeManager.refresh(songList);
        songList.revalidate();
        songList.repaint();
    }

    private boolean matches(String[] song, String query) {
        for (int i = TITLE; i <= GENRE; i++) {
            if (song[i] != null && song[i].toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fila de una canción.
     *
     * @param index    posición de la canción en la lista completa (la que se
     *                 envía al Presenter)
     * @param position posición en la lista que se ve, para intercalar colores
     */
    private MyPanel createRow(String[] song, int index, int position) {
        MyPanel row = new MyPanel();
        row.setBackgroundColor(position % 2 == 0 ? ROW : Palette.TRANSPARENT);
        row.setCornerRadius(8);
        row.applyLayout(new GridBagLayout());
        row.setPreferredSize(new Dimension(100, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = label(song[TITLE], Font.BOLD, 13, Palette.TEXT);
        JLabel details = label(song[ARTIST] + " · " + song[ALBUM], Font.PLAIN, 11, Palette.TEXT_MUTED);

        MyPanel texts = new MyPanel();
        texts.setBackgroundColor(Palette.TRANSPARENT);
        texts.applyLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setPreferredSize(null);
        texts.add(title);
        texts.add(Box.createVerticalStrut(2));
        texts.add(details);

        MyButton genreChip = new MyButton(song[GENRE], Palette.TRANSPARENT, Palette.ACCENT, Palette.ACCENT);
        genreChip.setFixedSize(84, 22);
        genreChip.setCornerRadius(11);
        genreChip.setFontStyle(Font.PLAIN, 10);

        MyButton addButton = new MyButton("+ Agregar", Palette.ACCENT, Palette.TEXT_DARK, null);
        addButton.setFixedSize(124, 28);
        addButton.setCornerRadius(14);
        addButton.setFontStyle(Font.BOLD, 11);
        if (added[index]) {
            markAdded(addButton);
        }
        addButton.addActionListener(event -> {
            if (!added[index] && onAdd.test(index)) {
                added[index] = true;
                markAdded(addButton);
                ThemeManager.refresh(addButton);
                statusLabel.setText("Agregada: " + song[TITLE]);
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 10, 0, 10);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        row.add(texts, c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        row.add(genreChip, c);
        c.gridx = 2;
        row.add(addButton, c);
        return row;
    }

    private void markAdded(MyButton button) {
        button.setText("✓ En la playlist");
        button.setColors(Palette.TRANSPARENT, Palette.TEXT_MUTED, Palette.CARD_BORDER);
        button.setFontStyle(Font.PLAIN, 11);
        button.setEnabled(false);
    }

    /**
     * Muestra un mensaje en la parte de abajo, por ejemplo cuando una canción
     * no se pudo agregar.
     *
     * @param message texto que ve el usuario
     */
    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    private JLabel label(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Se puede encoger con "..." si el texto es muy largo
        label.setMinimumSize(new Dimension(40, 16));
        return label;
    }

    private MyPanel createMessage(String text) {
        MyPanel box = new MyPanel();
        box.setBackgroundColor(Palette.TRANSPARENT);
        box.applyLayout(new BorderLayout());
        box.setPreferredSize(new Dimension(100, 40));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(label(text, Font.PLAIN, 12, Palette.TEXT_MUTED), BorderLayout.WEST);
        return box;
    }
}
