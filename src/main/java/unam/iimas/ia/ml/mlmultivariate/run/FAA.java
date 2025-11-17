package unam.iimas.ia.ml.mlmultivariate.run;


import unam.iimas.ia.ml.mlmultivariate.faa.AlgoritmoAscensoRapido;
import unam.iimas.ia.ml.mlmultivariate.faa.Properties;
import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.model.Modelo;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;
import java.util.*;

public class FAA {
    private static final double TOLERANCE = 0.3;

    public static void main(String[] args) {
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf;
        BigDecimal menor = BigDecimal.ONE;
        BigDecimal[][] bestCoeficients = null;
        long bestSeed = 0L;
        Modelo m = null;
        List<Vector> bestEpsiolonPhi = null;
        List<Vector> bestEpsiolonTheta= null;
        Random ran = new Random();
        Properties prop = new Properties(ran.nextLong(), 11, 15,file );
        aaf = new AlgoritmoAscensoRapido(prop);
        int i = 0;
        do {
            i++;
            aaf.run();
            //System.out.println(aaf.getBestCoeficients()[0][0]);
            //Sacar epsilon phi y ese es el de la tolerancioa
            //System.out.println(aaf.getBestEpsilonPhiValue());
            if(aaf.getBestEpsilonPhiValue().compareTo(menor)<=0){
                menor = new BigDecimal(aaf.getBestEpsilonPhiValue().toString());
                bestCoeficients = aaf.getBestCoeficients();
                bestSeed = aaf.getSeed();
                m = aaf.getModelo();
                bestEpsiolonPhi = aaf.getEpsilonPhi();
                bestEpsiolonTheta = aaf.getEpsilonTetha();
            }
        } while (i<100);
         System.out.println("---------------------");
         Matrix.print(bestCoeficients);
         System.out.println("seed: " +bestSeed);
         System.out.println(m);
         System.out.println(menor);
         System.out.println();
         System.out.println("---------------------");

        List<Vector> listaCompletaVectores = bestEpsiolonPhi;
        listaCompletaVectores.addAll(bestEpsiolonTheta);
        listaCompletaVectores.sort(Comparator.comparingInt(Vector::getIndex));

        for (Vector v:
                listaCompletaVectores) {
            System.out.println(v.getStringToGraph());
        }
        //System.out.println(aaf.getEpsilonPhi().get(0).getStringToCalculate());
    }
}
