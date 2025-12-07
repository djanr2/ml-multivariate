package unam.iimas.ia.ml.mlmultivariate.ega;

import unam.iimas.ia.ml.mlmultivariate.faa.AlgoritmoAscensoRapido;
import unam.iimas.ia.ml.mlmultivariate.faa.Properties;
import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;

import java.util.*;

public class EGA {
    private final int epoch = 100;
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
            System.out.println(epoch_);
            interact();
        }
        for (int i = 0; i < individuals.size(); i++) {
            System.out.println(individuals.get(i).getBestEpsilonPhiValue()+ ": "+individuals.get(i).getModelo());
        }
    }

    private List<AlgoritmoAscensoRapido> getNewGeneration(){
        List<AlgoritmoAscensoRapido> individuals = new ArrayList();
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf;
        while (individuals.size()<number_individuals){
            prop = new Properties(random.nextLong(), 7, 10,file);
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
                    //mutate(xm, ym);
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
                a.getModelo().getTerminos()[i] = a.getModelo().getRandomTermino(random,
                        prop.getMaxima_potencia_l());
            }
            if (i == randy){
                b.getModelo().getTerminos()[i] = b.getModelo().getRandomTermino(random,
                        prop.getMaxima_potencia_l());
            }
        }
        a.run();
        b.run();
        individuals.add(a);
        individuals.add(b);
    }
}
