package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.matrix.MatrixObject;
import unam.iimas.ia.ml.mlmultivariate.model.Modelo;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class AlgoritmoAscensoRapido {

    private static final int PRECISION = 20;
    private static final int RHO = 6;

    private LoadFile file;

    public static void main(String[] args) {
        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido();
        aaf.run(aaf);
    }

    public void run(AlgoritmoAscensoRapido aaf){
        Modelo m = Modelo.getCustomModel();
        List<BigDecimal[]> D = getDataFromFile().getVectores();
        List<BigDecimal[]> P = mapToPolynomial(D,m);
        List<BigDecimal[]> R = stabilizeVectors(P,m);
        List<BigDecimal[]> S = scaleVectorsZeroToOne(R,m);
        List<Vector> epsilonPhi = mapToVectors(S,m);
        List<Vector> epsilonTetha   = getRandomVectorsToEvaluate(epsilonPhi);

        printVectores(epsilonTetha);
        printVectores(epsilonPhi);

        BigDecimal[] solution =  Matrix.getGaussiaSolution(new MatrixObject(epsilonTetha));

        m.setCoeficientes(solution);

        PriorityQueue<Vector> epsilonTetha_ = new PriorityQueue<>();
        PriorityQueue<Vector> epsilonPhi_ = new PriorityQueue<>();

        for (Vector v:
             epsilonTetha ) {
            epsilonTetha_.add(v.evaluate());
        }

        for (Vector v:
                epsilonPhi ) {
            epsilonPhi_.add(v.evaluate());
        }

        while (!epsilonTetha_.isEmpty()) {
            System.out.println(epsilonTetha_.poll());
        }
        System.out.println();
        System.out.println();
        while (!epsilonPhi_.isEmpty()) {
            System.out.println(epsilonPhi_.poll());
        }
        System.out.println();

        for (Vector v:
                epsilonPhi_) {
            System.out.println(v);
        }


    }
    public AlgoritmoAscensoRapido(){
        this.file = new LoadFile();
    }

    private static List<BigDecimal[]> scaleVectorsZeroToOne(List<BigDecimal[]> vectores,
                                Modelo modelo){
        List<BigDecimal[]> stabilizedVectors = new ArrayList<>();
        for (BigDecimal[] vectorOriginal:
        vectores) {
            BigDecimal[] vector = new BigDecimal[vectorOriginal.length];
            for (int i = 0; i<vector.length;i++) {
                BigDecimal x = new BigDecimal(vectorOriginal[i].toString());
                BigDecimal a = modelo.getLowerLimitScale()[i];
                BigDecimal b = modelo.getUpperLimitScale()[i];
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
                multiply(new BigDecimal("1e-"+(RHO)));
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

    public static String showVectorValues(BigDecimal[] vector){
        return Arrays.toString(Arrays.stream(vector)
                .map(BigDecimal::toString)
                .toArray(String[]::new));
    }
    
    public static List<Vector> mapToVectors(List<BigDecimal[]> vectores, Modelo modelo){
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

    public static Modelo getCoeficientModel(BigDecimal[] gaussianSol, Modelo modelo){
        for (int i = 0; i < gaussianSol.length; i++) {
            modelo.getTerminos()[i].setCoeficiente(gaussianSol[i]);
        }
        return modelo;
    }

    public static void printVectores(List<Vector> vectores){
        for (Vector v:
                vectores) {
            System.out.println(v);
        }
        System.out.println();
    }
    public static void printBigDecimalsVectors(List<BigDecimal[]> vectores){
        for (BigDecimal[] v:
                vectores) {
            System.out.println(showVectorValues(v));
        }
        System.out.println();
    }

    public static List<BigDecimal[]> stabilizeVectors(List<BigDecimal[]> vectores,Modelo m){
        List<BigDecimal[]> vectores_ = new ArrayList<>();
        m.eraseLimits();
        for (BigDecimal[] v:
                vectores) {
            BigDecimal[] vector = new BigDecimal[v.length];
            for (int i = 0; i < v.length -1; i++) {
                vector[i] = v[i].add(getNoise());
            }
            vector[v.length-1] = v[v.length-1];
            m.calculateNewLimits(vector);
            vectores_.add(vector);
        }
        return vectores_;
    }
}
