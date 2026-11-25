package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 * Dashboard del administrador: tarjetas con los totales, canciones por género
 * y la actividad reciente.
 * @author programacion 2
 */
public class AdminDashboardPanel extends AdminSectionPanel {

    private final JLabel songsValue = bigNumber();
    private final JLabel usersValue = bigNumber();
    private final JLabel playlistsValue = bigNumber();
    private final JLabel playsValue = bigNumber();
    private final MyPanel genreList = createList();
    private final MyPanel activityList = createList();

    public AdminDashboardPanel() {
        add(createHeader("Dashboard"), BorderLayout.NORTH);

        MyPanel stats = AdminComponents.transparentPanel();
        stats.applyLayout(new GridLayout(1, 4, 14, 0));
        stats.add(statCard("Total de canciones", songsValue));
        stats.add(statCard("Usuarios", usersValue));
        stats.add(statCard("Playlists", playlistsValue));
        stats.add(statCard("Reproducciones hoy", playsValue));

        MyPanel bottom = AdminComponents.transparentPanel();
        bottom.applyLayout(new GridLayout(1, 2, 14, 0));
        bottom.add(listCard("Canciones por género", genreList));
        bottom.add(listCard("Actividad reciente", activityList));

        MyPanel center = AdminComponents.transparentPanel();
        center.applyLayout(new BorderLayout(0, 14));
        center.add(stats, BorderLayout.NORTH);
        MyPanel bottomWrapper = AdminComponents.transparentPanel();
        bottomWrapper.applyLayout(new BorderLayout());
        bottomWrapper.add(bottom, BorderLayout.NORTH);
        center.add(bottomWrapper, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    public void showStats(int songs, int users, int playlists, int playsToday) {
        songsValue.setText(String.valueOf(songs));
        usersValue.setText(String.valueOf(users));
        playlistsValue.setText(String.valueOf(playlists));
        playsValue.setText(String.valueOf(playsToday));
    }

    /**
     * Muestra una barra por género.
     *
     * @param genres cada elemento es {nombre, cantidad}
     */
    public void showGenreCounts(List<String[]> genres) {
        genreList.removeAll();
        int max = 1;
        for (String[] genre : genres) {
            max = Math.max(max, Integer.parseInt(genre[1]));
        }
        if (genres.isEmpty()) {
            genreList.add(AdminComponents.label("Todavía no hay canciones.", Palette.TEXT_MUTED, Font.PLAIN, 12));
        }
        for (String[] genre : genres) {
            genreList.add(genreBar(genre[0], Integer.parseInt(genre[1]), max, genre[2]));
            genreList.add(Box.createVerticalStrut(6));
        }
        refreshList(genreList);
    }

    /**
     * Muestra la actividad reciente.
     *
     * @param activity cada elemento es {texto, cuándo}
     */
    public void showActivity(List<String[]> activity) {
        activityList.removeAll();
        if (activity.isEmpty()) {
            activityList.add(AdminComponents.label("Sin actividad todavía.", Palette.TEXT_MUTED, Font.PLAIN, 12));
        }
        for (String[] item : activity) {
            MyPanel row = AdminComponents.transparentPanel();
            row.setBackgroundColor(Palette.CARD);
            row.applyLayout(new BorderLayout());
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(AdminComponents.label(item[0], Palette.TEXT, Font.PLAIN, 12), BorderLayout.WEST);
            row.add(AdminComponents.label(item[1], Palette.TEXT_MUTED, Font.PLAIN, 11), BorderLayout.EAST);
            activityList.add(row);
            activityList.add(Box.createVerticalStrut(4));
        }
        refreshList(activityList);
    }

    private static JLabel bigNumber() {
        return AdminComponents.label("0", Palette.ACCENT, Font.BOLD, 30);
    }

    private MyPanel statCard(String title, JLabel value) {
        MyPanel card = AdminComponents.card();
        card.applyLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(AdminComponents.label(title, Palette.TEXT_MUTED, Font.PLAIN, 12));
        card.add(value);
        return card;
    }

    private MyPanel listCard(String title, MyPanel list) {
        MyPanel card = AdminComponents.card();
        card.applyLayout(new BorderLayout(0, 12));
        card.add(AdminComponents.label(title, Palette.TEXT, Font.BOLD, 14), BorderLayout.NORTH);
        list.setBackgroundColor(Palette.CARD);
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    /** Fila "Pop ▬▬▬▬░░░░ 40": la barra se llena según la cantidad, con el color del género. */
    private MyPanel genreBar(String name, int count, int max, String colorName) {
        MyPanel row = AdminComponents.transparentPanel();
        row.setBackgroundColor(Palette.CARD);
        row.applyLayout(new BorderLayout(10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = AdminComponents.label(name, Palette.TEXT, Font.PLAIN, 12);
        nameLabel.setPreferredSize(new Dimension(80, 18));
        row.add(nameLabel, BorderLayout.WEST);

        MyPanel track = new MyPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                int filled = getWidth() * count / max;
                g.setColor(Palette.FIELD);
                g.fillRoundRect(0, getHeight() / 2 - 4, getWidth(), 8, 8, 8);
                g.setColor(AdminComponents.colorFor(colorName));
                g.fillRoundRect(0, getHeight() / 2 - 4, filled, 8, 8, 8);
            }
        };
        track.setBackgroundColor(Palette.CARD);
        track.setPreferredSize(null);
        row.add(track, BorderLayout.CENTER);

        JLabel countLabel = AdminComponents.label(String.valueOf(count), Palette.TEXT_MUTED, Font.PLAIN, 11);
        countLabel.setPreferredSize(new Dimension(30, 18));
        row.add(countLabel, BorderLayout.EAST);
        return row;
    }
}