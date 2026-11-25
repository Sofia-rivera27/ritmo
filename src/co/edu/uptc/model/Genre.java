package co.edu.uptc.model;

import java.io.File;

/**
 * Género musical que administra el administrador: nombre, color con el que se
 * muestra y una imagen por defecto para las canciones que no tienen portada.
 * @author programacion 2
 */
public class Genre {

    private String name;
    private String color;
    private File defaultImage;

    public Genre(String name, String color, File defaultImage) {
        this.name = name;
        this.color = color;
        this.defaultImage = defaultImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public File getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(File defaultImage) {
        this.defaultImage = defaultImage;
    }
}