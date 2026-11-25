package co.edu.uptc.presenter;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.awt.Window;
import co.edu.uptc.view.AddSongDialog;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.time.Duration;
import co.edu.uptc.model.Genre;
import co.edu.uptc.model.GenreManager;
import co.edu.uptc.view.AdminFoldersPanel;
import co.edu.uptc.view.AdminGenresPanel;
import co.edu.uptc.view.AdminPanel;
import co.edu.uptc.view.AdminSongsPanel;
import co.edu.uptc.view.AdminUsersPanel;
import co.edu.uptc.model.AudioPlayer;
import co.edu.uptc.model.FavoritesManager;
import co.edu.uptc.model.HistoryEntry;
import co.edu.uptc.model.HistoryManager;
import co.edu.uptc.model.IModel;
import co.edu.uptc.model.JamendoService;
import co.edu.uptc.model.MusicData;
import co.edu.uptc.model.MusicDataStore;
import co.edu.uptc.model.PersistenceManager;
import co.edu.uptc.model.PlayList;
import co.edu.uptc.model.PlaybackQueue;
import co.edu.uptc.model.PlaylistManager;
import co.edu.uptc.model.RitmoSystem;
import co.edu.uptc.model.SearchCriteria;
import co.edu.uptc.model.Song;
import co.edu.uptc.model.SongValidator;
import co.edu.uptc.model.UserStore;
import co.edu.uptc.view.EditSongDialog;
import co.edu.uptc.view.ExplorePanel;
import co.edu.uptc.view.FavoritesPanel;
import co.edu.uptc.view.HistoryPanel;
import co.edu.uptc.view.HomePanel;
import co.edu.uptc.view.IFrame;
import co.edu.uptc.view.LibraryPanel;
import co.edu.uptc.view.MainPanel;
import co.edu.uptc.view.PlaylistPanel;
import co.edu.uptc.view.RegisterPanel;
import co.edu.uptc.view.SongPickerDialog;
import co.edu.uptc.view.ViewFrame;
import co.edu.uptc.view.AdminComponents;

/**
 * Coordina la vista y los servicios del modelo para búsquedas, reproducción,
 * cola y gestión de favoritos y playlists.
 * 
 * @author programacion 2
 */
public class Presenter implements ActionListener {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	/** Cada cuánto se actualiza la barra de progreso, en milisegundos. */
	private static final int PLAYER_BAR_REFRESH_MS = 500;
	private static final String VOLUME_KEY = "player.volume";

	/** La vista. El Presenter solo la conoce por la interfaz IFrame. */
	private final IFrame frame;
	/**
	 * El modelo de canciones. El Presenter solo lo conoce por la interfaz IModel.
	 */
	private final IModel model;
	private final UserStore userStore;
	private final PersistenceManager persistenceManager;
	private final JamendoService jamendoService = new JamendoService();
	private final PlaybackQueue playbackQueue = new PlaybackQueue();
	private final AudioPlayer audioPlayer = new AudioPlayer();
	private final FavoritesManager favoritesManager = new FavoritesManager();
	private final PlaylistManager playlistManager = new PlaylistManager();
	private final HistoryManager historyManager = new HistoryManager();

	// Pantalla del administrador
	private static final String ADMIN_FOLDERS_KEY = "admin.folders";
	private static final int MAX_ADMIN_ACTIVITY = 5;
	private static final DateTimeFormatter ACTIVITY_DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
	private AdminPanel adminPanel;
	private final GenreManager genreManager = new GenreManager();
	/**
	 * Lo que se ve en cada tabla del administrador, en el mismo orden que las
	 * filas.
	 */
	private final List<Song> adminSongs = new ArrayList<>();
	private final List<String> adminUsers = new ArrayList<>();
	private final List<Genre> adminGenres = new ArrayList<>();
	/** Actividad reciente: texto y hora, del más nuevo al más viejo. */
	private final List<String> adminActivityTexts = new ArrayList<>();
	private final List<LocalDateTime> adminActivityTimes = new ArrayList<>();
	private String adminName = "";
	private Song adminEditingSong;
	private Song adminSongToDelete;
	private Genre adminEditingGenre;
	/** Canciones de todas las cuentas (sin repetir) y totales para el Dashboard. */
	private final List<Song> adminAllSongs = new ArrayList<>();
	private int adminPlaylistCount;
	private int adminPlaysToday;

	/** Datos del usuario que inició sesión; null mientras nadie haya entrado. */
	private MusicDataStore musicDataStore;
	/** Preferencias del reproductor que se recuerdan entre sesiones (volumen). */
	private final Preferences preferences = Preferences.userNodeForPackage(Presenter.class);
	/** Playlist seleccionada en la sección Playlists, o null si no hay ninguna. */
	private PlayList selectedPlaylist;
	// Los botones de resultados envían índices; esta lista conserva su
	// correspondencia.
	private final List<Song> shownSongs = new ArrayList<>();
	private final Map<String, List<Song>> exploreGenreResults = new LinkedHashMap<>();
	private long exploreSearchRequestId;
	/**
	 * Canciones que se ven en Biblioteca, en el mismo orden (puede estar filtrada).
	 */
	private final List<Song> shownLibrarySongs = new ArrayList<>();
	/**
	 * Favoritas que se ven en Favoritos, en el mismo orden (puede estar filtrada).
	 */
	private final List<Song> shownFavorites = new ArrayList<>();
	/** Recomendaciones de Home, en el mismo orden que muestran sus controles. */
	private final List<Song> homeRecommendedSongs = new ArrayList<>();
	private final Map<String, List<Song>> homeRecommendationsByGenre = new LinkedHashMap<>();
	private long homeRequestId;
	private String homeRecommendationGenre = "";
	private String homeRequestGenre = "";
	private boolean homeRecommendationsLoading;
	private boolean homeRecommendationsRetryNeeded;
	/** ruta relativa */
	private static final String DATA_DIR_KEY = "data.directory";

