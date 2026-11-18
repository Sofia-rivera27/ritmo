package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel principal de la aplicacion: menu a la izquierda, contenido en el centro
 * y controles de reproduccion abajo.
 * SOLO VISTA: quien decide que pasa al presionar un boton es el Presenter.
 */
public class PanelPrincipal extends MyPanel {

    private static final Color TARJETA = new Color(28, 28, 28);
    private static final Color AMARILLO = new Color(255, 193, 7);
    private static final Color TEXTO = new Color(235, 235, 235);
    private static final Color TEXTO_SUAVE = new Color(150, 150, 150);
    private static final Color TEXTO_OSCURO = new Color(20, 20, 20);
    private static final Color TRANSPARENTE = new Color(0, 0, 0, 0);

    private MyPanel options;
    private MyPanel footer;
    private MyPanel contenido;

    private MyButton Home;
    private MyButton Explorar;
    private MyButton Biblioteca;
    private MyButton Playlists;
    private MyButton Historial;
    private MyButton Configuracion;

    private MyButton pausa;
    private MyButton avanzar;
    private MyButton retroceder;

    public PanelPrincipal() {

        cambiarColor(new Color(3, 3, 3));
        cambiarLayout(new BorderLayout());
        setPreferredSize(null);

        crearMenu();
        crearContenido();
        crearFooter();

        add(options, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Al empezar, Home es el boton seleccionado
        seleccionarBoton(Home);
    }

    // ---------------------------------------------------------------
    // PANEL LATERAL (menu)
    // ---------------------------------------------------------------
    private void crearMenu() {
        options = new MyPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS)); // de arriba hacia abajo
        options.cambiarColor(new Color(9, 9, 9));
        options.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        options.setPreferredSize(new Dimension(220, 0));

        JLabel titulo = new JLabel("Ritmo");
        titulo.setForeground(AMARILLO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 23));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        options.add(titulo);
        options.add(Box.createVerticalStrut(25));

        // Todos nacen sin seleccionar (seleccionarBoton pinta el de Home)
        Home = new MyButton("Home", TRANSPARENTE, TEXTO_SUAVE, null);
        Explorar = new MyButton("Explorar", TRANSPARENTE, TEXTO_SUAVE, null);
        Biblioteca = new MyButton("Biblioteca", TRANSPARENTE, TEXTO_SUAVE, null);
        Playlists = new MyButton("Playlists", TRANSPARENTE, TEXTO_SUAVE, null);
        Historial = new MyButton("Historial", TRANSPARENTE, TEXTO_SUAVE, null);
        Configuracion = new MyButton("Configuración", TRANSPARENTE, TEXTO_SUAVE, null);

        agregarBotonMenu(Home);
        agregarBotonMenu(Explorar);
        agregarBotonMenu(Biblioteca);
        agregarBotonMenu(Playlists);
        agregarBotonMenu(Historial);
        agregarBotonMenu(Configuracion);
    }

    // Da el mismo aspecto a todos los botones del menu y los agrega
    private void agregarBotonMenu(MyButton boton) {
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);              // el boton queda pegado a la izquierda
        boton.setHorizontalAlignment(SwingConstants.LEFT);          // el texto queda a la izquierda del boton
        boton.setMargin(new Insets(0, 14, 0, 0));                   // 14 px de espacio antes del texto
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44)); // todo el ancho, 44 de alto
        boton.cambiarRedondeo(10);
        boton.cambiarFuente(Font.PLAIN, 14);

        options.add(boton);
        options.add(Box.createVerticalStrut(5));
    }

    // ---------------------------------------------------------------
    // PANEL CENTRAL (aqui se van cambiando los paneles)
    // ---------------------------------------------------------------
    private void crearContenido() {
        contenido = new MyPanel();
        contenido.setLayout(new BorderLayout());
        contenido.cambiarColor(new Color(3, 3, 3));
    }

    // ---------------------------------------------------------------
    // FOOTER (controles, por ahora no hacen nada)
    // ---------------------------------------------------------------
    private void crearFooter() {
        footer = new MyPanel();
        footer.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        footer.cambiarColor(TARJETA);
        footer.setPreferredSize(new Dimension(0, 90));

        retroceder = new MyButton("⏮", TRANSPARENTE, TEXTO, null);
        pausa = new MyButton("▶", TRANSPARENTE, AMARILLO, null);
        avanzar = new MyButton("⏭", TRANSPARENTE, TEXTO, null);

        retroceder.setPreferredSize(new Dimension(50, 44));
        pausa.setPreferredSize(new Dimension(50, 44));
        avanzar.setPreferredSize(new Dimension(50, 44));

        retroceder.cambiarFuente(Font.BOLD, 18);
        pausa.cambiarFuente(Font.BOLD, 18);
        avanzar.cambiarFuente(Font.BOLD, 18);

        retroceder.cambiarRedondeo(10);
        pausa.cambiarRedondeo(10);
        avanzar.cambiarRedondeo(10);

        footer.add(retroceder);
        footer.add(pausa);
        footer.add(avanzar);
    }

    // ---------------------------------------------------------------
    // Metodos que usa el Presenter
    // ---------------------------------------------------------------

    // Cambia el panel que se ve en el centro
    public void mostrarContenido(MyPanel panel) {
        contenido.removeAll();
        contenido.add(panel, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    // Pinta de amarillo el boton elegido y deja los demas normales
    public void seleccionarBoton(MyButton elegido) {
        pintarBoton(Home, elegido == Home);
        pintarBoton(Explorar, elegido == Explorar);
        pintarBoton(Biblioteca, elegido == Biblioteca);
        pintarBoton(Playlists, elegido == Playlists);
        pintarBoton(Historial, elegido == Historial);
        pintarBoton(Configuracion, elegido == Configuracion);

        // Obliga a redibujar el menu para que se vean los colores nuevos
        options.revalidate();
        options.repaint();
    }

    private void pintarBoton(MyButton boton, boolean seleccionado) {
        if (seleccionado) {
            boton.cambiarColores(AMARILLO, TEXTO_OSCURO, null);
            boton.cambiarFuente(Font.BOLD, 14);
        } else {
            boton.cambiarColores(TRANSPARENTE, TEXTO_SUAVE, null);
            boton.cambiarFuente(Font.PLAIN, 14);
        }
    }

    // Getters de los botones del menu
    public MyButton getHome() {
        return Home;
    }

    public MyButton getExplorar() {
        return Explorar;
    }

    public MyButton getBiblioteca() {
        return Biblioteca;
    }

    public MyButton getPlaylists() {
        return Playlists;
    }

    public MyButton getHistorial() {
        return Historial;
    }

    public MyButton getConfiguracion() {
        return Configuracion;
    }
}
