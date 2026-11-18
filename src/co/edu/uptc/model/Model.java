package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

public class Model implements IModel {

    private List<Song> songs;

    public Model() {
        songs = new ArrayList<Song>();
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void removeSong(Song song) {
        songs.remove(song);
    }

    public List<Song> searchSongs(String text, String criteria) {
        List<Song> results = new ArrayList<Song>();
        String search = text.toLowerCase().trim();

        for (Song song : songs) {
            String value;
            switch (criteria) {
                case "Artista":
                    value = song.getArtist();
                    break;
                case "Género":
                    value = song.getGenre();
                    break;
                default:
                    value = song.getTitle();
                    break;
            }

            if (value != null && value.toLowerCase().contains(search)) {
                results.add(song);
            }
        }
        return results;
    }

}
