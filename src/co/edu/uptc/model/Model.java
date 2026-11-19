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
 */
public class Model implements IModel {

    private static final String MP3_EXTENSION = ".mp3";
    /** Se le muestra al usuario cuando la canción no tiene artista, álbum o género. */
    private static final String UNKNOWN = "Desconocido";
    private static final int LOAD_TIMEOUT_SECONDS = 10;

    private static boolean javaFxStarted = false;

    private MediaPlayer loadingPlayer;
    private List<Song> songs;

    public Model() {
        songs = new ArrayList<>();
    }

    /**
     * Busca todos los archivos .mp3 de una carpeta (incluidas sus subcarpetas)
     * y lee sus metadatos.
     *
     * @param folder carpeta que se desea revisar
     * @return canciones encontradas, o una lista vacía si la carpeta no es válida
     */
    @Override
    public List<Song> detectMp3InFolder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return new ArrayList<>();
        }
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
        return file != null && file.isFile() && file.getName().toLowerCase().endsWith(MP3_EXTENSION);
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
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) {
            seconds = 0;
        }
        return new Song(textOrDefault(metadata.get("title"), fileTitle),
                textOrDefault(metadata.get("artist"), UNKNOWN),
                textOrDefault(metadata.get("album"), UNKNOWN),
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

    @Override
    public void addSong(Song song) {
        songs.add(song);
    }

    @Override
    public void removeSong(Song song) {
        songs.remove(song);
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