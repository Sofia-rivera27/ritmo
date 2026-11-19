package co.edu.uptc.model;

/**
 * Guarda todos los datos de la aplicación (usuarios y bibliotecas) en la misma carpeta.
 */
public class PersistenceManager {

    private UserStore userStore;
    private RitmoSystem ritmoSystem;

    public PersistenceManager(UserStore userStore, RitmoSystem ritmoSystem) {
        this.userStore = userStore;
        this.ritmoSystem = ritmoSystem;
    }

    /**
     * Guarda todos los datos en la carpeta indicada. Se llama desde el Presenter
     * cuando el usuario elige la carpeta.
     *
     * @param directoryPath ruta de la carpeta que eligió el usuario
     */
    public void saveAllDataInDirectory(String directoryPath) {
        userStore.setDirectory(directoryPath);
        ritmoSystem.setJsonDirectory(directoryPath);

        try {
            userStore.save();
        } catch (Exception e) {
            System.err.println("Error guardando usuarios: " + e.getMessage());
        }

        ritmoSystem.saveData();

        System.out.println("Datos guardados en: " + directoryPath);
    }
}