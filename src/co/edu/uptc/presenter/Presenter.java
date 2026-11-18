package co.edu.uptc.presenter;

import co.edu.uptc.view.ViewFrame;
import co.edu.uptc.model.RitmoSystem;
import co.edu.uptc.model.UserStore;
import co.edu.uptc.model.PersistenceManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Presenter implements ActionListener {

	private ViewFrame frame;

	private RitmoSystem ritmoSystem;
	private UserStore userStore;
	private PersistenceManager persistenceManager;

	public Presenter() {
		frame = new ViewFrame();

		this.ritmoSystem = new RitmoSystem();
		this.userStore = new UserStore();
		this.persistenceManager = new PersistenceManager(userStore, ritmoSystem);

		frame.agregarListener(this);
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String boton = e.getActionCommand();

		if (boton.equals(ViewFrame.HOME)) {
			mostrarHome();
		} else if (boton.equals(ViewFrame.EXPLORAR)) {
			mostrarExplorar();
		} else if (boton.equals(ViewFrame.BIBLIOTECA)) {
			mostrarBiblioteca();
		} else if (boton.equals(ViewFrame.PLAYLISTS)) {
			mostrarPlaylists();
		} else if (boton.equals(ViewFrame.HISTORIAL)) {
			mostrarHistorial();
		} else if (boton.equals(ViewFrame.CONFIGURACION)) {
			mostrarConfiguracion();
		} else if (boton.equals(ViewFrame.FAVORITES)) {
			mostrarFavorites();

		}
	}

	/**
	 * Este es el método que se debe llamar cuando el usuario elija la carpeta. Por
	 * ejemplo, desde el botón Elegir carpeta apenas inicie
	 */
	public void guardarEnCarpeta(String rutaElegida) {
		persistenceManager.saveAllDataInDirectory(rutaElegida);
	}

	private void mostrarHome() {
		frame.mostrarApartado(ViewFrame.HOME);
	}

	private void mostrarExplorar() {
		frame.mostrarApartado(ViewFrame.EXPLORAR);
	}

	private void mostrarBiblioteca() {
		frame.mostrarApartado(ViewFrame.BIBLIOTECA);
	}

	private void mostrarPlaylists() {
		frame.mostrarApartado(ViewFrame.PLAYLISTS);
	}

	private void mostrarHistorial() {
		frame.mostrarApartado(ViewFrame.HISTORIAL);
	}

	private void mostrarConfiguracion() {
		frame.mostrarApartado(ViewFrame.CONFIGURACION);
	}

	private void mostrarFavorites() {
		frame.mostrarApartado(ViewFrame.FAVORITES);
	}

	public static void main(String[] args) {
		new Presenter();
	}
}
