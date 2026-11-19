package co.edu.uptc.presenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import co.edu.uptc.model.PersistenceManager;
import co.edu.uptc.model.RitmoSystem;
import co.edu.uptc.model.UserStore;
import co.edu.uptc.view.ViewFrame;

/**
 * Conecta la vista con el modelo. Recibe las acciones del usuario desde la
 * vista y decide qué hacer con ellas.
 */
public class Presenter implements ActionListener {

    private ViewFrame frame;

    private RitmoSystem ritmoSystem;
    private UserStore userStore;
    private PersistenceManager persistenceManager;

    public Presenter() {
        frame = new ViewFrame();

        ritmoSystem = new RitmoSystem();
        userStore = new UserStore();
        persistenceManager = new PersistenceManager(userStore, ritmoSystem);

        frame.addMenuListener(this);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();

        switch (command) {
            case ViewFrame.HOME:
            case ViewFrame.EXPLORE:
            case ViewFrame.LIBRARY:
            case ViewFrame.FAVORITES:
            case ViewFrame.PLAYLISTS:
            case ViewFrame.HISTORY:
            case ViewFrame.SETTINGS:
                frame.showSection(command);
                break;
            default:
                break;
        }
    }

    /**
     * Guarda todos los datos en la carpeta que eligió el usuario. Se llama, por
     * ejemplo, desde el botón "Elegir carpeta" apenas inicia la aplicación.
     *
     * @param directoryPath ruta de la carpeta elegida
     */
    public void saveInDirectory(String directoryPath) {
        persistenceManager.saveAllDataInDirectory(directoryPath);
    }
}