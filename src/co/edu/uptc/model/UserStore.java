package co.edu.uptc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Guarda y consulta las credenciales (usuarios y administradores) en un archivo
 * .properties. Cada línea tiene la forma {@code rol.nombre = contraseña}.
 */
public class UserStore {

    private static final String FILE_NAME = "users.properties";
    private static final String DEFAULT_DIRECTORY = "data";

    private Properties properties;
    private File file;

    public UserStore() {
        properties = new Properties();
        file = new File(DEFAULT_DIRECTORY + File.separator + FILE_NAME);
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
            add("admin", "admin", "admin");
        }
    }

    /**
     * Escribe todas las credenciales en el archivo y crea la carpeta si hace falta.
     *
     * @throws IOException si no se puede escribir el archivo
     */
    public void save() throws IOException {
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, "Ritmo users");
        }
    }

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
     * Agrega una cuenta y guarda el archivo.
     *
     * @param role     rol de la cuenta: "user" o "admin"
     * @param name     nombre de la cuenta
     * @param password contraseña de la cuenta
     * @throws IOException si no se puede escribir el archivo
     */
    public void add(String role, String name, String password) throws IOException {
        properties.setProperty(key(role, name), password);
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
        return saved != null && saved.equals(password);
    }

    private String key(String role, String name) {
        return role + "." + name;
    }
}