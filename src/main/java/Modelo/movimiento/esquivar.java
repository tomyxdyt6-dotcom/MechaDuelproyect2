package Modelo.movimiento;

import Modelo.movimiento.movimiento;
import Modelo.Robot.Robot;
public class esquivar extends movimiento {

    @Override
    public void Ejecutar(Robot atacante, Robot defensor) {
        atacante.registrarEsquiva();
    }

    @Override
    public String getNombre() {
        return "dodge";
    }
}
