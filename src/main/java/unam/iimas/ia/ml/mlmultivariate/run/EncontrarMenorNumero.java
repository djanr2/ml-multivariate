package unam.iimas.ia.ml.mlmultivariate.run;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class EncontrarMenorNumero {

    public static void main(String[] args) {
        // Ruta del archivo
        String archivo = "src/main/resources/min_val";  // Cambia esto por la ruta de tu archivo

        // Llamada al método para encontrar el menor número
        double menorNumero = encontrarMenorNumero(archivo);

        // Imprimir el menor número
        if (Double.isNaN(menorNumero)) {
            System.out.println("No se pudo encontrar ningún número en el archivo.");
        } else {
            System.out.println("El menor número en el archivo es: " + menorNumero);
        }
    }

    public static double encontrarMenorNumero(String archivo) {
        double menorNumero = Double.MAX_VALUE;  // Inicializamos con el valor más grande posible
        boolean archivoValido = false;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                try {
                    // Convertir la línea a un número double
                    double numero = Double.parseDouble(linea.trim());

                    // Actualizar el menor número encontrado
                    if (numero < menorNumero) {
                        menorNumero = numero;
                    }

                    archivoValido = true;
                } catch (NumberFormatException e) {
                    System.err.println("Advertencia: No se pudo convertir la línea a un número válido: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }

        // Si no se encontró ningún número, retornamos NaN
        if (!archivoValido) {
            return Double.NaN;
        }

        return menorNumero;
    }
}
