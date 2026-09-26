

import edu.unal.ed.interfaces.Operation;
import edu.unal.ed.listas.SinglyLinkedList;
import java.time.Instant;
import java.time.Duration;
import java.util.Random;

public class Main {

    public static void exec(int size, String method, Operation operation) {
        Random rand = new Random();

        // Generamos los datos antes de iniciar la medición
        int[] datosAleatorios = new int[size];

        for (int i = 0; i < size; i++) {
            datosAleatorios[i] = rand.nextInt(100000);
        }

        // Iniciar Cronometro
        long start = System.nanoTime();

        // Medicion de la Operacion
        for (int i = 0; i < size; i++) {
            operation.apply(datosAleatorios[i]);
        }

        // Se detiene el cronometro
        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        System.out.printf(
                "Se ejecutó %s de %d elementos en: %d nanosegundos%n",
                method, size, timeElapsed
        );
    }

    public static void main(String[] args) {
        // Va creciendo en potencias de 10
        final int startSize = 10;
        final int endSize = 100000; // limite

        System.out.println("--- PRUEBAS DE COMPLEJIDAD: SinglyLinkedList (Sin Cola) ---");

        for (int size = startSize; size <= endSize; size *= 10) {
            // se repite 5 veces para promediar
            for (int i = 0; i < 5; i++) {
                SinglyLinkedList<Integer> lista = new SinglyLinkedList<>();

                // se le pasa todo a la funcion exec
                exec(size, "PushFront", lista::pushFront);
            }
            System.out.println("---------------------------------------------------------");
        }
    }
}