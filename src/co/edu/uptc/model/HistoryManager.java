package co.edu.uptc.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Administra el historial de reproducción. Guarda las canciones de la más
 * reciente a la más antigua y conserva como máximo {@link #MAX_ENTRIES}
 * registros.
 * @author programacion 2
 */
public class HistoryManager {

    /** Cantidad máxima de registros que se guardan. */
    public static final int MAX_ENTRIES = 100;

    /** Registros del historial; el primero es el más reciente. */
    private final List<HistoryEntry> entries;

    /**
     * Crea el historial vacío.
     */
    public HistoryManager() {
        this.entries = new ArrayList<>();
    }

    /**
     * Registra que una canción empezó a sonar en este momento. Si se llega al
     * máximo, se borra el registro más antiguo. Las canciones nulas se ignoran.
     *
     * @param song canción que se reprodujo
     */
    public void addPlayed(Song song) {
        addPlayed(song, LocalDateTime.now());
    }

    /**
     * Registra que una canción sonó en el momento indicado.
     *
     * @param song     canción que se reprodujo
     * @param playedAt fecha y hora de la reproducción
     */
    public void addPlayed(Song song, LocalDateTime playedAt) {
        if (song == null || playedAt == null) {
            return;
        }
        entries.add(0, new HistoryEntry(song, playedAt));
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
    }

    /**
     * Obtiene una copia del historial, para que la lista original no se pueda
     * modificar desde afuera.
     *
     * @return registros del historial, del más reciente al más antiguo
     */
    public List<HistoryEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * Obtiene la canción de un registro.
     *
     * @param index posición del registro, empezando en 0 (el más reciente)
     * @return la canción, o null si la posición no es válida
     */
    public Song getSongAt(int index) {
        if (index < 0 || index >= entries.size()) {
            return null;
        }
        return entries.get(index).getSong();
    }

    /**
     * Borra todos los registros del historial.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Indica si el historial no tiene registros.
     *
     * @return true si está vacío
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Cantidad de registros guardados.
     *
     * @return número de registros
     */
    public int size() {
        return entries.size();
    }
}