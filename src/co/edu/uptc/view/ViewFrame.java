package co.edu.uptc.view;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Ventana de la aplicación. Implementa IFrame, que es lo único que conoce el
 * Presenter. Aquí viven el panel principal y el panel de cada sección. Los
 * comandos del menú (HOME, EXPLORE...) están definidos en IFrame.
 * @author programacion 2
 */
public class ViewFrame extends JFrame implements IFrame {

    // Nombres de las dos pantallas de la ventana
    private static final String LOGIN_SCREEN = "LOGIN";
    private static final String APP_SCREEN = "APP";
    private static final String ADMIN_SCREEN = "ADMIN";

    //logo 
    private static final String ICON_PATH = "resources" + File.separator + "loguito.png";
    
    
    private MainPanel mainPanel;
    private RegisterPanel registerPanel;
    private HomePanel homePanel;
    private AdminPanel adminPanel;
    /** Muestra una pantalla a la vez: inicio de sesión o la aplicación. */
    private final CardLayout screens = new CardLayout();

    private boolean lightTheme =
            SettingsPanel.loadThemePreference();

    /** Panel y botón del menú de cada sección, según su comando de acción. */
    private final Map<String, MyPanel> sectionPanels =
            new LinkedHashMap<>();

    private final Map<String, MyButton> sectionButtons =
            new LinkedHashMap<>();

    public ViewFrame() {

        super("Ritmo");
        
        loadAppIcon();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        getContentPane().setBackground(
                Palette.WINDOW_BACKGROUND);

        getContentPane().setLayout(screens);

        // La ventana puede recibir el foco cuando ningún campo de texto lo tiene
        getRootPane().setFocusable(true);

        mainPanel = new MainPanel();
        adminPanel = new AdminPanel();

        // Pantalla de inicio de sesión: la tarjeta centrada sobre el fondo
        registerPanel = new RegisterPanel();
        MyPanel loginScreen = new MyPanel();
        loginScreen.setBackgroundColor(Palette.BACKGROUND);
        loginScreen.applyLayout(new GridBagLayout());
        loginScreen.add(registerPanel);

        getContentPane().add(loginScreen, LOGIN_SCREEN);
        getContentPane().add(mainPanel, APP_SCREEN);
        getContentPane().add(adminPanel, ADMIN_SCREEN);


        homePanel = new HomePanel();
        registerSection(HOME, homePanel, mainPanel.getHomeButton());

        // Explorar concentra la búsqueda,
        // los filtros y los resultados de Jamendo.
        registerSection(
                EXPLORE,
                new ExplorePanel(),
                mainPanel.getExploreButton());

        registerSection(
                LIBRARY,
                new LibraryPanel(),
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

    
    
    /**
     * Carga el ícono de la ventana desde una ruta relativa dentro del proyecto.
     *
     * @author progra 2
     */
    private void loadAppIcon() {
        File iconFile = new File(ICON_PATH);
        if (!iconFile.exists()) {
            iconFile = new File("src" + File.separator + ICON_PATH);
        }
        if (iconFile.exists() && iconFile.isFile()) {
            Image icon = new ImageIcon(iconFile.getAbsolutePath()).getImage();
            setIconImage(icon);
        } else {
            System.err.println("No se encontró el logo. Buscado en: " + iconFile.getAbsolutePath());
        }
    }
    
    
    /**
     * Muestra la pantalla de inicio de sesión y pone el cursor en el usuario.
     */
    @Override
    public void showLogin() {
        screens.show(getContentPane(), LOGIN_SCREEN);
        SwingUtilities.invokeLater(() -> registerPanel.getUserField().requestFocusInWindow());
    }

    /**
     * Muestra la aplicación, empezando en Home.
     */
    @Override
    public void showApp() {
        screens.show(getContentPane(), APP_SCREEN);
        showSection(HOME);
    }
    
    /**
     * Muestra la pantalla del administrador, empezando en el Dashboard.
     */
    @Override
    public void showAdmin() {
        screens.show(getContentPane(), ADMIN_SCREEN);
        adminPanel.showSection(AdminPanel.DASHBOARD);
    }
    
    @Override
    public AdminPanel getAdminPanel() {
        return adminPanel;
    }

    @Override
    public Component getDialogParent() {
        return this;
    }

    @Override
    public RegisterPanel getRegisterPanel() {
        return registerPanel;
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
    @Override
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
    @Override
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

    @Override
    public SettingsPanel getSettingsPanel() {

        return (SettingsPanel)
                sectionPanels.get(SETTINGS);
    }

    @Override
    public ExplorePanel getExplorePanel() {

        return (ExplorePanel)
                sectionPanels.get(EXPLORE);
    }

    @Override
    public MainPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public HomePanel getHomePanel() {
        return homePanel;
    }

    @Override
    public FavoritesPanel getFavoritesPanel() {

        return (FavoritesPanel)
                sectionPanels.get(FAVORITES);
    }

    @Override
    public HistoryPanel getHistoryPanel() {

        return (HistoryPanel)
                sectionPanels.get(HISTORY);
    }

    @Override
    public LibraryPanel getLibraryPanel() {

        return (LibraryPanel)
                sectionPanels.get(LIBRARY);
    }

    @Override
    public PlaylistPanel getPlaylistPanel() {

        return (PlaylistPanel)
                sectionPanels.get(PLAYLISTS);
    }

    /**
     * Vuelve a aplicar el tema actual. Se usa después de crear filas nuevas
     * en una lista, porque se crean con los colores del tema oscuro.
     */
    @Override
    public void refreshTheme() {
        applyTheme(lightTheme);
    }

}