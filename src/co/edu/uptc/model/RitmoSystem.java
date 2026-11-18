package co.edu.uptc.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RitmoSystem extends Model {
	private List<UserListener> users;
	private Admin admin;
	
	// Variable para la ruta del JSON (para poder cambiarla)
	private String jsonFilePath = "data" + File.separator + "ritmo_system.json";

	public RitmoSystem() {
		users = new ArrayList<UserListener>();
		admin = new Admin();
	}

	
	// Método para cambiar la carpeta donde se guarda el JSON
	public void setJsonDirectory(String directoryPath) {
		if (directoryPath != null && !directoryPath.trim().isEmpty()) {
			this.jsonFilePath = directoryPath + File.separator + "ritmo_system.json";
		}
	}

	public void saveData() {
		File file = new File(jsonFilePath);
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}

		StringBuilder json = new StringBuilder();
		json.append("{\n  \"users\": [\n");

		for (int i = 0; i < users.size(); i++) {
			UserListener u = users.get(i);
			json.append("    {\n");
			json.append("      \"name\": \"").append(escapeJson(u.getName())).append("\",\n");
			json.append("      \"password\": \"").append(escapeJson(u.getPassword())).append("\",\n");
			json.append("      \"library\": {\n");

			Library lib = u.getLibrary();
			String libName = (lib != null) ? lib.getName() : u.getName() + " Library";
			json.append("        \"name\": \"").append(escapeJson(libName)).append("\",\n");
			json.append("        \"playlists\": {\n");

			if (lib != null && lib.getPlaylists() != null) {
				int pIndex = 0;
				int pSize = lib.getPlaylists().size();
				for (Map.Entry<String, PlayList> entry : lib.getPlaylists().entrySet()) {
					PlayList pl = entry.getValue();
					json.append("          \"").append(escapeJson(entry.getKey())).append("\": {\n");
					json.append("            \"name\": \"").append(escapeJson(pl.getName())).append("\",\n");
					json.append("            \"songs\": [\n");

					List<Song> songs = pl.getSongs();
					for (int s = 0; s < songs.size(); s++) {
					    Song song = songs.get(s);
					    json.append("              {");
					    
					    // Verificamos si la imagen es null para no guardar la palabra "null"
					    String imgPath = (song.getImagenAlbum() != null) ? song.getImagenAlbum().getAbsolutePath() : "";
					    
					    json.append("\"image\": \"").append(escapeJson(imgPath)).append("\", ");
					    json.append("\"title\": \"").append(escapeJson(song.getTitle())).append("\", ");
					    json.append("\"artist\": \"").append(escapeJson(song.getArtist())).append("\", ");
					    json.append("\"album\": \"").append(escapeJson(song.getAlbum())).append("\", ");
					    json.append("\"genre\": \"").append(escapeJson(song.getGenre())).append("\"");
					    json.append("}").append(s < songs.size() - 1 ? "," : "").append("\n");
					}

					json.append("            ]\n");
					json.append("          }").append(++pIndex < pSize ? "," : "").append("\n");
				}
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

	public void loadData() {
		File file = new File(jsonFilePath);
		if (!file.exists()) {
			return;
		}

		try {
			String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			this.users.clear();

			Matcher userMatcher = Pattern.compile(
				"\"name\":\\s*\"(.*?)\",\\s*\"password\":\\s*\"(.*?)\",\\s*\"library\":\\s*\\{\\s*\"name\":\\s*\"(.*?)\",\\s*\"playlists\":\\s*\\{(.*?)\\}\\s*\\}\\s*\\}",
				Pattern.DOTALL
			).matcher(content);

			while (userMatcher.find()) {
				String uName = unescapeJson(userMatcher.group(1));
				String uPass = unescapeJson(userMatcher.group(2));
				String libName = unescapeJson(userMatcher.group(3));
				String playlistsBlock = userMatcher.group(4);

				Library library = new Library(libName);

				Matcher plMatcher = Pattern.compile(
					"\"(.*?)\":\\s*\\{\\s*\"name\":\\s*\"(.*?)\",\\s*\"songs\":\\s*\\[(.*?)\\]\\s*\\}",
					Pattern.DOTALL
				).matcher(playlistsBlock);

				while (plMatcher.find()) {
					String plName = unescapeJson(plMatcher.group(2));
					String songsBlock = plMatcher.group(3);
					PlayList playList = new PlayList(plName);

					Matcher songMatcher = Pattern.compile(
						"\\{\"image\":\\s*\"(.*?)\",\\s*\"title\":\\s*\"(.*?)\",\\s*\"artist\":\\s*\"(.*?)\",\\s*\"album\":\\s*\"(.*?)\",\\s*\"genre\":\\s*\"(.*?)\"\\}"
					).matcher(songsBlock);

					while (songMatcher.find()) {
					    // 1. Extraemos los datos del JSON
					    String imgStr = unescapeJson(songMatcher.group(1));
					    String title = unescapeJson(songMatcher.group(2));
					    String artist = unescapeJson(songMatcher.group(3));
					    String album = unescapeJson(songMatcher.group(4));
					    String genre = unescapeJson(songMatcher.group(5));

					    // 2. Convertimos la imagen (que es un String en el JSON) a File
					    File imagenFile = null;
					    if (imgStr != null && !imgStr.isEmpty() && !imgStr.equals("null")) {
					        imagenFile = new File(imgStr);
					    }

					    // 3. Creamos la canción usando el constructor que tiene 7 parámetros
					    // Como no guardamos la duración ni la ruta en el JSON (por ahora), ponemos 0.0 y null
					    Song song = new Song(title, artist, album, genre, imagenFile, 0.0, null);
					    
					    // 4. Agregamos la canción a la playlist
					    playList.addSong(song);
					}

					library.getPlaylists().put(plName, playList);
				}

				this.users.add(new UserListener(uName, uPass, library));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Auxiliares para JSON
	private String escapeJson(String text) {
		if (text == null) return "";
		return text.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private String unescapeJson(String text) {
		if (text == null) return "";
		return text.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	// Getters y Setters
	public List<UserListener> getUsers() { return users; }
	public void setUsers(List<UserListener> users) { this.users = users; }
	public Admin getAdmin() { return admin; }
	public void setAdmin(Admin admin) { this.admin = admin; }	
}