
package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

/**
 * Aplica el tema claro u oscuro al árbol de componentes Swing existente.
 */
public final class ThemeManager {

	private ThemeManager() {
	}

	public static void apply(Component component, boolean light) {

		if (component == null) {
			return;
		}

		if (component instanceof MyPanel) {

			MyPanel panel = (MyPanel) component;

			panel.setBackgroundColor(map(panel.getBackground(), light));

		} else if (component instanceof MyButton) {

			((MyButton) component).applyTheme(light);

		} else if (component instanceof JLabel) {

			JLabel label = (JLabel) component;

			label.setForeground(mapText(label.getForeground(), light));

		} else if (component instanceof JTextComponent) {

			JTextComponent field = (JTextComponent) component;

			field.setBackground(light ? Color.WHITE : Palette.FIELD);

			field.setForeground(light ? new Color(25, 25, 25) : Palette.TEXT);

			field.setCaretColor(light ? Color.BLACK : Palette.TEXT);

		} else if (component instanceof Container) {

			component.setBackground(map(component.getBackground(), light));
		}

		if (component instanceof Container) {

			for (Component child : ((Container) component).getComponents()) {

				apply(child, light);
			}
		}
	}

	private static Color map(Color color, boolean light) {

		if (color == null) {
			return null;
		}

		if (light) {

			if (color.equals(new Color(3, 3, 3)))
				return new Color(245, 245, 245);

			if (color.equals(new Color(9, 9, 9)))
				return Color.WHITE;

			if (color.equals(new Color(18, 18, 18)))
				return new Color(248, 248, 248);

			if (color.equals(new Color(28, 28, 28)))
				return Color.WHITE;

			if (color.equals(new Color(48, 48, 48)))
				return new Color(220, 220, 220);

			if (color.equals(new Color(38, 38, 38)))
				return new Color(238, 238, 238);

			if (color.equals(new Color(52, 52, 52)))
				return new Color(210, 210, 210);

		} else {

			if (color.equals(new Color(245, 245, 245)))
				return new Color(3, 3, 3);

			if (color.equals(Color.WHITE))
				return new Color(9, 9, 9);

			if (color.equals(new Color(248, 248, 248)))
				return new Color(18, 18, 18);

			if (color.equals(new Color(255, 255, 255)))
				return new Color(28, 28, 28);

			if (color.equals(new Color(220, 220, 220)))
				return new Color(48, 48, 48);

			if (color.equals(new Color(238, 238, 238)))
				return new Color(38, 38, 38);

			if (color.equals(new Color(210, 210, 210)))
				return new Color(52, 52, 52);
		}

		return color;
	}

	private static Color mapText(Color color, boolean light) {

		if (color == null) {
			return null;
		}

		if (light && (color.equals(new Color(235, 235, 235)) || color.equals(new Color(150, 150, 150)))) {

			return new Color(35, 35, 35);
		}

		if (!light && (color.equals(new Color(35, 35, 35)) || color.equals(new Color(90, 90, 90)))) {

			return new Color(235, 235, 235);
		}

		return color;
	}
}
