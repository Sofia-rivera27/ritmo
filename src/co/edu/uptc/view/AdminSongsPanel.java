package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Canciones (CRUD): tabla con todas las canciones y formulario a la derecha
 * para crear o editar una canción.
 * @author programacion 2
 */
public class AdminSongsPanel extends AdminSectionPanel {

    private static final String[] COLUMNS = { "ID", "", "Título", "Artista", "Álbum", "Género", "Archivo",
            "Acciones" };
    private static final int[] WIDTHS = { 30, 44, 130, 110, 90, 100, 60, 80 };

    private final MyButton newButton = AdminComponents.accentButton("+ Nueva canción", 120);
    private final MyButton scanButton = AdminComponents.smallButton("Escanear carpeta", 120);
    private final MyPanel list = createList();

    private final JLabel formTitle = new JLabel("Editar canción");
    private final MyTextField titleField = AdminComponents.field("Título", false);
    private final MyTextField artistField = AdminComponents.field("Artista", false);
    private final MyTextField albumField = AdminComponents.field("Álbum", false);
    private final JComboBox<String> genreBox = AdminComponents.comboBox(new String[0]);
    private final MyButton fileButton = AdminComponents.smallButton("Elegir archivo mp3", 130);
    private final JLabel fileLabel = AdminComponents.label("", Palette.TEXT_MUTED, Font.PLAIN, 11);
    private final MyPanel fileRow;
    private final MyPanel imageRow;
    private MyPanel cover = AdminComponents.coverBox(null, 50);
    private final MyButton imageButton = AdminComponents.smallButton("Cambiar imagen", 110);
    private final JLabel warningText = new JLabel();
    private final MyPanel warningBox = AdminComponents.messageBox(warningText, false);
    private final JLabel deleteText = new JLabel();
    private final MyButton confirmButton = AdminComponents.smallButton("Confirmar", 80);
    private final MyPanel deleteBox = AdminComponents.messageBox(deleteText, true);
    private final MyButton saveButton = AdminComponents.accentButton("Guardar cambios", FORM_WIDTH - 36);

    private ActionListener editListener;
    private ActionListener deleteListener;
    private File selectedImage;
    private File selectedMp3;

    public AdminSongsPanel() {
        add(createHeader("Canciones (CRUD)", newButton, scanButton), BorderLayout.NORTH);

        MyPanel table = AdminComponents.transparentPanel();
        table.applyLayout(new BorderLayout(0, 4));
        table.add(AdminComponents.tableHeader(COLUMNS, WIDTHS), BorderLayout.NORTH);
        table.add(AdminComponents.scroll(list), BorderLayout.CENTER);
        add(table, BorderLayout.CENTER);

        MyPanel form = createFormCard(formTitle);
        addToForm(form, titleField);
        addToForm(form, artistField);
        addToForm(form, albumField);
        addToForm(form, genreBox);
        fileRow = inlineRow(fileButton, fileLabel);
        addToForm(form, fileRow);
        imageRow = inlineRow(cover, imageButton);
        addToForm(form, imageRow);
        addToForm(form, warningBox);
        deleteBox.add(confirmButton);
        addToForm(form, deleteBox);
        form.add(saveButton);
        form.add(Box.createVerticalStrut(4));
        addForm(form);

        imageButton.addActionListener(event -> chooseImage());
        fileButton.addActionListener(event -> chooseMp3());
        showNewForm(List.of());
    }

