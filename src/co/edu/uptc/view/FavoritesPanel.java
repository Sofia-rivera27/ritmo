package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel encargado de mostrar la sección de canciones favoritas.
 * <p>
 * presenta la
 * información y notifica las acciones del usuario mediante listeners, pero
 * no contiene la lógica para eliminar canciones ni para reproducirlas.
 *
 * @author SofiaValentinaG
 */
public class FavoritesPanel extends MyPanel {

	// Paleta del proyecto
	private static final Color FONDO = new Color(3, 3, 3);
	private static final Color TARJETA = new Color(28, 28, 28);
	private static final Color AMARILLO = new Color(255, 193, 7);
	private static final Color TEXTO = new Color(235, 235, 235);
	private static final Color TEXTO_SUAVE = new Color(150, 150, 150);
	private static final Color TEXTO_OSCURO = new Color(20, 20, 20);
	private static final Color TRANSPARENTE = new Color(0, 0, 0, 0);
	// Extra: borde d elas tarjetas
	private static final Color BORDE_TARJETA = new Color(48, 48, 48);
	private static final Color CAJA_ICONO = new Color(40, 40, 40);

	private static final String PLACEHOLDER = "Buscar por artista, género o canción...";
	private static final int ALTO_FILA = 46;

	private JTextField txtBuscar;
	private JLabel lblTitulo;
	private MyButton btnReproducirTodo;
	private MyPanel lista;

	private final List<MyButton> botonesQuitar = new ArrayList<>();
	private ActionListener quitarListener;

    /**
     * Construye y configura el panel de favoritos.
     * <p>
     * Inicializa los colores, el diseño, la barra superior y el cuerpo
     * principal. Finalmente carga una lista inicial de canciones de prueba.
     */
	public FavoritesPanel() {
		cambiarColor(FONDO);
		cambiarLayout(new BorderLayout());
		setPreferredSize(null);
		setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

		add(crearBarraSuperior(), BorderLayout.NORTH);
		add(crearCuerpo(), BorderLayout.CENTER);

		cargarDatosDePrueba();
	}
	  /**
     * Crea la barra superior que contiene el campo de búsqueda y el avatar.
     *
     * @return panel configurado con los componentes de la barra superior
     */

