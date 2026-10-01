import edu.unal.ed.arreglos.DynamicArrayQueue;
import edu.unal.ed.arreglos.DynamicArrayStack;
import edu.unal.ed.interfaces.MyList;
import edu.unal.ed.interfaces.MyQueue;
import edu.unal.ed.interfaces.MyStack;
import edu.unal.ed.listas.DoublyLinkedList;
import edu.unal.ed.listas.DoublyLinkedListTail;
import edu.unal.ed.listas.SinglyLinkedList;
import edu.unal.ed.listas.SinglyLinkedListTail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;


public class Main {

    //  parametros
    static final int K = 100_000;                      // tamaño del lote
    static final int REPS = 15;                        // repeticiones medidas por tamaño
    static final long WARMUP_NS = 200_000_000L;        // calentamiento por caso: 200 ms
    static final long WARMUP_GLOBAL_NS = 50_000_000L;  // calentamiento por caso durante la fase 0
    static final int N_GLOBAL = 1_000;                 // tamaño usado en la fase 0
    static final int UMBRAL_RUIDO = 20;
    static final boolean INCLUIR_10M = false;
    static final String ARCHIVO_CSV = "resultados.csv";

    static final int[] TAM_BASE = {10, 100, 1_000, 10_000, 100_000, 1_000_000};
    static final int[] TAM_GRANDES = INCLUIR_10M
            ? new int[]{10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000}
            : TAM_BASE;


    //  datos pregenerados

    static final int MASK = (1 << 21) - 1;
    static final Integer[] DATOS = generarDatos(MASK + 1);
    static final Integer OBJETIVO = DATOS[0];
    static final Integer EXTRA = -1;


    static Object sink;
    static long sinkL;

    // estado arnes
    static boolean faseGlobal = false;
    static long ruidoTimerNs = 1;
    static final List<String> FILAS = new ArrayList<>();


    //  tipos auxiliares

    @FunctionalInterface
    interface Accion {
        void run(int i);
    }


    @FunctionalInterface
    interface Escenario {
        DoubleSupplier preparar(int n);
    }

    enum Tipo {
        LOTE_O1(1_000), OPERACION_ON(1_000), AMORTIZADO(50_000);

        final int nCalentamiento; // tamaño de estructura que se descarta del calentamiento

        Tipo(int nCalentamiento) {
            this.nCalentamiento = nCalentamiento;
        }
    }


    //  generacion de datos y structuras por fuera del cronometro

    static Integer[] generarDatos(int n) {
        Integer[] d = new Integer[n];
        for (int i = 0; i < n; i++) d[i] = i;
        Random r = new Random(42);
        for (int i = n - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            Integer t = d[i];
            d[i] = d[j];
            d[j] = t;
        }
        return d;
    }


    static MyList<Integer> listaConN(Supplier<MyList<Integer>> f, int n) {
        MyList<Integer> l = f.get();
        for (int i = 0; i < n; i++) l.pushFront(DATOS[i & MASK]);
        return l;
    }


    static MyStack<Integer> pilaConN(Supplier<MyStack<Integer>> f, int n) {
        MyStack<Integer> p = f.get();
        for (int i = 0; i < n; i++) p.push(DATOS[i & MASK]);
        return p;
    }


    static MyQueue<Integer> colaConN(Supplier<MyQueue<Integer>> f, int n) {
        MyQueue<Integer> q = f.get();
        for (int i = 0; i < n; i++) q.enqueue(DATOS[i & MASK]);
        return q;
    }


    //  medicion



    static double medirLote(int k, Accion op, Accion deshacer) {
        long ini = System.nanoTime();
        for (int i = 0; i < k; i++) {
            op.run(i);
        }
        long fin = System.nanoTime();

        if (deshacer != null) {
            for (int i = 0; i < k; i++) {
                deshacer.run(i);
            }
        }
        return (fin - ini) / (double) k;
    }


    static double medirUna(Runnable op, Runnable deshacer) {
        long ini = System.nanoTime();
        op.run();
        long fin = System.nanoTime();

        if (deshacer != null) {
            deshacer.run();
        }
        return (double) (fin - ini);
    }

    static double mediana(double[] t) {
        double[] c = t.clone();
        Arrays.sort(c);
        return c[c.length / 2];
    }


    static long estimarRuidoTimer() {
        long min = Long.MAX_VALUE;
        for (int i = 0; i < 200_000; i++) {
            long a = System.nanoTime();
            long b = System.nanoTime();
            long d = b - a;
            if (d > 0 && d < min) min = d;
        }
        return min == Long.MAX_VALUE ? 1 : min;
    }


