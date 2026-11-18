package co.edu.uptc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Guarda y consulta las credenciales (usuario y administrador) en un archivo
 * .properties. Cada línea tiene la forma rol.nombre = contraseña.
 */
public class UserStore {

	private Properties props;
	private File file;

	public UserStore() {
		props = new Properties();
		file = new File("data" + File.separator + "users.properties");
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
				props.load(in);
			}
		} else {
			add("admin", "admin", "admin");
		}
	}

	/**
	 * Escribe todas las credenciales en el archivo, creando la carpeta si hace falta.
	 *
	 * @throws IOException si no se puede escribir el archivo
	 */
	public void save() throws IOException {
		File dir = file.getParentFile();
		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}
		try (FileOutputStream out = new FileOutputStream(file)) {
			props.store(out, "Ritmo users");
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
		return props.containsKey(role + "." + name);
	}

	/**
	 * Agrega una cuenta y guarda el archivo.
	 *
	 * @param role rol de la cuenta: "user" o "admin"
	 * @param name nombre de la cuenta
	 * @param pass contraseña de la cuenta
	 * @throws IOException si no se puede escribir el archivo
	 */
	public void add(String role, String name, String pass) throws IOException {
		props.setProperty(role + "." + name, pass);
		save();
	}

	/**
	 * Verifica la contraseña de una cuenta.
	 *
	 * @param role rol de la cuenta: "user" o "admin"
	 * @param name nombre de la cuenta
	 * @param pass contraseña a comprobar
	 * @return true si la cuenta existe y la contraseña coincide
	 */
	public boolean checkPass(String role, String name, String pass) {
		String saved = props.getProperty(role + "." + name);
		return saved != null && saved.equals(pass);
	}
	
	// AÑADE ESTO DEBAJO DEL CONSTRUCTOR
	public void setDirectory(String directoryPath) {
		if (directoryPath != null && !directoryPath.trim().isEmpty()) {
			this.file = new File(directoryPath + File.separator + "users.properties");
		}
	}
}