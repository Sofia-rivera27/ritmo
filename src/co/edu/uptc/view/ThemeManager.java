package co.edu.uptc.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

/**
 * Aplica el tema claro u oscuro al árbol de componentes Swing existente.
 */
public final class ThemeManager {

	/** Tema activo; lo actualiza ViewFrame cada vez que se cambia el tema. */
	private static boolean lightTheme = false;

	private ThemeManager() {
	}

	/**
	 * Guarda cuál es el tema activo, para que refresh sepa qué colores usar.
	 *
	 * @param light true para el tema claro
	 */
	public static void setCurrentTheme(boolean light) {
		lightTheme = light;
	}

	public static boolean isLightTheme() {
		return lightTheme;
	}

	/**
	 * Vuelve a aplicar el tema activo a un componente y a todo lo que tiene
	 * dentro. Se usa cuando una pantalla crea o repinta componentes después
	 * de haber cambiado el tema (por ejemplo, filas de resultados nuevas o un
	 * botón que se acaba de seleccionar), porque esos componentes se pintan
	 * con los colores del tema oscuro.
	 *
	 * @param component componente que se desea actualizar
	 */
	public static void refresh(Component component) {
		apply(component, lightTheme);
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

			// Se convierte el color actual en vez de forzar uno solo, así el
			// texto de ayuda (gris) se sigue distinguiendo de lo escrito
			field.setForeground(mapText(field.getForeground(), light));

			field.setCaretColor(light ? Color.BLACK : Palette.TEXT);

		} else if (component instanceof JList) {

			// Las listas (por ejemplo la de la cola) necesitan cambiar también
			// el color del texto, o quedaría texto blanco sobre fondo blanco
			JList<?> list = (JList<?>) component;

			list.setBackground(map(list.getBackground(), light));

			list.setForeground(mapText(list.getForeground(), light));

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

			// Tarjetas y barra de controles. Usan un blanco propio (252) para
			// que al volver al tema oscuro recuperen su gris y no queden negras.
			if (color.equals(new Color(28, 28, 28)))
				return new Color(252, 252, 252);

			if (color.equals(new Color(48, 48, 48)))
				return new Color(220, 220, 220);

			if (color.equals(new Color(38, 38, 38)))
				return new Color(238, 238, 238);

			if (color.equals(new Color(52, 52, 52)))
				return new Color(210, 210, 210);

			// Colores propios de la pantalla Home (fondo y círculos)
			if (color.equals(new Color(17, 17, 17)))
				return new Color(242, 242, 242);

			if (color.equals(new Color(41, 41, 41)))
				return new Color(230, 230, 230);

		} else {

			if (color.equals(new Color(245, 245, 245)))
				return new Color(3, 3, 3);

			if (color.equals(Color.WHITE))
				return new Color(9, 9, 9);

			if (color.equals(new Color(248, 248, 248)))
				return new Color(18, 18, 18);

			if (color.equals(new Color(252, 252, 252)))
				return new Color(28, 28, 28);

			if (color.equals(new Color(220, 220, 220)))
				return new Color(48, 48, 48);

			if (color.equals(new Color(238, 238, 238)))
				return new Color(38, 38, 38);

			if (color.equals(new Color(210, 210, 210)))
				return new Color(52, 52, 52);

			if (color.equals(new Color(242, 242, 242)))
				return new Color(17, 17, 17);

			if (color.equals(new Color(230, 230, 230)))
				return new Color(41, 41, 41);
		}

		return color;
	}

	private static Color mapText(Color color, boolean light) {

		if (color == null) {
			return null;
		}

		// Texto principal: claro en el tema oscuro, oscuro en el claro
		if (light && color.equals(DARK_TEXT)) {
			return LIGHT_TEXT;
		}

		if (!light && color.equals(LIGHT_TEXT)) {
			return DARK_TEXT;
		}

		// Texto secundario (gris): tiene su propio color en cada tema, así
		// al volver al tema oscuro sigue siendo gris y no queda blanco
		if (light && color.equals(DARK_MUTED_TEXT)) {
			return LIGHT_MUTED_TEXT;
		}

		if (!light && color.equals(LIGHT_MUTED_TEXT)) {
			return DARK_MUTED_TEXT;
		}

		return color;
	}

	private static final Color DARK_TEXT = new Color(235, 235, 235);
	private static final Color LIGHT_TEXT = new Color(35, 35, 35);
	private static final Color DARK_MUTED_TEXT = new Color(150, 150, 150);
	private static final Color LIGHT_MUTED_TEXT = new Color(105, 105, 105);
}