	private MyPanel crearBarraSuperior() {
		MyPanel barra = new MyPanel();
		barra.cambiarLayout(new BorderLayout(12, 0));
		barra.cambiarColor(FONDO);
		barra.setPreferredSize(new Dimension(0, 62));
		barra.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

		MyPanel buscador = new MyPanel();
		buscador.cambiarLayout(new BorderLayout());
		buscador.cambiarColor(TARJETA);
		buscador.cambiarRedondeo(22);
		buscador.cambiarBorde(BORDE_TARJETA);
		buscador.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

		txtBuscar = new JTextField(PLACEHOLDER);
		txtBuscar.setOpaque(false);
		txtBuscar.setBorder(null);
		txtBuscar.setForeground(TEXTO_SUAVE);
		txtBuscar.setCaretColor(TEXTO);
		txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 13));
		// Comportamiento de placeholder
		txtBuscar.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (txtBuscar.getText().equals(PLACEHOLDER)) {
					txtBuscar.setText("");
					txtBuscar.setForeground(TEXTO);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (txtBuscar.getText().isEmpty()) {
					txtBuscar.setText(PLACEHOLDER);
					txtBuscar.setForeground(TEXTO_SUAVE);
				}
			}
		});
		buscador.add(txtBuscar, BorderLayout.CENTER);

		MyButton avatar = new MyButton("L", AMARILLO, TEXTO_OSCURO, null);
		avatar.cambiarTamano(40, 40);
		avatar.cambiarRedondeo(20); // radio = mitad del tamaño circulo
		avatar.cambiarFuente(Font.BOLD, 14);

		barra.add(buscador, BorderLayout.CENTER);
		barra.add(avatar, BorderLayout.EAST);
		return barra;
	}

	/**
     * Construye el contenido principal de la vista de favoritos.
     * <p>
     * Organiza el encabezado, el subtítulo, la lista de canciones y la
     * tarjeta inferior en una columna vertical.
     *
     * @return panel que contiene el cuerpo de la vista
     */
	private MyPanel crearCuerpo() {
		MyPanel cuerpo = new MyPanel();
		cuerpo.cambiarLayout(new BorderLayout());
		cuerpo.cambiarColor(FONDO);
		cuerpo.setPreferredSize(null);

		// Columna vertical que se pega arriba 
		MyPanel columna = new MyPanel();
		columna.cambiarLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
		columna.cambiarColor(FONDO);
		columna.setPreferredSize(null);

		columna.add(crearEncabezado());

		JLabel subtitulo = new JLabel("Solo las canciones marcadas con ♥. Quitarlas aquí no las borra de la biblioteca.");
		subtitulo.setForeground(TEXTO_SUAVE);
		subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
		subtitulo.setBorder(BorderFactory.createEmptyBorder(2, 0, 14, 0));
		subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		columna.add(subtitulo);

		lista = new MyPanel();
		lista.cambiarLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
		lista.cambiarColor(FONDO);
		lista.setPreferredSize(null);
		lista.setAlignmentX(Component.LEFT_ALIGNMENT);
		columna.add(lista);

		columna.add(Box.createVerticalStrut(10));
		columna.add(crearTarjetaInferior());

		cuerpo.add(columna, BorderLayout.NORTH);
		return cuerpo;
	}
	 /**
     * Crea el encabezado con el título de favoritos y el botón
     * para reproducir todas las canciones.
     *
     * @return panel del encabezado
     */

	private MyPanel crearEncabezado() {
		MyPanel encabezado = new MyPanel();
		encabezado.cambiarLayout(new BorderLayout());
		encabezado.cambiarColor(FONDO);
		encabezado.setPreferredSize(new Dimension(0, 34));
		encabezado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		encabezado.setAlignmentX(Component.LEFT_ALIGNMENT);

		lblTitulo = new JLabel("Favoritos · 0");
		lblTitulo.setForeground(TEXTO);
		lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));

		btnReproducirTodo = new MyButton("Reproducir todo", AMARILLO, TEXTO_OSCURO, null);
		btnReproducirTodo.cambiarTamano(130, 30);
		btnReproducirTodo.cambiarRedondeo(15);
		btnReproducirTodo.cambiarFuente(Font.BOLD, 11);

		encabezado.add(lblTitulo, BorderLayout.WEST);
		encabezado.add(btnReproducirTodo, BorderLayout.EAST);
		return encabezado;
	}

    /**
     * Crea la tarjeta informativa ubicada debajo de la lista de canciones.
     *
     * @return panel con el mensaje para agregar canciones a favoritos
     */

	private MyPanel crearTarjetaInferior() {
		MyPanel tarjeta = new MyPanel();
		tarjeta.cambiarLayout(new BorderLayout());
		tarjeta.cambiarColor(TARJETA);
		tarjeta.cambiarRedondeo(10);
		tarjeta.cambiarBorde(BORDE_TARJETA);
		tarjeta.setPreferredSize(new Dimension(0, 46));
		tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
		tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
		tarjeta.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

		JLabel texto = new JLabel("Para agregar más, ve a Biblioteca y toca ♡ en la canción.");
		texto.setForeground(TEXTO_SUAVE);
		texto.setFont(new Font("SansSerif", Font.PLAIN, 11));
		tarjeta.add(texto, BorderLayout.CENTER);
		return tarjeta;
	}
	   /**
     * Construye una fila visual para representar una canción favorita.
     * <p>
     * El arreglo recibido debe contener, en orden: título, artista,
     * álbum, género y duración.
     *
     * @param cancion datos de la canción que se mostrarán en la fila
     * @param indice posición de la canción en la lista; se utiliza para
     *               mostrar el número y establecer el comando del botón
     * @return panel que representa la canción
     */
	
	private MyPanel crearFila(String[] cancion, int indice) {
		// cancion = {titulo, artista, album, genero, duracion}
		MyPanel fila = new MyPanel();
		fila.cambiarLayout(new GridBagLayout());
		fila.cambiarColor(TARJETA);
		fila.cambiarRedondeo(10);
		fila.setPreferredSize(new Dimension(0, ALTO_FILA));
		fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO_FILA));
		fila.setAlignmentX(Component.LEFT_ALIGNMENT);

		// 1. Numero
		JLabel numero = crearTexto(String.valueOf(indice + 1), TEXTO_SUAVE, Font.PLAIN, 11);
		numero.setPreferredSize(new Dimension(24, 20));
		agregarEnFila(fila, numero, 0, 0);

		// 2. Icono de cancion
		agregarEnFila(fila, crearIcono(), 1, 0);

		// 3. Titulo, artista, album (se reparten el espacio sobrante)
		agregarEnFila(fila, crearTexto(cancion[0], TEXTO, Font.BOLD, 13), 2, 3);
		agregarEnFila(fila, crearTexto(cancion[1], TEXTO_SUAVE, Font.PLAIN, 12), 3, 2);
		agregarEnFila(fila, crearTexto(cancion[2], TEXTO_SUAVE, Font.PLAIN, 12), 4, 2);

		// 4. Etiqueta de genero
		agregarEnFila(fila, crearEtiquetaGenero(cancion[3]), 5, 0);

		// 5. Corazon + duracion
		agregarEnFila(fila, crearTexto("♥", AMARILLO, Font.BOLD, 14), 6, 0);
		agregarEnFila(fila, crearTexto(cancion[4], TEXTO_SUAVE, Font.PLAIN, 11), 7, 0);

		// 6. Boton quitar
		MyButton quitar = new MyButton("Quitar de favoritos", TRANSPARENTE, TEXTO, TEXTO_SUAVE);
		quitar.cambiarTamano(140, 28);
		quitar.cambiarRedondeo(14);
		quitar.cambiarFuente(Font.PLAIN, 11);
		quitar.setActionCommand(String.valueOf(indice)); // el Presenter sabe cual fila fue
		botonesQuitar.add(quitar);
		agregarEnFila(fila, quitar, 8, 0);

		return fila;
	}
	
	/**
     *
     * @param fila panel al que se agregará el componente
     * @param componente elemento visual que se desea agregar
     * @param columna posición horizontal del componente
     * @param peso proporción de espacio horizontal que puede ocupar;
     *             si es mayor que cero, el componente se expande horizontalmente
     */

	private void agregarEnFila(MyPanel fila, Component componente, int columna, double peso) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = columna;
		gbc.gridy = 0;
		gbc.weightx = peso;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = peso > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 8);
		fila.add(componente, gbc);
	}
	   /**
     * Crea una etiqueta de texto con el color, estilo y tamaño indicados.
     *
     * @param texto contenido que mostrará la etiqueta
     * @param color color del texto
     * @param estilo estilo de fuente, por ejemplo 
     * @param tamano tamaño de la fuente en puntos
     * @return etiqueta configurada
     */
	private JLabel crearTexto(String texto, Color color, int estilo, int tamano) {
		JLabel label = new JLabel(texto);
		label.setForeground(color);
		label.setFont(new Font("SansSerif", estilo, tamano));
		return label;
	}

    /**
     * Crea el recuadro decorativo que contiene el símbolo musical.
     *
     * @return panel que representa el icono de una canción
     */

	private MyPanel crearIcono() {
		MyPanel icono = new MyPanel();
		icono.cambiarLayout(new GridBagLayout()); // centra el simbolo
		icono.cambiarColor(CAJA_ICONO);
		icono.cambiarRedondeo(8);
		icono.setPreferredSize(new Dimension(34, 34));
		icono.add(crearTexto("♪", AMARILLO, Font.BOLD, 14));
		return icono;
	}
	

    /**
     * Crea una etiqueta visual para identificar el género musical.
     *
     * @param genero nombre del género que se mostrará
     * @return panel con el género musical
     */

	private MyPanel crearEtiquetaGenero(String genero) {
		MyPanel etiqueta = new MyPanel();
		etiqueta.cambiarLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		etiqueta.cambiarColor(TRANSPARENTE);
		etiqueta.cambiarRedondeo(9);
		etiqueta.cambiarBorde(AMARILLO);
		etiqueta.setPreferredSize(null); // que se ajuste al texto
		etiqueta.add(crearTexto(genero, AMARILLO, Font.PLAIN, 10));
		return etiqueta;
	}



	/** Cada String[] es {titulo, artista, album, genero, duracion}. */

    /**
     * Actualiza la lista visual con las canciones favoritas recibidas.
     * <p>
     * Elimina las filas anteriores, crea una nueva fila por canción,
     * actualiza el contador y registra el listener de eliminación en los
     * botones recién creados, si ya se había establecido.
     *
     * @param canciones lista de canciones; cada elemento debe ser un arreglo
     *                  con título, artista, álbum, género y duración
     *//
	public void mostrarFavoritos(List<String[]> canciones) {
		lista.removeAll();
		botonesQuitar.clear();

		for (int i = 0; i < canciones.size(); i++) {
			lista.add(crearFila(canciones.get(i), i));
			lista.add(Box.createVerticalStrut(6));
		}

		lblTitulo.setText("Favoritos · " + canciones.size());

		// Los botones nuevos tambien deben escuchar
		if (quitarListener != null) {
			for (MyButton boton : botonesQuitar) {
				boton.addActionListener(quitarListener);
			}
		}

		lista.revalidate();
		lista.repaint();
	}

 	/** El actionCommand del boton es el indice de la fila (0, 1, 2...). */

	    /**
	     * Registra el listener que recibirá los eventos de los botones
	     * "Quitar de favoritos".
	     * <p>
	     * El comando de acción de cada botón contiene el índice de su fila.
	     *
	     * @param listener listener que atenderá los eventos de eliminación;
	     *                 puede ser registrado también para los botones actuales
	     */
	public void addRemoveFavoriteListener(ActionListener listener) {
		quitarListener = listener;
		for (MyButton boton : botonesQuitar) {
			boton.addActionListener(listener);
		}
	}

	public void addPlayAllListener(ActionListener listener) {
		btnReproducirTodo.addActionListener(listener);
	}

	// Datos de ejemplo, luego los reemplaza el Presenter con mostrarFavoritos(...)
	private void cargarDatosDePrueba() {
		List<String[]> datos = new ArrayList<>();
		datos.add(new String[] { "Amanecer", "Los Cantores", "Horizonte", "Pop", "3:41" });
		datos.add(new String[] { "Noches de Neón", "Vera Luz", "Ciudad", "Electrónica", "3:42" });
		datos.add(new String[] { "Camino Largo", "Mateo Rivas", "Raíces", "Folk", "3:43" });
		datos.add(new String[] { "Fuego Lento", "Dúo Ámbar", "Brasas", "Rock", "3:44" });
		datos.add(new String[] { "Marea", "Sol y Sal", "Costa", "Indie", "3:45" });
		mostrarFavoritos(datos);
	}
}