package controller.vista;

import controller.archivos.GestorArchivos;
import controller.conexiones.Sockets;
import controller.control_ataque.Reglas;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import Modelo.movimiento.*;
import Modelo.Robot.Robot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CombateController {
    @FXML private Label lblTurno, lblNombrePropio, lblHpPropio, lblNrgPropio, lblStatsPropio, lblEfectosPropio;
    @FXML private Label lblNombreRival, lblHpRival, lblNrgRival, lblStatsRival, lblEfectosRival;
    @FXML private ProgressBar barHpPropio, barHpRival;
    @FXML private ListView<String> listaRegistro;
    @FXML private HBox panelBotones;
    @FXML private Pane arenaPane;

    private Sockets conexion;
    private Robot miRobot;
    private Robot robotRival;
    private Reglas reglas;
    private List<String> estrategia;
    private int indiceEstrategia = 0;
    private List<String> misMovimientosEjecutados = new ArrayList<>();

    private Group mechaPropioVisual;
    private Group mechaRivalVisual;

    @FXML
    public void initialize() {
        construirEscenarioMechas();
    }

    private void construirEscenarioMechas() {
        // Suelo / plataforma de la arena
        Rectangle suelo = new Rectangle(0, 185, 600, 35);
        suelo.widthProperty().bind(arenaPane.widthProperty());
        suelo.setFill(Color.web("#181825"));

        Line lineaNeon = new Line(0, 185, 600, 185);
        lineaNeon.endXProperty().bind(arenaPane.widthProperty());
        lineaNeon.setStroke(Color.web("#89b4fa"));
        lineaNeon.setStrokeWidth(2);

        // Crear ambos robots (Izquierda = Propio en verde/cian, Derecha = Rival en rojo/naranja)
        mechaPropioVisual = crearSpriteMecha(Color.web("#a6e3a1"), Color.web("#89b4fa"), false);
        mechaPropioVisual.setLayoutX(65);
        mechaPropioVisual.setLayoutY(65);

        mechaRivalVisual = crearSpriteMecha(Color.web("#f38ba8"), Color.web("#fab387"), true);
        mechaRivalVisual.setLayoutX(295);
        mechaRivalVisual.setLayoutY(65);

        arenaPane.getChildren().addAll(suelo, lineaNeon, mechaPropioVisual, mechaRivalVisual);

        // Animación continua de "respiración" / flotación
        iniciarAnimacionIdle(mechaPropioVisual, 0);
        iniciarAnimacionIdle(mechaRivalVisual, 400);
    }

    private Group crearSpriteMecha(Color colorPrincipal, Color colorVisor, boolean mirarIzquierda) {
        Group robot = new Group();

        // Antena
        Line antena = new Line(35, 0, 35, 12);
        antena.setStroke(colorVisor);
        antena.setStrokeWidth(3);
        Circle puntaAntena = new Circle(35, 0, 3.5, colorVisor);

        // Cabeza y visor
        Rectangle cabeza = new Rectangle(20, 12, 30, 22);
        cabeza.setArcWidth(8);
        cabeza.setArcHeight(8);
        cabeza.setFill(Color.web("#313244"));
        cabeza.setStroke(colorPrincipal);
        cabeza.setStrokeWidth(2);

        Rectangle visor = new Rectangle(mirarIzquierda ? 22 : 34, 18, 14, 7);
        visor.setArcWidth(4);
        visor.setArcHeight(4);
        visor.setFill(colorVisor);
        visor.setEffect(new DropShadow(10, colorVisor));

        // Torso blindado + núcleo central
        Rectangle torso = new Rectangle(12, 36, 46, 44);
        torso.setArcWidth(10);
        torso.setArcHeight(10);
        torso.setFill(colorPrincipal);
        torso.setStroke(Color.web("#11111b"));
        torso.setStrokeWidth(2);

        Circle nucleo = new Circle(35, 56, 8, colorVisor);
        nucleo.setEffect(new DropShadow(12, colorVisor));

        // Brazo / Cañón frontal
        Rectangle canon = new Rectangle(mirarIzquierda ? -6 : 52, 44, 24, 12);
        canon.setArcWidth(5);
        canon.setArcHeight(5);
        canon.setFill(Color.web("#cdd6f4"));

        // Piernas mecánicas
        Rectangle piernaIzq = new Rectangle(16, 80, 14, 38);
        piernaIzq.setArcWidth(4);
        piernaIzq.setArcHeight(4);
        piernaIzq.setFill(Color.web("#45475a"));

        Rectangle piernaDer = new Rectangle(40, 80, 14, 38);
        piernaDer.setArcWidth(4);
        piernaDer.setArcHeight(4);
        piernaDer.setFill(Color.web("#45475a"));

        robot.getChildren().addAll(antena, puntaAntena, piernaIzq, piernaDer, torso, nucleo, cabeza, visor, canon);
        return robot;
    }

    private void iniciarAnimacionIdle(Group mecha, double delayMs) {
        TranslateTransition idle = new TranslateTransition(Duration.millis(900), mecha);
        idle.setFromY(0);
        idle.setToY(-6);
        idle.setCycleCount(Animation.INDEFINITE);
        idle.setAutoReverse(true);
        idle.setDelay(Duration.millis(delayMs));
        idle.play();
    }

    private void reproducirAnimacionAccion(boolean esPropio, String accion, String resultado) {
        Group atacante = esPropio ? mechaPropioVisual : mechaRivalVisual;
        Group defensor = esPropio ? mechaRivalVisual : mechaPropioVisual;
        double dir = esPropio ? 1.0 : -1.0;

        if (accion.equals("ATTACK") || accion.equals("SPECIAL")) {
            // 1. Impulso hacia adelante del atacante
            TranslateTransition embestida = new TranslateTransition(Duration.millis(160), atacante);
            embestida.setByX(35 * dir);
            embestida.setAutoReverse(true);
            embestida.setCycleCount(2);

            // 2. Proyectil / Rayo láser cruzando la arena
            double startX = atacante.getLayoutX() + (esPropio ? 75 : -5);
            double endX = defensor.getLayoutX() + 35;
            double y = 112;

            Line laser = new Line(startX, y, startX + (25 * dir), y);
            boolean esEspecial = accion.equals("SPECIAL");
            Color colorRayo = esEspecial ? Color.web("#fab387") : Color.web("#89b4fa");
            laser.setStroke(colorRayo);
            laser.setStrokeWidth(esEspecial ? 8 : 4);
            laser.setEffect(new DropShadow(15, colorRayo));
            arenaPane.getChildren().add(laser);

            TranslateTransition vueloLaser = new TranslateTransition(Duration.millis(220), laser);
            vueloLaser.setToX(endX - startX);
            vueloLaser.setOnFinished(e -> {
                arenaPane.getChildren().remove(laser);
                if (resultado.equals("HIT")) {
                    // Temblor y destello rojo de impacto en el defensor
                    defensor.setEffect(new DropShadow(25, Color.RED));
                    TranslateTransition shake = new TranslateTransition(Duration.millis(55), defensor);
                    shake.setByX(12 * dir);
                    shake.setAutoReverse(true);
                    shake.setCycleCount(4);
                    shake.setOnFinished(ev -> defensor.setEffect(null));
                    shake.play();
                } else if (resultado.equals("DODGE")) {
                    // Salto hacia atrás de esquiva
                    TranslateTransition dodge = new TranslateTransition(Duration.millis(180), defensor);
                    dodge.setByX(35 * dir);
                    dodge.setByY(-25);
                    dodge.setAutoReverse(true);
                    dodge.setCycleCount(2);
                    dodge.play();
                }
            });

            embestida.play();
            vueloLaser.play();

        } else if (accion.equals("DODGE")) {
            TranslateTransition posturaEsquiva = new TranslateTransition(Duration.millis(200), atacante);
            posturaEsquiva.setByX(-20 * dir);
            posturaEsquiva.setByY(-15);
            posturaEsquiva.setAutoReverse(true);
            posturaEsquiva.setCycleCount(2);
            posturaEsquiva.play();

        } else if (accion.equals("HEAL") || accion.equals("CHARGE")) {
            Color auraColor = accion.equals("HEAL") ? Color.web("#a6e3a1") : Color.web("#f9e2af");
            atacante.setEffect(new DropShadow(28, auraColor));
            ScaleTransition pulso = new ScaleTransition(Duration.millis(220), atacante);
            pulso.setToX(1.12);
            pulso.setToY(1.12);
            pulso.setAutoReverse(true);
            pulso.setCycleCount(2);
            pulso.setOnFinished(e -> atacante.setEffect(null));
            pulso.play();
        }
    }

    public void iniciarCombate(Sockets conexion, Robot miRobot, Robot robotRival, List<String> estrategia) {
        this.conexion = conexion;
        this.miRobot = miRobot;
        this.robotRival = robotRival;
        this.estrategia = estrategia;

        if (conexion.isEsServidor()) {
            this.reglas = new Reglas(miRobot, robotRival);
        } else {
            this.reglas = new Reglas(robotRival, miRobot);
        }

        lblNombrePropio.setText(miRobot.getNombre() + " (TÚ)");
        lblNombreRival.setText(robotRival.getNombre() + " (RIVAL)");
        actualizarUI();

        new Thread(this::bucleDeJuego).start();
    }

    private void bucleDeJuego() {
        try {
            while (!reglas.terminado()) {
                Robot turnoDe = reglas.iniciarTurno();
                Platform.runLater(this::actualizarUI);

                if (reglas.terminado()) break;

                if (turnoDe == miRobot) {
                    Platform.runLater(() -> lblTurno.setText("¡ES TU TURNO!"));

                    String accionElegida;
                    if (estrategia != null && !estrategia.isEmpty()) {
                        Platform.runLater(() -> panelBotones.setDisable(true));
                        Thread.sleep(900);
                        accionElegida = estrategia.get(indiceEstrategia % estrategia.size());
                        indiceEstrategia++;
                    } else {
                        accionElegida = esperarSeleccionManual();
                    }

                    misMovimientosEjecutados.add(accionElegida);
                    conexion.enviarMensaje(accionElegida);

                    if (accionElegida.equals("ATTACK") || accionElegida.equals("SPECIAL")) {
                        String respuesta = conexion.recibirMensaje();
                        aplicarMovimientoLocal(miRobot, robotRival, accionElegida, respuesta, true);
                    } else {
                        aplicarMovimientoLocal(miRobot, robotRival, accionElegida, "OK", true);
                    }

                } else {
                    Platform.runLater(() -> {
                        lblTurno.setText("Turno del rival... esperando jugada");
                        panelBotones.setDisable(true);
                    });

                    String accionRival = conexion.recibirMensaje();
                    if (accionRival == null) break;

                    if (accionRival.equals("ATTACK") || accionRival.equals("SPECIAL")) {
                        boolean esquivado = Math.random() < miRobot.getProbabilidadEsquiva();
                        String resultado = esquivado ? "DODGE" : "HIT";
                        conexion.enviarMensaje(resultado);
                        aplicarMovimientoLocal(robotRival, miRobot, accionRival, resultado, false);
                        miRobot.reiniciarEsquivas();
                    } else {
                        aplicarMovimientoLocal(robotRival, miRobot, accionRival, "OK", false);
                    }
                }

                Platform.runLater(this::actualizarUI);
            }

            Platform.runLater(this::mostrarFinPartida);

        } catch (Exception e) {
            Platform.runLater(() -> listaRegistro.getItems().add("Error de conexión: " + e.getMessage()));
        }
    }

    private void aplicarMovimientoLocal(Robot atacante, Robot defensor, String accion, String resultado, boolean esPropio) {
        movimiento mov = crearMovimiento(accion);
        if (!accion.equals("DODGE")) {
            atacante.reiniciarEsquivas();
        }
        if (resultado.equals("HIT") || resultado.equals("OK")) {
            mov.Ejecutar(atacante, defensor);
        }
        String log = atacante.getNombre() + " usó " + accion + " -> " + resultado;
        Platform.runLater(() -> {
            reproducirAnimacionAccion(esPropio, accion, resultado);
            listaRegistro.getItems().add(log);
            listaRegistro.scrollTo(listaRegistro.getItems().size() - 1);
        });
    }

    private movimiento crearMovimiento(String nombre) {
        return switch (nombre) {
            case "SPECIAL" -> new especial();
            case "DODGE" -> new esquivar();
            case "HEAL" -> new curar();
            case "CHARGE" -> new cargar();
            default -> new ataque();
        };
    }

    private final Object lockManual = new Object();
    private String jugadaManualPendiente = null;

    private String esperarSeleccionManual() throws InterruptedException {
        Platform.runLater(() -> panelBotones.setDisable(false));
        synchronized (lockManual) {
            jugadaManualPendiente = null;
            while (jugadaManualPendiente == null) {
                lockManual.wait();
            }
            Platform.runLater(() -> panelBotones.setDisable(true));
            return jugadaManualPendiente;
        }
    }

    private void seleccionarJugada(String jugada) {
        synchronized (lockManual) {
            jugadaManualPendiente = jugada;
            lockManual.notifyAll();
        }
    }

    @FXML private void onAttack() { seleccionarJugada("ATTACK"); }
    @FXML private void onSpecial() { seleccionarJugada("SPECIAL"); }
    @FXML private void onDodge() { seleccionarJugada("DODGE"); }
    @FXML private void onHeal() { seleccionarJugada("HEAL"); }
    @FXML private void onCharge() { seleccionarJugada("CHARGE"); }

    private void actualizarUI() {
        lblHpPropio.setText(String.format("HP: %.1f / 100", miRobot.getVidaActual()));
        barHpPropio.setProgress(Math.max(0, miRobot.getVidaActual() / 100.0));
        lblNrgPropio.setText("NRG: " + miRobot.getEnergia());
        lblStatsPropio.setText("ATK: " + miRobot.getAtaque() + " | DEF: " + miRobot.getDefensa() + " | SPD: " + miRobot.getVelocidad());

        lblHpRival.setText(String.format("HP: %.1f / 100", robotRival.getVidaActual()));
        barHpRival.setProgress(Math.max(0, robotRival.getVidaActual() / 100.0));
        lblNrgRival.setText("NRG: " + robotRival.getEnergia());
        lblStatsRival.setText("ATK: " + robotRival.getAtaque() + " | DEF: " + robotRival.getDefensa() + " | SPD: " + robotRival.getVelocidad());
    }

    private void mostrarFinPartida() {
        panelBotones.setDisable(true);
        Robot ganador = reglas.getGanador();
        String textoGanador = (ganador != null) ? "¡Ganador: " + ganador.getNombre() + "!" : "¡Empate!";
        lblTurno.setText("COMBATE FINALIZADO - " + textoGanador);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fin de la Partida");
        alert.setHeaderText(textoGanador);
        alert.setContentText("¿Deseas guardar tu robot en robots.bin y tus movimientos en estrategia_partida.txt?");

        ButtonType btnGuardar = new ButtonType("Guardar Archivos");
        ButtonType btnCerrar = new ButtonType("Salir", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnGuardar, btnCerrar);

        alert.showAndWait().ifPresent(resp -> {
            if (resp == btnGuardar) {
                try {
                    GestorArchivos.guardarRobotBinario(miRobot, new File("robots.bin"));
                    GestorArchivos.guardarMovimientosTxt(misMovimientosEjecutados, new File("estrategia_partida.txt"));
                    listaRegistro.getItems().add("Archivos robots.bin y estrategia_partida.txt guardados.");
                } catch (Exception e) {
                    listaRegistro.getItems().add("Error al guardar: " + e.getMessage());
                }
            }
            conexion.cerrar();
        });
    }
}