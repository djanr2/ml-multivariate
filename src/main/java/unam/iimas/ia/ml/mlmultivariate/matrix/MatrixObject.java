package unam.iimas.ia.ml.mlmultivariate.matrix;

import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatrixObject {
    private final BigDecimal[][] matrix;
    private final BigDecimal[][] vectorSolution;

    public MatrixObject(List<Vector> matrix) {
        BigDecimal[][] matrix_ = new BigDecimal[matrix.size()][matrix.size()];
        BigDecimal[][] vector_ = new BigDecimal[matrix.size()][1];

        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.size(); j++) {
                matrix_[i][j] = matrix.get(i).getVector()[j];
            }
            vector_[i][0]=matrix.get(i).getVector()[matrix.size()];
        }
        this.matrix = matrix_;
        this.vectorSolution = vector_;
    }

    public BigDecimal[][] getMatrix() {
        return matrix;
    }

    public BigDecimal[][] getVectorSolution() {
        return vectorSolution;
    }

}