    /**
     * Muestra las canciones en la tabla.
     *
     * @param songs cada elemento es {título, artista, álbum, género, color del
     *              género, ruta de la imagen o ""}
     */
    public void showSongs(List<String[]> songs) {
        list.removeAll();
        if (songs.isEmpty()) {
            list.add(AdminComponents.label("  No hay canciones. Usa \"Escanear carpeta\" o \"+ Nueva canción\".",
                    Palette.TEXT_MUTED, Font.PLAIN, 12));
        }
        for (int i = 0; i < songs.size(); i++) {
            String[] song = songs.get(i);
            File image = song[5].isEmpty() ? null : new File(song[5]);
            list.add(AdminComponents.tableRow(i % 2 == 0 ? AdminComponents.ROW_DARK : Palette.WINDOW_BACKGROUND,
                    56, WIDTHS,
                    AdminComponents.label(String.valueOf(i + 1), Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.coverBox(image, 32),
                    AdminComponents.label(song[0], Palette.TEXT, Font.BOLD, 12),
                    AdminComponents.label(song[1], Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.label(song[2], Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.genreTag(song[3], song[4]),
                    AdminComponents.label(song[6], Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.stackedButtons(rowButton("Editar", i, editListener),
                            rowButton("Eliminar", i, deleteListener))));
            list.add(Box.createVerticalStrut(3));
        }
        refreshList(list);
    }

    /** Deja el formulario vacío para crear una canción nueva. */
    public void showNewForm(List<String> genres) {
        formTitle.setText("Nueva canción");
        saveButton.setText("Guardar");
        fillForm("", "", "", genres, "", null);
        selectedMp3 = null;
        fileLabel.setText("Ningún archivo");
        fileRow.setVisible(true);
    }

    /** Llena el formulario con los datos de la canción que se va a editar. */
    public void showEditForm(String title, String artist, String album, List<String> genres, String genre,
            File image) {
        formTitle.setText("Editar canción");
        saveButton.setText("Guardar cambios");
        fillForm(title, artist, album, genres, genre, image);
        fileRow.setVisible(false);
    }

    private void fillForm(String title, String artist, String album, List<String> genres, String genre,
            File image) {
        titleField.setText(title);
        artistField.setText(artist);
        albumField.setText(album);
        genreBox.removeAllItems();
        for (String name : genres) {
            genreBox.addItem(name);
        }
        if (!genre.isEmpty()) {
            genreBox.setSelectedItem(genre);
        }
        selectedImage = image;
        showCover();
        hideMessages();
    }

    public void showWarning(String message) {
        deleteBox.setVisible(false);
        warningText.setText(message);
        warningBox.setVisible(true);
    }

    /** Muestra el aviso rojo para confirmar que se quiere eliminar. */
    public void showDeleteConfirm(String title) {
        warningBox.setVisible(false);
        deleteText.setText("<html>¿Eliminar \"" + title + "\"?<br>No se puede deshacer.</html>");
        deleteText.setPreferredSize(new java.awt.Dimension(FORM_WIDTH - 140, 56));
        deleteBox.setVisible(true);
        revalidate();
    }

    public void hideMessages() {
        warningBox.setVisible(false);
        deleteBox.setVisible(false);
    }

    public String getTitleText() {
        return titleField.getValue();
    }

    public String getArtistText() {
        return artistField.getValue();
    }

    public String getAlbumText() {
        return albumField.getValue();
    }

    public String getGenreText() {
        Object genre = genreBox.getSelectedItem();
        return genre == null ? "" : genre.toString();
    }

    public File getSelectedImage() {
        return selectedImage;
    }

    public File getSelectedMp3() {
        return selectedMp3;
    }

    /** Abre el selector de carpetas. Devuelve null si se cancela. */
    public File chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Escanear carpeta");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Elegir imagen");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (.png, .jpg, .jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = chooser.getSelectedFile();
            showCover();
        }
    }

    private void chooseMp3() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Elegir canción");
        chooser.setFileFilter(new FileNameExtensionFilter("Canciones (.mp3)", "mp3"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedMp3 = chooser.getSelectedFile();
            fileLabel.setText(selectedMp3.getName());
            if (titleField.getValue().isEmpty()) {
                titleField.setText(selectedMp3.getName().replaceAll("(?i)\\.mp3$", ""));
            }
        }
    }

    private void showCover() {
        imageRow.remove(cover);
        cover = AdminComponents.coverBox(selectedImage, 50);
        imageRow.add(cover, 0);
        imageRow.revalidate();
        imageRow.repaint();
    }

    public void addNewListener(ActionListener listener) {
        newButton.addActionListener(listener);
    }

    public void addScanListener(ActionListener listener) {
        scanButton.addActionListener(listener);
    }

    public void addSaveListener(ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    public void addConfirmDeleteListener(ActionListener listener) {
        confirmButton.addActionListener(listener);
    }

    /** El actionCommand del evento es el índice de la fila. */
    public void addEditListener(ActionListener listener) {
        editListener = listener;
    }

    /** El actionCommand del evento es el índice de la fila. */
    public void addDeleteListener(ActionListener listener) {
        deleteListener = listener;
    }
}