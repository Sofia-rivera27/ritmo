package co.edu.uptc.model;

import java.util.Collection;

/**
 * Validaciones de las playlists: nombre, nombres repetidos, y canciones que se
 * agregan o se quitan. Todas las validaciones lanzan IllegalArgumentException
 * con el mensaje que ve el usuario.
 */
public class PlaylistValidator {

    /** Longitud máxima del nombre de una playlist. */
    public static final int MAX_NAME_LENGTH = 60;

    public static final String MSG_EMPTY_NAME = "El nombre de la playlist no puede estar vacío.";
    public static final String MSG_LONG_NAME = "El nombre de la playlist no puede superar los "
            + MAX_NAME_LENGTH + " caracteres.";
    public static final String MSG_DUPLICATE_NAME = "Ya existe una playlist con ese nombre.";
    public static final String MSG_NULL_PLAYLIST = "La playlist no existe.";
    public static final String MSG_NULL_SONG = "La canción no existe.";
    public static final String MSG_DUPLICATE_SONG = "Esta canción ya está agregada en la playlist.";
    public static final String MSG_SONG_NOT_IN_PLAYLIST = "La canción no está en la playlist.";

    private PlaylistValidator() {
    }

    /**
     * Valida el nombre de una playlist y lo devuelve sin espacios en los extremos.
     *
     * @param name nombre que escribió el usuario
     * @return nombre limpio
     */
    public static String validateName(String name) {
        String cleaned = SongValidator.clean(name);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(MSG_EMPTY_NAME);
        }
        if (cleaned.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(MSG_LONG_NAME);
        }
        return cleaned;
    }

    /**
     * Valida que ninguna otra playlist tenga el mismo nombre, sin importar
     * mayúsculas ni espacios extra.
     *
     * @param name      nombre ya limpio
     * @param playlists playlists del usuario
     * @param ignored   playlist que se está renombrando (no cuenta como repetida), o null si se crea una nueva
     */
    public static void validateNameAvailable(String name, Collection<PlayList> playlists, PlayList ignored) {
        String normalized = SongValidator.normalize(name);
        for (PlayList other : playlists) {
            if (other != ignored && SongValidator.normalize(other.getName()).equals(normalized)) {
                throw new IllegalArgumentException(MSG_DUPLICATE_NAME);
            }
        }
    }

    /**
     * Valida que la playlist exista.
     *
     * @param playList playlist que se desea revisar
     */
    public static void validateExists(PlayList playList) {
        if (playList == null) {
            throw new IllegalArgumentException(MSG_NULL_PLAYLIST);
        }
    }

    /**
     * Valida que se pueda agregar la canción: la playlist y la canción existen
     * y la canción no está ya en la playlist.
     *
     * @param playList playlist destino
     * @param song     canción que se desea agregar
     */
    public static void validateAddSong(PlayList playList, Song song) {
        validateExists(playList);
        if (song == null) {
            throw new IllegalArgumentException(MSG_NULL_SONG);
        }
        if (playList.getSongs().contains(song)) {
            throw new IllegalArgumentException(MSG_DUPLICATE_SONG);
        }
    }

    /**
     * Valida que se pueda quitar la canción: la playlist y la canción existen
     * y la canción realmente está en la playlist.
     *
     * @param playList playlist de la que se quita
     * @param song     canción que se desea quitar
     */
    public static void validateRemoveSong(PlayList playList, Song song) {
        validateExists(playList);
        if (song == null) {
            throw new IllegalArgumentException(MSG_NULL_SONG);
        }
        if (!playList.getSongs().contains(song)) {
            throw new IllegalArgumentException(MSG_SONG_NOT_IN_PLAYLIST);
        }
    }
}
