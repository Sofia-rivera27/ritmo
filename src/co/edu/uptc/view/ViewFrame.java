package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;

/**
 * Ventana de la aplicación. Es la única clase de la vista que conoce el
 * Presenter. Aquí viven el panel principal y el panel de cada sección.
 */
public class ViewFrame extends JFrame {

    // Comandos de acción de los botones del menú
    public static final String HOME = "HOME";
    public static final String EXPLORE = "EXPLORE";
    public static final String LIBRARY = "LIBRARY";
    public static final String FAVORITES = "FAVORITES";
    public static final String PLAYLISTS = "PLAYLISTS";
    public static final String HISTORY = "HISTORY";
    public static final String SETTINGS = "SETTINGS";

    private MainPanel mainPanel;

    private boolean lightTheme =
            SettingsPanel.loadThemePreference();

    /** Panel y botón del menú de cada sección, según su comando de acción. */
    private final Map<String, MyPanel> sectionPanels =
            new LinkedHashMap<>();

    private final Map<String, MyButton> sectionButtons =
            new LinkedHashMap<>();

    public ViewFrame() {

        super("Aplicacion Musica");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        getContentPane().setBackground(
                Palette.WINDOW_BACKGROUND);

        getContentPane().setLayout(
                new BorderLayout());

        // La ventana puede recibir el foco cuando ningún campo de texto lo tiene
        getRootPane().setFocusable(true);

        mainPanel = new MainPanel();

        getContentPane().add(
                mainPanel, BorderLayout.CENTER);

        // Paneles provisionales (SectionPanel)
        // hasta que se diseñe cada sección

        registerSection(
                HOME,
                new HomePanel(),
                mainPanel.getHomeButton());

        // Explorar concentra la búsqueda,
        // los filtros y los resultados de Jamendo.
        registerSection(
                EXPLORE,
                new ExplorePanel(),
                mainPanel.getExploreButton());

        registerSection(
                LIBRARY,
                new SectionPanel("Biblioteca"),
                mainPanel.getLibraryButton());

        registerSection(
                FAVORITES,
                new FavoritesPanel(),
                mainPanel.getFavoritesButton());

        registerSection(
                PLAYLISTS,
                new PlaylistPanel(),
                mainPanel.getPlaylistsButton());

        registerSection(
                HISTORY,
                new HistoryPanel(),
                mainPanel.getHistoryButton());

        // Panel de configuración funcional
        registerSection(
                SETTINGS,
                new SettingsPanel(
                        lightTheme,
                        this::applyTheme),
                mainPanel.getSettingsButton());

        showSection(HOME);

        // Aplica el tema guardado al iniciar
        applyTheme(lightTheme);
    }

    private void registerSection(
            String command,
            MyPanel panel,
            MyButton button) {

        button.setActionCommand(command);

        sectionPanels.put(command, panel);
        sectionButtons.put(command, button);
    }

    /**
     * Pone al listener (el Presenter) a escuchar
     * los botones del menú.
     *
     * @param listener listener de los botones del menú
     */
    public void addMenuListener(ActionListener listener) {

        for (MyButton button : sectionButtons.values()) {

            button.addActionListener(listener);
        }
    }

    /**
     * Muestra el panel de una sección y resalta
     * su botón del menú.
     *
     * @param section comando de acción de la sección
     */
    public void showSection(String section) {

        MyPanel panel = sectionPanels.get(section);

        if (panel != null) {

            mainPanel.showContent(panel);

            mainPanel.selectButton(
                    sectionButtons.get(section));

            // Deja el foco en la ventana y no en el buscador de la sección,
            // para que la barra espaciadora pause la música en vez de escribir.
            getRootPane().requestFocusInWindow();
        }
    }

    /** Aplica el tema elegido a todas las pantallas. */
    public void applyTheme(boolean light) {

        lightTheme = light;

        // Lo guarda para las pantallas que repintan cosas después (ThemeManager.refresh)
        ThemeManager.setCurrentTheme(light);

        ThemeManager.apply(
                getContentPane(), light);

        // Las secciones que no están en pantalla no hacen parte del
        // contentPane, así que se les aplica el tema por separado.
        for (MyPanel panel : sectionPanels.values()) {
            ThemeManager.apply(panel, light);
        }

        getContentPane().revalidate();
        getContentPane().repaint();
    }

    public boolean isLightTheme() {
        return lightTheme;
    }

    public SettingsPanel getSettingsPanel() {

        return (SettingsPanel)
                sectionPanels.get(SETTINGS);
    }

    public ExplorePanel getExplorePanel() {

        return (ExplorePanel)
                sectionPanels.get(EXPLORE);
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public FavoritesPanel getFavoritesPanel() {

        return (FavoritesPanel)
                sectionPanels.get(FAVORITES);
    }

    public HistoryPanel getHistoryPanel() {

        return (HistoryPanel)
                sectionPanels.get(HISTORY);
    }

    public PlaylistPanel getPlaylistPanel() {

        return (PlaylistPanel)
                sectionPanels.get(PLAYLISTS);
    }

    /**
     * Vuelve a aplicar el tema actual. Se usa después de crear filas nuevas
     * en una lista, porque se crean con los colores del tema oscuro.
     */
    public void refreshTheme() {
        applyTheme(lightTheme);
    }
}