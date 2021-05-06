package com.example.capstone2.model.util.dtw;

import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone2.R;
import com.example.capstone2.model.util.TimestampedPerson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class DTWTaskManager implements Runnable{
    private final String TAG = "DTWTaskManager";
    private PriorityBlockingQueue<TimestampedPerson> personQueue;
    private ArrayList<DTWTask> dtwTasks = new ArrayList<>();
    private ArrayList<DTWTask> terminated = new ArrayList<>();

    private PoseCsvHelper poseCsvHelper = null;

    private AppCompatActivity activityContext;
    private final long IGNORE_THRESHOLD = 1500;
    private long lastInitTime;


    private boolean killSignal = false;

    public DTWTaskManager(PriorityBlockingQueue<TimestampedPerson> personQueue, AppCompatActivity activityContext){
        this.personQueue = personQueue;
        this.activityContext = activityContext;
        try {
            InputStream inputStream =  activityContext.getResources().openRawResource(R.raw.totaled);
            InputStreamReader inputStreamReader =  new InputStreamReader(inputStream);
            this.poseCsvHelper = new PoseCsvHelper(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastInitTime = -IGNORE_THRESHOLD *2;

    }


    @Override
    public void run() {
        while (true) {
            if(killSignal)
                break;



            //Log.i(TAG, "dtwTasks : "+ dtwTasks.size() + " terminated :" + terminated.size());

            try {
                TimestampedPerson timestampedPerson = personQueue.take();


                activityContext.runOnUiThread(new Runnable() {
                    TextView textView = activityContext.findViewById(R.id.currentTimeText);
                    @Override
                    public void run() {
                        textView.setText(" now : " + timestampedPerson.timestamp);
                    }
                });


                if(timestampedPerson.person.mark) {
                    if(timestampedPerson.timestamp > 1000 && IGNORE_THRESHOLD < timestampedPerson.timestamp - lastInitTime){

                        activityContext.runOnUiThread(new Runnable() {
                            TextView textView = activityContext.findViewById(R.id.dtwinitText);
                            @Override
                            public void run() {
                                textView.setText("new dtw initialized : " + timestampedPerson.timestamp);
                            }
                        });

                        dtwTasks.add(new DTWTask(timestampedPerson.timestamp, this.poseCsvHelper.getPoseList()));
                        lastInitTime = timestampedPerson.timestamp;
                    }
                    else{
                        timestampedPerson.person.mark = false;
                    }

                }

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

        finalizing();
        Log.i(TAG, "run: done");
    }


    private void finalizing(){
        Iterator<DTWTask> iter = dtwTasks.iterator();
        while (iter.hasNext()) {
            DTWTask task = iter.next();
            task.terminate();
            if(task.getScore()!=-1 || task.getStart()!=task.getEnd())
                terminated.add(task);
            iter.remove();
        }
        // TODO 이 루프를 죽이고, 지금까지 나온 후보 dtw들을 어딘가에서 받아서 정리 - 칼로리화 해주는 방법을 마련해야 할 것
        // dtw 1번이랑 dtw 2번이랑 1초 이상 곂치면 같은거로 일단 세버리는 전략


        long workingTime = 0;
        for (DTWTask t:terminated) {
            Log.i(TAG, "finalizing: score : " +  t.getScore() +" start " + t.getStart() + " end " + t.getEnd() + " diff : " + (t.getEnd() - t.getStart()));
            workingTime += t.getEnd() - t.getStart();
        }



        terminated.size();



    }


    public void stop() {
        killSignal = true;
    }
}