package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;

/**
 * Tarjeta de inicio de sesion / registro.
 * SOLO VISTA: aqui no hay logica, los botones todavia no hacen nada.
 * Mas adelante el Presenter se encargara de conectarlos usando los getters.
 */
public class PanelRegister extends MyPanel {

    // Colores
    private static final Color TARJETA = new Color(28, 28, 28);
    private static final Color BORDE_TARJETA = new Color(48, 48, 48);
    private static final Color CAMPO = new Color(38, 38, 38);
    private static final Color AMARILLO = new Color(255, 193, 7);
    private static final Color TEXTO = new Color(235, 235, 235);
    private static final Color TEXTO_SUAVE = new Color(150, 150, 150);
    private static final Color TEXTO_OSCURO = new Color(20, 20, 20);
    private static final Color TRANSPARENTE = new Color(0, 0, 0, 0);
    private static final int ANCHO = 274;

    // Componentes
    private JLabel titulo;
    private JLabel subtitulo;
    private MyTextField campoUsuario;
    private MyTextField campoClave;
    private MyTextField campoConfirmar;
    private MyPanel filaRoles;
    private MyButton btnUsuario;
    private MyButton btnAdmin;
    private MyPanel cajaError; // caja roja redondeada
    private JLabel textoError; // texto dentro de la caja
    private MyButton btnPrincipal;
    private MyButton btnSecundario;

    // Espacios que se ocultan junto con su componente
    private JComponent espacioConfirmar;
    private JComponent espacioRoles;
    private JComponent espacioError;

    public PanelRegister() {

        cambiarColor(TARJETA);
        cambiarRedondeo(12);
        cambiarBorde(BORDE_TARJETA);
        cambiarLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(26, 27, 26, 27));
        setPreferredSize(null);
        crearComponentes();
        armarVista();

