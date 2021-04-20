package com.example.capstone2.model;


import android.content.Context;
import android.util.Log;

import com.example.capstone2.model.util.TimestampedBitmap;
import com.example.capstone2.model.util.TimestampedPerson;

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
    private PriorityBlockingQueue<TimestampedPerson> personQueue = new PriorityBlockingQueue<TimestampedPerson>(1024);



    public CalorieEstimator(Exercise exercise, Context context){
        this.exercise = exercise;
        this.context = context;

        for(int i=0; i<POSENET_THREAD; i++){
            new Thread(new PosenetRunnable(context, imageQueue, personQueue)).start();
        }
        new Thread(new DTWTaskManager(personQueue)).start();
    }

    public void put(TimestampedBitmap image){
        try {
            imageQueue.put(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}


class DTWTaskManager implements Runnable{
    private PriorityBlockingQueue<TimestampedPerson> personQueue;
    private ArrayList<DTWTask> dtwTasks = new ArrayList<>();
    private ArrayList<DTWTask> terminated = new ArrayList<>();

    public DTWTaskManager(PriorityBlockingQueue<TimestampedPerson> personQueue){
        this.personQueue = personQueue;
    }


    @Override
    public void run() {

        // 이 루프를 죽이고, 지금까지 나온 후보 dtw들을 어딘가에서 받아서 정리 - 칼로리화 해주는 방법을 마련해야 할 것
        // dtw1번이랑 dtw2번이랑 1초 이상 곂치면 같은거로 일단 세버리는 전략
        while (true) {

            try {
                TimestampedPerson timestampedPerson = personQueue.take();

                if (timestampedPerson.person.mark)
                    dtwTasks.add(new DTWTask(timestampedPerson.timestamp));

                Iterator<DTWTask> iter = dtwTasks.iterator();

                while (iter.hasNext()) {
                    DTWTask task = iter.next();
                    if (!task.continueTask(timestampedPerson)) {
                        terminated.add(task);
                        iter.remove();
                    }
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class DTWTask {
    private static final int TERMINATE_THRESH_HOLD = 5;
    private long startTimestamp;
    private boolean terminated = false;
    private ArrayList<timeAndScore> scores = new ArrayList<>();

    private double bestScore;
    private long bestTermindateTime;


    public DTWTask(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public double getScore(){
        return bestScore;
    }
    public long getStart(){
        return startTimestamp;
    }
    public long getTerm() {
        return bestTermindateTime;
    }

    public boolean continueTask(TimestampedPerson timestampedPerson){
        Person person = timestampedPerson.person;

        if(terminated)
            return false;

        //calc DTW
        if(person.mark){
            scores.add(new timeAndScore(timestampedPerson.timestamp, 2.0));
            if(scores.size() > TERMINATE_THRESH_HOLD){
                terminated = true;
                timeAndScore ts = Collections.max(scores);
                bestScore = ts.score;
                bestTermindateTime = ts.time;
                return false;
            }
        }

        return true;

    }

    class timeAndScore implements Comparable<timeAndScore>{
        public double score;
        public long time;

        public timeAndScore( long time, double score) {
            this.score = score;
            this.time = time;
        }

        @Override
        public int compareTo(timeAndScore o) {
            return Double.compare(this.score, o.score);
        }
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
                Person person = posenet.estimateSinglePose(timestampedBitmap.bitmap);
                double[] target = new double[34];
                int i = 0;
                for (KeyPoint kp : person.keyPoints){
                    target[i++] = (double) kp.position.x/257;
                    target[i++] = (double) kp.position.y/257;
                }
                person.mark = StablePoseClassifier.forward(target);
                personQueue.put(new TimestampedPerson(timestampedBitmap.timestamp, person));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.i("CalorieEstimator", "imagequeue "+ this.imageQueue.size() + " personqueue "+this.personQueue.size());
        }
    }
}