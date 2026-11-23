package co.edu.uptc.model;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Validaciones de las canciones: datos obligatorios, archivo de audio, imagen
 * de portada, carpetas autorizadas, duplicados y duración. Todas las
 * validaciones lanzan IllegalArgumentException con el mensaje que ve el
 * usuario.
 * @author programacion 2
 */
public class SongValidator {

    /** Valor que se asigna cuando la canción no trae artista. */
    public static final String DEFAULT_ARTIST = "Desconocido";
    /** Valor que se asigna cuando la canción no trae álbum. */
    public static final String DEFAULT_ALBUM = "Sin álbum";

    public static final String MSG_TITLE = "El título es obligatorio.";
    public static final String MSG_GENRE = "El género es obligatorio.";
    public static final String MSG_DUPLICATE = "Esta canción ya existe en la biblioteca.";
    public static final String MSG_DUPLICATE_EDIT = "Ya existe una canción con ese título y artista.";
    public static final String MSG_FILE = "El archivo no existe o no se puede leer.";
    public static final String MSG_FOLDER = "Carpeta no autorizada: acceso bloqueado.";
    public static final String MSG_IMAGE = "La imagen de portada no existe o no es válida (.png, .jpg, .jpeg).";
    public static final String MSG_DURATION = "La duración no puede ser negativa.";
    public static final String MSG_NULL_SONG = "La canción no puede ser nula.";
    public static final String MSG_NOT_FOUND = "La canción no existe en la biblioteca.";

    private static final String MP3_EXTENSION = ".mp3";
    private static final String[] IMAGE_EXTENSIONS = { ".png", ".jpg", ".jpeg" };

    private SongValidator() {
    }

    /**
     * Quita los espacios del inicio y del final de un texto.
     *
     * @param text texto que se desea limpiar
     * @return texto sin espacios en los extremos, o "" si es nulo
     */
    public static String clean(String text) {
        return text == null ? "" : text.trim();
    }

    /**
     * Limpia un texto y, si queda vacío, devuelve el valor por defecto.
     *
     * @param text         texto que se desea limpiar
     * @param defaultValue valor que se usa si el texto es nulo o vacío
     * @return texto limpio o valor por defecto
     */
    public static String cleanOrDefault(String text, String defaultValue) {
        String cleaned = clean(text);
        return cleaned.isEmpty() ? defaultValue : cleaned;
    }

    /**
     * Prepara un texto para compararlo: sin espacios extra y sin importar
     * mayúsculas o minúsculas.
     *
     * @param text texto que se desea normalizar
     * @return texto normalizado
     */
    public static String normalize(String text) {
        return clean(text).replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Valida que el título no sea nulo, vacío ni solo espacios.
     *
     * @param title título que se desea validar
     */
    public static void validateTitle(String title) {
        if (clean(title).isEmpty()) {
            throw new IllegalArgumentException(MSG_TITLE);
        }
    }

    /**
     * Valida que el género no sea nulo ni vacío.
     *
     * @param genre género que se desea validar
     */
    public static void validateGenre(String genre) {
        if (clean(genre).isEmpty()) {
            throw new IllegalArgumentException(MSG_GENRE);
        }
    }

    /**
     * Indica si el archivo existe, se puede leer y tiene extensión .mp3, sin
     * importar mayúsculas o minúsculas.
     *
     * @param file archivo que se desea revisar
     * @return true si es un archivo .mp3 válido
     */
    public static boolean isMp3File(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead()
                && file.getName().toLowerCase().endsWith(MP3_EXTENSION);
    }

    /**
     * Valida la ruta del archivo de audio de una canción.
     *
     * @param filePath ruta del archivo .mp3
     */
    public static void validateAudioFile(String filePath) {
        if (clean(filePath).isEmpty() || !isMp3File(new File(filePath.trim()))) {
            throw new IllegalArgumentException(MSG_FILE);
        }
    }

    /**
     * Valida la imagen de portada. La portada es opcional: si es null no se
     * valida nada.
     *
     * @param image imagen de portada, o null si no tiene
     */
    public static void validateAlbumImage(File image) {
        if (image == null) {
            return;
        }
        boolean validExtension = false;
        String name = image.getName().toLowerCase();
        for (String extension : IMAGE_EXTENSIONS) {
            if (name.endsWith(extension)) {
                validExtension = true;
            }
        }
        if (!validExtension || !image.exists() || !image.isFile() || !image.canRead()) {
            throw new IllegalArgumentException(MSG_IMAGE);
        }
    }

    /**
     * Valida que la duración no sea negativa.
     *
     * @param duration duración en segundos
     */
    public static void validateDuration(double duration) {
        if (!(duration >= 0)) {
            throw new IllegalArgumentException(MSG_DURATION);
        }
    }

    /**
     * Valida todos los datos de una canción nueva: título, género, archivo de
     * audio, portada y duración.
     *
     * @param song canción que se desea validar
     */
    public static void validateSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException(MSG_NULL_SONG);
        }
        validateTitle(song.getTitle());
        validateGenre(song.getGenre());
        validateAudioFile(song.getFilePath());
        validateAlbumImage(song.getAlbumImage());
        validateDuration(song.getDuration());
    }

    /**
     * Valida que una canción nueva no exista ya en la biblioteca, ni por la
     * ruta de su archivo ni por la combinación título y artista.
     *
     * @param song    canción nueva
     * @param library canciones que ya están en la biblioteca
     */
    public static void validateNotDuplicate(Song song, List<Song> library) {
        for (Song other : library) {
            if (other.equals(song) || samePath(other.getFilePath(), song.getFilePath())) {
                throw new IllegalArgumentException(MSG_DUPLICATE);
            }
        }
    }

    /**
     * Valida que la nueva combinación título y artista no pertenezca a otra
     * canción distinta de la que se está editando.
     *
     * @param edited  canción que se está editando
     * @param title   nuevo título
     * @param artist  nuevo artista
     * @param library canciones de la biblioteca
     */
    public static void validateNotDuplicateOnEdit(Song edited, String title, String artist, List<Song> library) {
        String newTitle = normalize(title);
        String newArtist = normalize(artist);
        for (Song other : library) {
            if (other != edited && normalize(other.getTitle()).equals(newTitle)
                    && normalize(other.getArtist()).equals(newArtist)) {
                throw new IllegalArgumentException(MSG_DUPLICATE_EDIT);
            }
        }
    }

    /**
     * Valida que la carpeta sea un directorio válido y, si ya hay carpetas
     * autorizadas, que esté dentro de alguna de ellas.
     *
     * @param folder     carpeta que se desea revisar
     * @param authorized carpetas autorizadas; si está vacía no hay restricción
     */
    public static void validateFolder(File folder, List<File> authorized) {
        if (folder == null || !folder.isDirectory()) {
            throw new IllegalArgumentException(MSG_FOLDER);
        }
        if (authorized.isEmpty()) {
            return;
        }
        Path path = folder.toPath().toAbsolutePath().normalize();
        for (File allowed : authorized) {
            if (path.startsWith(allowed.toPath().toAbsolutePath().normalize())) {
                return;
            }
        }
        throw new IllegalArgumentException(MSG_FOLDER);
    }

    private static boolean samePath(String first, String second) {
        if (clean(first).isEmpty() || clean(second).isEmpty()) {
            return false;
        }
        try {
            Path a = Paths.get(first.trim()).toAbsolutePath().normalize();
            Path b = Paths.get(second.trim()).toAbsolutePath().normalize();
            return a.equals(b);
        } catch (InvalidPathException e) {
            return false;
        }
    }
}
