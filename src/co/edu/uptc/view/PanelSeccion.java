package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel provisional para cada seccion del menu (solo muestra el nombre).
 * Cuando decidas como sera cada seccion, lo reemplazas por tu propio panel.
 */
public class PanelSeccion extends MyPanel {

    public PanelSeccion(String nombre) {
        cambiarColor(new Color(3, 3, 3));
        cambiarLayout(new GridBagLayout());

        JLabel texto = new JLabel(nombre);
        texto.setFont(new Font("SansSerif", Font.BOLD, 28));
        texto.setForeground(new Color(150, 150, 150));

        agregarComponente(texto);
    }
}
