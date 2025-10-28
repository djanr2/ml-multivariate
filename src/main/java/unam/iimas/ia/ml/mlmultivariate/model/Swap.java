package unam.iimas.ia.ml.mlmultivariate.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Swap {
    private final Set<List<Integer>> setListas;

    public Swap(){
        this.setListas= new HashSet<>();
    }

    public boolean contains(List<Integer> l){
        return this.setListas.contains(l);
    }

    public void add(List<Integer> l){
        this.setListas.add(l);
    }

    public boolean notContains(List<Integer> l){
        return !this.setListas.contains(l);
    }

    public Set<List<Integer>> getSetListas() {
        return setListas;
    }
}

