package co.edu.uptc.model;

import java.util.ArrayList;
import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Cola simple de reproduccion. Mantiene el orden de las canciones y la
 * posicion actual sin acoplarse a Swing.
 */
public class PlaybackQueue {

    private final List<Song> songs = new ArrayList<>();
    private int currentIndex = -1;

    public boolean add(Song song) {
        if (!isPlayable(song)) {
            return false;
        }
        songs.add(song);
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        return true;
    }

    private boolean isPlayable(Song song) {
        // Acepta transmisiones HTTP(S) y archivos MP3 locales que existan.
        if (song == null || song.getFilePath() == null || song.getFilePath().isBlank()) {
            return false;
        }
        String path = song.getFilePath();
        if (path.startsWith("https://") || path.startsWith("http://")) {
            try {
                URI uri = URI.create(path);
                return uri.getHost() != null
                        && (uri.getScheme().equals("https") || uri.getScheme().equals("http"));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        File file = new File(path);
        return file.isFile() && file.getName().toLowerCase().endsWith(".mp3");
    }

    public void clear() {
        songs.clear();
        currentIndex = -1;
    }

    public int size() {
        return songs.size();
    }

    public List<Song> getSongs() {
        return new ArrayList<>(songs);
    }

    public Song getCurrent() {
        if (currentIndex >= 0 && currentIndex < songs.size()) {
            return songs.get(currentIndex);
        }
        return null;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Song next() {
        if (songs.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex + 1) % songs.size();
        return getCurrent();
    }

    public Song previous() {
        if (songs.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex - 1 + songs.size()) % songs.size();
        return getCurrent();
    }

    public boolean select(int index) {
        if (index < 0 || index >= songs.size()) {
            return false;
        }
        currentIndex = index;
        return true;
    }

    public boolean remove(int index) {
        if (index < 0 || index >= songs.size()) {
            return false;
        }
        songs.remove(index);
        if (songs.isEmpty()) {
            currentIndex = -1;
        } else if (index < currentIndex) {
            // Conserva la pista actual cuando se elimina una entrada anterior.
            currentIndex--;
        } else if (index == currentIndex) {
            currentIndex = Math.min(currentIndex, songs.size() - 1);
        }
        return true;
    }
}
