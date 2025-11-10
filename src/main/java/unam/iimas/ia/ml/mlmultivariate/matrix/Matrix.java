package unam.iimas.ia.ml.mlmultivariate.matrix;

import unam.iimas.ia.ml.mlmultivariate.model.Precision;
import unam.iimas.ia.ml.mlmultivariate.model.Vector;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class Matrix {

    private static final MathContext MC = new MathContext(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
    public static BigDecimal[][] gaussianElimination(BigDecimal[][] a, BigDecimal[][] b) {
        BigDecimal[][] aCopy = cloneMatrix(a);
        BigDecimal[][] bCopy = cloneMatrix(b);
        int n = bCopy.length;

        for (int pivot = 0; pivot < n; pivot++) {
            BigDecimal pivotVal = aCopy[pivot][pivot];
            for (int j = pivot; j < n; j++) {
                aCopy[pivot][j] = aCopy[pivot][j].divide(pivotVal, MC);
            }
            bCopy[pivot][0] =  bCopy[pivot][0].divide(pivotVal, MC);
            for (int i = pivot + 1; i < n; i++) {
                BigDecimal factor = aCopy[i][pivot];
                for (int j = pivot; j < n; j++) {
                    aCopy[i][j] = aCopy[i][j].subtract(factor.multiply(aCopy[pivot][j]));
                }
                bCopy[i][0] = bCopy[i][0].subtract(factor.multiply(bCopy[pivot][0]));
            }
        }
        BigDecimal[][] x = new BigDecimal[n][1];
        for (int i = n - 1; i >= 0; i--) {
            x[i][0] = bCopy[i][0];
            for (int j = i + 1; j < n; j++) {
                x[i][0] = x[i][0].subtract(aCopy[i][j].multiply(x[j][0])).setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
            }
        }
        return x;
    }

    public static BigDecimal[][] gaussianElimination(BigDecimal[][] augmentedMatrix) {
        int rows = augmentedMatrix.length;
        int cols = augmentedMatrix[0].length;

        BigDecimal[][] a = new BigDecimal[rows][cols - 1];
        BigDecimal[][] b = new BigDecimal[rows][1];

        for (int i = 0; i < rows; i++) {
            // Copy all but last column into A
            for (int j = 0; j < cols - 1; j++) {
                a[i][j] = augmentedMatrix[i][j];
            }
            // Last column is b
            b[i][0] = augmentedMatrix[i][cols - 1];
        }
        return gaussianElimination(a, b);
    }

    public static BigDecimal[][] getGaussiaSolution(MatrixObject matrix){
        return gaussianElimination(matrix.getMatrix(), matrix.getVectorSolution());
    }

    public static BigDecimal[][] invertMatrix(BigDecimal[][] matrix) {
        int size = matrix.length;
        BigDecimal[][] original;
        if (size != matrix[0].length)
            throw new IllegalArgumentException("Matrix must be square.");
        //Copy Matrix into another
        original = new BigDecimal[size][size];
        for (int i = 0; i < size; i++)
            System.arraycopy(matrix[i], 0, original[i], 0, size);
        BigDecimal[][] augmented = createAugmentedMatrix(size, original);
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

    private static BigDecimal[][] createAugmentedMatrix(int size, BigDecimal[][] original) {
        BigDecimal[][] augmented = new BigDecimal[size][2 * size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(original[i], 0, augmented[i], 0, size);
            for (int j = 0; j < size; j++) {
                augmented[i][j + size] = (i == j) ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        }
        return augmented;
    }

    private static void swapRows(BigDecimal[][] matrix, int row1, int row2) {
        BigDecimal[] temp = matrix[row1];
        matrix[row1] = matrix[row2];
        matrix[row2] = temp;
    }

    private static void normalizeRow(BigDecimal[][] matrix, int row, BigDecimal pivot) {
        for (int j = 0; j < matrix[row].length; j++) {
            matrix[row][j] = matrix[row][j].divide(pivot,MC);
        }
    }

    private static void eliminateRow(BigDecimal[][] matrix, int targetRow, int pivotRow) {
        BigDecimal factor = matrix[targetRow][pivotRow];
        for (int j = 0; j < matrix[targetRow].length; j++) {
            BigDecimal value = factor.multiply(matrix[pivotRow][j]);
            matrix[targetRow][j] = matrix[targetRow][j].subtract(value).setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
        }
    }

    public static void print(BigDecimal[][] matrix) {
        for (BigDecimal[] row : matrix) {
            for (BigDecimal val : row) {
                System.out.print(val+" ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static BigDecimal getCofactor(int row, int column, BigDecimal[][] matrix) {
        BigDecimal[][] minor = getMinor(matrix, row, column);
        BigDecimal detMinor = determinant(minor);

        // Calcular el signo (-1)^(i+j)
        int sign = ((row + column) % 2 == 0) ? 1 : -1;
        return detMinor.multiply(BigDecimal.valueOf(sign));
    }

    public static BigDecimal[][] getMinor(BigDecimal[][] matrix, int rowToRemove, int colToRemove) {
        int size = matrix.length;
        BigDecimal[][] minor = new BigDecimal[size - 1][size - 1];

        int r = 0;
        for (int i = 0; i < size; i++) {
            if (i == rowToRemove) continue;
            int c = 0;
            for (int j = 0; j < size; j++) {
                if (j == colToRemove) continue;
                minor[r][c] = matrix[i][j];
                c++;
            }
            r++;
        }

        return minor;
    }
    public static BigDecimal determinant(BigDecimal[][] matrix) {
        int n = matrix.length;

        if (n == 1) {
            return matrix[0][0];
        }

        if (n == 2) {
            return matrix[0][0].multiply(matrix[1][1]).subtract(matrix[0][1].multiply(matrix[1][0]))
                    .setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
        }

        BigDecimal det = BigDecimal.ZERO;

        for (int j = 0; j < n; j++) {
            BigDecimal[][] minor = getMinor(matrix, 0, j);
            BigDecimal cofactor = matrix[0][j].multiply(determinant(minor)).setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
            if (j % 2 == 0) {
                det = det.add(cofactor);
            } else {
                det = det.subtract(cofactor);
            }
        }

        return det.setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
    }

    public static BigDecimal[][] mul(BigDecimal[][] a, BigDecimal[][] b) {
        int filasA = a.length;
        int columnasA = a[0].length;
        int filasB = b.length;
        int columnasB = b[0].length;

        // Validar dimensiones
        if (columnasA != filasB) {
            throw new IllegalArgumentException("Las dimensiones de las matrices no son compatibles para la multiplicación.");
        }

        // Crear matriz resultado
        BigDecimal[][] resultado = new BigDecimal[filasA][columnasB];

        // Inicializar resultado con ceros
        for (int i = 0; i < filasA; i++) {
            for (int j = 0; j < columnasB; j++) {
                resultado[i][j] = BigDecimal.ZERO;
            }
        }

        // Multiplicación de matrices
        for (int i = 0; i < filasA; i++) {
            for (int j = 0; j < columnasB; j++) {
                for (int k = 0; k < columnasA; k++) {
                    resultado[i][j] = resultado[i][j].add(a[i][k].multiply(b[k][j]))
                            .setScale(Precision.MIN_PRECISION, Precision.ROUNDING_MODE);
                }
            }
        }

        return resultado;
    }

    public static BigDecimal[][] getMatrixRow(BigDecimal[][] matrix, int row){
        BigDecimal[][] rowMatrix = new BigDecimal[1][matrix[0].length];
        for (int i = 0; i < matrix[0].length; i++) {
            rowMatrix[0][i] = matrix[row][i];
        }
        return rowMatrix;
    }

    public static BigDecimal[][] getMatrixCol(BigDecimal[][] matrix, int col){
        BigDecimal[][] rowMatrix = new BigDecimal[matrix.length][1];
        for (int j = 0; j < matrix.length; j++) {
            rowMatrix[j][0] = matrix[j][col];
        }
        return rowMatrix;
    }

    public static BigDecimal[][] addMatrices(BigDecimal[][] a, BigDecimal[][] b) {
        validateSameSize(a, b);
        int rows = a.length;
        int cols = a[0].length;
        BigDecimal[][] result = new BigDecimal[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j].add(b[i][j]);
            }
        }
        return result;
    }

    public static BigDecimal[][] subtractMatrices(BigDecimal[][] a, BigDecimal[][] b) {
        validateSameSize(a, b);
        int rows = a.length;
        int cols = a[0].length;
        BigDecimal[][] result = new BigDecimal[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j].subtract(b[i][j]);
            }
        }
        return result;
    }

    public static BigDecimal[][] transponer(BigDecimal[][] matriz) {
        int filas = matriz.length;
        int columnas = matriz[0].length;

        BigDecimal[][] transpuesta = new BigDecimal[columnas][filas];

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                transpuesta[j][i] = matriz[i][j];
            }
        }
        return transpuesta;
    }

    private static void validateSameSize(BigDecimal[][] a, BigDecimal[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            throw new IllegalArgumentException("Matrices must be the same size");
        }
    }

    public static BigDecimal[][] getLeftDeterminant(List<Vector> matrix){
        BigDecimal[][] v_ = new BigDecimal[matrix.size()][matrix.size()-1];

        for (int  i = 0 ; i < matrix.size(); i ++ ){
            v_[i]= Arrays.copyOf( matrix.get(i).getVector(),matrix.get(i).getVector().length - 1);
        }
        return v_;
    }

    public static BigDecimal[][] addFirstColumnONES(BigDecimal[][] matrix) {
        int filas = matrix.length;
        int columnas = matrix[0].length;
        BigDecimal[][] nuevaMatriz = new BigDecimal[filas][columnas + 1];
        for (int i = 0; i < filas; i++) {
            nuevaMatriz[i][0] = BigDecimal.ONE;
            for (int j = 0; j < columnas; j++) {
                nuevaMatriz[i][j + 1] = matrix[i][j];
            }
        }
        return nuevaMatriz;
    }

    public static BigDecimal[][] cloneMatrix(BigDecimal[][] a) {
        if (a == null) return null;
        BigDecimal[][] copy = new BigDecimal[a.length][];
        for (int i = 0; i < a.length; i++) {
            BigDecimal[] row = a[i];
            if (row == null) {
                copy[i] = null;
            } else {
                // BigDecimal es inmutable: copiar referencias de celdas es seguro
                copy[i] = new BigDecimal[row.length];
                System.arraycopy(row, 0, copy[i], 0, row.length);
            }
        }
        return copy;
    }
}
