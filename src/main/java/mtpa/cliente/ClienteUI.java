package mtpa.cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Interfaz gráfica del cliente MTPA.
 * Muestra una ventana de login/registro y, tras autenticarse,
 * una ventana de chat con salas y mensajes privados.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class ClienteUI {

    ConexionServidor conex;
    String usuarioActual;
    private String salonActual;
    final Map<String, VentanaPrivada> ventanasPrivadas = new HashMap<>();

    //Componentes de la ventana de login
    private JFrame frameLogin;
    private JTextField campoUsuario;
    private JPasswordField campoClave;
    private JLabel lblEstado;

    //Componentes de la ventana de chat
    JFrame frameChat;
    private JLabel lblSalaActiva;
    private DefaultListModel<String> modeloSalones;
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private JButton btnEnviar;
    private JButton btnSalirSala;
    private JButton btnHistorial;
    private JLabel lblFechaHistorial;
    private LocalDate fechaHistorial;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteUI().mostrarLogin());
    }

    //Construye y muestra la ventana de login/registro
    private void mostrarLogin() {
        frameLogin = new JFrame("MTPA  Login");
        frameLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameLogin.setSize(360, 200);
        frameLogin.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill   = GridBagConstraints.HORIZONTAL;

        campoUsuario = new JTextField(18);
        campoClave   = new JPasswordField(18);
        JButton btnRegistrar = new JButton("Registrarse");
        JButton btnEntrar    = new JButton("Entrar");
        lblEstado = new JLabel(" ");

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Usuario:"), c);
        c.gridx = 1;               panel.add(campoUsuario, c);
        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Clave:"), c);
        c.gridx = 1;               panel.add(campoClave, c);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnEntrar);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        panel.add(panelBotones, c);

        c.gridy = 3;
        panel.add(lblEstado, c);

        frameLogin.add(panel);
        frameLogin.setVisible(true);

        btnRegistrar.addActionListener(e -> accionRegistrar());
        btnEntrar.addActionListener(e -> accionEntrar());
        campoClave.addActionListener(e -> accionEntrar());
    }

    //Abre la conexión TCP la primera vez que se necesita
    private void conectarSiNecesario() throws IOException {
        if (conex == null) {
            conex = new ConexionServidor("localhost", 8080);
            conex.iniciarLector(msg -> SwingUtilities.invokeLater(() -> manejarMensaje(msg)));
        }
    }

    //Envía REGISTER al servidor con el nombre de usuario introducido
    private void accionRegistrar() {
        String usuario = campoUsuario.getText().trim();
        if (usuario.isEmpty()) {
            lblEstado.setText("Introduce un nombre de usuario.");
            return;
        }
        try {
            conectarSiNecesario();
            LogsCliente.info(usuario, "REGISTER", "Solicitando registro");
            conex.enviar("REGISTER|" + usuario);
        } catch (IOException ex) {
            LogsCliente.error(usuario, "REGISTER", "Error al conectar: " + ex.getMessage());
            lblEstado.setText("No se pudo conectar: " + ex.getMessage());
            conex = null;
        }
    }

    //Envía LOGIN al servidor con las credenciales introducidas
    private void accionEntrar() {
        String usuario = campoUsuario.getText().trim();
        String clave   = new String(campoClave.getPassword()).trim();
        if (usuario.isEmpty() || clave.isEmpty()) {
            lblEstado.setText("Rellena usuario y clave.");
            return;
        }
        try {
            conectarSiNecesario();
            LogsCliente.info(usuario, "LOGIN", "Solicitando login");
            conex.enviar("LOGIN|" + usuario + "|" + clave);
        } catch (IOException ex) {
            LogsCliente.error(usuario, "LOGIN", "Error al conectar: " + ex.getMessage());
            lblEstado.setText("No se pudo conectar: " + ex.getMessage());
            conex = null;
        }
    }


    //Punto de entrada para todos los mensajes del servidor, siempre en el EDT
    private void manejarMensaje(String linea) {
        if (frameChat != null && frameChat.isVisible()) {
            procesarMensajeChat(linea);
        } else {
            procesarLoginRespuesta(linea);
        }
    }

    //Procesa las respuestas del servidor durante la fase de login/registro
    private void procesarLoginRespuesta(String linea) {
        if (linea.startsWith("REGISTER_OK|")) {
            String clave = linea.substring("REGISTER_OK|".length());
            campoClave.setText(clave);
            lblEstado.setText("Registrado. Tu clave es " + clave + ". Pulsa Entrar.");

        } else if (linea.startsWith("LOGIN_OK|")) {
            String[] salas = linea.substring("LOGIN_OK|".length()).split(",");
            usuarioActual = campoUsuario.getText().trim();
            LogsCliente.info(usuarioActual, "LOGIN_OK", "Login exitoso");
            conex.iniciarHeartbeat();
            frameLogin.setVisible(false);
            construirVentanaChat(salas);

        } else if (linea.startsWith("ERROR|")) {
            String[] p = linea.split("\\|", 3);
            String detalle = p.length >= 3 ? p[1] + ": " + p[2] : linea;
            LogsCliente.error(campoUsuario.getText().trim(), "ERROR", detalle);
            lblEstado.setText(detalle);
            if (conex != null) { conex.cerrar(); conex = null; }

        } else if (linea.equals("__DESCONECTADO__")) {
            if (conex == null) return;
            lblEstado.setText("Conexión perdida con el servidor.");
            conex = null;
        }
    }

    //Procesa todos los mensajes del servidor durante la fase de chat
    private void procesarMensajeChat(String linea) {
        if (linea.equals("__DESCONECTADO__")) {
            if (conex == null) return;
            conex = null;
            cerrarVentanaChat();
            JOptionPane.showMessageDialog(frameLogin,
                    "Conexión perdida con el servidor.", "Desconectado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Límite 5 para preservar el contenido aunque lleve '|' dentro
        String[] p = linea.split("\\|", 5);

        switch (p[0]) {
            case "JOIN_OK"      -> onJoinOk(p);
            case "ROOM_USERS"   -> onRoomUsers(p);
            case "HISTORY_DATA" -> onHistoryData(p);
            case "PUSH_ROOM"    -> onPushRoom(p);
            case "NOTIF"        -> onNotif(linea);
            case "JOIN_PRIV_OK" -> onJoinPrivOk(p);
            case "PRIV_INVITE"  -> onPrivInvite(p);
            case "PUSH_PRIV"    -> onPushPriv(linea);
            case "PRIV_CLOSED"  -> onPrivClosed(p);
            case "ERROR"        -> onError(linea);
            case "HEARTBEAT_OK" -> {} //silencioso
        }
    }


    private void onJoinOk(String[] p) {
        areaChat.setText("");
        setSalonActual(p[1]);
        LogsCliente.info(usuarioActual, "JOIN_ROOM", "Entró al salón " + p[1]);
        appendChat("[Entraste al salón: " + p[1] + "]");
    }

    private void onRoomUsers(String[] p) {
        appendChat("[Usuarios en sala: " + (p.length > 2 ? p[2] : "") + "]");
    }

    private void onHistoryData(String[] p) {
        //Formato del bloque: ts;usuario;mensaje#ts;usuario;mensaje#
        String bloque = p.length > 2 ? p[2] : "";
        for (String entrada : bloque.split("#")) {
            if (entrada.isEmpty()) continue;
            String[] f = entrada.split(";", 3);
            if (f.length == 3) appendChat("[" + horaCorta(f[0]) + "] " + f[1] + ": " + f[2]);
        }
    }

    private void onPushRoom(String[] p) {
        //PUSH_ROOM|sala|timestamp|usuario|texto
        if (p.length >= 5) appendChat("[" + horaCorta(p[2]) + "] " + p[3] + ": " + p[4]);
    }

    private void onNotif(String linea) {
        //NOTIF|sala|texto libre
        String[] n = linea.split("\\|", 3);
        appendChat("[Sistema] " + (n.length > 2 ? n[2] : ""));
    }

    private void onJoinPrivOk(String[] p) {
        obtenerOCrearVentanaPrivada(p[1]).mostrar();
    }

    private void onPrivInvite(String[] p) {
        VentanaPrivada v = obtenerOCrearVentanaPrivada(p[1]);
        v.appendMensaje("[" + p[1] + " te ha invitado al privado]");
        v.mostrar();
    }

    private void onPushPriv(String linea) {
        //PUSH_PRIV|remitente|texto
        String[] pp = linea.split("\\|", 3);
        if (pp.length < 3) return;
        VentanaPrivada v = obtenerOCrearVentanaPrivada(pp[1]);
        v.appendMensaje(pp[1] + ": " + pp[2]);
        v.setVisible(true);
    }

    private void onPrivClosed(String[] p) {
        VentanaPrivada v = ventanasPrivadas.get(p[1]);
        if (v != null) v.cerrarPorRemoto(p[1]);
    }

    private void onError(String linea) {
        String[] e = linea.split("\\|", 3);
        String detalle = (e.length > 1 ? e[1] : "") + ": " + (e.length > 2 ? e[2] : "");
        LogsCliente.error(usuarioActual, "ERROR", detalle);
        appendChat("[ERROR " + detalle + "]");
    }


    //Ensambla la ventana de chat a partir de los paneles individuales
    private void construirVentanaChat(String[] salas) {
        salonActual = null;
        ventanasPrivadas.clear();

        frameChat = new JFrame("MTPA – Chat (" + usuarioActual + ")");
        frameChat.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frameChat.setSize(720, 520);
        frameChat.setLocationRelativeTo(null);
        frameChat.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { accionDesconectar(); }
        });

        JPanel panelSur = new JPanel(new BorderLayout(0, 3));
        panelSur.add(crearPanelEnvio(),     BorderLayout.NORTH);
        panelSur.add(crearPanelHistorial(), BorderLayout.CENTER);
        panelSur.add(crearPanelPrivado(),   BorderLayout.SOUTH);

        JPanel panelCentro = new JPanel(new BorderLayout(0, 5));
        panelCentro.add(crearAreaChat(), BorderLayout.CENTER);
        panelCentro.add(panelSur,        BorderLayout.SOUTH);

        frameChat.setLayout(new BorderLayout(4, 4));
        frameChat.getRootPane().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        frameChat.add(crearBarraSuperior(),     BorderLayout.NORTH);
        frameChat.add(crearPanelSalones(salas), BorderLayout.WEST);
        frameChat.add(panelCentro,              BorderLayout.CENTER);
        frameChat.setVisible(true);
    }

    //Barra superior: sala activa + botón desconectar
    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barra.add(new JLabel("Sala activa:"));
        lblSalaActiva = new JLabel("-");
        barra.add(lblSalaActiva);
        barra.add(Box.createHorizontalStrut(30));
        JButton btnDesconectar = new JButton("Desconectar");
        btnDesconectar.addActionListener(e -> accionDesconectar());
        barra.add(btnDesconectar);
        return barra;
    }

    //Panel izquierdo: lista de salones + botones unirse/salir
    private JPanel crearPanelSalones(String[] salas) {
        modeloSalones = new DefaultListModel<>();
        for (String s : salas) modeloSalones.addElement(s.trim());
        JList<String> listaSalones = new JList<>(modeloSalones);
        listaSalones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (modeloSalones.getSize() > 0) listaSalones.setSelectedIndex(0);

        JButton btnUnirse = new JButton("Unirse");
        btnSalirSala = new JButton("Salir sala");
        btnSalirSala.setEnabled(false);

        btnUnirse.addActionListener(e -> {
            String sel = listaSalones.getSelectedValue();
            if (sel == null || conex == null) return;
            if (salonActual != null) conex.enviar("LEAVE_ROOM|" + salonActual);
            conex.enviar("JOIN_ROOM|" + sel);
        });
        btnSalirSala.addActionListener(e -> {
            if (salonActual != null && conex != null) {
                conex.enviar("LEAVE_ROOM|" + salonActual);
                setSalonActual(null);
            }
        });

        JPanel botonesIzq = new JPanel(new GridLayout(2, 1, 3, 3));
        botonesIzq.add(btnUnirse);
        botonesIzq.add(btnSalirSala);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Salones"));
        panel.setPreferredSize(new Dimension(145, 0));
        panel.add(new JScrollPane(listaSalones), BorderLayout.CENTER);
        panel.add(botonesIzq, BorderLayout.SOUTH);
        return panel;
    }

    //Área de chat con scroll
    private JScrollPane crearAreaChat() {
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);
        areaChat.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return new JScrollPane(areaChat);
    }

    //Panel de envío de mensaje al salón
    private JPanel crearPanelEnvio() {
        campoMensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);

        btnEnviar.addActionListener(e -> accionEnviarMensaje());
        campoMensaje.addActionListener(e -> accionEnviarMensaje());

        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.add(campoMensaje, BorderLayout.CENTER);
        panel.add(btnEnviar,    BorderLayout.EAST);
        return panel;
    }

    //Panel de carga de historial por fecha
    private JPanel crearPanelHistorial() {
        btnHistorial = new JButton("← Día anterior");
        btnHistorial.setEnabled(false);
        lblFechaHistorial = new JLabel("");

        btnHistorial.addActionListener(e -> {
            if (salonActual == null || conex == null) return;
            appendChat("[Cargando historial de " + fechaHistorial + "...]");
            conex.enviar("GET_HISTORY|" + salonActual + "|" + fechaHistorial);
            fechaHistorial = fechaHistorial.minusDays(1);
            lblFechaHistorial.setText(fechaHistorial.toString());
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panel.add(btnHistorial);
        panel.add(lblFechaHistorial);
        return panel;
    }

    //Panel de apertura de conversación privada
    private JPanel crearPanelPrivado() {
        JTextField campoPrivado = new JTextField(14);
        JButton btnPrivado = new JButton("Abrir privado");

        btnPrivado.addActionListener(e -> {
            String dest = campoPrivado.getText().trim();
            if (!dest.isEmpty() && conex != null) conex.enviar("JOIN_PRIV|" + dest);
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panel.add(new JLabel("Privado con:"));
        panel.add(campoPrivado);
        panel.add(btnPrivado);
        return panel;
    }


    //Envía el mensaje del campo de texto al salón activo y limpia el campo
    private void accionEnviarMensaje() {
        if (salonActual == null || conex == null) return;
        String texto = campoMensaje.getText().trim();
        if (texto.isEmpty()) return;
        LogsCliente.info(usuarioActual, "SEND_ROOM", "Mensaje en salón " + salonActual);
        conex.enviar("SEND_ROOM|" + salonActual + "|" + texto);
        campoMensaje.setText("");
    }

    //Actualiza el salón activo y habilita o deshabilita los controles que dependen de él
    private void setSalonActual(String salon) {
        salonActual = salon;
        lblSalaActiva.setText(salon != null ? salon : "-");
        btnEnviar.setEnabled(salon != null);
        btnSalirSala.setEnabled(salon != null);
        if (salon != null) {
            fechaHistorial = LocalDate.now().minusDays(1);
            btnHistorial.setEnabled(true);
            lblFechaHistorial.setText(fechaHistorial.toString());
        } else {
            btnHistorial.setEnabled(false);
            lblFechaHistorial.setText("");
        }
    }

    //Añade una línea al área de chat y hace scroll hasta el final
    private void appendChat(String texto) {
        areaChat.append(texto + "\n");
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }

    //Cierra la conexión y vuelve a la ventana de login
    private void accionDesconectar() {
        LogsCliente.info(usuarioActual != null ? usuarioActual : "CLIENTE", "DESCONECTAR", "El usuario cerró la sesión");
        if (conex != null) { conex.cerrar(); conex = null; }
        cerrarVentanaChat();
        frameLogin.setVisible(true);
    }

    //Cierra y limpia la ventana de chat y todos los privados abiertos
    private void cerrarVentanaChat() {
        ventanasPrivadas.values().forEach(Window::dispose);
        ventanasPrivadas.clear();
        if (frameChat != null) { frameChat.dispose(); frameChat = null; }
        usuarioActual = null;
        salonActual   = null;
        campoClave.setText("");
        lblEstado.setText(" ");
    }

    //Extrae HH:mm:ss de un timestamp con formato LocalDateTime (2026-06-08T14:30:45.123)
    private static String horaCorta(String timestamp) {
        if (timestamp == null) return "";
        int t = timestamp.indexOf('T');
        if (t < 0) return timestamp;
        String hora = timestamp.substring(t + 1);
        return hora.length() >= 8 ? hora.substring(0, 8) : hora;
    }

    //Obtiene la ventana privada del mapa o la crea si todavía no existe
    private VentanaPrivada obtenerOCrearVentanaPrivada(String usuarioRemoto) {
        return ventanasPrivadas.computeIfAbsent(usuarioRemoto, k -> new VentanaPrivada(this, k));
    }
}
