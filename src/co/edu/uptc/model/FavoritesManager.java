package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Administra las canciones favoritas de la biblioteca: agrega, elimina y consulta.
 *
 * @author SofiaValentinaG
 * @version 1.0
 */
public class FavoritesManager {

    /** Canciones marcadas como favoritas. */
    private List<Song> favorites;

    /**
     * Crea el administrador con la lista de favoritos vacía.
     */
    public FavoritesManager() {
        this.favorites = new ArrayList<>();
    }

    /**
     * Agrega una canción a favoritos y pone su atributo favorite en true. Ignora
     * las canciones nulas o repetidas.
     *
     * @param song canción que se desea marcar como favorita
     */
    public void markFavorite(Song song) {
        if (song != null && !favorites.contains(song)) {
            favorites.add(song);
            song.setFavorite(true);
        }
    }

    /**
     * Quita una canción de favoritos y pone su atributo favorite en false. No la
     * elimina de la biblioteca.
     *
     * @param song canción que se desea quitar de favoritos
     */
    public void unmarkFavorite(Song song) {
        if (song != null && favorites.remove(song)) {
            song.setFavorite(false);
        }
    }

    /**
     * Comprueba si una canción está en favoritos.
     *
     * @param song canción que se desea consultar
     * @return true si la canción es favorita, false en caso contrario
     */
    public boolean isFavorite(Song song) {
        return favorites.contains(song);
    }

    /**
     * Obtiene una copia de la lista de favoritos, para que la lista original
     * no se pueda modificar desde afuera.
     *
     * @return lista de canciones favoritas
     */
    public List<Song> getFavorites() {
        return new ArrayList<>(favorites);
    }
}