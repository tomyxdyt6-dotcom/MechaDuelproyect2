package controller.vista;

import controller.conexiones.Sockets;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConexionController {
    @FXML private TextField txtIp;
    @FXML private TextField txtPuerto;
    @FXML private Button btnServidor;
    @FXML private Button btnCliente;
    @FXML private Label lblEstado;

    @FXML
    private void onCrearServidor() {
        int puerto = Integer.parseInt(txtPuerto.getText().trim());
        deshabilitarBotones(true);
        lblEstado.setText("Esperando contrincante en puerto " + puerto + "...");

        new Thread(() -> {
            try {
                Sockets conexion = new Sockets();
                conexion.iniciarServidor(puerto);
                Platform.runLater(() -> irAEnsamble(conexion));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblEstado.setText("Error: " + e.getMessage());
                    deshabilitarBotones(false);
                });
            }
        }).start();
    }

    @FXML
    private void onUnirseCliente() {
        String ip = txtIp.getText().trim();
        int puerto = Integer.parseInt(txtPuerto.getText().trim());
        deshabilitarBotones(true);
        lblEstado.setText("Conectando a " + ip + ":" + puerto + "...");

        new Thread(() -> {
            try {
                Sockets conexion = new Sockets();
                conexion.conectarCliente(ip, puerto);
                Platform.runLater(() -> irAEnsamble(conexion));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblEstado.setText("No se pudo conectar: " + e.getMessage());
                    deshabilitarBotones(false);
                });
            }
        }).start();
    }

    private void deshabilitarBotones(boolean bloquear) {
        btnServidor.setDisable(bloquear);
        btnCliente.setDisable(bloquear);
    }

    private void irAEnsamble(Sockets conexion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/EnsambleView.fxml"));
            Parent root = loader.load();
            EnsambleController controller = loader.getController();
            controller.setConexion(conexion);

            Stage stage = (Stage) btnServidor.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mecha Duel - Ensamble");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}