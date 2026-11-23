package controller.vista;

import controller.archivos.GestorArchivos;
import controller.conexiones.Sockets;
import exceptions.RobotInvalidoException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Modelo.efectos.Nucleo_activo;
import Modelo.efectos.Quemadura;
import Modelo.efectos.Regeneracion;
import Modelo.Robot.Robot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EnsambleController {
    @FXML private TextField txtNombre, txtAtk, txtDef, txtSpd, txtNrg;
    @FXML private Label lblPuntos, lblEstrategia, lblEstadoEnsamble;
    @FXML private CheckBox chkQuemadura, chkRegeneracion, chkNucleo;
    @FXML private Button btnConfirmar;

    private Sockets conexion;
    private Robot robotCargadoBin = null;
    private List<String> estrategiaCargada = new ArrayList<>();

    public void setConexion(Sockets conexion) {
        this.conexion = conexion;
        File defecto = new File("estrategia.txt");
        if (defecto.exists()) {
            try {
                estrategiaCargada = GestorArchivos.cargarEstrategia(defecto);
                lblEstrategia.setText("estrategia.txt detectado (" + estrategiaCargada.size() + " movs)");
            } catch (Exception ignored) {}
        }
    }

    @FXML
    public void initialize() {
        for (TextField tf : new TextField[]{txtAtk, txtDef, txtSpd, txtNrg}) {
            tf.textProperty().addListener((obs, oldV, newV) -> actualizarSuma());
        }
    }

    private void actualizarSuma() {
        try {
            int suma = Integer.parseInt(txtAtk.getText().trim()) +
                    Integer.parseInt(txtDef.getText().trim()) +
                    Integer.parseInt(txtSpd.getText().trim()) +
                    Integer.parseInt(txtNrg.getText().trim());
            lblPuntos.setText("Puntos asignados: " + suma + " / 200");
            lblPuntos.setStyle(suma == 200 ? "-fx-text-fill: #a6e3a1;" : "-fx-text-fill: #f38ba8;");
        } catch (NumberFormatException e) {
            lblPuntos.setText("Ingresa solo números enteros");
            lblPuntos.setStyle("-fx-text-fill: #f38ba8;");
        }
    }

    @FXML
    private void onCargarRobotBin() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar robot desde robots.bin");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo Binario", "*.bin"));
        File archivo = fc.showOpenDialog(btnConfirmar.getScene().getWindow());
        if (archivo != null) {
            try {
                robotCargadoBin = GestorArchivos.cargarRobotBinario(archivo);
                txtNombre.setText(robotCargadoBin.getNombre());
                txtAtk.setText(String.valueOf(robotCargadoBin.getAtaque()));
                txtDef.setText(String.valueOf(robotCargadoBin.getDefensa()));
                txtSpd.setText(String.valueOf(robotCargadoBin.getVelocidad()));
                txtNrg.setText(String.valueOf(robotCargadoBin.getEnergia()));
                lblEstadoEnsamble.setText("Robot cargado desde " + archivo.getName());
            } catch (Exception e) {
                lblEstadoEnsamble.setText("Error al leer binario: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onSeleccionarEstrategia() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo estrategia.txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de Texto", "*.txt"));
        File archivo = fc.showOpenDialog(btnConfirmar.getScene().getWindow());
        if (archivo != null) {
            try {
                estrategiaCargada = GestorArchivos.cargarEstrategia(archivo);
                lblEstrategia.setText(archivo.getName() + " (" + estrategiaCargada.size() + " movs)");
            } catch (Exception e) {
                lblEstadoEnsamble.setText("Error leyendo estrategia: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onConfirmarEnsamble() {
        try {
            int atk = Integer.parseInt(txtAtk.getText().trim());
            int def = Integer.parseInt(txtDef.getText().trim());
            int spd = Integer.parseInt(txtSpd.getText().trim());
            int nrg = Integer.parseInt(txtNrg.getText().trim());

            Robot miRobot = new Robot(txtNombre.getText().trim(), atk, def, spd, nrg);
            if (chkQuemadura.isSelected()) miRobot.agregarEfecto(new Quemadura());
            if (chkRegeneracion.isSelected()) miRobot.agregarEfecto(new Regeneracion());
            if (chkNucleo.isSelected()) miRobot.agregarEfecto(new Nucleo_activo());

            btnConfirmar.setDisable(true);
            lblEstadoEnsamble.setText("Enviando datos y esperando al rival...");

            new Thread(() -> {
                try {
                    conexion.enviarRobot(miRobot);
                    Robot robotRival = conexion.recibirRobot("Rival");
                    Platform.runLater(() -> irACombate(miRobot, robotRival));
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblEstadoEnsamble.setText("Error de red: " + e.getMessage());
                        btnConfirmar.setDisable(false);
                    });
                }
            }).start();

        } catch (RobotInvalidoException e) {
            lblEstadoEnsamble.setText(e.getMessage());
        } catch (NumberFormatException e) {
            lblEstadoEnsamble.setText("Verifica que todos los campos numéricos sean válidos.");
        }
    }

    private void irACombate(Robot miRobot, Robot robotRival) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/CombateView.fxml"));
            Parent root = loader.load();
            CombateController controller = loader.getController();
            controller.iniciarCombate(conexion, miRobot, robotRival, estrategiaCargada);

            Stage stage = (Stage) btnConfirmar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mecha Duel - Combate");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}