    static void correr(String estructura, String metodo, Tipo tipo, String complejidad,
                       int[] tamanos, Escenario esc) {
        // calentamiento
        DoubleSupplier calentamiento = esc.preparar(tipo.nCalentamiento);
        long limite = faseGlobal ? WARMUP_GLOBAL_NS : WARMUP_NS;
        long t0 = System.nanoTime();
        do {
            calentamiento.getAsDouble();
        } while (System.nanoTime() - t0 < limite);

        //mediciones
        int[] ns = faseGlobal ? new int[]{N_GLOBAL} : tamanos;
        for (int n : ns) {
            if (!faseGlobal) System.gc();
            DoubleSupplier real = esc.preparar(n);
            double[] t = new double[REPS];
            for (int r = 0; r < REPS; r++) {
                t[r] = real.getAsDouble();
            }
            if (faseGlobal) continue;

            double med = mediana(t);
            int ops = (tipo == Tipo.LOTE_O1) ? K : (tipo == Tipo.OPERACION_ON) ? 1 : n;
            int bajo = (med * ops < UMBRAL_RUIDO * (double) ruidoTimerNs) ? 1 : 0;
            registrar(estructura, metodo, n, med, tipo.name(), ops, REPS, complejidad, bajo);
            System.out.printf(Locale.ROOT, "%-15s %-10s n=%-9d %14.2f ns/op  [%s]%s%n",
                    estructura, metodo, n, med, tipo.name(), bajo == 1 ? "  (bajo_resolucion)" : "");
        }
    }

    static void registrar(String estructura, String metodo, int n, double nsPorOp, String tipoMedicion,
                          int ops, int reps, String complejidad, int bajoResolucion) {
        FILAS.add(String.format(Locale.ROOT, "%s,%s,%d,%.2f,%s,%d,%d,%s,%d",
                estructura, metodo, n, nsPorOp, tipoMedicion, ops, reps, complejidad, bajoResolucion));
    }


    //  listas

    static void suiteLista(String nombre, Supplier<MyList<Integer>> f, boolean pushBackO1, boolean popBackO1) {


        correr(nombre, "pushFront", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyList<Integer> l = listaConN(f, n);
            return () -> medirLote(K, i -> l.pushFront(DATOS[i & MASK]), i -> sink = l.popFront());
        });


