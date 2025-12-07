package unam.iimas.ia.ml.mlmultivariate.faa;

import unam.iimas.ia.ml.mlmultivariate.file.LoadFile;

public class Properties {
    private final long seed;
    private final int numero_terminos;
    private final int maxima_potencia_l;
    private final LoadFile file;

    public Properties(long seed, int numero_terminos, int maxima_potencia_l, LoadFile file) {
        this.seed = seed;
        this.numero_terminos = numero_terminos;
        this.maxima_potencia_l = maxima_potencia_l;
        this.file = file;
    }

    public Properties(){
        this.seed = 141412341234123L;
        this.numero_terminos = 11;
        this.maxima_potencia_l = 15;
        this.file = new LoadFile();
    }

    public long getSeed() {
        return seed;
    }

    public LoadFile getFile() {
        return file;
    }

    public int getNumero_terminos() {
        return numero_terminos;
    }

    public int getMaxima_potencia_l() {
        return maxima_potencia_l;
    }
}
