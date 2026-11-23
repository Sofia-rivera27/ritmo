package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel funcional de configuración: tema, colores por género y atajos.
 * La vista comunica los cambios al Presenter mediante callbacks.
 * @author programacion 2
 */
public class SettingsPanel extends MyPanel {

    private static final String[] GENRES = {
        "Pop", "Rock", "Electrónica", "Folk"
    };

    private final Preferences preferences =
            Preferences.userNodeForPackage(SettingsPanel.class);

    private final Map<String, MyButton> colorButtons =
            new LinkedHashMap<>();

    private final Consumer<Boolean> themeListener;
    private boolean lightTheme;

    // Tarjeta "Mi cuenta": se llena con setAccount al iniciar sesión
    private JLabel accountNameLabel;
    private JLabel accountRoleLabel;
    private MyButton logoutButton;

    public static boolean loadThemePreference() {
        return Preferences.userNodeForPackage(SettingsPanel.class)
                .getBoolean("settings.lightTheme", false);
    }
    
    /**
     * Construye el panel de configuración e inicializa sus componentes.
     *
     * @param lightTheme indica si se debe utilizar el tema claro.
     * @param themeListener callback que comunica los cambios de tema
     *                      al Presenter.
     */

    public SettingsPanel(boolean lightTheme,
            Consumer<Boolean> themeListener) {

        this.lightTheme = lightTheme;
        this.themeListener = themeListener;

        setBackgroundColor(Palette.BACKGROUND);
        setLayout(new BorderLayout(18, 18));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Configuración");
        title.setForeground(Palette.TEXT);
        title.setFont(new Font(
                Palette.FONT_FAMILY, Font.BOLD, 22));

        add(title, BorderLayout.NORTH);

        JPanel columns = new JPanel(
                new GridLayout(1, 2, 18, 0));

        columns.setOpaque(false);
        columns.add(createLeftColumn());
        columns.add(createRightColumn());

        add(columns, BorderLayout.CENTER);
    }
    /**
     * Construye la columna izquierda del panel de configuración.
     *
     * Contiene las opciones de apariencia y la personalización
     * de colores para los diferentes géneros musicales.
     *
     * @return JPanel con las opciones de apariencia y géneros.
     */

    private JPanel createLeftColumn() {

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(
                new BoxLayout(column, BoxLayout.Y_AXIS));

        MyPanel appearance = createCard("Apariencia");

        appearance.setLayout(
                new BoxLayout(appearance, BoxLayout.Y_AXIS));

        appearance.add(themeOption("Tema claro", true));
        appearance.add(Box.createVerticalStrut(8));
        appearance.add(themeOption("Tema oscuro", false));

        MyPanel genres =
                createCard("Personalización por género");

        genres.setLayout(
                new BoxLayout(genres, BoxLayout.Y_AXIS));

        for (String genre : GENRES) {

            JPanel row = new JPanel(
                    new BorderLayout(10, 0));

            row.setOpaque(false);

            JLabel label = new JLabel(genre);
            label.setForeground(Palette.TEXT);

            MyButton change = new MyButton(
                    "Cambiar",
                    Palette.CARD,
                    Palette.TEXT,
                    Palette.CARD_BORDER);

            change.setFixedSize(92, 30);

            change.addActionListener(e ->
                    chooseGenreColor(genre, change));

            colorButtons.put(genre, change);

            row.add(label, BorderLayout.CENTER);
            row.add(change, BorderLayout.EAST);

            genres.add(row);
            genres.add(Box.createVerticalStrut(7));

            updateGenreButton(genre, change);
        }

        column.add(appearance);
        column.add(Box.createVerticalStrut(16));
        column.add(genres);

        return column;
    }
    
    /**
     * Construye la columna derecha del panel de configuración.
     *
     * Incluye los atajos de teclado disponibles y la sección
     * de información de la cuenta del usuario.
     *
     * @return JPanel con los atajos de teclado y la información
     *         de la cuenta.
     */

