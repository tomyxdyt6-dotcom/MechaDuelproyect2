package Modelo.efectos;

import Modelo.Robot.Robot;

public class Regeneracion extends Efecto{
    @Override
    public void aplicar(Robot propio,Robot rival){
        propio.curar(3);

    }

    @Override
    public String getNombre(){
        return "regeneracion";
    }
}
//efecto 2