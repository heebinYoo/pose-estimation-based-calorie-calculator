package com.example.capstone2;

import com.example.capstone2.model.util.TimestampedPerson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

import static java.lang.Math.abs;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void pbq_isCorrect() throws InterruptedException {
        PriorityBlockingQueue<Integer> personQueue = new PriorityBlockingQueue<>(1024);
        personQueue.put(1);
        personQueue.put(9);
        personQueue.put(2);
        personQueue.put(4);
        System.out.println(personQueue.take());
        System.out.println(personQueue.take());
        System.out.println(personQueue.take());
        System.out.println(personQueue.take());

    }

    @Test
    public void DTW_isCorrect(){

        double[] sig1 = {0,1,2,3,2,0};
        double[] sig2 = {1,2,3,2,0};

        ArrayList<double[]> matrix = new ArrayList<>();
        //시그널 2에서 데이터를 한개씩 보내준다.
        for(int i=0; i<sig2.length; i++){
            double current_input = sig2[i];

            double[] newRow = new double[sig1.length];

            if(i==0){
                newRow[0] = d(sig1[0], current_input);
                for(int j=1; j<sig1.length; j++){
                    newRow[j] = newRow[j-1] + d(sig1[j], current_input);
                }
            }
            else{
                newRow[0] = matrix.get(matrix.size()-1)[0] + d(sig1[0], current_input);
                for(int j=1; j<sig1.length; j++){
                    double num1 = newRow[j-1];
                    double num2 = matrix.get(i-1)[j];
                    double num3 = matrix.get(i-1)[j-1];
                    double res = (num1 < num2)? num1: num2;
                    res = (num3 < res)? num3: res;
                    newRow[j] = res + d(sig1[j], current_input);
                }
            }
            matrix.add(newRow);
        }


        System.out.println("d : "+matrix.get(matrix.size()-1)[sig1.length-1] / (sig1.length+sig2.length));


    }

    private double d(double i, double i1) {
        return abs(i-i1);
    }

}