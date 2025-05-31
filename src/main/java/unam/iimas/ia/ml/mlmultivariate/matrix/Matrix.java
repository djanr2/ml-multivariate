package unam.iimas.ia.ml.mlmultivariate.matrix;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Matrix {

    private static final int PRECISION = 20;

    public static BigDecimal[] gaussianElimination(BigDecimal[][] A, BigDecimal[] b) {
        int n = b.length;

        for (int pivot = 0; pivot < n; pivot++) {
            BigDecimal pivotVal = A[pivot][pivot];
            for (int j = pivot; j < n; j++) {
                A[pivot][j] = A[pivot][j].divide(pivotVal, PRECISION, RoundingMode.HALF_UP);
            }
            b[pivot] =  b[pivot].divide(pivotVal, PRECISION, RoundingMode.HALF_UP);
            for (int i = pivot + 1; i < n; i++) {
                BigDecimal factor = A[i][pivot];
                for (int j = pivot; j < n; j++) {
                    A[i][j] = A[i][j].subtract(factor.multiply(A[pivot][j]));
                }
                b[i] = b[i].subtract(factor.multiply(b[pivot]));
            }
        }
        BigDecimal[] x = new BigDecimal[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = b[i];
            for (int j = i + 1; j < n; j++) {
                x[i] = x[i].subtract(A[i][j].multiply(x[j]));
            }
        }

        return x;
    }


    public static void main(String[] args) {
        BigDecimal[][] A = {
                {new BigDecimal(1), new BigDecimal(1), new BigDecimal(1), new BigDecimal(1)},
                {new BigDecimal(1), new BigDecimal(3), new BigDecimal(9), new BigDecimal(27)},
                {new BigDecimal(1), new BigDecimal(6), new BigDecimal(36), new BigDecimal(216)},
                {new BigDecimal(1), new BigDecimal(8), new BigDecimal(64), new BigDecimal(512)}
        };

        BigDecimal[] b = {new BigDecimal(2), new BigDecimal(5), new BigDecimal(3), new BigDecimal(4.5)};


        BigDecimal[] solution = gaussianElimination(A, b);

        System.out.printf("Solution:%n");
        System.out.printf("w = %.6f%n", solution[0]);
        System.out.printf("x = %.6f%n", solution[1]);
        System.out.printf("y = %.6f%n", solution[2]);
        System.out.printf("z = %.6f%n", solution[3]);
    }
}
