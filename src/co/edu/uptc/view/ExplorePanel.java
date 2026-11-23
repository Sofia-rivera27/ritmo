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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

import co.edu.uptc.model.Song;

public class ExplorePanel extends MyPanel {

    private static final String[] GENRES = { "Pop", "Rock", "Electrónica", "Folk", "Indie", "Jazz" };
    private static final Color ICON_BACKGROUND = new Color(41, 41, 41);

    private final JTextField searchField = new JTextField();
    private final MyButton artistModeButton = new MyButton("Por artista",
            Palette.CARD, Palette.TEXT, null);
    private final MyButton genreModeButton = new MyButton("Por género",
            Palette.CARD, Palette.TEXT, null);
    private final MyPanel genreOptions = new MyPanel();
    private final MyPanel songsList = new MyPanel();
    private final List<MyPanel> songRows = new ArrayList<>();
    private final List<MyButton> favoriteButtons = new ArrayList<>();
    private final JLabel resultTitle = new JLabel("Busca canciones o selecciona un género");
    private final JLabel status = new JLabel(" ");
    private final MyButton mp3Button = new MyButton("Importar MP3",
            Palette.CARD, Palette.TEXT, Palette.CARD_BORDER);

    private ActionListener searchListener;
    private ActionListener genreListener;
    private ActionListener playListener;
    private ActionListener favoriteListener;
    private ActionListener playlistListener;
    private ActionListener mp3Listener;
    private boolean genreMode = true;
    private int selectedIndex = -1;

