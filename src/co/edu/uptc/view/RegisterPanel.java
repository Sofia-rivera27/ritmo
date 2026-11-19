package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Tarjeta de inicio de sesión y registro. Solo vista: los botones todavía no
 * hacen nada. Más adelante el Presenter los conecta usando los getters.
 */
public class RegisterPanel extends MyPanel {

    private static final Color ERROR_BACKGROUND = new Color(62, 26, 26);
    private static final Color ERROR_BORDER = new Color(225, 90, 90);
    private static final Color ERROR_TEXT = new Color(255, 170, 170);
    private static final Color SECONDARY_BORDER = new Color(85, 85, 85);
    private static final int CONTENT_WIDTH = 274;
    private static final int FIELD_HEIGHT = 33;
    private static final int SPACING = 10;

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private MyTextField userField;
    private MyTextField passwordField;
    private MyTextField confirmPasswordField;
    private MyPanel roleRow;
    private MyButton userRoleButton;
    private MyButton adminRoleButton;
    private MyPanel errorBox;
    private JLabel errorLabel;
    private MyButton primaryButton;
    private MyButton secondaryButton;

    // Espacios que se ocultan junto con su componente
    private JComponent confirmSpacer;
    private JComponent roleSpacer;
    private JComponent errorSpacer;

    public RegisterPanel() {
        setBackgroundColor(Palette.CARD);
        setCornerRadius(12);
        setBorderColor(Palette.CARD_BORDER);
        applyLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(26, 27, 26, 27));
        setPreferredSize(null);

        createComponents();
        buildLayout();

