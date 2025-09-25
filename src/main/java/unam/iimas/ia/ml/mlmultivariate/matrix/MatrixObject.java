package unam.iimas.ia.ml.mlmultivariate.matrix;

import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;
import java.util.List;

public class MatrixObject {
    private final BigDecimal[][] matrix;
    private final BigDecimal[][] vectorSolution;
    private final BigDecimal[][] vectorSolutionInverted;

    public MatrixObject(List<Vector> matrix) {

        BigDecimal[][] matrix_ = new BigDecimal[matrix.size()][matrix.size()];
        BigDecimal[][] vector_ = new BigDecimal[matrix.size()][1];
        BigDecimal[][] vectorInverted_ = new BigDecimal[1][matrix.size()];

        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.size(); j++) {
                matrix_[i][j] = matrix.get(i).getVector()[j];
            }
            vector_[i][0]=matrix.get(i).getVector()[matrix.size()];
            vectorInverted_[0][i] = vector_[i][0];
        }
        this.matrix = matrix_;
        this.vectorSolution = vector_;
        this.vectorSolutionInverted = vectorInverted_;
    }

    public BigDecimal[][] getMatrix() {
        return matrix;
    }

    public BigDecimal[][] getVectorSolution() {
        return vectorSolution;
    }

    public BigDecimal[][] getVectorSolutionInverted(){
        return vectorSolutionInverted;
    }
}
