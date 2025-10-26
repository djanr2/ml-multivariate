package unam.iimas.ia.ml.mlmultivariate.run;


import unam.iimas.ia.ml.mlmultivariate.faa.AlgoritmoAscensoRapido;
import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;

import java.math.BigDecimal;
import java.util.Arrays;

public class FAA {

    private static final double TOLERANCE = 0.02;

    public static void main(String[] args) {
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf;

        while (true) {
            aaf = new AlgoritmoAscensoRapido(file);
            aaf.run(file.getVectores(), file.getLowerLimitScale(), file.getUpperLimitScale());
            System.out.println(aaf.getBestCoeficients()[0][0]);
            if (aaf.getBestCoeficients()[0][0].abs().compareTo(new BigDecimal(TOLERANCE))<= 0){
                break;
            }
        }
        System.out.println("---------------------");
        Matrix.print(aaf.getBestCoeficients());
        AlgoritmoAscensoRapido.printVectores(aaf.getEpsilonThetha());
        AlgoritmoAscensoRapido.printVectores(aaf.getEpsilonPhi());
        System.out.println(aaf.getEpsilonPhi().get(0).getStringToCalculate());
    }
}
