package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

/**
 * Panel principal de la aplicación: menú a la izquierda, contenido en el centro
 * y controles de reproducción abajo. Solo vista: el Presenter decide qué
 * pasa al presionar un botón.
 * @author programacion 2
 */
public class MainPanel extends MyPanel {

    private static final int MENU_WIDTH = 220;
    private static final int MENU_BUTTON_HEIGHT = 44;
    private static final int MENU_FONT_SIZE = 14;
    private static final int FOOTER_HEIGHT = 96;
    private static final int NOW_PLAYING_WIDTH = 240;
    private static final int PROGRESS_WIDTH = 460;
    private static final int TIME_LABEL_WIDTH = 42;
    private static final String NOTHING_PLAYING = "Nada sonando";
    private static final String NOTHING_PLAYING_HINT = "Elige una canción para empezar";

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
    private MyButton queueButton;

    // "Sonando ahora", progreso y volumen
    private JLabel nowPlayingTitle;
    private JLabel nowPlayingArtist;
    private JLabel elapsedLabel;
    private JLabel totalLabel;
    private MySlider progressSlider;
    private MySlider volumeSlider;
    private JLabel volumeLabel;
    /** true mientras el usuario arrastra la barra de progreso. */
    private boolean seeking;

    private JLabel sessionLabel;
    private MyButton logoutButton;

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