        seleccionarRol(false);
        cambiarModo(false);
    }

    // ---------------------------------------------------------------
    // Creacion de componentes
    // ---------------------------------------------------------------
    private void crearComponentes() {
        titulo = new JLabel("Ritmo");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(AMARILLO);

        subtitulo = new JLabel("Inicia sesión para continuar");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitulo.setForeground(TEXTO_SUAVE);

        campoUsuario = new MyTextField("Usuario", false);
        campoUsuario.cambiarTamano(ANCHO, 33);

        campoClave = new MyTextField("Contraseña", true);
        campoClave.cambiarTamano(ANCHO, 33);

        campoConfirmar = new MyTextField("Confirmar contraseña", true);
        campoConfirmar.cambiarTamano(ANCHO, 33);

        // Botones tipo "chip" para elegir el rol
        btnUsuario = new MyButton("Usuario", AMARILLO, TEXTO_OSCURO, null);
        btnUsuario.cambiarTamano(67, 28);
        btnUsuario.cambiarRedondeo(14);

        btnAdmin = new MyButton("Administrador", CAMPO, TEXTO, null);
        btnAdmin.cambiarTamano(97, 28);
        btnAdmin.cambiarRedondeo(14);

        filaRoles = new MyPanel();
        filaRoles.cambiarColor(TRANSPARENTE);
        filaRoles.cambiarLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaRoles.cambiarTamaño(ANCHO, 28);
        filaRoles.setMaximumSize(new Dimension(ANCHO, 28));
        filaRoles.agregarComponente(btnUsuario);
        filaRoles.agregarComponente(Box.createHorizontalStrut(7));
        filaRoles.agregarComponente(btnAdmin);

        textoError = new JLabel();
        textoError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        textoError.setForeground(new Color(255, 170, 170));
        textoError.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        cajaError = new MyPanel();
        cajaError.cambiarColor(new Color(62, 26, 26));
        cajaError.cambiarBorde(new Color(225, 90, 90));
        cajaError.cambiarRedondeo(4);
        cajaError.cambiarLayout(new BorderLayout());
        cajaError.cambiarTamaño(ANCHO, 28);
        cajaError.setMaximumSize(new Dimension(ANCHO, 28));
        cajaError.agregarComponente(textoError);

        // Botones principales
        btnPrincipal = new MyButton("Ingresar", AMARILLO, TEXTO_OSCURO, null);
        btnPrincipal.cambiarFuente(Font.BOLD, 12);
        btnPrincipal.cambiarTamano(ANCHO, 30);

        btnSecundario = new MyButton("Crear cuenta", TRANSPARENTE, TEXTO, new Color(85, 85, 85));
        btnSecundario.cambiarTamano(ANCHO, 30);

        espacioConfirmar = espacio(10);
        espacioRoles = espacio(10);
        espacioError = espacio(10);
    }

    // Agrega todo en orden, de arriba hacia abajo
    private void armarVista() {
        agregar(titulo);
        agregar(espacio(4));
        agregar(subtitulo);
        agregar(espacio(14));
        agregar(campoUsuario);
        agregar(espacio(10));
        agregar(campoClave);
        agregar(espacio(10));
        agregar(campoConfirmar);
        agregar(espacioConfirmar);
        agregar(filaRoles);
        agregar(espacioRoles);
        agregar(cajaError);
        agregar(espacioError);
        agregar(btnPrincipal);
        agregar(espacio(10));
        agregar(btnSecundario);
    }

    private void agregar(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(c);
    }

    private JComponent espacio(int alto) {
        JComponent e = (JComponent) Box.createVerticalStrut(alto);
        e.setAlignmentX(Component.LEFT_ALIGNMENT);
        return e;
    }

    // ---------------------------------------------------------------
    // Cambios visuales (el Presenter los llamara cuando los conectemos)
    // ---------------------------------------------------------------

    // Pinta de amarillo el chip del rol elegido (admin o usuario)
    public void seleccionarRol(boolean admin) {
        if (admin) {
            btnAdmin.cambiarColores(AMARILLO, TEXTO_OSCURO, null);
            btnAdmin.cambiarFuente(Font.BOLD, 12);
            btnUsuario.cambiarColores(CAMPO, TEXTO, null);
            btnUsuario.cambiarFuente(Font.PLAIN, 12);
        } else {
            btnUsuario.cambiarColores(AMARILLO, TEXTO_OSCURO, null);
            btnUsuario.cambiarFuente(Font.BOLD, 12);
            btnAdmin.cambiarColores(CAMPO, TEXTO, null);
            btnAdmin.cambiarFuente(Font.PLAIN, 12);
        }
    }

    // Muestra la tarjeta como "registro" (true) o como "inicio de sesion" (false)
    public void cambiarModo(boolean registro) {
        campoUsuario.limpiar();
        campoClave.limpiar();
        campoConfirmar.limpiar();
        ocultarMensaje();

        if (registro) {
            subtitulo.setText("Crea tu cuenta para continuar");
            btnPrincipal.setText("Registrarse");
            btnSecundario.setText("Ya tengo cuenta");

            campoConfirmar.setVisible(true);
            espacioConfirmar.setVisible(true);
            filaRoles.setVisible(false);
            espacioRoles.setVisible(false);
        } else {
            subtitulo.setText("Inicia sesión para continuar");
            btnPrincipal.setText("Ingresar");
            btnSecundario.setText("Crear cuenta");

            campoConfirmar.setVisible(false);
            espacioConfirmar.setVisible(false);
            filaRoles.setVisible(true);
            espacioRoles.setVisible(true);
        }

        revalidate();
        repaint();
    }

    public void mostrarMensaje(String texto) {
        textoError.setText(texto);
        cajaError.setVisible(true);
        espacioError.setVisible(true);
        revalidate();
        repaint();
    }

    public void ocultarMensaje() {
        cajaError.setVisible(false);
        espacioError.setVisible(false);
        revalidate();
        repaint();
    }

    // ---------------------------------------------------------------
    // Getters para que el Presenter pueda conectar los botones despues
    // ---------------------------------------------------------------
    public MyTextField getCampoUsuario() {
        return campoUsuario;
    }

    public MyTextField getCampoClave() {
        return campoClave;
    }

    public MyTextField getCampoConfirmar() {
        return campoConfirmar;
    }

    public MyButton getBtnUsuario() {
        return btnUsuario;
    }

    public MyButton getBtnAdmin() {
        return btnAdmin;
    }

    public MyButton getBtnPrincipal() {
        return btnPrincipal;
    }

    public MyButton getBtnSecundario() {
        return btnSecundario;
    }
}
