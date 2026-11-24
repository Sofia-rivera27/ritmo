package co.edu.uptc.view;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.JRootPane;

/**
 * Lo que el Presenter necesita de la ventana. El Presenter trabaja con esta
 * interfaz y no con ViewFrame, así la vista se puede cambiar (o reemplazar por
 * una de prueba) sin tocar el Presenter.
 * @author programacion 2
 */
public interface IFrame {

    // Comandos de acción de los botones del menú
    String HOME = "HOME";
    String EXPLORE = "EXPLORE";
    String LIBRARY = "LIBRARY";
    String FAVORITES = "FAVORITES";
    String PLAYLISTS = "PLAYLISTS";
    String HISTORY = "HISTORY";
    String SETTINGS = "SETTINGS";

    // ---------------------------------------------------------------
    // Pantallas y navegación
    // ---------------------------------------------------------------

    /** Muestra la pantalla de inicio de sesión. */
    void showLogin();

    /** Muestra la aplicación, empezando en Home. */
    void showApp();

    /**
     * Muestra el panel de una sección y resalta su botón del menú.
     *
     * @param section comando de la sección, por ejemplo IFrame.HOME
     */
    void showSection(String section);

    /** Pone al listener a escuchar los botones del menú. */
    void addMenuListener(ActionListener listener);

    /** Vuelve a aplicar el tema actual a todas las pantallas. */
    void refreshTheme();

    // ---------------------------------------------------------------
    // Paneles de cada sección
    // ---------------------------------------------------------------

    MainPanel getMainPanel();

    RegisterPanel getRegisterPanel();

    HomePanel getHomePanel();

    ExplorePanel getExplorePanel();

    LibraryPanel getLibraryPanel();

    FavoritesPanel getFavoritesPanel();

    PlaylistPanel getPlaylistPanel();

    HistoryPanel getHistoryPanel();

    SettingsPanel getSettingsPanel();

    // ---------------------------------------------------------------
    // Ventana (JFrame ya trae estos métodos)
    // ---------------------------------------------------------------

    /** Componente sobre el que se centran los cuadros de diálogo. */
    Component getDialogParent();

    /** Raíz de la ventana, donde se registran los atajos de teclado. */
    JRootPane getRootPane();

    void addWindowListener(WindowListener listener);

    void setVisible(boolean visible);
}