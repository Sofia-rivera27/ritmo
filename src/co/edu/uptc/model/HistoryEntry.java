package co.edu.uptc.model;

import java.time.LocalDateTime;

/**
 * Registro de una reproducción: la canción que sonó y el momento en que
 * empezó a sonar.
 */
public class HistoryEntry {

    private final Song song;
    private final LocalDateTime playedAt;

    /**
     * Crea un registro del historial.
     *
     * @param song     canción reproducida
     * @param playedAt fecha y hora en que empezó a sonar
     */
    public HistoryEntry(Song song, LocalDateTime playedAt) {
        this.song = song;
        this.playedAt = playedAt;
    }

    public Song getSong() {
        return song;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }
}
