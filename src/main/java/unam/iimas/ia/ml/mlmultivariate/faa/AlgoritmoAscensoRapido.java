package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.model.L;
import unam.iimas.ia.ml.mlmultivariate.model.Modelo;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlgoritmoAscensoRapido {

    private static final int PRECISION = 20;

    private boolean injectNoise = true;
    private static Random random;
    private LoadFile file;

    public static void main(String[] args) {

        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido();
        aaf.run(aaf);

    }

    public void run(AlgoritmoAscensoRapido aaf){

    }
    public AlgoritmoAscensoRapido(){
        this.file = new LoadFile();
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
            if (injectNoise){
                vector[i] = vector[i].add(getNoise());
            }
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

    private static BigDecimal getNoise(){
        random = new Random();
        return BigDecimal.valueOf(random.nextDouble()).
                multiply(new BigDecimal("1e-"+(PRECISION-1)));
    }

    private static List<BigDecimal[]> mapToPolynomial(List<BigDecimal[]> vectors, Modelo modelo){
        List<BigDecimal[]> vectorsToPolynomial = new ArrayList<>();
        for (BigDecimal[] vector:
        vectors) {
           // BigDecimal[] newVectorMapTOPolynomial = new BigDecimal[modelo.getTerminos().size()+1];
            BigDecimal value = new BigDecimal(1);
        }
        return vectorsToPolynomial;
    }
    public LoadFile getFile() {
        return file;
    }
}
