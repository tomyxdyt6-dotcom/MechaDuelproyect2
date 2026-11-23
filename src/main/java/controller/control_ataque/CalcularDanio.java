package controller.control_ataque;

public class CalcularDanio {
    public static double danioNormal(int ataque, int defensa){
        double normal;
        normal=(1.0*ataque*ataque)/(defensa+ ataque);
        return normal;
    }
    public static double DanioEspecial(int ataque, int defensa, int energia){
        double Total;
        double Especial;

        Total=ataque+0.5*energia;
        Especial=((Total*Total)/(Total+defensa));

        return Especial;
    }
}
//daño 1