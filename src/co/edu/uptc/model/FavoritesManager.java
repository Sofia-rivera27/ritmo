
package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de administrar las canciones favoritas de la biblioteca.
 * Permite agregar, eliminar, consultar y obtener las canciones favoritas.
 *
 * @author SofiaValentinaG
 * @version 1.0
 */
public class FavoritesManager {

    /**
     * Lista que almacena las canciones marcadas como favoritas.
     */
    private List<Song> favorites;

    /**
     * Constructor de la clase.
     * Inicializa la lista de canciones favoritas vacía.
     */
    public FavoritesManager() {
        super();
        this.favorites = new ArrayList<>();
    }

    /**
     * Agrega una canción a la lista de favoritos y actualiza
     * su atributo favorite a true.
     * Evita agregar canciones nulas o duplicadas.
     *
     * @param song canción que se desea marcar como favorita.
     */
    public void markFavorite(Song song) {
        if (song != null && !favorites.contains(song)) {
            favorites.add(song);
            song.setFavorite(true);
        }
    }

    /**
     * Elimina una canción de la lista de favoritos y actualiza
     * su atributo favorite a false.
     * No elimina la canción de la biblioteca.
     *
     * @param song canción que se desea quitar de favoritos.
     */
    public void unmarkFavorite(Song song) {
        if (song != null && favorites.remove(song)) {
            song.setFavorite(false);
        }
    }

    /**
     * Comprueba si una canción se encuentra en la lista
     * de canciones favoritas.
     *
     * @param song canción que se desea consultar.
     * @return true si la canción es favorita, false en caso contrario.
     */
    public boolean isFavorite(Song song) {
        return favorites.contains(song);
    }

    /**
     * Obtiene la lista de canciones favoritas.
     * Retorna una copia para evitar modificaciones directas
     * sobre la lista original.
     *
     * @return lista de canciones favoritas.
     */
    public List<Song> getFavorites() {
        return new ArrayList<>(favorites);
    }

}
