package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;


public class MusicData {

    private final List<Song> library = new ArrayList<>();
    private final List<PlayList> playlists = new ArrayList<>();
    private final List<Song> favorites = new ArrayList<>();
    /** Historial; el primero es el más reciente, igual que en HistoryManager. */
    private final List<HistoryEntry> history = new ArrayList<>();

    public List<Song> getLibrary() {
        return library;
    }

    public List<PlayList> getPlaylists() {
        return playlists;
    }

    public List<Song> getFavorites() {
        return favorites;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }
}