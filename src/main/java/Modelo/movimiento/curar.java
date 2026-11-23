package Modelo.movimiento;
import Modelo.movimiento.movimiento;
import Modelo.Robot.Robot;


public class curar extends movimiento {

    @Override
    public void Ejecutar (Robot atacante, Robot defensor){
        atacante.curar(10);

    }
@Override
public String getNombre() {
    return "Curar";
}


}
