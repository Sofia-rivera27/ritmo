package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Administra las listas de reproducción: las crea, renombra, elimina y consulta.
 *
 * @author SofiaValentinaG
 */
public class PlaylistManager {

    /** Listas de reproducción guardadas. */
    private List<PlayList> playLists;

    /**
     * Crea el administrador sin listas de reproducción.
     */
    public PlaylistManager() {
        this.playLists = new ArrayList<>();
    }

    /**
     * Crea una lista de reproducción. El nombre no puede estar vacío, tener más
     * de 60 caracteres ni coincidir con el de otra lista.
     *
     * @param name nombre de la nueva lista de reproducción
     * @return la lista creada
     * @throws IllegalArgumentException si el nombre no es válido o ya existe
     */
    public PlayList createPlayList(String name) {
        String cleanName = PlaylistValidator.validateName(name);
        PlaylistValidator.validateNameAvailable(cleanName, playLists, null);
        PlayList playList = new PlayList(cleanName);
        playLists.add(playList);
        return playList;
    }

    /**
     * Cambia el nombre de una lista de reproducción existente.
     *
     * @param oldName nombre actual de la lista
     * @param newName nuevo nombre que se asigna
     * @throws IllegalArgumentException si la lista no existe o el nombre nuevo no es válido o ya existe
     */
    public void renamePlayList(String oldName, String newName) {
        PlayList playList = findPlayList(oldName);
        PlaylistValidator.validateExists(playList);
        String cleanName = PlaylistValidator.validateName(newName);
        PlaylistValidator.validateNameAvailable(cleanName, playLists, playList);
        playList.setName(cleanName);
    }

    /**
     * Elimina la lista de reproducción con ese nombre.
     *
     * @param name nombre de la lista que se desea eliminar
     * @throws IllegalArgumentException si la lista no existe
     */
    public void deletePlayList(String name) {
        PlayList playList = findPlayList(name);
        PlaylistValidator.validateExists(playList);
        playLists.remove(playList);
    }

    /**
     * Agrega una canción a una lista de reproducción.
     *
     * @param playListName nombre de la lista destino
     * @param song         canción que se desea agregar
     * @throws IllegalArgumentException si la lista o la canción no existen, o la canción ya está en la lista
     */
    public void addSongToPlayList(String playListName, Song song) {
        findOrFail(playListName).addSong(song);
    }

    /**
     * Quita una canción de una lista de reproducción, sin eliminarla de la biblioteca.
     *
     * @param playListName nombre de la lista
     * @param song         canción que se desea quitar
     * @throws IllegalArgumentException si la lista o la canción no existen, o la canción no está en la lista
     */
    public void removeSongFromPlayList(String playListName, Song song) {
        findOrFail(playListName).removeSong(song);
    }

    /**
     * Busca una lista por nombre, sin importar mayúsculas ni espacios extra.
     *
     * @param name nombre de la lista
     * @return la lista, o null si no existe
     */
    public PlayList findPlayList(String name) {
        String normalized = SongValidator.normalize(name);
        for (PlayList playList : playLists) {
            if (SongValidator.normalize(playList.getName()).equals(normalized)) {
                return playList;
            }
        }
        return null;
    }

    private PlayList findOrFail(String name) {
        PlayList playList = findPlayList(name);
        PlaylistValidator.validateExists(playList);
        return playList;
    }

    public List<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(List<PlayList> playLists) {
        this.playLists = playLists;
    }
}