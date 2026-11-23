package co.edu.uptc.model;

import java.io.File;
import java.util.Objects;

/**
 * Canción con sus datos, la ruta de su archivo de audio y si está marcada
 * como favorita. Dos canciones son iguales si tienen el mismo título y
 * artista (sin importar mayúsculas ni espacios extra).
 * @author programacion 2
 */
public class Song {

    private String title;
    private String artist;
    private String album;
    private String genre;
    private File albumImage;
    private double duration;
    private String filePath;
    private boolean favorite;

    /**
     * Crea una canción.
     *
     * @param title      título de la canción
     * @param artist     nombre del artista
     * @param album      nombre del álbum
     * @param genre      género musical
     * @param albumImage imagen del álbum, o null si no tiene
     * @param duration   duración en segundos
     * @param filePath   ruta completa del archivo de audio
     */
    public Song(String title, String artist, String album, String genre, File albumImage, double duration,
            String filePath) {
        this.title = SongValidator.clean(title);
        this.artist = SongValidator.cleanOrDefault(artist, SongValidator.DEFAULT_ARTIST);
        this.album = SongValidator.cleanOrDefault(album, SongValidator.DEFAULT_ALBUM);
        this.genre = SongValidator.clean(genre);
        this.albumImage = albumImage;
        this.duration = duration;
        this.filePath = filePath;
        this.favorite = false;
    }

    /**
     * Crea una canción sin archivo de audio, usando la ruta de la imagen del álbum.
     *
     * @param title          título de la canción
     * @param artist         nombre del artista
     * @param album          nombre del álbum
     * @param genre          género musical
     * @param albumImagePath ruta de la imagen del álbum
     * @param duration       duración en segundos
     */
    public Song(String title, String artist, String album, String genre, String albumImagePath, double duration) {
        this(title, artist, album, genre,
                SongValidator.clean(albumImagePath).isEmpty() ? null : new File(albumImagePath.trim()), duration, null);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = SongValidator.clean(title);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = SongValidator.cleanOrDefault(artist, SongValidator.DEFAULT_ARTIST);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = SongValidator.cleanOrDefault(album, SongValidator.DEFAULT_ALBUM);
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = SongValidator.clean(genre);
    }

    public File getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(File albumImage) {
        this.albumImage = albumImage;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Song)) {
            return false;
        }
        Song other = (Song) obj;
        return SongValidator.normalize(title).equals(SongValidator.normalize(other.title))
                && SongValidator.normalize(artist).equals(SongValidator.normalize(other.artist));
    }

    @Override
    public int hashCode() {
        return Objects.hash(SongValidator.normalize(title), SongValidator.normalize(artist));
    }

    @Override
    public String toString() {
        return "Song [title=" + title + ", artist=" + artist + ", album=" + album + ", genre=" + genre
                + ", albumImage=" + albumImage + "]";
    }
}