package unam.iimas.ia.ml.mlmultivariate.model;

import java.util.Random;

public class L {
    public static final int powers[] = {0,1,3,5,7,9,
                                        11,15,21,25,27,
                                        33,35,45,49,55,
                                        63,77,81,99,121};
    public static int getIndexPower(int i){
        return L.powers[i];
    }
}
