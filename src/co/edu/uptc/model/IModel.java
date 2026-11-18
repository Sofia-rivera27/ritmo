package co.edu.uptc.model;

import java.io.File;
import java.util.List;

public interface IModel {

	List<Song> detectMp3InFolder(File folder);

	List<Song> detectMp3(List<File> files);
}
