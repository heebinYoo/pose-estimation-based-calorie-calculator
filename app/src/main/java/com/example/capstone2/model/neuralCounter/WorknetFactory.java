package com.example.capstone2.model.neuralCounter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone2.model.Exercise;

import org.tensorflow.lite.examples.posenet.lib.Armworknet;
import org.tensorflow.lite.examples.posenet.lib.Legworknet;
import org.tensorflow.lite.examples.posenet.lib.Squatworknet;
import org.tensorflow.lite.examples.posenet.lib.Worknet;

public class WorknetFactory {
    public Worknet buildWorknet(Exercise exercise, AppCompatActivity activityContext){
        if(exercise == Exercise.LEG){
            return new Legworknet(activityContext);
        }
        else if(exercise == Exercise.ARM){
            return new Armworknet(activityContext);
        }
        else if(exercise == Exercise.SQURT){
            return new Squatworknet(activityContext);
        }

        return null;
    }
}
