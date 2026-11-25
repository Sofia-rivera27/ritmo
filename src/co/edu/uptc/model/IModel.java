package co.edu.uptc.model;

import java.io.File;
import java.util.List;

/**
 * Operaciones del modelo que usa el Presenter.
 * @author programacion 2
 */
public interface IModel {

    List<Song> detectMp3InFolder(File folder);

    List<Song> detectMp3(List<File> files);

    List<Song> getSongs();

    void addSong(Song song);

    void removeSong(Song song);

    void updateSong(Song song, String title, String artist, String album, String genre, File albumImage);

    void authorizeFolder(File folder);

    void restoreAuthorizedFolder(File folder);

    List<Song> searchSongs(String text, SearchCriteria criteria);
    
    List<File> getAuthorizedFolders();

    void removeAuthorizedFolder(File folder);

    int countMp3InFolder(File folder);
    
}