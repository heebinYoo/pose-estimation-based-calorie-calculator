package com.example.capstone2.model.neuralCounter;

import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone2.ExerciseActivity;
import com.example.capstone2.PoseNetSimpleTest;
import com.example.capstone2.R;
import com.example.capstone2.model.util.TimestampedPerson;

import java.text.SimpleDateFormat;
import java.util.concurrent.PriorityBlockingQueue;

import static com.example.capstone2.ExerciseActivityKt.frameDuration;

public class NeuralCounterManager  implements Runnable{

    private final String TAG = "NeuralCounterManager";
    private PriorityBlockingQueue<TimestampedPerson> personQueue;
    private AppCompatActivity activityContext;
    private long workingTime = 0;
    private int count = 0;
    private boolean killSignal = false;

    private TimestampedPerson[] nearArray = new TimestampedPerson[7];
    private final int LAST_NEAR_ARRAY_ELT = 6;

    private final double UP_PROB_THRESHOLD = 0.60;//0.8;
    private final double DOWN_PROB_THRESHOLD = 0.40;//0.2;

    private int state=0;
    private long last_up = 0;
    private long last_down = 0;
    private double mean_time_of_upping = -1;
    private double mean_time_of_downing = -1;

    private float POSE_SCORE_THRESHOLD = 0.8f;

    private long startTime=0;
    private long idleFrame=0;

    public NeuralCounterManager(PriorityBlockingQueue<TimestampedPerson> personQueue, AppCompatActivity activityContext){
        this.personQueue = personQueue;
        this.activityContext = activityContext;
    }

