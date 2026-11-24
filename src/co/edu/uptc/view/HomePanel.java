package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import co.edu.uptc.model.Song;

/** Inicio con recomendaciones de canciones, artistas y álbumes. */
public class HomePanel extends MyPanel {

    public static final int MAX_RECOMMENDATIONS = 5;

    private static final Color HOME_BACKGROUND = new Color(17, 17, 17);
    private static final Color TILE = new Color(41, 41, 41);
    private static final int ARTIST_SIZE = 126;
    private static final int ALBUM_SIZE = 110;
    private static final int CARD_GAP = 12;

    private final List<MyButton> favoriteButtons = new ArrayList<>();
    private final MyPanel artistsRow = createRow();
    private final MyPanel albumsRow = createRow();
    private final MyPanel songsList = createTransparentPanel();
    private final JLabel statusLabel = createLabel(" ", Palette.TEXT_MUTED, Font.PLAIN, 11);
    private ActionListener artistListener;
    private ActionListener albumListener;
    private ActionListener playListener;
    private ActionListener favoriteListener;

    public HomePanel() {
        setBackgroundColor(HOME_BACKGROUND);
        applyLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 23, 12, 23));

        MyPanel recommendations = createTransparentPanel();
        recommendations.applyLayout(new BoxLayout(recommendations, BoxLayout.Y_AXIS));
        recommendations.add(createSectionTitle("Recomendados"));
        recommendations.add(Box.createVerticalStrut(10));
        recommendations.add(artistsRow);
        recommendations.add(Box.createVerticalStrut(10));
        recommendations.add(createSectionTitle("Álbumes"));
        recommendations.add(Box.createVerticalStrut(8));
        recommendations.add(albumsRow);
        recommendations.add(Box.createVerticalStrut(8));
        recommendations.add(createSectionTitle("Canciones"));
        recommendations.add(Box.createVerticalStrut(8));
        recommendations.add(songsList);
        recommendations.add(statusLabel);
        artistsRow.setPreferredSize(new Dimension(0, 150));
        artistsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        albumsRow.setPreferredSize(new Dimension(0, 134));
        albumsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 134));
        songsList.applyLayout(new BoxLayout(songsList, BoxLayout.Y_AXIS));
        statusLabel.setPreferredSize(new Dimension(0, 16));
        statusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));

        JScrollPane scroll = new JScrollPane(recommendations);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(HOME_BACKGROUND);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
        showRecommendations(List.of(), "Cargando recomendaciones de Jamendo...");
    }

    private MyPanel createSectionTitle(String text) {
        MyPanel section = createTransparentPanel();
        section.applyLayout(new BorderLayout());
        section.setPreferredSize(new Dimension(0, 24));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel label = createLabel(text, Palette.TEXT, Font.BOLD, 18);
        section.add(label, BorderLayout.CENTER);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }

    private MyPanel createArtist(String name, int index, int count) {
        boolean highlighted = index == 0 || index == count - 1;
        MyButton button = createIconButton(true, highlighted ? Palette.ACCENT : TILE,
                highlighted ? Palette.TEXT_DARK : Palette.ACCENT, ARTIST_SIZE / 2);
        button.setFixedSize(ARTIST_SIZE, ARTIST_SIZE);
        button.setToolTipText("Buscar música de " + name);
        button.setActionCommand(name);
        button.addActionListener(event -> forward(artistListener, event));
        return createItem(button, name, ARTIST_SIZE, 150);
    }

    private MyPanel createAlbum(String name) {
        MyButton button = createIconButton(false, TILE, Palette.ACCENT, 8);
        button.setFixedSize(ALBUM_SIZE, ALBUM_SIZE);
        button.setToolTipText("Buscar el álbum " + name);
        button.setActionCommand(name);
        button.addActionListener(event -> forward(albumListener, event));
        return createItem(button, name, ALBUM_SIZE, 134);
    }

    private MyButton createIconButton(boolean artist, Color background, Color iconColor, int radius) {
        MyButton button = new MyButton("", background, iconColor, null) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getForeground());
                g.setStroke(new BasicStroke(artist ? 2.8f : 2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                if (artist) {
                    g.drawOval(centerX - 6, centerY - 12, 12, 12);
                    g.drawArc(centerX - 12, centerY + 2, 24, 20, 0, 180);
                } else {
                    g.drawOval(centerX - 11, centerY - 11, 22, 22);
                    g.drawOval(centerX - 3, centerY - 3, 6, 6);
                }
                g.dispose();
            }
        };
        button.setCornerRadius(radius);
        return button;
    }

    private MyPanel createItem(MyButton button, String name, int width, int height) {
        JLabel label = createLabel(name, Palette.TEXT, Font.BOLD, 12);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setToolTipText(name);

        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        MyPanel item = createTransparentPanel();
        item.applyLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setPreferredSize(new Dimension(width, height));
        item.setMaximumSize(new Dimension(width, height));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.add(button);
        item.add(Box.createVerticalStrut(8));
        item.add(label);
        return item;
    }

    private MyPanel createRow() {
        MyPanel row = createTransparentPanel();
        row.applyLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private void showSongs(List<Song> songs) {
        int visibleCount = Math.min(songs.size(), MAX_RECOMMENDATIONS);
        favoriteButtons.clear();
        songsList.removeAll();
        if (songs.isEmpty()) {
            songsList.add(createLabel("No se encontraron canciones para este género.",
                    Palette.TEXT_MUTED, Font.PLAIN, 12));
        }
        for (int i = 0; i < visibleCount; i++) {
            Song song = songs.get(i);
            MyPanel row = new MyPanel();
            row.setBackgroundColor(i % 2 == 0 ? Palette.CARD : HOME_BACKGROUND);
            row.setCornerRadius(10);
            row.applyLayout(new GridBagLayout());
            row.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            row.setPreferredSize(new Dimension(0, 40));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel number = createLabel(String.valueOf(i + 1), Palette.TEXT_MUTED, Font.PLAIN, 11);
            number.setPreferredSize(new Dimension(20, 20));

            MyPanel icon = new MyPanel();
            icon.setBackgroundColor(TILE);
            icon.setCornerRadius(7);
            icon.setPreferredSize(new Dimension(30, 30));
            icon.applyLayout(new GridBagLayout());
            icon.add(createLabel("♫", Palette.ACCENT, Font.BOLD, 13));

            JLabel title = createLabel(song.getTitle(), Palette.TEXT, Font.BOLD, 12);
            JLabel artist = createLabel(song.getArtist(), Palette.TEXT_MUTED, Font.PLAIN, 11);
            JLabel album = createLabel(song.getAlbum(), Palette.TEXT_MUTED, Font.PLAIN, 11);
            MyPanel genreTag = new MyPanel();
            genreTag.setBackgroundColor(Palette.TRANSPARENT);
            genreTag.setBorderColor(Palette.ACCENT);
            genreTag.setCornerRadius(12);
            genreTag.applyLayout(new FlowLayout(FlowLayout.CENTER, 7, 2));
            genreTag.add(createLabel(song.getGenre(), Palette.ACCENT, Font.PLAIN, 10));
            JLabel duration = createLabel(formatDuration(song.getDuration()), Palette.TEXT_MUTED, Font.PLAIN, 11);

            addSongCell(row, number, 0, 0);
            addSongCell(row, icon, 1, 0);
            addSongCell(row, title, 2, 3);
            addSongCell(row, artist, 3, 1.5);
            addSongCell(row, album, 4, 1.5);
            addSongCell(row, genreTag, 5, 0);
            MyButton favoriteButton = artistFavoriteButton(song, i);
            favoriteButtons.add(favoriteButton);
            addSongCell(row, favoriteButton, 6, 0);
            addSongCell(row, duration, 7, 0);

            attachDoubleClick(title, i);
            attachDoubleClick(artist, i);
            attachDoubleClick(album, i);

            songsList.add(row);
            songsList.add(Box.createVerticalStrut(6));
        }
        ThemeManager.refresh(songsList);
        int listHeight = visibleCount == 0 ? 20 : visibleCount * 46;
        songsList.setPreferredSize(new Dimension(0, listHeight));
        songsList.setMaximumSize(new Dimension(Integer.MAX_VALUE, listHeight));
        songsList.revalidate();
        songsList.repaint();
    }

    private void addSongCell(MyPanel row, Component component, int column, double weight) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = 0;
        constraints.weightx = weight;
        constraints.fill = weight > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new java.awt.Insets(0, 6, 0, 6);
        row.add(component, constraints);
    }

    private void attachDoubleClick(Component component, int index) {
        if (component instanceof JComponent) {
            ((JComponent) component).setToolTipText("Doble clic para reproducir");
        }
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    forward(playListener, new ActionEvent(HomePanel.this,
                            ActionEvent.ACTION_PERFORMED, String.valueOf(index)));
                }
            }
        });
    }

    private String formatDuration(double seconds) {
        int total = Math.max(0, (int) seconds);
        return String.format("%d:%02d", total / 60, total % 60);
    }

    private MyPanel createTransparentPanel() {
        MyPanel panel = new MyPanel();
        panel.setBackgroundColor(Palette.TRANSPARENT);
        panel.setPreferredSize(null);
        return panel;
    }

    private JLabel createLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    private void addItem(MyPanel row, MyPanel item) {
        if (row.getComponentCount() > 0) {
            row.add(Box.createHorizontalStrut(CARD_GAP));
        }
        row.add(item);
    }

    private void forward(ActionListener listener, ActionEvent event) {
        if (listener != null) {
            listener.actionPerformed(event);
        }
    }

    public void showRecommendations(List<Song> songs, String status) {
        Map<String, String> artists = new LinkedHashMap<>();
        Map<String, String> albums = new LinkedHashMap<>();
        for (Song song : songs) {
            addUnique(artists, song.getArtist());
            addUnique(albums, song.getAlbum());
        }

        artistsRow.removeAll();
        int artistIndex = 0;
        for (String artist : artists.values()) {
            addItem(artistsRow, createArtist(artist, artistIndex++, artists.size()));
        }
        albumsRow.removeAll();
        for (String album : albums.values()) {
            addItem(albumsRow, createAlbum(album));
        }
        showSongs(songs);
        setStatus(status);
        ThemeManager.refresh(this);
        revalidate();
        repaint();
    }

    private void addUnique(Map<String, String> values, String value) {
        if (values.size() >= MAX_RECOMMENDATIONS || value == null || value.isBlank()
                || value.equalsIgnoreCase("desconocido")
                || value.equalsIgnoreCase("sin álbum")) {
            return;
        }
        values.putIfAbsent(value.trim().toLowerCase(Locale.ROOT), value.trim());
    }

    private MyButton artistFavoriteButton(Song song, int index) {
        boolean favorite = song.isFavorite();
        MyButton button = new MyButton(favorite ? "♥" : "♡", Palette.TRANSPARENT,
                favorite ? Palette.ACCENT : Palette.TEXT_MUTED, null);
        button.setFixedSize(22, 22);
        button.setFontStyle(Font.BOLD, 14);
        button.setToolTipText(favorite ? "Quitar de favoritos" : "Agregar a favoritos");
        button.setActionCommand(String.valueOf(index));
        button.addActionListener(event -> forward(favoriteListener, event));
        return button;
    }

    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    public void addArtistListener(ActionListener listener) {
        artistListener = listener;
    }

    public void addAlbumListener(ActionListener listener) {
        albumListener = listener;
    }

    public void addPlayListener(ActionListener listener) {
        playListener = listener;
    }

    public void addFavoriteListener(ActionListener listener) {
        favoriteListener = listener;
    }

    public void setFavorite(int index, boolean favorite) {
        if (index >= 0 && index < favoriteButtons.size()) {
            MyButton button = favoriteButtons.get(index);
            button.setText(favorite ? "♥" : "♡");
            button.setForeground(favorite ? Palette.ACCENT : Palette.TEXT_MUTED);
            button.setToolTipText(favorite ? "Quitar de favoritos" : "Agregar a favoritos");
            ThemeManager.refresh(button);
        }
    }

}
