package co.edu.uptc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

/**
 * Guarda y consulta las cuentas (usuarios y administradores) en un archivo
 * .properties. Cada línea tiene la forma {@code rol.nombre = contraseña protegida}.
 * <p>
 * Las contraseñas no se guardan tal cual: se guarda un "hash" SHA-256 con una
 * sal aleatoria ({@code sha256$sal$hash}). Así, quien abra el archivo no puede
 * leer las contraseñas.
 * <p>
 * Por defecto el archivo queda en {@code ~/.ritmo/users.properties}, fuera del
 * proyecto, para que nunca se suba a GitHub.
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

    private static final String FILE_NAME = "users.properties";
    private static final String APP_FOLDER = ".ritmo";
    private static final String NAME_PATTERN = "[a-z0-9._-]{" + MIN_NAME_LENGTH + "," + MAX_NAME_LENGTH + "}";
    private static final String HASH_PREFIX = "sha256$";
    private static final int SALT_BYTES = 16;

    private final Properties properties;
    private File file;

    /**
     * Crea el almacén en la ubicación por defecto: {@code ~/.ritmo/users.properties}.
     */
    public UserStore() {
        properties = new Properties();
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
     * Carga las credenciales del archivo. Si el archivo no existe, lo crea con
     * un administrador por defecto (admin / admin).
     *
     * @throws IOException si no se puede leer o escribir el archivo
     */
    public void load() throws IOException {
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
            }
        } else {
            add(ROLE_ADMIN, "admin", "admin");
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
     * @throws IllegalArgumentException si algún dato no es válido; el mensaje
     *                                  se le puede mostrar al usuario
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
        add(ROLE_USER, cleanName, password);
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
     * Agrega una cuenta, con la contraseña protegida, y guarda el archivo.
     *
     * @param role     rol de la cuenta: "user" o "admin"
     * @param name     nombre de la cuenta
     * @param password contraseña de la cuenta
     * @throws IOException si no se puede escribir el archivo
     */
    public void add(String role, String name, String password) throws IOException {
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