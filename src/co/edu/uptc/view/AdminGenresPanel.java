package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
 * Géneros y personalización: tabla de géneros con su color y formulario para
 * crear o editar un género.
 * @author programacion 2
 */
public class AdminGenresPanel extends AdminSectionPanel {

    private static final String[] COLUMNS = { "Color", "Género", "Canciones", "Acciones" };
    private static final int[] WIDTHS = { 70, 150, 100, 150 };

    private final MyButton newButton = AdminComponents.accentButton("+ Nuevo género", 120);
    private final MyPanel list = createList();

    private final JLabel formTitle = new JLabel("Nuevo género");
    private final MyTextField nameField = AdminComponents.field("Nombre", false);
    private final JComboBox<String> colorBox = AdminComponents.comboBox(AdminComponents.GENRE_COLORS);
    private final MyButton imageButton = AdminComponents.smallButton("Imagen por defecto", 130);
    private final MyPanel imageRow;
    private MyPanel cover = AdminComponents.coverBox(null, 50);
    private final JLabel errorText = new JLabel();
    private final MyPanel errorBox = AdminComponents.messageBox(errorText, true);
    private final MyButton saveButton = AdminComponents.accentButton("Guardar", FORM_WIDTH - 36);

    private ActionListener editListener;
    private ActionListener deleteListener;
    private File selectedImage;
    private boolean editing;

    public AdminGenresPanel() {
        add(createHeader("Géneros y personalización", newButton), BorderLayout.NORTH);

        MyPanel table = AdminComponents.transparentPanel();
        table.applyLayout(new BorderLayout(0, 4));
        table.add(AdminComponents.tableHeader(COLUMNS, WIDTHS), BorderLayout.NORTH);
        table.add(AdminComponents.scroll(list), BorderLayout.CENTER);
        add(table, BorderLayout.CENTER);

        MyPanel form = createFormCard(formTitle);
        addToForm(form, nameField);
        addToForm(form, colorBox);
        imageRow = inlineRow(cover, imageButton);
        addToForm(form, imageRow);
        addToForm(form, errorBox);
        form.add(saveButton);
        form.add(Box.createVerticalStrut(4));
        addForm(form);

        imageButton.addActionListener(event -> chooseImage());
        showNewForm();
    }

    /**
     * Muestra los géneros.
     *
     * @param genres cada elemento es {color, nombre, cantidad de canciones}
     */
    public void showGenres(List<String[]> genres) {
        list.removeAll();
        for (int i = 0; i < genres.size(); i++) {
            String[] genre = genres.get(i);
            MyPanel actions = AdminComponents.transparentPanel();
            actions.setBackgroundColor(Palette.TRANSPARENT);
            actions.applyLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            actions.add(rowButton("Editar", i, editListener));
            actions.add(rowButton("Eliminar", i, deleteListener));
            list.add(AdminComponents.tableRow(i % 2 == 0 ? Palette.WINDOW_BACKGROUND : AdminComponents.ROW_DARK,
                    32, WIDTHS,
                    AdminComponents.colorPill(genre[0]),
                    AdminComponents.label(genre[1], Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.label(genre[2] + " canciones", Palette.TEXT, Font.PLAIN, 12), actions));
            list.add(Box.createVerticalStrut(2));
        }
        refreshList(list);
    }

    public void showNewForm() {
        editing = false;
        formTitle.setText("Nuevo género");
        fillForm("", AdminComponents.GENRE_COLORS[0], null);
    }

    public void showEditForm(String name, String color, File image) {
        editing = true;
        formTitle.setText("Editar género");
        fillForm(name, color, image);
    }

    private void fillForm(String name, String color, File image) {
        nameField.setText(name);
        colorBox.setSelectedItem(color);
        selectedImage = image;
        showCover();
        errorBox.setVisible(false);
    }

    public void showError(String message) {
        errorText.setText(message);
        errorBox.setVisible(true);
        revalidate();
    }

    public boolean isEditing() {
        return editing;
    }

    public String getNameText() {
        return nameField.getValue();
    }

    public String getColorName() {
        return String.valueOf(colorBox.getSelectedItem());
    }

    public File getSelectedImage() {
        return selectedImage;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Imagen por defecto");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (.png, .jpg, .jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = chooser.getSelectedFile();
            showCover();
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

    public void addSaveListener(ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    public void addEditListener(ActionListener listener) {
        editListener = listener;
    }

    public void addDeleteListener(ActionListener listener) {
        deleteListener = listener;
    }
}