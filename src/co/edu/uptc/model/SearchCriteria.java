package co.edu.uptc.model;

/**
 * Campo por el que se buscan canciones. Cada valor tiene el texto que ve el
 * usuario, así que se puede poner directamente en un JComboBox.
 * @author programacion 2
 */
public enum SearchCriteria {

    TITLE("Canción"),
    ARTIST("Artista"),
    GENRE("Género");

    private final String label;

    SearchCriteria(String label) {
        this.label = label;
    }

    /**
     * Obtiene el texto que ve el usuario.
     *
     * @return etiqueta del criterio
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}