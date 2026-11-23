package Modelo.movimiento;

import controller.control_ataque.CalcularDanio;
import Modelo.Robot.Robot;

public class especial extends movimiento {

    @Override
    public void Ejecutar(Robot atacante, Robot defensor) {
        double danio = CalcularDanio.DanioEspecial(atacante.getAtaque(), atacante.getEnergia(), defensor.getDefensa());
        defensor.recibirDanio(danio);
        atacante.vaciarEnergia();
    }

    @Override
    public String getNombre() {
        return "Especial";
    }
}