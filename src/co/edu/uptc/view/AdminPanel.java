package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Pantalla completa del administrador: menú lateral (Dashboard, Canciones,
 * Usuarios, Carpetas y Géneros), buscador arriba y la sección elegida en el
 * centro.
 * @author programacion 2
 */
public class AdminPanel extends MyPanel {

    public static final String DASHBOARD = "ADMIN_DASHBOARD";
    public static final String SONGS = "ADMIN_SONGS";
    public static final String USERS = "ADMIN_USERS";
    public static final String FOLDERS = "ADMIN_FOLDERS";
    public static final String GENRES = "ADMIN_GENRES";

    private static final int MENU_WIDTH = 190;
    private static final int MENU_BUTTON_HEIGHT = 40;

    private final MyPanel menu = new MyPanel();
    private final CardLayout cards = new CardLayout();
    private final MyPanel sections = new MyPanel();
    private final MyButton[] menuButtons = new MyButton[5];
    private final MyTextField searchField = new MyTextField("Buscar en el panel...", false);
    private final JLabel avatar = AdminComponents.label("A", Palette.TEXT_DARK, Font.BOLD, 16);
    private final JLabel sessionLabel = AdminComponents.label(" ", Palette.TEXT_MUTED, Font.PLAIN, 11);
    private final MyButton logoutButton = new MyButton("Cerrar sesión", Palette.TRANSPARENT, Palette.TEXT,
            Palette.CARD_BORDER);

    private final AdminDashboardPanel dashboardPanel = new AdminDashboardPanel();
    private final AdminSongsPanel songsPanel = new AdminSongsPanel();
    private final AdminUsersPanel usersPanel = new AdminUsersPanel();
    private final AdminFoldersPanel foldersPanel = new AdminFoldersPanel();
    private final AdminGenresPanel genresPanel = new AdminGenresPanel();

    private String currentSection = DASHBOARD;

    public AdminPanel() {
        applyLayout(new BorderLayout());
        setBackgroundColor(Palette.WINDOW_BACKGROUND);
        setPreferredSize(null);
        createMenu();
        add(menu, BorderLayout.WEST);

        sections.applyLayout(cards);
        sections.setBackgroundColor(Palette.WINDOW_BACKGROUND);
        sections.setPreferredSize(null);
        sections.add(dashboardPanel, DASHBOARD);
        sections.add(songsPanel, SONGS);
        sections.add(usersPanel, USERS);
        sections.add(foldersPanel, FOLDERS);
        sections.add(genresPanel, GENRES);

        MyPanel center = AdminComponents.transparentPanel();
        center.applyLayout(new BorderLayout(0, 16));
        center.setBorder(BorderFactory.createEmptyBorder(14, 20, 20, 20));
        center.add(createTopBar(), BorderLayout.NORTH);
        center.add(sections, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        showSection(DASHBOARD);
    }

    private void createMenu() {
        menu.applyLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackgroundColor(Palette.MENU_BACKGROUND);
        menu.setBorderColor(Palette.CARD_BORDER);
        menu.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
        menu.setPreferredSize(new Dimension(MENU_WIDTH, 0));

        MyPanel title = new MyPanel();
        title.applyLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        title.setBackgroundColor(Palette.MENU_BACKGROUND);
        title.setPreferredSize(null);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.add(AdminComponents.label("Ritmo", Palette.ACCENT, Font.BOLD, 20));
        MyPanel badge = new MyPanel();
        badge.applyLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        badge.setBackgroundColor(Palette.ACCENT);
        badge.setCornerRadius(6);
        badge.setPreferredSize(null);
        badge.add(AdminComponents.label("ADMIN", Palette.TEXT_DARK, Font.BOLD, 9));
        title.add(badge);
        menu.add(title);
        menu.add(Box.createVerticalStrut(25));

        String[] texts = { "Dashboard", "Canciones", "Usuarios", "Carpetas", "Géneros" };
        String[] commands = { DASHBOARD, SONGS, USERS, FOLDERS, GENRES };
        for (int i = 0; i < texts.length; i++) {
            menuButtons[i] = createMenuButton(texts[i], commands[i]);
        }

        menu.add(Box.createVerticalGlue());
        sessionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(sessionLabel);
        menu.add(Box.createVerticalStrut(8));
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        logoutButton.setPreferredSize(new Dimension(MENU_WIDTH, 34));
        logoutButton.setCornerRadius(10);
        logoutButton.setFontStyle(Font.PLAIN, 12);
        menu.add(logoutButton);
    }

    private MyButton createMenuButton(String text, String command) {
        MyButton button = new MyButton(text, Palette.TRANSPARENT, Palette.TEXT_MUTED, null);
        button.setActionCommand(command);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 12, 0, 0));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, MENU_BUTTON_HEIGHT));
        button.setPreferredSize(new Dimension(MENU_WIDTH, MENU_BUTTON_HEIGHT));
        button.setCornerRadius(10);
        button.setFontStyle(Font.PLAIN, 13);
        menu.add(button);
        menu.add(Box.createVerticalStrut(5));
        return button;
    }

    /** Barra de arriba: buscador y círculo con la inicial del administrador. */
    private MyPanel createTopBar() {
        MyPanel bar = AdminComponents.transparentPanel();
        bar.applyLayout(new BorderLayout(14, 0));
        searchField.setPreferredSize(new Dimension(300, 38));
        searchField.setCornerRadius(19);
        bar.add(searchField, BorderLayout.CENTER);

        MyPanel circle = new MyPanel();
        circle.applyLayout(new java.awt.GridBagLayout());
        circle.setBackgroundColor(Palette.ACCENT);
        circle.setCornerRadius(38);
        circle.setPreferredSize(38, 38);
        circle.add(avatar);
        bar.add(circle, BorderLayout.EAST);
        return bar;
    }

    /** Cambia la sección del centro y resalta su botón del menú. */
    public void showSection(String section) {
        currentSection = section;
        cards.show(sections, section);
        for (MyButton button : menuButtons) {
            boolean selected = button.getActionCommand().equals(section);
            button.setColors(selected ? Palette.ACCENT : Palette.TRANSPARENT,
                    selected ? Palette.TEXT_DARK : Palette.TEXT_MUTED, null);
            button.setFontStyle(selected ? Font.BOLD : Font.PLAIN, 13);
        }
        menu.repaint();
    }

    public String getCurrentSection() {
        return currentSection;
    }

    public void setAdminName(String name) {
        avatar.setText(name == null || name.isEmpty() ? "A" : name.substring(0, 1).toUpperCase());
        sessionLabel.setText("Sesión: " + name + " (admin)");
    }

    public String getSearchText() {
        return searchField.getValue();
    }

    public void clearSearch() {
        searchField.setText("");
    }

    public void addMenuListener(ActionListener listener) {
        for (MyButton button : menuButtons) {
            button.addActionListener(listener);
        }
    }

    /** Avisa cada vez que cambia el texto del buscador. */
    public void addSearchListener(Runnable listener) {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                listener.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                listener.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listener.run();
            }
        });
    }

    public MyButton getLogoutButton() {
        return logoutButton;
    }

    public AdminDashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public AdminSongsPanel getSongsPanel() {
        return songsPanel;
    }

    public AdminUsersPanel getUsersPanel() {
        return usersPanel;
    }

    public AdminFoldersPanel getFoldersPanel() {
        return foldersPanel;
    }

    public AdminGenresPanel getGenresPanel() {
        return genresPanel;
    }
}