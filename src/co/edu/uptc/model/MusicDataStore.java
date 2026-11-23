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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author programacion 2
 * MusicDataStore
 */
public class MusicDataStore {

    private static final String FILE_NAME = "musica.properties";
    private static final String APP_FOLDER = ".ritmo";
    private static final String SEPARATOR = ",";

    private final File file;

    public MusicDataStore() {
        this(new File(System.getProperty("user.home"), APP_FOLDER + File.separator + FILE_NAME));
    }

    public MusicDataStore(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void save(MusicData data) throws IOException {
        Properties properties = new Properties();
        Map<Song, Integer> ids = new LinkedHashMap<>();


        for (Song song : data.getLibrary()) {
            idOf(song, ids);
        }
        for (PlayList playList : data.getPlaylists()) {
            for (Song song : playList.getSongs()) {
                idOf(song, ids);
            }
        }
        for (Song song : data.getFavorites()) {
            idOf(song, ids);
        }
        for (HistoryEntry entry : data.getHistory()) {
            idOf(entry.getSong(), ids);
        }


        properties.setProperty("song.count", String.valueOf(ids.size()));
        for (Map.Entry<Song, Integer> entry : ids.entrySet()) {
            writeSong(properties, "song." + entry.getValue() + ".", entry.getKey());
        }


        properties.setProperty("library", joinIds(data.getLibrary(), ids));
        properties.setProperty("favorites", joinIds(data.getFavorites(), ids));

        properties.setProperty("playlist.count", String.valueOf(data.getPlaylists().size()));
        for (int i = 0; i < data.getPlaylists().size(); i++) {
            PlayList playList = data.getPlaylists().get(i);
            properties.setProperty("playlist." + i + ".name", playList.getName());
            properties.setProperty("playlist." + i + ".songs", joinIds(playList.getSongs(), ids));
        }

        properties.setProperty("history.count", String.valueOf(data.getHistory().size()));
        for (int i = 0; i < data.getHistory().size(); i++) {
            HistoryEntry entry = data.getHistory().get(i);
            properties.setProperty("history." + i + ".song", String.valueOf(ids.get(entry.getSong())));
            properties.setProperty("history." + i + ".time", entry.getPlayedAt().toString());
        }

        writeFile(properties);
    }

    private int idOf(Song song, Map<Song, Integer> ids) {
        Integer id = ids.get(song);
        if (id == null) {
            id = ids.size();
            ids.put(song, id);
        }
        return id;
    }

    private void writeSong(Properties properties, String prefix, Song song) {
        properties.setProperty(prefix + "title", textOrEmpty(song.getTitle()));
        properties.setProperty(prefix + "artist", textOrEmpty(song.getArtist()));
        properties.setProperty(prefix + "album", textOrEmpty(song.getAlbum()));
        properties.setProperty(prefix + "genre", textOrEmpty(song.getGenre()));
        properties.setProperty(prefix + "duration", String.valueOf(song.getDuration()));
        properties.setProperty(prefix + "file", textOrEmpty(song.getFilePath()));
        String image = song.getAlbumImage() == null ? "" : song.getAlbumImage().getPath();
        properties.setProperty(prefix + "image", image);
    }

    private String joinIds(List<Song> songs, Map<Song, Integer> ids) {
        StringBuilder text = new StringBuilder();
        for (Song song : songs) {
            if (text.length() > 0) {
                text.append(SEPARATOR);
            }
            text.append(ids.get(song));
        }
        return text.toString();
    }

    private void writeFile(Properties properties) throws IOException {
        File folder = file.getAbsoluteFile().getParentFile();
        if (folder != null && !folder.exists() && !folder.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + folder);
        }
        File temp = new File(file.getAbsolutePath() + ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8)) {
            properties.store(writer, "Ritmo - datos de musica");
        }
        Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }


    public MusicData load() throws IOException {
        MusicData data = new MusicData();
        if (!file.exists()) {
            return data;
        }

        Properties properties = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }


        Map<Integer, Song> songs = new LinkedHashMap<>();
        int songCount = readInt(properties, "song.count");
        for (int i = 0; i < songCount; i++) {
            Song song = readSong(properties, "song." + i + ".");
            if (song != null) {
                songs.put(i, song);
            }
        }


        data.getLibrary().addAll(readSongList(properties.getProperty("library"), songs));
        data.getFavorites().addAll(readSongList(properties.getProperty("favorites"), songs));

        int playlistCount = readInt(properties, "playlist.count");
        for (int i = 0; i < playlistCount; i++) {
            String name = properties.getProperty("playlist." + i + ".name");
            if (name == null || name.isBlank()) {
                continue;
            }
            PlayList playList = new PlayList(name);
            for (Song song : readSongList(properties.getProperty("playlist." + i + ".songs"), songs)) {
                if (!playList.getSongs().contains(song)) {
                    playList.getSongs().add(song);
                }
            }
            data.getPlaylists().add(playList);
        }

        int historyCount = readInt(properties, "history.count");
        for (int i = 0; i < historyCount; i++) {
            Song song = songs.get(parseInt(properties.getProperty("history." + i + ".song")));
            LocalDateTime time = parseTime(properties.getProperty("history." + i + ".time"));
            if (song != null && time != null) {
                data.getHistory().add(new HistoryEntry(song, time));
            }
        }

        for (Song song : data.getFavorites()) {
            song.setFavorite(true);
        }
        return data;
    }

    private Song readSong(Properties properties, String prefix) {
        String title = properties.getProperty(prefix + "title", "");
        if (title.isBlank()) {
            return null;
        }
        String filePath = properties.getProperty(prefix + "file", "");
        String image = properties.getProperty(prefix + "image", "");
        double duration;
        try {
            duration = Double.parseDouble(properties.getProperty(prefix + "duration", "0"));
        } catch (NumberFormatException e) {
            duration = 0;
        }
        return new Song(title,
                properties.getProperty(prefix + "artist", ""),
                properties.getProperty(prefix + "album", ""),
                properties.getProperty(prefix + "genre", ""),
                image.isBlank() ? null : new File(image),
                duration,
                filePath.isBlank() ? null : filePath);
    }

    private List<Song> readSongList(String ids, Map<Integer, Song> songs) {
        List<Song> result = new ArrayList<>();
        if (ids == null || ids.isBlank()) {
            return result;
        }
        for (String id : ids.split(SEPARATOR)) {
            Song song = songs.get(parseInt(id));
            if (song != null) {
                result.add(song);
            }
        }
        return result;
    }

    private int readInt(Properties properties, String key) {
        Integer value = parseInt(properties.getProperty(key));
        return value == null ? 0 : Math.max(0, value);
    }

    private Integer parseInt(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseTime(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String textOrEmpty(String text) {
        return text == null ? "" : text;
    }
}