package co.edu.uptc.model;

public class PersistenceManager {

    private UserStore userStore;
    private RitmoSystem ritmoSystem;

    public PersistenceManager(UserStore userStore, RitmoSystem ritmoSystem) {
        this.userStore = userStore;
        this.ritmoSystem = ritmoSystem;
    }

    /**
     * Este es el método que debes llamar desde la interfaz (o el Presenter)
     * cuando el usuario elija la carpeta.
     */
    public void saveAllDataInDirectory(String directoryPath) {
        // 1. Le decimos a UserStore dónde guardar
        userStore.setDirectory(directoryPath);
        
        // 2. Le decimos a RitmoSystem dónde guardar
        ritmoSystem.setJsonDirectory(directoryPath);
        
        // 3. Ejecutamos los guardados
        try {
            userStore.save();
        } catch (Exception e) {
            System.err.println("Error guardando usuarios: " + e.getMessage());
        }
        
        ritmoSystem.saveData();
        
        System.out.println("Persistencia completada en: " + directoryPath);
    }
}