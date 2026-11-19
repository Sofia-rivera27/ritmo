package co.edu.uptc.runner;

import javax.swing.SwingUtilities;

import co.edu.uptc.presenter.Presenter;

/**
 * Punto de entrada de la aplicación.
 */
public class Run {

    public static void main(String[] args) {
        // Las ventanas de Swing se deben crear en el hilo de eventos de Swing
        SwingUtilities.invokeLater(Presenter::new);
    }
}