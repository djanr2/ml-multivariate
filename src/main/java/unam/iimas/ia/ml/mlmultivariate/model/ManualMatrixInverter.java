package unam.iimas.ia.ml.mlmultivariate.model;

public class ManualMatrixInverter {

    public static void main(String[] args) {
        double[][] matrix = {
                {2, 1, 1, 0, 0, 0},
                {4, 3, 3, 1, 0, 0},
                {8, 7, 9, 5, 1, 0},
                {6, 7, 9, 8, 6, 4},
                {4, 5, 7, 9, 10, 8},
                {2, 3, 5, 7, 9, 11}
        };

        try {
            double[][] inverse = invert(matrix);
            System.out.println("Inverse matrix:");
            printMatrix(inverse);
        } catch (IllegalArgumentException e) {
            System.out.println("Matrix is singular or not square: " + e.getMessage());
        }
    }

    public static double[][] invert(double[][] matrix) {
        int n = matrix.length;
        if (n != matrix[0].length) {
            throw new IllegalArgumentException("Matrix must be square");
        }

        // Create augmented matrix [A|I]
        double[][] augmented = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, n);
            augmented[i][i + n] = 1;
        }

        // Forward elimination
        for (int i = 0; i < n; i++) {
            // Find pivot
            double pivot = augmented[i][i];
            if (pivot == 0) {
                // Search for non-zero pivot and swap
                boolean swapped = false;
                for (int j = i + 1; j < n; j++) {
                    if (augmented[j][i] != 0) {
                        double[] temp = augmented[i];
                        augmented[i] = augmented[j];
                        augmented[j] = temp;
                        pivot = augmented[i][i];
                        swapped = true;
                        break;
                    }
                }
                if (!swapped) throw new IllegalArgumentException("Matrix is singular and not invertible.");
            }

            // Normalize pivot row
            for (int j = 0; j < 2 * n; j++) {
                augmented[i][j] /= pivot;
            }

            // Eliminate other rows
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmented[k][i];
                    for (int j = 0; j < 2 * n; j++) {
                        augmented[k][j] -= factor * augmented[i][j];
                    }
                }
            }
        }

        // Extract inverse matrix from augmented
        double[][] inverse = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(augmented[i], n, inverse[i], 0, n);
        }

        return inverse;
    }

    public static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double val : row) {
                System.out.printf("%10.4f", val);
            }
            System.out.println();
        }
    }
}
