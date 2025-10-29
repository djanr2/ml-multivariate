package unam.iimas.ia.ml.mlmultivariate.run;

import java.util.Random;

public class Playgrond {
    public static void main(String[] args) {
        Random r = new Random();
        double x,y;

        for (int i = 0; i < 100; i++) {
            x = r.nextDouble();
            y = r.nextDouble();
            functionX2Y2(x,y);
        }

    }

    public static void functionX2Y2(double x, double y){
        double fx_=(x*x) + (y*y);
        System.out.print(x+"\t"+y+"\t"+fx_);
        System.out.println();
    }
}
