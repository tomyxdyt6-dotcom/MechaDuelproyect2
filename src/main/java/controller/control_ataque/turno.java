package controller.control_ataque;

import Modelo.Robot.Robot;
public class turno {

    private Robot robot1;
    private Robot robot2;

    public turno(Robot robot1,Robot robot2){

        this.robot1=robot1;
        this.robot2=robot2;
    }

    public Robot siguiente( ){

        Robot elegido = null;
        while(!robot1.tieneTurno()&&!robot2.tieneTurno()){
            robot2.acumularVelocidad();
            robot1.acumularVelocidad();
        }


        if(robot1.tieneTurno()&&!robot2.tieneTurno()) elegido = robot1;
        if(!robot1.tieneTurno()&&robot2.tieneTurno()) elegido = robot2;

        if(robot1.tieneTurno()&&robot2.tieneTurno()) {

            if (robot1.getVelocidad() >= robot2.getVelocidad()) {
                elegido=robot1;
            }
            else {
                elegido=robot2;
            }
        }
        elegido.consumirTurno();
        return elegido;

    }

}
//turno1