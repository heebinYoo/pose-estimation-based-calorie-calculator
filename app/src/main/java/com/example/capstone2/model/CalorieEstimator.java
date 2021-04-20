package com.example.capstone2.model;


import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Posenet;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CalorieEstimator {
    private static final int POSENET_THREAD = 4;

    private Exercise exercise;
    private Context context;

    private BlockingQueue<Bitmap> imageQueue = new ArrayBlockingQueue<Bitmap>(1024);
    private BlockingQueue<Person> personQueue = new ArrayBlockingQueue<Person>(1024);



    public CalorieEstimator(Exercise exercise, Context context){
        this.exercise = exercise;
        this.context = context;

        for(int i=0; i<POSENET_THREAD; i++){
            new Thread(new PosenetRunnable(context, imageQueue, personQueue)).start();
        }
    }

    public void put(Bitmap image){
        try {
            imageQueue.put(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}

class PosenetRunnable implements Runnable{
    private Posenet posenet;
    private BlockingQueue<Bitmap> imageQueue;
    private BlockingQueue<Person> personQueue;


    PosenetRunnable(Context context, BlockingQueue<Bitmap> imageQueue, BlockingQueue<Person> personQueue){
        this.posenet = new Posenet(context);
        this.imageQueue = imageQueue;
        this.personQueue = personQueue;

    }

    @Override
    public void run() {
        while(true) {
            try {
                Person person = posenet.estimateSinglePose(imageQueue.take());
                double[] target = new double[34];
                int i = 0;
                for (KeyPoint kp : person.keyPoints){
                    target[i++] = (double) kp.position.x/257;
                    target[i++] = (double) kp.position.y/257;
                }

                person.mark = StablePoseClassifier.forward(target);
                personQueue.put(person);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}