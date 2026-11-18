package co.edu.uptc.presenter;

import co.edu.uptc.view.ViewFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Presenter: conecta la vista con las acciones.
 * Solo conoce a ViewFrame (la ventana); no sabe nada de botones ni paneles.
 * Escucha los botones del menu de la izquierda (los del footer todavia no).
 */
public class Presenter implements ActionListener {

    private ViewFrame frame;

    public Presenter() {
        frame = new ViewFrame();
        frame.agregarListener(this);
        frame.setVisible(true);
    }

    // Se llama cada vez que se presiona alguno de los botones del menu
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
        }
    }

    // Una funcion por cada apartado
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

    public static void main(String[] args) {
        new Presenter();
    }
}
