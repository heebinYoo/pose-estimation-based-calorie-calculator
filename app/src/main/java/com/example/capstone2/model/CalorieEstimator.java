package com.example.capstone2.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone2.database.vo.WorkTimeAndCalorie;
import com.example.capstone2.model.neuralCounter.NeuralCounterManager;
import com.example.capstone2.model.neuralCounter.WorknetFactory;
import com.example.capstone2.model.util.BitmapResizer;
import com.example.capstone2.model.util.TimestampedBitmap;
import com.example.capstone2.model.util.TimestampedPerson;
import com.example.capstone2.model.util.normalized.NormalizedPerson;
import com.example.capstone2.util.PreferenceKeys;
import com.example.capstone2.util.PreferenceManager;

import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Posenet;
import org.tensorflow.lite.examples.posenet.lib.Worknet;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class CalorieEstimator {
    private static final int POSENET_THREAD = 4;
    private Exercise exercise;
    private AppCompatActivity activityContext;

    private BlockingQueue<TimestampedBitmap> imageQueue = new ArrayBlockingQueue<TimestampedBitmap>(4096);
    private PriorityBlockingQueue<TimestampedPerson> personQueue = new PriorityBlockingQueue<>(1024);

    private ArrayList<PosenetRunnable> posenetRunnables = new ArrayList<>();


    private NeuralCounterManager neuralCounterManager;
    private Thread neuralCounterThread;

    private WorknetFactory worknetFactory;

    public CalorieEstimator(Exercise exercise, AppCompatActivity activityContext){
        this.exercise = exercise;
        this.activityContext = activityContext;
        this.worknetFactory = new WorknetFactory();

        for(int i=0; i<POSENET_THREAD; i++){
            PosenetRunnable posenetRunnable
                    = new PosenetRunnable(activityContext,
                    imageQueue,
                    personQueue,
                    worknetFactory.buildWorknet(exercise, activityContext));
            new Thread(posenetRunnable).start();
            posenetRunnables.add(posenetRunnable);
        }
        neuralCounterManager = new NeuralCounterManager(personQueue, activityContext);
        neuralCounterThread = new Thread(neuralCounterManager);
        neuralCounterThread.start();
    }

    //give any size of bitmap, this class will handle it in thread, for speed reason
    //put bitmap is automatically recyled
    public void put(TimestampedBitmap image){
        try {
            imageQueue.put(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public WorkTimeAndCalorie stop() {
        Iterator<PosenetRunnable> itr = posenetRunnables.iterator();
        while(itr.hasNext()){
            itr.next().stop();
        }
        neuralCounterManager.stop();
        try {
            neuralCounterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        WorkTimeAndCalorie workTimeAndCalorie = new WorkTimeAndCalorie();

        workTimeAndCalorie.mills = neuralCounterManager.getWorkingTime();

        int weight = PreferenceManager.getInt(activityContext, PreferenceKeys.weight);
        if (weight == PreferenceManager.DEFAULT_VALUE_INT) {
            //디폴트 체중이 65키로라고 생각함
            weight = 65;
        }
        workTimeAndCalorie.calorie = 6 * weight * ((double) neuralCounterManager.getWorkingTime()/(1000*60*60));
        return workTimeAndCalorie;
    }
}





class PosenetRunnable implements Runnable{
    private Posenet posenet;
    private BlockingQueue<TimestampedBitmap> imageQueue;
    private PriorityBlockingQueue<TimestampedPerson> personQueue;
    private boolean terminate = false;
    private Worknet worknet;

    PosenetRunnable(Context context,
                    BlockingQueue<TimestampedBitmap> imageQueue,
                    PriorityBlockingQueue<TimestampedPerson> personQueue,
                    Worknet worknet
                    ){
        this.posenet = new Posenet(context);
        this.imageQueue = imageQueue;
        this.personQueue = personQueue;
        this.worknet = worknet;
    }

    @Override
    public void run() {
        while(true) {
            if (terminate)
                break;

            try {
                TimestampedBitmap timestampedBitmap = imageQueue.take();


                Bitmap resizedBitmap = BitmapResizer.getResizedBitmap(timestampedBitmap.bitmap, 257, 257);


                Person person = posenet.estimateSinglePose(resizedBitmap);
                ///재사용 금지!!!!!!!
                timestampedBitmap.bitmap.recycle();

                person.mark = worknet.estimateSinglePose(new NormalizedPerson(person).getFlattenKeyPointArray());



                TimestampedPerson timestampedPerson = new TimestampedPerson(timestampedBitmap.timestamp, person);
                personQueue.put(timestampedPerson);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Log.i("CalorieEstimator", "imagequeue "+ this.imageQueue.size() + " personqueue "+this.personQueue.size());
        }
    }

    public void stop() {
        this.terminate = true;

    }
}