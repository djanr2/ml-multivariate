package unam.iimas.ia.ml.mlmultivariate.matrix;


import unam.iimas.ia.ml.mlmultivariate.model.Precision;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Matrix {

    private static final int PRECISION = Precision.MIN_PRECISION;
    private static final RoundingMode ROUNDING_MODE = Precision.ROUNDING_MODE;
    private  BigDecimal[][] original;
    private  int size;


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


        BigDecimal[][] matrix = {
                {new BigDecimal(2), new BigDecimal(1), new BigDecimal(1), new BigDecimal(0), new BigDecimal(0), new BigDecimal(0)},
                {new BigDecimal(4), new BigDecimal(3), new BigDecimal(3), new BigDecimal(1), new BigDecimal(0), new BigDecimal(0)},
                {new BigDecimal(8), new BigDecimal(7), new BigDecimal(9), new BigDecimal(5), new BigDecimal(1), new BigDecimal(0)},
                {new BigDecimal(6), new BigDecimal(7), new BigDecimal(9), new BigDecimal(8), new BigDecimal(6), new BigDecimal(4)},
                {new BigDecimal(4), new BigDecimal(5), new BigDecimal(7), new BigDecimal(9), new BigDecimal(10), new BigDecimal(8)},
                {new BigDecimal(2), new BigDecimal(3), new BigDecimal(5), new BigDecimal(7), new BigDecimal(9), new BigDecimal(11)}
        };

        Matrix inverter = new Matrix();
        BigDecimal[][] inverse = inverter.invertStepByStep(matrix);

        System.out.println("\nFinal Inverse Matrix:");
        inverter.printMatrix(inverse);

    }

    public static BigDecimal[] gaussianElimination(BigDecimal[][] A, BigDecimal[] b) {
        int n = b.length;

        for (int pivot = 0; pivot < n; pivot++) {
            BigDecimal pivotVal = A[pivot][pivot];
            for (int j = pivot; j < n; j++) {
                A[pivot][j] = A[pivot][j].divide(pivotVal, PRECISION, ROUNDING_MODE);
            }
            b[pivot] =  b[pivot].divide(pivotVal, PRECISION, ROUNDING_MODE);
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

    public static BigDecimal[] getGaussiaSolution(MatrixObject matrix){
        return gaussianElimination(matrix.getMatrix(), matrix.getVectorSolution());
    }
    public BigDecimal[][] invertStepByStep(BigDecimal[][] matrix) {

        this.size = matrix.length;
        if (size != matrix[0].length)
            throw new IllegalArgumentException("Matrix must be square.");

        //Copy Matrix into another
        this.original = new BigDecimal[size][size];
        for (int i = 0; i < size; i++)
            System.arraycopy(matrix[i], 0, original[i], 0, size);

        BigDecimal[][] augmented = createAugmentedMatrix();

        for (int i = 0; i < size; i++) {
            BigDecimal pivot = augmented[i][i];
            if (pivot.compareTo(BigDecimal.ZERO) == 0) {
                boolean swapped = false;
                for (int j = i + 1; j < size; j++) {
                    if (augmented[j][i].compareTo(BigDecimal.ZERO) != 0) {
                        swapRows(augmented, i, j);
                        swapped = true;
                        break;
                    }
                }
                if (!swapped) throw new ArithmeticException("Matrix is singular and cannot be inverted.");
                pivot = augmented[i][i];
            }
            normalizeRow(augmented, i, pivot);
            for (int k = 0; k < size; k++) {
                if (k != i) {
                    eliminateRow(augmented, k, i);
                }
            }
        }

        // Extract inverse from augmented matrix
        BigDecimal[][] inverse = new BigDecimal[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(augmented[i], size, inverse[i], 0, size);
        }

        return inverse;
    }

    private BigDecimal[][] createAugmentedMatrix() {
        BigDecimal[][] augmented = new BigDecimal[size][2 * size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(original[i], 0, augmented[i], 0, size);
            for (int j = 0; j < size; j++) {
                augmented[i][j + size] = (i == j) ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        }
        return augmented;
    }

    private void swapRows(BigDecimal[][] matrix, int row1, int row2) {
        BigDecimal[] temp = matrix[row1];
        matrix[row1] = matrix[row2];
        matrix[row2] = temp;
    }

    private void normalizeRow(BigDecimal[][] matrix, int row, BigDecimal pivot) {
        for (int j = 0; j < matrix[row].length; j++) {
            matrix[row][j] = matrix[row][j].divide(pivot, PRECISION, ROUNDING_MODE);
        }
    }

    private void eliminateRow(BigDecimal[][] matrix, int targetRow, int pivotRow) {
        BigDecimal factor = matrix[targetRow][pivotRow];
        for (int j = 0; j < matrix[targetRow].length; j++) {
            BigDecimal value = factor.multiply(matrix[pivotRow][j]);
            matrix[targetRow][j] = matrix[targetRow][j].subtract(value).setScale(PRECISION,ROUNDING_MODE);
        }
    }

    private void printMatrix(BigDecimal[][] matrix) {
        for (BigDecimal[] row : matrix) {
            for (BigDecimal val : row) {
                System.out.print(val+" ");
            }
            System.out.println();
        }
    }
}
