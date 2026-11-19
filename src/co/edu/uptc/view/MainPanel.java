package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Panel principal de la aplicación: menú a la izquierda, contenido en el centro
 * y controles de reproducción abajo. Solo vista: el Presenter decide qué
 * pasa al presionar un botón.
 */
public class MainPanel extends MyPanel {

    private static final int MENU_WIDTH = 220;
    private static final int MENU_BUTTON_HEIGHT = 44;
    private static final int MENU_FONT_SIZE = 14;
    private static final int FOOTER_HEIGHT = 90;

    private MyPanel menu;
    private MyPanel footer;
    private MyPanel content;

    private MyButton homeButton;
    private MyButton exploreButton;
    private MyButton libraryButton;
    private MyButton playlistsButton;
    private MyButton historyButton;
    private MyButton settingsButton;
    private MyButton favoritesButton;

    private MyButton playPauseButton;
    private MyButton nextButton;
    private MyButton previousButton;

    public MainPanel() {
        setBackgroundColor(Palette.BACKGROUND);
        applyLayout(new BorderLayout());
        setPreferredSize(null);

        createMenu();
        createContent();
        createFooter();

        add(menu, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        selectButton(homeButton);
    }

    // ---------------------------------------------------------------
    // Menú lateral
    // ---------------------------------------------------------------
    private void createMenu() {
        menu = new MyPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackgroundColor(Palette.MENU_BACKGROUND);
        menu.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        menu.setPreferredSize(new Dimension(MENU_WIDTH, 0));

        JLabel appTitle = new JLabel("Ritmo");
        appTitle.setForeground(Palette.ACCENT);
        appTitle.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 23));
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        menu.add(appTitle);
        menu.add(Box.createVerticalStrut(25));

        // Todos empiezan sin seleccionar; selectButton resalta Home
        homeButton = createMenuButton("Home");
        exploreButton = createMenuButton("Explorar");
        libraryButton = createMenuButton("Biblioteca");
        favoritesButton = createMenuButton("Favoritos");
        playlistsButton = createMenuButton("Playlists");
        historyButton = createMenuButton("Historial");
        settingsButton = createMenuButton("Configuración");
    }

    /** Crea un botón con el estilo del menú y lo agrega al menú. */
    private MyButton createMenuButton(String text) {
        MyButton button = new MyButton(text, Palette.TRANSPARENT, Palette.TEXT_MUTED, null);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 14, 0, 0));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, MENU_BUTTON_HEIGHT));
        button.setCornerRadius(10);
        button.setFontStyle(Font.PLAIN, MENU_FONT_SIZE);

        menu.add(button);
        menu.add(Box.createVerticalStrut(5));
        return button;
    }

    // ---------------------------------------------------------------
    // Contenido central (aquí se van cambiando los paneles de cada sección)
    // ---------------------------------------------------------------
    private void createContent() {
        content = new MyPanel();
        content.setLayout(new BorderLayout());
        content.setBackgroundColor(Palette.BACKGROUND);
    }

    // ---------------------------------------------------------------
    // Footer con los controles de reproducción (todavía no hacen nada)
    // ---------------------------------------------------------------
    private void createFooter() {
        footer = new MyPanel();
        footer.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        footer.setBackgroundColor(Palette.CARD);
        footer.setPreferredSize(new Dimension(0, FOOTER_HEIGHT));

        previousButton = createControlButton("⏮", Palette.TEXT);
        playPauseButton = createControlButton("▶", Palette.ACCENT);
        nextButton = createControlButton("⏭", Palette.TEXT);

        footer.add(previousButton);
        footer.add(playPauseButton);
        footer.add(nextButton);
    }

    private MyButton createControlButton(String symbol, Color color) {
        MyButton button = new MyButton(symbol, Palette.TRANSPARENT, color, null);
        button.setPreferredSize(new Dimension(50, 44));
        button.setFontStyle(Font.BOLD, 18);
        button.setCornerRadius(10);
        return button;
    }

    // ---------------------------------------------------------------
    // Métodos que usa la ventana
    // ---------------------------------------------------------------

    /**
     * Cambia el panel que se ve en el centro.
     *
     * @param panel panel que se desea mostrar
     */
    public void showContent(MyPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    /**
     * Resalta el botón elegido del menú y deja los demás normales.
     *
     * @param selected botón que se desea resaltar
     */
    public void selectButton(MyButton selected) {
        MyButton[] buttons = { homeButton, exploreButton, libraryButton, favoritesButton, playlistsButton,
                historyButton, settingsButton };
        for (MyButton button : buttons) {
            paintMenuButton(button, button == selected);
        }

        // Obliga a redibujar el menú para que se vean los colores nuevos
        menu.revalidate();
        menu.repaint();
    }

    private void paintMenuButton(MyButton button, boolean selected) {
        if (selected) {
            button.setColors(Palette.ACCENT, Palette.TEXT_DARK, null);
            button.setFontStyle(Font.BOLD, MENU_FONT_SIZE);
        } else {
            button.setColors(Palette.TRANSPARENT, Palette.TEXT_MUTED, null);
            button.setFontStyle(Font.PLAIN, MENU_FONT_SIZE);
        }
    }

    public MyButton getHomeButton() {
        return homeButton;
    }

    public MyButton getExploreButton() {
        return exploreButton;
    }

    public MyButton getLibraryButton() {
        return libraryButton;
    }

    public MyButton getFavoritesButton() {
        return favoritesButton;
    }

    public MyButton getPlaylistsButton() {
        return playlistsButton;
    }

    public MyButton getHistoryButton() {
        return historyButton;
    }

    public MyButton getSettingsButton() {
        return settingsButton;
    }

    public MyButton getPlayPauseButton() {
        return playPauseButton;
    }

    public MyButton getNextButton() {
        return nextButton;
    }

    public MyButton getPreviousButton() {
        return previousButton;
    }
}