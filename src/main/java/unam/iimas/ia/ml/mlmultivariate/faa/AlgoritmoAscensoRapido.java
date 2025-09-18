package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.matrix.MatrixObject;
import unam.iimas.ia.ml.mlmultivariate.model.Modelo;
import unam.iimas.ia.ml.mlmultivariate.model.Precision;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class AlgoritmoAscensoRapido {

    private static final int PRECISION = Precision.MIN_PRECISION;
    private static final int RHO = Precision.RHO;
    private static final RoundingMode ROUNDING_MODE = Precision.ROUNDING_MODE;

    //file is filled on constructor AlgoritmoAscensoRapido
    private final LoadFile file;

    public AlgoritmoAscensoRapido(){
        this.file = new LoadFile();
    }

    public static void main(String[] args) {
        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido();
        aaf.run(aaf);
    }

    public void run(AlgoritmoAscensoRapido aaf){

        Modelo m = Modelo.getCustomModel();

        //TODO sTART WORKING WITH VECTORS ARRAYS INSTEAD OF BIGDECIMAL ARRAY

        List<BigDecimal[]> d = aaf.getFile().getVectores();

        List<BigDecimal[]> p = mapToPolynomial(d,m);

        List<BigDecimal[]> r = stabilizeVectors(p,m);

        List<BigDecimal[]> s = scaleVectorsZeroToOne(r,m);

        List<Vector> epsilonPhi = mapToVectors(s,m);
        //TODO PARA QUE SACA LA MATRIZ TRANSPUESTA.
        //List<Vector> epsilonTetha   = getOrderVectorsToEvaluate(epsilonPhi);
        //List<Vector> epsilonTetha   = getRandomVectorsToEvaluate(epsilonPhi);

        int[] customVectors = {18,15,14,5};
        List<Vector> epsilonTetha   = getCustomVectorsToEvaluate(epsilonPhi, customVectors);

        List<Vector> minMaxEquation = getAMatrix(epsilonTetha, m);

        BigDecimal[] solution = Matrix.getGaussiaSolution(new MatrixObject(minMaxEquation));

        for (BigDecimal x:
             solution) {
            System.out.println(x);
        }

        printVectores(minMaxEquation);

    }

    private BigDecimal[] addEpsilonThetaErrorToMinMaxCoeficients(BigDecimal internalErrorEpsilonTetha, BigDecimal[] minMaxCoeficients){
        BigDecimal[] minMaxError = new BigDecimal[minMaxCoeficients.length + 1];
        minMaxError[0] = internalErrorEpsilonTetha;
        System.arraycopy(minMaxCoeficients, 0, minMaxError, 1, minMaxCoeficients.length);
        return minMaxError;
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
                        divide((b.subtract(a)), PRECISION, ROUNDING_MODE);
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
    public LoadFile getFile() {
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
        while (vectoresRandom.size()!=vectores.get(0).getVector().length){
            index = random.nextInt(0,vectores.size());
            vectoresRandom.add(vectores.get(index));
            vectores.remove(index);
        }
        return vectoresRandom;
    }

    public List<Vector> getCustomVectorsToEvaluate(List<Vector> vectores, int[] index){
        List<Vector> vectoresCustom =new ArrayList<>();
        int requiredNumVector= vectores.get(0).getVector().length;
        if(!(requiredNumVector == index.length)){
            String message = "El numero de vectores tiene que ser: " + requiredNumVector + " se introdujeron: "
                    + index.length + "";
            throw new RuntimeException(message);
        }
        for (int i:
                index ) {
            vectoresCustom.add(vectores.get(i));
        }
        return vectoresCustom;
    }

    public List<Vector> getOrderVectorsToEvaluate(List<Vector> vectores){
        List<Vector> vectoresRandom =new ArrayList<>();
        while (vectoresRandom.size()!=vectores.get(0).getVector().length){
            vectoresRandom.add(vectores.get(0));
            vectores.remove(0);
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

    public static void  print(List<BigDecimal[]> vectores){
        printBigDecimalsVectors(vectores);
    }

    public static List<BigDecimal[]> stabilizeVectors(List<BigDecimal[]> vectores, Modelo m){
        List<BigDecimal[]> vectores_ = new ArrayList<>();
        m.eraseLimits();
        for (BigDecimal[] v:
                vectores) {
            BigDecimal[] vector = new BigDecimal[v.length];
            for (int i = 0; i < v.length -1; i++) {
                vector[i] = v[i].add(getNoise()).setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
            }
            vector[v.length-1] = v[v.length-1];
            m.calculateNewLimits(vector);
            vectores_.add(vector);
        }
        return vectores_;
    }

    private List<Vector> getAMatrix(List<Vector> epsilonTetha, Modelo m){
        int lastCofactorSign= -1;
        int matrixSize = epsilonTetha.size();
        BigDecimal[][] deltaSigns = new BigDecimal[matrixSize][matrixSize];
        List<Vector> minMaxEquation = new ArrayList<>();
        BigDecimal[] cofactor = new BigDecimal[matrixSize];
        BigDecimal[][] equation = new BigDecimal[matrixSize][matrixSize];

        for (int row = 0; row < epsilonTetha.size(); row++) {
            deltaSigns[row][0]= BigDecimal.ONE;
            for (int col = 0; col < matrixSize-1; col++) {
                deltaSigns[row][col + 1] =  epsilonTetha.get(row).getVector()[col];
            }
        }

        for (int row = 0; row < matrixSize; row++) {
            cofactor[row] = Matrix.getCofactor(row, 0, deltaSigns);
        }

        for (int i = 1; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                equation[i-1][j] = cofactor[j].multiply(deltaSigns[j][i]).setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
                if( j == (matrixSize-1) && lastCofactorSign == -1) { //last sign == -1 if apply
                    equation[i - 1][j] = equation[i - 1][j].multiply(new BigDecimal(-1));
                }
            }
            minMaxEquation.add(new Vector(i, m, equation[i-1]));
        }

        BigDecimal[] solution = Matrix.getGaussiaSolution(new MatrixObject(minMaxEquation));

        for (int i = 0; i < solution.length; i++) {
            deltaSigns[i][0] = (solution[i].compareTo(BigDecimal.ZERO)>0)?BigDecimal.ONE:new BigDecimal(-1);
        }
        deltaSigns[solution.length][0] = new BigDecimal(lastCofactorSign);

        minMaxEquation.clear();

        for (int i = 0; i < epsilonTetha.size(); i++) {
            epsilonTetha.get(i).getVector();
            BigDecimal[] expanded = new BigDecimal[epsilonTetha.get(i).getVector().length+1];
            expanded[0] = deltaSigns[i][0];
            System.arraycopy( epsilonTetha.get(i).getVector(), 0, expanded, 1,  epsilonTetha.get(i).getVector().length);
            minMaxEquation.add(new Vector(epsilonTetha.get(i).getIndex(), m, expanded));
        }

        return minMaxEquation;
    }
    private void codeToSave(){
          /*
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

        BigDecimal internalErrorEpsilonTetha = epsilonTetha_.peek().getError();
        BigDecimal externalErrorEpsilonPhi = epsilonPhi_.peek().getError();

        while (!epsilonTetha_.isEmpty()) {
            System.out.println(epsilonTetha_.poll());
        }
        System.out.println();

        while (!epsilonPhi_.isEmpty()) {
            System.out.println(epsilonPhi_.poll());
        }

        for (Vector v:
                epsilonPhi_) {
            System.out.println(v);
        }
         */
    }
}
