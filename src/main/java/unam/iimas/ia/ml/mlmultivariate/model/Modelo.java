package unam.iimas.ia.ml.mlmultivariate.model;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Stream;


public class Modelo {

    private Long id;
    private Termino[] terminos;
    private BigDecimal[] lowerLimitScale;
    private BigDecimal[] upperLimitScale;
    private final int l;


    public Modelo(Termino[] terminos){
        this.l =  Arrays.stream(terminos).mapToInt(Termino::getPotencia).sum();
        this.terminos = terminos;
    }


    public static Modelo getRandomModelo(int potenciaL, int numeroMaximoTerminos, int numeroVariables){
        System.out.println(numeroMaximoTerminos);
        int potenciaAux = potenciaL;
        HashSet<Termino> term = new HashSet<>();
        Random rand = new Random();
        do{
            int potencia = rand.nextInt(1,potenciaAux+1);
            if(numeroMaximoTerminos==1){
                potencia = potenciaAux;
            }
            Termino t = Termino.getRandomTermino(potencia,numeroVariables);
            if(term.add(t)){
                potenciaAux-=potencia;
                numeroMaximoTerminos--;
            }

        } while ( potenciaAux > 0 );

        return new Modelo(Stream.concat(
                Stream.of(Termino.getZeroTerm(numeroVariables)),
                Stream.of(term.toArray(new Termino[term.size()]))).toArray(Termino[]::new));
    }


    @Override
    public String toString(){
       return Arrays.toString(this.terminos);
    }

    public Termino[] getTerminos() {
        return terminos;
    }

    public int getL() {
        return l;
    }

    public BigDecimal[] getPolynomialVector(BigDecimal[] vectorOriginal){
        BigDecimal[] polynomialVector = new BigDecimal[this.getTerminos().length+1];
        // Se agrega la variable dependiente
        // Es decir no se modifica el valor de el ultimo termino que esta como variable independiete
        // al mapéarse al modelo
        polynomialVector[this.getTerminos().length] = vectorOriginal[vectorOriginal.length-1];
       //TODO ANALYZE IF LOWER LIMIT IS NEEDED TO SET AS 0
        for (int i = 0; i< polynomialVector.length; i++) {
            if(i< (polynomialVector.length-1)){ // se excluye el ultimo valor pues ya se agrego en las lineas anteriores
                polynomialVector[i] = this.getTerminos()[i].evaluate(vectorOriginal);
                polynomialVector[i] = (polynomialVector[i].compareTo(BigDecimal.ZERO)==0)?
                        BigDecimal.ZERO: polynomialVector[i];
                if(lowerLimitScale == null && upperLimitScale == null){
                    lowerLimitScale = new BigDecimal[polynomialVector.length];
                    upperLimitScale = new BigDecimal[polynomialVector.length];
                    if(lowerLimitScale[i] == null && polynomialVector[i].compareTo(new BigDecimal(1))==0){
                        lowerLimitScale[i] = BigDecimal.ZERO;
                    }
                }
            }
            upperLimitScale[i] = (upperLimitScale[i]==null)?polynomialVector[i]:upperLimitScale[i];
            lowerLimitScale[i] = (lowerLimitScale[i]==null)?polynomialVector[i]:lowerLimitScale[i];

            upperLimitScale[i]=(polynomialVector[i].compareTo(upperLimitScale[i])>0)?
                    polynomialVector[i]:upperLimitScale[i];
            lowerLimitScale[i]=(polynomialVector[i].compareTo(lowerLimitScale[i])<0)?
                    polynomialVector[i]:lowerLimitScale[i];
        }
        return polynomialVector;
    }

    public void calculateNewLimits(BigDecimal[] vectorOriginal){
        for (int i = 0; i< vectorOriginal.length; i++) {
            if(lowerLimitScale == null && upperLimitScale == null){
                lowerLimitScale = new BigDecimal[vectorOriginal.length];
                upperLimitScale = new BigDecimal[vectorOriginal.length];
            }
            upperLimitScale[i] = (upperLimitScale[i]==null)?vectorOriginal[i]:upperLimitScale[i];
            lowerLimitScale[i] = (lowerLimitScale[i]==null)?vectorOriginal[i]:lowerLimitScale[i];

            upperLimitScale[i]=(vectorOriginal[i].compareTo(upperLimitScale[i])>0)?
                    vectorOriginal[i]:upperLimitScale[i];
            lowerLimitScale[i]=(vectorOriginal[i].compareTo(lowerLimitScale[i])<0)?
                    vectorOriginal[i]:lowerLimitScale[i];

        }
    }

    public BigDecimal[] getLowerLimitScale() {
        return lowerLimitScale;
    }

    public BigDecimal[] getUpperLimitScale() {
        return upperLimitScale;
    }

    /*
    public static void main(String[] args) {
        System.out.println(11);
        System.out.println(4);
        Modelo m = getRandomModelo(11, 4, 6);
    }

 */
    public static Modelo getCustomModel(){
        Termino[] terminos = {new Termino(new int[]{0, 0, 0, 0, 0})
                            ,new Termino(new int[]{7, 0, 0, 0, 3})
                            ,new Termino(new int[]{14, 0, 8, 1, 7})
                            ,new Termino(new int[]{2, 0, 0, 4, 0})
                            ,new Termino(new int[]{1, 7, 2, 3, 1})
                            ,new Termino(new int[]{0, 0, 0, 0, 1})
                            ,new Termino(new int[]{0, 0, 1, 0, 0})
                            ,new Termino(new int[]{0, 0, 0, 1, 0})};

        Termino[]  terminos2 = {new Termino(new int[]{1, 0, 0, 0, 1})
                ,new Termino(new int[]{0, 1, 0, 1, 0})
                ,new Termino(new int[]{0, 0, 1, 0, 0})};
        Termino[]  terminos3 = {
                new Termino(new int[]{0, 0}),
                new Termino(new int[]{0, 1}),
                new Termino(new int[]{0, 2}),
                new Termino(new int[]{1, 0}),
                new Termino(new int[]{1, 1}),
                new Termino(new int[]{2, 0}),
                new Termino(new int[]{2, 1}),
                new Termino(new int[]{2, 2})};

        Termino[] terminos4 = {new Termino(new int[]{0, 0, 0, 0})
                ,new Termino(new int[]{2, 3,  2, 3})
                ,new Termino(new int[]{5, 8, 1, 7})
                ,new Termino(new int[]{2, 3, 4, 1})
                ,new Termino(new int[]{1, 3, 2, 4})
                ,new Termino(new int[]{1, 3, 1, 1})
                ,new Termino(new int[]{2, 1, 1, 1})
                ,new Termino(new int[]{0, 0, 1, 0})};
        return new Modelo(terminos4);
    }

    public void eraseLimits(){
        this.upperLimitScale = null;
        this.lowerLimitScale = null;
    }

    public void setCoeficientes(BigDecimal[][] coeficientes){
        for (int i = 0; i < this.terminos.length; i++) {
                this.terminos[i].setCoeficiente(coeficientes[i][0]);
        }
    }

    public void setSolutionCoeficientes(BigDecimal[][] coeficientes){
        for (int i = 1; i < coeficientes.length; i++) {
            this.terminos[i-1].setCoeficiente(coeficientes[i][0]);
        }
    }

    public void setLowerLimitScale(BigDecimal[] lowerLimitScale) {
        this.lowerLimitScale = lowerLimitScale;
    }

    public void setUpperLimitScale(BigDecimal[] upperLimitScale) {
        this.upperLimitScale = upperLimitScale;
    }
}
