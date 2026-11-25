package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * Usuarios (CRUD): tabla con las cuentas y formulario para crear o editar.
 * @author programacion 2
 */
public class AdminUsersPanel extends AdminSectionPanel {

    public static final String ROLE_USER_TEXT = "Usuario";
    public static final String ROLE_ADMIN_TEXT = "Administrador";
    public static final String ACTIVE_TEXT = "Activo";
    public static final String INACTIVE_TEXT = "Inactivo";

    private static final String[] COLUMNS = { "Nombre", "Rol", "Estado", "Acciones" };
    private static final int[] WIDTHS = { 160, 120, 90, 80 };

    private final MyButton newButton = AdminComponents.accentButton("+ Nuevo usuario", 120);
    private final MyPanel list = createList();

    private final JLabel formTitle = new JLabel("Nuevo usuario");
    private final MyTextField nameField = AdminComponents.field("Nombre", false);
    private final MyTextField passwordField = AdminComponents.field("Contraseña", true);
    private final JComboBox<String> roleBox = AdminComponents
            .comboBox(new String[] { "Rol: " + ROLE_USER_TEXT, "Rol: " + ROLE_ADMIN_TEXT });
    private final JComboBox<String> stateBox = AdminComponents
            .comboBox(new String[] { "Estado: " + ACTIVE_TEXT, "Estado: " + INACTIVE_TEXT });
    private final JLabel errorText = new JLabel();
    private final MyPanel errorBox = AdminComponents.messageBox(errorText, true);
    private final MyButton saveButton = AdminComponents.accentButton("Guardar", FORM_WIDTH - 36);

    private ActionListener editListener;
    private ActionListener deleteListener;

    public AdminUsersPanel() {
        add(createHeader("Usuarios (CRUD)", newButton), BorderLayout.NORTH);

        MyPanel table = AdminComponents.transparentPanel();
        table.applyLayout(new BorderLayout(0, 4));
        table.add(AdminComponents.tableHeader(COLUMNS, WIDTHS), BorderLayout.NORTH);
        table.add(AdminComponents.scroll(list), BorderLayout.CENTER);
        add(table, BorderLayout.CENTER);

        MyPanel form = createFormCard(formTitle);
        addToForm(form, nameField);
        addToForm(form, passwordField);
        addToForm(form, roleBox);
        addToForm(form, stateBox);
        addToForm(form, errorBox);
        form.add(saveButton);
        form.add(Box.createVerticalStrut(4));
        addForm(form);
        showNewForm();
    }

    /**
     * Muestra las cuentas.
     *
     * @param users cada elemento es {nombre, rol, estado}
     */
    public void showUsers(List<String[]> users) {
        list.removeAll();
        for (int i = 0; i < users.size(); i++) {
            String[] user = users.get(i);
            list.add(AdminComponents.tableRow(i % 2 == 0 ? Palette.WINDOW_BACKGROUND : AdminComponents.ROW_DARK,
                    52, WIDTHS,
                    AdminComponents.label(user[0], Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.label(user[1], Palette.TEXT, Font.PLAIN, 12),
                    AdminComponents.label(user[2], INACTIVE_TEXT.equals(user[2]) ? Palette.TEXT_MUTED : Palette.TEXT,
                            Font.PLAIN, 12),
                    AdminComponents.stackedButtons(rowButton("Editar", i, editListener),
                            rowButton("Eliminar", i, deleteListener))));
            list.add(Box.createVerticalStrut(3));
        }
        refreshList(list);
    }

    public void showNewForm() {
        formTitle.setText("Nuevo usuario");
        nameField.setText("");
        nameField.setEnabled(true);
        passwordField.setText("");
        roleBox.setSelectedIndex(0);
        stateBox.setSelectedIndex(0);
        stateBox.setVisible(false);
        errorBox.setVisible(false);
    }

    /** Llena el formulario con la cuenta que se va a editar. El nombre no se cambia. */
    public void showEditForm(String name, boolean admin, boolean active) {
        formTitle.setText("Editar usuario");
        nameField.setText(name);
        nameField.setEnabled(false);
        passwordField.setText("");
        roleBox.setSelectedIndex(admin ? 1 : 0);
        stateBox.setSelectedIndex(active ? 0 : 1);
        stateBox.setVisible(true);
        errorBox.setVisible(false);
    }

    public void showError(String message) {
        errorText.setText(message);
        errorBox.setVisible(true);
        revalidate();
    }

    public boolean isEditing() {
        return !nameField.isEnabled();
    }

    public String getNameText() {
        return nameField.getValue();
    }

    public String getPasswordText() {
        return passwordField.getValue();
    }

    public boolean isAdminSelected() {
        return roleBox.getSelectedIndex() == 1;
    }

    public boolean isActiveSelected() {
        return stateBox.getSelectedIndex() == 0;
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