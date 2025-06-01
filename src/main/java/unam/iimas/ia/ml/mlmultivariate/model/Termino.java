package unam.iimas.ia.ml.mlmultivariate.model;

import java.math.BigDecimal;
import java.util.*;

public class Termino {

    private static final int MAXIMO_TERMINO = 10; //
    private static final int MINIMO_TERMINO = 4; //

    private Long id;
    private int[] potencias;
    private BigDecimal coeficiente;
    private int potencia;

    public Termino(BigDecimal coeficiente, int potencia, int numeroVariables){
        this.coeficiente = coeficiente;
        this.potencias = new int[numeroVariables];
        this.potencia = potencia;
    }



    public static int getRandomTerminosNumber(){
        Random random = new Random();
        return random.nextInt(MINIMO_TERMINO,MAXIMO_TERMINO + 1);// Número + 1 inclusive
    }

    public static Termino getRandomTermino(int potenciaTermino, int numeroDeVariables){
        Termino termino = new Termino(new BigDecimal(1), potenciaTermino,numeroDeVariables);
        int potenciaTerminoAux = potenciaTermino;
        int potencia=0;
        Random rand = new Random();
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < numeroDeVariables; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        for (int i = 0; i < numeroDeVariables; i++) {
            potencia = rand.nextInt(potenciaTerminoAux+1);
            if(i==(numeroDeVariables-1)){
                potencia = potenciaTerminoAux;
            }
            termino.getPotencias()[numbers.get(i)]=potencia;
            potenciaTerminoAux-=potencia;
        }
        return termino;
    }

    public static Termino getZeroTerm(int numeroVariables){
        return new Termino(new BigDecimal(1), 0,numeroVariables);

    }
    @Override
    public String toString(){
        return Arrays.toString(this.potencias);
    }

    public int getPotencia() {
        return potencia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public BigDecimal getCoeficiente() {
        return coeficiente;
    }

    public void setCoeficiente(BigDecimal coeficiente) {
        this.coeficiente = coeficiente;
    }

    public void setPotencia(int potencia) {
        this.potencia = potencia;
    }

    public int[] getPotencias() {
        return potencias;
    }

    public static void main(String[] args) {
        Termino t = Termino.getRandomTermino(40,6);
        System.out.println(t);
    }

}
