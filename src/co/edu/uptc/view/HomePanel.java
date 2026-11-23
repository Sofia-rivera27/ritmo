package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 * Sección de inicio: una fila de artistas recomendados y una fila de álbumes.
 * @author programacion 2
 */
public class HomePanel extends MyPanel {

    private static final Color HOME_BACKGROUND = new Color(17, 17, 17);
    private static final Color TILE = new Color(41, 41, 41);

    private static final int ARTIST_SIZE = 136;
    private static final int ARTIST_ROW_HEIGHT = 175;
    private static final int ALBUM_SIZE = 115;
    private static final int ALBUM_ROW_HEIGHT = 150;
    private static final int ITEM_SPACING = 20;

    // Nombres de prueba provisionales, los ve el usuario
    private static final String[] SAMPLE_ARTISTS = {
            "Los Cantores", "Vera Luz", "Mateo Rivas", "Duo Ámbar", "Sol y Sal", "Nose", "S", "SW" };
    private static final String[] SAMPLE_ALBUMS = {
            "Horizonte", "Ciudad", "Raíces", "Brasas", "Costa", "Eco" };

    private final List<MyButton> artistButtons = new ArrayList<>();
    private final List<MyButton> albumButtons = new ArrayList<>();

    private MyPanel artistsRow;
    private MyPanel albumsRow;

    public HomePanel() {
        setBackgroundColor(HOME_BACKGROUND);
        applyLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        artistsRow = createRow(ARTIST_ROW_HEIGHT);
        albumsRow = createRow(ALBUM_ROW_HEIGHT);

        for (String artist : SAMPLE_ARTISTS) {
            artistButtons.add(addArtist(artist));
        }
        for (String album : SAMPLE_ALBUMS) {
            albumButtons.add(addAlbum(album));
        }

        add(createTitle("Recomendados"));
        add(Box.createVerticalStrut(12));
        add(artistsRow);
        add(Box.createVerticalStrut(20));

        add(createTitle("Álbumes"));
        add(Box.createVerticalStrut(12));
        add(albumsRow);

        add(Box.createVerticalGlue()); // empuja todo hacia arriba
    }

    /**
     * Agrega un botón redondo de artista con la primera letra del nombre. Luego
     * la letra se cambia por la imagen del artista.
     */
    private MyButton addArtist(String name) {
        MyButton button = new MyButton(name.substring(0, 1), TILE, Palette.ACCENT, null);
        button.setFixedSize(ARTIST_SIZE, ARTIST_SIZE);
        button.setCornerRadius(ARTIST_SIZE / 2); // la mitad del tamaño forma un círculo
        button.setFontStyle(Font.BOLD, 44);

        addToRow(artistsRow, createItem(button, name, ARTIST_SIZE, ARTIST_ROW_HEIGHT));
        return button;
    }

    /**
     * Agrega un botón cuadrado redondeado de álbum con el símbolo de un disco.
     */
    private MyButton addAlbum(String name) {
        MyButton button = new MyButton("◉", TILE, Palette.ACCENT, null);
        button.setFixedSize(ALBUM_SIZE, ALBUM_SIZE);
        button.setCornerRadius(8);
        button.setFontStyle(Font.PLAIN, 36);

        addToRow(albumsRow, createItem(button, name, ALBUM_SIZE, ALBUM_ROW_HEIGHT));
        return button;
    }

    /** Crea un panelito con el botón arriba y el nombre debajo. */
    private MyPanel createItem(MyButton button, String name, int width, int height) {
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 13));
        nameLabel.setForeground(Palette.TEXT);

        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        MyPanel item = new MyPanel();
        item.setBackgroundColor(Palette.TRANSPARENT);
        item.applyLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setPreferredSize(width, height);
        item.setMaximumSize(new Dimension(width, height)); // para que no se estire
        item.add(button);
        item.add(Box.createVerticalStrut(10));
        item.add(nameLabel);
        return item;
    }

    /** Agrega un elemento a la fila, con espacio antes de cada uno menos el primero. */
    private void addToRow(MyPanel row, MyPanel item) {
        if (row.getComponentCount() > 0) {
            row.add(Box.createHorizontalStrut(ITEM_SPACING));
        }
        row.add(item);
    }

    /** Crea una fila horizontal, de izquierda a derecha. */
    private MyPanel createRow(int height) {
        MyPanel row = new MyPanel();
        row.setBackgroundColor(Palette.TRANSPARENT);
        row.applyLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setPreferredSize(null); // mide lo que midan sus botones
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); // no se estira hacia abajo
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JLabel createTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 20));
        title.setForeground(Palette.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        return title;
    }

    /**
     * Obtiene los botones de artistas, para que el Presenter les ponga listeners.
     *
     * @return botones de artistas en el orden en que se muestran
     */
    public List<MyButton> getArtistButtons() {
        return artistButtons;
    }

    /**
     * Obtiene los botones de álbumes, para que el Presenter les ponga listeners.
     *
     * @return botones de álbumes en el orden en que se muestran
     */
    public List<MyButton> getAlbumButtons() {
        return albumButtons;
    }
}