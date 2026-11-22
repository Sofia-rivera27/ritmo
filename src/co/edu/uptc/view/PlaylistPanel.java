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
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class PlaylistPanel extends MyPanel {

    private static final Color ICON_BOX = new Color(40, 40, 40);
    private static final int ROW_HEIGHT = 46;
    private static final int SCROLL_SPEED = 16;

    private static final int PLAYLIST_NAME = 0;
    private static final int PLAYLIST_COUNT = 1;

    private static final int TITLE = 0;
    private static final int ARTIST = 1;
    private static final int ALBUM = 2;
    private static final int DURATION = 3;

    private final MyPanel header;
    private final MyPanel content;
    private final JLabel statusLabel;


    private ActionListener createListener;
    private ActionListener openListener;
    private ActionListener renameListener;
    private ActionListener deleteListener;
    private ActionListener backListener;
    private ActionListener playAllListener;
    private ActionListener playSongListener;
    private ActionListener removeSongListener;

    public PlaylistPanel() {
        setBackgroundColor(Palette.BACKGROUND);
        applyLayout(new BorderLayout());
        setPreferredSize(null);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        header = new MyPanel();
        header.applyLayout(new BorderLayout());
        header.setBackgroundColor(Palette.BACKGROUND);
        header.setPreferredSize(new Dimension(0, 60));

        content = new MyPanel();
        content.applyLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackgroundColor(Palette.BACKGROUND);
        content.setPreferredSize(null);

        statusLabel = createLabel(" ", Palette.ACCENT, Font.PLAIN, 11);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        add(header, BorderLayout.NORTH);
        add(createScroll(content), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        showPlaylists(List.of());
    }

    public void showPlaylists(List<String[]> playlists) {
        MyButton createButton = createButton("+ Nueva playlist", Palette.ACCENT, Palette.TEXT_DARK, null, 140);
        createButton.setFontStyle(Font.BOLD, 11);
        createButton.addActionListener(event -> forward(createListener, event));

        setHeader(null, "Playlists · " + playlists.size(),
                "Tus listas de reproducción. Agrega canciones desde Explorar.", createButton);

        content.removeAll();
        if (playlists.isEmpty()) {
            content.add(createEmptyMessage(
                    "Todavía no tienes playlists. Crea una con \"+ Nueva playlist\"."));
        }
        for (int i = 0; i < playlists.size(); i++) {
            content.add(createPlaylistRow(playlists.get(i), i));
            content.add(Box.createVerticalStrut(6));
        }
        refreshContent();
    }

    private MyPanel createPlaylistRow(String[] playlist, int index) {
        MyPanel row = createRow();
        addToRow(row, createIcon("♫"), 0, 0);
        addToRow(row, createLabel(playlist[PLAYLIST_NAME], Palette.TEXT, Font.BOLD, 13), 1, 3);
        addToRow(row, createLabel(playlist[PLAYLIST_COUNT], Palette.TEXT_MUTED, Font.PLAIN, 12), 2, 1);
        addToRow(row, createRowButton("Abrir", Palette.ACCENT, index, () -> openListener, true), 3, 0);
        addToRow(row, createRowButton("Renombrar", Palette.TEXT, index, () -> renameListener, false), 4, 0);
        addToRow(row, createRowButton("Eliminar", Palette.TEXT, index, () -> deleteListener, false), 5, 0);
        return row;
    }


    public void showPlaylistDetail(String name, List<String[]> songs) {
        MyButton backButton = createButton("← Volver", Palette.TRANSPARENT, Palette.TEXT, Palette.TEXT_MUTED, 90);
        backButton.addActionListener(event -> forward(backListener, event));

        MyButton playAllButton = createButton("▶ Reproducir todo", Palette.ACCENT, Palette.TEXT_DARK, null, 150);
        playAllButton.setFontStyle(Font.BOLD, 11);
        playAllButton.setEnabled(!songs.isEmpty());
        playAllButton.addActionListener(event -> forward(playAllListener, event));

        String count = songs.size() == 1 ? "1 canción" : songs.size() + " canciones";
        setHeader(backButton, name, count, playAllButton);

        content.removeAll();
        if (songs.isEmpty()) {
            content.add(createEmptyMessage(
                    "Esta playlist está vacía. En Explorar, usa el botón de playlist de una canción para agregarla."));
        }
        for (int i = 0; i < songs.size(); i++) {
            content.add(createSongRow(songs.get(i), i));
            content.add(Box.createVerticalStrut(6));
        }
        refreshContent();
    }

    private MyPanel createSongRow(String[] song, int index) {
        MyPanel row = createRow();
        JLabel number = createLabel(String.valueOf(index + 1), Palette.TEXT_MUTED, Font.PLAIN, 11);
        number.setPreferredSize(new Dimension(24, 20));
        addToRow(row, number, 0, 0);
        addToRow(row, createIcon("♪"), 1, 0);
        addToRow(row, createLabel(song[TITLE], Palette.TEXT, Font.BOLD, 13), 2, 3);
        addToRow(row, createLabel(song[ARTIST], Palette.TEXT_MUTED, Font.PLAIN, 12), 3, 2);
        addToRow(row, createLabel(song[ALBUM], Palette.TEXT_MUTED, Font.PLAIN, 12), 4, 2);
        addToRow(row, createLabel(song[DURATION], Palette.TEXT_MUTED, Font.PLAIN, 11), 5, 0);
        addToRow(row, createRowButton("▶", Palette.ACCENT, index, () -> playSongListener, true), 6, 0);
        addToRow(row, createRowButton("Quitar", Palette.TEXT, index, () -> removeSongListener, false), 7, 0);
        return row;
    }


    private void setHeader(MyButton leftButton, String title, String subtitle, MyButton rightButton) {
        header.removeAll();

        MyPanel texts = new MyPanel();
        texts.applyLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setBackgroundColor(Palette.BACKGROUND);
        texts.setPreferredSize(null);
        texts.add(createLabel(title, Palette.TEXT, Font.BOLD, 20));
        texts.add(Box.createVerticalStrut(4));
        texts.add(createLabel(subtitle, Palette.TEXT_MUTED, Font.PLAIN, 11));

        if (leftButton != null) {
            MyPanel left = createCentered(leftButton);
            left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
            header.add(left, BorderLayout.WEST);
        }
        header.add(texts, BorderLayout.CENTER);
        header.add(createCentered(rightButton), BorderLayout.EAST);

        statusLabel.setText(" ");
        header.revalidate();
        header.repaint();
    }

    /** Envuelve un componente para centrarlo verticalmente. */
    private MyPanel createCentered(Component component) {
        MyPanel box = new MyPanel();
        box.applyLayout(new GridBagLayout());
        box.setBackgroundColor(Palette.BACKGROUND);
        box.setPreferredSize(null);
        box.add(component);
        return box;
    }

    private JScrollPane createScroll(MyPanel list) {
        // Contenedor que mantiene la lista pegada arriba aunque tenga pocas filas
        MyPanel wrapper = new MyPanel();
        wrapper.applyLayout(new BorderLayout());
        wrapper.setBackgroundColor(Palette.BACKGROUND);
        wrapper.setPreferredSize(null);
        wrapper.add(list, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(Palette.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private MyPanel createRow() {
        MyPanel row = new MyPanel();
        row.applyLayout(new GridBagLayout());
        row.setBackgroundColor(Palette.CARD);
        row.setCornerRadius(10);
        row.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

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

    private MyButton createRowButton(String text, Color color, int index, Supplier<ActionListener> listener,
            boolean highlighted) {
        MyButton button = createButton(text, Palette.TRANSPARENT, color,
                highlighted ? Palette.ACCENT : Palette.TEXT_MUTED, text.length() <= 2 ? 40 : 95);
        button.setActionCommand(String.valueOf(index));
        button.addActionListener(event -> forward(listener.get(), event));
        return button;
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

    private MyPanel createIcon(String symbol) {
        MyPanel icon = new MyPanel();
        icon.applyLayout(new GridBagLayout()); // centra el símbolo
        icon.setBackgroundColor(ICON_BOX);
        icon.setCornerRadius(8);
        icon.setPreferredSize(new Dimension(34, 34));
        icon.add(createLabel(symbol, Palette.ACCENT, Font.BOLD, 14));
        return icon;
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

    private void refreshContent() {
        content.revalidate();
        content.repaint();
    }

    private void forward(ActionListener listener, ActionEvent event) {
        if (listener != null) {
            listener.actionPerformed(event);
        }
    }


    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    public void addCreateListener(ActionListener listener) {
        createListener = listener;
    }

    public void addOpenListener(ActionListener listener) {
        openListener = listener;
    }

    public void addRenameListener(ActionListener listener) {
        renameListener = listener;
    }

    public void addDeleteListener(ActionListener listener) {
        deleteListener = listener;
    }

    public void addBackListener(ActionListener listener) {
        backListener = listener;
    }

    public void addPlayAllListener(ActionListener listener) {
        playAllListener = listener;
    }

    public void addPlaySongListener(ActionListener listener) {
        playSongListener = listener;
    }

    public void addRemoveSongListener(ActionListener listener) {
        removeSongListener = listener;
    }
}