package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una lista de reproduccin.
 * Permite almacenar canciones, agregarlas, eliminarlas
 * y consultar su información.
 *  @author SofiaValentinaG
 * 
 */
public class PlayList {

    /** Nombre de la lista de reproducción. */
    private String name;

    /** Lista que almacena las canciones. */
    private List<Song> songs;

    /**
     * Constructor de la clase.
     * Inicializa el nombre y crea una lista vacia de canciones.
     *
     * @param name nombre de la lista de reproducción.
     */
    public PlayList(String name) {
        this.name = name;
        songs = new ArrayList<Song>();
    }

    /**
     * Obtiene el objeto actual de la lista de reproducción.
     *
     * @return objeto actual de PlayList.
     */
    public PlayList getPlayList() {
        return this;
    }

    /**
     * Elimina las canciones que coincidan con el título y el artista.
     *
     * @param title título de la canción que se desea eliminar.
     * @param artist artista de la canción que se desea eliminar.
     */
    public void removeSong(String title, String artist) {
        songs.removeIf(song -> song.getTitle().equals(title) &&
                song.getArtist().equals(artist));
    }

    /**
     * Agrega una canción a la lista de reproducción.
     *
     * @param song canción que se desea agregar.
     */
    public void addSong(Song song) {
        songs.add(song);
    }

    /**
     * Obtiene el nombre de la lista de reproducción.
     *
     * @return nombre de la playlist.
     */
    public String getName() {
        return name;
    }

    /**
     * Modifica el nombre de la lista de reproducción.
     *
     * @param name nuevo nombre de la playlist.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene todas las canciones almacenadas en la playlist.
     *
     * @return lista de canciones.
     */
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * Asigna una nueva lista de canciones.
     *
     * @param songs lista de canciones que se desea asignar.
     */
    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    /**
     * Representa la información de la playlist como una cadena de texto.
     *
     * @return representación en texto de la playlist.
     */
    @Override
    public String toString() {
        return "PlayList [name=" + name + ", songs=" + songs.toArray() + "]";
    }
}
