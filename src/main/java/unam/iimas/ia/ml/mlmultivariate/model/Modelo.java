package unam.iimas.ia.ml.mlmultivariate.model;

import java.util.*;


public class Modelo {

    private Long id;
    private Termino[] terminos;
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
        System.out.println(m);
        return m;
    }


    @Override
    public String toString(){
        String s_= "";
        for (Termino t:
        this.terminos) {
            s_ += t+ " ";
        }
       return s_;
    }

    public Termino[] getTerminos() {
        return terminos;
    }

    public static void main(String[] args) {
        System.out.println(11);
        System.out.println(4);
        Modelo m = getRandomModelo(11, 4, 6);
    }

}
