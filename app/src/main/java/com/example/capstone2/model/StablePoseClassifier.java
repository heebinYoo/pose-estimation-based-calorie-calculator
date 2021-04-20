package com.example.capstone2.model;

import static java.lang.Math.exp;

public class StablePoseClassifier {



    private static final double [] coef = {45.43263085,   46.67112692,  -77.23653037,   32.76223053,
            56.06332496,   11.06783437, -117.35149251,  -18.79425469,
            -13.10738348,  -99.08577211,   37.74214548,   84.94075452,
            -9.28890777,   17.3270762 ,  -35.64682317,  -48.65400637,
            -17.40679191,  138.71218573,   17.67571014,  -24.62740834,
            29.68937579,   55.82778689,   69.18884522, -280.58164487,
            93.82847419,  -69.89482466,  181.82176581,  -63.19020833,
            -64.80461226,  -48.19845133,  228.68449539,  -46.91235689,
            -187.67764228,   63.69826004};
    private static final double intercept = -13.965912;


    public static boolean forward(double[] data){
        double result = intercept;
        for(int i=0; i < 34; i++)
            result += coef[i] * data[i];

        result = exp(result);

        result = result / (1+result);


        return result > 0.5;
    }


}
