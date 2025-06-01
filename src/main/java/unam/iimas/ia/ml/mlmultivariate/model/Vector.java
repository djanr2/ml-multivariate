package unam.iimas.ia.ml.mlmultivariate.model;

import java.util.ArrayList;
import java.util.List;

public class Vector {
    private List<Variable> variables;
    private List<Variable> independentVariables;
    private Variable dependentVariable;
    private Vector lowerLimitScale;
    private Vector upperLimitScale;
    private boolean isScaled;



    public Vector() {
        variables = new ArrayList<>();
        independentVariables= new ArrayList<>();
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public Variable getDependentVariable() {
        return dependentVariable;
    }

    public void setDependentVariable(Variable dependentVariable) {
        this.dependentVariable = dependentVariable;
    }

    public List<Variable> getIndependentVariables() {
        return independentVariables;
    }

    public void setIndependentVariables(List<Variable> independentVariables) {
        this.independentVariables = independentVariables;
    }

    public void addVariable(Variable var){
        variables.add(var);
    }
    public void addIndependentVariable(Variable var){
        independentVariables.add(var);
    }

    public void setLowerLimitScale(Vector lowerLimitScale) {
        this.lowerLimitScale = lowerLimitScale;
    }

    public void setUpperLimitScale(Vector upperLimitScale) {
        this.upperLimitScale = upperLimitScale;
    }
}
