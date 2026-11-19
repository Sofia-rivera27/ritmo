package co.edu.uptc.presenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import co.edu.uptc.model.AudioPlayer;
import co.edu.uptc.model.FavoritesManager;
import co.edu.uptc.model.JamendoService;
import co.edu.uptc.model.PersistenceManager;
import co.edu.uptc.model.PlayList;
import co.edu.uptc.model.PlaybackQueue;
import co.edu.uptc.model.PlaylistManager;
import co.edu.uptc.model.RitmoSystem;
import co.edu.uptc.model.Song;
import co.edu.uptc.model.UserStore;
import co.edu.uptc.view.ExplorePanel;
import co.edu.uptc.view.FavoritesPanel;
import co.edu.uptc.view.ViewFrame;

/**
 * Coordina la vista y los servicios del modelo para búsquedas, reproducción,
 * cola y gestión de favoritos y playlists.
 */
public class Presenter implements ActionListener {

    private final ViewFrame frame;
    private final RitmoSystem ritmoSystem;
    private final UserStore userStore;
    private final PersistenceManager persistenceManager;
    private final JamendoService jamendoService = new JamendoService();
    private final PlaybackQueue playbackQueue = new PlaybackQueue();
    private final AudioPlayer audioPlayer = new AudioPlayer();
    private final FavoritesManager favoritesManager = new FavoritesManager();
    private final PlaylistManager playlistManager = new PlaylistManager();
    // Los botones de resultados envían índices; esta lista conserva su correspondencia.
    private final List<Song> shownSongs = new ArrayList<>();

    public Presenter() {
        frame = new ViewFrame();
        ritmoSystem = new RitmoSystem();
        userStore = new UserStore();
        persistenceManager = new PersistenceManager(userStore, ritmoSystem);

        // Centraliza aquí las conexiones para que las vistas no accedan directamente al modelo.
        frame.addMenuListener(this);
        wirePlaybackControls();
        wireExplore();
        wireFavorites();
        // La reproducción avisa al presentador para mostrar errores y avanzar al terminar.
        audioPlayer.setErrorListener(message ->
                SwingUtilities.invokeLater(() -> frame.getExplorePanel()
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
        frame.getMainPanel().getQueueButton().addActionListener(event ->
                frame.getExplorePanel().showQueueDialog(queueRows(),
                        index -> removeFromQueue(index, frame.getExplorePanel())));
        updateQueueControls();
    }

    private void wireExplore() {
        // Traduce las acciones de Explorar en búsquedas y operaciones sobre cada canción.
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
            }
        });
        panel.addPlaylistListener(event -> {
            Song song = getSongAt(panel, event.getActionCommand());
            if (song != null) {
                addToPlaylist(song, panel);
            }
        });
    }

    private void wireFavorites() {
        // Mantiene la lista de favoritos actualizada y permite reproducirla como una cola.
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
        // La lectura de metadatos también ocurre en segundo plano para mantener la interfaz activa.
        new Thread(() -> {
            List<Song> found = ritmoSystem.detectMp3(selected);
            for (Song song : found) {
                ritmoSystem.addSong(song);
            }
            String title = "Resultados: archivos MP3 · " + found.size();
            SwingUtilities.invokeLater(() -> showSongs(panel, found, title,
                    found.isEmpty() ? "No se pudieron leer archivos MP3." : " "));
        }, "mp3-metadata").start();
    }

    private void showSongs(ExplorePanel panel, List<Song> songs, String title, String message) {
        // Guarda el mismo orden que la vista usa para resolver los índices de sus acciones.
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

    private void addToQueue(Song song, ExplorePanel panel, boolean playNow) {
        // Valida la pista antes de añadirla y permite iniciar inmediatamente la seleccionada.
        if (!playbackQueue.add(song)) {
            panel.setStatus("No se pudo agregar: se requiere una URL o un archivo MP3 valido.");
            return;
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
    }

    private void removeFromQueue(int index, ExplorePanel panel) {
        // Si se quita la pista activa, detiene el reproductor y pasa a la nueva pista actual.
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
        // Sincroniza el reproductor, el estado visual y el mensaje de la pista seleccionada.
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

    private void addToPlaylist(Song song, ExplorePanel panel) {
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
        String name = selected.toString();
        if (name.equals(createOption)) {
            name = JOptionPane.showInputDialog(frame, "Nombre de la playlist:");
            if (name == null || name.isBlank()) {
                return;
            }
            playlistManager.createPlayList(name.trim());
            name = name.trim();
        }
        for (PlayList playlist : playlistManager.getPlayLists()) {
            if (playlist.getName().equals(name)) {
                if (!playlist.getSongs().contains(song)) {
                    playlist.addSong(song);
                }
                panel.setStatus("Agregada a playlist: " + name);
                return;
            }
        }
    }

    private void refreshFavorites() {
        // Refleja los cambios en Favoritos y actualiza los corazones de los resultados visibles.
        frame.getFavoritesPanel().showFavorites(favoriteRows());
        ExplorePanel explore = frame.getExplorePanel();
        for (int i = 0; i < shownSongs.size(); i++) {
            explore.setFavorite(i, favoritesManager.isFavorite(shownSongs.get(i)));
        }
    }

    private List<String[]> favoriteRows() {
        List<String[]> rows = new ArrayList<>();
        for (Song song : favoritesManager.getFavorites()) {
            int seconds = (int) song.getDuration();
            rows.add(new String[] { song.getTitle(), song.getArtist(), song.getAlbum(),
                    song.getGenre(), String.format("%d:%02d", seconds / 60, seconds % 60) });
        }
        return rows;
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
