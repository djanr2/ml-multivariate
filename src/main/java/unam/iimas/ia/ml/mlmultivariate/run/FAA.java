package unam.iimas.ia.ml.mlmultivariate.run;


import unam.iimas.ia.ml.mlmultivariate.faa.AlgoritmoAscensoRapido;
import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class FAA {

    private static final double TOLERANCE = 0.3;

    public static void main(String[] args) {
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf;

        while (true) {
            aaf = new AlgoritmoAscensoRapido(file);
            aaf.run(file.getVectores(), file.getLowerLimitScale(), file.getUpperLimitScale());
            //System.out.println(aaf.getBestCoeficients()[0][0]);
            //TODO tienes que validar que metodo es mejor para encontrar los coeficientes. Priorizar que epsion phi sea el mas peuqueño
            //Sacar epsilon phi y ese es el de la tolerancioa
            if (aaf.getBestCoeficients()[0][0].abs().compareTo(new BigDecimal(TOLERANCE))<= 0){
                break;
            }
        }
        System.out.println("---------------------");
        Matrix.print(aaf.getBestCoeficients());
        System.out.println("seed: " +aaf.getSeed());
        System.out.println(aaf.getM());
        System.out.println();
        System.out.println("---------------------");
        //AlgoritmoAscensoRapido.printVectores(aaf.getEpsilonThetha());
        //AlgoritmoAscensoRapido.printVectores(aaf.getEpsilonPhi());

        List<Vector> listaCompletaVectores = aaf.getEpsilonPhi();
        listaCompletaVectores.addAll(aaf.getEpsilonTetha());
        listaCompletaVectores.sort(Comparator.comparingInt(Vector::getIndex));

        for (Vector v:
                listaCompletaVectores) {
            System.out.println(v.getStringToGraph());
        }
        //System.out.println(aaf.getEpsilonPhi().get(0).getStringToCalculate());
    }
}