    public ExplorePanel() {
        setBackgroundColor(Palette.BACKGROUND);
        applyLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 16, 25));

        add(createTopBar(), BorderLayout.NORTH);
        add(createResultsArea(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        updateModeButtons();
        updateGenreButtons(null);
    }

    private MyPanel createTopBar() {
        MyPanel top = new MyPanel();
        top.applyLayout(new BorderLayout(0, 18));
        top.setBackgroundColor(Palette.BACKGROUND);
        top.setPreferredSize(new Dimension(0, 340));

        MyPanel searchBar = new MyPanel();
        searchBar.applyLayout(new BorderLayout(12, 0));
        searchBar.setBackgroundColor(Palette.BACKGROUND);
        searchBar.setPreferredSize(new Dimension(0, 44));
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        MyPanel searchBox = new MyPanel();
        searchBox.applyLayout(new BorderLayout());
        searchBox.setBackgroundColor(Palette.CARD);
        searchBox.setCornerRadius(22);
        searchBox.setBorderColor(Palette.CARD_BORDER);
        searchBox.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        searchField.setText("Buscar por artista, género o canción...");
        searchField.setForeground(Palette.TEXT_MUTED);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                if (searchField.getText().equals("Buscar por artista, género o canción...")) {
                    searchField.setText("");
                    searchField.setForeground(Palette.TEXT);
                    ThemeManager.refresh(searchField);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                if (searchField.getText().isBlank()) {
                    searchField.setText("Buscar por artista, género o canción...");
                    searchField.setForeground(Palette.TEXT_MUTED);
                    ThemeManager.refresh(searchField);
                }
            }
        });
        searchField.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 13));
        searchField.setOpaque(false);
        searchField.setForeground(Palette.TEXT);
        searchField.setCaretColor(Palette.ACCENT);
        searchField.setBorder(null);
        searchField.setForeground(Palette.TEXT_MUTED);
        searchField.addActionListener(event -> fire(searchListener, searchField.getText()));
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchBox, BorderLayout.CENTER);

        MyButton avatar = new MyButton("L", Palette.ACCENT, Palette.TEXT_DARK, null);
        avatar.setFixedSize(40, 40);
        avatar.setCornerRadius(20);
        avatar.setFontStyle(Font.BOLD, 14);
        searchBar.add(avatar, BorderLayout.EAST);
        top.add(searchBar, BorderLayout.NORTH);

        MyPanel filters = new MyPanel();
        filters.applyLayout(new GridBagLayout());
        filters.setBackgroundColor(Palette.BACKGROUND);
        filters.setPreferredSize(null);

        JLabel title = createLabel("Explorar", Palette.TEXT, Font.BOLD, 20);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        GridBagConstraints filterConstraints = new GridBagConstraints();
        filterConstraints.gridx = 0;
        filterConstraints.gridy = 0;
        filterConstraints.weightx = 1;
        filterConstraints.anchor = GridBagConstraints.WEST;
        filterConstraints.fill = GridBagConstraints.HORIZONTAL;
        filterConstraints.insets = new Insets(0, 0, 12, 0);
        filters.add(title, filterConstraints);

        MyPanel modeRow = new MyPanel();
        modeRow.applyLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        modeRow.setBackgroundColor(Palette.BACKGROUND);
        artistModeButton.setFixedSize(94, 34);
        genreModeButton.setFixedSize(96, 34);
        artistModeButton.setCornerRadius(17);
        genreModeButton.setCornerRadius(17);
        artistModeButton.setActionCommand("ARTIST");
        genreModeButton.setActionCommand("GENRE");
        artistModeButton.addActionListener(event -> {
            genreMode = false;
            updateModeButtons();
            genreOptions.setVisible(false);
        });
        genreModeButton.addActionListener(event -> {
            genreMode = true;
            updateModeButtons();
            genreOptions.setVisible(true);
        });
        modeRow.add(artistModeButton);
        modeRow.add(genreModeButton);
        filterConstraints.gridy = 1;
        filterConstraints.insets = new Insets(0, 0, 12, 0);
        filters.add(modeRow, filterConstraints);

        // Los chips aplican el filtro de género seleccionado en Jamendo.
        genreOptions.applyLayout(new GridBagLayout());
        genreOptions.setBackgroundColor(Palette.BACKGROUND);
        genreOptions.setPreferredSize(new Dimension(0, 168));
        for (int i = 0; i < GENRES.length; i++) {
            String genre = GENRES[i];
            MyButton button = new MyButton(genre, Palette.CARD, Palette.TEXT, null);
            button.setFixedSize(150, 76);
            button.setCornerRadius(12);
            button.setFontStyle(Font.BOLD, 12);
            button.setActionCommand(genre);
            button.addActionListener(event -> {
                searchField.setText(genre);
                searchField.setForeground(Palette.TEXT);
                ThemeManager.refresh(searchField);
                updateGenreButtons(genre);
                fire(genreListener, genre);
            });
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = i % 4;
            constraints.gridy = i / 4;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(3, 4, 3, 8);
            genreOptions.add(button, constraints);
        }
        filterConstraints.gridy = 2;
        filterConstraints.weighty = 1;
        filterConstraints.fill = GridBagConstraints.BOTH;
        filterConstraints.insets = new Insets(0, 0, 0, 0);
        filters.add(genreOptions, filterConstraints);
        top.add(filters, BorderLayout.CENTER);
        return top;
    }

    private MyPanel createResultsArea() {
        MyPanel area = new MyPanel();
        area.applyLayout(new BorderLayout(0, 10));
        area.setBackgroundColor(Palette.BACKGROUND);

        resultTitle.setForeground(Palette.TEXT);
        resultTitle.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 16));
        resultTitle.setHorizontalAlignment(SwingConstants.LEFT);
        area.add(resultTitle, BorderLayout.NORTH);

        songsList.applyLayout(new BoxLayout(songsList, BoxLayout.Y_AXIS));
        songsList.setBackgroundColor(Palette.BACKGROUND);
        JScrollPane scroll = new JScrollPane(songsList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Palette.BACKGROUND);
        area.add(scroll, BorderLayout.CENTER);
        return area;
    }

    private MyPanel createStatusBar() {
        MyPanel footer = new MyPanel();
        footer.applyLayout(new BorderLayout());
        footer.setBackgroundColor(Palette.BACKGROUND);
        footer.setPreferredSize(new Dimension(0, 38));

        status.setForeground(Palette.TEXT_MUTED);
        mp3Button.setFixedSize(122, 30);
        mp3Button.setFontStyle(Font.PLAIN, 11);
        mp3Button.addActionListener(event -> fire(mp3Listener, ""));
        footer.add(status, BorderLayout.CENTER);
        footer.add(mp3Button, BorderLayout.EAST);
        return footer;
    }

    private MyPanel createSongRow(Song song, int index) {
        MyPanel row = new MyPanel();
        row.applyLayout(new GridBagLayout());
        row.setBackgroundColor(Palette.CARD);
        row.setCornerRadius(9);
        row.setPreferredSize(new Dimension(0, 46));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel number = createLabel(String.valueOf(index + 1), Palette.TEXT_MUTED, Font.PLAIN, 11);
        number.setPreferredSize(new Dimension(24, 20));
        addToRow(row, number, 0, 0);

        MyPanel icon = new MyPanel();
        icon.applyLayout(new GridBagLayout());
        icon.setBackgroundColor(ICON_BACKGROUND);
        icon.setCornerRadius(7);
        icon.setPreferredSize(new Dimension(34, 34));
        icon.add(createLabel("♫", Palette.ACCENT, Font.BOLD, 13));
        addToRow(row, icon, 1, 0);

        addToRow(row, createLabel(song.getTitle(), Palette.TEXT, Font.BOLD, 12), 2, 3);
        addToRow(row, createLabel(song.getArtist(), Palette.TEXT_MUTED, Font.PLAIN, 12), 3, 2);
        addToRow(row, createLabel(song.getAlbum(), Palette.TEXT_MUTED, Font.PLAIN, 12), 4, 2);
        addToRow(row, createGenreTag(song.getGenre()), 5, 0);

        MyButton favorite = new MyButton(song.isFavorite() ? "♥" : "♡",
                Palette.TRANSPARENT, song.isFavorite() ? Palette.ACCENT : Palette.TEXT_MUTED, null);
        favorite.setFixedSize(30, 30);
        favorite.setFontStyle(Font.BOLD, 14);
        favorite.addActionListener(event -> fire(favoriteListener, String.valueOf(index)));
        favoriteButtons.add(favorite);
        addToRow(row, favorite, 6, 0);

        int seconds = (int) song.getDuration();
        addToRow(row, createLabel(String.format("%d:%02d", seconds / 60, seconds % 60),
                Palette.TEXT_MUTED, Font.PLAIN, 11), 7, 0);

        MyButton playlist = new MyButton("+ Playlist", Palette.TRANSPARENT,
                Palette.TEXT, Palette.TEXT_MUTED);
        playlist.setFixedSize(82, 28);
        playlist.setFontStyle(Font.PLAIN, 10);
        playlist.addActionListener(event -> fire(playlistListener, String.valueOf(index)));
        addToRow(row, playlist, 8, 0);

        MouseListener rowListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                selectRow(index);
                if (event.getClickCount() == 2) {
                    fire(playListener, String.valueOf(index));
                }
            }
        };
        // El doble clic reproduce la fila sin añadir otro botón a la vista.
        addMouseListenerRecursively(row, rowListener);
        return row;
    }

    private void addMouseListenerRecursively(Component component, MouseListener listener) {
        component.addMouseListener(listener);
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                if (!(child instanceof MyButton)) {
                    addMouseListenerRecursively(child, listener);
                }
            }
        }
    }

    private void addToRow(MyPanel row, Component component, int column, double weight) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = 0;
        constraints.weightx = weight;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = weight > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 7, 0, 7);
        row.add(component, constraints);
    }

    private MyPanel createGenreTag(String genre) {
        MyPanel tag = new MyPanel();
        tag.applyLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
        tag.setBackgroundColor(Palette.TRANSPARENT);
        tag.setCornerRadius(10);
        tag.setBorderColor(Palette.ACCENT);
        tag.add(createLabel(genre, Palette.ACCENT, Font.PLAIN, 10));
        return tag;
    }

    private JLabel createLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    private void updateModeButtons() {
        artistModeButton.setColors(genreMode ? Palette.CARD : Palette.ACCENT,
                genreMode ? Palette.TEXT : Palette.TEXT_DARK, null);
        genreModeButton.setColors(genreMode ? Palette.ACCENT : Palette.CARD,
                genreMode ? Palette.TEXT_DARK : Palette.TEXT, null);
        // Los colores de arriba son del tema oscuro; se ajustan al tema activo
        ThemeManager.refresh(artistModeButton);
        ThemeManager.refresh(genreModeButton);
        revalidate();
        repaint();
    }

    private void updateGenreButtons(String selectedGenre) {
        for (Component component : genreOptions.getComponents()) {
            MyButton button = (MyButton) component;
            boolean selected = button.getActionCommand().equals(selectedGenre);
            button.setColors(selected ? Palette.ACCENT : Palette.CARD,
                    selected ? Palette.TEXT_DARK : Palette.TEXT, null);
        }
        ThemeManager.refresh(genreOptions);
    }

    private void selectRow(int index) {
        selectedIndex = index;
        for (int i = 0; i < songRows.size(); i++) {
            songRows.get(i).setBackgroundColor(i == index ? Palette.CARD_BORDER : Palette.CARD);
        }
        ThemeManager.refresh(songsList);
    }

    private void fire(ActionListener listener, String command) {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command));
        }
    }

    public String getQuery() {
        String query = searchField.getText().trim();
        return query.equals("Buscar por artista, género o canción...") ? "" : query;
    }

    public boolean isGenreMode() {
        return genreMode;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void addSearchListener(ActionListener listener) {
        searchListener = listener;
    }

    public void addGenreListener(ActionListener listener) {
        genreListener = listener;
    }

    public void addPlayListener(ActionListener listener) {
        playListener = listener;
    }

    public void addFavoriteListener(ActionListener listener) {
        favoriteListener = listener;
    }

    public void addPlaylistListener(ActionListener listener) {
        playlistListener = listener;
    }

    public void addMp3Listener(ActionListener listener) {
        mp3Listener = listener;
    }

    public void showResults(List<Song> songs, String title) {
        songsList.removeAll();
        songRows.clear();
        favoriteButtons.clear();
        selectedIndex = -1;
        for (int i = 0; i < songs.size(); i++) {
            MyPanel row = createSongRow(songs.get(i), i);
            songRows.add(row);
            songsList.add(row);
            songsList.add(Box.createVerticalStrut(6));
        }
        resultTitle.setText(title);
        // Las filas nuevas se crean con colores del tema oscuro
        ThemeManager.refresh(songsList);
        songsList.revalidate();
        songsList.repaint();
    }

    public void setFavorite(int index, boolean favorite) {
        if (index >= 0 && index < favoriteButtons.size()) {
            favoriteButtons.get(index).setText(favorite ? "♥" : "♡");
            favoriteButtons.get(index).setForeground(favorite ? Palette.ACCENT : Palette.TEXT_MUTED);
            ThemeManager.refresh(favoriteButtons.get(index));
        }
    }

    public void setStatus(String message) {
        status.setText(message);
    }

    public void showQueueDialog(List<String[]> songs, IntConsumer removeSong) {
        if (songs.isEmpty()) {
            setStatus("La cola esta vacia.");
            return;
        }
        JDialog dialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this),
                "Cola de reproduccion", JDialog.ModalityType.APPLICATION_MODAL);
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String[] song : songs) {
            model.addElement(song[0] + " - " + song[1]);
        }
        JList<String> queueList = new JList<>(model);
        queueList.setBackground(Palette.CARD);
        queueList.setForeground(Palette.TEXT);
        JScrollPane scroll = new JScrollPane(queueList);
        scroll.setPreferredSize(new Dimension(420, 240));
        scroll.getViewport().setBackground(Palette.CARD);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBackground(Palette.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(scroll, BorderLayout.CENTER);
        MyPanel buttons = new MyPanel();
        buttons.applyLayout(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackgroundColor(Palette.BACKGROUND);
        MyButton remove = new MyButton("Quitar seleccionada",
                Palette.CARD, Palette.TEXT, Palette.ACCENT);
        MyButton close = new MyButton("Cerrar", Palette.ACCENT, Palette.TEXT_DARK, null);
        remove.setFixedSize(160, 32);
        close.setFixedSize(90, 32);
        remove.addActionListener(event -> {
            int index = queueList.getSelectedIndex();
            if (index >= 0) {
                removeSong.accept(index);
                model.remove(index);
                setStatus("Cancion quitada de la cola.");
            }
        });
        close.addActionListener(event -> dialog.dispose());
        buttons.add(remove);
        buttons.add(close);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        ThemeManager.refresh(content);
        dialog.pack();
        dialog.setLocationRelativeTo(javax.swing.SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }
}