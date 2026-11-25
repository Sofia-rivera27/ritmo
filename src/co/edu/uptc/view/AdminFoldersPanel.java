package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

/**
 * Carpetas y escaneo: lista de carpetas autorizadas y resultado del último
 * escaneo.
 * @author programacion 2
 */
public class AdminFoldersPanel extends AdminSectionPanel {

    private static final int[] WIDTHS = { 220, 80, 90, 70 };

    private final MyButton authorizeButton = AdminComponents.accentButton("+ Autorizar carpeta", 140);
    private final MyButton scanAllButton = AdminComponents.accentButton("Escanear todas", 110);
    private final MyPanel list = createList();
    private final JLabel newValue = resultValue();
    private final JLabel existingValue = resultValue();
    private final JLabel noInfoValue = resultValue();
    private final JLabel errorText = new JLabel();
    private final MyPanel errorBox = AdminComponents.messageBox(errorText, true);

    private ActionListener removeListener;

    public AdminFoldersPanel() {
        add(createHeader("Carpetas y escaneo", authorizeButton), BorderLayout.NORTH);

        MyPanel foldersCard = AdminComponents.card();
        foldersCard.applyLayout(new BoxLayout(foldersCard, BoxLayout.Y_AXIS));
        foldersCard.add(AdminComponents.label("Carpetas autorizadas", Palette.TEXT, Font.BOLD, 14));
        foldersCard.add(AdminComponents.label("El escaneo solo lee carpetas autorizadas.", Palette.TEXT_MUTED,
                Font.PLAIN, 11));
        foldersCard.add(Box.createVerticalStrut(10));
        list.setBackgroundColor(Palette.CARD);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
        foldersCard.add(list);
        foldersCard.add(Box.createVerticalStrut(10));
        MyPanel scanRow = AdminComponents.transparentPanel();
        scanRow.setBackgroundColor(Palette.CARD);
        scanRow.applyLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        scanRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        scanRow.add(scanAllButton);
        foldersCard.add(scanRow);

        MyPanel resultCard = AdminComponents.card();
        resultCard.applyLayout(new BoxLayout(resultCard, BoxLayout.Y_AXIS));
        resultCard.add(AdminComponents.label("Resultado del último escaneo", Palette.TEXT, Font.BOLD, 14));
        resultCard.add(Box.createVerticalStrut(10));
        resultCard.add(resultRow("Nuevas", newValue));
        resultCard.add(resultRow("Ya existían", existingValue));
        resultCard.add(resultRow("Sin información", noInfoValue));
        resultCard.add(Box.createVerticalStrut(8));
        errorBox.setBackgroundColor(AdminComponents.ERROR_BACKGROUND);
        resultCard.add(errorBox);

        MyPanel cards = AdminComponents.transparentPanel();
        cards.applyLayout(new GridLayout(1, 2, 14, 0));
        cards.add(foldersCard);
        cards.add(resultCard);
        MyPanel wrapper = AdminComponents.transparentPanel();
        wrapper.applyLayout(new BorderLayout());
        wrapper.add(cards, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);
        showScanResult(0, 0, 0);
    }

    /**
     * Muestra las carpetas autorizadas.
     *
     * @param folders cada elemento es {ruta, cantidad de mp3}
     */
    public void showFolders(List<String[]> folders) {
        list.removeAll();
        if (folders.isEmpty()) {
            list.add(AdminComponents.label("No hay carpetas autorizadas.", Palette.TEXT_MUTED, Font.PLAIN, 12));
        }
        for (int i = 0; i < folders.size(); i++) {
            String[] folder = folders.get(i);
            MyButton remove = rowButton("Quitar", i, removeListener);
            list.add(AdminComponents.tableRow(Palette.CARD, 30, WIDTHS,
                    AdminComponents.label(folder[0], Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.label(folder[1] + " mp3", Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.label("Autorizada", Palette.TEXT, Font.PLAIN, 12), remove));
        }
        refreshList(list);
    }

    public void showScanResult(int newSongs, int existing, int noInfo) {
        newValue.setText(String.valueOf(newSongs));
        existingValue.setText(String.valueOf(existing));
        noInfoValue.setText(String.valueOf(noInfo));
    }

    /** Muestra un error en rojo; si el mensaje es null lo oculta. */
    public void showError(String message) {
        errorBox.setVisible(message != null);
        errorText.setText(message == null ? "" : message);
        revalidate();
    }

    /** Abre el selector de carpetas. Devuelve null si se cancela. */
    public File chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Autorizar carpeta");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private static JLabel resultValue() {
        return AdminComponents.label("0", Palette.TEXT, Font.BOLD, 12);
    }

    private MyPanel resultRow(String title, JLabel value) {
        MyPanel row = AdminComponents.transparentPanel();
        row.setBackgroundColor(Palette.CARD);
        row.applyLayout(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(AdminComponents.label(title, Palette.TEXT, Font.PLAIN, 12), BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    public void addAuthorizeListener(ActionListener listener) {
        authorizeButton.addActionListener(listener);
    }

    public void addScanAllListener(ActionListener listener) {
        scanAllButton.addActionListener(listener);
    }

    /** El actionCommand del evento es el índice de la carpeta. */
    public void addRemoveListener(ActionListener listener) {
        removeListener = listener;
    }
}