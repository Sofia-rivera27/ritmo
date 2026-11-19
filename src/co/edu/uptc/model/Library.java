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
}