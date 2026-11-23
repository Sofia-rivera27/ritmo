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
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;


import co.edu.uptc.model.AudioPlayer;
import co.edu.uptc.model.FavoritesManager;
import co.edu.uptc.model.HistoryEntry;
import co.edu.uptc.model.HistoryManager;
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
import co.edu.uptc.model.UserStore;
import co.edu.uptc.view.ExplorePanel;
import co.edu.uptc.view.FavoritesPanel;
import co.edu.uptc.view.HistoryPanel;
import co.edu.uptc.view.LibraryPanel;
import co.edu.uptc.view.PlaylistPanel;
import co.edu.uptc.view.ViewFrame;



/**
 * Coordina la vista y los servicios del modelo para búsquedas, reproducción,
 * cola y gestión de favoritos y playlists.
 */
public class Presenter implements ActionListener {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ViewFrame frame;
    private final RitmoSystem ritmoSystem;
    private final UserStore userStore;
    private final PersistenceManager persistenceManager;
    private final JamendoService jamendoService = new JamendoService();
    private final PlaybackQueue playbackQueue = new PlaybackQueue();
    private final AudioPlayer audioPlayer = new AudioPlayer();
    private final FavoritesManager favoritesManager = new FavoritesManager();
    private final PlaylistManager playlistManager = new PlaylistManager();
    private final HistoryManager historyManager = new HistoryManager();
    private final MusicDataStore musicDataStore = new MusicDataStore();
    /** Playlist abierta en la sección Playlists, o null si se ve la lista. */
    private PlayList openPlaylist;
    // Los botones de resultados envían índices; esta lista conserva su
    // correspondencia.
    private final List<Song> shownSongs = new ArrayList<>();
    /** Canciones que se ven en Biblioteca, en el mismo orden (puede estar filtrada). */
    private final List<Song> shownLibrarySongs = new ArrayList<>();

