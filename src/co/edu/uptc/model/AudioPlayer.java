package co.edu.uptc.model;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Reproduce y controla archivos MP3 locales y transmisiones HTTP.
 * @author programacion 2
 */
public class AudioPlayer {

    private static final int MAX_VOLUME = 100;

    private volatile MediaPlayer mediaPlayer;
    private volatile Song currentSong;
    private volatile boolean playing;
    private volatile int progress;
    private volatile int duration;
    private volatile String lastError;
    // Evita que una respuesta tardia de una pista anterior reemplace la actual.
    private final AtomicLong playRequest = new AtomicLong();
    private Consumer<String> errorListener = message -> {
    };
    private Runnable endListener = () -> {
    };
    private int volume = 50;

    public void play(Song song) throws Exception {
        if (song == null || song.getFilePath() == null || song.getFilePath().isBlank()) {
            throw new IllegalArgumentException("La cancion no tiene una ruta de audio.");
        }
        String path = song.getFilePath();
        boolean remote = path.startsWith("http://") || path.startsWith("https://");
        if (!remote && (!new File(path).isFile() || !path.toLowerCase().endsWith(".mp3"))) {
            throw new IllegalArgumentException("Selecciona un archivo MP3 valido.");
        }

        startJavaFx();
        stop();
        long request = playRequest.incrementAndGet();
        lastError = null;
        progress = 0;
        duration = 0;
        currentSong = song;

        // JavaFX requiere crear y controlar MediaPlayer desde su propio hilo.
        Platform.runLater(() -> {
            if (request != playRequest.get()) {
                return;
            }
            try {
                String source = remote ? path : new File(path).toURI().toString();
                Media media = new Media(source);
                MediaPlayer player = new MediaPlayer(media);
                mediaPlayer = player;
                player.setVolume(volume / (double) MAX_VOLUME);
                player.setOnReady(() -> {
                    if (mediaPlayer == player && request == playRequest.get()) {
                        duration = (int) player.getTotalDuration().toSeconds();
                        player.play();
                    }
                });
                player.setOnPlaying(() -> {
                    if (request == playRequest.get()) {
                        playing = true;
                    }
                });
                player.setOnPaused(() -> {
                    if (request == playRequest.get()) {
                        playing = false;
                    }
                });
                player.setOnStopped(() -> {
                    if (request == playRequest.get()) {
                        playing = false;
                    }
                });
                player.setOnEndOfMedia(() -> {
                    if (request == playRequest.get()) {
                        playing = false;
                        endListener.run();
                    }
                });
                player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                    if (request == playRequest.get()) {
                        progress = (int) newValue.toSeconds();
                    }
                });
                player.setOnError(() -> {
                    if (mediaPlayer == player && request == playRequest.get()) {
                        reportError(player.getError());
                    }
                });
                media.setOnError(() -> {
                    if (mediaPlayer == player && request == playRequest.get()) {
                        reportError(media.getError());
                    }
                });
            } catch (RuntimeException e) {
                if (request == playRequest.get()) {
                    reportError(e);
                }
            }
        });
    }

    private static void startJavaFx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException alreadyStarted) {
            // JavaFX was started by the metadata reader or another player.
        }
        Platform.setImplicitExit(false);
    }

    private void reportError(Throwable error) {
        if (error != null) {
            lastError = error.getMessage() == null ? error.toString() : error.getMessage();
        } else {
            lastError = "No se pudo cargar el audio.";
        }
        playing = false;
        currentSong = null;
        errorListener.accept(lastError);
    }

    public void pause() {
        MediaPlayer player = mediaPlayer;
        if (player != null) {
            Platform.runLater(player::pause);
        }
    }

    public void resume() {
        MediaPlayer player = mediaPlayer;
        if (player != null) {
            Platform.runLater(player::play);
        }
    }

    public void stop() {
        playRequest.incrementAndGet();
        MediaPlayer player = mediaPlayer;
        mediaPlayer = null;
        currentSong = null;
        playing = false;
        progress = 0;
        duration = 0;
        if (player != null && Platform.isFxApplicationThread()) {
            player.stop();
            player.dispose();
        } else if (player != null) {
            Platform.runLater(() -> {
                player.stop();
                player.dispose();
            });
        }
    }

    public void togglePause() {
        if (isPlaying()) {
            pause();
        } else {
            resume();
        }
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(MAX_VOLUME, volume));
        MediaPlayer player = mediaPlayer;
        if (player != null) {
            Platform.runLater(() -> player.setVolume(this.volume / (double) MAX_VOLUME));
        }
    }

    public int getVolume() {
        return volume;
    }

    public int getProgress() {
        return progress;
    }

    public int getDuration() {
        return duration;
    }

    public void seek(int seconds) {
        MediaPlayer player = mediaPlayer;
        if (player != null) {
            Platform.runLater(() -> player.seek(Duration.seconds(Math.max(0, seconds))));
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public String getLastError() {
        return lastError;
    }

    public void setErrorListener(Consumer<String> listener) {
        errorListener = Objects.requireNonNull(listener);
    }

    public void setEndListener(Runnable listener) {
        endListener = Objects.requireNonNull(listener);
    }
}