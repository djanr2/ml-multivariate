package unam.iimas.ia.ml.mlmultivariate.faa;

import com.fasterxml.jackson.core.JsonToken;
import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.matrix.MatrixObject;
import unam.iimas.ia.ml.mlmultivariate.model.Modelo;
import unam.iimas.ia.ml.mlmultivariate.model.Precision;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;


import javax.swing.text.html.Option;
import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

public class AlgoritmoAscensoRapido {

    private static final int PRECISION = Precision.MIN_PRECISION;
    private static final int RHO = Precision.RHO;
    private static final RoundingMode ROUNDING_MODE = Precision.ROUNDING_MODE;
    private static final int MIN_ERROR_EXPECTED = 1;
    private static Modelo m;
    private static int seed = 19;
    private static Random random = new Random(seed);
    Scanner sc = new Scanner(System.in);


    //file is filled on constructor AlgoritmoAscensoRapido
    private final LoadFile file;

    public AlgoritmoAscensoRapido(){
        this.file = new LoadFile("perfectfit");
    }

    public static void main(String[] args) {
        //Aqui se carga el archivo.
        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido();
        BigDecimal[][] c = aaf.run(aaf);
        System.out.println(m);
    }

    public BigDecimal[][] run(AlgoritmoAscensoRapido aaf) {
        BigDecimal[][] c;
        m = Modelo.getCustomModel();

        List<BigDecimal[]> d = aaf.getFile().getVectores();
        //print(d);
        //m.setLowerLimitScale(aaf.getFile().getLowerLimitScale());
        //m.setUpperLimitScale(aaf.getFile().getUpperLimitScale());
        //d = stabilizeVectors(d, m);
        //List<BigDecimal[]> e = scaleVectorsZeroToOne(d,m);
        List<BigDecimal[]> p = mapToPolynomial(d, m);

        List<BigDecimal[]> s = stabilizeVectors(p, m);
        List<Vector> epsilonPhi = mapToVectors(s, m);
        //printVectores(epsilonPhi);
        //List<Vector> epsilonTetha   = getOrderVectorsToEvaluate(epsilonPhi);
        List<Vector> epsilonTetha   = getRandomVectorsToEvaluate(epsilonPhi);

        //int[] customVectors = {19, 15, 14, 5};
        //int[] customVectors = {2, 23,3, 18};
        //int[] customVectors = {7, 11, 23, 18};
        //int[] customVectors = {6, 1, 7, 9};
        //List<Vector> epsilonTetha = getCustomVectorsToEvaluate(epsilonPhi, customVectors);
        List<Vector> minMaxEquation = getMinMaxSigns(epsilonTetha, m);

        MatrixObject matrixA = new MatrixObject(minMaxEquation);
        BigDecimal[][] b = Matrix.invertMatrix(matrixA.getMatrix());
        //printVectores(epsilonTetha);
        //printVectores(epsilonPhi);
        //--------------------loop
        //System.out.println("-------------------------------------");
        //TODO Modificar esta parte del codigo para no volver a generar
        // el Matrix Object. Y solo obtener el vector solucion derivado del sqap
        minMaxEquation = getMinMaxSigns2(epsilonTetha, m);

        matrixA = new MatrixObject(minMaxEquation);
        while (true){
            c = getCoeficientsAndEpsilonTetha(b, matrixA.getVectorSolution());
            m.setSolutionCoeficientes(c);
            Vector epsilonPhiVector = getEpsilonPhiVector(epsilonPhi);
            //System.out.println("eT: "+c[0][0].abs()+" eP: "+epsilonPhiVector.getError());
            if(c[0][0].abs().compareTo(epsilonPhiVector.getError())>=0){
                break;
            }
            BigDecimal[][] lamdas = getLamdas(epsilonPhiVector.getMiMaxSignVector(), b);
            BigDecimal[][] betas = getBetas(lamdas, Matrix.getMatrixRow(b, 0),  epsilonPhiVector.getSign());
            int indexBeta = getBetaIndex(betas);
            swapVector(epsilonTetha, epsilonPhi, epsilonTetha.get(indexBeta), epsilonPhiVector);
            b = getInverseFromLamda(epsilonPhiVector.getMiMaxSignVector(), b, indexBeta);
        }

        return c;
    }

    private BigDecimal[][] getInverseFromLamda(BigDecimal[][] epsilonPhi, BigDecimal[][] b, int indexBeta){
       // epsilonPhi[0][0] = BigDecimal.ONE.multiply(new BigDecimal(epsilonPhi[0][0].signum()));
        //Matrix.printMatrix(epsilonPhi);
        BigDecimal[][] lambdaBeta = Matrix.mul(epsilonPhi, Matrix.getMatrixCol(b, indexBeta));
        int m = epsilonPhi[0].length;
        BigDecimal[][] inverseB = new BigDecimal[m][m];
        for (int row = 0; row < m ; row++) {
            inverseB[row][indexBeta]=b[row][indexBeta].divide(lambdaBeta[0][0], PRECISION, ROUNDING_MODE);
        }
        for (int col = 0; col < m; col++) {
            BigDecimal[][] vectorialProduct = Matrix.mul(epsilonPhi, Matrix.getMatrixCol(b, col));
            for (int row = 0; row < m; row++) {
                if(col != indexBeta){
                    inverseB[row][col] = b[row][col].subtract(vectorialProduct[0][0].multiply(inverseB[row][indexBeta]))
                     .setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
                }
            }
        }
        return inverseB;
    }

