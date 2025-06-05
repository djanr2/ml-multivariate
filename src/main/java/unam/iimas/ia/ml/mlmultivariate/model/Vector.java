package unam.iimas.ia.ml.mlmultivariate.model;

import java.math.BigDecimal;
import java.util.Arrays;

public class Vector implements Comparable<Vector>{
    private Long id;
    private final int index;
    private Modelo modelo;
    private BigDecimal[] vector;
    private BigDecimal error;
    private BigDecimal value;

    public Vector(int index, Modelo modelo, BigDecimal[] vector) {
        this.index = index;
        this.modelo = modelo;
        this.vector = vector;
    }

    public Vector evaluate(){
        value = new BigDecimal(0);
        for (int i = 0; i < modelo.getTerminos().length; i++) {
            value = value.add(vector[i].multiply(modelo.getTerminos()[i].getCoeficiente()));
        }
        error =  (value.subtract(vector[vector.length-1])).abs();
        return this;
    }

    public Modelo getModelo() {
        return modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public BigDecimal[] getVector() {
        return vector;
    }

    public void setVector(BigDecimal[] vector) {
        this.vector = vector;
    }

    public BigDecimal getError() {
        return error;
    }

    public void setError(BigDecimal error) {
        this.error = error;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString(){
        //return this.index+" "+error;
        return this.index+" "+Arrays.toString(this.vector)+ " = " + error;
    }

    @Override
    public int compareTo(Vector o) {
        return (o.error.compareTo(this.error) == 1)? 1: -1;
    }

}