	public Presenter() {
		// Único lugar donde se eligen las clases concretas de la vista y el modelo
		frame = new ViewFrame();
		RitmoSystem ritmoSystem = new RitmoSystem();
		model = ritmoSystem;
		userStore = new UserStore();
		persistenceManager = new PersistenceManager(userStore, ritmoSystem);

		// Centraliza aquí las conexiones para que las vistas no accedan directamente al
		// modelo.
		frame.addMenuListener(this);
		chooseDataDirectory();
		wirePlaybackControls();
		wirePlayerBar();
		wireKeyboardShortcuts();
		wireHome();
		wireExplore();
		wireFavorites();
		wireHistory();
		wirePlaylists();
		wireLibrary();
		wireLogin();
		wireAdmin();
		// Al cerrar la ventana guarda los datos del usuario que esté en sesión
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				saveMusicData();
			}
		});
		// La reproducción avisa al presentador para mostrar errores y avanzar al
		// terminar.
		audioPlayer.setErrorListener(message -> SwingUtilities
				.invokeLater(() -> frame.getExplorePanel().setStatus("Error de reproduccion: " + message)));
		audioPlayer.setEndListener(() -> SwingUtilities.invokeLater(this::playNext));
		// Lo primero que se ve es el inicio de sesión
		frame.showLogin();
		frame.setVisible(true);
	}

	// ---------------------------------------------------------------
	// Inicio de sesión y registro
	// ---------------------------------------------------------------

	private void wireLogin() {
		try {
			userStore.load();
		} catch (IOException e) {
			System.err.println("No se pudieron cargar los usuarios: " + e.getMessage());
		}
		RegisterPanel panel = frame.getRegisterPanel();
		panel.getPrimaryButton().addActionListener(event -> submitLogin());
		// Enter en cualquier campo hace lo mismo que el botón principal
		panel.getUserField().addActionListener(event -> submitLogin());
		panel.getPasswordField().addActionListener(event -> submitLogin());
		panel.getConfirmPasswordField().addActionListener(event -> submitLogin());
		panel.getSecondaryButton().addActionListener(event -> panel.setSignUpMode(!panel.isSignUpMode()));
		panel.getUserRoleButton().addActionListener(event -> panel.selectRole(false));
		panel.getAdminRoleButton().addActionListener(event -> panel.selectRole(true));
		// Los dos botones de "Cerrar sesión" (menú y Configuración) hacen lo mismo
		frame.getMainPanel().getLogoutButton().addActionListener(event -> logout());
		frame.getSettingsPanel().getLogoutButton().addActionListener(event -> logout());
		frame.getAdminPanel().getLogoutButton().addActionListener(event -> logout());
	}

	/** Botón "Ingresar" o "Registrarse", según el modo de la tarjeta. */
	private void submitLogin() {
		RegisterPanel panel = frame.getRegisterPanel();
		String name = panel.getUserName();
		String password = panel.getPasswordText();

		if (panel.isSignUpMode()) {
			try {
				String savedName = userStore.register(name, password, panel.getConfirmPasswordText());
				startSession(UserStore.ROLE_USER, savedName);
			} catch (IllegalArgumentException e) {
				panel.showMessage(e.getMessage());
			} catch (IOException e) {
				panel.showMessage("No se pudo guardar la cuenta.");
			}
			return;
		}

		if (UserStore.normalizeName(name).isEmpty() || password.isEmpty()) {
			panel.showMessage(UserStore.MSG_EMPTY);
			return;
		}
		String role = panel.isAdminSelected() ? UserStore.ROLE_ADMIN : UserStore.ROLE_USER;
		if (userStore.login(role, name, password)) {
			if (!userStore.isActive(UserStore.normalizeName(name))) {
				panel.showMessage(UserStore.MSG_INACTIVE);
				return;
			}
			startSession(role, UserStore.normalizeName(name));
		} else {
			panel.showMessage("Usuario o contraseña incorrectos.");
		}
	}

	/*
	 * Al iniciar el proyecto elige la carpeta
	 */
	private void chooseDataDirectory() {
		File folder = null;
		String saved = preferences.get(DATA_DIR_KEY, "");
		if (!saved.isEmpty() && new File(saved).isDirectory()) {
			folder = new File(saved);
		}
		boolean chosen = folder != null;
		while (!chosen) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Elige la carpeta donde se guardan los datos de Ritmo");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				folder = chooser.getSelectedFile();
				chosen = true;
			} else {
				JOptionPane.showMessageDialog(null, "Debes elegir una carpeta para continuar.");
			}
		}
		preferences.put(DATA_DIR_KEY, folder.getAbsolutePath());
		persistenceManager.setBaseDirectory(folder);
	}

	/**
	 * Abre la aplicación para un usuario: carga sus datos (cada usuario tiene su
	 * propia carpeta) y muestra Home.
	 */
	private void startSession(String role, String userName) {
		homeRequestId++;
		homeRecommendationsLoading = false;
		homeRequestGenre = "";
		homeRecommendationsRetryNeeded = false;
		homeRecommendationsByGenre.clear();
		homeRecommendedSongs.clear();
		musicDataStore = new MusicDataStore(userDataFile(role, userName));
        loadGenres();
		loadMusicData();
		frame.getMainPanel().setSessionUser(userName, UserStore.ROLE_ADMIN.equals(role));
		frame.getSettingsPanel().setAccount(userName, UserStore.ROLE_ADMIN.equals(role));
		frame.getExplorePanel().setUserName(userName);
		frame.getFavoritesPanel().setUserName(userName);
		frame.getRegisterPanel().setSignUpMode(false); // limpia los campos
		if (UserStore.ROLE_ADMIN.equals(role)) {
			frame.showAdmin();
			startAdmin(userName);
		} else {
			frame.showApp();
		}
		homeRecommendationGenre = "";
		homeRecommendedSongs.clear();
		loadHomeRecommendations(preferredHomeGenre());
	}

	/**
	 * 
	 */
	private File userDataFile(String role, String userName) {
		return persistenceManager.getUserDataFile(role, userName);
	}

	/**
	 * Guarda los datos, detiene la música, borra de la memoria todo lo del usuario
	 * y vuelve a la pantalla de inicio de sesión.
	 */
	private void logout() {
		saveMusicData();
		musicDataStore = null;
		homeRequestId++;
		homeRecommendationsLoading = false;
		homeRequestGenre = "";
		homeRecommendationsRetryNeeded = false;
		homeRecommendationGenre = "";
		homeRecommendationsByGenre.clear();
		homeRecommendedSongs.clear();
		frame.getHomePanel().showRecommendations(List.of(), "Inicia sesión para ver recomendaciones.");
		exploreSearchRequestId++;
		exploreGenreResults.clear();

		audioPlayer.stop();
		updatePlayerBar();
		playbackQueue.clear();
		frame.getMainPanel().getPlayPauseButton().setText("▶");
		updateQueueControls();

		model.getSongs().clear();
		for (Song song : favoritesManager.getFavorites()) {
			favoritesManager.unmarkFavorite(song);
		}
		playlistManager.getPlayLists().clear();
		historyManager.clear();
		selectedPlaylist = null;

		shownSongs.clear();
		frame.getExplorePanel().showResults(new ArrayList<>(), "Busca canciones o selecciona un género");
		frame.getExplorePanel().setStatus(" ");
		refreshFavorites();
		refreshHistory();
		refreshPlaylists();
		refreshLibrary();

		frame.getExplorePanel().setUserName(null);
		frame.getFavoritesPanel().setUserName(null);
		frame.getSettingsPanel().setAccount(null, false);
		frame.showLogin();
	}

	private void wirePlaybackControls() {
		// Los controles del footer actúan sobre la pista actual y la cola compartida.
		frame.getMainPanel().getPlayPauseButton().addActionListener(event -> {
			if (audioPlayer.isPlaying()) {
				audioPlayer.pause();
				frame.getMainPanel().getPlayPauseButton().setText("▶");
			} else if (audioPlayer.getCurrentSong() != null) {
				audioPlayer.resume();
				frame.getMainPanel().getPlayPauseButton().setText("⏸");
			} else {
				playCurrent();
			}

		});
		frame.getMainPanel().getNextButton().addActionListener(event -> playNext());
		frame.getMainPanel().getPreviousButton().addActionListener(event -> playPrevious());
		frame.getMainPanel().getQueueButton().addActionListener(event -> frame.getExplorePanel()
				.showQueueDialog(queueRows(), index -> removeFromQueue(index, frame.getExplorePanel())));
		updateQueueControls();
	}

	private void wireHome() {
		HomePanel panel = frame.getHomePanel();
		panel.addArtistListener(event -> searchFromHome(event.getActionCommand()));
		panel.addAlbumListener(event -> searchFromHome(event.getActionCommand()));
		panel.addFavoriteListener(event -> {
			int index = parseIndex(event.getActionCommand());
			if (index < 0 || index >= homeRecommendedSongs.size()) {
				panel.setStatus("No se pudo identificar la canción recomendada.");
				return;
			}
			Song song = homeRecommendedSongs.get(index);
			if (favoritesManager.isFavorite(song)) {
				favoritesManager.unmarkFavorite(song);
			} else {
				favoritesManager.markFavorite(song);
			}
			panel.setFavorite(index, favoritesManager.isFavorite(song));
			refreshFavorites();
			saveMusicData();
		});
		panel.addPlayListener(event -> {
			int index = parseIndex(event.getActionCommand());
			if (index < 0 || index >= homeRecommendedSongs.size()) {
				panel.setStatus("No se pudo identificar la canción recomendada.");
				return;
			}
			Song song = homeRecommendedSongs.get(index);
			if (addToQueue(song, frame.getExplorePanel(), true)) {
				panel.setStatus("Reproduciendo: " + song.getTitle() + " · " + song.getArtist());
			} else {
				panel.setStatus("No se pudo reproducir \"" + song.getTitle() + "\".");
			}
		});
	}

	private void loadHomeRecommendations(String genre) {
		if (musicDataStore == null) {
			return;
		}
		String selectedGenre = genre == null || genre.isBlank() ? "Pop" : genre.trim();
		if (homeRecommendationsLoading && selectedGenre.equalsIgnoreCase(homeRequestGenre)) {
			return;
		}
		long requestId = ++homeRequestId;
		homeRecommendationsLoading = true;
		homeRequestGenre = selectedGenre;
		HomePanel panel = frame.getHomePanel();
		panel.showRecommendations(homeRecommendedSongs,
				"Buscando recomendaciones de " + selectedGenre + " en Jamendo...");
		new Thread(() -> {
			try {
				List<Song> found = jamendoService.recommendByGenre(selectedGenre);
				SwingUtilities.invokeLater(() -> {
					if (requestId != homeRequestId || musicDataStore == null) {
						return;
					}
					homeRecommendationsLoading = false;
					homeRequestGenre = "";
					if (found.isEmpty()) {
						homeRecommendationsRetryNeeded = true;
						List<Song> previous = homeRecommendationsByGenre.get(normalizeGenreKey(selectedGenre));
						if (previous == null || previous.isEmpty()) {
							previous = homeRecommendedSongs;
						}
						String status = previous.isEmpty()
								? "Jamendo no encontró canciones para " + selectedGenre
										+ ". Se volverá a intentar al regresar a Home."
								: "Jamendo no respondió con resultados para " + selectedGenre
										+ "; se conservan las recomendaciones anteriores.";
						setHomeRecommendations(previous, status);
						return;
					}
					for (Song song : found) {
						song.setFavorite(favoritesManager.isFavorite(song));
					}
					homeRecommendationsByGenre.put(normalizeGenreKey(selectedGenre), new ArrayList<>(found));
					homeRecommendationGenre = selectedGenre;
					homeRecommendationsRetryNeeded = false;
					setHomeRecommendations(found, "Recomendaciones de " + selectedGenre + " · "
							+ Math.min(found.size(), HomePanel.MAX_RECOMMENDATIONS) + " canciones de Jamendo.");
				});
			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> {
					if (requestId == homeRequestId && musicDataStore != null) {
						homeRecommendationsLoading = false;
						homeRequestGenre = "";
						homeRecommendationsRetryNeeded = true;
						panel.setStatus("No se pudieron cargar recomendaciones de Jamendo: " + e.getMessage());
					}
				});
			}
		}, "jamendo-home-recommendations").start();
	}

	private void setHomeRecommendations(List<Song> songs, String status) {
		homeRecommendedSongs.clear();
		homeRecommendedSongs.addAll(songs.subList(0, Math.min(songs.size(), HomePanel.MAX_RECOMMENDATIONS)));
		frame.getHomePanel().showRecommendations(homeRecommendedSongs, status);
	}

	private String normalizeGenreKey(String genre) {
		return SongValidator.normalize(genre);
	}

	private String preferredHomeGenre() {
		Map<String, Integer> playsByGenre = new LinkedHashMap<>();
		Map<String, String> genreNames = new LinkedHashMap<>();
		for (HistoryEntry entry : historyManager.getEntries()) {
			String genre = SongValidator.clean(entry.getSong().getGenre());
			if (!genre.isBlank() && !genre.equalsIgnoreCase("desconocido")) {
				String key = SongValidator.normalize(genre);
				playsByGenre.merge(key, 1, Integer::sum);
				genreNames.putIfAbsent(key, genre);
			}
		}
		String preferred = "Pop";
		int mostPlayed = 0;
		for (Map.Entry<String, Integer> entry : playsByGenre.entrySet()) {
			if (entry.getValue() > mostPlayed) {
				mostPlayed = entry.getValue();
				preferred = genreNames.get(entry.getKey());
			}
		}
		return mostPlayed >= 3 ? preferred : "Pop";
	}

	private void updateHomeGenrePreference() {
		String preferred = preferredHomeGenre();
		if (!preferred.equalsIgnoreCase(homeRecommendationGenre)) {
			loadHomeRecommendations(preferred);
		}
	}

	private void searchFromHome(String query) {
		frame.getExplorePanel().setSearchQuery(query);
		frame.showSection(IFrame.EXPLORE);
		searchJamendo(frame.getExplorePanel(), query, false);
	}

	/**
	 * Conecta "Sonando ahora", la barra de progreso y el volumen. Un Timer de Swing
	 * revisa el reproductor dos veces por segundo y actualiza la barra.
	 */
	private void wirePlayerBar() {
		MainPanel bar = frame.getMainPanel();

		// Volumen guardado de la sesión anterior (50 si es la primera vez)
		int volume = preferences.getInt(VOLUME_KEY, audioPlayer.getVolume());
		audioPlayer.setVolume(volume);
		bar.setVolume(volume);
		bar.addVolumeListener(value -> {
			audioPlayer.setVolume(value);
			preferences.putInt(VOLUME_KEY, value);
		});

		// Al soltar la barra de progreso, salta a ese segundo
		bar.addSeekListener(seconds -> {
			audioPlayer.seek(seconds);
			bar.showProgress(seconds, currentDuration());
		});

		Timer timer = new Timer(PLAYER_BAR_REFRESH_MS, event -> updatePlayerBar());
		timer.start();
		updatePlayerBar();
	}

	private void updatePlayerBar() {
		MainPanel bar = frame.getMainPanel();
		Song song = audioPlayer.getCurrentSong();
		if (song == null) {
			bar.showNowPlaying(null, null);
			return;
		}
		bar.showNowPlaying(song.getTitle(), song.getArtist());
		bar.showProgress(audioPlayer.getProgress(), currentDuration());
	}

	/**
	 * Duración de la canción actual. Mientras el audio carga, el reproductor
	 * todavía no la sabe, así que se usa la que trae la canción.
	 */
	private int currentDuration() {
		if (audioPlayer.getDuration() > 0) {
			return audioPlayer.getDuration();
		}
		Song song = audioPlayer.getCurrentSong();
		return song == null ? 0 : (int) song.getDuration();
	}

	private void wireExplore() {
		// Traduce las acciones de Explorar en búsquedas y operaciones sobre cada
		// canción.
		ExplorePanel panel = frame.getExplorePanel();
		panel.addSearchListener(event -> searchJamendo(panel));
		panel.addMp3Listener(event -> selectMp3Files(panel));
		panel.addGenreListener(event -> searchJamendoByGenre(panel, event.getActionCommand()));
		panel.addPlayListener(event -> {
			Song song = getSongAt(panel, event.getActionCommand());
			if (song != null) {
				addToQueue(song, panel, true);
			}
		});
		panel.addFavoriteListener(event -> {
			Song song = getSongAt(panel, event.getActionCommand());
			if (song != null) {
				if (favoritesManager.isFavorite(song)) {
					favoritesManager.unmarkFavorite(song);
				} else {
					favoritesManager.markFavorite(song);
				}
				panel.setFavorite(Integer.parseInt(event.getActionCommand()), song.isFavorite());
				refreshFavorites();
				saveMusicData();
			}
		});
		panel.addPlaylistListener(event -> {
			Song song = getSongAt(panel, event.getActionCommand());
			if (song != null) {
				addToPlaylist(song, panel::setStatus);
			}
		});
	}

	private void wireFavorites() {
		// Mantiene la lista de favoritos actualizada y permite reproducirla como una
		// cola.
		FavoritesPanel panel = frame.getFavoritesPanel();
		panel.addSearchListener(event -> refreshFavorites());
		panel.addRemoveFavoriteListener(event -> {
			// El índice es el de la fila en la lista que se ve (puede estar filtrada)
			int index = parseIndex(event.getActionCommand());
			if (index >= 0 && index < shownFavorites.size()) {
				favoritesManager.unmarkFavorite(shownFavorites.get(index));
				refreshFavorites();
				saveMusicData();
			}
		});
		panel.addPlayAllListener(event -> {
			// Reproduce las favoritas que se están viendo, con o sin filtro
			playbackQueue.clear();
			for (Song song : shownFavorites) {
				playbackQueue.add(song);
			}
			updateQueueControls();
			playCurrent();
		});
		refreshFavorites();
	}

	private void wireHistory() {
		// Permite volver a reproducir una canción del historial o borrarlo completo.
		HistoryPanel panel = frame.getHistoryPanel();
		panel.addPlayListener(event -> {
			int index;
			try {
				index = Integer.parseInt(event.getActionCommand());
			} catch (NumberFormatException e) {
				return;
			}
			Song song = historyManager.getSongAt(index);
			if (song != null) {
				addToQueue(song, frame.getExplorePanel(), true);
			}
		});
		panel.addClearListener(event -> {
			int answer = JOptionPane.showConfirmDialog(frame.getDialogParent(),
					"¿Seguro que quieres eliminar todo el historial de reproducciones?\nEsta acción no se puede deshacer.",
					"Eliminar historial", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				historyManager.clear();
				refreshHistory();
				saveMusicData();
				loadHomeRecommendations(preferredHomeGenre());
			}
		});
	}

	private void searchJamendo(ExplorePanel panel) {
		String query = panel.getQuery();
		if (query.isEmpty()) {
			panel.setStatus("Escribe un artista o una cancion para buscar.");
			return;
		}
		searchJamendo(panel, query, panel.isGenreMode());
	}

	private void searchJamendoByGenre(ExplorePanel panel, String genre) {
		searchJamendo(panel, genre, true);
	}

	private void searchJamendo(ExplorePanel panel, String query, boolean byGenre) {
		long requestId = ++exploreSearchRequestId;
		panel.setStatus("Buscando en Jamendo...");
		// La consulta de red corre fuera del hilo de Swing para no bloquear la vista.
		new Thread(() -> {
			try {
				List<Song> found = byGenre ? jamendoService.searchByGenre(query) : jamendoService.search(query);
				SwingUtilities.invokeLater(() -> {
					if (requestId != exploreSearchRequestId) {
						return;
					}
					if (!found.isEmpty()) {
						if (byGenre) {
							exploreGenreResults.put(normalizeGenreKey(query), new ArrayList<>(found));
						}
						String title = byGenre ? "Resultados: género " + query + " · " + found.size()
								: "Resultados: " + query + " · " + found.size();
						showSongs(panel, found, title, " ");
						return;
					}

					List<Song> previous = byGenre ? exploreGenreResults.get(normalizeGenreKey(query)) : null;
					if (previous != null && !previous.isEmpty()) {
						showSongs(panel, previous, "Resultados guardados: género " + query + " · " + previous.size(),
								"Jamendo no devolvió resultados nuevos; se muestran los últimos resultados disponibles.");
					} else {
						String title = byGenre ? "Resultados: género " + query + " · 0"
								: "Resultados: " + query + " · 0";
						showSongs(panel, found, title,
								"No se encontraron canciones. Puedes volver a intentarlo más tarde.");
					}
				});
			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> {
					if (requestId == exploreSearchRequestId) {
						panel.setStatus("Error de Jamendo: " + e.getMessage());
					}
				});
			}
		}, "jamendo-search").start();
	}

	private void selectMp3Files(ExplorePanel panel) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(new FileNameExtensionFilter("Archivos MP3", "mp3"));
		if (chooser.showOpenDialog(frame.getDialogParent()) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File[] files = chooser.getSelectedFiles();
		if (files.length == 0 && chooser.getSelectedFile() != null) {
			files = new File[] { chooser.getSelectedFile() };
		}
		List<File> selected = List.of(files);
		panel.setStatus("Leyendo archivos MP3...");
		// Leer los metadatos es lento, por eso se hace en otro hilo. Agregar las
		// canciones a la biblioteca se hace después en el hilo de Swing.
		new Thread(() -> {
			try {
				List<Song> found = model.detectMp3(selected);
				SwingUtilities.invokeLater(() -> addImportedSongs(panel, found));
			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> panel.setStatus("No se pudieron leer los MP3: " + e.getMessage()));
			}
		}, "mp3-metadata").start();
	}

	/**
	 * Agrega a la biblioteca las canciones leídas. Si una ya estaba, se usa la que
	 * ya existía en vez de lanzar un error, así importar el mismo archivo dos veces
	 * no deja la aplicación colgada.
	 */
	private void addImportedSongs(ExplorePanel panel, List<Song> found) {
		List<Song> results = new ArrayList<>();
		int added = 0;
		int repeated = 0;
		int invalid = 0;
		for (Song song : found) {
			Song existing = findInLibrary(song);
			if (existing != null) {
				results.add(existing);
				repeated++;
				continue;
			}
			try {
				model.addSong(song);
				results.add(song);
				added++;
			} catch (IllegalArgumentException e) {
				invalid++;
			}
		}
		if (added > 0) {
			saveMusicData();
		}
		String title = "Resultados: archivos MP3 · " + results.size();
		String message = importMessage(found.size(), added, repeated, invalid);
		showSongs(panel, results, title, message);
		refreshLibrary();
		frame.getLibraryPanel().setStatus(message);
	}

	private Song findInLibrary(Song song) {
		for (Song other : model.getSongs()) {
			if (other.equals(song) || sameText(other.getFilePath(), song.getFilePath())) {
				return other;
			}
		}
		return null;
	}

	private boolean sameText(String first, String second) {
		return first != null && second != null && first.equalsIgnoreCase(second);
	}

	private String importMessage(int read, int added, int repeated, int invalid) {
		if (read == 0) {
			return "No se pudieron leer archivos MP3.";
		}
		StringBuilder message = new StringBuilder();
		message.append(added == 1 ? "1 canción nueva" : added + " canciones nuevas");
		if (repeated > 0) {
			message.append(" · ").append(repeated == 1 ? "1 ya estaba" : repeated + " ya estaban")
					.append(" en la biblioteca");
		}
		if (invalid > 0) {
			message.append(" · ").append(invalid).append(" no válidas");
		}
		return message.toString();
	}

	private void showSongs(ExplorePanel panel, List<Song> songs, String title, String message) {
		// Guarda el mismo orden que la vista usa para resolver los índices de sus
		// acciones.
		shownSongs.clear();
		shownSongs.addAll(songs);
		panel.showResults(songs, title);
		panel.setStatus(message);
	}

	private Song getSongAt(ExplorePanel panel, String command) {
		int index;
		try {
			index = Integer.parseInt(command);
		} catch (NumberFormatException e) {
			panel.setStatus("Seleccion de cancion invalida.");
			return null;
		}
		if (index < 0 || index >= shownSongs.size()) {
			panel.setStatus("Seleccion de cancion invalida.");
			return null;
		}
		return shownSongs.get(index);
	}

	private boolean addToQueue(Song song, ExplorePanel panel, boolean playNow) {
		// Valida la pista antes de añadirla y permite iniciar inmediatamente la
		// seleccionada.
		if (!playbackQueue.add(song)) {
			panel.setStatus("No se pudo agregar: se requiere una URL o un archivo MP3 valido.");
			return false;
		}
		if (playNow) {
			playbackQueue.select(playbackQueue.size() - 1);
			playCurrent();
		} else if (audioPlayer.getCurrentSong() == null) {
			playCurrent();
		}
		updateQueueControls();
		panel.setStatus(playNow ? "Reproduciendo: " + song.getTitle() : "Agregada a la cola: " + song.getTitle());
		return true;
	}

	private void removeFromQueue(int index, ExplorePanel panel) {
		// Si se quita la pista activa, detiene el reproductor y pasa a la nueva pista
		// actual.
		boolean removedCurrent = index == playbackQueue.getCurrentIndex();
		if (!playbackQueue.remove(index)) {
			panel.setStatus("Seleccion de cola invalida.");
			return;
		}
		if (removedCurrent) {
			audioPlayer.stop();
			playCurrent();
		}
		updateQueueControls();
	}

	private List<String[]> queueRows() {
		List<String[]> rows = new ArrayList<>();
		for (Song song : playbackQueue.getSongs()) {
			rows.add(new String[] { song.getTitle(), song.getArtist() });
		}
		return rows;
	}

	private void playCurrent() {
		// Sincroniza el reproductor, el estado visual y el mensaje de la pista
		// seleccionada.
		Song song = playbackQueue.getCurrent();
		if (song == null) {
			audioPlayer.stop();
			frame.getMainPanel().getPlayPauseButton().setText("▶");
			frame.getExplorePanel().setStatus("La cola no tiene canciones.");
			updateQueueControls();
			return;
		}
		try {
			audioPlayer.play(song);
			historyManager.addPlayed(song);
			refreshHistory();
			saveMusicData();
			updateHomeGenrePreference();
			frame.getExplorePanel().setStatus("Reproduciendo: " + song.getTitle() + " - " + song.getArtist());
			frame.getMainPanel().getPlayPauseButton().setText("⏸");
			updateQueueControls();
		} catch (Exception e) {
			frame.getExplorePanel().setStatus("Error de reproduccion: " + e.getMessage());
		}
	}

	private void playNext() {
		// Al terminar o avanzar, la cola decide cuál canción queda seleccionada.
		if (playbackQueue.size() <= 1) {
			audioPlayer.stop();
			frame.getMainPanel().getPlayPauseButton().setText("▶");
			updateQueueControls();
			return;
		}
		Song song = playbackQueue.next();
		if (song != null) {
			playCurrent();
		}
	}

	private void playPrevious() {
		Song song = playbackQueue.previous();
		if (song != null) {
			playCurrent();
		}
	}

	private void updateQueueControls() {
		int count = playbackQueue.size();
		frame.getMainPanel().getNextButton().setToolTipText("Siguiente · cola: " + count);
		frame.getMainPanel().getPreviousButton().setToolTipText("Anterior · cola: " + count);
		frame.getMainPanel().getQueueButton().setToolTipText("Cola · " + count + " canciones");
		frame.getMainPanel().getPlayPauseButton()
				.setToolTipText(audioPlayer.getCurrentSong() == null ? "Reproducir cola · " + count + " canciones"
						: audioPlayer.getCurrentSong().getTitle());
	}

	/**
	 * Pregunta a qué playlist agregar la canción (o permite crear una nueva) y la
	 * agrega.
	 *
	 * @param song   canción que se desea agregar
	 * @param status dónde mostrar el resultado, por ejemplo panel::setStatus
	 */
	private void addToPlaylist(Song song, Consumer<String> status) {
		// Ofrece playlists existentes o permite crear una antes de agregar la canción.
		String createOption = "Nueva playlist...";
		List<String> options = new ArrayList<>();
		for (PlayList playlist : playlistManager.getPlayLists()) {
			options.add(playlist.getName());
		}
		options.add(createOption);
		Object selected = JOptionPane.showInputDialog(frame.getDialogParent(), "Selecciona una playlist:",
				"Agregar a playlist", JOptionPane.PLAIN_MESSAGE, null, options.toArray(), options.get(0));
		if (selected == null) {
			return;
		}
		PlayList playlist;
		if (selected.toString().equals(createOption)) {
			playlist = askNewPlaylist();
			if (playlist == null) {
				return;
			}
		} else {
			playlist = playlistManager.findPlayList(selected.toString());
		}
		if (playlist == null) {
			return;
		}
		if (playlist.getSongs().contains(song)) {
			status.accept("Esa canción ya está en la playlist " + playlist.getName() + ".");
			return;
		}
		playlist.addSong(song);
		status.accept("Agregada a playlist: " + playlist.getName());
		refreshPlaylists();
		saveMusicData();
	}

	/**
	 * Pide el nombre de una playlist nueva y la crea. Si el nombre no es válido
	 * (vacío, muy largo o repetido) muestra el motivo y lo vuelve a pedir.
	 *
	 * @return la playlist creada, o null si el usuario canceló
	 */
	private PlayList askNewPlaylist() {
		String name = "";
		while (true) {
			name = (String) JOptionPane.showInputDialog(frame.getDialogParent(), "Nombre de la playlist:",
					"Nueva playlist", JOptionPane.PLAIN_MESSAGE, null, null, name);
			if (name == null) {
				return null;
			}
			try {
				PlayList playlist = playlistManager.createPlayList(name);
				refreshPlaylists();
				saveMusicData();
				return playlist;
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(frame.getDialogParent(), e.getMessage(), "No se pudo crear la playlist",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void refreshFavorites() {
		// Refleja los cambios en Favoritos y actualiza los corazones de los resultados
		// visibles.
		FavoritesPanel panel = frame.getFavoritesPanel();
		List<Song> favorites = favoritesManager.getFavorites();
		String query = SongValidator.normalize(panel.getQuery());
		shownFavorites.clear();
		for (Song song : favorites) {
			if (query.isEmpty() || matchesFavoriteSearch(song, query)) {
				shownFavorites.add(song);
			}
		}
		panel.showFavorites(favoriteRows(), favorites.size());
		refreshLibrary();
		ExplorePanel explore = frame.getExplorePanel();
		for (int i = 0; i < shownSongs.size(); i++) {
			explore.setFavorite(i, favoritesManager.isFavorite(shownSongs.get(i)));
		}
	}

	/**
	 * Indica si una favorita coincide con la búsqueda. Se busca en título, artista,
	 * álbum y género a la vez, sin importar mayúsculas.
	 */
	private boolean matchesFavoriteSearch(Song song, String query) {
		String[] fields = { song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre() };
		for (String field : fields) {
			if (SongValidator.normalize(field).contains(query)) {
				return true;
			}
		}
		return false;
	}

	private List<String[]> favoriteRows() {
		List<String[]> rows = new ArrayList<>();
		for (Song song : shownFavorites) {
			rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre(),
					formatDuration(song.getDuration()) });
		}
		return rows;
	}

	private void refreshHistory() {
		frame.getHistoryPanel().showHistory(historyRows());
		frame.refreshTheme();
	}

	private List<String[]> historyRows() {
		List<String[]> rows = new ArrayList<>();
		for (HistoryEntry entry : historyManager.getEntries()) {
			Song song = entry.getSong();
			rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(),
					formatPlayedAt(entry.getPlayedAt()) });
		}
		return rows;
	}

	/**
	 * Muestra solo la hora si la canción sonó hoy; si no, la fecha y la hora.
	 */
	private String formatPlayedAt(LocalDateTime playedAt) {
		String time = playedAt.format(TIME_FORMAT);
		if (playedAt.toLocalDate().equals(LocalDate.now())) {
			return "Hoy, " + time;
		}
		return playedAt.format(DATE_FORMAT) + ", " + time;
	}

	/**
	 * Conecta los atajos globales que se muestran en Configuración: Espacio para
	 * pausar o reanudar, y las flechas para cambiar de canción.
	 */
	private void wireKeyboardShortcuts() {
		bindShortcut("SPACE", "togglePlayback", () -> frame.getMainPanel().getPlayPauseButton().doClick());
		bindShortcut("RIGHT", "nextTrack", this::playNext);
		bindShortcut("LEFT", "previousTrack", this::playPrevious);
	}

	/**
	 * Registra un atajo en toda la ventana. El atajo se desactiva mientras el
	 * cursor está en un campo de texto, para que escribir un espacio o mover el
	 * cursor con las flechas no pause ni cambie la música.
	 *
	 * @param key    tecla, por ejemplo "SPACE"
	 * @param name   nombre interno de la acción
	 * @param action lo que hace el atajo
	 */
	private void bindShortcut(String key, String name, Runnable action) {
		JComponent root = frame.getRootPane();
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
		root.getActionMap().put(name, new AbstractAction() {
			@Override
			public boolean isEnabled() {
				return !isTyping();
			}

			@Override
			public void actionPerformed(ActionEvent event) {
				action.run();
			}
		});
	}

	private boolean isTyping() {
		Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return focused instanceof JTextComponent;
	}

	// ---------------------------------------------------------------
	// Biblioteca
	// ---------------------------------------------------------------

	private void wireLibrary() {
		LibraryPanel panel = frame.getLibraryPanel();
		panel.addSearchListener(event -> refreshLibrary());
		panel.addAddSongListener(event -> openAddSongDialog());
		panel.addImportListener(event -> {
			panel.setStatus("Leyendo archivos MP3...");
			// Usa el mismo importador de Explorar; al terminar actualiza Biblioteca
			selectMp3Files(frame.getExplorePanel());
		});
		panel.addPlayAllListener(event -> playLibrary());
		panel.addPlayListener(event -> {
			Song song = librarySongAt(event.getActionCommand());
			if (song != null && !addToQueue(song, frame.getExplorePanel(), true)) {
				panel.setStatus("No se puede reproducir \"" + song.getTitle()
						+ "\": el archivo ya no existe en el computador.");
			}
		});
		panel.addFavoriteListener(event -> {
			Song song = librarySongAt(event.getActionCommand());
			if (song == null) {
				return;
			}
			if (favoritesManager.isFavorite(song)) {
				favoritesManager.unmarkFavorite(song);
			} else {
				favoritesManager.markFavorite(song);
			}
			refreshFavorites(); // también actualiza Biblioteca
			saveMusicData();
		});
		panel.addPlaylistListener(event -> {
			Song song = librarySongAt(event.getActionCommand());
			if (song != null) {
				addToPlaylist(song, panel::setStatus);
			}
		});
		panel.addEditListener(event -> editLibrarySong(librarySongAt(event.getActionCommand())));
		panel.addRemoveListener(event -> removeFromLibrary(librarySongAt(event.getActionCommand())));
		refreshLibrary();
	}

	private void editLibrarySong(Song song) {
		if (song == null) {
			return;
		}
		Window owner = SwingUtilities.getWindowAncestor(frame.getDialogParent());
		if (owner == null && frame.getDialogParent() instanceof Window) {
			owner = (Window) frame.getDialogParent();
		}
		EditSongDialog dialog = new EditSongDialog(owner, song.getTitle(), song.getAlbumImage());
		dialog.addSaveListener(event -> {
			try {
				model.updateSong(song, dialog.getSongTitle(), song.getArtist(), song.getAlbum(), song.getGenre(),
						dialog.getSelectedImage());
				dialog.dispose();
				refreshFavorites();
				refreshPlaylists();
				refreshHistory();
				saveMusicData();
				frame.getLibraryPanel().setStatus("Canción actualizada: " + song.getTitle());
			} catch (IllegalArgumentException e) {
				dialog.setStatus(e.getMessage());
			}
		});
		dialog.setVisible(true);
	}

	/**
	 * Quita una canción de la biblioteca después de pedir confirmación. El archivo
	 * MP3 no se borra del computador, y la canción sigue en las playlists y en el
	 * historial donde ya estaba.
	 */
	private void removeFromLibrary(Song song) {
		if (song == null) {
			return;
		}
		int answer = JOptionPane.showConfirmDialog(frame.getDialogParent(),
				"¿Quitar \"" + song.getTitle() + "\" de la biblioteca?\n" + "El archivo no se borra de tu computador.",
				"Quitar de la biblioteca", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}
		model.removeSong(song);
		if (favoritesManager.isFavorite(song)) {
			favoritesManager.unmarkFavorite(song);
		}
		refreshFavorites(); // también actualiza Biblioteca
		frame.getLibraryPanel().setStatus("Quitada de la biblioteca: " + song.getTitle());
		saveMusicData();
	}

	/**
	 * Pone en la cola las canciones que se ven en Biblioteca y empieza a
	 * reproducir.
	 */
	private void playLibrary() {
		playbackQueue.clear();
		int skipped = 0;
		for (Song song : shownLibrarySongs) {
			if (!playbackQueue.add(song)) {
				skipped++;
			}
		}
		updateQueueControls();
		LibraryPanel panel = frame.getLibraryPanel();
		if (playbackQueue.size() == 0) {
			panel.setStatus("Ninguna de estas canciones se puede reproducir (archivos no encontrados).");
			return;
		}
		playCurrent();
		panel.setStatus(skipped == 0 ? "Reproduciendo " + playbackQueue.size() + " canciones"
				: "Reproduciendo " + playbackQueue.size() + " canciones · " + skipped
						+ " no se encontraron en el computador");
	}

	/**
	 * Vuelve a mostrar la biblioteca: todas las canciones o, si hay algo escrito en
	 * el buscador, solo las que coinciden con el criterio elegido.
	 */
	private void refreshLibrary() {
		LibraryPanel panel = frame.getLibraryPanel();
		String query = panel.getQuery();
		SearchCriteria criteria = panel.getCriteria();
		List<Song> songs = query.isEmpty() ? model.getSongs() : model.searchSongs(query, criteria);

		shownLibrarySongs.clear();
		shownLibrarySongs.addAll(songs);

		List<String[]> rows = new ArrayList<>();
		for (Song song : shownLibrarySongs) {
			rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre(),
					formatDuration(song.getDuration()), favoritesManager.isFavorite(song) ? "1" : "0" });
		}
		panel.showSongs(rows, model.getSongs().size());
	}

	private Song librarySongAt(String command) {
		int index = parseIndex(command);
		return index >= 0 && index < shownLibrarySongs.size() ? shownLibrarySongs.get(index) : null;
	}

	// ---------------------------------------------------------------
	// Playlists
	// ---------------------------------------------------------------

	private void wirePlaylists() {
		PlaylistPanel panel = frame.getPlaylistPanel();
		panel.addCreateListener(event -> createPlaylistFromPanel());
		panel.addSelectListener(event -> {
			PlayList playlist = playlistAt(event.getActionCommand());
			if (playlist != null) {
				selectedPlaylist = playlist;
				panel.setStatus(" ");
				refreshPlaylists();
			}
		});
		panel.addRenameListener(event -> renamePlaylist(selectedPlaylist));
		panel.addDeleteListener(event -> deletePlaylist(selectedPlaylist));
		panel.addPlayAllListener(event -> playSelectedPlaylist());
		panel.addAddFromLibraryListener(event -> openSongPicker());
		panel.addPlaySongListener(event -> {
			Song song = selectedPlaylistSongAt(event.getActionCommand());
			if (song == null) {
				return;
			}
			if (addToQueue(song, frame.getExplorePanel(), true)) {
				panel.setStatus("Reproduciendo: " + song.getTitle());
			} else {
				panel.setStatus("No se puede reproducir \"" + song.getTitle()
						+ "\": el archivo ya no existe o el enlace no es válido.");
			}
		});
		panel.addRemoveSongListener(event -> {
			Song song = selectedPlaylistSongAt(event.getActionCommand());
			if (song != null) {
				selectedPlaylist.removeSong(song);
				panel.setStatus("Quitada de " + selectedPlaylist.getName() + ": " + song.getTitle());
				refreshPlaylists();
				saveMusicData();
			}
		});
		refreshPlaylists();
	}

	/**
	 * Crea una playlist con el nombre escrito en la tarjeta "Crear playlist". Si el
	 * nombre no es válido, muestra el motivo en la caja roja.
	 */
	private void createPlaylistFromPanel() {
		PlaylistPanel panel = frame.getPlaylistPanel();
		try {
			PlayList playlist = playlistManager.createPlayList(panel.getNewPlaylistName());
			selectedPlaylist = playlist;
			panel.clearNewPlaylistName();
			panel.hideError();
			panel.setStatus("Playlist creada: " + playlist.getName());
			refreshPlaylists();
			saveMusicData();
		} catch (IllegalArgumentException e) {
			panel.showError(e.getMessage());
		}
	}

	/**
	 * Abre la ventana para elegir canciones de la biblioteca y agregarlas a la
	 * playlist seleccionada.
	 */
	private void openSongPicker() {
		if (selectedPlaylist == null) {
			return;
		}
		PlayList playlist = selectedPlaylist;
		List<Song> library = new ArrayList<>(model.getSongs());
		List<String[]> rows = new ArrayList<>();
		for (Song song : library) {
			rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre(),
					playlist.getSongs().contains(song) ? "1" : "0" });
		}
		SongPickerDialog[] dialog = new SongPickerDialog[1];
		dialog[0] = new SongPickerDialog(dialogWindow(), playlist.getName(), rows, index -> {
			Song song = library.get(index);
			try {
				playlist.addSong(song);
			} catch (IllegalArgumentException e) {
				dialog[0].setStatus(e.getMessage());
				return false;
			}
			refreshPlaylists();
			saveMusicData();
			return true;
		});
		dialog[0].setVisible(true);
	}

	/** Ventana sobre la que se abren los diálogos propios (no los JOptionPane). */
	private Window dialogWindow() {
		Window owner = SwingUtilities.getWindowAncestor(frame.getDialogParent());
		if (owner == null && frame.getDialogParent() instanceof Window) {
			owner = (Window) frame.getDialogParent();
		}
		return owner;
	}

	private void renamePlaylist(PlayList playlist) {
		if (playlist == null) {
			return;
		}
		String name = playlist.getName();
		while (true) {
			name = (String) JOptionPane.showInputDialog(frame.getDialogParent(), "Nuevo nombre:", "Renombrar playlist",
					JOptionPane.PLAIN_MESSAGE, null, null, name);
			if (name == null) {
				return;
			}
			try {
				playlistManager.renamePlayList(playlist.getName(), name);
				refreshPlaylists();
				saveMusicData();
				return;
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(frame.getDialogParent(), e.getMessage(), "No se pudo renombrar",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void deletePlaylist(PlayList playlist) {
		if (playlist == null) {
			return;
		}
		int answer = JOptionPane.showConfirmDialog(frame.getDialogParent(),
				"¿Seguro que quieres eliminar la playlist \"" + playlist.getName() + "\"?\n"
						+ "Las canciones no se borran de la biblioteca.",
				"Eliminar playlist", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}
		playlistManager.deletePlayList(playlist.getName());
		if (selectedPlaylist == playlist) {
			selectedPlaylist = null; // refreshPlaylists elige la primera que quede
		}
		frame.getPlaylistPanel().setStatus("Playlist eliminada: " + playlist.getName());
		refreshPlaylists();
		saveMusicData();
	}

	/**
	 * Pone en la cola las canciones de la playlist seleccionada y empieza a
	 * reproducir.
	 */
	private void playSelectedPlaylist() {
		if (selectedPlaylist == null) {
			return;
		}
		playbackQueue.clear();
		int skipped = 0;
		for (Song song : selectedPlaylist.getSongs()) {
			if (!playbackQueue.add(song)) {
				skipped++;
			}
		}
		updateQueueControls();
		PlaylistPanel panel = frame.getPlaylistPanel();
		if (playbackQueue.size() == 0) {
			panel.setStatus("Ninguna canción de esta playlist se puede reproducir.");
			return;
		}
		playCurrent();
		panel.setStatus(skipped == 0 ? "Reproduciendo " + selectedPlaylist.getName()
				: "Reproduciendo " + selectedPlaylist.getName() + " · " + skipped
						+ " no se pudieron agregar (archivo no encontrado)");
	}

	/**
	 * Vuelve a mostrar la sección Playlists. Si la playlist seleccionada ya no
	 * existe (o no hay ninguna), selecciona la primera.
	 */
	private void refreshPlaylists() {
		PlaylistPanel panel = frame.getPlaylistPanel();
		List<PlayList> playlists = playlistManager.getPlayLists();
		if (selectedPlaylist == null || !playlists.contains(selectedPlaylist)) {
			selectedPlaylist = playlists.isEmpty() ? null : playlists.get(0);
		}
		panel.showPlaylists(playlistRows(), playlists.indexOf(selectedPlaylist));
		if (selectedPlaylist == null) {
			panel.showNoPlaylistSelected(!playlists.isEmpty());
		} else {
			int count = selectedPlaylist.getSongs().size();
			panel.showSelectedPlaylist(selectedPlaylist.getName(), count == 1 ? "1 canción" : count + " canciones",
					playlistSongRows(selectedPlaylist.getSongs()));
		}
	}

	private List<String[]> playlistRows() {
		List<String[]> rows = new ArrayList<>();
		for (PlayList playlist : playlistManager.getPlayLists()) {
			int count = playlist.getSongs().size();
			rows.add(new String[] { playlist.getName(), count == 1 ? "1 canción" : count + " canciones" });
		}
		return rows;
	}

	private List<String[]> playlistSongRows(List<Song> songs) {
		List<String[]> rows = new ArrayList<>();
		for (Song song : songs) {
			rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre(),
					formatDuration(song.getDuration()), favoritesManager.isFavorite(song) ? "1" : "0" });
		}
		return rows;
	}

	private PlayList playlistAt(String command) {
		List<PlayList> playlists = playlistManager.getPlayLists();
		int index = parseIndex(command);
		return index >= 0 && index < playlists.size() ? playlists.get(index) : null;
	}

	private Song selectedPlaylistSongAt(String command) {
		if (selectedPlaylist == null) {
			return null;
		}
		int index = parseIndex(command);
		List<Song> songs = selectedPlaylist.getSongs();
		return index >= 0 && index < songs.size() ? songs.get(index) : null;
	}

	/**
	 * Convierte el comando de un botón en índice; devuelve -1 si no es un número.
	 */
	private int parseIndex(String command) {
		try {
			return Integer.parseInt(command);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private String formatDuration(double duration) {
		int seconds = (int) Math.max(0, duration);
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}

	// ---------------------------------------------------------------
	// Guardar y cargar
	// ---------------------------------------------------------------

	/**
	 * Carga la biblioteca, las playlists, los favoritos y el historial de la sesión
	 * anterior. Si algo falla, la aplicación arranca vacía en vez de cerrarse.
	 */
	private void loadMusicData() {
		if (musicDataStore == null) {
			return; // nadie ha iniciado sesión
		}
		MusicData data;
		try {
			data = musicDataStore.load();
		} catch (IOException e) {
			System.err.println("No se pudieron cargar los datos guardados: " + e.getMessage());
			return;
		}
		for (Song song : data.getLibrary()) {
			try {
				model.addSong(song);
			} catch (IllegalArgumentException e) {
				// El archivo ya no existe o la canción está repetida: se omite
			}
		}
		for (Song song : data.getFavorites()) {
			favoritesManager.markFavorite(song);
		}
		for (PlayList saved : data.getPlaylists()) {
			try {
				PlayList playlist = playlistManager.createPlayList(saved.getName());
				for (Song song : saved.getSongs()) {
					if (!playlist.getSongs().contains(song)) {
						playlist.addSong(song);
					}
				}
			} catch (IllegalArgumentException e) {
				// Nombre inválido o repetido: se omite esa playlist
			}
		}
		// El historial se guarda del más reciente al más antiguo; se agrega al
		// revés porque addPlayed pone cada registro de primero.
		List<HistoryEntry> history = data.getHistory();
		for (int i = history.size() - 1; i >= 0; i--) {
			historyManager.addPlayed(history.get(i).getSong(), history.get(i).getPlayedAt());
		}
		refreshFavorites();
		refreshHistory();
		refreshPlaylists();
	}

	/**
	 * Guarda el estado actual. Se llama después de cada cambio y al cerrar la
	 * ventana, así no se pierde nada aunque la aplicación se cierre de golpe.
	 */
	private void saveMusicData() {
		if (musicDataStore == null) {
			return; // nadie ha iniciado sesión
		}
		MusicData data = new MusicData();
		data.getLibrary().addAll(model.getSongs());
		data.getPlaylists().addAll(playlistManager.getPlayLists());
		data.getFavorites().addAll(favoritesManager.getFavorites());
		data.getHistory().addAll(historyManager.getEntries());
		try {
			musicDataStore.save(data);
		} catch (IOException e) {
			System.err.println("No se pudieron guardar los datos: " + e.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
		case IFrame.HOME:
			frame.showSection(IFrame.HOME);
			String preferredGenre = preferredHomeGenre();
			if (homeRecommendationsRetryNeeded || homeRecommendedSongs.isEmpty()
					|| !preferredGenre.equalsIgnoreCase(homeRecommendationGenre)) {
				loadHomeRecommendations(preferredGenre);
			}
			break;
		case IFrame.EXPLORE:
		case IFrame.LIBRARY:
		case IFrame.FAVORITES:
		case IFrame.PLAYLISTS:
		case IFrame.HISTORY:
		case IFrame.SETTINGS:
			frame.showSection(event.getActionCommand());
			break;
		default:
			break;
		}
	}

	public void saveInDirectory(String directoryPath) {
		persistenceManager.saveAllDataInDirectory(directoryPath);
	}

	/**
	 * Abre la pantalla modal para agregar canciones desde una carpeta o
	 * manualmente.
	 *
	 * @author progra 2
	 */
	private void openAddSongDialog() {
		Window owner = SwingUtilities.getWindowAncestor(frame.getDialogParent());
		if (owner == null && frame.getDialogParent() instanceof Window) {
			owner = (Window) frame.getDialogParent();
		}
		AddSongDialog dialog = new AddSongDialog(owner);
		dialog.addImportFolderListener(event -> importSongsFromFolder(dialog));
		dialog.addManualSongListener(event -> addManualSong(dialog));
		dialog.setVisible(true);
	}

	/**
	 * Escanea la carpeta elegida en un hilo secundario y agrega los MP3 encontrados
	 * a la biblioteca.
	 *
	 * @param dialog diálogo desde el que se solicitó la importación
	 * @author progra 2
	 */
	private void importSongsFromFolder(AddSongDialog dialog) {
		File folder = dialog.getSelectedFolder();
		if (folder == null) {
			dialog.setStatus("Primero selecciona una carpeta.");
			return;
		}
		dialog.setStatus("Escaneando carpeta...");
		new Thread(() -> {
			try {
				List<Song> found = model.detectMp3InFolder(folder);
				SwingUtilities.invokeLater(() -> {
					addImportedSongs(frame.getExplorePanel(), found);
					dialog.setStatus(frame.getLibraryPanel() != null
							? "Importación completada (" + found.size() + " MP3 leídos)."
							: "Importación completada.");
				});
			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> dialog.setStatus("Error: " + e.getMessage()));
			}
		}, "folder-mp3-scan").start();
	}

	/**
	 * Valida y agrega una canción ingresada manualmente a la biblioteca.
	 *
	 * @param dialog diálogo con los datos de la canción
	 * @author progra 2
	 */
	private void addManualSong(AddSongDialog dialog) {
		File audio = dialog.getSelectedAudio();
		String audioPath = audio == null ? "" : audio.getAbsolutePath();
		Song song = new Song(dialog.getSongTitle(), dialog.getSongArtist(), dialog.getSongAlbum(),
				dialog.getSongGenre(), dialog.getSelectedImage(), 0, audioPath);
		try {
			model.addSong(song);
			saveMusicData();
			refreshLibrary();
			frame.getLibraryPanel().setStatus("Canción agregada: " + song.getTitle());
			dialog.clearManualFields();
			dialog.setStatus("Canción agregada a la biblioteca.");
		} catch (IllegalArgumentException e) {
			dialog.setStatus(e.getMessage());
		}
	}

	// ---------------------------------------------------------------
	// Pantalla del administrador
	// ---------------------------------------------------------------

	/** Conecta los botones de la pantalla del administrador. */
	private void wireAdmin() {
		adminPanel = frame.getAdminPanel();
		loadAdminFolders();
		adminPanel.addMenuListener(event -> {
			adminPanel.showSection(event.getActionCommand());
			refreshAdmin();
		});
		adminPanel.addSearchListener(this::refreshAdmin);
		wireAdminSongs();
		wireAdminUsers();
		wireAdminFolders();
		wireAdminGenres();
	}

    /**
     * Se llama cuando un administrador inicia sesión.
     *
     * @param name nombre del administrador
     */
    private void startAdmin(String name) {
        adminName = name;
        adminPanel.setAdminName(name);
        adminPanel.clearSearch();
        loadAllUsersData();
        for (Song song : adminAllSongs) {
            genreManager.addIfMissing(song.getGenre());
        }
        saveAdminGenres();
        adminPanel.getSongsPanel().showNewForm(adminGenreNames());
        adminPanel.getUsersPanel().showNewForm();
        adminPanel.getGenresPanel().showNewForm();
        adminPanel.getFoldersPanel().showError(null);
        refreshAdmin();
    }

	private void refreshAdmin() {
		loadAllUsersData();
		refreshAdminDashboard();
		refreshAdminSongs();
		refreshAdminUsers();
		refreshAdminFolders();
		refreshAdminGenres();
	}

	// ---------------------------------------------------------------
	// Dashboard
	// ---------------------------------------------------------------

	/**
	 * Cada usuario guarda su propia biblioteca. Aquí se juntan las canciones de
	 * todas las cuentas (sin repetir) y se cuentan sus playlists y las
	 * reproducciones de hoy.
	 */
	private void loadAllUsersData() {
		adminAllSongs.clear();
		adminAllSongs.addAll(model.getSongs());
		adminPlaylistCount = playlistManager.getPlayLists().size();
		adminPlaysToday = countPlaysToday(historyManager.getEntries());
		for (MusicDataStore store : otherUserStores()) {
			try {
				MusicData data = store.load();
                for (Song song : songsOf(data)) {
					if (!adminAllSongs.contains(song)) {
						adminAllSongs.add(song);
					}
				}
				adminPlaylistCount += data.getPlaylists().size();
				adminPlaysToday += countPlaysToday(data.getHistory());
			} catch (IOException e) {
				System.err.println("No se pudieron leer los datos de " + store.getFile() + ": " + e.getMessage());
			}
		}
	}
	
    /**
     * Canciones guardadas de un usuario: su biblioteca (mp3 locales), sus
     * favoritos y sus playlists (ahí quedan las canciones de Jamendo).
     */
    private List<Song> songsOf(MusicData data) {
        List<Song> songs = new ArrayList<>(data.getLibrary());
        songs.addAll(data.getFavorites());
        for (PlayList playList : data.getPlaylists()) {
            songs.addAll(playList.getSongs());
        }
        return songs;
    }

	/**
	 * Archivos de datos de las demás cuentas (el admin en sesión ya está cargado en
	 * el modelo).
	 */
	private List<MusicDataStore> otherUserStores() {
		List<MusicDataStore> stores = new ArrayList<>();
		for (String[] account : userStore.getAccounts()) {
			File file = persistenceManager.getUserDataFile(account[0], account[1]);
			if (!account[1].equals(adminName) && file.exists()) {
				stores.add(new MusicDataStore(file));
			}
		}
		return stores;
	}

	private int countPlaysToday(List<HistoryEntry> entries) {
		int plays = 0;
		for (HistoryEntry entry : entries) {
			if (entry.getPlayedAt().toLocalDate().equals(LocalDate.now())) {
				plays++;
			}
		}
		return plays;
	}

	private void refreshAdminDashboard() {
		adminPanel.getDashboardPanel().showStats(adminAllSongs.size(), userStore.getAccounts().size(),
				adminPlaylistCount, adminPlaysToday);

		List<String[]> genres = new ArrayList<>();
		for (Genre genre : genreManager.getGenres()) {
			genres.add(new String[] { genre.getName(), String.valueOf(countSongsOfGenre(genre.getName())),
					genre.getColor() });
		}
		adminPanel.getDashboardPanel().showGenreCounts(genres);

		List<String[]> activity = new ArrayList<>();
		for (int i = 0; i < adminActivityTexts.size(); i++) {
			activity.add(new String[] { adminActivityTexts.get(i), adminTimeAgo(adminActivityTimes.get(i)) });
		}
		adminPanel.getDashboardPanel().showActivity(activity);
	}

	/** Agrega un registro a la actividad reciente. */
	private void logAdminActivity(String text) {
		adminActivityTexts.add(0, text);
		adminActivityTimes.add(0, LocalDateTime.now());
		if (adminActivityTexts.size() > MAX_ADMIN_ACTIVITY) {
			adminActivityTexts.remove(MAX_ADMIN_ACTIVITY);
			adminActivityTimes.remove(MAX_ADMIN_ACTIVITY);
		}
	}

	private String adminTimeAgo(LocalDateTime time) {
		long minutes = Duration.between(time, LocalDateTime.now()).toMinutes();
		if (minutes < 1) {
			return "ahora";
		}
		if (minutes < 60) {
			return "hace " + minutes + " min";
		}
		if (minutes < 24 * 60) {
			return "hace " + (minutes / 60) + " h";
		}
		return time.format(ACTIVITY_DAY_FORMAT);
	}

	// ---------------------------------------------------------------
	// Canciones
	// ---------------------------------------------------------------

	private void wireAdminSongs() {
		AdminSongsPanel songsPanel = adminPanel.getSongsPanel();
		songsPanel.addNewListener(event -> {
			adminEditingSong = null;
			songsPanel.showNewForm(adminGenreNames());
		});
		songsPanel.addScanListener(event -> {
			File folder = songsPanel.chooseFolder();
			if (folder != null) {
				List<File> folders = new ArrayList<>();
				folders.add(folder);
				scanAdminFolders(folders);
			}
		});
		songsPanel.addEditListener(event -> {
			adminEditingSong = adminSongs.get(Integer.parseInt(event.getActionCommand()));
			songsPanel.showEditForm(adminEditingSong.getTitle(), adminEditingSong.getArtist(),
					adminEditingSong.getAlbum(), adminGenreNames(), adminEditingSong.getGenre(),
					adminEditingSong.getAlbumImage());
		});
		songsPanel.addDeleteListener(event -> {
			adminSongToDelete = adminSongs.get(Integer.parseInt(event.getActionCommand()));
			songsPanel.showDeleteConfirm(adminSongToDelete.getTitle());
		});
		songsPanel.addConfirmDeleteListener(event -> deleteAdminSong());
		songsPanel.addSaveListener(event -> saveAdminSong());
	}

	private void refreshAdminSongs() {
		adminSongs.clear();
		List<String[]> rows = new ArrayList<>();
		for (Song song : adminAllSongs) {
			if (matchesAdminSearch(song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre())) {
				adminSongs.add(song);
				Genre genre = genreManager.findGenre(song.getGenre());
				File image = song.getAlbumImage();
				if (image == null && genre != null) {
					image = genre.getDefaultImage();
				}
                rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre(),
                        genre == null ? GenreManager.DEFAULT_COLOR : genre.getColor(),
                        image == null ? "" : image.getAbsolutePath(),
                        song.getFilePath() != null && song.getFilePath().startsWith("http") ? "Jamendo" : "mp3 ✓" });
			}
		}
		adminPanel.getSongsPanel().showSongs(rows);
	}

	private void saveAdminSong() {
		AdminSongsPanel songsPanel = adminPanel.getSongsPanel();
		String title = songsPanel.getTitleText();
		String artist = songsPanel.getArtistText();
		String album = songsPanel.getAlbumText();
		String genre = songsPanel.getGenreText();
		try {
			SongValidator.validateTitle(title);
			SongValidator.validateGenre(genre);
			if (adminEditingSong == null) {
				addAdminSong(title, artist, album, genre, songsPanel.getSelectedImage(), songsPanel.getSelectedMp3());
				logAdminActivity(adminName + " agregó \"" + SongValidator.clean(title) + "\"");
			} else {
				updateSongEverywhere(adminEditingSong, title, artist, album, genre, songsPanel.getSelectedImage());
				logAdminActivity(adminName + " editó \"" + adminEditingSong.getTitle() + "\"");
			}
			saveMusicData();
			adminEditingSong = null;
			songsPanel.showNewForm(adminGenreNames());
			refreshAdmin();
		} catch (IllegalArgumentException e) {
			songsPanel.showWarning(e.getMessage());
		} catch (IOException e) {
			songsPanel.showWarning("No se pudieron guardar los cambios.");
		}
	}

	/**
	 * Edita la canción en la biblioteca del admin y en la de cada usuario que la
	 * tenga.
	 *
	 * @throws IOException si no se puede guardar el archivo de algún usuario
	 */
	private void updateSongEverywhere(Song song, String title, String artist, String album, String genre, File image)
			throws IOException {
		// Copia con el título y artista de antes, para encontrarla en los demás
		// usuarios
		Song oldSong = new Song(song.getTitle(), song.getArtist(), "", "", (File) null, 0, null);
		if (model.getSongs().contains(song)) {
			model.updateSong(song, title, artist, album, genre, image);
		} else {
			SongValidator.validateTitle(SongValidator.clean(title));
			SongValidator.validateGenre(genre);
			SongValidator.validateAlbumImage(image);
		}
		for (MusicDataStore store : otherUserStores()) {
			MusicData data = store.load();
            for (Song saved : songsOf(data)) {
				if (saved.equals(oldSong)) {
					saved.setTitle(SongValidator.clean(title));
					saved.setArtist(SongValidator.cleanOrDefault(artist, SongValidator.DEFAULT_ARTIST));
					saved.setAlbum(album);
					saved.setGenre(genre);
					saved.setAlbumImage(image);
				}
			}
			store.save(data);
		}
		saveMusicData();
	}

	/**
	 * Quita la canción de la biblioteca del admin y de la de cada usuario (también
	 * de sus favoritos, playlists e historial).
	 *
	 * @throws IOException si no se puede guardar el archivo de algún usuario
	 */
	private void removeSongEverywhere(Song song) throws IOException {
		if (model.getSongs().contains(song)) {
			model.removeSong(song);
		}
		for (MusicDataStore store : otherUserStores()) {
			MusicData data = store.load();
			data.getLibrary().removeIf(saved -> saved.equals(song));
			data.getFavorites().removeIf(saved -> saved.equals(song));
			for (PlayList playList : data.getPlaylists()) {
				playList.getSongs().removeIf(saved -> saved.equals(song));
			}
			data.getHistory().removeIf(entry -> entry.getSong().equals(song));
			store.save(data);
		}
		saveMusicData();
	}

	/**
	 * Lee el mp3 elegido (para sacar la duración) y lo agrega con los datos del
	 * formulario.
	 */
	private void addAdminSong(String title, String artist, String album, String genre, File image, File mp3) {
		if (mp3 == null) {
			throw new IllegalArgumentException("Elige el archivo mp3 de la canción.");
		}
		validateAuthorizedFolder(mp3.getParentFile());
		SongValidator.validateAlbumImage(image);
		List<File> files = new ArrayList<>();
		files.add(mp3);
		List<Song> found = model.detectMp3(files);
		if (found.isEmpty()) {
			throw new IllegalArgumentException(SongValidator.MSG_FILE);
		}
		Song song = found.get(0);
		model.addSong(song);
		try {
			model.updateSong(song, title, artist, album, genre, image);
		} catch (IllegalArgumentException e) {
			model.removeSong(song); // si los datos no sirven, no se deja a medias
			throw e;
		}
	}

	private void deleteAdminSong() {
		if (adminSongToDelete == null) {
			return;
		}
		try {
			removeSongEverywhere(adminSongToDelete);
		} catch (IOException e) {
			adminPanel.getSongsPanel().showWarning("No se pudo eliminar la canción de todos los usuarios.");
			return;
		}
		logAdminActivity(adminName + " eliminó \"" + adminSongToDelete.getTitle() + "\"");
		if (adminSongToDelete == adminEditingSong) {
			adminEditingSong = null;
		}
		adminSongToDelete = null;
		adminPanel.getSongsPanel().showNewForm(adminGenreNames());
		refreshAdmin();
	}

	// ---------------------------------------------------------------
	// Carpetas y escaneo
	// ---------------------------------------------------------------

	private void wireAdminFolders() {
		AdminFoldersPanel foldersPanel = adminPanel.getFoldersPanel();
		foldersPanel.addAuthorizeListener(event -> {
			File folder = foldersPanel.chooseFolder();
			if (folder == null) {
				return;
			}
			try {
				model.authorizeFolder(folder);
				saveAdminFolders();
				foldersPanel.showError(null);
				logAdminActivity("Carpeta autorizada: " + folder.getName());
				refreshAdmin();
			} catch (IllegalArgumentException e) {
				foldersPanel.showError(e.getMessage());
			}
		});
		foldersPanel.addRemoveListener(event -> {
			File folder = model.getAuthorizedFolders().get(Integer.parseInt(event.getActionCommand()));
			model.removeAuthorizedFolder(folder);
			saveAdminFolders();
			refreshAdmin();
		});
		foldersPanel.addScanAllListener(event -> {
			if (model.getAuthorizedFolders().isEmpty()) {
				foldersPanel.showError("Primero autoriza una carpeta.");
				return;
			}
			scanAdminFolders(new ArrayList<>(model.getAuthorizedFolders()));
		});
	}

	private void refreshAdminFolders() {
		List<String[]> rows = new ArrayList<>();
		for (File folder : model.getAuthorizedFolders()) {
			rows.add(new String[] { folder.getAbsolutePath(), String.valueOf(model.countMp3InFolder(folder)) });
		}
		adminPanel.getFoldersPanel().showFolders(rows);
	}

	/**
	 * Escanea las carpetas y agrega las canciones nuevas. Cuenta cuántas son
	 * nuevas, cuántas ya existían y cuántas no traían artista o álbum.
	 */
	private void scanAdminFolders(List<File> folders) {
		int newSongs = 0;
		int existing = 0;
		int noInfo = 0;
		String error = null;
		for (File folder : folders) {
			List<Song> found;
			try {
				validateAuthorizedFolder(folder);
				found = model.detectMp3InFolder(folder);
			} catch (IllegalArgumentException e) {
				error = e.getMessage();
				continue;
			}
			for (Song song : found) {
				try {
					model.addSong(song);
					newSongs++;
					genreManager.addIfMissing(song.getGenre());
					if (SongValidator.DEFAULT_ARTIST.equals(song.getArtist())
							|| SongValidator.DEFAULT_ALBUM.equals(song.getAlbum())) {
						noInfo++;
					}
				} catch (IllegalArgumentException e) {
					existing++;
				}
			}
			logAdminActivity("Escaneo en " + folder.getName() + ": " + found.size() + " mp3");
		}
		AdminFoldersPanel foldersPanel = adminPanel.getFoldersPanel();
		foldersPanel.showScanResult(newSongs, existing, noInfo);
		foldersPanel.showError(error);
		if (error != null) {
			adminPanel.getSongsPanel().showWarning(error);
		}
		saveMusicData();
		saveAdminGenres();
		refreshAdmin();
	}

	/** Solo se puede leer dentro de una carpeta autorizada. */
	private void validateAuthorizedFolder(File folder) {
		if (model.getAuthorizedFolders().isEmpty()) {
			throw new IllegalArgumentException(SongValidator.MSG_FOLDER);
		}
		SongValidator.validateFolder(folder, model.getAuthorizedFolders());
	}

	/** Recuerda las carpetas autorizadas para la próxima vez que se abra Ritmo. */
	private void saveAdminFolders() {
		StringBuilder paths = new StringBuilder();
		for (File folder : model.getAuthorizedFolders()) {
			if (paths.length() > 0) {
				paths.append(File.pathSeparator);
			}
			paths.append(folder.getAbsolutePath());
		}
		preferences.put(ADMIN_FOLDERS_KEY, paths.toString());
	}

	private void loadAdminFolders() {
		String saved = preferences.get(ADMIN_FOLDERS_KEY, "");
		for (String path : saved.split(File.pathSeparator)) {
			File folder = new File(path);
			if (!path.isEmpty() && folder.isDirectory() && !model.getAuthorizedFolders().contains(folder)) {
				model.authorizeFolder(folder);
			}
		}
	}

	// ---------------------------------------------------------------
	// Usuarios
	// ---------------------------------------------------------------

	private void wireAdminUsers() {
		AdminUsersPanel usersPanel = adminPanel.getUsersPanel();
		usersPanel.addNewListener(event -> usersPanel.showNewForm());
		usersPanel.addEditListener(event -> {
			String name = adminUsers.get(Integer.parseInt(event.getActionCommand()));
			usersPanel.showEditForm(name, UserStore.ROLE_ADMIN.equals(userStore.getRole(name)),
					userStore.isActive(name));
		});
		usersPanel.addDeleteListener(
				event -> deleteAdminUser(adminUsers.get(Integer.parseInt(event.getActionCommand()))));
		usersPanel.addSaveListener(event -> saveAdminUser());
	}

	private void refreshAdminUsers() {
		adminUsers.clear();
		List<String[]> rows = new ArrayList<>();
		for (String[] account : userStore.getAccounts()) {
			String name = account[1];
			if (matchesAdminSearch(name)) {
				adminUsers.add(name);
				rows.add(new String[] { name,
						UserStore.ROLE_ADMIN.equals(account[0]) ? AdminUsersPanel.ROLE_ADMIN_TEXT
								: AdminUsersPanel.ROLE_USER_TEXT,
						userStore.isActive(name) ? AdminUsersPanel.ACTIVE_TEXT : AdminUsersPanel.INACTIVE_TEXT });
			}
		}
		adminPanel.getUsersPanel().showUsers(rows);
	}

	private void saveAdminUser() {
		AdminUsersPanel usersPanel = adminPanel.getUsersPanel();
		String role = usersPanel.isAdminSelected() ? UserStore.ROLE_ADMIN : UserStore.ROLE_USER;
		try {
			if (usersPanel.isEditing()) {
				String name = usersPanel.getNameText();
				if (name.equals(adminName) && (!usersPanel.isActiveSelected() || !UserStore.ROLE_ADMIN.equals(role))) {
					throw new IllegalArgumentException("No puedes desactivar ni quitarle el rol a tu propia cuenta.");
				}
				userStore.updateAccount(name, usersPanel.getPasswordText(), role, usersPanel.isActiveSelected());
				logAdminActivity("Usuario editado: " + name);
			} else {
				String name = userStore.createAccount(usersPanel.getNameText(), usersPanel.getPasswordText(), role);
				logAdminActivity("Nuevo usuario: " + name);
			}
			usersPanel.showNewForm();
			refreshAdmin();
		} catch (IllegalArgumentException e) {
			usersPanel.showError(e.getMessage());
		} catch (IOException e) {
			usersPanel.showError("No se pudo guardar el archivo de usuarios.");
		}
	}

	private void deleteAdminUser(String name) {
		if (name.equals(adminName)) {
			JOptionPane.showMessageDialog(adminPanel, "No puedes eliminar tu propia cuenta.");
			return;
		}
		int answer = JOptionPane.showConfirmDialog(adminPanel, "¿Eliminar al usuario \"" + name + "\"?",
				"Eliminar usuario", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}
		try {
			userStore.deleteAccount(name);
			logAdminActivity(adminName + " eliminó al usuario " + name);
			adminPanel.getUsersPanel().showNewForm();
			refreshAdmin();
		} catch (IOException e) {
			adminPanel.getUsersPanel().showError("No se pudo guardar el archivo de usuarios.");
		}
	}

	// ---------------------------------------------------------------
	// Géneros
	// ---------------------------------------------------------------

	private void wireAdminGenres() {
		AdminGenresPanel genresPanel = adminPanel.getGenresPanel();
		genresPanel.addNewListener(event -> {
			adminEditingGenre = null;
			genresPanel.showNewForm();
		});
		genresPanel.addEditListener(event -> {
			adminEditingGenre = adminGenres.get(Integer.parseInt(event.getActionCommand()));
			genresPanel.showEditForm(adminEditingGenre.getName(), adminEditingGenre.getColor(),
					adminEditingGenre.getDefaultImage());
		});
		genresPanel.addDeleteListener(
				event -> deleteAdminGenre(adminGenres.get(Integer.parseInt(event.getActionCommand()))));
		genresPanel.addSaveListener(event -> saveAdminGenre());
	}

	private void refreshAdminGenres() {
		adminGenres.clear();
		List<String[]> rows = new ArrayList<>();
		for (Genre genre : genreManager.getGenres()) {
			if (matchesAdminSearch(genre.getName())) {
				adminGenres.add(genre);
				rows.add(new String[] { genre.getColor(), genre.getName(),
						String.valueOf(countSongsOfGenre(genre.getName())) });
			}
		}
		adminPanel.getGenresPanel().showGenres(rows);
	}

	private void saveAdminGenre() {
		AdminGenresPanel genresPanel = adminPanel.getGenresPanel();
		try {
			if (adminEditingGenre == null) {
				Genre genre = genreManager.addGenre(genresPanel.getNameText(), genresPanel.getColorName(),
						genresPanel.getSelectedImage());
				logAdminActivity("Nuevo género: " + genre.getName());
			} else {
				String oldName = adminEditingGenre.getName();
				genreManager.updateGenre(adminEditingGenre, genresPanel.getNameText(), genresPanel.getColorName(),
						genresPanel.getSelectedImage());
				renameGenreInSongs(oldName, adminEditingGenre.getName());
				logAdminActivity("Género editado: " + adminEditingGenre.getName());
			}
			saveAdminGenres();
			adminEditingGenre = null;
			genresPanel.showNewForm();
			refreshAdmin();
		} catch (IllegalArgumentException e) {
			genresPanel.showError(e.getMessage());
		} catch (IOException e2) {
			genresPanel.showError("No se pudieron actualizar las canciones de los usuarios.");
		}
	}

    /** Si se cambia el nombre de un género, las canciones de ese género también cambian. */
    private void renameGenreInSongs(String oldName, String newName) throws IOException {
        if (oldName.equals(newName)) {
            return;
        }
        for (Song song : new ArrayList<>(adminAllSongs)) {
            if (song.getGenre().equalsIgnoreCase(oldName)) {
                updateSongEverywhere(song, song.getTitle(), song.getArtist(), song.getAlbum(), newName,
                        song.getAlbumImage());
            }
        }
    }

	private void deleteAdminGenre(Genre genre) {
		int songs = countSongsOfGenre(genre.getName());
		if (songs > 0) {
			adminPanel.getGenresPanel().showError("No se puede eliminar: hay " + songs + " canciones con este género.");
			return;
		}
		int answer = JOptionPane.showConfirmDialog(adminPanel, "¿Eliminar el género \"" + genre.getName() + "\"?",
				"Eliminar género", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			genreManager.removeGenre(genre);
			saveAdminGenres();
			logAdminActivity("Género eliminado: " + genre.getName());
			adminEditingGenre = null;
			adminPanel.getGenresPanel().showNewForm();
			refreshAdmin();
		}
	}

    private void saveAdminGenres() {
        try {
            genreManager.save();
        } catch (IOException e) {
            System.err.println("No se pudieron guardar los géneros: " + e.getMessage());
        }
        showGenreColors();
    }

    /**
     * Carga los géneros (con sus colores) al iniciar sesión cualquier cuenta,
     * para que las etiquetas de género se pinten con el color elegido.
     */
    private void loadGenres() {
        genreManager.setDirectory(persistenceManager.getBaseDirectory());
        try {
            genreManager.load();
        } catch (IOException | NumberFormatException e) {
            System.err.println("No se pudieron cargar los géneros: " + e.getMessage());
        }
        showGenreColors();
    }

    /** Le pasa a la vista el color de cada género. */
    private void showGenreColors() {
        AdminComponents.clearGenreColors();
        for (Genre genre : genreManager.getGenres()) {
            AdminComponents.setGenreColor(genre.getName(), genre.getColor());
        }
    }

	private List<String> adminGenreNames() {
		List<String> names = new ArrayList<>();
		for (Genre genre : genreManager.getGenres()) {
			names.add(genre.getName());
		}
		return names;
	}

	private int countSongsOfGenre(String genre) {
		int count = 0;
        for (Song song : adminAllSongs) {
			if (song.getGenre().equalsIgnoreCase(genre)) {
				count++;
			}
		}
		return count;
	}

	/** true si algún texto contiene lo escrito en el buscador (o si está vacío). */
	private boolean matchesAdminSearch(String... texts) {
		String search = adminPanel.getSearchText().trim().toLowerCase();
		if (search.isEmpty()) {
			return true;
		}
		for (String text : texts) {
			if (text != null && text.toLowerCase().contains(search)) {
				return true;
			}
		}
		return false;
	}

}