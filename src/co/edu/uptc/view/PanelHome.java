package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;

public class PanelHome extends MyPanel {

    private static final Color AMARILLO = new Color(255, 193, 7);
    private static final Color TEXTO = new Color(235, 235, 235);
    private static final Color CUADRO = new Color(41, 41, 41);
    private static final Color FONDO = new Color(17, 17, 17);
    private static final Color TRANSPARENTE = new Color(0, 0, 0, 0);

    private MyButton artista1;
    private MyButton artista2;
    private MyButton artista3;
    private MyButton artista4;
    private MyButton artista5;
    private MyButton artista6;
    private MyButton artista7;
    private MyButton artista8;
    
    private MyPanel panelArtistas;

    private MyButton album1;
    private MyButton album2;
    private MyButton album3;
    private MyButton album4;
    private MyButton album5;
    private MyButton album6;
    private MyPanel panelAlbumes;

    public PanelHome() {
        cambiarColor(FONDO);
        cambiarLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // de arriba hacia abajo
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        panelArtistas = crearFila(175);
        panelAlbumes = crearFila(150);

        crearArtistas();
        crearAlbumes();

        add(crearTitulo("Recomendados"));
        add(Box.createVerticalStrut(12));
        add(panelArtistas);
        add(Box.createVerticalStrut(20));

        add(crearTitulo("Álbumes"));
        add(Box.createVerticalStrut(12));
        add(panelAlbumes);

        add(Box.createVerticalGlue()); // empuja todo hacia arriba
    }

    // ---------------------------------------------------------------
    // Crear los botones (aqui cambias los nombres)
    // ---------------------------------------------------------------
    private void crearArtistas() {
        artista1 = agregarArtista("Los Cantores");
        artista2 = agregarArtista("Vera Luz");
        artista3 = agregarArtista("Mateo Rivas");
        artista4 = agregarArtista("Duo Ámbar");
        artista5 = agregarArtista("Sol y Sal");
        artista6 = agregarArtista("Nose");
        artista7 = agregarArtista("S");
        artista8 = agregarArtista("SW");
        
    
    }

    private void crearAlbumes() {
        album1 = agregarAlbum("Horizonte");
        album2 = agregarAlbum("Ciudad");
        album3 = agregarAlbum("Raíces");
        album4 = agregarAlbum("Brasas");
        album5 = agregarAlbum("Costa");
        album6 = agregarAlbum("Eco");
    }

    // ---------------------------------------------------------------
    // Crean el boton, lo meten en la fila y lo devuelven
    // ---------------------------------------------------------------

    // Circulo con la inicial del artista
    private MyButton agregarArtista(String nombre) {
        MyButton boton = new MyButton(nombre.substring(0, 1), CUADRO, AMARILLO, null);//agarra la primer letra y muestra, eso se cambia por la imagen
        boton.cambiarTamano(136, 136);
        boton.cambiarRedondeo(68); // la mitad del tamano = circulo
        boton.cambiarFuente(Font.BOLD, 44);

        agregarEnFila(panelArtistas, crearColumna(boton, nombre, 136, 175));
        return boton;
    }

    // Cuadrado redondeado con un disco
    private MyButton agregarAlbum(String nombre) {
        MyButton boton = new MyButton("◉", CUADRO, AMARILLO, null);
        boton.cambiarTamano(115, 115);
        boton.cambiarRedondeo(8);
        boton.cambiarFuente(Font.PLAIN, 36);

        agregarEnFila(panelAlbumes, crearColumna(boton, nombre, 115, 150));
        return boton;
    }

    // ---------------------------------------------------------------
    // Ayudas
    // ---------------------------------------------------------------

    // Panelito con el boton arriba y el nombre debajo
    private MyPanel crearColumna(MyButton boton, String nombre, int ancho, int alto) {
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 13));
        etiqueta.setForeground(TEXTO);

        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);

        MyPanel columna = new MyPanel();
        columna.cambiarColor(TRANSPARENTE);
        columna.cambiarLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.cambiarTamaño(ancho, alto);
        columna.setMaximumSize(new Dimension(ancho, alto)); // para que no se estire
        columna.add(boton);
        columna.add(Box.createVerticalStrut(10));
        columna.add(etiqueta);
        return columna;
    }

    // Agrega a la fila, con 20 px de espacio antes (menos la primera)
    private void agregarEnFila(MyPanel fila, MyPanel columna) {
        if (fila.getComponentCount() > 0) {
            fila.add(Box.createHorizontalStrut(20));
        }
        fila.add(columna);
    }

    // Fila horizontal de izquierda a derecha
    private MyPanel crearFila(int alto) {
        MyPanel fila = new MyPanel();
        fila.cambiarColor(TRANSPARENTE);
        fila.cambiarLayout(new BoxLayout(fila, BoxLayout.X_AXIS));
        fila.setPreferredSize(null); // que mida lo que midan sus botones
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto)); // no se estira hacia abajo
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        return fila;
    }

    private JLabel crearTitulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return titulo;
    }

    // ---------------------------------------------------------------
    // Getters (para que el Presenter les ponga listener despues)
    // ---------------------------------------------------------------
    public MyButton getArtista1() {
        return artista1;
    }

    public MyButton getAlbum1() {
        return album1;
    }
}