    public void swapVector( List<Vector> epsilonTheta,List<Vector> epsilonPhi, Vector epsilonThetaVector, Vector epsilonPhiVector){
        //System.out.println(" swap: {["+epsilonThetaVector.getIndexEpsilonTheta()+"] "+ epsilonThetaVector +"    \n       " + epsilonPhiVector + "}");
        //System.out.println("["+epsilonThetaVector.getIndexEpsilonTheta()+"]");
        epsilonPhi.remove(epsilonPhiVector.setIndexEpsilonTheta(epsilonThetaVector.getIndexEpsilonTheta()));
        epsilonPhi.add(epsilonThetaVector);
        epsilonTheta.set(epsilonThetaVector.getIndexEpsilonTheta(),epsilonPhiVector);
    }





    public Vector getEpsilonPhiVector(List<Vector> epsilonPhi){
        PriorityQueue<Vector> epsilonPhi_ = new PriorityQueue<>();
        for (Vector v:
                epsilonPhi ) {
            epsilonPhi_.add(v.evaluate());
        }
        /*
        while (!epsilonPhi_.isEmpty()) {
            System.out.println(epsilonPhi_.poll());
        }
        System.out.println(epsilonPhi_.peek());
        */
        return epsilonPhi_.peek();
    }

    private BigDecimal[][] getCoeficientsAndEpsilonTetha(BigDecimal[][] matrixB, BigDecimal[][] fx){
        //System.out.println("CRAMER RULE");
        return Matrix.mul(matrixB, fx);
    }

    private BigDecimal[][] getLamdas(BigDecimal[][] vector, BigDecimal[][] matrixB){
        return Matrix.mul(vector, matrixB);
    }

    private BigDecimal[][] getBetas(BigDecimal[][] lamdas, BigDecimal[][] errorRow, int sign){
        BigDecimal[][] betas = new BigDecimal[1][lamdas[0].length];
        //System.out.println("Sign: "+ sign);
        for (int i = 0; i < lamdas[0].length; i++) {
            betas[0][i] = lamdas[0][i].multiply(new BigDecimal(sign)).
                    divide(errorRow[0][i], PRECISION, ROUNDING_MODE);
        }
        return betas;
    }
    private int getBetaIndex(BigDecimal[][] betas){
        int k=-1;
        BigDecimal maxLamda = betas[0][0];
        for (int i = 0; i < betas[0].length; i++) {
            if(maxLamda.compareTo(betas[0][i])<=0){
                maxLamda = betas[0][i];
                k=i;
            }
        }
        return k;
    }

    private BigDecimal[] addEpsilonThetaErrorToMinMaxCoeficients(BigDecimal internalErrorEpsilonTetha, BigDecimal[] minMaxCoeficients){
        BigDecimal[] minMaxError = new BigDecimal[minMaxCoeficients.length + 1];
        minMaxError[0] = internalErrorEpsilonTetha;
        System.arraycopy(minMaxCoeficients, 0, minMaxError, 1, minMaxCoeficients.length);
        return minMaxError;
    }


    private static List<BigDecimal[]> scaleVectorsZeroToOne(List<BigDecimal[]> vectores,
                                Modelo modelo){
        List<BigDecimal[]> scaledVectors = new ArrayList<>();
        for (BigDecimal[] vectorOriginal:
                vectores) {
            BigDecimal[] vector = new BigDecimal[vectorOriginal.length];
            for (int i = 0; i<vector.length;i++) {
                BigDecimal x = new BigDecimal(vectorOriginal[i].toString());
                BigDecimal a = modelo.getLowerLimitScale()[i];
                BigDecimal b = modelo.getUpperLimitScale()[i];
                if(a.compareTo(x)==0){
                    vector[i] = BigDecimal.ZERO;
                } else {
                    vector[i] = ((x.subtract(a))).
                            divide((b.subtract(a)), PRECISION, ROUNDING_MODE);
                    vector[i] = (vector[i].compareTo(BigDecimal.ONE)==0)?BigDecimal.ONE:vector[i];
                }
            }
            scaledVectors.add(vector);
        }
        return scaledVectors;
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
        //int mult = (random.nextBoolean())?1:-1;
        return BigDecimal.valueOf(random.nextDouble()).
                multiply(new BigDecimal("1e-"+(RHO)));
    }

