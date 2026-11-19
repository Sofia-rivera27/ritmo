package co.edu.uptc.model;

/**
 * Cuenta de administrador.
 */
public class Admin extends User {

    public Admin(String name, String password) {
        super(name, password);
    }

    public Admin() {
        super("", "");
    }
}