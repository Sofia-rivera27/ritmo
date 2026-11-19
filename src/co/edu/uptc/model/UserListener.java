package co.edu.uptc.model;

/**
 * Cuenta de usuario oyente. Cada oyente tiene su propia biblioteca musical.
 */
public class UserListener extends User {

    private Library library;

    public UserListener(String name, String password, Library library) {
        super(name, password);
        this.library = library;
    }

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }
}