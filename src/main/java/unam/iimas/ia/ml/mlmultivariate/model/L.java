package unam.iimas.ia.ml.mlmultivariate.model;

import java.util.Random;

public class L {
    private static final int POTENCIA_MAXIMA = 122; // 122
    private static final int POTENCIA_MINIMA = 1; // 122
    public static final int powers[] = {1,3,5,7,9,
                                        11,15,21,25,27,
                                        33,35,45,49,55,
                                        63,77,81,99,121};

    public static int getRandomLPower(){
        Random random = new Random();
        int randVal = random.nextInt(POTENCIA_MINIMA,POTENCIA_MAXIMA);
        return getMinPower(randVal);
    }

    public static int getMinPower(int power){
        int l = 0;
        for (int li:
                L.powers) {
            if(li > power){
                break;
            }
            l = li;
        }
        return l;
    }



}
