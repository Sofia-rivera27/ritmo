package co.edu.uptc.model;

import java.io.File;

/**
 * Guarda todos los datos de la aplicación (usuarios y bibliotecas) en la misma carpeta.
 * @author programacion 2
 */
public class PersistenceManager {

    private UserStore userStore;
    private RitmoSystem ritmoSystem;
    private File baseDirectory;
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

public void setBaseDirectory(File directory) {
    this.baseDirectory = directory;
    userStore.setDirectory(directory.getAbsolutePath());
}

public File getBaseDirectory() {
    return baseDirectory;
}

public File getUserDataFile(String role, String userName) {
    return new File(baseDirectory, "usuarios" + File.separator
            + role + "-" + userName + File.separator + "musica.properties");
}
}