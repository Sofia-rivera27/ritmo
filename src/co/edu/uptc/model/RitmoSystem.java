package co.edu.uptc.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sistema principal de la aplicación. Guarda los usuarios con sus bibliotecas
 * y los guarda o carga desde un archivo JSON.
 */
public class RitmoSystem extends Model {

    private static final String JSON_FILE_NAME = "ritmo_system.json";

    private static final Pattern USER_PATTERN = Pattern.compile(
            "\"name\":\\s*\"(.*?)\",\\s*\"password\":\\s*\"(.*?)\",\\s*\"library\":\\s*\\{\\s*\"name\":\\s*\"(.*?)\",\\s*\"playlists\":\\s*\\{(.*?)\\}\\s*\\}\\s*\\}",
            Pattern.DOTALL);
    private static final Pattern PLAYLIST_PATTERN = Pattern.compile(
            "\"(.*?)\":\\s*\\{\\s*\"name\":\\s*\"(.*?)\",\\s*\"songs\":\\s*\\[(.*?)\\]\\s*\\}",
            Pattern.DOTALL);
    private static final Pattern SONG_PATTERN = Pattern.compile(
            "\\{\"image\":\\s*\"(.*?)\",\\s*\"title\":\\s*\"(.*?)\",\\s*\"artist\":\\s*\"(.*?)\",\\s*\"album\":\\s*\"(.*?)\",\\s*\"genre\":\\s*\"(.*?)\"\\}");

    private List<UserListener> users;
    private Admin admin;

    /** Ruta del archivo JSON. Se puede cambiar con setJsonDirectory. */
    private String jsonFilePath = "data" + File.separator + JSON_FILE_NAME;

    public RitmoSystem() {
        users = new ArrayList<>();
        admin = new Admin();
    }

    /**
     * Cambia la carpeta donde se guarda el archivo JSON.
     *
     * @param directoryPath ruta de la carpeta
     */
    public void setJsonDirectory(String directoryPath) {
        if (directoryPath != null && !directoryPath.trim().isEmpty()) {
            this.jsonFilePath = directoryPath + File.separator + JSON_FILE_NAME;
        }
    }

    /**
     * Guarda todos los usuarios, con sus bibliotecas y playlists, en el archivo JSON.
     */
    public void saveData() {
        File file = new File(jsonFilePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"users\": [\n");

        for (int i = 0; i < users.size(); i++) {
            UserListener user = users.get(i);
            json.append("    {\n");
            json.append("      \"name\": \"").append(escapeJson(user.getName())).append("\",\n");
            json.append("      \"password\": \"").append(escapeJson(user.getPassword())).append("\",\n");
            json.append("      \"library\": {\n");

            Library library = user.getLibrary();
            String libraryName = (library != null) ? library.getName() : user.getName() + " Library";
            json.append("        \"name\": \"").append(escapeJson(libraryName)).append("\",\n");
            json.append("        \"playlists\": {\n");

            if (library != null && library.getPlaylists() != null) {
                appendPlaylists(json, library.getPlaylists());
            }

            json.append("        }\n");
            json.append("      }\n");
            json.append("    }").append(i < users.size() - 1 ? "," : "").append("\n");
        }

        json.append("  ]\n}");

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendPlaylists(StringBuilder json, Map<String, PlayList> playlists) {
        int index = 0;
        int size = playlists.size();
        for (Map.Entry<String, PlayList> entry : playlists.entrySet()) {
            PlayList playList = entry.getValue();
            json.append("          \"").append(escapeJson(entry.getKey())).append("\": {\n");
            json.append("            \"name\": \"").append(escapeJson(playList.getName())).append("\",\n");
            json.append("            \"songs\": [\n");

            List<Song> songs = playList.getSongs();
            for (int s = 0; s < songs.size(); s++) {
                Song song = songs.get(s);
                // Si no hay imagen se guarda un texto vacío en lugar de la palabra "null"
                String imagePath = (song.getAlbumImage() != null) ? song.getAlbumImage().getAbsolutePath() : "";

                json.append("              {");
                json.append("\"image\": \"").append(escapeJson(imagePath)).append("\", ");
                json.append("\"title\": \"").append(escapeJson(song.getTitle())).append("\", ");
                json.append("\"artist\": \"").append(escapeJson(song.getArtist())).append("\", ");
                json.append("\"album\": \"").append(escapeJson(song.getAlbum())).append("\", ");
                json.append("\"genre\": \"").append(escapeJson(song.getGenre())).append("\"");
                json.append("}").append(s < songs.size() - 1 ? "," : "").append("\n");
            }

            json.append("            ]\n");
            json.append("          }").append(++index < size ? "," : "").append("\n");
        }
    }

    /**
     * Carga los usuarios, con sus bibliotecas y playlists, desde el archivo JSON.
     * Si el archivo no existe, no carga nada.
     */
    public void loadData() {
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            users.clear();

            Matcher userMatcher = USER_PATTERN.matcher(content);
            while (userMatcher.find()) {
                String userName = unescapeJson(userMatcher.group(1));
                String password = unescapeJson(userMatcher.group(2));
                String libraryName = unescapeJson(userMatcher.group(3));
                String playlistsBlock = userMatcher.group(4);

                Library library = new Library(libraryName);
                loadPlaylists(playlistsBlock, library);
                users.add(new UserListener(userName, password, library));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlaylists(String playlistsBlock, Library library) {
        Matcher playlistMatcher = PLAYLIST_PATTERN.matcher(playlistsBlock);
        while (playlistMatcher.find()) {
            String playlistName = unescapeJson(playlistMatcher.group(2));
            String songsBlock = playlistMatcher.group(3);
            PlayList playList = new PlayList(playlistName);

            Matcher songMatcher = SONG_PATTERN.matcher(songsBlock);
            while (songMatcher.find()) {
                String imagePath = unescapeJson(songMatcher.group(1));
                String title = unescapeJson(songMatcher.group(2));
                String artist = unescapeJson(songMatcher.group(3));
                String album = unescapeJson(songMatcher.group(4));
                String genre = unescapeJson(songMatcher.group(5));

                File imageFile = null;
                if (!imagePath.isEmpty() && !imagePath.equals("null")) {
                    imageFile = new File(imagePath);
                }

                // La duración y la ruta todavía no se guardan en el JSON, por eso se usan 0.0 y null
                try {
                    playList.addSong(new Song(title, artist, album, genre, imageFile, 0.0, null));
                } catch (IllegalArgumentException e) {
                    // Canción repetida en un JSON antiguo: se ignora para que la carga no falle
                }
            }

            library.getPlaylists().put(playlistName, playList);
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public List<UserListener> getUsers() {
        return users;
    }

    public void setUsers(List<UserListener> users) {
        this.users = users;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
}