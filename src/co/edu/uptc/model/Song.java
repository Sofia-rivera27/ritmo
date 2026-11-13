package co.edu.uptc.model;

import java.io.File;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String genre;
    private File imagenAlbum; 
    
    public Song(String title, String artist, String album,String genre, File imagenAlbum) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.imagenAlbum = imagenAlbum;
    }

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getTitulo() {
		return title;
	}

	public void setTitulo(String title) {
		this.title = title;
	}

	public String getArtista() {
		return artist;
	}

	public void setArtista(String artista) {
		this.artist = artista;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public File getImagenAlbumUrl() {
		return imagenAlbum;
	}

	public void setImagenAlbumUrl(File imagenAlbum) {
		this.imagenAlbum = imagenAlbum;
	}

	@Override
	public String toString() {
		return "Song [titulo=" + title + ", artista=" + artist + ", album=" + album + ", genre=" + genre
				+ ", imagenAlbumUrl=" + imagenAlbum + "]";
	}
  
}
