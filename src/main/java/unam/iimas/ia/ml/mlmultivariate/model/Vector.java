package unam.iimas.ia.ml.mlmultivariate.model;

import unam.iimas.ia.ml.mlmultivariate.matrix.Matrix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vector implements Comparable<Vector>{
    private final int index;
    private Modelo modelo;
    private BigDecimal[] vector;
    private BigDecimal error;
    private BigDecimal value;
    private static final int PRECISION = Precision.MIN_PRECISION;
    private static final RoundingMode ROUNDING_MODE = Precision.ROUNDING_MODE;
    private int sign;
    private int indexEpsilonTheta;

    public Vector(int index, Modelo modelo, BigDecimal[] vector) {
        this.index = index;
        this.modelo = modelo;
        this.vector = vector;
    }

    public int getIndexEpsilonTheta() {
        return indexEpsilonTheta;
    }

    public Vector setIndexEpsilonTheta(int indexEpsilonTheta) {
        this.indexEpsilonTheta = indexEpsilonTheta;
        return this;
    }

    public Vector evaluate(){
        value = new BigDecimal(0);
        for (int i = 0; i < modelo.getTerminos().length; i++) {
            value = value.add(vector[i].multiply(modelo.getTerminos()[i].getCoeficiente()));
        }
        value = value.setScale(PRECISION, ROUNDING_MODE);
        error =  (vector[vector.length-1].subtract(value));
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
        int sign_;
        if(sign < 0){
            sign_ = -1;
        }else{
            sign_ = 1;
        }
        //minMax[0][0] = error.multiply(new BigDecimal(sign));
        minMax[0][0] = BigDecimal.ONE.multiply(new BigDecimal(sign_));
        for (int i = 0; i < this.vector.length-1; i++) {
            minMax[0][i+1] = this.vector[i];
        }
        return minMax;
    }

    public BigDecimal[][] getMiMaxSignVector(int sign_){
        // Esto es correcto Ya no mover
        BigDecimal[][] minMax= new BigDecimal[1][this.vector.length];
        //minMax[0][0] = error.multiply(new BigDecimal(sign));
        minMax[0][0] = BigDecimal.ONE.multiply(new BigDecimal(sign_));
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

    public String getStringToCalculate(){

        String output = "";
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < this.modelo.getTerminos().length; i++) {
            int sign_int = this.modelo.getTerminos()[i].getCoeficiente().signum();
            String sign = (sign_int>0)?"+":"-";
            output+=sign + "("+this.modelo.getTerminos()[i].getCoeficiente().abs() + "*" + this.vector[i]+")";
            result = result.add(this.modelo.getTerminos()[i].getCoeficiente().multiply(this.vector[i]));
        }
        String signRes= (result.signum()>0)?"-":"+";// se cambia el signo al imprimir porque es una resta
        return output+ " = "+ this.vector[vector.length-1] + signRes +result.abs().setScale(PRECISION, ROUNDING_MODE)
                + " : "+this.vector[vector.length-1].subtract(result).setScale(PRECISION, ROUNDING_MODE);
    }

    public String getStringToGraph(){

        String output = "";
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < this.modelo.getTerminos().length; i++) {
            int sign_int = this.modelo.getTerminos()[i].getCoeficiente().signum();
            String sign = (sign_int>0)?"":"-";
            output+=sign + "("+this.modelo.getTerminos()[i].getCoeficiente().abs() + "*" + this.vector[i]+")";
            result = result.add(this.modelo.getTerminos()[i].getCoeficiente().multiply(this.vector[i]));
        }
        String signRes= (result.signum()>0)?"+":"-";// se cambia el signo al imprimir porque es una resta
        return "" + this.vector[vector.length-1] +"\t"+ signRes +result.abs().setScale(PRECISION, ROUNDING_MODE)
                + "\t"+this.vector[vector.length-1].subtract(result).setScale(PRECISION, ROUNDING_MODE);
    }

    @Override
    public int compareTo(Vector o) {
        return (o.error.compareTo(this.error) == 1)? 1: -1;
    }

}
