package Modelo.efectos;

import Modelo.Robot.Robot;

public class Quemadura extends Efecto {

    @Override
    public void aplicar(Robot propio,Robot rival){

rival.recibirDanio(3);
    }
@Override
    public String getNombre() {
        return "quemadura";
}

//Efecto 1

}