        correr(nombre, "popFront", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyList<Integer> l = listaConN(f, n + K);
            return () -> medirLote(K, i -> sink = l.popFront(), i -> l.pushFront(DATOS[i & MASK]));
        });


        if (pushBackO1) {
            correr(nombre, "pushBack", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
                MyList<Integer> l = listaConN(f, n);
                return () -> medirLote(K, i -> l.pushBack(DATOS[i & MASK]), i -> sink = l.popFront());
            });
        } else {
            correr(nombre, "pushBack", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> {
                MyList<Integer> l = listaConN(f, n);
                return () -> medirUna(() -> l.pushBack(EXTRA), () -> sink = l.popFront());
            });
        }


        if (popBackO1) {
            correr(nombre, "popBack", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
                MyList<Integer> l = listaConN(f, n + K);
                return () -> medirLote(K, i -> sink = l.popBack(), i -> l.pushBack(DATOS[i & MASK]));
            });
        } else {
            correr(nombre, "popBack", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> {
                MyList<Integer> l = listaConN(f, n);
                return () -> medirUna(() -> sink = l.popBack(), () -> l.pushFront(EXTRA));
            });
        }


        correr(nombre, "find", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> {
            MyList<Integer> l = listaConN(f, n);
            return () -> medirUna(() -> sink = l.find(OBJETIVO), null);
        });


        correr(nombre, "erase", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> {
            MyList<Integer> l = listaConN(f, n);
            return () -> medirUna(() -> l.erase(OBJETIVO), () -> l.pushBack(OBJETIVO));
        });


        correr(nombre, "addBefore", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> {
            MyList<Integer> l = listaConN(f, n);
            return () -> medirUna(() -> l.addBefore(OBJETIVO, EXTRA), () -> l.erase(EXTRA));
        });


        correr(nombre, "addAfter", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> {
            MyList<Integer> l = listaConN(f, n);
            return () -> medirUna(() -> l.addAfter(OBJETIVO, EXTRA), () -> l.erase(EXTRA));
        });


        correr(nombre, "isEmpty", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyList<Integer> l = listaConN(f, n);
            return () -> medirLote(K, i -> sinkL += l.isEmpty() ? 1 : 0, null);
        });
    }


    //  stack

    static void suitePila() {
        Supplier<MyStack<Integer>> f = DynamicArrayStack::new;


        correr("Stack", "push", Tipo.AMORTIZADO, "O(1) amortizado", TAM_GRANDES, n -> () -> {
            MyStack<Integer> p = f.get(); // creada antes de iniciar el cronómetro
            return medirLote(n, i -> p.push(DATOS[i & MASK]), null);
        });


        correr("Stack", "pop", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyStack<Integer> p = pilaConN(f, n + K);
            return () -> medirLote(K, i -> sink = p.pop(), i -> p.push(DATOS[i & MASK]));
        });


        correr("Stack", "peek", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyStack<Integer> p = pilaConN(f, n);
            return () -> medirLote(K, i -> sink = p.peek(), null);
        });
        correr("Stack", "isEmpty", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyStack<Integer> p = pilaConN(f, n);
            return () -> medirLote(K, i -> sinkL += p.isEmpty() ? 1 : 0, null);
        });
        correr("Stack", "size", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyStack<Integer> p = pilaConN(f, n);
            return () -> medirLote(K, i -> sinkL += p.size(), null);
        });


        correr("Stack", "delete", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> () -> {
            MyStack<Integer> p = pilaConN(f, n);
            return medirUna(() -> p.delete(OBJETIVO), null);
        });
    }

    // queue
    static void suiteCola() {
        Supplier<MyQueue<Integer>> f = DynamicArrayQueue::new;


        correr("Queue", "enqueue", Tipo.AMORTIZADO, "O(1) amortizado", TAM_GRANDES, n -> () -> {
            MyQueue<Integer> q = f.get();
            return medirLote(n, i -> q.enqueue(DATOS[i & MASK]), null);
        });


        correr("Queue", "dequeue", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyQueue<Integer> q = colaConN(f, n + K);
            return () -> medirLote(K, i -> sink = q.dequeue(), i -> q.enqueue(DATOS[i & MASK]));
        });


        correr("Queue", "front", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyQueue<Integer> q = colaConN(f, n);
            return () -> medirLote(K, i -> sink = q.front(), null);
        });
        correr("Queue", "isEmpty", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyQueue<Integer> q = colaConN(f, n);
            return () -> medirLote(K, i -> sinkL += q.isEmpty() ? 1 : 0, null);
        });
        correr("Queue", "size", Tipo.LOTE_O1, "O(1)", TAM_GRANDES, n -> {
            MyQueue<Integer> q = colaConN(f, n);
            return () -> medirLote(K, i -> sinkL += q.size(), null);
        });


        correr("Queue", "delete", Tipo.OPERACION_ON, "O(n)", TAM_BASE, n -> {
            MyQueue<Integer> q = colaConN(f, n);
            Integer objetivo = DATOS[(n - 1) & MASK];
            return () -> medirUna(() -> q.delete(objetivo), () -> q.enqueue(objetivo));
        });
    }


    //  conjunto completo pruebas

    static void suiteCompleta() {

        suiteLista("SinglyList",     SinglyLinkedList::new,      false, false);
        suiteLista("SinglyListTail", SinglyLinkedListTail::new,  true,  false);
        suiteLista("DoublyList",     DoublyLinkedList::new,      false, false);
        suiteLista("DoublyListTail", DoublyLinkedListTail::new,  true,  true);
        suitePila();
        suiteCola();
    }


    static void calibrar() {
        registrar("Calibracion", "nanoTime_min_delta", 0, ruidoTimerNs, "CALIBRACION", 1, 1, "n/a", 0);

        double[] a = new double[REPS];
        for (int r = 0; r < REPS; r++) a[r] = medirLote(K, i -> { }, null);
        registrar("Calibracion", "lote_vacio", 0, mediana(a), "CALIBRACION", K, REPS, "n/a", 0);

        double[] b = new double[1001];
        for (int r = 0; r < b.length; r++) b[r] = medirUna(() -> { }, null);
        registrar("Calibracion", "una_vacia", 0, mediana(b), "CALIBRACION", 1, b.length, "n/a", 0);
    }

    //   Main
    public static void main(String[] args) throws IOException {
        ruidoTimerNs = estimarRuidoTimer();
        System.out.println("Resolucion efectiva de nanoTime (min. delta > 0): " + ruidoTimerNs + " ns");

        System.out.println("Fase 0: calentamiento global (resultados descartados)...");
        faseGlobal = true;
        suiteCompleta();
        faseGlobal = false;

        calibrar();

        System.out.println("Fase 1: mediciones");
        suiteCompleta();


        List<String> lineas = new ArrayList<>();
        lineas.add("estructura,metodo,n,ns_por_op,tipo_medicion,ops_por_medicion,repeticiones,complejidad_esperada,bajo_resolucion");
        lineas.addAll(FILAS);
        Files.write(Paths.get(ARCHIVO_CSV), lineas, StandardCharsets.UTF_8);
        System.out.println("Listo. CSV: " + Paths.get(ARCHIVO_CSV).toAbsolutePath() + " (" + FILAS.size() + " filas)");
    }
}
