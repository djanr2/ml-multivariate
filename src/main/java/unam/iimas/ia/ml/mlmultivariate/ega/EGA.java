package unam.iimas.ia.ml.mlmultivariate.ega;

import unam.iimas.ia.ml.mlmultivariate.faa.AlgoritmoAscensoRapido;
import unam.iimas.ia.ml.mlmultivariate.faa.Properties;
import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;
import java.util.*;

// TODO
//  1. Abrir un archivo data .txt separado por tabs
//      1.1 Separar las tuplas por TRN y TST aleatoriamente. Con un porcentaje de seleccion
//      1.2 Mantener conjunto de datos completo en memoria (copia) para no tener que estar abriendo de nuevo el archivo.
//      1.3 Del mismo archivo duplicar los datos
//      1.4 Trabajar con las copias de los dattos y no con los originales.
// TODO
//  2. Establecer las clases individual, gen (->modelo), ega
//      2.1 Establecer las propiedades (metadatos para correr el algoritmo EGA)
//          2.1.1 numero de terminos
//          2.1.2 potencia maxima l
//          2.1.3 nombre/ubicacion del archivo -> determinar si el archivo tiene encabezados o hay que ponerlos
//              2.1.3.1 Separar el archivo en TRN y TST en archivos para saber cuales fueron
//              2.1.3.2 Archivos de cada proceso ? -> validar que archivos conviene imprimir
//              2.1.3.3 Archivo de salida con resultado (modelo)
//          *2.1.4 Quasi Minmax -> Se tiene que cambiar el AlgoritmoAscensoRapido
//          2.1.5 Pc, Probabilidad de cruce siempre se cruzan.
//          2.1.6 Pm, Probabilidad de mutacion valores del libro
//          2.1.7 numero de individuos por generacion
//          2.1.8 numero de generaciones
//          2.1.9 Factor de regularizacion 0.00001 genberalmente (Esto esta en la clase Precision en RHO)
//          *2.1.9 Ver si se establece el error RMS
// TODO
//  3. Establecer conexion con base de datos
//      3.1 Generar tablas
// TODO
//  4. Establecer servicio API rest
// TODO
//  5. Diseñar el front-end
// TODO
//  6. Revisar el comparable de Vector

public class EGA {
    private final int epoch = 50;
    private final double mutation_probability = 0.5;
    private final double cross_probability = 1.0;
    private final int number_individuals= 40; // simpre debe de ser par
    private Random random = new Random();
    Properties prop;
    private List<AlgoritmoAscensoRapido> individuals;
    private Long seed;

    public static void main(String[] args) {
        //TODO EN EL CONSTRUCTOR HAY QUE PASARLE LAS PROPERTIES DEL EGA
        EGA ega = new EGA();
        ega.run();
    }

    public void run(){
        individuals = getNewGeneration();
        for (int epoch_ = 0; epoch_ < epoch; epoch_++) {
            System.out.println("epoch: "+epoch_);
            interact();
        }
        for (int i = 0; i < individuals.size(); i++) {
            System.out.println(individuals.get(i).getBestEpsilonPhiValue()+ ": "+individuals.get(i).getModelo());
        }
        BigDecimal[][] listaCompletaVectores = individuals.get(0).getBestCoeficients();
        Matrix.print(listaCompletaVectores);

        List<Vector> listaCompleta = individuals.get(0).getEpsilonPhi();
        listaCompleta.addAll(individuals.get(0).getEpsilonTetha());
        listaCompleta.sort(Comparator.comparingInt(unam.iimas.ia.ml.mlmultivariate.model.Vector::getIndex));

        for (Vector v:
                listaCompleta) {
            System.out.println(v.getStringToGraph());
        }


    }

    private List<AlgoritmoAscensoRapido> getNewGeneration(){
        List<AlgoritmoAscensoRapido> individuals = new ArrayList();
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf;
        while (individuals.size()<number_individuals){
            prop = new Properties(random.nextLong(), 9, 10,file);
            aaf = new AlgoritmoAscensoRapido(prop);
            aaf.run();
            individuals.add(aaf);
        }
        Collections.sort(individuals);
        return individuals;
    }

    private void interact(){
        int size = individuals.size();
        for (int i = 0; i < size/2; i++) {
            AlgoritmoAscensoRapido x = individuals.get(i);
            AlgoritmoAscensoRapido y = individuals.get(size-i-1);
            int n = prop.getNumero_terminos();
            int start_to_cross = random.nextInt(n);
            double mutation = random.nextDouble();
            double cross = random.nextDouble();
            if(cross_probability-cross > 0){
                cross(x, y, start_to_cross);
                if(mutation_probability-mutation > 0){
                    AlgoritmoAscensoRapido xm = individuals.get(individuals.size()-1);
                    AlgoritmoAscensoRapido ym = individuals.get(individuals.size()-2);
                    mutate(xm, ym);
                }
            }
        }
        Collections.sort(individuals);
        individuals = individuals.subList(0, number_individuals);
    }

    private void cross(AlgoritmoAscensoRapido x, AlgoritmoAscensoRapido y, int start){
        int i = start;
        int n = prop.getNumero_terminos();
        int half = (n / 2) - 1;
        AlgoritmoAscensoRapido a = new AlgoritmoAscensoRapido(prop);
        AlgoritmoAscensoRapido b = new AlgoritmoAscensoRapido(prop);
        do {
            i = (i + 1) % n;
            if(half<0){
                a.getModelo().getTerminos()[i] = x.getModelo().cloneTermino(i);
                b.getModelo().getTerminos()[i] = y.getModelo().cloneTermino(i);
            }else{
                b.getModelo().getTerminos()[i] = x.getModelo().cloneTermino(i);
                a.getModelo().getTerminos()[i] = y.getModelo().cloneTermino(i);
            }
            half --;
        } while (i != start);
        a.run();
        b.run();
        individuals.add(a);
        individuals.add(b);
    }

    //TODO VALIDAR QUE NO ESTE COLOCANDO UN TERMINO REPETIDO
    private void mutate(AlgoritmoAscensoRapido x, AlgoritmoAscensoRapido y){
        int n = prop.getNumero_terminos();
        int randx = random.nextInt(n);
        int randy = random.nextInt(n);
        AlgoritmoAscensoRapido a = new AlgoritmoAscensoRapido(prop);
        AlgoritmoAscensoRapido b = new AlgoritmoAscensoRapido(prop);
        for (int i = 0; i < n; i++) {
            a.getModelo().getTerminos()[i] = x.getModelo().cloneTermino(i);
            b.getModelo().getTerminos()[i] = y.getModelo().cloneTermino(i);
            if (i == randx){
                a.getModelo().getTerminos()[i] = x.getModelo().getRandomTermino(random,
                        prop.getMaxima_potencia_l(), prop.getFile().getNumeroVariables());
            }
            if (i == randy){
                b.getModelo().getTerminos()[i] = y.getModelo().getRandomTermino(random,
                        prop.getMaxima_potencia_l(), prop.getFile().getNumeroVariables());
            }
        }
        a.run();
        b.run();
        individuals.add(a);
        individuals.add(b);
    }
}
