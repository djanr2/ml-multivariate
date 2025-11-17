package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;
import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;
import unam.iimas.ia.ml.mlmultivariate.matrix.MatrixObject;
import unam.iimas.ia.ml.mlmultivariate.model.*;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;


import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.List;

public class AlgoritmoAscensoRapido {

    private static final int RHO = Precision.RHO;
    private static final MathContext MC = new MathContext(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
    private final Random random;
    private final Map<IdSwap, Swap> swaps;
    private final LoadFile file;
    private final Long seed;
    private Modelo m;
    private BigDecimal[][] bestCoeficients;
    private List<Vector> epsilonPhi;
    private List<Vector> epsilonTetha;
    private BigDecimal bestEpsilonPhiValue = BigDecimal.ONE;

    public AlgoritmoAscensoRapido(LoadFile file){
        this.file = file;
        swaps = new HashMap<>();
        Random localRandom = new Random();
        seed = localRandom.nextLong();
        random = new Random(seed);
        m = Modelo.getRandomModelo(random, 9, 15, file.getNumeroVariables());
    }

    public AlgoritmoAscensoRapido(long seed_, LoadFile file){
        this.file = file;
        swaps = new HashMap<>();
        seed = seed_;
        random = new Random(seed);
        m = Modelo.getRandomModelo(random, 9, 15, file.getNumeroVariables());
    }

    public static void main(String[] args) {
        //Aqui se carga el archivo.
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido(file);
        aaf.run(file.getVectores(), file.getLowerLimitScale(), file.getUpperLimitScale());
        System.out.println(aaf.getModelo());
        Matrix.print(aaf.getBestCoeficients());
    }

    public void run(List<BigDecimal[]> vectores, BigDecimal[] lowerLimitToScale,BigDecimal[] upperLimitToScale) {
        BigDecimal menor = new BigDecimal("1E1000");
        List<BigDecimal[]> d = vectores;
        m.setOriginalLowerLimitScale(lowerLimitToScale);
        m.setOriginalUpperLimitScale(upperLimitToScale);
        List<BigDecimal[]> e = scaleVectorsZeroToOne(d);
        List<BigDecimal[]> p = mapToPolynomial(e);
        List<BigDecimal[]> s = stabilizeVectors(p);
        List<Vector> epsilonPhi = mapToVectors(s);
        List<Vector> epsilonTetha   = getRandomVectorsToEvaluate(epsilonPhi);
        List<Vector> minMaxEquation = getMinMaxSigns(epsilonTetha);
        MatrixObject matrixA = new MatrixObject(minMaxEquation);
        BigDecimal[][] b = Matrix.invertMatrix(matrixA.getMatrix());
        BigDecimal[][] solution = matrixA.getVectorSolution();
        BigDecimal[][] c;
        while (true){
            c = getCoeficientsAndEpsilonTetha(b, solution);
            m.setSolutionCoeficientes(c);
            Vector epsilonPhiVector = getEpsilonPhiVector(epsilonPhi);
            BigDecimal[][] lamdas = getLamdas(epsilonPhiVector.getMiMaxSignVector(), b);
            BigDecimal[][] betas = getBetas(lamdas, Matrix.getMatrixRow(b, 0),  epsilonPhiVector.getSign());
            int indexBeta = getBetaIndex(betas);
            //System.out.println("eT: "+c[0][0].abs()+" eP: "+epsilonPhiVector.getError());
            if(epsilonPhiVector.getError().compareTo(menor)<0){
                menor = new BigDecimal(epsilonPhiVector.getError().toString());
                saveBestCoeficients(c);
                bestEpsilonPhiValue = new BigDecimal(epsilonPhiVector.getError().toString());
            }
            if(c[0][0].abs().compareTo(epsilonPhiVector.getError())>=0){
                //System.out.println("Convergio:");
                //System.out.println(" eT: "+c[0][0].abs()+" < eP: "+epsilonPhiVector.getError());
                //System.out.println(getBestEpsilonPhiValue());
                break;
            }else if(wasSwapped(epsilonTetha, epsilonTetha.get(indexBeta),epsilonPhiVector)){
                //System.out.println("Cycled");
                break;
            }
            //TODO Validar cual es el mejor signo para tomar. El del error, o el de las betas.
            //b = getInverseFromLamda(epsilonPhiVector.getMiMaxSignVector(), b, indexBeta);
            b = getInverseFromLamda(epsilonPhiVector.getMiMaxSignVector(b[0][indexBeta].signum()), b, indexBeta);
            swapVector(epsilonTetha, epsilonPhi, epsilonTetha.get(indexBeta), epsilonPhiVector);
            solution[indexBeta][0] = epsilonTetha.get(indexBeta).getVector()[epsilonTetha.get(indexBeta).getVector().length-1];

        }
        setEpsilonPhi(epsilonPhi);
        setEpsilonTetha(epsilonTetha);
        m.setSolutionCoeficientes(getBestCoeficients());
    }

    private BigDecimal[][] getInverseFromLamda(BigDecimal[][] epsilonPhi, BigDecimal[][] b, int indexBeta){
        BigDecimal[][] lambdaBeta = Matrix.mul(epsilonPhi, Matrix.getMatrixCol(b, indexBeta));
        int m = epsilonPhi[0].length;
        BigDecimal[][] inverseB = new BigDecimal[m][m];
        for (int row = 0; row < m ; row++) {
            inverseB[row][indexBeta]=b[row][indexBeta].divide(lambdaBeta[0][0], MC);
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
        //System.out.println("["+epsilonThetaVector.getIndex()+"]<->["+epsilonPhiVector.getIndex()+"]");
        List<Integer> idsVectoresEpsilonTheta = new ArrayList<>();
        for (Vector v:
                epsilonTheta) {
            idsVectoresEpsilonTheta.add(v.getIndex());
        }
        // el orden debe de mantenerse para el Set de wasswapped()
        //System.out.println(">"+Arrays.toString(idVectoresEpsilonTheta.toArray()));
        Swap swap =  swaps.get(IdSwap.of(epsilonThetaVector.getIndex(), epsilonPhiVector.getIndex()));
        if(swap!=null){
            if(swap.notContains(idsVectoresEpsilonTheta)){
                swap.add(idsVectoresEpsilonTheta);
            }
        }else {
            swap = new Swap();
            swap.add(idsVectoresEpsilonTheta);
            swaps.put(IdSwap.of(epsilonThetaVector.getIndex(), epsilonPhiVector.getIndex()), swap);
        }

        epsilonPhi.remove(epsilonPhiVector.setIndexEpsilonTheta(epsilonThetaVector.getIndexEpsilonTheta()));
        epsilonPhi.add(epsilonThetaVector);
        epsilonTheta.set(epsilonThetaVector.getIndexEpsilonTheta(),epsilonPhiVector);
    }

    public boolean wasSwapped(List<Vector> epsilonTheta, Vector epsilonThetaVector, Vector epsilonPhiVector) {
        List<Integer> idsIndex = new ArrayList<>();
        // Agregar los índices de los vectores en orden
        for (Vector v : epsilonTheta) {
            idsIndex.add(v.getIndex());
        }
        // Buscar si ya se intercambió (ahora swaps debe ser Set<List<Integer>> o similar)
        Swap swap = swaps.get(IdSwap.of(epsilonThetaVector.getIndex(), epsilonPhiVector.getIndex()));
        if (swap != null) {
            return swap.contains(idsIndex);
        }else{
            return false;
        }
    }

    public void printSwaps(){
        for (Map.Entry<IdSwap, Swap> entrada : swaps.entrySet()) {
            System.out.println(entrada.getKey());
            for (List<Integer> s:
                    entrada.getValue().getSetListas()) {
                System.out.print("\t");
                System.out.println(Arrays.toString(s.toArray()));
            }
        }
    }

    public Vector getEpsilonPhiVector(List<Vector> epsilonPhi){
        PriorityQueue<Vector> epsilonPhi_ = new PriorityQueue<>();
        for (Vector v:
                epsilonPhi ) {
            epsilonPhi_.add(v.evaluate());
        }
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
        String s = (sign<0)?"-":"";

        for (int i = 0; i < lamdas[0].length; i++) {
            betas[0][i] = lamdas[0][i].multiply(new BigDecimal(sign)).
                    divide(errorRow[0][i], MC);
        }
        return betas;
    }
    private int getBetaIndex(BigDecimal[][] betas){
        int k=-1;
        BigDecimal maxBeta = betas[0][0];
        for (int i = 0; i < betas[0].length; i++) {
            if(maxBeta.compareTo(betas[0][i])<=0){
                maxBeta = betas[0][i];
                k=i;
            }
        }
        return k;
    }

    private List<BigDecimal[]> scaleVectorsZeroToOne(List<BigDecimal[]> vectores){
        List<BigDecimal[]> scaledVectors = new ArrayList<>();
        for (BigDecimal[] vectorOriginal:
                vectores) {
            BigDecimal[] vector = new BigDecimal[vectorOriginal.length];
            for (int i = 0; i<vector.length;i++) {
                BigDecimal x = new BigDecimal(vectorOriginal[i].toString());
                BigDecimal a = m.getOriginalLowerLimitScale()[i];
                BigDecimal b = m.getOriginalUpperLimitScale()[i];
                if(a.compareTo(x)==0){
                    vector[i] = BigDecimal.ZERO;
                } else {
                    vector[i] = ((x.subtract(a))).
                            divide((b.subtract(a)), MC);
                    vector[i] = (vector[i].compareTo(BigDecimal.ONE)==0)?BigDecimal.ONE:vector[i];
                }
            }
            scaledVectors.add(vector);
        }
        return scaledVectors;
    }


    private BigDecimal getNoise(){
        return BigDecimal.valueOf(random.nextDouble()).
                multiply(new BigDecimal("1e-"+(RHO)));
    }

    private List<BigDecimal[]> mapToPolynomial(List<BigDecimal[]> vectors){
        List<BigDecimal[]> vectorsToPolynomial = new ArrayList<>();
        for (BigDecimal[] vector:
        vectors) {
            vectorsToPolynomial.add(m.getPolynomialVector(vector));
        }
        return vectorsToPolynomial;
    }

    public String getStringFromVector(BigDecimal[] vector){
        return Arrays.toString(Arrays.stream(vector)
                .map(BigDecimal::toString)
                .toArray(String[]::new));
    }
    
    public List<Vector> mapToVectors(List<BigDecimal[]> vectores){
        List<Vector> vectores_ = new ArrayList<>();
        for (int i = 0; i < vectores.size(); i++) {
            vectores_.add(new Vector(i, m, vectores.get(i)));
        }
        return vectores_;
    }

    public List<Vector> getRandomVectorsToEvaluate(List<Vector> vectores){
        //Se agrega tambien los valores de orden de epsilonTetha
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

    public void printVectores(List<Vector> vectores){
        for (Vector v:
                vectores) {
            System.out.print(v.getIndexEpsilonTheta()+": ");
            System.out.println(v);
        }
        System.out.println();
    }

    public void printBigDecimalsVectors(List<BigDecimal[]> vectores){
        for (BigDecimal[] v:
                vectores) {
            System.out.println(getStringFromVector(v));
        }
        System.out.println();
    }

    public void print(List<BigDecimal[]> vectores){
        printBigDecimalsVectors(vectores);
    }

    public List<BigDecimal[]> stabilizeVectors(List<BigDecimal[]> vectores){
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

    private List<Vector> getMinMaxSigns(List<Vector> epsilonTetha){
        List<Vector> minMaxEquation = new ArrayList<>();
        BigDecimal[][] determinante = Matrix.getLeftDeterminant(epsilonTetha);
        BigDecimal[][] traspuesta = Matrix.transponer(determinante);
        BigDecimal[][] solution = Matrix.gaussianElimination(traspuesta);
        for (int i = 0; i < epsilonTetha.size(); i++) {
            Vector vectorOriginal = epsilonTetha.get(i);
            BigDecimal[] augmentedData = new BigDecimal[epsilonTetha.size()+1];
            if(i<epsilonTetha.size()-1){
                augmentedData[0] = (solution[i][0].signum()<0)?new BigDecimal(-1):BigDecimal.ONE;
            }else{
                augmentedData[0] = new BigDecimal(-1); // se le coloca el -1 por default
            }
            System.arraycopy(vectorOriginal.getVector(), 0, augmentedData, 1, epsilonTetha.size());
            Vector newVector = new Vector(vectorOriginal.getIndex(), vectorOriginal.getModelo(), augmentedData);
            minMaxEquation.add(newVector.setIndexEpsilonTheta(i));
        }
        return minMaxEquation;
    }

    public Modelo getModelo() {
        return m;
    }

    public void saveBestCoeficients(BigDecimal[][] coeficients){
        this.bestCoeficients = coeficients;
    }

    public BigDecimal[][] getBestCoeficients() {
        return this.bestCoeficients;
    }

    public List<Vector> getEpsilonPhi() {
        return epsilonPhi;
    }

    public void setEpsilonPhi(List<Vector> epsilonPhi) {
        this.epsilonPhi = epsilonPhi;
    }

    public BigDecimal getBestEpsilonPhiValue() {
        return bestEpsilonPhiValue;
    }

    public List<Vector> getEpsilonTetha() {
        return epsilonTetha;
    }

    public void setEpsilonTetha(List<Vector> epsilonTetha) {
        this.epsilonTetha = epsilonTetha;
    }

    public Long getSeed() {
        return seed;
    }
}
