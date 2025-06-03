package unam.iimas.ia.ml.mlmultivariate.model;

import java.math.BigDecimal;
import java.util.*;


public class Modelo {

    private Long id;
    private Termino[] terminos;
    private BigDecimal[] lowerLimitScale;
    private BigDecimal[] upperLimitScale;
    private int l;


    public Modelo(int l, Termino[] terminos){
        this.l = l;
        this.terminos = terminos;
    }


    public static Modelo getRandomModelo(int potenciaL, int numeroMaximoTerminos, int numeroVariables){
        int potenciaAux = potenciaL;
        ArrayList<Termino> term = new ArrayList<>();
        term.add(Termino.getZeroTerm(numeroVariables));
        Random rand = new Random();
        for (int i = 0; i < numeroMaximoTerminos; i++) {
            int potencia = rand.nextInt(0,potenciaAux+1);
            if(i==numeroMaximoTerminos-1){
                potencia=potenciaAux;
            }
            if (potencia != 0){
                Termino termino = Termino.getRandomTermino(potencia,numeroVariables);
                term.add(termino);
            }
            potenciaAux-=potencia;
        }
        Modelo m = new Modelo(potenciaL, term.toArray(new Termino[term.size()]));
        return m;
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

        //Se agrega la variable dependiente
        polynomialVector[this.getTerminos().length] = vectorOriginal[vectorOriginal.length-1];

        for (int i = 0; i< polynomialVector.length; i++) {
            if(i< (polynomialVector.length-1)){
                polynomialVector[i] = this.getTerminos()[i].evaluate(vectorOriginal);
                if(lowerLimitScale == null && upperLimitScale == null){
                    lowerLimitScale = new BigDecimal[polynomialVector.length];
                    upperLimitScale = new BigDecimal[polynomialVector.length];
                    if(lowerLimitScale[i] == null && polynomialVector[i].compareTo(new BigDecimal("1"))==0){
                        lowerLimitScale[i] = new BigDecimal("0");
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

}
