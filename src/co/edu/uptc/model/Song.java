package co.edu.uptc.model;

import java.io.File;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String genre;
    private File imagenAlbum; 
    private double duration;
    
	public Song(String title, String artist, String album, String genre, File imagenAlbum, double duration) {
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.genre = genre;
		this.imagenAlbum = imagenAlbum;
		this.duration = duration;
	}

	public Song(String title, String artist, String album, String genre, String imagenAlbumName, double duration) {
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.genre = genre;
		this.imagenAlbum = new File(imagenAlbumName);
		this.duration = duration;
	}	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public File getImagenAlbum() {
		return imagenAlbum;
	}

	public void setImagenAlbum(File imagenAlbum) {
		this.imagenAlbum = imagenAlbum;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "Song [titulo=" + title + ", artista=" + artist + ", album=" + album + ", genre=" + genre
				+ ", imagenAlbumUrl=" + imagenAlbum + "]";
	}
  
}
