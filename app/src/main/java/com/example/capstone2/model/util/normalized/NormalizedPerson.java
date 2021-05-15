package com.example.capstone2.model.util.normalized;

import org.tensorflow.lite.examples.posenet.lib.BodyPart;
import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class NormalizedPerson {

    public List<NormalizedKeyPoint> normalizedKeyPoints = new ArrayList<>();

    private Person person;

    public float[][] getFlattenKeyPointArray(){
        float[][] array = new float[34][1];
        for (int i = 0; i < 17; i++) {
            array[2*i] = new float[1];
            array[2*i][0] = (float) person.keyPoints.get(i).position.x;
            array[2*i + 1] = new float[1];
            array[2*i + 1][0] = (float) person.keyPoints.get(i).position.y;
        }

        return array;
    }


    public NormalizedPerson(){
    }

    private double distance(NormalizedPosition p1, NormalizedPosition p2){
        return sqrt((p1.x-p2.x) * (p1.x-p2.x) + (p1.y-p2.y) * (p1.y-p2.y));
    }
    public double getPoseScale(Person person){
        this.person = person;

        double sholder_middleX = person.keyPoints.get(BodyPart.RIGHT_SHOULDER.ordinal()).position.x
                + person.keyPoints.get(BodyPart.LEFT_SHOULDER.ordinal()).position.x;
        double sholder_middleY = person.keyPoints.get(BodyPart.RIGHT_SHOULDER.ordinal()).position.y
                + person.keyPoints.get(BodyPart.LEFT_SHOULDER.ordinal()).position.y;
        NormalizedPosition sholder_middle = new NormalizedPosition();
        sholder_middle.x = sholder_middleX;
        sholder_middle.y = sholder_middleY;

        KeyPoint kp_left_hip =  person.keyPoints.get(BodyPart.LEFT_HIP.ordinal());
        KeyPoint kp_right_hip = person.keyPoints.get(BodyPart.RIGHT_HIP.ordinal());
        double hip_middleX = (kp_left_hip.position.x + kp_right_hip.position.x)/2.0;
        double hip_middleY = (kp_left_hip.position.x + kp_right_hip.position.x)/2.0;
        NormalizedPosition hip_middle = new NormalizedPosition();
        hip_middle.x = hip_middleX;
        hip_middle.y = hip_middleY;

        double max = distance(sholder_middle, hip_middle);
        for(KeyPoint kp : person.keyPoints){
            double now = distance(hip_middle, new NormalizedPosition(kp.position.x, kp.position.y));
            if(max < now){
                max = now;
            }
        }
        return max;
    }


    //TODO
    public NormalizedPerson(Person person){
        KeyPoint kp_left_hip =  person.keyPoints.get(BodyPart.LEFT_HIP.ordinal());
        KeyPoint kp_right_hip = person.keyPoints.get(BodyPart.RIGHT_HIP.ordinal());
        double centerX = (kp_left_hip.position.x + kp_right_hip.position.x)/2.0;
        double centerY = (kp_left_hip.position.x + kp_right_hip.position.x)/2.0;
        double normFactor = getPoseScale(person);
        for(KeyPoint kp : person.keyPoints){
            NormalizedKeyPoint nkp = new NormalizedKeyPoint();
            nkp.bodyPart = kp.bodyPart;
            nkp.normPosition.x = (kp.position.x - centerX)/normFactor;
            nkp.normPosition.y = (kp.position.y - centerY)/normFactor;
            normalizedKeyPoints.add(nkp);

        }
    }


}

