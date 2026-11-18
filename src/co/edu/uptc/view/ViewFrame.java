package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Ventana de la aplicacion. Es la unica clase de la vista que conoce el
 * Presenter. Aqui adentro viven el panel principal y los paneles de cada
 * apartado.
 */
public class ViewFrame extends JFrame {

	// esto es para probar q el boton sirviera
	public static final String HOME = "HOME";
	public static final String EXPLORAR = "EXPLORAR";
	public static final String BIBLIOTECA = "BIBLIOTECA";
	public static final String FAVORITES = "FAVORITES";
	public static final String PLAYLISTS = "PLAYLISTS";
	public static final String HISTORIAL = "HISTORIAL";
	public static final String CONFIGURACION = "CONFIGURACION";

	private PanelPrincipal panelPrincipal;

	// Un panel por cada boton del menu (provisionales, luego se cambian)
	private MyPanel panelHome;
	private MyPanel panelExplorar;
	private MyPanel panelBiblioteca;
	private MyPanel panelPlaylists;
	private MyPanel panelHistorial;
	private MyPanel panelConfiguracion;
	private FavoritesPanel panelFavorites;

	public ViewFrame() {
		super("Aplicacion Musica");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 800);
		setLocationRelativeTo(null);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		getContentPane().setBackground(new Color(18, 18, 18));
		getContentPane().setLayout(new BorderLayout());
//esto es provisional tambien ajajjajaja
		panelPrincipal = new PanelPrincipal();
		getContentPane().add(panelPrincipal, BorderLayout.CENTER);

		panelHome = new PanelHome();
		panelExplorar = new PanelSeccion("Explorar");
		panelBiblioteca = new PanelSeccion("Biblioteca");
		panelPlaylists = new PanelSeccion("Playlists");
		panelHistorial = new PanelSeccion("Historial");
		panelConfiguracion = new PanelSeccion("Configuración");
		panelFavorites = new FavoritesPanel();

		// Cada boton del menu avisa con su nombre cuando se presiona
		panelPrincipal.getHome().setActionCommand(HOME);
		panelPrincipal.getExplorar().setActionCommand(EXPLORAR);
		panelPrincipal.getBiblioteca().setActionCommand(BIBLIOTECA);
		panelPrincipal.getPlaylists().setActionCommand(PLAYLISTS);
		panelPrincipal.getHistorial().setActionCommand(HISTORIAL);
		panelPrincipal.getConfiguracion().setActionCommand(CONFIGURACION);
		panelPrincipal.getFavorites().setActionCommand(FAVORITES);

		mostrarApartado(HOME); // se empieza en Home
	}

	// Pone al listener (el Presenter) a escuchar los botones del menu
	public void agregarListener(ActionListener listener) {
		panelPrincipal.getHome().addActionListener(listener);
		panelPrincipal.getExplorar().addActionListener(listener);
		panelPrincipal.getBiblioteca().addActionListener(listener);
		panelPrincipal.getPlaylists().addActionListener(listener);
		panelPrincipal.getHistorial().addActionListener(listener);
		panelPrincipal.getConfiguracion().addActionListener(listener);
		panelPrincipal.getFavorites().addActionListener(listener);
	}

	// Muestra el panel de ese apartado y pinta de amarillo su boton
	public void mostrarApartado(String apartado) {
		if (apartado.equals(HOME)) {
			cambiarPanel(panelHome, panelPrincipal.getHome());
		} else if (apartado.equals(EXPLORAR)) {
			cambiarPanel(panelExplorar, panelPrincipal.getExplorar());
		} else if (apartado.equals(BIBLIOTECA)) {
			cambiarPanel(panelBiblioteca, panelPrincipal.getBiblioteca());
		} else if (apartado.equals(PLAYLISTS)) {
			cambiarPanel(panelPlaylists, panelPrincipal.getPlaylists());
		} else if (apartado.equals(HISTORIAL)) {
			cambiarPanel(panelHistorial, panelPrincipal.getHistorial());
		} else if (apartado.equals(CONFIGURACION)) {
			cambiarPanel(panelConfiguracion, panelPrincipal.getConfiguracion());
		} else if (apartado.equals(FAVORITES)) {
			cambiarPanel(panelFavorites, panelPrincipal.getFavorites());
		}
	}

	private void cambiarPanel(MyPanel panel, MyButton boton) {
		panelPrincipal.mostrarContenido(panel);
		panelPrincipal.seleccionarBoton(boton);
	}
}
