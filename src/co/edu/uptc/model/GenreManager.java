package co.edu.uptc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Administra los géneros: los crea, edita, elimina y los guarda en el archivo
 * generos.properties dentro de la carpeta de datos de Ritmo.
 * @author programacion 2
 */
public class GenreManager {

    public static final String MSG_NAME = "El nombre del género es obligatorio.";
    public static final String MSG_EXISTS = "Ese género ya existe.";
    public static final String DEFAULT_COLOR = "Amarillo";

    private static final String FILE_NAME = "generos.properties";

    private final List<Genre> genres;
    private File file;

    public GenreManager() {
        genres = new ArrayList<>();
        file = new File(FILE_NAME);
    }

    /**
     * Cambia la carpeta donde se guarda el archivo de géneros.
     *
     * @param directory carpeta de datos de Ritmo
     */
    public void setDirectory(File directory) {
        if (directory != null) {
            file = new File(directory, FILE_NAME);
        }
    }

    /**
     * Carga los géneros del archivo. Si el archivo no existe, crea unos
     * géneros iniciales.
     *
     * @throws IOException si no se puede leer el archivo
     */
    public void load() throws IOException {
        genres.clear();
        if (!file.exists()) {
            genres.add(new Genre("Pop", "Amarillo", null));
            genres.add(new Genre("Rock", "Gris", null));
            genres.add(new Genre("Electrónica", "Gris", null));
            genres.add(new Genre("Folk", "Amarillo", null));
            save();
            return;
        }
        Properties properties = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        int count = Integer.parseInt(properties.getProperty("genre.count", "0"));
        for (int i = 0; i < count; i++) {
            String name = properties.getProperty("genre." + i + ".name", "");
            String color = properties.getProperty("genre." + i + ".color", DEFAULT_COLOR);
            String image = properties.getProperty("genre." + i + ".image", "");
            if (!name.isBlank()) {
                genres.add(new Genre(name, color, image.isBlank() ? null : new File(image)));
            }
        }
    }

    /**
     * Guarda todos los géneros en el archivo.
     *
     * @throws IOException si no se puede escribir el archivo
     */
    public void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("genre.count", String.valueOf(genres.size()));
        for (int i = 0; i < genres.size(); i++) {
            Genre genre = genres.get(i);
            properties.setProperty("genre." + i + ".name", genre.getName());
            properties.setProperty("genre." + i + ".color", genre.getColor());
            properties.setProperty("genre." + i + ".image",
                    genre.getDefaultImage() == null ? "" : genre.getDefaultImage().getAbsolutePath());
        }
        File folder = file.getAbsoluteFile().getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            properties.store(writer, "Ritmo - generos");
        }
    }

    public List<Genre> getGenres() {
        return genres;
    }

    /**
     * Busca un género por nombre, sin importar mayúsculas.
     *
     * @param name nombre del género
     * @return el género, o null si no existe
     */
    public Genre findGenre(String name) {
        for (Genre genre : genres) {
            if (genre.getName().equalsIgnoreCase(SongValidator.clean(name))) {
                return genre;
            }
        }
        return null;
    }

    /**
     * Crea un género nuevo.
     *
     * @throws IllegalArgumentException si el nombre está vacío o ya existe
     */
    public Genre addGenre(String name, String color, File image) {
        String cleanName = SongValidator.clean(name);
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException(MSG_NAME);
        }
        if (findGenre(cleanName) != null) {
            throw new IllegalArgumentException(MSG_EXISTS);
        }
        SongValidator.validateAlbumImage(image);
        Genre genre = new Genre(cleanName, color, image);
        genres.add(genre);
        return genre;
    }

    /**
     * Cambia los datos de un género.
     *
     * @throws IllegalArgumentException si el nombre está vacío o es de otro género
     */
    public void updateGenre(Genre genre, String name, String color, File image) {
        String cleanName = SongValidator.clean(name);
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException(MSG_NAME);
        }
        Genre other = findGenre(cleanName);
        if (other != null && other != genre) {
            throw new IllegalArgumentException(MSG_EXISTS);
        }
        SongValidator.validateAlbumImage(image);
        genre.setName(cleanName);
        genre.setColor(color);
        genre.setDefaultImage(image);
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }

    /**
     * Agrega el género si todavía no está en la lista (por ejemplo, el género
     * de una canción escaneada).
     *
     * @param name nombre del género
     * @return true si se agregó
     */
    public boolean addIfMissing(String name) {
        if (SongValidator.clean(name).isEmpty() || findGenre(name) != null) {
            return false;
        }
        genres.add(new Genre(SongValidator.clean(name), "Gris", null));
        return true;
    }
}