        // Abajo del menú: quién inició sesión y el botón para salir
        menu.add(Box.createVerticalGlue());
        sessionLabel = new JLabel(" ");
        sessionLabel.setForeground(Palette.TEXT_MUTED);
        sessionLabel.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));
        sessionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(sessionLabel);
        menu.add(Box.createVerticalStrut(8));

        logoutButton = new MyButton("Cerrar sesión", Palette.TRANSPARENT, Palette.TEXT, Palette.CARD_BORDER);
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        logoutButton.setPreferredSize(new Dimension(MENU_WIDTH, 34));
        logoutButton.setMinimumSize(new Dimension(0, 34));
        logoutButton.setCornerRadius(10);
        logoutButton.setFontStyle(Font.PLAIN, 12);
        menu.add(logoutButton);
    }

    /**
     * Muestra en el menú quién inició sesión.
     *
     * @param userName nombre del usuario
     * @param admin    true si entró como administrador
     */
    public void setSessionUser(String userName, boolean admin) {
        sessionLabel.setText("Sesión: " + userName + (admin ? " (admin)" : ""));
    }

    public MyButton getLogoutButton() {
        return logoutButton;
    }

    /** Crea un botón con el estilo del menú y lo agrega al menú. */
    private MyButton createMenuButton(String text) {
        MyButton button = new MyButton(text, Palette.TRANSPARENT, Palette.TEXT_MUTED, null);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 14, 0, 0));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, MENU_BUTTON_HEIGHT));
        // Altura fija: así el espacio libre del menú no los aplasta
        button.setPreferredSize(new Dimension(MENU_WIDTH, MENU_BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(0, MENU_BUTTON_HEIGHT));
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
    // Controles de reproducción y acceso a la cola.
    // ---------------------------------------------------------------
    private void createFooter() {
        footer = new MyPanel();
        footer.setLayout(new BorderLayout(16, 0));
        footer.setBackgroundColor(Palette.CARD);
        footer.setPreferredSize(new Dimension(0, FOOTER_HEIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 16));

        footer.add(createNowPlaying(), BorderLayout.WEST);
        footer.add(createPlayerControls(), BorderLayout.CENTER);
        footer.add(createVolumeControls(), BorderLayout.EAST);
    }

    /** Izquierda: título y artista de la canción que suena. */
    private MyPanel createNowPlaying() {
        nowPlayingTitle = createFooterLabel(NOTHING_PLAYING, Palette.TEXT, Font.BOLD, 13);
        nowPlayingArtist = createFooterLabel(NOTHING_PLAYING_HINT, Palette.TEXT_MUTED, Font.PLAIN, 11);
        // Ancho fijo: si el título es muy largo, Swing lo corta con "..."
        nowPlayingTitle.setPreferredSize(new Dimension(NOW_PLAYING_WIDTH, 18));
        nowPlayingArtist.setPreferredSize(new Dimension(NOW_PLAYING_WIDTH, 16));

        MyPanel texts = createFooterPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(nowPlayingTitle);
        texts.add(Box.createVerticalStrut(3));
        texts.add(nowPlayingArtist);
        return centerVertically(texts);
    }

    /** Centro: botones de reproducción y, debajo, la barra de progreso. */
    private MyPanel createPlayerControls() {
        previousButton = createControlButton("⏮", Palette.TEXT);
        playPauseButton = createControlButton("▶", Palette.ACCENT);
        nextButton = createControlButton("⏭", Palette.TEXT);

        MyPanel buttons = createFooterPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttons.add(previousButton);
        buttons.add(playPauseButton);
        buttons.add(nextButton);

        elapsedLabel = createFooterLabel("0:00", Palette.TEXT_MUTED, Font.PLAIN, 11);
        elapsedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        elapsedLabel.setPreferredSize(new Dimension(TIME_LABEL_WIDTH, 16));
        totalLabel = createFooterLabel("0:00", Palette.TEXT_MUTED, Font.PLAIN, 11);
        totalLabel.setPreferredSize(new Dimension(TIME_LABEL_WIDTH, 16));

        progressSlider = new MySlider(0, 1, 0);
        progressSlider.setEnabled(false);
        progressSlider.setToolTipText("Arrastra para adelantar o retroceder");
        // Mientras se arrastra, el tiempo de la izquierda muestra a dónde se va a
        // saltar
        progressSlider.addChangeListener(this::previewSeek);

        MyPanel progress = createFooterPanel();
        progress.setLayout(new BorderLayout(10, 0));
        progress.setPreferredSize(new Dimension(PROGRESS_WIDTH, 18));
        progress.add(elapsedLabel, BorderLayout.WEST);
        progress.add(progressSlider, BorderLayout.CENTER);
        progress.add(totalLabel, BorderLayout.EAST);

        MyPanel column = createFooterPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress.setMaximumSize(new Dimension(PROGRESS_WIDTH, 18));
        column.add(buttons);
        column.add(Box.createVerticalStrut(6));
        column.add(progress);
        return centerVertically(column);
    }

    /** Derecha: volumen y botón de la cola. */
    private MyPanel createVolumeControls() {
        JLabel volumeTitle = createFooterLabel("Vol.", Palette.TEXT_MUTED, Font.PLAIN, 11);

        volumeSlider = new MySlider(0, 100, 50);
        volumeSlider.setPreferredSize(new Dimension(110, 18));
        volumeSlider.setToolTipText("Volumen");

        volumeLabel = createFooterLabel("50%", Palette.TEXT_MUTED, Font.PLAIN, 11);
        volumeLabel.setPreferredSize(new Dimension(34, 16));
        volumeSlider.addChangeListener(event -> volumeLabel.setText(volumeSlider.getValue() + "%"));

        // Mantiene el acceso a la cola en el reproductor, como en el diseño.
        queueButton = new MyButton("Cola", Palette.CARD, Palette.TEXT, Palette.CARD_BORDER);
        queueButton.setFixedSize(72, 34);
        queueButton.setFontStyle(Font.PLAIN, 11);
        queueButton.setToolTipText("Mostrar cola de reproducción");

        MyPanel row = createFooterPanel();
        row.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.add(volumeTitle);
        row.add(volumeSlider);
        row.add(volumeLabel);
        row.add(Box.createHorizontalStrut(8));
        row.add(queueButton);
        return centerVertically(row);
    }

    private MyPanel createFooterPanel() {
        MyPanel panel = new MyPanel();
        panel.setBackgroundColor(Palette.CARD);
        panel.setPreferredSize(null);
        return panel;
    }

    private JLabel createFooterLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    /** Envuelve un componente para centrarlo verticalmente en la barra. */
    private MyPanel centerVertically(MyPanel inner) {
        MyPanel wrapper = createFooterPanel();
        wrapper.setLayout(new GridBagLayout());
        wrapper.add(inner);
        return wrapper;
    }

    private void previewSeek(ChangeEvent event) {
        if (progressSlider.getValueIsAdjusting()) {
            seeking = true;
            elapsedLabel.setText(formatTime(progressSlider.getValue()));
        }
    }

    /** Convierte segundos al formato m:ss, por ejemplo 125 → "2:05". */
    private String formatTime(int seconds) {
        int safe = Math.max(0, seconds);
        return String.format("%d:%02d", safe / 60, safe % 60);
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

    // ---------------------------------------------------------------
    // Sonando ahora, progreso y volumen (los usa el Presenter)
    // ---------------------------------------------------------------

    /**
     * Muestra la canción que está sonando. Si el título es null, muestra
     * "Nada sonando" y desactiva la barra de progreso.
     *
     * @param title  título de la canción, o null si no suena nada
     * @param artist artista de la canción
     */
    public void showNowPlaying(String title, String artist) {
        boolean playing = title != null;
        nowPlayingTitle.setText(playing ? title : NOTHING_PLAYING);
        nowPlayingArtist.setText(playing ? artist : NOTHING_PLAYING_HINT);
        // Muestra el nombre completo al pasar el mouse, por si no cabe
        nowPlayingTitle.setToolTipText(playing ? title + " - " + artist : null);
        progressSlider.setEnabled(playing);
        if (!playing) {
            showProgress(0, 0);
        }
    }

    /**
     * Actualiza la barra de progreso y los tiempos. Si el usuario está
     * arrastrando la barra, no la mueve para no pelear con él.
     *
     * @param elapsed segundos que ya sonaron
     * @param total   duración total en segundos (0 si todavía no se sabe)
     */
    public void showProgress(int elapsed, int total) {
        if (seeking || progressSlider.getValueIsAdjusting()) {
            return;
        }
        int max = Math.max(1, total);
        progressSlider.setMaximum(max);
        progressSlider.setValue(Math.min(Math.max(0, elapsed), max));
        elapsedLabel.setText(formatTime(elapsed));
        totalLabel.setText(formatTime(total));
    }

    /**
     * Registra lo que pasa cuando el usuario suelta la barra de progreso.
     *
     * @param listener recibe el segundo al que se quiere saltar
     */
    public void addSeekListener(IntConsumer listener) {
        progressSlider.addChangeListener(event -> {
            if (seeking && !progressSlider.getValueIsAdjusting()) {
                seeking = false;
                listener.accept(progressSlider.getValue());
            }
        });
    }

    /**
     * Registra lo que pasa cuando el usuario mueve el volumen.
     *
     * @param listener recibe el volumen nuevo, de 0 a 100
     */
    public void addVolumeListener(IntConsumer listener) {
        volumeSlider.addChangeListener(event -> listener.accept(volumeSlider.getValue()));
    }

    /**
     * Pone el volumen en la barra, por ejemplo al abrir la aplicación con el
     * volumen guardado. Llámalo antes de addVolumeListener.
     *
     * @param volume volumen de 0 a 100
     */
    public void setVolume(int volume) {
        volumeSlider.setValue(Math.max(0, Math.min(100, volume)));
        volumeLabel.setText(volumeSlider.getValue() + "%");
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

    public MyButton getQueueButton() {
        return queueButton;
    }
}