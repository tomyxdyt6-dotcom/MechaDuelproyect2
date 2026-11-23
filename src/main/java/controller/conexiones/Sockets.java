package controller.conexiones;

import exceptions.RobotInvalidoException;
import Modelo.efectos.Nucleo_activo;
import Modelo.efectos.Quemadura;
import Modelo.efectos.Regeneracion;
import Modelo.Robot.Robot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Sockets {
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private boolean esServidor;

    public void iniciarServidor(int puerto) throws IOException {
        this.esServidor = true;
        this.serverSocket = new ServerSocket(puerto);
        this.socket = serverSocket.accept();
        configurarFlujos();
    }

    public void conectarCliente(String ip, int puerto) throws IOException {
        this.esServidor = false;
        this.socket = new Socket(ip, puerto);
        configurarFlujos();
    }

    private void configurarFlujos() throws IOException {
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    public boolean isEsServidor() {
        return esServidor;
    }

    public void enviarRobot(Robot r) {
        List<String> effs = new ArrayList<>();
        for (int i = 0; i < r.getCantidadEfectos(); i++) {
            String nombreClase = r.getEfectos()[i].getClass().getSimpleName();
            if (nombreClase.equalsIgnoreCase("Quemadura")) effs.add("Q");
            else if (nombreClase.equalsIgnoreCase("Regeneracion")) effs.add("R");
            else if (nombreClase.equalsIgnoreCase("Nucleo_activo")) effs.add("N");
        }

        String mensaje = "{ HP: " + (int) r.getVidaActual() +
                ", ATK:" + r.getAtaque() +
                ", DEF:" + r.getDefensa() +
                ", SPD:" + r.getVelocidad() +
                ", NRG:" + r.getEnergia() +
                ", EFF: [ " + String.join(", ", effs) + "] }";
        salida.println(mensaje);
    }

    public Robot recibirRobot(String nombreRival) throws IOException, RobotInvalidoException {
        String trama = entrada.readLine();
        if (trama == null) throw new IOException("Conexión cerrada por el rival.");

        String limpio = trama.replace("{", "").replace("}", "").trim();
        int atk = extraerEntero(limpio, "ATK:");
        int def = extraerEntero(limpio, "DEF:");
        int spd = extraerEntero(limpio, "SPD:");
        int nrg = extraerEntero(limpio, "NRG:");

        Robot rival = new Robot(nombreRival, atk, def, spd, nrg);

        int idxEff = limpio.indexOf("EFF:");
        if (idxEff != -1) {
            String parteEff = limpio.substring(idxEff);
            if (parteEff.contains("Q")) rival.agregarEfecto(new Quemadura());
            if (parteEff.contains("R")) rival.agregarEfecto(new Regeneracion());
            if (parteEff.contains("N")) rival.agregarEfecto(new Nucleo_activo());
        }
        return rival;
    }

    private int extraerEntero(String texto, String clave) {
        int inicio = texto.indexOf(clave) + clave.length();
        int fin = texto.indexOf(",", inicio);
        if (fin == -1) fin = texto.length();
        return Integer.parseInt(texto.substring(inicio, fin).trim());
    }

    public void enviarMensaje(String msg) {
        if (salida != null) salida.println(msg);
    }

    public String recibirMensaje() throws IOException {
        return entrada != null ? entrada.readLine() : null;
    }

    public void cerrar() {
        try {
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }
}