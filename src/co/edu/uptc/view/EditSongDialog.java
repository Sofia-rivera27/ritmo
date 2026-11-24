package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Diálogo para cambiar el título y la portada de una canción de la biblioteca. */
public class EditSongDialog extends JDialog {

    private final MyTextField titleField = new MyTextField("Título de la canción *", false);
    private final JLabel imageNameLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private final MyButton chooseImageButton = new MyButton("Cambiar imagen...", Palette.CARD, Palette.TEXT,
            Palette.CARD_BORDER);
    private final MyButton removeImageButton = new MyButton("Quitar", Palette.TRANSPARENT, Palette.TEXT,
            Palette.TEXT_MUTED);
    private final MyButton saveButton = new MyButton("Guardar cambios", Palette.ACCENT, Palette.TEXT_DARK, null);
    private File selectedImage;

    public EditSongDialog(Window owner, String title, File image) {
        super(owner, "Editar canción", ModalityType.APPLICATION_MODAL);
        selectedImage = image;
        setResizable(false);

        MyPanel root = new MyPanel();
        root.setBackgroundColor(Palette.BACKGROUND);
        root.applyLayout(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.setPreferredSize(new Dimension(440, 250));

        MyPanel body = new MyPanel();
        body.setBackgroundColor(Palette.BACKGROUND);
        body.applyLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setPreferredSize(null);

        JLabel titleLabel = createLabel("Título", Palette.TEXT_MUTED, Font.PLAIN, 11);
        titleField.setText(title);
        titleField.setFixedSize(396, 36);
        body.add(titleLabel);
        body.add(Box.createVerticalStrut(6));
        body.add(titleField);
        body.add(Box.createVerticalStrut(16));
        body.add(createImageSection());

        statusLabel.setForeground(Palette.ACCENT);
        statusLabel.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));

        MyButton cancelButton = new MyButton("Cancelar", Palette.TRANSPARENT, Palette.TEXT, Palette.TEXT_MUTED);
        cancelButton.setFixedSize(88, 32);
        cancelButton.addActionListener(event -> dispose());
        saveButton.setFixedSize(138, 32);
        saveButton.setFontStyle(Font.BOLD, 11);

        MyPanel buttons = new MyPanel();
        buttons.setBackgroundColor(Palette.BACKGROUND);
        buttons.applyLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        MyPanel footer = new MyPanel();
        footer.setBackgroundColor(Palette.BACKGROUND);
        footer.applyLayout(new BorderLayout(8, 0));
        footer.setPreferredSize(new Dimension(0, 36));
        footer.add(statusLabel, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.EAST);

        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        chooseImageButton.setFixedSize(128, 32);
        chooseImageButton.addActionListener(event -> chooseImage());
        removeImageButton.setFixedSize(62, 32);
        removeImageButton.addActionListener(event -> {
            selectedImage = null;
            updateImageName();
            setStatus(" ");
        });

        setContentPane(root);
        ThemeManager.refresh(root);
        updateImageName();
        pack();
        setLocationRelativeTo(owner);
    }

    private MyPanel createImageSection() {
        MyPanel imageRow = new MyPanel();
        imageRow.setBackgroundColor(Palette.CARD);
        imageRow.setCornerRadius(10);
        imageRow.setBorderColor(Palette.CARD_BORDER);
        imageRow.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        imageRow.applyLayout(new BorderLayout(12, 0));

        MyPanel coverIcon = new MyPanel();
        coverIcon.setBackgroundColor(new Color(40, 40, 40));
        coverIcon.setCornerRadius(8);
        coverIcon.setPreferredSize(new Dimension(46, 46));
        coverIcon.applyLayout(new GridBagLayout());
        JLabel musicNote = createLabel("♪", Palette.ACCENT, Font.BOLD, 18);
        coverIcon.add(musicNote);

        imageNameLabel.setForeground(Palette.TEXT_MUTED);
        imageNameLabel.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));
        imageNameLabel.setToolTipText(selectedImage == null ? null : selectedImage.getAbsolutePath());

        MyPanel imageButtons = new MyPanel();
        imageButtons.setBackgroundColor(Palette.TRANSPARENT);
        imageButtons.applyLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        imageButtons.add(chooseImageButton);
        imageButtons.add(removeImageButton);

        imageRow.add(coverIcon, BorderLayout.WEST);
        imageRow.add(imageNameLabel, BorderLayout.CENTER);
        imageRow.add(imageButtons, BorderLayout.EAST);
        return imageRow;
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar imagen de portada");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (.png, .jpg, .jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = chooser.getSelectedFile();
            updateImageName();
            setStatus(" ");
        }
    }

    private void updateImageName() {
        imageNameLabel.setText(selectedImage == null ? "Sin imagen de portada" : selectedImage.getName());
        imageNameLabel.setToolTipText(selectedImage == null ? null : selectedImage.getAbsolutePath());
    }

    private JLabel createLabel(String text, Color color, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font(Palette.FONT_FAMILY, style, size));
        return label;
    }

    public void addSaveListener(ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    public String getSongTitle() {
        return titleField.getValue();
    }

    public File getSelectedImage() {
        return selectedImage;
    }

    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }
}
