package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class AlgoritmoAscensoRapido {

    private static final int PRECISION = 20;

    public static void main(String[] args) {
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido ffa = new AlgoritmoAscensoRapido();
        List<BigDecimal[]> vectores = file.getVectores();

        for (BigDecimal[] vector:
                vectores ) {
                System.out.println(file.vectorToString(ffa.normalizeZeroToOne(vector,
                        file.getLowerLimitScale(),
                        file.getUpperLimitScale())));
            System.out.println(file.vectorToString(ffa.escaleAtoB(vector,
                    file.getLowerLimitScale(),
                    file.getUpperLimitScale())));
        }
    }

    private BigDecimal[] normalizeZeroToOne(BigDecimal[] vector,
                                BigDecimal[] lowerValues,
                                BigDecimal[] upperValues){
        for (int i = 0; i<vector.length;i++) {
            BigDecimal x =vector[i];
            BigDecimal a = lowerValues[i];
            BigDecimal b = upperValues[i];
            vector[i] = ((x.subtract(a))).
                        divide((b.subtract(a)), PRECISION, RoundingMode.HALF_UP);
        }
        return vector;
    }

    private BigDecimal[] escaleAtoB(BigDecimal[] vector,
                                            BigDecimal[] lowerValues,
                                            BigDecimal[] upperValues){

        for (int i = 0; i<vector.length;i++) {
            BigDecimal x =vector[i];
            BigDecimal a = lowerValues[i];
            BigDecimal b = upperValues[i];
            vector[i] = x.multiply(b.subtract(a)).add(a);
        }
        return vector;
    }

}