    public Presenter() {
        frame = new ViewFrame();
        ritmoSystem = new RitmoSystem();
        userStore = new UserStore();
        persistenceManager = new PersistenceManager(userStore, ritmoSystem);

        // Centraliza aquí las conexiones para que las vistas no accedan directamente al
        // modelo.
        frame.addMenuListener(this);
        wirePlaybackControls();
        wireKeyboardShortcuts();
        wireExplore();
        wireFavorites();
        wireHistory();
        wirePlaylists();
        wireLibrary();
        // Carga lo guardado en la sesión anterior y lo guarda otra vez al cerrar
        loadMusicData();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                saveMusicData();
            }
        });
        // La reproducción avisa al presentador para mostrar errores y avanzar al
        // terminar.
        audioPlayer.setErrorListener(message -> SwingUtilities.invokeLater(() -> frame.getExplorePanel()
                .setStatus("Error de reproduccion: " + message)));
        audioPlayer.setEndListener(() -> SwingUtilities.invokeLater(this::playNext));
        frame.setVisible(true);
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
        frame.getMainPanel().getQueueButton()
                .addActionListener(event -> frame.getExplorePanel().showQueueDialog(queueRows(),
                        index -> removeFromQueue(index, frame.getExplorePanel())));
        updateQueueControls();
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
        panel.showFavorites(favoriteRows());
        panel.addRemoveFavoriteListener(event -> {
            int index;
            try {
                index = Integer.parseInt(event.getActionCommand());
            } catch (NumberFormatException e) {
                return;
            }
            List<Song> songs = favoritesManager.getFavorites();
            if (index >= 0 && index < songs.size()) {
                favoritesManager.unmarkFavorite(songs.get(index));
                refreshFavorites();
                saveMusicData();
            }
        });
        panel.addPlayAllListener(event -> {
            playbackQueue.clear();
            for (Song song : favoritesManager.getFavorites()) {
                playbackQueue.add(song);
            }
            updateQueueControls();
            playCurrent();
        });
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
            int answer = JOptionPane.showConfirmDialog(frame, "¿Seguro que quieres borrar el historial?",
                    "Borrar historial", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                historyManager.clear();
                refreshHistory();
                saveMusicData();
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
        panel.setStatus("Buscando en Jamendo...");
        // La consulta de red corre fuera del hilo de Swing para no bloquear la vista.
        new Thread(() -> {
            try {
                List<Song> found = byGenre ? jamendoService.searchByGenre(query)
                        : jamendoService.search(query);
                String title = byGenre ? "Resultados: género " + query + " · " + found.size()
                        : "Resultados: " + query + " · " + found.size();
                SwingUtilities.invokeLater(() -> showSongs(panel, found, title,
                        found.isEmpty() ? "No se encontraron canciones." : " "));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> panel.setStatus("Error de Jamendo: " + e.getMessage()));
            }
        }, "jamendo-search").start();
    }

    private void selectMp3Files(ExplorePanel panel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos MP3", "mp3"));
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
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
                List<Song> found = ritmoSystem.detectMp3(selected);
                SwingUtilities.invokeLater(() -> addImportedSongs(panel, found));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> panel.setStatus("No se pudieron leer los MP3: " + e.getMessage()));
            }
        }, "mp3-metadata").start();
    }

    /**
     * Agrega a la biblioteca las canciones leídas. Si una ya estaba, se usa la
     * que ya existía en vez de lanzar un error, así importar el mismo archivo
     * dos veces no deja la aplicación colgada.
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
                ritmoSystem.addSong(song);
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
        for (Song other : ritmoSystem.getSongs()) {
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
        panel.setStatus(playNow ? "Reproduciendo: " + song.getTitle()
                : "Agregada a la cola: " + song.getTitle());
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
            frame.getExplorePanel().setStatus("Reproduciendo: " + song.getTitle()
                    + " - " + song.getArtist());
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
        frame.getMainPanel().getPlayPauseButton().setToolTipText(
                audioPlayer.getCurrentSong() == null ? "Reproducir cola · " + count + " canciones"
                        : audioPlayer.getCurrentSong().getTitle());
    }

    /**
     * Pregunta a qué playlist agregar la canción (o permite crear una nueva) y
     * la agrega.
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
        Object selected = JOptionPane.showInputDialog(frame, "Selecciona una playlist:",
                "Agregar a playlist", JOptionPane.PLAIN_MESSAGE, null, options.toArray(),
                options.get(0));
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
            name = (String) JOptionPane.showInputDialog(frame, "Nombre de la playlist:", "Nueva playlist",
                    JOptionPane.PLAIN_MESSAGE, null, null, name);
            if (name == null) {
                return null;
            }
            try {
                PlayList playlist = playlistManager.createPlayList(name);
                refreshPlaylists();
                saveMusicData();
                return playlist;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "No se pudo crear la playlist",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void refreshFavorites() {
        // Refleja los cambios en Favoritos y actualiza los corazones de los resultados
        // visibles.
        frame.getFavoritesPanel().showFavorites(favoriteRows());
        frame.refreshTheme();
        refreshLibrary();
        ExplorePanel explore = frame.getExplorePanel();
        for (int i = 0; i < shownSongs.size(); i++) {
            explore.setFavorite(i, favoritesManager.isFavorite(shownSongs.get(i)));
        }
    }

    private List<String[]> favoriteRows() {
        List<String[]> rows = new ArrayList<>();
        for (Song song : favoritesManager.getFavorites()) {
            rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(),
                    song.getGenre(), formatDuration(song.getDuration()) });
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
     * Conecta los atajos globales que se muestran en Configuración: Espacio
     * para pausar o reanudar, y las flechas para cambiar de canción.
     */
    private void wireKeyboardShortcuts() {
        bindShortcut("SPACE", "togglePlayback",
                () -> frame.getMainPanel().getPlayPauseButton().doClick());
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
        panel.addRemoveListener(event -> removeFromLibrary(librarySongAt(event.getActionCommand())));
        refreshLibrary();
    }

    /**
     * Quita una canción de la biblioteca después de pedir confirmación. El
     * archivo MP3 no se borra del computador, y la canción sigue en las
     * playlists y en el historial donde ya estaba.
     */
    private void removeFromLibrary(Song song) {
        if (song == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(frame,
                "¿Quitar \"" + song.getTitle() + "\" de la biblioteca?\n"
                        + "El archivo no se borra de tu computador.",
                "Quitar de la biblioteca", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        ritmoSystem.removeSong(song);
        if (favoritesManager.isFavorite(song)) {
            favoritesManager.unmarkFavorite(song);
        }
        refreshFavorites(); // también actualiza Biblioteca
        frame.getLibraryPanel().setStatus("Quitada de la biblioteca: " + song.getTitle());
        saveMusicData();
    }

    /** Pone en la cola las canciones que se ven en Biblioteca y empieza a reproducir. */
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
     * Vuelve a mostrar la biblioteca: todas las canciones o, si hay algo
     * escrito en el buscador, solo las que coinciden con el criterio elegido.
     */
    private void refreshLibrary() {
        LibraryPanel panel = frame.getLibraryPanel();
        String query = panel.getQuery();
        SearchCriteria criteria = panel.getCriteria();
        List<Song> songs = query.isEmpty() ? ritmoSystem.getSongs() : ritmoSystem.searchSongs(query, criteria);

        shownLibrarySongs.clear();
        shownLibrarySongs.addAll(songs);

        List<String[]> rows = new ArrayList<>();
        for (Song song : shownLibrarySongs) {
            rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(), song.getGenre(),
                    formatDuration(song.getDuration()), favoritesManager.isFavorite(song) ? "1" : "0" });
        }
        panel.showSongs(rows, ritmoSystem.getSongs().size());
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
        panel.addCreateListener(event -> askNewPlaylist());
        panel.addOpenListener(event -> {
            PlayList playlist = playlistAt(event.getActionCommand());
            if (playlist != null) {
                openPlaylist = playlist;
                refreshPlaylists();
            }
        });
        panel.addBackListener(event -> {
            openPlaylist = null;
            refreshPlaylists();
        });
        panel.addRenameListener(event -> renamePlaylist(playlistAt(event.getActionCommand())));
        panel.addDeleteListener(event -> deletePlaylist(playlistAt(event.getActionCommand())));
        panel.addPlayAllListener(event -> playOpenPlaylist());
        panel.addPlaySongListener(event -> {
            Song song = openPlaylistSongAt(event.getActionCommand());
            if (song != null && !addToQueue(song, frame.getExplorePanel(), true)) {
                panel.setStatus("No se puede reproducir \"" + song.getTitle()
                        + "\": el archivo ya no existe o el enlace no es válido.");
            }
        });
        panel.addRemoveSongListener(event -> {
            Song song = openPlaylistSongAt(event.getActionCommand());
            if (song != null) {
                openPlaylist.removeSong(song);
                refreshPlaylists();
                saveMusicData();
            }
        });
        refreshPlaylists();
    }

    private void renamePlaylist(PlayList playlist) {
        if (playlist == null) {
            return;
        }
        String name = playlist.getName();
        while (true) {
            name = (String) JOptionPane.showInputDialog(frame, "Nuevo nombre:", "Renombrar playlist",
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
                JOptionPane.showMessageDialog(frame, e.getMessage(), "No se pudo renombrar",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void deletePlaylist(PlayList playlist) {
        if (playlist == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(frame,
                "¿Seguro que quieres eliminar la playlist \"" + playlist.getName() + "\"?\n"
                        + "Las canciones no se borran de la biblioteca.",
                "Eliminar playlist", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        playlistManager.deletePlayList(playlist.getName());
        if (openPlaylist == playlist) {
            openPlaylist = null;
        }
        refreshPlaylists();
        saveMusicData();
    }

    /** Pone en la cola las canciones de la playlist abierta y empieza a reproducir. */
    private void playOpenPlaylist() {
        if (openPlaylist == null) {
            return;
        }
        playbackQueue.clear();
        int skipped = 0;
        for (Song song : openPlaylist.getSongs()) {
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
        panel.setStatus(skipped == 0 ? "Reproduciendo " + openPlaylist.getName()
                : "Reproduciendo " + openPlaylist.getName() + " · " + skipped
                        + " no se pudieron agregar (archivo no encontrado)");
    }

    private void refreshPlaylists() {
        PlaylistPanel panel = frame.getPlaylistPanel();
        if (openPlaylist != null && playlistManager.getPlayLists().contains(openPlaylist)) {
            panel.showPlaylistDetail(openPlaylist.getName(), songRows(openPlaylist.getSongs()));
        } else {
            openPlaylist = null;
            panel.showPlaylists(playlistRows());
        }
        frame.refreshTheme();
    }

    private List<String[]> playlistRows() {
        List<String[]> rows = new ArrayList<>();
        for (PlayList playlist : playlistManager.getPlayLists()) {
            int count = playlist.getSongs().size();
            rows.add(new String[] { playlist.getName(), count == 1 ? "1 canción" : count + " canciones" });
        }
        return rows;
    }

    private List<String[]> songRows(List<Song> songs) {
        List<String[]> rows = new ArrayList<>();
        for (Song song : songs) {
            rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(),
                    formatDuration(song.getDuration()) });
        }
        return rows;
    }

    private PlayList playlistAt(String command) {
        List<PlayList> playlists = playlistManager.getPlayLists();
        int index = parseIndex(command);
        return index >= 0 && index < playlists.size() ? playlists.get(index) : null;
    }

    private Song openPlaylistSongAt(String command) {
        if (openPlaylist == null) {
            return null;
        }
        int index = parseIndex(command);
        List<Song> songs = openPlaylist.getSongs();
        return index >= 0 && index < songs.size() ? songs.get(index) : null;
    }

    /** Convierte el comando de un botón en índice; devuelve -1 si no es un número. */
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
     * Carga la biblioteca, las playlists, los favoritos y el historial de la
     * sesión anterior. Si algo falla, la aplicación arranca vacía en vez de
     * cerrarse.
     */
    private void loadMusicData() {
        MusicData data;
        try {
            data = musicDataStore.load();
        } catch (IOException e) {
            System.err.println("No se pudieron cargar los datos guardados: " + e.getMessage());
            return;
        }
        for (Song song : data.getLibrary()) {
            try {
                ritmoSystem.addSong(song);
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
        MusicData data = new MusicData();
        data.getLibrary().addAll(ritmoSystem.getSongs());
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
            case ViewFrame.HOME:
            case ViewFrame.EXPLORE:
            case ViewFrame.LIBRARY:
            case ViewFrame.FAVORITES:
            case ViewFrame.PLAYLISTS:
            case ViewFrame.HISTORY:
            case ViewFrame.SETTINGS:
                frame.showSection(event.getActionCommand());
                break;
            default:
                break;
        }
    }

    public void saveInDirectory(String directoryPath) {
        persistenceManager.saveAllDataInDirectory(directoryPath);
    }
}   