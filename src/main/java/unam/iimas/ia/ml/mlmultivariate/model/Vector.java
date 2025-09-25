package unam.iimas.ia.ml.mlmultivariate.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class Vector implements Comparable<Vector>{
    private final int index;
    private Modelo modelo;
    private BigDecimal[] vector;
    private BigDecimal error;
    private BigDecimal value;
    private static final int PRECISION = Precision.MIN_PRECISION;
    private static final RoundingMode ROUNDING_MODE = Precision.ROUNDING_MODE;
    private int sign;

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
        error =  (value.subtract(vector[vector.length-1]));
        sign = (error.signum() < 0)?-1:1;
        error = error.abs().setScale(PRECISION, ROUNDING_MODE);
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

    public int getIndex() {
        return index;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal[][] getMiMaxSignVector(){
        BigDecimal[][] minMax= new BigDecimal[1][this.vector.length];

        minMax[0][0] = error.multiply(new BigDecimal(sign));
        for (int i = 0; i < this.vector.length-1; i++) {
            minMax[0][i+1] = this.vector[i];
        }
        return minMax;
    }
    public int getSign() {
        return sign;
    }
    public void setSign(int sign) {
        this.sign = sign;
    }

    @Override
    public String toString(){
        //return this.index+" "+error;
        String sign_ = (sign<0)?"-":"";
        return this.index+" "+Arrays.toString(this.vector)+ " = " +sign_+ error;
    }

    @Override
    public int compareTo(Vector o) {
        return (o.error.compareTo(this.error) == 1)? 1: -1;
    }

}
