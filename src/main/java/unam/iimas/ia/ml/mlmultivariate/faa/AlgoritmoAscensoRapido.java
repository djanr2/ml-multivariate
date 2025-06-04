package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.matrix.MatrixObject;
import unam.iimas.ia.ml.mlmultivariate.model.Modelo;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AlgoritmoAscensoRapido {

    private static final int PRECISION = 20;

    private LoadFile file;

    public static void main(String[] args) {
        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido();
        aaf.run(aaf);
    }

    public void run(AlgoritmoAscensoRapido aaf){
        Modelo m = Modelo.getCustomModel();
        //prepare data;
        List<Vector> vectores= mapToVectors(m,stabilizeVectorsZeroToOne(mapToPolynomial(
                getDataFromFile().getVectores()
                , m),m.getLowerLimitScale(), m.getUpperLimitScale()));

        System.out.println(m);
        
        List<Vector> vectoresToEvaluate = getRandomVectorsToEvaluate(vectores);

        for (Vector v:
             vectores) {
            System.out.println(v);
        }
        System.out.println();

        for (Vector v:
                vectoresToEvaluate) {
            System.out.println(v);
        }

        System.out.println("------------------------------------------------------");

        BigDecimal[] solution= Matrix.getGaussiaSolution(new MatrixObject(vectoresToEvaluate));

        System.out.println(Arrays.toString(solution));


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
    
    public static List<Vector> mapToVectors(Modelo modelo, List<BigDecimal[]> vectores){
        List<Vector> vectores_ = new ArrayList<>();
        for (int i = 0; i < vectores.size(); i++) {
            vectores_.add(new Vector(i, modelo, vectores.get(i)));
        }
        return vectores_;
    }

    public List<Vector> getRandomVectorsToEvaluate(List<Vector> vectores){
        List<Vector> vectoresRandom =new ArrayList<>();
        Random random = new Random();
        int index=0;
        while (vectoresRandom.size()!=vectores.get(0).getVector().length-1){
            index = random.nextInt(0,vectores.size());
            vectoresRandom.add(vectores.get(index));
            vectores.remove(index);
        }
        return vectoresRandom;
    }
}
