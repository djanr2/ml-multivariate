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
    private static final String PREFIX_VARIABLE_NAME = "X";
    private static final int VARIABLE_NUMBER_DIGITS = 2;
    private static final char CHAR_LEFT_PADDING = '0';
    private int numeroVariables;
    private List<BigDecimal[]> vectores;
    private File file;
    private BufferedReader br;
    private BigDecimal[] lowerLimitScale;
    private BigDecimal[] upperLimitScale;
    private String[] variableNames;

    private static boolean fileHasHeaders = false;
    private static boolean variableNamesFullfilled = false;

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
                if(fileHasHeaders && !variableNamesFullfilled){
                    variableNames = variables;
                    variableNamesFullfilled = !variableNamesFullfilled;
                }else {
                    if(!variableNamesFullfilled){
                        variableNames = getVariablesNames(variables.length);
                        variableNamesFullfilled = !variableNamesFullfilled;
                    }
                    vectores.add(getVector(variables));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception LoadFile");
        }
        numeroVariables = vectores.get(0).length-1;
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

    private String[] getVariablesNames(int numVariables){
        String[] varaibleNames= new String[numVariables];
        for (int i = 0;i<numVariables; i++){
            varaibleNames[i] = PREFIX_VARIABLE_NAME +
                    String.format("%" + VARIABLE_NUMBER_DIGITS + "s", ""+(i+1)).
                            replace(' ', CHAR_LEFT_PADDING);
        }
        return varaibleNames;
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

    public String[] getVariableNames() {
        return variableNames;
    }

    public int getNumeroVariables() {
        return numeroVariables;
    }
}

