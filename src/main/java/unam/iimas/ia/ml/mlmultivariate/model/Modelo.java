package unam.iimas.ia.ml.mlmultivariate.model;

import java.math.BigDecimal;
import java.util.*;

public class Modelo {

    private Long id;
    private Termino[] terminos;
    private BigDecimal[] originalLowerLimitScale;
    private BigDecimal[] originalUpperLimitScale;
    private BigDecimal[] polynomialLowerLimitScale;
    private BigDecimal[] polynomialUpperLimitScale;

    public Modelo(Termino[] terminos){
        this.terminos = terminos;
    }

    public static Modelo getRandomModelo(Random random, int numeroTerminos, int indexPotenciaMaximaL, int numeroVariables){
        Set<Termino> set_terminos = new HashSet<>();
        if(indexPotenciaMaximaL>21){
            throw new IllegalArgumentException("La lista 'L' solo contiene 21 potencias, el valor solicitado no existe");
        }
        while (set_terminos.size() < numeroTerminos){
            int indexL = 21;// se coloca el nuemro 21 para establecer un limite maximo nunca lacanzado en la primera ireracion
            while(indexL>indexPotenciaMaximaL) {
                indexL = random.nextInt(21);
            }
            Termino ter = Termino.getRandomTermino(random, L.getIndexPower(indexL),numeroVariables);
            set_terminos.add(ter);
        }
        List<Termino> lista_terminos = new ArrayList<>(set_terminos);
        //Collections.sort(lista_terminos);

        return new Modelo(lista_terminos.toArray(new Termino[lista_terminos.size()]));
    }



    @Override
    public String toString(){
       return Arrays.toString(this.terminos);
    }

    public Termino[] getTerminos() {
        return terminos;
    }

    public BigDecimal[] getPolynomialVector(BigDecimal[] vectorOriginal){
        if(polynomialUpperLimitScale == null){
            polynomialUpperLimitScale = new BigDecimal[this.getTerminos().length+1];
        }
        if(polynomialLowerLimitScale == null){
            polynomialLowerLimitScale = new BigDecimal[this.getTerminos().length+1];
        }
        BigDecimal[] polynomialVector = new BigDecimal[this.getTerminos().length+1];
        // Se agrega la variable dependiente
        // Es decir no se modifica el valor de el ultimo termino que esta como variable independiete
        // al mapéarse al modelo
        polynomialVector[this.getTerminos().length] = vectorOriginal[vectorOriginal.length-1];
        for (int i = 0; i< polynomialVector.length; i++) {
            if(i< (polynomialVector.length-1)){ // se excluye el ultimo valor pues ya se agrego en las lineas anteriores
                polynomialVector[i] = this.getTerminos()[i].evaluate(vectorOriginal);
                polynomialVector[i] = (polynomialVector[i].compareTo(BigDecimal.ZERO)==0)?
                        BigDecimal.ZERO: polynomialVector[i];
            }
            polynomialUpperLimitScale[i] = (polynomialUpperLimitScale[i]==null)?polynomialVector[i]: polynomialUpperLimitScale[i];
            polynomialLowerLimitScale[i] = (polynomialLowerLimitScale[i]==null)?polynomialVector[i]: polynomialLowerLimitScale[i];

            polynomialUpperLimitScale[i]=(polynomialVector[i].compareTo(polynomialUpperLimitScale[i])>0)?
                    polynomialVector[i]: polynomialUpperLimitScale[i];
            polynomialLowerLimitScale[i]=(polynomialVector[i].compareTo(polynomialLowerLimitScale[i])<0)?
                    polynomialVector[i]: polynomialLowerLimitScale[i];
        }
        return polynomialVector;
    }

    public void calculateNewLimits(BigDecimal[] vectorOriginal){
        for (int i = 0; i< vectorOriginal.length; i++) {
            if(originalLowerLimitScale == null && originalUpperLimitScale == null){
                originalLowerLimitScale = new BigDecimal[vectorOriginal.length];
                originalUpperLimitScale = new BigDecimal[vectorOriginal.length];
            }
            originalUpperLimitScale[i] = (originalUpperLimitScale[i]==null)?vectorOriginal[i]: originalUpperLimitScale[i];
            originalLowerLimitScale[i] = (originalLowerLimitScale[i]==null)?vectorOriginal[i]: originalLowerLimitScale[i];

            originalUpperLimitScale[i]=(vectorOriginal[i].compareTo(originalUpperLimitScale[i])>0)?
                    vectorOriginal[i]: originalUpperLimitScale[i];
            originalLowerLimitScale[i]=(vectorOriginal[i].compareTo(originalLowerLimitScale[i])<0)?
                    vectorOriginal[i]: originalLowerLimitScale[i];

        }
    }

    public BigDecimal[] getOriginalLowerLimitScale() {
        return originalLowerLimitScale;
    }

    public BigDecimal[] getOriginalUpperLimitScale() {
        return originalUpperLimitScale;
    }
    public static Modelo getCustomModel(){
        Termino[] terminos = {new Termino(new int[]{0, 0, 0, 0})
                            ,new Termino(new int[]{7, 0, 0, 3})
                            ,new Termino(new int[]{3, 0, 0, 7})};
        return new Modelo(terminos);
    }

    public void eraseLimits(){
        this.originalUpperLimitScale = null;
        this.originalLowerLimitScale = null;
    }

    public void setSolutionCoeficientes(BigDecimal[][] coeficientes){
        //se omite el primer elemento porque ese es epsilon theta y solo necesitamos los coeficientes
        for (int i = 1; i < coeficientes.length; i++) {
            this.terminos[i-1].setCoeficiente(coeficientes[i][0]);
        }
    }

    public void setOriginalLowerLimitScale(BigDecimal[] originalLowerLimitScale) {
        this.originalLowerLimitScale = originalLowerLimitScale;
    }

    public void setOriginalUpperLimitScale(BigDecimal[] originalUpperLimitScale) {
        this.originalUpperLimitScale = originalUpperLimitScale;
    }
}
