package Modelo.efectos;

import Modelo.Robot.Robot;

public class Nucleo_activo extends Efecto {
    @Override
    public void aplicar(Robot propio,Robot rival){

        propio.cargarEnergia(2);
    }
    @Override
    public String getNombre() {
        return "Nucleo Activo";
    }


//efecto 3
}