    @Override
    public void run() {


        for (int i = 0; i < LAST_NEAR_ARRAY_ELT; i++) {
            TimestampedPerson timestampedPerson;
            try {
                float score;
                do {
                    timestampedPerson = personQueue.take();

                    if(timestampedPerson.person==null)
                        return;
                    score = timestampedPerson.person.score;
                    //Log.i(TAG, "run: score "+score);
                } while(score<POSE_SCORE_THRESHOLD);
            } catch (InterruptedException e) {
                continue;
            }
            nearArray[i] = timestampedPerson;
        }




        while (true) {
            TimestampedPerson timestampedPerson;
            try {
                float score = 0;
                do {
                    timestampedPerson = personQueue.take();
                    if (timestampedPerson.person != null) {
                        score = timestampedPerson.person.score;
                        idleFrame++;
                    }
                    else {
                        break;
                    }
                } while(score<POSE_SCORE_THRESHOLD);
                idleFrame--;

                if(timestampedPerson.person==null)
                    break;
                //Log.i(TAG, "run: score "+score);
            } catch (InterruptedException e) {
                continue;
            }


            //if(killSignal)
                //Log.d(TAG, "?????? ???????????? ?????? ????????? ??? : "+personQueue.size());
                //activityContext.runOnUiThread(() -> Toast.makeText(activityContext,"?????? ???????????? ?????? ????????? ??? : "+personQueue.size(),Toast.LENGTH_SHORT).show());

            nearArray[nearArray.length-1] = timestampedPerson;

            //????????? ??????
            int upCount = 0, downCount = 0;
            for (int i = 0; i < nearArray.length; i++) {
                if(nearArray[i].person.mark > UP_PROB_THRESHOLD)
                    upCount++;
                else if(nearArray[i].person.mark < DOWN_PROB_THRESHOLD)
                    downCount++;
            }

            //????????? ??? ??????
            double filteredScored;
            if (upCount >= 4)
                filteredScored = 1;
            else if (downCount >= 4)
                filteredScored = 0;
            else
                filteredScored = 0.5;

            Log.i("hi", String.format("run: filtered : %.3f, orig : %.3f", filteredScored, nearArray[3].person.mark));


            if(state==0){ //???????????? (??????????????? ??????)
                if(filteredScored > UP_PROB_THRESHOLD){//???????????? ????????? ??????
                    startTime = timestampedPerson.timestamp;
                    last_up = timestampedPerson.timestamp;
                    state=1;
                    Log.i(TAG, "run: 0 -> 1");
                }
            }
            else if(state==1){ //?????????
                if(filteredScored < DOWN_PROB_THRESHOLD){
                    long duration = timestampedPerson.timestamp - last_up;
                    if(mean_time_of_downing < 0) {
                        mean_time_of_downing = duration;
                        last_down = timestampedPerson.timestamp;
                        state = 2;
                        Log.i(TAG, "run: 1 -> 2");
                    }
                    else {//if(duration > mean_time_of_downing * 0.8 && duration < mean_time_of_downing * 1.2){
                        mean_time_of_downing = (mean_time_of_downing + duration)/2;
                        last_down = timestampedPerson.timestamp;
                        state = 2;
                        Log.i(TAG, "run: 1 -> 2");
                    }
                    //else{}
                }

            }
            else if(state==2){ // ????????????
                if(filteredScored > UP_PROB_THRESHOLD){
                    long duration = timestampedPerson.timestamp - last_down;
                    if (mean_time_of_upping < 0) {
                        mean_time_of_upping = duration;
                        last_up = timestampedPerson.timestamp;
                        state = 1;
                        count += 1;
                        Log.i(TAG, "run: 2 -> 1 count++ : " + count);
                    }
                    else {//if(duration > mean_time_of_upping * 0.8 && duration < mean_time_of_upping * 1.2){
                        mean_time_of_upping = (mean_time_of_upping + duration)/2;
                        last_up = timestampedPerson.timestamp;
                        state = 1;
                        count += 1;
                        Log.i(TAG, "run: 2 -> 1 count++ : " + count);
                    }
                    //else{}
                }
            }



            //?????? ??? ??????
            for (int i = 0; i < LAST_NEAR_ARRAY_ELT; i++) {
                nearArray[i] = nearArray[i+1];
            }

            //SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");


                //TODO for debug
                if(activityContext instanceof PoseNetSimpleTest) {
                    TimestampedPerson finalTimestampedPerson = timestampedPerson;
                    activityContext.runOnUiThread(new Runnable() {
                        TextView textView = activityContext.findViewById(R.id.currentTimeText);
                        TextView counterTextView = activityContext.findViewById(R.id.testcountText);

                        @Override
                        public void run() {
                            if(startTime!=0)
                                //textView.setText(String.format(" ????????? : %d.%03d", (finalTimestampedPerson.timestamp - startTime) / 1000, (finalTimestampedPerson.timestamp - startTime) % 1000));
                            counterTextView.setText("count : " +  count);
                            counterTextView.setTextColor(count%2==0?Color.RED:Color.BLUE);
                        }
                    });
                }
                else if(activityContext instanceof ExerciseActivity){
                    TimestampedPerson finalTimestampedPerson1 = timestampedPerson;
                    activityContext.runOnUiThread(new Runnable() {
                        TextView textView = activityContext.findViewById(R.id.timeText);
                        TextView counterTextView = activityContext.findViewById(R.id.countText);

                        @Override
                        public void run() {
                            if(startTime!=0)
                            //textView.setText(String.format(" ????????? : %d, real : %d", (finalTimestampedPerson1.timestamp - startTime) / 1000, (System.currentTimeMillis() - startTime) / 1000));

                                textView.setText(String.format(" ?????? ?????? : %d", (finalTimestampedPerson1.timestamp - startTime - idleFrame * frameDuration) / 1000));
                            counterTextView.setText("count : " +  count);

                            counterTextView.setTextColor(count%2==0?Color.RED:Color.BLUE);
                        }
                    });
                }


        }

        workingTime = last_up - startTime - idleFrame * frameDuration;
        Log.i(TAG, "run: done");
    }

    public void stop() {
        killSignal = true;
        //???????????? ??????????????? ?????????.
        for (int i = 0; i <5 ; i++) {

            personQueue.add(new TimestampedPerson(Long.MAX_VALUE,null));
        }

    }

    public long getWorkingTime() {
        return workingTime;
    }

    public int getCount() {
        return count;
    }
}