    private  static  List<BigDecimal[]> mapToPolynomial(List<BigDecimal[]> vectors, Modelo modelo){
        modelo.eraseLimits();
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
        int index=0;
        int i =0;
        while (vectoresRandom.size()!=vectores.get(0).getVector().length){
            index = random.nextInt(0,vectores.size());
            vectoresRandom.add(vectores.get(index).setIndexEpsilonTheta(i++));
            vectores.remove(index);
        }
        return vectoresRandom;
    }

    public List<Vector> getCustomVectorsToEvaluate(List<Vector> vectores, int[] indexes){
        List<Vector> vectoresCustom =new ArrayList<>();
        int requiredNumVector= vectores.get(0).getVector().length;
        if(!(requiredNumVector == indexes.length)){
            String message = "El numero de vectores tiene que ser: " + requiredNumVector + " se introdujeron: "
                    + indexes.length + "";
            throw new RuntimeException(message);
        }
        for (int i = 0;i<indexes.length;i++) {
            final int aux = i;
            Optional<Vector> vi = vectores.stream().filter( v -> v.getIndex() == indexes[aux]).findFirst();
            vectoresCustom.add(vi.get().setIndexEpsilonTheta(i));
            vectores.removeIf( v -> v.getIndex() == indexes[aux]);
        }
        return vectoresCustom;
    }

    public List<Vector> getOrderVectorsToEvaluate(List<Vector> vectores){
        List<Vector> vectoresOrdered =new ArrayList<>();
        int i= 0;
        while (vectoresOrdered.size()!=vectores.get(0).getVector().length){
            vectoresOrdered.add(vectores.get(0).setIndexEpsilonTheta(i++));
            vectores.remove(0);
        }
        return vectoresOrdered;
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
            System.out.print(v.getIndexEpsilonTheta()+": ");
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
                if(v[i].compareTo(BigDecimal.ZERO)==0){
                    vector[i] = v[i].add(getNoise()).abs().setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
                }else {
                    vector[i] = v[i].add(getNoise()).setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
                }
            }
            vector[v.length-1] = v[v.length-1];
            m.calculateNewLimits(vector);
            vectores_.add(vector);
        }
        return vectores_;
    }

    private List<Vector> getMinMaxSigns(List<Vector> epsilonTetha, Modelo m){
        final int lastCofactorSign = -1;
        List<Vector> minMaxEquation = new ArrayList<>();

        BigDecimal[][] determinante = Matrix.getLeftDeterminant(epsilonTetha);
        BigDecimal[][] transpuesta = Matrix.transponer(determinante);
        BigDecimal[][] solution = Matrix.gaussianElimination(transpuesta);
        BigDecimal[] v_;
        for (int i = 0; i < solution.length; i++) {
            int sign = solution[i][0].signum();
            v_ = new BigDecimal[epsilonTetha.get(0).getVector().length+1];;
            v_[0] = BigDecimal.ONE.multiply(new BigDecimal(sign));
            System.arraycopy(epsilonTetha.get(i).getVector(), 0, v_,
                    1, epsilonTetha.get(i).getVector().length);
            minMaxEquation.add(new Vector(i, m, v_));
        }
        // adding last vector:
        v_ = new BigDecimal[epsilonTetha.get(0).getVector().length+1];
        v_[0] = BigDecimal.ONE.multiply(new BigDecimal(lastCofactorSign));
        System.arraycopy(epsilonTetha.get(epsilonTetha.size()-1).getVector(), 0, v_,
                1, epsilonTetha.get(epsilonTetha.size()-1).getVector().length);
        minMaxEquation.add(new Vector(epsilonTetha.size()-1, m, v_));
        return minMaxEquation;
    }

    private List<Vector> getMinMaxSigns2(List<Vector> epsilonTetha, Modelo m){
        List<Vector> minMaxEquation = new ArrayList<>();
        BigDecimal[][] determinante = Matrix.getLeftDeterminant(epsilonTetha);
        BigDecimal[][] cofactores = getCofactors(determinante);
        BigDecimal[] v_;
        for (int i = 0; i < cofactores.length; i++) {
            int sign = cofactores[i][0].signum();
            v_ = new BigDecimal[epsilonTetha.get(0).getVector().length+1];;
            v_[0] = BigDecimal.ONE.multiply(new BigDecimal(sign));
            System.arraycopy(epsilonTetha.get(i).getVector(), 0, v_,
                    1, epsilonTetha.get(i).getVector().length);
            minMaxEquation.add(new Vector(i, m, v_));
        }
        return minMaxEquation;
    }

    private BigDecimal[][] getCofactors(BigDecimal[][] m){
        BigDecimal[][] mxm = Matrix.addFirstColumnONES(m);
        BigDecimal[][] cofactors = new BigDecimal[m.length][1];
        for (int i = 0; i < m.length ; i++) {
            cofactors[i][0] = Matrix.getCofactor(i, 0, mxm);
        }
        return cofactors;
    }
}
