
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


public class Model implements IModel {
	private static final String MP3_EXTENSION = ".mp3";
	private static final String UNKNOWN = "Desconocido";
	private static final int LOAD_TIMEOUT_SECONDS = 10;
	private static boolean javaFxStarted = false;

	private MediaPlayer loadingPlayer;

	@Override
	public List<Song> detectMp3InFolder(File folder) {
		List<Song> songs = new ArrayList<>();
		if (folder == null || !folder.isDirectory()) {
			return songs;
		}
		List<File> candidates = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(folder.toPath())) {
			paths.map(Path::toFile).filter(this::hasMp3Extension).forEach(candidates::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return detectMp3(candidates);
	}

	@Override
	public List<Song> detectMp3(List<File> files) {
		checkNotFxThread();
		List<Song> songs = new ArrayList<>();
		if (files == null) {
			return songs;
		}
		startJavaFx();
		for (File file : files) {
			if (hasMp3Extension(file)) {
				Song song = readWithJavaFx(file);
				if (song != null) {
					songs.add(song);
				}
			}
		}
		return songs;
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
		String name = file.getName();
		String fileTitle = name.substring(0, name.length() - MP3_EXTENSION.length());

		double seconds = media.getDuration().toSeconds();
		if (Double.isNaN(seconds) || Double.isInfinite(seconds)) {
			seconds = 0;
		}
		return new Song(text(metadata.get("title"), fileTitle), text(metadata.get("artist"), UNKNOWN),
				text(metadata.get("album"), UNKNOWN), text(metadata.get("genre"), UNKNOWN), null, seconds,
				file.getAbsolutePath());
	}

	private String text(Object value, String defaultValue) {
		if (value == null || value.toString().isBlank()) {
			return defaultValue;
		}
		return value.toString().trim();
	}
}