        selectRole(false);
        setSignUpMode(false);
    }

    // ---------------------------------------------------------------
    // Componentes
    // ---------------------------------------------------------------
    private void createComponents() {
        titleLabel = new JLabel("Ritmo");
        titleLabel.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 26));
        titleLabel.setForeground(Palette.ACCENT);

        subtitleLabel = new JLabel("Inicia sesión para continuar");
        subtitleLabel.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));
        subtitleLabel.setForeground(Palette.TEXT_MUTED);

        userField = new MyTextField("Usuario", false);
        userField.setFixedSize(CONTENT_WIDTH, FIELD_HEIGHT);

        passwordField = new MyTextField("Contraseña", true);
        passwordField.setFixedSize(CONTENT_WIDTH, FIELD_HEIGHT);

        confirmPasswordField = new MyTextField("Confirmar contraseña", true);
        confirmPasswordField.setFixedSize(CONTENT_WIDTH, FIELD_HEIGHT);

        // Botones tipo "chip" para elegir el rol
        userRoleButton = new MyButton("Usuario", Palette.ACCENT, Palette.TEXT_DARK, null);
        userRoleButton.setFixedSize(67, 28);
        userRoleButton.setCornerRadius(14);

        adminRoleButton = new MyButton("Administrador", Palette.FIELD, Palette.TEXT, null);
        adminRoleButton.setFixedSize(97, 28);
        adminRoleButton.setCornerRadius(14);

        roleRow = new MyPanel();
        roleRow.setBackgroundColor(Palette.TRANSPARENT);
        roleRow.applyLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        roleRow.setPreferredSize(CONTENT_WIDTH, 28);
        roleRow.setMaximumSize(new Dimension(CONTENT_WIDTH, 28));
        roleRow.addComponent(userRoleButton);
        roleRow.addComponent(Box.createHorizontalStrut(7));
        roleRow.addComponent(adminRoleButton);

        errorLabel = new JLabel();
        errorLabel.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));
        errorLabel.setForeground(ERROR_TEXT);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        errorBox = new MyPanel();
        errorBox.setBackgroundColor(ERROR_BACKGROUND);
        errorBox.setBorderColor(ERROR_BORDER);
        errorBox.setCornerRadius(4);
        errorBox.applyLayout(new BorderLayout());
        errorBox.setPreferredSize(CONTENT_WIDTH, 28);
        errorBox.setMaximumSize(new Dimension(CONTENT_WIDTH, 28));
        errorBox.addComponent(errorLabel);

        primaryButton = new MyButton("Ingresar", Palette.ACCENT, Palette.TEXT_DARK, null);
        primaryButton.setFontStyle(Font.BOLD, 12);
        primaryButton.setFixedSize(CONTENT_WIDTH, 30);

        secondaryButton = new MyButton("Crear cuenta", Palette.TRANSPARENT, Palette.TEXT, SECONDARY_BORDER);
        secondaryButton.setFixedSize(CONTENT_WIDTH, 30);

        confirmSpacer = spacer(SPACING);
        roleSpacer = spacer(SPACING);
        errorSpacer = spacer(SPACING);
    }

    /** Agrega todos los componentes en orden, de arriba hacia abajo. */
    private void buildLayout() {
        addAligned(titleLabel);
        addAligned(spacer(4));
        addAligned(subtitleLabel);
        addAligned(spacer(14));
        addAligned(userField);
        addAligned(spacer(SPACING));
        addAligned(passwordField);
        addAligned(spacer(SPACING));
        addAligned(confirmPasswordField);
        addAligned(confirmSpacer);
        addAligned(roleRow);
        addAligned(roleSpacer);
        addAligned(errorBox);
        addAligned(errorSpacer);
        addAligned(primaryButton);
        addAligned(spacer(SPACING));
        addAligned(secondaryButton);
    }

    private void addAligned(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(component);
    }

    private JComponent spacer(int height) {
        JComponent spacer = (JComponent) Box.createVerticalStrut(height);
        spacer.setAlignmentX(Component.LEFT_ALIGNMENT);
        return spacer;
    }

    // ---------------------------------------------------------------
    // Cambios visuales (el Presenter los llamará cuando se conecten)
    // ---------------------------------------------------------------

    /**
     * Resalta el chip del rol elegido.
     *
     * @param admin true para elegir administrador, false para elegir usuario
     */
    public void selectRole(boolean admin) {
        paintRoleButton(adminRoleButton, admin);
        paintRoleButton(userRoleButton, !admin);
    }

    private void paintRoleButton(MyButton button, boolean selected) {
        if (selected) {
            button.setColors(Palette.ACCENT, Palette.TEXT_DARK, null);
            button.setFontStyle(Font.BOLD, 12);
        } else {
            button.setColors(Palette.FIELD, Palette.TEXT, null);
            button.setFontStyle(Font.PLAIN, 12);
        }
    }

    /**
     * Muestra la tarjeta en modo registro o en modo inicio de sesión.
     *
     * @param signUp true para registro, false para inicio de sesión
     */
    public void setSignUpMode(boolean signUp) {
        userField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        hideMessage();

        if (signUp) {
            subtitleLabel.setText("Crea tu cuenta para continuar");
            primaryButton.setText("Registrarse");
            secondaryButton.setText("Ya tengo cuenta");
        } else {
            subtitleLabel.setText("Inicia sesión para continuar");
            primaryButton.setText("Ingresar");
            secondaryButton.setText("Crear cuenta");
        }

        // El campo de confirmar solo va en registro; los chips de rol solo en inicio de sesión
        confirmPasswordField.setVisible(signUp);
        confirmSpacer.setVisible(signUp);
        roleRow.setVisible(!signUp);
        roleSpacer.setVisible(!signUp);

        revalidate();
        repaint();
    }

    /**
     * Muestra un mensaje de error en la caja roja.
     *
     * @param message texto que ve el usuario
     */
    public void showMessage(String message) {
        errorLabel.setText(message);
        errorBox.setVisible(true);
        errorSpacer.setVisible(true);
        revalidate();
        repaint();
    }

    public void hideMessage() {
        errorBox.setVisible(false);
        errorSpacer.setVisible(false);
        revalidate();
        repaint();
    }

    // ---------------------------------------------------------------
    // Getters para que el Presenter pueda conectar los componentes después
    // ---------------------------------------------------------------
    public MyTextField getUserField() {
        return userField;
    }

    public MyTextField getPasswordField() {
        return passwordField;
    }

    public MyTextField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    public MyButton getUserRoleButton() {
        return userRoleButton;
    }

    public MyButton getAdminRoleButton() {
        return adminRoleButton;
    }

    public MyButton getPrimaryButton() {
        return primaryButton;
    }

    public MyButton getSecondaryButton() {
        return secondaryButton;
    }
}