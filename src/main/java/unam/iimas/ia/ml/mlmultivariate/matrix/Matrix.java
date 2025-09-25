package unam.iimas.ia.ml.mlmultivariate.matrix;


import unam.iimas.ia.ml.mlmultivariate.model.Precision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public class Matrix {

    private static final int PRECISION = Precision.MIN_PRECISION;
    private static final RoundingMode ROUNDING_MODE = Precision.ROUNDING_MODE;


    public static void main(String[] args) {
        BigDecimal[][] a = {
                {new BigDecimal(1), new BigDecimal(1), new BigDecimal(1), new BigDecimal(1)},
                {new BigDecimal(1), new BigDecimal(3), new BigDecimal(9), new BigDecimal(27)},
                {new BigDecimal(1), new BigDecimal(6), new BigDecimal(36), new BigDecimal(216)},
                {new BigDecimal(1), new BigDecimal(8), new BigDecimal(64), new BigDecimal(512)}
        };

        BigDecimal[][] b = {
                {new BigDecimal(2)},
                {new BigDecimal(5)},
                {new BigDecimal(3)},
                {new BigDecimal(4.5)}
        };


        BigDecimal[][] solution = gaussianElimination(a, b);

        System.out.printf("Solution:%n");
        System.out.printf("w = %.6f%n", solution[0][0]);
        System.out.printf("x = %.6f%n", solution[1][0]);
        System.out.printf("y = %.6f%n", solution[2][0]);
        System.out.printf("z = %.6f%n", solution[3][0]);


        BigDecimal[][] matrix = {
                {new BigDecimal(2), new BigDecimal(1), new BigDecimal(1), new BigDecimal(0), new BigDecimal(0), new BigDecimal(0)},
                {new BigDecimal(4), new BigDecimal(3), new BigDecimal(3), new BigDecimal(1), new BigDecimal(0), new BigDecimal(0)},
                {new BigDecimal(8), new BigDecimal(7), new BigDecimal(9), new BigDecimal(5), new BigDecimal(1), new BigDecimal(0)},
                {new BigDecimal(6), new BigDecimal(7), new BigDecimal(9), new BigDecimal(8), new BigDecimal(6), new BigDecimal(4)},
                {new BigDecimal(4), new BigDecimal(5), new BigDecimal(7), new BigDecimal(9), new BigDecimal(10), new BigDecimal(8)},
                {new BigDecimal(2), new BigDecimal(3), new BigDecimal(5), new BigDecimal(7), new BigDecimal(9), new BigDecimal(11)}
        };

        Matrix inverter = new Matrix();
        BigDecimal[][] inverse = inverter.invertMatrix(matrix);

        System.out.println("\nFinal Inverse Matrix:");
        inverter.printMatrix(inverse);

        BigDecimal[][] cofactor = {
                {new BigDecimal(1), new BigDecimal(2), new BigDecimal(3), new BigDecimal(3)},
                {new BigDecimal(1), new BigDecimal(-2), new BigDecimal(1), new BigDecimal(-2)},
                {new BigDecimal(1), new BigDecimal(1), new BigDecimal(2), new BigDecimal(-1)},
                {new BigDecimal(2), new BigDecimal(2), new BigDecimal(-1), new BigDecimal(-1)}
        };

        BigDecimal[][] cofactor2 = {
            {new BigDecimal(1),new BigDecimal( 3.4036574123), new BigDecimal( 0.7777104424), new BigDecimal( 2.5450038943)},
            {new BigDecimal(1),new BigDecimal(-3.2753809464), new BigDecimal(-1.2259223606), new BigDecimal(-1.2616234678)},
            {new BigDecimal(1),new BigDecimal(-2.4268597233), new BigDecimal(-1.6606458474), new BigDecimal(-1.1460239692)},
            {new BigDecimal(1),new BigDecimal(-0.2541887185), new BigDecimal(-3.5013090525), new BigDecimal( 2.4086533150)}
        };



        System.out.println("Cofactor");

        BigDecimal c= getCofactor(3,0, cofactor2);

        System.out.println(c);

        System.out.println("-----------------------------------------------------------------------");

        // A y B = A^{-1}
        BigDecimal[][] A = {
                {bd("1"), bd("2")},
                {bd("3"), bd("4")}
        };

        BigDecimal[][] B = {
                {bd("-2"), bd("1")},
                {bd("1.5"), bd("-0.5")}
        };

        // Reemplazamos la fila 0 con beta = [5, 6]
        BigDecimal[][] beta = {
                {bd("5"), bd("6")}
        };

        int fila = 0;

        System.out.println("Inversa original B = A^{-1}:");
        printMatrix(B);

        BigDecimal[][] B_actualizada = updateInverse(B, A, fila, beta);

        System.out.println("\nNueva inversa B' después de reemplazar fila " + fila + " con " + Arrays.deepToString(beta[0]) + ":");
        printMatrix(B_actualizada);

    }

    public static BigDecimal[][] gaussianElimination(BigDecimal[][] A, BigDecimal[][] b) {
        int n = b.length;

        for (int pivot = 0; pivot < n; pivot++) {
            BigDecimal pivotVal = A[pivot][pivot];
            for (int j = pivot; j < n; j++) {
                A[pivot][j] = A[pivot][j].divide(pivotVal, PRECISION, ROUNDING_MODE);
            }
            b[pivot][0] =  b[pivot][0].divide(pivotVal, PRECISION, ROUNDING_MODE);
            for (int i = pivot + 1; i < n; i++) {
                BigDecimal factor = A[i][pivot];
                for (int j = pivot; j < n; j++) {
                    A[i][j] = A[i][j].subtract(factor.multiply(A[pivot][j]));
                }
                b[i][0] = b[i][0].subtract(factor.multiply(b[pivot][0]));
            }
        }
        BigDecimal[][] x = new BigDecimal[n][1];
        for (int i = n - 1; i >= 0; i--) {
            x[i][0] = b[i][0];
            for (int j = i + 1; j < n; j++) {
                x[i][0] = x[i][0].subtract(A[i][j].multiply(x[j][0])).setScale(PRECISION, ROUNDING_MODE);
            }
        }
        return x;
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
            matrix[row][j] = matrix[row][j].divide(pivot, PRECISION, ROUNDING_MODE);
        }
    }

    private static void eliminateRow(BigDecimal[][] matrix, int targetRow, int pivotRow) {
        BigDecimal factor = matrix[targetRow][pivotRow];
        for (int j = 0; j < matrix[targetRow].length; j++) {
            BigDecimal value = factor.multiply(matrix[pivotRow][j]);
            matrix[targetRow][j] = matrix[targetRow][j].subtract(value).setScale(PRECISION,ROUNDING_MODE);
        }
    }

    public static void printMatrix(BigDecimal[][] matrix) {
        for (BigDecimal[] row : matrix) {
            for (BigDecimal val : row) {
                System.out.print(val+" ");
            }
            System.out.println();
        }
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
            return matrix[0][0].multiply(matrix[1][1]).subtract(matrix[0][1].multiply(matrix[1][0])).setScale(PRECISION, ROUNDING_MODE);
        }

        BigDecimal det = BigDecimal.ZERO;

        for (int j = 0; j < n; j++) {
            BigDecimal[][] minor = getMinor(matrix, 0, j);
            BigDecimal cofactor = matrix[0][j].multiply(determinant(minor)).setScale(PRECISION, ROUNDING_MODE);
            if (j % 2 == 0) {
                det = det.add(cofactor);
            } else {
                det = det.subtract(cofactor);
            }
        }

        return det.setScale(PRECISION, ROUNDING_MODE);
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
                    resultado[i][j] = resultado[i][j].add(a[i][k].multiply(b[k][j])).setScale(PRECISION, ROUNDING_MODE);
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

    //TODO TEOREMA DE LA MATRIZ INVERSA

    static final MathContext MC = new MathContext(6);  // Precisión arbitraria

    // Multiplica una matriz (m x n) por un vector columna (n x 1) → resultado (m x 1)
    public static BigDecimal[][] multiplyMatrixByColVector(BigDecimal[][] matrix, BigDecimal[][] colVector) {
        int m = matrix.length;
        int n = matrix[0].length;
        BigDecimal[][] result = new BigDecimal[m][1];
        for (int i = 0; i < m; i++) {
            result[i][0] = BigDecimal.ZERO;
            for (int j = 0; j < n; j++) {
                result[i][0] = result[i][0].add(matrix[i][j].multiply(colVector[j][0], MC), MC);
            }
        }
        return result;
    }

    // Producto punto entre un vector fila (1 x m) y un vector columna (m x 1) → escalar
    public static BigDecimal dotProduct(BigDecimal[][] rowVec, BigDecimal[][] colVec) {
        int n = rowVec[0].length;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            sum = sum.add(rowVec[0][i].multiply(colVec[i][0], MC), MC);
        }
        return sum;
    }

    // Producto externo: columna (m x 1) * fila (1 x n) = matriz (m x n)
    public static BigDecimal[][] outerProduct(BigDecimal[][] col, BigDecimal[][] row) {
        int m = col.length;
        int n = row[0].length;
        BigDecimal[][] result = new BigDecimal[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = col[i][0].multiply(row[0][j], MC);
            }
        }
        return result;
    }

    // Resta de matrices: A - B
    public static BigDecimal[][] subtractMatrices(BigDecimal[][] A, BigDecimal[][] B) {
        int m = A.length;
        int n = A[0].length;
        BigDecimal[][] result = new BigDecimal[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j].subtract(B[i][j], MC);
            }
        }
        return result;
    }

    // Escalar por matriz
    public static BigDecimal[][] scalarMultiply(BigDecimal[][] matrix, BigDecimal scalar) {
        int m = matrix.length;
        int n = matrix[0].length;
        BigDecimal[][] result = new BigDecimal[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = matrix[i][j].multiply(scalar, MC);
            }
        }
        return result;
    }

    // Clona una fila de A en forma de matriz 1xN
    public static BigDecimal[][] getRow(BigDecimal[][] A, int rowIndex) {
        int n = A[0].length;
        BigDecimal[][] row = new BigDecimal[1][n];
        for (int j = 0; j < n; j++) {
            row[0][j] = A[rowIndex][j];
        }
        return row;
    }

    // Clona una fila de B como vector fila 1xN
    public static BigDecimal[][] getRowFromMatrix(BigDecimal[][] B, int rowIndex) {
        int n = B[0].length;
        BigDecimal[][] row = new BigDecimal[1][n];
        for (int j = 0; j < n; j++) {
            row[0][j] = B[rowIndex][j];
        }
        return row;
    }

    // Actualiza la inversa B usando el vector beta y la fila cambiada
    public static BigDecimal[][] updateInverse(BigDecimal[][] B, BigDecimal[][] A, int rowIndex, BigDecimal[][] betaRow) {
        BigDecimal[][] a_i = getRow(A, rowIndex); // fila original
        int n = a_i[0].length;
        BigDecimal[][] u = new BigDecimal[1][n];

        for (int j = 0; j < n; j++) {
            u[0][j] = betaRow[0][j].subtract(a_i[0][j], MC);
        }

        // u como columna (transpuesta)
        BigDecimal[][] uCol = new BigDecimal[n][1];
        for (int i = 0; i < n; i++) {
            uCol[i][0] = u[0][i];
        }

        // Bu = B * u^T (columna)
        BigDecimal[][] Bu = multiplyMatrixByColVector(B, uCol);

        // e_i^T * B = fila i de B
        BigDecimal[][] row_i_B = getRowFromMatrix(B, rowIndex);

        // escalar: 1 + e_i^T B u^T
        BigDecimal denominator = BigDecimal.ONE.add(dotProduct(row_i_B, uCol), MC);

        if (denominator.abs().compareTo(new BigDecimal("1e-18")) < 0) {
            throw new ArithmeticException("Denominador cercano a cero: la matriz A' no es invertible.");
        }

        // outer = (Bu) * (e_i^T B)
        BigDecimal[][] outer = outerProduct(Bu, row_i_B);

        // correction = outer / denominator
        BigDecimal[][] correction = scalarMultiply(outer, BigDecimal.ONE.divide(denominator, MC));

        // B' = B - correction
        return subtractMatrices(B, correction);
    }

    // Atajo para crear BigDecimal
    private static BigDecimal bd(String val) {
        return new BigDecimal(val, MC);
    }







}
