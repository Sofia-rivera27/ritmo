package co.edu.uptc.model;

/**
 * Sesión actual de la aplicación: guarda quién inició sesión y con qué rol.
 * Es la única fuente para saber si se puede hacer una operación de
 * administrador. La misma sesión la comparten el Presenter, el modelo y el
 * almacén de cuentas, así todos consultan el mismo usuario.
 * @author programacion 2
 */
public class UserSession {

    /** Mensaje cuando alguien sin permisos intenta una operación de administrador. */
    public static final String MSG_ACCESS_DENIED = "Acceso denegado: no tienes permisos para realizar esta acción.";

    private String userName;
    private String role;

    /**
     * Abre la sesión de una cuenta.
     *
     * @param role     rol de la cuenta: {@link UserStore#ROLE_USER} o {@link UserStore#ROLE_ADMIN}
     * @param userName nombre de la cuenta
     */
    public void start(String role, String userName) {
        this.role = role;
        this.userName = userName;
    }

    /** Cierra la sesión: después de esto nadie tiene permisos. */
    public void end() {
        role = null;
        userName = null;
    }

    /**
     * @return true si hay una cuenta con la sesión iniciada
     */
    public boolean isActive() {
        return userName != null && role != null;
    }

    /**
     * @return true solo si hay sesión y la cuenta tiene rol de administrador
     */
    public boolean isAdministrator() {
        return isActive() && UserStore.ROLE_ADMIN.equals(role);
    }

    /**
     * Comprueba que se pueda hacer una operación de administrador. Se llama al
     * inicio de cada operación administrativa (guard clause).
     *
     * @throws SecurityException si no hay sesión o la cuenta es de usuario;
     *                           el mensaje se le puede mostrar a la persona
     */
    public void validateAdminPermission() {
        if (!isAdministrator()) {
            throw new SecurityException(MSG_ACCESS_DENIED);
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }
}
