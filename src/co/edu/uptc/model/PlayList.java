package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de reproducción. Guarda canciones y permite agregarlas, eliminarlas y consultarlas.
 *
 * @author SofiaValentinaG
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
     * Obtiene esta lista de reproducción.
     *
     * @return objeto actual de PlayList
     */
    public PlayList getPlayList() {
        return this;
    }

    /**
     * Elimina las canciones que coincidan con el título y el artista.
     *
     * @param title  título de la canción que se desea eliminar
     * @param artist artista de la canción que se desea eliminar
     */
    public void removeSong(String title, String artist) {
        songs.removeIf(song -> song.getTitle().equals(title) && song.getArtist().equals(artist));
    }

    /**
     * Agrega una canción a la lista de reproducción.
     *
     * @param song canción que se desea agregar
     */
    public void addSong(Song song) {
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