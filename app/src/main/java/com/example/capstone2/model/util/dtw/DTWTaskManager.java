package com.example.capstone2.model.util.dtw;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.capstone2.R;
import com.example.capstone2.model.util.TimestampedPerson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class DTWTaskManager implements Runnable{
    private PriorityBlockingQueue<TimestampedPerson> personQueue;
    private ArrayList<DTWTask> dtwTasks = new ArrayList<>();
    private ArrayList<DTWTask> terminated = new ArrayList<>();

    private PoseCsvHelper poseCsvHelper = null;

    public DTWTaskManager(PriorityBlockingQueue<TimestampedPerson> personQueue, Context context){
        this.personQueue = personQueue;
        try {
            InputStream inputStream =  context.getResources().openRawResource(R.raw.sample0);
            InputStreamReader inputStreamReader =  new InputStreamReader(inputStream);
            this.poseCsvHelper = new PoseCsvHelper(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        // TODO 이 루프를 죽이고, 지금까지 나온 후보 dtw들을 어딘가에서 받아서 정리 - 칼로리화 해주는 방법을 마련해야 할 것
        // dtw 1번이랑 dtw 2번이랑 1초 이상 곂치면 같은거로 일단 세버리는 전략
        while (true) {

            Log.i("DTWTaskManager", "dtwTasks : "+ dtwTasks.size() + " terminated :" + terminated.size());

            try {
                TimestampedPerson timestampedPerson = personQueue.take();

                if (timestampedPerson.person.mark)
                    dtwTasks.add(new DTWTask(timestampedPerson.timestamp, this.poseCsvHelper.getPoseList()));

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