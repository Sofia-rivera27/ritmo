package co.edu.uptc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Biblioteca musical de un usuario. Guarda sus playlists por nombre.
 */
public class Library {

    private String name;
    private Map<String, PlayList> playlists;

    public Library(String name) {
        this.name = name;
        this.playlists = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, PlayList> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Map<String, PlayList> playlists) {
        this.playlists = playlists;
    }

    /**
     * Crea una playlist en esta biblioteca. El nombre no puede estar vacío, tener
     * más de 60 caracteres ni coincidir con el de otra playlist del usuario.
     *
     * @param name nombre de la nueva playlist
     * @return la playlist creada
     * @throws IllegalArgumentException si el nombre no es válido o ya existe
     */
    public PlayList createPlaylist(String name) {
        String cleanName = PlaylistValidator.validateName(name);
        PlaylistValidator.validateNameAvailable(cleanName, playlists.values(), null);
        PlayList playList = new PlayList(cleanName);
        playlists.put(cleanName, playList);
        return playList;
    }

    /**
     * Cambia el nombre de una playlist de esta biblioteca.
     *
     * @param oldName nombre actual
     * @param newName nombre nuevo
     * @throws IllegalArgumentException si la playlist no existe o el nombre nuevo no es válido o ya existe
     */
    public void renamePlaylist(String oldName, String newName) {
        PlayList playList = findPlaylist(oldName);
        PlaylistValidator.validateExists(playList);
        String cleanName = PlaylistValidator.validateName(newName);
        PlaylistValidator.validateNameAvailable(cleanName, playlists.values(), playList);
        playlists.values().remove(playList);
        playList.setName(cleanName);
        playlists.put(cleanName, playList);
    }

    /**
     * Elimina una playlist de esta biblioteca.
     *
     * @param name nombre de la playlist
     * @throws IllegalArgumentException si la playlist no existe
     */
    public void deletePlaylist(String name) {
        PlayList playList = findPlaylist(name);
        PlaylistValidator.validateExists(playList);
        playlists.values().remove(playList);
    }

    /**
     * Agrega una canción a una playlist de esta biblioteca.
     *
     * @param playlistName nombre de la playlist destino
     * @param song         canción que se desea agregar
     * @throws IllegalArgumentException si la playlist o la canción no existen, o la canción ya está en la playlist
     */
    public void addSongToPlaylist(String playlistName, Song song) {
        PlayList playList = findPlaylist(playlistName);
        PlaylistValidator.validateExists(playList);
        playList.addSong(song);
    }

    /**
     * Quita una canción de una playlist, sin eliminarla de la biblioteca general.
     *
     * @param playlistName nombre de la playlist
     * @param song         canción que se desea quitar
     * @throws IllegalArgumentException si la playlist o la canción no existen, o la canción no está en la playlist
     */
    public void removeSongFromPlaylist(String playlistName, Song song) {
        PlayList playList = findPlaylist(playlistName);
        PlaylistValidator.validateExists(playList);
        playList.removeSong(song);
    }

    /**
     * Busca una playlist por nombre, sin importar mayúsculas ni espacios extra.
     *
     * @param name nombre de la playlist
     * @return la playlist, o null si no existe
     */
    public PlayList findPlaylist(String name) {
        String normalized = SongValidator.normalize(name);
        for (PlayList playList : playlists.values()) {
            if (SongValidator.normalize(playList.getName()).equals(normalized)) {
                return playList;
            }
        }
        return null;
    }
}