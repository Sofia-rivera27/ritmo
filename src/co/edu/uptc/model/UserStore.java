package co.edu.uptc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * Guarda y consulta las cuentas (usuarios y administradores) en un archivo
 * .properties. Cada línea tiene la forma
 * {@code rol.nombre = contraseña protegida}.
 * <p>
 * Las contraseñas no se guardan tal cual: se guarda un "hash" SHA-256 con una
 * sal aleatoria ({@code sha256$sal$hash}). Así, quien abra el archivo no puede
 * leer las contraseñas.
 * <p>
 * Por defecto el archivo queda en {@code ~/.ritmo/users.properties}, fuera del
 * proyecto, para que nunca se suba a GitHub.
 * 
 * @author programacion 2
 */
public class UserStore {

	public static final String ROLE_USER = "user";
	public static final String ROLE_ADMIN = "admin";

	public static final int MIN_NAME_LENGTH = 3;
	public static final int MAX_NAME_LENGTH = 20;
	public static final int MIN_PASSWORD_LENGTH = 4;

	// Mensajes cortos: caben en la caja de error de la pantalla de inicio
	public static final String MSG_EMPTY = "Escribe tu usuario y contraseña.";
	public static final String MSG_NAME = "Usuario: 3 a 20 letras, números, . - _";
	public static final String MSG_PASSWORD = "Contraseña: mínimo " + MIN_PASSWORD_LENGTH + " caracteres.";
	public static final String MSG_CONFIRM = "Las contraseñas no coinciden.";
	public static final String MSG_EXISTS = "Ese usuario ya existe.";
    public static final String MSG_NOT_FOUND = "El usuario no existe.";
    public static final String MSG_INACTIVE = "Tu cuenta se encuentra inactiva.";
    public static final String MSG_BAD_LOGIN = "Usuario o contraseña incorrectos.";
    public static final String MSG_NOT_ADMIN = "No tienes permisos de administrador.";
    public static final String MSG_ADMIN_ACCOUNT = "Esta cuenta es de administrador.";

    // Cuenta inactiva: inactive.nombre = true
    private static final String INACTIVE_PREFIX = "inactive.";
    
	private static final String FILE_NAME = "users.properties";
	private static final String APP_FOLDER = ".ritmo";
	private static final String NAME_PATTERN = "[a-z0-9._-]{" + MIN_NAME_LENGTH + "," + MAX_NAME_LENGTH + "}";
	private static final String HASH_PREFIX = "sha256$";
	private static final int SALT_BYTES = 16;

	private final Properties properties;
	/** Sesión actual; se consulta para saber si se pueden administrar cuentas. */
	private final UserSession session;
	private File file;

	/**
	 * Crea el almacén en la ubicación por defecto:
	 * {@code ~/.ritmo/users.properties}. Con una sesión propia en la que nadie ha
	 * entrado, por lo que no se pueden hacer operaciones de administrador.
	 */
	public UserStore() {
		this(new UserSession());
	}

	/**
	 * Crea el almacén en la ubicación por defecto usando la sesión recibida para
	 * validar los permisos.
	 *
	 * @param session sesión compartida con el resto de la aplicación
	 */
	public UserStore(UserSession session) {
		properties = new Properties();
		this.session = session;
		file = new File(System.getProperty("user.home"), APP_FOLDER + File.separator + FILE_NAME);
	}

	/**
	 * Cambia la carpeta donde se guarda el archivo de credenciales.
	 *
	 * @param directoryPath ruta de la carpeta
	 */
	public void setDirectory(String directoryPath) {
		if (directoryPath != null && !directoryPath.trim().isEmpty()) {
			this.file = new File(directoryPath + File.separator + FILE_NAME);
		}
	}

	/**
	 * Carga las credenciales del archivo. Si el archivo no existe, lo crea con un
	 * administrador por defecto (admin / admin).
	 *
	 * @throws IOException si no se puede leer o escribir el archivo
	 */
	public void load() throws IOException {
		if (file.exists()) {
			try (FileInputStream in = new FileInputStream(file)) {
				properties.load(in);
			}
		} else {
			store(ROLE_ADMIN, "admin", "admin");
		}
	}

	/**
	 * Escribe todas las credenciales en el archivo y crea la carpeta si hace falta.
	 *
	 * @throws IOException si no se puede escribir el archivo
	 */
	public void save() throws IOException {
		File directory = file.getAbsoluteFile().getParentFile();
		if (directory != null && !directory.exists()) {
			directory.mkdirs();
		}
		try (FileOutputStream out = new FileOutputStream(file)) {
			properties.store(out, "Ritmo users");
		}
	}

	// ---------------------------------------------------------------
	// Registro e inicio de sesión
	// ---------------------------------------------------------------

