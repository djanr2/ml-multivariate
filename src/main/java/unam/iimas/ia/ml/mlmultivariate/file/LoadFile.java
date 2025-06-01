package unam.iimas.ia.ml.mlmultivariate.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LoadFile {
    private static final String PATH_DEFAULT = "src/main/resources/";
    private static final String FILE_DEFAULT = "data";
    private static final String EXTENTION_DEFAULT = ".txt";
    private List<BigDecimal[]> vectores;
    private File file;
    private BufferedReader br;
    private BigDecimal[] lowerLimitScale;
    private BigDecimal[] upperLimitScale;

    public LoadFile() {
        this(FILE_DEFAULT);
    }

    public LoadFile(String file_name) {
        vectores = new ArrayList<>();
        String[] variables;
        file = new File(PATH_DEFAULT+file_name+EXTENTION_DEFAULT);
        try {
            br = new BufferedReader(new FileReader(file));
            String linea;
            while ((linea = br.readLine()) != null) {
                variables= linea.split("\\s+");
                vectores.add(getVector(variables));
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception LoadFile");
        }
    }

    private BigDecimal[] getVector(String[] valoresFromDataVector){
        BigDecimal[] vector= new BigDecimal[valoresFromDataVector.length];
        for (int i = 0; i < valoresFromDataVector.length;i++){
            vector[i] = new BigDecimal(valoresFromDataVector[i]);
            if (this.lowerLimitScale == null && this.upperLimitScale== null){
                this.lowerLimitScale = new BigDecimal[valoresFromDataVector.length];
                this.upperLimitScale = new BigDecimal[valoresFromDataVector.length];
            }else {
                if(this.lowerLimitScale[i] == null ){
                    this.lowerLimitScale[i] = vector[i];
                }else {
                    if( this.lowerLimitScale[i].compareTo(vector[i]) > 0){
                        this.lowerLimitScale[i] = vector[i];
                    }
                }
                if(this.upperLimitScale[i] == null ){
                    this.upperLimitScale[i] = vector[i];
                }else {
                    if( this.upperLimitScale[i].compareTo(vector[i]) < 0){
                        this.upperLimitScale[i] = vector[i];
                    }
                }
            }
        }
        return vector;
    }
    public String vectorToString(BigDecimal[] vector){
        String vectorString = "";
        for (BigDecimal bd:
                vector) {
            vectorString += (bd + "\t");
        }
        return vectorString;
    }

    public List<BigDecimal[]> getVectores() {
        return this.vectores;
    }

    public BigDecimal[] getLowerLimitScale() {
        return lowerLimitScale;
    }

    public BigDecimal[] getUpperLimitScale() {
        return upperLimitScale;
    }
}

