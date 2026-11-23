package exceptions;

public class RobotInvalidoException extends Exception {
    public RobotInvalidoException(String mensaje) {
        super(mensaje);
    }
}