    private JPanel createRightColumn() {

        JPanel column = new JPanel();

        column.setOpaque(false);
        column.setLayout(
                new BoxLayout(column, BoxLayout.Y_AXIS));

        MyPanel shortcuts =
                createCard("Atajos de teclado");

        shortcuts.setLayout(
                new BoxLayout(shortcuts, BoxLayout.Y_AXIS));

        shortcuts.add(shortcutRow(
                "Pausar / reanudar", "Espacio"));

        shortcuts.add(Box.createVerticalStrut(10));

        shortcuts.add(shortcutRow(
                "Adelantar", "→"));

        shortcuts.add(Box.createVerticalStrut(10));

        shortcuts.add(shortcutRow(
                "Retroceder", "←"));

        MyPanel account = createCard("Mi cuenta");

        account.setLayout(
                new BoxLayout(account, BoxLayout.Y_AXIS));

        // Nombre y tipo de cuenta; el Presenter los cambia con setAccount
        accountNameLabel = new JLabel(" ");
        accountNameLabel.setForeground(Palette.TEXT);
        accountNameLabel.setFont(new Font(
                Palette.FONT_FAMILY, Font.BOLD, 14));

        accountRoleLabel = new JLabel(" ");
        accountRoleLabel.setForeground(Palette.TEXT_MUTED);
        accountRoleLabel.setFont(new Font(
                Palette.FONT_FAMILY, Font.PLAIN, 11));

        account.add(accountNameLabel);
        account.add(Box.createVerticalStrut(4));
        account.add(accountRoleLabel);
        account.add(Box.createVerticalStrut(12));

        // El Presenter decide qué pasa al presionarlo (getLogoutButton)
        logoutButton = new MyButton(
                "Cerrar sesión",
                Palette.CARD,
                Palette.TEXT,
                Palette.CARD_BORDER);

        logoutButton.setFixedSize(125, 32);

        JPanel buttonRow = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0));

        buttonRow.setOpaque(false);
        buttonRow.add(logoutButton);

        account.add(buttonRow);

        column.add(shortcuts);
        column.add(Box.createVerticalStrut(16));
        column.add(account);

        return column;
    }

    /**
     * Muestra en la tarjeta "Mi cuenta" quién inició sesión.
     *
     * @param userName nombre del usuario, o null si nadie ha iniciado sesión
     * @param admin    true si entró como administrador
     */
    public void setAccount(String userName, boolean admin) {
        if (userName == null) {
            accountNameLabel.setText(" ");
            accountRoleLabel.setText(" ");
            return;
        }
        accountNameLabel.setText(userName);
        accountRoleLabel.setText(admin ? "Cuenta de administrador"
                : "Cuenta de usuario · tus datos se guardan en este computador");
    }

    /**
     * Botón "Cerrar sesión" de la tarjeta "Mi cuenta".
     *
     * @return el botón, para que el Presenter le ponga su acción
     */
    public MyButton getLogoutButton() {
        return logoutButton;
    }

    private MyPanel createCard(String heading) {

        MyPanel card = new MyPanel();

        card.setBackgroundColor(Palette.CARD);
        card.setBorderColor(Palette.CARD_BORDER);
        card.setCornerRadius(12);

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        16, 16, 16, 16));

        JLabel label = new JLabel(heading);

        label.setForeground(Palette.TEXT);
        label.setFont(new Font(
                Palette.FONT_FAMILY, Font.BOLD, 15));

        card.add(label);
        card.add(Box.createVerticalStrut(12));

        return card;
    }
    
    /**
     * Crea un botón para seleccionar un tema de apariencia.
     *
     * Cuando el usuario selecciona una opción, se guarda la preferencia
     * y se notifica el cambio mediante el callback correspondiente.
     *
     * @param text texto que aparecerá en el botón.
     * @param light indica si la opción corresponde al tema claro.
     * @return JPanel que contiene el botón de selección del tema.
     */

    private JPanel themeOption(String text, boolean light) {

        JPanel row = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 2));

        row.setOpaque(false);

        MyButton option = new MyButton(
                text,
                Palette.FIELD,
                Palette.TEXT,
                Palette.CARD_BORDER);

        option.setFixedSize(150, 34);

        option.addActionListener(e -> {

            this.lightTheme = light;

            preferences.putBoolean(
                    "settings.lightTheme", light);

            themeListener.accept(light);
        });

        row.add(option);

        return row;
    }
    
    /**
     * Construye una fila que muestra una acción de la aplicación
     * junto con su respectivo atajo de teclado.
     *
     * @param action descripción de la acción que se realiza.
     * @param key tecla asociada a la acción.
     * @return JPanel con la descripción y la tecla correspondiente.
     */

    private JPanel shortcutRow(String action, String key) {

        JPanel row = new JPanel(
                new BorderLayout(8, 0));

        row.setOpaque(false);

        JLabel label = new JLabel(action);
        label.setForeground(Palette.TEXT);

        MyButton keyButton = new MyButton(
                key,
                Palette.FIELD,
                Palette.TEXT,
                Palette.CARD_BORDER);

        keyButton.setFixedSize(76, 28);

        keyButton.setCursor(
                java.awt.Cursor.getDefaultCursor());

        row.add(label, BorderLayout.CENTER);
        row.add(keyButton, BorderLayout.EAST);

        return row;
    }
    
    /**
     * Abre un selector de colores para permitir al usuario
     * personalizar el color de un género musical.
     *
     * Si se selecciona un color, este se almacena en las preferencias
     * y se actualiza la apariencia del botón correspondiente.
     *
     * @param genre nombre del género musical que se va a personalizar.
     * @param button botón que representa la opción de cambio de color.
     */

    private void chooseGenreColor(
            String genre, MyButton button) {

        Color initial = getGenreColor(genre);

        Color selected = JColorChooser.showDialog(
                this,
                "Color para " + genre,
                initial);

        if (selected != null) {

            preferences.putInt(
                    "genre." + genre,
                    selected.getRGB());

            updateGenreButton(genre, button);
        }
    }

    private Color getGenreColor(String genre) {

        int defaultColor;

        switch (genre) {

            case "Pop":
                defaultColor =
                        new Color(255, 193, 7).getRGB();
                break;

            case "Rock":
                defaultColor =
                        new Color(220, 70, 70).getRGB();
                break;

            case "Electrónica":
                defaultColor =
                        new Color(100, 120, 255).getRGB();
                break;

            default:
                defaultColor =
                        new Color(60, 180, 130).getRGB();
                break;
        }

        return new Color(
                preferences.getInt(
                        "genre." + genre, defaultColor),
                true);
    }

    private void updateGenreButton(
            String genre, MyButton button) {

        Color color = getGenreColor(genre);

        button.setColors(
                color,
                Color.BLACK,
                color.darker());

        button.setText("Cambiar");
    }
}