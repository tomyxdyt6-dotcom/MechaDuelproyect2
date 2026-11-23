package Modelo.movimiento;

import controller.control_ataque.CalcularDanio;
import Modelo.Robot.Robot;

public class ataque extends movimiento {

    @Override
    public void Ejecutar(Robot atacante, Robot defensor) {
        double danio = CalcularDanio.danioNormal(atacante.getAtaque(), defensor.getDefensa());
        defensor.recibirDanio(danio);
    }

    @Override
    public String getNombre() {
        return "Ataque";
    }
}