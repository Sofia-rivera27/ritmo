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
     * Crea una lista de reproducción si no existe otra con el mismo nombre.
     *
     * @param name nombre de la nueva lista de reproducción
     */
    public void createPlayList(String name) {
        for (PlayList playList : playLists) {
            if (playList.getName().equals(name)) {
                return;
            }
        }
        playLists.add(new PlayList(name));
    }

    /**
     * Cambia el nombre de una lista de reproducción existente.
     *
     * @param oldName nombre actual de la lista
     * @param newName nuevo nombre que se asigna
     */
    public void renamePlayList(String oldName, String newName) {
        for (PlayList playList : playLists) {
            if (playList.getName().equals(oldName)) {
                playList.setName(newName);
                return;
            }
        }
    }

    /**
     * Elimina las listas de reproducción que coincidan con el nombre.
     *
     * @param name nombre de la lista que se desea eliminar
     */
    public void deletePlayList(String name) {
        playLists.removeIf(playList -> playList.getName().equals(name));
    }

    public List<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(List<PlayList> playLists) {
        this.playLists = playLists;
    }
}