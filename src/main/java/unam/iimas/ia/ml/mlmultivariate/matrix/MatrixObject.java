package unam.iimas.ia.ml.mlmultivariate.matrix;

import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;
import java.util.List;

public class MatrixObject {
    private final BigDecimal[][] matrix;
    private final BigDecimal[] vectorSolution;

    public MatrixObject(List<Vector> matriwx) {
        BigDecimal[][] matrix_ = new BigDecimal[matriwx.size()][matriwx.size()];
        BigDecimal[] vector_ = new BigDecimal[matriwx.size()];
        for (int i = 0; i < matriwx.size(); i++) {
            for (int j = 0; j < matriwx.size(); j++) {
                matrix_[i][j] = matriwx.get(i).getVector()[j];
            }
            vector_[i]=matriwx.get(i).getVector()[matriwx.size()];
        }
        this.matrix = matrix_;
        this.vectorSolution = vector_;
    }

    public BigDecimal[][] getMatrix() {
        return matrix;
    }

    public BigDecimal[] getVectorSolution() {
        return vectorSolution;
    }
}
