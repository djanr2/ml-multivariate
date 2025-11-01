package unam.iimas.ia.ml.mlmultivariate.run;


import unam.iimas.ia.ml.mlmultivariate.faa.AlgoritmoAscensoRapido;
import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;

public class FAA {

    private static final double TOLERANCE = 1;

    public static void main(String[] args) {
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf;

        while (true) {
            aaf = new AlgoritmoAscensoRapido(file);
            aaf.run(file.getVectores(), file.getLowerLimitScale(), file.getUpperLimitScale());
            //System.out.println(aaf.getBestCoeficients()[0][0]);
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

        for (Vector v:
        aaf.getEpsilonPhi()) {
            System.out.println(v.getStringToGraph());
        }
        //System.out.println(aaf.getEpsilonPhi().get(0).getStringToCalculate());
    }
}
