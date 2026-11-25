package co.edu.uptc.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Catálogo de canciones. Detecta archivos .mp3, lee sus metadatos con JavaFX
 * y permite buscar entre las canciones cargadas.
 * @author programacion 2
 */
public class Model implements IModel {

    private static final String MP3_EXTENSION = ".mp3";
    /** Se le muestra al usuario cuando la canción no tiene género. */
    private static final String UNKNOWN = "Desconocido";
    private static final int LOAD_TIMEOUT_SECONDS = 10;

    private static boolean javaFxStarted = false;

    private MediaPlayer loadingPlayer;
    private List<Song> songs;
    /** Carpetas autorizadas para escanear. Si está vacía, no hay restricción. */
    private List<File> authorizedFolders;
    /** Sesión actual; se consulta antes de las operaciones de administrador. */
    private final UserSession session;

    public Model() {
        this(new UserSession());
    }

    /**
     * Crea el modelo usando la sesión recibida para validar los permisos.
     *
     * @param session sesión compartida con el resto de la aplicación
     */
    public Model(UserSession session) {
        songs = new ArrayList<>();
        authorizedFolders = new ArrayList<>();
        this.session = session;
    }

    /**
     * Comprueba que quien está en sesión sea administrador. Las operaciones
     * administrativas del modelo la llaman antes de hacer cualquier cambio.
     *
     * @throws SecurityException si no hay sesión o la cuenta es de usuario
     */
    protected void validateAdminPermission() {
        session.validateAdminPermission();
    }

    /**
     * Busca todos los archivos .mp3 de una carpeta (incluidas sus subcarpetas)
     * y lee sus metadatos.
     *
     * @param folder carpeta que se desea revisar
     * @return canciones encontradas
     * @throws IllegalArgumentException si la carpeta no es un directorio válido o
     *                                  no está autorizada
     */
    @Override
    public List<Song> detectMp3InFolder(File folder) {
        SongValidator.validateFolder(folder, authorizedFolders);
        List<File> candidates = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(folder.toPath())) {
            paths.map(Path::toFile).filter(this::hasMp3Extension).forEach(candidates::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return detectMp3(candidates);
    }

    /**
     * Lee los metadatos de los archivos .mp3 recibidos. Este método bloquea el
     * hilo, así que no se debe llamar desde el hilo de JavaFX.
     *
     * @param files archivos que se desean leer
     * @return canciones que se pudieron leer
     */
    @Override
    public List<Song> detectMp3(List<File> files) {
        checkNotFxThread();
        List<Song> found = new ArrayList<>();
        if (files == null) {
            return found;
        }
        startJavaFx();
        for (File file : files) {
            if (hasMp3Extension(file)) {
                Song song = readWithJavaFx(file);
                if (song != null) {
                    found.add(song);
                }
            }
        }
        return found;
    }

    private boolean hasMp3Extension(File file) {
        return SongValidator.isMp3File(file);
    }

    private void checkNotFxThread() {
        if (javaFxStarted && Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "detectMp3 bloquea el hilo; llamarlo desde un hilo aparte (Task o Thread), no desde el hilo de JavaFX.");
        }
    }

    private static synchronized void startJavaFx() {
        if (!javaFxStarted) {
            try {
                Platform.startup(() -> {
                });
            } catch (IllegalStateException e) {
                // JavaFX ya estaba iniciado
            }
            Platform.setImplicitExit(false);
            javaFxStarted = true;
        }
    }

