package controller.archivos;

import Modelo.Robot.Robot;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {

    public static void guardarRobotBinario(Robot robot, File archivo) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(robot);
        }
    }

    public static Robot cargarRobotBinario(File archivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (Robot) ois.readObject();
        }
    }

    public static List<String> cargarEstrategia(File archivo) throws IOException {
        List<String> movimientos = new ArrayList<>();
        if (archivo == null || !archivo.exists()) return movimientos;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim().toUpperCase();
                if (!linea.isEmpty()) {
                    movimientos.add(linea);
                }
            }
        }
        return movimientos;
    }

    public static void guardarMovimientosTxt(List<String> movimientos, File archivo) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
            for (String mov : movimientos) {
                pw.println(mov);
            }
        }
    }
}