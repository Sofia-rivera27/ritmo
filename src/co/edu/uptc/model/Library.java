package co.edu.uptc.model;

import java.util.HashMap;
import java.util.Map;

public class Library {
	private String name;
	private Map<String, PlayList> playlists;
	
	public Library(String name) {
		this.name = name;
		playlists = new HashMap<String, PlayList>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, PlayList> getPlaylists() {
		return playlists;
	}

	public void setPlaylists(Map<String, PlayList> playlists) {
		this.playlists = playlists;
	}
}
