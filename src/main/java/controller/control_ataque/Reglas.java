package controller.control_ataque;
import java.util.ArrayList;
import Modelo.movimiento.movimiento;
import Modelo.Robot.Robot;

public class Reglas {

    private ArrayList<String> registro;
    private Robot robot1;
    private Robot robot2;
    private turno turno;
    private Robot actual;

    public Reglas(Robot robot1, Robot robot2) {
        this.robot1 = robot1;
        this.robot2 = robot2;
        this.turno = new turno(robot1, robot2);
        this.actual = null;
        this.registro = new ArrayList<>();
    }

    private Robot obtenerRival(Robot r) {
        if (r == robot1) {
            return robot2;
        }
        return robot1;
    }

    public Robot iniciarTurno() {
        this.actual = turno.siguiente();
        Robot rival = obtenerRival(actual);

        for (int i = 0; i < actual.getCantidadEfectos(); i++) {
            actual.getEfectos()[i].aplicar(actual, rival);
        }
        return actual;
    }

    public String ejecutarMovimiento(movimiento m) {
        Robot rival = obtenerRival(actual);
        String nombre = m.getNombre();
        String resultado;

        if (nombre.equals("ATTACK") || nombre.equals("SPECIAL")) {
            double probabilidad = rival.getProbabilidadEsquiva();

            if (Math.random() < probabilidad) {
                resultado = "DODGE";
            } else {
                m.Ejecutar(actual, rival);
                resultado = "HIT";
            }
            rival.reiniciarEsquivas();
        } else {
            m.Ejecutar(actual, rival);
            resultado = "OK";
        }

        if (!nombre.equals("DODGE")) {
            actual.reiniciarEsquivas();
        }
        registro.add(actual.getNombre() + ": " + nombre + " -> " + resultado);
        return resultado;
    }

    public boolean terminado() {
        return !robot1.estaVivo() || !robot2.estaVivo();
    }

    public Robot getGanador() {
        if (robot1.estaVivo() && !robot2.estaVivo()) {
            return robot1;
        }
        if (robot2.estaVivo() && !robot1.estaVivo()) {
            return robot2;
        }
        return null;
    }

    public Robot getActual() {
        return actual;
    }
    public ArrayList<String> getRegistro() {
        return registro;
    }
}
//reglas1