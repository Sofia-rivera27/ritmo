
package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de administrar las listas de reproduccion. Permite crear,
 * renombrar, eliminar y consultar playlists.
 * 
 * @author SofiaValentinaG
 */
public class PlaylistManager {

	/** Lista que almacena las listas de reproduccion. */
	private List<PlayList> playLists;

	/**
	 * Constructor de la clase nicializa la lista de reproducción vacía.
	 */
	public PlaylistManager() {
		this.playLists = new ArrayList<>();
	}

	/**
	 * Crea una nueva lista de reproducción si no existe otra con el mismo nombre.
	 *
	 * @param name nombre de la nueva lista de reproducción.
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
	 * @param oldName nombre actual de la lista.
	 * @param newName nuevo nombre que se asignaa.
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
	 * Elimina las listas de reproducción que coincidan con el nombre proporcionado.
	 *
	 * @param name nombre de la lista que se desea eliminar.
	 */
	public void deletePlayList(String name) {
		playLists.removeIf(i -> i.getName().equals(name));
	}

	/**
	 * Obtiene la lista de todas las playlists almacenadas.
	 *
	 * @return lista de reproducción.
	 */
	public List<PlayList> getPlayLists() {
		return playLists;
	}

	/**
	 * Asigna una nueva lista de reproducción al administrador.
	 *
	 * @param playLists lista de reproducción que se desea asignar.
	 */
	public void setPlayLists(List<PlayList> playLists) {
		this.playLists = playLists;
	}

}