    private Song readWithJavaFx(File file) {
        CompletableFuture<Song> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                Media media = new Media(file.toURI().toString());
                loadingPlayer = new MediaPlayer(media);
                loadingPlayer.setOnReady(() -> result.complete(buildSong(file, media)));
                loadingPlayer.setOnError(() -> result.complete(null));
                media.setOnError(() -> result.complete(null));
            } catch (Exception e) {
                result.complete(null);
            }
        });
        try {
            return result.get(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException | TimeoutException e) {
            return null;
        } finally {
            Platform.runLater(() -> {
                if (loadingPlayer != null) {
                    loadingPlayer.dispose();
                    loadingPlayer = null;
                }
            });
        }
    }

    private Song buildSong(File file, Media media) {
        ObservableMap<String, Object> metadata = media.getMetadata();
        String fileName = file.getName();
        String fileTitle = fileName.substring(0, fileName.length() - MP3_EXTENSION.length());

        double seconds = media.getDuration().toSeconds();
        if (Double.isNaN(seconds) || Double.isInfinite(seconds) || seconds < 0) {
            seconds = 0;
        }
        return new Song(textOrDefault(metadata.get("title"), fileTitle),
                textOrDefault(metadata.get("artist"), SongValidator.DEFAULT_ARTIST),
                textOrDefault(metadata.get("album"), SongValidator.DEFAULT_ALBUM),
                textOrDefault(metadata.get("genre"), UNKNOWN),
                null, seconds, file.getAbsolutePath());
    }

    private String textOrDefault(Object value, String defaultValue) {
        if (value == null || value.toString().isBlank()) {
            return defaultValue;
        }
        return value.toString().trim();
    }

    @Override
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * Agrega una canción a la biblioteca después de validarla: título, género,
     * archivo .mp3, portada, duración y que no esté repetida.
     *
     * @param song canción que se desea agregar
     * @throws IllegalArgumentException si algún dato no es válido o la canción ya
     *                                  existe
     */
    @Override
    public void addSong(Song song) {
        SongValidator.validateSong(song);
        SongValidator.validateNotDuplicate(song, songs);
        songs.add(song);
    }

    /**
     * Quita una canción de la biblioteca, siempre que exista en ella.
     *
     * @param song canción que se desea eliminar
     * @throws IllegalArgumentException si la canción es nula o no está en la
     *                                  biblioteca
     */
    @Override
    public void removeSong(Song song) {
        if (song == null || !songs.contains(song)) {
            throw new IllegalArgumentException(SongValidator.MSG_NOT_FOUND);
        }
        songs.remove(song);
    }

    /**
     * Edita los datos de una canción de la biblioteca. Si algo no es válido no
     * se cambia nada.
     *
     * @param song       canción que se desea editar
     * @param title      nuevo título
     * @param artist     nuevo artista (si está vacío queda "Desconocido")
     * @param album      nuevo álbum (si está vacío queda "Sin álbum")
     * @param genre      nuevo género
     * @param albumImage nueva portada, o null si no tiene
     * @throws IllegalArgumentException si algún dato no es válido o ya existe otra
     *                                  canción con ese título y artista
     */
    @Override
    public void updateSong(Song song, String title, String artist, String album, String genre, File albumImage) {
        int index = (song == null) ? -1 : songs.indexOf(song);
        if (index < 0) {
            throw new IllegalArgumentException(SongValidator.MSG_NOT_FOUND);
        }
        Song target = songs.get(index);

        String newTitle = SongValidator.clean(title);
        String newArtist = SongValidator.cleanOrDefault(artist, SongValidator.DEFAULT_ARTIST);
        SongValidator.validateTitle(newTitle);
        SongValidator.validateGenre(genre);
        SongValidator.validateAlbumImage(albumImage);
        SongValidator.validateNotDuplicateOnEdit(target, newTitle, newArtist, songs);

        target.setTitle(newTitle);
        target.setArtist(newArtist);
        target.setAlbum(album);
        target.setGenre(genre);
        target.setAlbumImage(albumImage);
    }

    /**
     * Autoriza una carpeta para escanear canciones. Desde que hay al menos una
     * carpeta autorizada, solo se pueden escanear esas carpetas y sus subcarpetas.
     *
     * @param folder carpeta que se desea autorizar
     * @throws SecurityException        si quien la llama no es administrador
     * @throws IllegalArgumentException si no es un directorio válido
     */
    @Override
    public void authorizeFolder(File folder) {
        validateAdminPermission();
        restoreAuthorizedFolder(folder);
    }

    /**
     * Vuelve a poner una carpeta que el administrador ya había autorizado. Se usa
     * al abrir la aplicación, antes de que alguien inicie sesión, para recuperar
     * la configuración guardada; por eso no valida la sesión.
     *
     * @param folder carpeta autorizada que se desea recuperar
     * @throws IllegalArgumentException si no es un directorio válido
     */
    @Override
    public void restoreAuthorizedFolder(File folder) {
        SongValidator.validateFolder(folder, new ArrayList<>());
        if (!authorizedFolders.contains(folder)) {
            authorizedFolders.add(folder);
        }
    }
    
    @Override
    public List<File> getAuthorizedFolders() {
        return authorizedFolders;
    }

    /**
     * Quita una carpeta de las autorizadas.
     *
     * @param folder carpeta que se desea quitar
     * @throws SecurityException si quien la llama no es administrador
     */
    @Override
    public void removeAuthorizedFolder(File folder) {
        validateAdminPermission();
        authorizedFolders.remove(folder);
    }

    /**
     * Cuenta los archivos .mp3 de una carpeta y sus subcarpetas, sin leerlos.
     *
     * @param folder carpeta que se desea revisar
     * @return cantidad de archivos .mp3, o 0 si la carpeta no existe
     */
    @Override
    public int countMp3InFolder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return 0;
        }
        try (Stream<Path> paths = Files.walk(folder.toPath())) {
            return (int) paths.map(Path::toFile).filter(this::hasMp3Extension).count();
        } catch (IOException e) {
            return 0;
        }
    }
    

    /**
     * Busca las canciones cargadas cuyo título, artista o género contenga el
     * texto, sin importar mayúsculas o minúsculas.
     *
     * @param text     texto que se desea buscar
     * @param criteria campo en el que se busca
     * @return canciones que coinciden
     */
    @Override
    public List<Song> searchSongs(String text, SearchCriteria criteria) {
        List<Song> results = new ArrayList<>();
        String query = text.toLowerCase().trim();

        for (Song song : songs) {
            String value;
            switch (criteria) {
                case ARTIST:
                    value = song.getArtist();
                    break;
                case GENRE:
                    value = song.getGenre();
                    break;
                default:
                    value = song.getTitle();
                    break;
            }

            if (value != null && value.toLowerCase().contains(query)) {
                results.add(song);
            }
        }
        return results;
    }
}