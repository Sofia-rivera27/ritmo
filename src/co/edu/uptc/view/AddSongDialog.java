package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

/**
 * Pantalla modal para agregar canciones a la biblioteca desde una carpeta
 * o ingresando sus datos manualmente.
 *
 * @author progra 2
 */
public class AddSongDialog extends JDialog {

    private static final int FIELD_WIDTH = 340;
    private static final int FIELD_HEIGHT = 32;

    private final MyTextField folderField = new MyTextField("Ninguna carpeta seleccionada", false);
    private final MyButton chooseFolderButton = new MyButton("Elegir carpeta...", Palette.CARD, Palette.TEXT,
            Palette.CARD_BORDER);
    private final MyButton importFolderButton = new MyButton("Importar carpeta", Palette.ACCENT, Palette.TEXT_DARK,
            null);

    private final MyTextField titleField = new MyTextField("Título de la canción *", false);
    private final MyTextField artistField = new MyTextField("Artista (opcional)", false);
    private final MyTextField albumField = new MyTextField("Álbum (opcional)", false);
    private final MyTextField genreField = new MyTextField("Género musical *", false);
    private final MyTextField audioField = new MyTextField("Ruta del archivo .mp3 *", false);
    private final MyTextField imageField = new MyTextField("Imagen de portada (opcional)", false);

    private final MyButton chooseAudioButton = new MyButton("Buscar MP3...", Palette.CARD, Palette.TEXT,
            Palette.CARD_BORDER);
    private final MyButton chooseImageButton = new MyButton("Buscar imagen...", Palette.CARD, Palette.TEXT,
            Palette.CARD_BORDER);
    private final MyButton addManualButton = new MyButton("Agregar canción", Palette.ACCENT, Palette.TEXT_DARK, null);
    private final MyButton closeButton = new MyButton("Cerrar", Palette.TRANSPARENT, Palette.TEXT,
            Palette.TEXT_MUTED);
    private final JLabel statusLabel = new JLabel(" ");

    private File selectedFolder;
    private File selectedAudio;
    private File selectedImage;

    /**
     * Construye el diálogo modal para agregar canciones.
     *
     * @param owner ventana padre sobre la que se centra el diálogo
     */
    public AddSongDialog(Window owner) {
        super(owner, "Agregar canciones", ModalityType.APPLICATION_MODAL);
        setResizable(false);

        MyPanel root = new MyPanel();
        root.setBackgroundColor(Palette.BACKGROUND);
        root.applyLayout(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        root.setPreferredSize(new Dimension(500, 560));

        MyPanel body = new MyPanel();
        body.setBackgroundColor(Palette.BACKGROUND);
        body.applyLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setPreferredSize(null);

        body.add(createFolderSection());
        body.add(Box.createVerticalStrut(16));
        body.add(createManualSection());

        statusLabel.setForeground(Palette.ACCENT);
        statusLabel.setFont(new Font(Palette.FONT_FAMILY, Font.PLAIN, 11));

        closeButton.setFixedSize(90, 30);
        closeButton.addActionListener(event -> dispose());

        MyPanel footer = new MyPanel();
        footer.setBackgroundColor(Palette.BACKGROUND);
        footer.applyLayout(new BorderLayout(8, 0));
        footer.setPreferredSize(new Dimension(0, 34));
        footer.add(statusLabel, BorderLayout.CENTER);
        footer.add(closeButton, BorderLayout.EAST);

        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        wireChoosers();
        setContentPane(root);
        ThemeManager.refresh(root);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Crea la tarjeta para seleccionar e importar canciones desde una carpeta.
     *
     * @return panel de importación por carpeta
     */
    private MyPanel createFolderSection() {
        MyPanel card = createCard("Agregar desde una carpeta");

        folderField.setEditable(false);
        chooseFolderButton.setFixedSize(128, FIELD_HEIGHT);
        importFolderButton.setFixedSize(150, 32);
        importFolderButton.setFontStyle(Font.BOLD, 11);

        MyPanel actionRow = createRowPanel();
        actionRow.applyLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionRow.add(importFolderButton);

        card.add(createPickerRow(folderField, chooseFolderButton));
        card.add(Box.createVerticalStrut(10));
        card.add(actionRow);
        return card;
    }

    /**
     * Crea la tarjeta con el formulario para agregar una canción manualmente.
     *
     * @return panel de registro manual
     */
    private MyPanel createManualSection() {
        MyPanel card = createCard("Agregar canción manualmente");

        titleField.setFixedSize(420, FIELD_HEIGHT);
        artistField.setFixedSize(420, FIELD_HEIGHT);
        albumField.setFixedSize(420, FIELD_HEIGHT);
        genreField.setFixedSize(420, FIELD_HEIGHT);

        audioField.setEditable(false);
        chooseAudioButton.setFixedSize(128, FIELD_HEIGHT);

        imageField.setEditable(false);
        chooseImageButton.setFixedSize(128, FIELD_HEIGHT);

        addManualButton.setFixedSize(150, 32);
        addManualButton.setFontStyle(Font.BOLD, 11);

        addField(card, titleField);
        addField(card, artistField);
        addField(card, albumField);
        addField(card, genreField);

        card.add(createPickerRow(audioField, chooseAudioButton));
        card.add(Box.createVerticalStrut(8));

        card.add(createPickerRow(imageField, chooseImageButton));
        card.add(Box.createVerticalStrut(12));

        MyPanel actionRow = createRowPanel();
        actionRow.applyLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionRow.add(addManualButton);
        card.add(actionRow);

        return card;
    }

    /**
     * Crea una fila con un campo de texto a la izquierda y su botón selector a
     * la derecha.
     *
     * @param field  campo que muestra la ruta seleccionada
     * @param button botón que abre el selector de archivos
     * @return panel con ambos componentes alineados
     */
    private MyPanel createPickerRow(MyTextField field, MyButton button) {
        MyPanel row = new MyPanel();
        row.setBackgroundColor(Palette.TRANSPARENT);
        row.applyLayout(new BorderLayout(8, 0));
        row.setPreferredSize(new Dimension(420, FIELD_HEIGHT));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(field, BorderLayout.CENTER);
        row.add(button, BorderLayout.EAST);
        return row;
    }
    /**
     * Conecta los selectores de archivos locales (carpeta, MP3 e imagen).
     */
    private void wireChoosers() {
        chooseFolderButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar carpeta con archivos MP3");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFolder = chooser.getSelectedFile();
                folderField.setText(selectedFolder.getAbsolutePath());
                setStatus(" ");
            }
        });

        chooseAudioButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar archivo MP3");
            chooser.setFileFilter(new FileNameExtensionFilter("Archivos MP3", "mp3"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedAudio = chooser.getSelectedFile();
                audioField.setText(selectedAudio.getAbsolutePath());
                setStatus(" ");
            }
        });

        chooseImageButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar imagen de portada");
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (.png, .jpg, .jpeg)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImage = chooser.getSelectedFile();
                imageField.setText(selectedImage.getAbsolutePath());
                setStatus(" ");
            }
        });
    }

    private MyPanel createCard(String title) {
        MyPanel card = new MyPanel();
        card.setBackgroundColor(Palette.CARD);
        card.setBorderColor(Palette.CARD_BORDER);
        card.setCornerRadius(12);
        card.applyLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(null);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(title);
        label.setForeground(Palette.TEXT);
        label.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(label);
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    private MyPanel createRowPanel() {
        MyPanel row = new MyPanel();
        row.setBackgroundColor(Palette.TRANSPARENT);
        row.applyLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setPreferredSize(null);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private void addField(MyPanel card, MyTextField field) {
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(field);
        card.add(Box.createVerticalStrut(8));
    }

    /**
     * Registra el listener del botón "Importar carpeta".
     *
     * @param listener listener que atiende la importación de carpeta
     */
    public void addImportFolderListener(ActionListener listener) {
        importFolderButton.addActionListener(listener);
    }

    /**
     * Registra el listener del botón "Agregar canción".
     *
     * @param listener listener que atiende el registro manual
     */
    public void addManualSongListener(ActionListener listener) {
        addManualButton.addActionListener(listener);
    }

    /**
     * Limpia los campos del formulario manual tras agregar una canción.
     */
    public void clearManualFields() {
        titleField.clear();
        artistField.clear();
        albumField.clear();
        genreField.clear();
        audioField.clear();
        imageField.clear();
        selectedAudio = null;
        selectedImage = null;
    }

    /**
     * Muestra un mensaje de estado o error en la parte inferior del diálogo.
     *
     * @param message texto que ve el usuario
     */
    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? " " : message);
    }

    public File getSelectedFolder() {
        return selectedFolder;
    }

    public String getSongTitle() {
        return titleField.getValue();
    }

    public String getSongArtist() {
        return artistField.getValue();
    }

    public String getSongAlbum() {
        return albumField.getValue();
    }

    public String getSongGenre() {
        return genreField.getValue();
    }

    public File getSelectedAudio() {
        return selectedAudio;
    }

    public File getSelectedImage() {
        return selectedImage;
    }
}