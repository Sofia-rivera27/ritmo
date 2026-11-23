package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de reproducción. Guarda canciones y permite agregarlas, eliminarlas y
 * consultarlas.
 *
 * @author programacion 2
 */
public class PlayList {

    /** Nombre de la lista de reproducción. */
    private String name;

    /** Canciones de la lista de reproducción. */
    private List<Song> songs;

    /**
     * Crea una lista de reproducción vacía.
     *
     * @param name nombre de la lista de reproducción
     */
    public PlayList(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    /**
     * Quita de la lista de reproducción la canción con ese título y artista.
     * Solo la quita de esta lista, no de la biblioteca.
     *
     * @param title  título de la canción que se desea quitar
     * @param artist artista de la canción que se desea quitar
     * @throws IllegalArgumentException si la canción no está en la lista
     */
    public void removeSong(String title, String artist) {
        removeSong(new Song(title, artist, null, null, null, 0, null));
    }

    /**
     * Quita una canción de la lista de reproducción. Solo la quita de esta
     * lista, no de la biblioteca.
     *
     * @param song canción que se desea quitar
     * @throws IllegalArgumentException si la canción es nula o no está en la lista
     */
    public void removeSong(Song song) {
        PlaylistValidator.validateRemoveSong(this, song);
        songs.remove(song);
    }

    /**
     * Agrega una canción a la lista de reproducción.
     *
     * @param song canción que se desea agregar
     * @throws IllegalArgumentException si la canción es nula o ya está en la lista
     */
    public void addSong(Song song) {
        PlaylistValidator.validateAddSong(this, song);
        songs.add(song);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public String toString() {
        return "PlayList [name=" + name + ", songs=" + songs + "]";
    }
}