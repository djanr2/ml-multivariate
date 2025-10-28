package unam.iimas.ia.ml.mlmultivariate.model;

public record IdSwap(int first, int second) {
    // Constructor auxiliar opcional
    public static IdSwap of(int first, int second) {
        return new IdSwap(first, second);
    }
    @Override
    public String toString() {
        return "[" + first + ", " + second + "]";
    }
}
