package Modelo.Robot;

import exceptions.RobotInvalidoException;
import Modelo.efectos.Efecto;
import java.io.Serializable;

public class Robot implements Serializable {

    // Constantes
    public static final int VIDA_MAXIMA = 100;
    public static final int PUNTOS_TOTALES = 200;

    // Atributos
    private String nombre;
    private int ataque;
    private int defensa;
    private int velocidad;
    private int energia;
    private double vidaActual;
    private int contadorTurno;
    private int esquivasSeguidas;
    private Efecto[] efectos;
    private int cantidadEfectos;

    // Constructor
    public Robot(String nombre, int ataque, int defensa, int velocidad, int energia)
            throws RobotInvalidoException {

        int suma = ataque + defensa + velocidad + energia;

        if (suma != PUNTOS_TOTALES) {
            throw new RobotInvalidoException(
                    "La suma de ATK, DEF, SPD y NRG debe ser 200. Actual: " + suma);
        }
        if (ataque < 0 || defensa < 0 || velocidad < 0 || energia < 0) {
            throw new RobotInvalidoException("Las estadisticas no pueden ser negativas.");
        }

        this.nombre = nombre;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
        this.energia = energia;
        this.vidaActual = VIDA_MAXIMA;
        this.contadorTurno = 0;
        this.esquivasSeguidas = 0;
        this.efectos = new Efecto[3];   // maximo 3 efectos distintos
        this.cantidadEfectos = 0;
    }

    // ----- Vida -----
    public void recibirDanio(double cantidad) {
        this.vidaActual = this.vidaActual - cantidad;
        if (this.vidaActual < 0) {
            this.vidaActual = 0;
        }
    }

    public void curar(double cantidad) {
        this.vidaActual = this.vidaActual + cantidad;
        if (this.vidaActual > VIDA_MAXIMA) {
            this.vidaActual = VIDA_MAXIMA;
        }
    }

    public boolean estaVivo() {
        return this.vidaActual > 0;
    }

    // ----- Energia -----
    public void cargarEnergia(int cantidad) {
        this.energia = this.energia + cantidad;
    }

    public void vaciarEnergia() {
        this.energia = 0;
    }

    // ----- Esquivar -----
    public void registrarEsquiva() {
        if (this.esquivasSeguidas < 2) {
            this.esquivasSeguidas++;
        }
    }

    public void reiniciarEsquivas() {
        this.esquivasSeguidas = 0;
    }

    public double getProbabilidadEsquiva() {
        if (this.esquivasSeguidas == 1) {
            return 0.5;
        }
        if (this.esquivasSeguidas == 2) {
            return 0.75;
        }
        return 0;
    }

    // ----- Turnos -----
    public void acumularVelocidad() {
        if (this.contadorTurno < 100) {
            this.contadorTurno = this.contadorTurno + this.velocidad;
        }
    }

    public boolean tieneTurno() {
        return this.contadorTurno >= 100;
    }

    public void consumirTurno() {
        this.contadorTurno = this.contadorTurno - 100;
    }

    // ----- Efectos -----
    public boolean agregarEfecto(Efecto nuevo) {
        // No se permite repetir el mismo efecto
        for (int i = 0; i < cantidadEfectos; i++) {
            if (efectos[i].getClass() == nuevo.getClass()) {
                return false;
            }
        }
        if (cantidadEfectos < efectos.length) {
            efectos[cantidadEfectos] = nuevo;
            cantidadEfectos++;
            return true;
        }
        return false;
    }

    // ----- Getters -----
    public String getNombre() {
        return nombre;
    }

    public int getAtaque() {
        return ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public int getEnergia() {
        return energia;
    }

    public double getVidaActual() {
        return vidaActual;
    }

    public int getContadorTurno() {
        return contadorTurno;
    }

    public Efecto[] getEfectos() {
        return efectos;
    }

    public int getCantidadEfectos() {
        return cantidadEfectos;
    }
}