	/**
	 * Crea una cuenta de usuario (rol "user") después de validar los datos.
	 *
	 * @param name            nombre de usuario
	 * @param password        contraseña
	 * @param confirmPassword contraseña repetida
	 * @return el nombre de usuario tal como quedó guardado (en minúsculas)
	 * @throws IllegalArgumentException si algún dato no es válido; el mensaje se le
	 *                                  puede mostrar al usuario
	 * @throws IOException              si no se puede guardar el archivo
	 */
	public String register(String name, String password, String confirmPassword) throws IOException {
		String cleanName = normalizeName(name);
		if (cleanName.isEmpty() || password == null || password.isEmpty()) {
			throw new IllegalArgumentException(MSG_EMPTY);
		}
		if (!cleanName.matches(NAME_PATTERN)) {
			throw new IllegalArgumentException(MSG_NAME);
		}
		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw new IllegalArgumentException(MSG_PASSWORD);
		}
		if (!password.equals(confirmPassword)) {
			throw new IllegalArgumentException(MSG_CONFIRM);
		}
		// El nombre no se puede repetir ni siquiera entre roles distintos
		if (exists(ROLE_USER, cleanName) || exists(ROLE_ADMIN, cleanName)) {
			throw new IllegalArgumentException(MSG_EXISTS);
		}
		// El registro público siempre crea cuentas de usuario, nunca de administrador
		store(ROLE_USER, cleanName, password);
		return cleanName;
	}

	/**
	 * Comprueba el inicio de sesión.
	 *
	 * @param role     rol elegido: {@link #ROLE_USER} o {@link #ROLE_ADMIN}
	 * @param name     nombre de usuario (no importan las mayúsculas)
	 * @param password contraseña
	 * @return true si la cuenta existe con ese rol y la contraseña es correcta
	 */
	public boolean login(String role, String name, String password) {
		return checkPassword(role, normalizeName(name), password);
	}

	/**
	 * Comprueba el inicio de sesión validando también el rol y el estado de la
	 * cuenta. El motivo del rechazo solo se da cuando la contraseña es correcta,
	 * para no revelar qué cuentas existen.
	 *
	 * @param role     rol elegido: {@link #ROLE_USER} o {@link #ROLE_ADMIN}
	 * @param name     nombre de usuario (no importan las mayúsculas)
	 * @param password contraseña
	 * @throws IllegalArgumentException si el acceso no es válido; el mensaje se le
	 *                                  puede mostrar a la persona
	 */
	public void authenticate(String role, String name, String password) {
		String cleanName = normalizeName(name);
		if (checkPassword(role, cleanName, password)) {
			if (!isActive(cleanName)) {
				throw new IllegalArgumentException(MSG_INACTIVE);
			}
			return;
		}
		// La contraseña es de una cuenta con el otro rol: se explica el motivo
		if (ROLE_ADMIN.equals(role) && checkPassword(ROLE_USER, cleanName, password)) {
			throw new IllegalArgumentException(MSG_NOT_ADMIN);
		}
		if (ROLE_USER.equals(role) && checkPassword(ROLE_ADMIN, cleanName, password)) {
			throw new IllegalArgumentException(MSG_ADMIN_ACCOUNT);
		}
		throw new IllegalArgumentException(MSG_BAD_LOGIN);
	}

	/**
	 * Nombre de usuario sin espacios al inicio ni al final y en minúsculas, para
	 * que "Santiago" y "santiago" sean la misma cuenta.
	 *
	 * @param name nombre escrito por el usuario
	 * @return nombre normalizado, o cadena vacía si es null
	 */
	public static String normalizeName(String name) {
		return name == null ? "" : name.trim().toLowerCase();
	}

	// ---------------------------------------------------------------
	// Operaciones básicas
	// ---------------------------------------------------------------

	/**
	 * Indica si ya existe una cuenta con ese rol y nombre.
	 *
	 * @param role rol de la cuenta: "user" o "admin"
	 * @param name nombre de la cuenta
	 * @return true si la cuenta existe
	 */
	public boolean exists(String role, String name) {
		return properties.containsKey(key(role, name));
	}

	/**
	 * Agrega una cuenta con el rol indicado, con la contraseña protegida, y guarda
	 * el archivo. Es una operación de administrador (puede crear cuentas
	 * administrativas), por eso se valida la sesión.
	 *
	 * @param role     rol de la cuenta: "user" o "admin"
	 * @param name     nombre de la cuenta
	 * @param password contraseña de la cuenta
	 * @throws SecurityException si quien la llama no es administrador
	 * @throws IOException       si no se puede escribir el archivo
	 */
	public void add(String role, String name, String password) throws IOException {
		session.validateAdminPermission();
		store(role, name, password);
	}

	/** Guarda la cuenta sin validar permisos: solo para uso interno del almacén. */
	private void store(String role, String name, String password) throws IOException {
		properties.setProperty(key(role, name), protect(password));
		save();
	}

	/**
	 * Verifica la contraseña de una cuenta.
	 *
	 * @param role     rol de la cuenta: "user" o "admin"
	 * @param name     nombre de la cuenta
	 * @param password contraseña que se desea comprobar
	 * @return true si la cuenta existe y la contraseña coincide
	 */
	public boolean checkPassword(String role, String name, String password) {
		String saved = properties.getProperty(key(role, name));
		if (saved == null || password == null) {
			return false;
		}
		if (!saved.startsWith(HASH_PREFIX)) {
			// Cuenta guardada antes de proteger las contraseñas (texto plano)
			return saved.equals(password);
		}
		String[] parts = saved.split("\\$");
		if (parts.length != 3) {
			return false;
		}
		try {
			byte[] salt = Base64.getDecoder().decode(parts[1]);
			return MessageDigest.isEqual(hash(salt, password), Base64.getDecoder().decode(parts[2]));
		} catch (IllegalArgumentException e) {
			return false; // línea dañada en el archivo
		}
	}
	
    // ---------------------------------------------------------------
    // Administración de cuentas (pantalla del administrador)
    // ---------------------------------------------------------------

    /**
     * Lista todas las cuentas guardadas, ordenadas por nombre.
     *
     * @return cada elemento es un arreglo {rol, nombre}
     * @throws SecurityException si quien la llama no es administrador
     */
    public List<String[]> getAccounts() {
        session.validateAdminPermission();
        List<String[]> accounts = new ArrayList<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(ROLE_USER + ".")) {
                accounts.add(new String[] { ROLE_USER, key.substring(ROLE_USER.length() + 1) });
            } else if (key.startsWith(ROLE_ADMIN + ".")) {
                accounts.add(new String[] { ROLE_ADMIN, key.substring(ROLE_ADMIN.length() + 1) });
            }
        }
        accounts.sort((first, second) -> first[1].compareTo(second[1]));
        return accounts;
    }

    /**
     * Rol de una cuenta.
     *
     * @param name nombre de la cuenta
     * @return "admin", "user" o null si no existe
     */
    public String getRole(String name) {
        if (exists(ROLE_ADMIN, name)) {
            return ROLE_ADMIN;
        }
        return exists(ROLE_USER, name) ? ROLE_USER : null;
    }

    public boolean isActive(String name) {
        return !properties.containsKey(INACTIVE_PREFIX + name);
    }

    /**
     * Crea una cuenta desde la pantalla del administrador.
     *
     * @return el nombre tal como quedó guardado
     * @throws SecurityException        si quien la llama no es administrador
     * @throws IllegalArgumentException si algún dato no es válido
     * @throws IOException              si no se puede guardar el archivo
     */
    public String createAccount(String name, String password, String role) throws IOException {
        session.validateAdminPermission();
        String cleanName = normalizeName(name);
        if (cleanName.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException(MSG_EMPTY);
        }
        if (!cleanName.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(MSG_NAME);
        }
        if (getRole(cleanName) != null) {
            throw new IllegalArgumentException(MSG_EXISTS);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(MSG_PASSWORD);
        }
        store(role, cleanName, password);
        return cleanName;
    }

    /**
     * Edita una cuenta. El nombre no cambia; si la contraseña está vacía se
     * conserva la anterior.
     *
     * @throws SecurityException        si quien la llama no es administrador
     * @throws IllegalArgumentException si algún dato no es válido
     * @throws IOException              si no se puede guardar el archivo
     */
    public void updateAccount(String name, String password, String role, boolean active)
            throws IOException {
        session.validateAdminPermission();
        String oldRole = getRole(name);
        if (oldRole == null) {
            throw new IllegalArgumentException(MSG_NOT_FOUND);
        }
        if (!password.isEmpty() && password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(MSG_PASSWORD);
        }
        if (!oldRole.equals(role)) {
            String savedPassword = properties.getProperty(key(oldRole, name));
            properties.remove(key(oldRole, name));
            properties.setProperty(key(role, name), savedPassword);
        }
        if (!password.isEmpty()) {
            properties.setProperty(key(role, name), protect(password));
        }
        if (active) {
            properties.remove(INACTIVE_PREFIX + name);
        } else {
            properties.setProperty(INACTIVE_PREFIX + name, "true");
        }
        save();
    }

    /**
     * Borra una cuenta.
     *
     * @throws SecurityException si quien la llama no es administrador
     * @throws IOException       si no se puede guardar el archivo
     */
    public void deleteAccount(String name) throws IOException {
        session.validateAdminPermission();
        properties.remove(key(ROLE_USER, name));
        properties.remove(key(ROLE_ADMIN, name));
        properties.remove(INACTIVE_PREFIX + name);
        save();
    }
    
    

	private String key(String role, String name) {
		return role + "." + name;
	}

	/** Convierte la contraseña en "sha256$sal$hash" con una sal aleatoria nueva. */
	private String protect(String password) {
		byte[] salt = new byte[SALT_BYTES];
		new SecureRandom().nextBytes(salt);
		Base64.Encoder encoder = Base64.getEncoder();
		return HASH_PREFIX + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash(salt, password));
	}

	private byte[] hash(byte[] salt, String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(salt);
			return digest.digest(password.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			// Toda instalación de Java trae SHA-256, así que esto no debería pasar
			throw new IllegalStateException("SHA-256 no está disponible", e);
		}
	}
}