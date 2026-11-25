package co.edu.uptc.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

/**
 * Base de las secciones del administrador: título arriba, tabla en el centro
 * y (si hace falta) un formulario a la derecha.
 * @author programacion 2
 */
public abstract class AdminSectionPanel extends MyPanel {

    protected static final int FORM_WIDTH = 270;

    protected AdminSectionPanel() {
        applyLayout(new BorderLayout(18, 12));
        setBackgroundColor(Palette.WINDOW_BACKGROUND);
        setPreferredSize(null);
    }

    /** Fila de arriba: título a la izquierda y botones a la derecha. */
    protected MyPanel createHeader(String title, MyButton... buttons) {
        MyPanel header = AdminComponents.transparentPanel();
        header.applyLayout(new BorderLayout());
        header.add(AdminComponents.label(title, Palette.TEXT, Font.BOLD, 18), BorderLayout.WEST);
        MyPanel right = AdminComponents.transparentPanel();
        right.applyLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        for (MyButton button : buttons) {
            right.add(button);
        }
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /** Panel donde se apilan las filas de una tabla. */
    protected MyPanel createList() {
        MyPanel list = AdminComponents.transparentPanel();
        list.applyLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        return list;
    }

    /** Tarjeta del formulario de la derecha. */
    protected MyPanel createFormCard(JLabel title) {
        MyPanel form = AdminComponents.card();
        form.applyLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(null);
        title.setFont(new Font(Palette.FONT_FAMILY, Font.BOLD, 14));
        title.setForeground(Palette.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(12));
        return form;
    }

    /** Pone la tarjeta del formulario arriba a la derecha, sin estirarla. */
    protected void addForm(MyPanel form) {
        MyPanel east = AdminComponents.transparentPanel();
        east.applyLayout(new BorderLayout());
        east.setPreferredSize(new Dimension(FORM_WIDTH, 0));
        east.add(form, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);
    }

    /** Agrega un componente al formulario con un espacio debajo. */
    protected void addToForm(MyPanel form, Component component) {
        if (component instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        form.add(component);
        form.add(Box.createVerticalStrut(8));
    }

    /** Fila con un botón y un texto al lado (por ejemplo "Cambiar imagen"). */
    protected MyPanel inlineRow(Component... components) {
        MyPanel row = AdminComponents.transparentPanel();
        row.setBackgroundColor(Palette.CARD);
        row.applyLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBorder(BorderFactory.createEmptyBorder());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        for (Component component : components) {
            row.add(component);
        }
        return row;
    }

    /** Agrega un listener que recibe el índice de la fila en el actionCommand. */
    protected MyButton rowButton(String text, int index, ActionListener listener) {
        MyButton button = AdminComponents.smallButton(text, 64);
        button.setActionCommand(String.valueOf(index));
        if (listener != null) {
            button.addActionListener(listener);
        }
        return button;
    }

    /** Vuelve a dibujar la lista después de cambiar sus filas. */
    protected void refreshList(MyPanel list) {
        list.revalidate();
        list.repaint();
    }
}