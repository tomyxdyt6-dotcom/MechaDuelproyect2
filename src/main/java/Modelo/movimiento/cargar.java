package Modelo.movimiento;
import Modelo.movimiento.movimiento;
import Modelo.Robot.Robot;

public class cargar extends movimiento {

    @Override
    public void  Ejecutar (Robot atacante,Robot defensor){
        atacante.cargarEnergia(10);
    }
    @Override
    public String getNombre() {
        return "Carga";
    }
}
