package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.model.Modelo;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AlgoritmoAscensoRapido {

    private static final int PRECISION = 20;

    private static boolean injectNoise = true;
    private LoadFile file;

    public static void main(String[] args) {
        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido();
        aaf.run(aaf);
    }

    public void run(AlgoritmoAscensoRapido aaf){
        Modelo m = Modelo.getRandomModelo(15,9,aaf.getDataFromFile().getNumeroVariables());
        System.out.print("modelo("+ m.getL()+"):");
        System.out.println(m);

        for (BigDecimal[] vector:
                mapToPolynomial(
                    getDataFromFile().getVectores()
                , m)){
            System.out.println(showVectorValues(vector));
        }
        System.out.println();
        System.out.println(showVectorValues(m.getUpperLimitScale()));
        System.out.println(showVectorValues(m.getLowerLimitScale()));
        System.out.println();
        for (BigDecimal[] vector:
                stabilizeVectorsZeroToOne(mapToPolynomial(
                        getDataFromFile().getVectores()
                        , m),m.getLowerLimitScale(), m.getUpperLimitScale())){
            System.out.println(showVectorValues(vector));
        }


    }
    public AlgoritmoAscensoRapido(){
        this.file = new LoadFile();
    }

    private static List<BigDecimal[]> stabilizeVectorsZeroToOne(List<BigDecimal[]> vectores,
                                BigDecimal[] lowerValues,
                                BigDecimal[] upperValues){
        List<BigDecimal[]> stabilizedVectors = new ArrayList<>();
        for (BigDecimal[] vectorOriginal:
        vectores) {
            BigDecimal[] vector = new BigDecimal[vectorOriginal.length];
            for (int i = 0; i<vector.length;i++) {
                BigDecimal x = new BigDecimal(vectorOriginal[i].toString());
                BigDecimal a = lowerValues[i];
                BigDecimal b = upperValues[i];
                vector[i] = vectorOriginal[i].add(getNoise());
                vector[i] = ((x.subtract(a))).
                        divide((b.subtract(a)), PRECISION, RoundingMode.HALF_UP);
            }
            stabilizedVectors.add(vector);
        }
        return stabilizedVectors;
    }


    private static  BigDecimal[] escaleAtoB(BigDecimal[] vector,
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
        Random random = new Random();
        int mult = (random.nextBoolean())?1:-1;
        return BigDecimal.valueOf(random.nextDouble()*mult).
                multiply(new BigDecimal("1e-"+(PRECISION-1)));
    }

    private  static  List<BigDecimal[]> mapToPolynomial(List<BigDecimal[]> vectors, Modelo modelo){
        List<BigDecimal[]> vectorsToPolynomial = new ArrayList<>();
        for (BigDecimal[] vector:
        vectors) {
            vectorsToPolynomial.add(modelo.getPolynomialVector(vector));
        }

        return vectorsToPolynomial;
    }
    public LoadFile getDataFromFile() {
        return file;
    }

    public String showVectorValues(BigDecimal[] vector){
        return Arrays.toString(Arrays.stream(vector)
                .map(BigDecimal::toString)
                .toArray(String[]::new));
    }
}
