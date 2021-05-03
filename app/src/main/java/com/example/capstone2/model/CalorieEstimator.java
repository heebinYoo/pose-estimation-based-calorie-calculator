package com.example.capstone2.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.capstone2.model.util.BitmapResizer;
import com.example.capstone2.model.util.TimestampedBitmap;
import com.example.capstone2.model.util.TimestampedPerson;
import com.example.capstone2.model.util.dtw.DTWTaskManager;

import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Posenet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class CalorieEstimator {
    private static final int POSENET_THREAD = 4;
    private Exercise exercise;
    private Context context;

    private BlockingQueue<TimestampedBitmap> imageQueue = new ArrayBlockingQueue<TimestampedBitmap>(4096);
    private PriorityBlockingQueue<TimestampedPerson> personQueue = new PriorityBlockingQueue<>(1024);

    private DTWTaskManager dtwTaskManager;

    public CalorieEstimator(Exercise exercise, Context context){
        this.exercise = exercise;
        this.context = context;

        for(int i=0; i<POSENET_THREAD; i++){
            new Thread(new PosenetRunnable(context, imageQueue, personQueue)).start();
        }
        dtwTaskManager = new DTWTaskManager(personQueue, context);
        new Thread(dtwTaskManager).start();
    }

    //give any size of bitmap, this class will handle it in thread, for speed reason
    public void put(TimestampedBitmap image){
        try {
            imageQueue.put(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        dtwTaskManager.stop();

    }
}





class PosenetRunnable implements Runnable{
    private Posenet posenet;
    private BlockingQueue<TimestampedBitmap> imageQueue;
    private PriorityBlockingQueue<TimestampedPerson> personQueue;


    PosenetRunnable(Context context, BlockingQueue<TimestampedBitmap> imageQueue, PriorityBlockingQueue<TimestampedPerson> personQueue){
        this.posenet = new Posenet(context);
        this.imageQueue = imageQueue;
        this.personQueue = personQueue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                TimestampedBitmap timestampedBitmap = imageQueue.take();

//                long start, end; // 서버에서 가져오는 시간 측정
//                start = System.currentTimeMillis();
//                end = System.currentTimeMillis();
//                Log.i("time", "run: " + (end - start));

                Bitmap resizedBitmap = BitmapResizer.getResizedBitmap(timestampedBitmap.bitmap, 257, 257);


                Person person = posenet.estimateSinglePose(resizedBitmap);
                ///재사용 금지!!!!!!!
                timestampedBitmap.bitmap.recycle();
                double[] target = new double[34];
                int i = 0;
                for (KeyPoint kp : person.keyPoints){
                    target[i++] = (double) kp.position.x/257;
                    target[i++] = (double) kp.position.y/257;
                }
                person.mark = StablePoseClassifier.forward(target);
                TimestampedPerson timestampedPerson = new TimestampedPerson(timestampedBitmap.timestamp, person);
                personQueue.put(timestampedPerson);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.i("CalorieEstimator", "imagequeue "+ this.imageQueue.size() + " personqueue "+this.personQueue.size());
        }
    }
}