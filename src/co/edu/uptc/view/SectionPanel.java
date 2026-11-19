package co.edu.uptc.view;

import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JLabel;

/**
 * Panel provisional para una sección del menú. Solo muestra el nombre de la
 * sección. Se reemplaza por el panel real cuando se diseñe la sección.
 */
public class SectionPanel extends MyPanel {

    /**
     * Crea el panel.
     *
     * @param title nombre de la sección que ve el usuario
     */
    public SectionPanel(String title) {
        setBackgroundColor(Palette.BACKGROUND);
        applyLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 28));
        titleLabel.setForeground(Palette.TEXT_MUTED);

        addComponent(titleLabel);
    }
}