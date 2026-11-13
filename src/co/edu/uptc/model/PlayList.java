package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;

public class PlayList {
	private String name;
	private List<Song> songs;
	
	public PlayList(String name) {
		this.name = name;
		songs = new ArrayList<Song>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Song> getSongs() {
		return songs;
	}

	public void setSongs(List<Song> songs) {
		this.songs = songs;
	}

	@Override
	public String toString() {
		return "PlayList [name=" + name + ", songs=" + songs.toArray() + "]";
	}
}
