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
    private static final MathContext MC = new MathContext(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);;
    private static final double MIN_ERROR_EXPECTED = 0.1;
    private static final double ALPHA =1.0; // α>0  peso del cociente (más grande => favorece más n/d
    private static final double BETA =0.5; // penaliza magnitud de n + d
    private static final double GAMMA =1.0; // γ≥0  fuerza del bonus por denominador pequeño.
    private static final double DELTA =3.0; // δ>0 — cómo decae el bonus con d (si δ=1 es inversamente proporcional).
    private static Random random = new Random();
    private final Map<IdSwap, Swap> swaps;
    private Modelo m;
    private Long seed;
    private BigDecimal[][] bestCoeficients;
    private List<Vector> epsilonPhi;

    private List<Vector> epsilonTetha;
    private BigDecimal bestFitness = BigDecimal.ZERO;

    //file is filled on constructor AlgoritmoAscensoRapido
    private final LoadFile file;

    public AlgoritmoAscensoRapido(LoadFile file){
        this.file = file;
        this.swaps = new HashMap<>();
    }

    public static void main(String[] args) {
        //Aqui se carga el archivo.
        LoadFile file = new LoadFile();
        AlgoritmoAscensoRapido aaf = new AlgoritmoAscensoRapido(file);
        aaf.run(file.getVectores(), file.getLowerLimitScale(), file.getUpperLimitScale());
        //System.out.println(aaf.getM());
        Matrix.print(aaf.getBestCoeficients());
    }

    public void run(List<BigDecimal[]> vectores, BigDecimal[] lowerLimitToScale,BigDecimal[] upperLimitToScale ) {
        BigDecimal menor = new BigDecimal("1E1000");;
        Random localRandom = new Random();
        seed = localRandom.nextLong();
        seed = -3729315339680374604L;
        random = new Random(seed);
        this.m = Modelo.getRandomModelo(seed, 11, 10, this.file.getNumeroVariables());
        List<BigDecimal[]> d = vectores;
        this.m.setOriginalLowerLimitScale(lowerLimitToScale);
        this.m.setOriginalUpperLimitScale(upperLimitToScale);
        List<BigDecimal[]> e = scaleVectorsZeroToOne(d,this.m);
        List<BigDecimal[]> p = mapToPolynomial(e, this.m);
        List<BigDecimal[]> s = stabilizeVectors(p, this.m);
        List<Vector> epsilonPhi = mapToVectors(s, this.m);
        List<Vector> epsilonTetha   = getRandomVectorsToEvaluate(epsilonPhi);
        //TODO Modificar esta parte del codigo para no volver a generar
        // el Matrix Object. Y solo obtener el vector solucion derivado del sqap
        List<Vector> minMaxEquation = getMinMaxSigns3(epsilonTetha);
        MatrixObject matrixA = new MatrixObject(minMaxEquation);
        BigDecimal[][] b = Matrix.invertMatrix(matrixA.getMatrix());
        BigDecimal[][] solution = matrixA.getVectorSolution();
        //TODO hay que actualizar tambien el vector solucion
        BigDecimal[][] c;

        while (true){
            c = getCoeficientsAndEpsilonTetha(b, solution);
            this.m.setSolutionCoeficientes(c);
            Vector epsilonPhiVector = getEpsilonPhiVector(epsilonPhi);
            BigDecimal[][] lamdas = getLamdas(epsilonPhiVector.getMiMaxSignVector(), b);
            BigDecimal[][] betas = getBetas(lamdas, Matrix.getMatrixRow(b, 0),  epsilonPhiVector.getSign());
            int indexBeta = getBetaIndex(betas);
            BigDecimal ponderacion = getFitnessValue2(c[0][0].abs(), epsilonPhiVector.getError());
            System.out.println(seed+ " eT: "+c[0][0].abs()+" eP: "+epsilonPhiVector.getError()+ " ponderacion: "+ ponderacion);
            if(epsilonPhiVector.getError().compareTo(menor)<0){
                menor = new BigDecimal(epsilonPhiVector.getError().toString());
            }
            if(ponderacion.compareTo(bestFitness)>=0){
                System.out.println("*");
                bestFitness = ponderacion;
                saveBestCoeficients(c);
            }
            //if(c[0][0].abs().compareTo(epsilonPhiVector.getError())>=0){
            if(c[0][0].abs().compareTo(epsilonPhiVector.getError())>=0){
                System.out.println(c[0][0].abs()+ ": "+ epsilonPhiVector.getError());
                System.out.println("Convergio");
                saveBestCoeficients(c);
                break;
            }else if(wasSwapped(epsilonTetha, epsilonTetha.get(indexBeta),epsilonPhiVector)){
                break;
            }
            b = getInverseFromLamda(epsilonPhiVector.getMiMaxSignVector(b[0][indexBeta].signum()), b, indexBeta);
            swapVector(epsilonTetha, epsilonPhi, epsilonTetha.get(indexBeta), epsilonPhiVector);
            solution[indexBeta][0] = epsilonTetha.get(indexBeta).getVector()[epsilonTetha.get(indexBeta).getVector().length-1];

        }
        System.out.println("Menor: "+menor);

        setEpsilonPhi(epsilonPhi);
        setEpsilonTetha(epsilonTetha);
        //falta poner los mejores coeficientes
        m.setSolutionCoeficientes(getBestCoeficients());
    }

    public static BigDecimal getFitnessValue(BigDecimal n, BigDecimal d) {
        // (n / d)
        BigDecimal ratio = n.divide(d, MC);
        // (n / d)^α
        double ratioPow = Math.pow(ratio.doubleValue(), ALPHA);
        // (n + d)^β
        BigDecimal sum = n.add(d);
        double sumPow = Math.pow(sum.doubleValue(), BETA);
        // E(n, d) = (ratio^α) / (sum^β)
        double fitness = ratioPow / sumPow;
        return new BigDecimal(fitness, MC);
    }

    public static BigDecimal getFitnessValue2(BigDecimal n, BigDecimal d) {
        // ratio = n/d
        BigDecimal ratio = n.divide(d, MC);

        // Convert to double for fractional powers (mantener precision razonable)
        double ratioD = ratio.doubleValue();
        double sumD = n.add(d).doubleValue();
        double dD = d.doubleValue();

        // calcular potencias con Math.pow
        double ratioPow = Math.pow(ratioD, ALPHA);      // (n/d)^alpha
        double sumPow = Math.pow(sumD, BETA);           // (n+d)^beta
        double bonus = 1.0 + (GAMMA / Math.pow(dD, DELTA)); // 1 + gamma / d^delta

        double fitnessD = (ratioPow / sumPow) * bonus;

        // Volver a BigDecimal con el MathContext
        return new BigDecimal(fitnessD, MC);
    }

    private void validateIfItsBetterOptionToSwap(List<Vector> epsilonTetha, List<Vector> epsilonPhi, Vector epsilonPhiVector,
                                                 BigDecimal[][] b, BigDecimal[][] lamdas, BigDecimal[][] betas,
                                                 BigDecimal[][] vectorSolucion) {
        List<Vector> copyEpsilonTheta = new ArrayList<>();
        List<Vector> copyEpsilonPhi = new ArrayList<>();
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
        System.out.print("Lamda: ");
        Matrix.print(lamdas);
        System.out.print("Beta: ");
        Matrix.print(betas);


        int indexET=0;

        for (Vector v_:
                epsilonTetha) {
            BigDecimal[] newData=Arrays.copyOf(v_.getVector(), v_.getVector().length);
            Vector new_vector = new Vector(v_.getIndex(), v_.getModelo(), newData);
            copyEpsilonTheta.add(new_vector.setIndexEpsilonTheta(indexET++));
        }

        for (Vector v_:
                epsilonPhi) {
            BigDecimal[] newData=Arrays.copyOf(v_.getVector(), v_.getVector().length);
            Vector new_vector = new Vector(v_.getIndex(), v_.getModelo(), newData);
            copyEpsilonPhi.add(new_vector);
        }
        for (int i = 0; i < epsilonTetha.size(); i++) {
            BigDecimal[][] copyB = Matrix.cloneMatrix(b);
            BigDecimal[][] copyVectorSolucion =  Matrix.cloneMatrix(vectorSolucion);
            BigDecimal[] newData=Arrays.copyOf(epsilonPhiVector.getVector(), epsilonPhiVector.getVector().length);
            Vector copyEpsilonPhiVector = new Vector(epsilonPhiVector.getIndex(), epsilonPhiVector.getModelo(), newData);
            /* Esto demuestra que efectivamente se esta intercambiando correctamente el vector de epsilon theta.
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
            System.out.print("["+i+"]  Lamda: ");
            Matrix.print(copyLamda);
            System.out.print("["+i+"]  Beta: ");
            Matrix.print(copyBeta);
            Matrix.print(copyVectorSolucion);
            printVectores(copyEpsilonTheta);
            System.out.print("eT -> ");
            System.out.println(copyEpsilonTheta.get(i));
            System.out.print("eP -> ");
            System.out.println(epsilonPhiVector);
            System.out.println();
            */
            /*
            System.out.println("A simple test");
            System.out.println(Arrays.toString(epsilonTetha.get(i).getVector()));
            Matrix.print(epsilonTetha.get(i).getMiMaxSignVector(b[0][i].signum()));
            System.out.println();
            Matrix.print(b);
            Matrix.print(Matrix.invertMatrix(b));
            b = getInverseFromLamda(epsilonTetha.get(i).getMiMaxSignVector(b[0][i].signum()), b, i);
            Matrix.print(b);
            Matrix.print(Matrix.invertMatrix(b));
             */

            copyB = getInverseFromLamda(epsilonPhiVector.getMiMaxSignVector(), copyB, i);
            Vector v_ = copyEpsilonTheta.get(i);
            swapVector(copyEpsilonTheta, copyEpsilonPhi, copyEpsilonTheta.get(i), copyEpsilonPhiVector);
            BigDecimal aux = copyVectorSolucion[i][0];
            copyVectorSolucion[i][0] = copyEpsilonTheta.get(i).getVector()[copyEpsilonTheta.get(i).getVector().length-1];
            /* Esto demuestra que efectivamente se esta intercambiando correctamente el vector de epsilon theta.
            printVectores(copyEpsilonTheta);
            Matrix.print(copyVectorSolucion);
            */
            BigDecimal[][] c = getCoeficientsAndEpsilonTetha(copyB, copyVectorSolucion);
            System.out.println("["+i+"]  "+c[0][0]);
              //Matrix.print(Matrix.invertMatrix(copyB));
            List<Vector> minMaxEquation = getMinMaxSigns3(copyEpsilonTheta);
            MatrixObject matrixA = new MatrixObject(minMaxEquation);
              //Matrix.print(matrixA.getMatrix());
            swapVector(copyEpsilonTheta, copyEpsilonPhi, copyEpsilonTheta.get(i), v_);
            copyVectorSolucion[i][0] = aux;
        }
    }

    private BigDecimal[][] getInverseFromLamda(BigDecimal[][] epsilonPhi, BigDecimal[][] b, int indexBeta){
        //epsilonPhi[0][0] = BigDecimal.ONE.multiply(new BigDecimal(epsilonPhi[0][0].signum()));
        //Matrix.printMatrix(epsilonPhi);
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

        Swap swap =  this.swaps.get(IdSwap.of(epsilonThetaVector.getIndex(), epsilonPhiVector.getIndex()));
        if(swap!=null){
            if(swap.notContains(idsVectoresEpsilonTheta)){
                swap.add(idsVectoresEpsilonTheta);
            }
        }else {
            swap = new Swap();
            swap.add(idsVectoresEpsilonTheta);
            this.swaps.put(IdSwap.of(epsilonThetaVector.getIndex(), epsilonPhiVector.getIndex()), swap);
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
        Swap swap = this.swaps.get(IdSwap.of(epsilonThetaVector.getIndex(), epsilonPhiVector.getIndex()));
        if (swap != null) {
            boolean wasSwapped = swap.contains(idsIndex);
            if(wasSwapped){
                System.out.println("Se ciclo");
            }
            return wasSwapped;
        }else{
            return false;
        }
    }

    public void printSwaps(){
        for (Map.Entry<IdSwap, Swap> entrada : this.swaps.entrySet()) {
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
        String s = (sign<0)?"-":"";

        for (int i = 0; i < lamdas[0].length; i++) {
            betas[0][i] = lamdas[0][i].multiply(new BigDecimal(sign)).
                    divide(errorRow[0][i], MC);
            //System.out.print(s+""+lamdas[0][i]+"/"+errorRow[0][i]+"="+betas[0][i]+"\t");
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
                BigDecimal a = modelo.getOriginalLowerLimitScale()[i];
                BigDecimal b = modelo.getOriginalUpperLimitScale()[i];
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
        //modelo.eraseLimits();
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

    private List<Vector> getMinMaxSigns3(List<Vector> epsilonTetha){
        //printVectores(epsilonTetha);
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
        //printVectores(minMaxEquation);
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

    public Modelo getM() {
        return this.m;
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

    public List<Vector> getEpsilonTetha() {
        return epsilonTetha;
    }

    public void setEpsilonTetha(List<Vector> epsilonTetha) {
        this.epsilonTetha = epsilonTetha;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }
}
