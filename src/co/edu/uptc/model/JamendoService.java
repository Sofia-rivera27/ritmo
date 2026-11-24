package co.edu.uptc.model;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consultas basicas a la API publica de Jamendo.
 * @author programacion 2
 */
public class JamendoService {

    private static final String BASE_URL = "https://api.jamendo.com/v3.0/tracks/";
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 500;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String getClientId() {
        String id = System.getProperty("jamendo.clientId");
        if (id == null || id.isBlank()) {
            id = System.getenv("JAMENDO_CLIENT_ID");
        }
        if (id == null || id.isBlank()) {
            id = "041ffbf6";
        }
        return id == null ? "" : id.trim();
    }

    public void setClientId(String clientId) {
        if (clientId != null) {
            System.setProperty("jamendo.clientId", clientId.trim());
        }
    }

    public List<Song> search(String query) throws Exception {
        return searchTracks("search", query, 12);
    }

    public List<Song> searchByGenre(String genre) throws Exception {
        return searchGenreTracks(genre, 12);
    }

    public List<Song> recommendByGenre(String genre) throws Exception {
        return searchGenreTracks(genre, 30);
    }

    private List<Song> searchGenreTracks(String genre, int limit) throws Exception {
        String tag = normalizeGenre(genre);
        List<Song> songs = searchTracks("tags", tag, limit);
        return songs.isEmpty() ? searchTracks("fuzzytags", tag, limit) : songs;
    }

    private String normalizeGenre(String genre) {
        String tag = genre == null ? "" : genre.trim().toLowerCase(Locale.ROOT);
        // La API espera el tag en inglés aunque la vista muestre "Electrónica".
        if (tag.equals("electrónica") || tag.equals("electronica")) {
            tag = "electronic";
        }
        return tag;
    }

    private List<Song> searchTracks(String parameter, String query, int limit) throws Exception {
        String clientId = getClientId();
        if (clientId.isBlank()) {
            throw new IllegalStateException(
                    "Configura la variable de entorno JAMENDO_CLIENT_ID.");
        }

        String url = BASE_URL + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&format=json&limit=" + limit + "&" + parameter + "="
                + URLEncoder.encode(query == null ? "" : query.trim(), StandardCharsets.UTF_8)
                + "&audioformat=mp32&include=musicinfo";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .GET()
                .build();
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            HttpResponse<String> response;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                waitBeforeRetry(attempt);
                continue;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }

            if (response.statusCode() != 200) {
                IOException error = new IOException("Jamendo respondio con estado " + response.statusCode());
                if (!isRetryableStatus(response.statusCode()) || attempt == MAX_ATTEMPTS) {
                    throw error;
                }
                waitBeforeRetry(attempt);
                continue;
            }

            List<Song> songs = parseTracks(response.body());
            if (!songs.isEmpty() || attempt == MAX_ATTEMPTS) {
                return songs;
            }
            waitBeforeRetry(attempt);
        }
        return List.of();
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 408 || statusCode == 429 || statusCode >= 500;
    }

    private void waitBeforeRetry(int attempt) throws InterruptedException {
        try {
            Thread.sleep(RETRY_DELAY_MS * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private List<Song> parseTracks(String json) throws IOException {
        List<Song> songs = new ArrayList<>();
        int resultsIndex = json.indexOf("\"results\"");
        if (resultsIndex < 0) {
            throw new IOException("La respuesta de Jamendo no contiene resultados.");
        }
        int arrayStart = json.indexOf('[', resultsIndex);
        if (arrayStart < 0) {
            throw new IOException("La respuesta de Jamendo tiene un formato invalido.");
        }

        // Se cuentan llaves respetando cadenas para no cortar musicinfo anidado.
        boolean inString = false;
        boolean escaped = false;
        boolean arrayClosed = false;
        int objectStart = -1;
        int depth = 0;
        for (int i = arrayStart + 1; i < json.length(); i++) {
            char current = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                if (depth++ == 0) {
                    objectStart = i;
                }
            } else if (current == '}' && depth > 0 && --depth == 0) {
                addTrack(songs, json.substring(objectStart, i + 1));
            } else if (current == ']' && depth == 0) {
                arrayClosed = true;
                break;
            }
        }
        if (!arrayClosed) {
            throw new IOException("La respuesta de Jamendo llego incompleta.");
        }
        return songs;
    }

    private void addTrack(List<Song> songs, String track) {
        String title = getString(track, "name");
        String artist = getString(track, "artist_name");
        String album = getString(track, "album_name");
        String genre = getFirstGenre(track);
        String audio = getString(track, "audio");
        double duration = getDouble(track, "duration");
        if (!title.isBlank() && !audio.isBlank()) {
            songs.add(new Song(title, valueOrUnknown(artist), valueOrUnknown(album),
                    displayGenre(genre), null, duration, audio));
        }
    }

    private String getFirstGenre(String json) {
        Matcher matcher = Pattern.compile("\"genres\"\\s*:\\s*\\[\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        return matcher.find() ? unescape(matcher.group(1)) : "";
    }

    private String displayGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            return "Desconocido";
        }
        if (genre.equalsIgnoreCase("electronic")) {
            return "Electrónica";
        }
        return Character.toUpperCase(genre.charAt(0)) + genre.substring(1);
    }

    private String getString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        return matcher.find() ? unescape(matcher.group(1)) : "";
    }

    private double getDouble(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9.]+)")
                .matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "Desconocido" : value;
    }

    private String unescape(String value) {
        StringBuilder decoded = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current != '\\' || i + 1 >= value.length()) {
                decoded.append(current);
                continue;
            }
            char escaped = value.charAt(++i);
            switch (escaped) {
                case '"':
                case '\\':
                case '/':
                    decoded.append(escaped);
                    break;
                case 'b':
                    decoded.append('\b');
                    break;
                case 'f':
                    decoded.append('\f');
                    break;
                case 'n':
                case 'r':
                case 't':
                    decoded.append(' ');
                    break;
                case 'u':
                    if (i + 4 < value.length()) {
                        try {
                            decoded.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            decoded.append("\\u");
                        }
                    } else {
                        decoded.append("\\u");
                    }
                    break;
                default:
                    decoded.append('\\').append(escaped);
                    break;
            }
        }
        return decoded.toString();
    }
}
