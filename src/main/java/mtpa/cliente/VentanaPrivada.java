package mtpa.cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Ventana de conversación privada con un usuario concreto.
 * Se crea al abrir un privado o al recibir una invitación.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
class VentanaPrivada extends JDialog {

    private final ClienteUI ui;
    private final String usuarioRemoto;
    private final JTextArea area;
    private final JTextField campo;
    private final JButton btnEnviarPriv;

    VentanaPrivada(ClienteUI ui, String usuarioRemoto) {
        super(ui.frameChat, "Privado: " + usuarioRemoto, false);
        this.ui = ui;
        this.usuarioRemoto = usuarioRemoto;
        setSize(380, 280);
        setLocationRelativeTo(ui.frameChat);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        campo = new JTextField();
        btnEnviarPriv = new JButton("Enviar");
        JButton btnCerrar = new JButton("Cerrar");

        JPanel panelEnvio = new JPanel(new BorderLayout(4, 0));
        panelEnvio.add(campo, BorderLayout.CENTER);
        panelEnvio.add(btnEnviarPriv, BorderLayout.EAST);

        JPanel panelSur = new JPanel(new BorderLayout(0, 3));
        panelSur.add(panelEnvio, BorderLayout.NORTH);
        panelSur.add(btnCerrar,  BorderLayout.SOUTH);

        setLayout(new BorderLayout(0, 5));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);

        btnEnviarPriv.addActionListener(e -> enviarPrivado());
        campo.addActionListener(e -> enviarPrivado());
        btnCerrar.addActionListener(e -> cerrarPropio());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cerrarPropio(); }
        });
    }

    //Hace visible la ventana y la trae al frente
    void mostrar() {
        setVisible(true);
        toFront();
    }

    //Añade un mensaje al área de texto y hace scroll hasta el final
    void appendMensaje(String texto) {
        area.append(texto + "\n");
        area.setCaretPosition(area.getDocument().getLength());
    }

    //El otro extremo cerró la conversación; se deshabilita el envío
    void cerrarPorRemoto(String quien) {
        appendMensaje("[Conversación cerrada por " + quien + "]");
        campo.setEnabled(false);
        btnEnviarPriv.setEnabled(false);
    }

    //Envía SEND_PRIV al servidor y muestra el mensaje localmente
    private void enviarPrivado() {
        String texto = campo.getText().trim();
        if (texto.isEmpty() || ui.conex == null) return;
        LogsCliente.info(ui.usuarioActual, "SEND_PRIV", "Mensaje privado a " + usuarioRemoto);
        ui.conex.enviar("SEND_PRIV|" + usuarioRemoto + "|" + texto);
        appendMensaje("Yo: " + texto);
        campo.setText("");
    }

    //Envía LEAVE_PRIV al servidor, elimina la ventana del mapa y la cierra
    private void cerrarPropio() {
        LogsCliente.info(ui.usuarioActual, "LEAVE_PRIV", "Cerró conversación privada con " + usuarioRemoto);
        if (ui.conex != null) ui.conex.enviar("LEAVE_PRIV|" + usuarioRemoto);
        ui.ventanasPrivadas.remove(usuarioRemoto);
        dispose();
    }
}
