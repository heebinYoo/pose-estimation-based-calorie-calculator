package com.example.capstone2.model.util.normalized;

import org.tensorflow.lite.examples.posenet.lib.BodyPart;
import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class NormalizedPerson {

    public List<NormalizedKeyPoint> normalizedKeyPoints = new ArrayList<>();


    public float[][] getFlattenKeyPointArray(){
        float[][] array = new float[34][1];
        for (int i = 0; i < 17; i++) {
            array[2*i] = new float[1];
            array[2*i][0] = (float) normalizedKeyPoints.get(i).normPosition.x;
            array[2*i + 1] = new float[1];
            array[2*i + 1][0] = (float) normalizedKeyPoints.get(i).normPosition.y;
        }

        return array;
    }


    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < 17; i++) {

            result +=(float) normalizedKeyPoints.get(i).normPosition.x;
            result +=",";
            result +=(float) normalizedKeyPoints.get(i).normPosition.y;
            result += "\n";
        }

        return result;
    }

    public NormalizedPerson(){
    }

    private double distance(NormalizedPosition p1, NormalizedPosition p2){
        return sqrt((p1.x-p2.x) * (p1.x-p2.x) + (p1.y-p2.y) * (p1.y-p2.y));
    }
    public double getPoseScale(){

        double sholder_middleX = (normalizedKeyPoints.get(BodyPart.RIGHT_SHOULDER.ordinal()).normPosition.x
                + normalizedKeyPoints.get(BodyPart.LEFT_SHOULDER.ordinal()).normPosition.x)/2.0;
        double sholder_middleY = (normalizedKeyPoints.get(BodyPart.RIGHT_SHOULDER.ordinal()).normPosition.y
                + normalizedKeyPoints.get(BodyPart.LEFT_SHOULDER.ordinal()).normPosition.y)/2.0;
        NormalizedPosition sholder_middle = new NormalizedPosition();
        sholder_middle.x = sholder_middleX;
        sholder_middle.y = sholder_middleY;

        NormalizedKeyPoint kp_left_hip =  normalizedKeyPoints.get(BodyPart.LEFT_HIP.ordinal());
        NormalizedKeyPoint kp_right_hip = normalizedKeyPoints.get(BodyPart.RIGHT_HIP.ordinal());
        double hip_middleX = (kp_left_hip.normPosition.x + kp_right_hip.normPosition.x)/2.0;
        double hip_middleY = (kp_left_hip.normPosition.x + kp_right_hip.normPosition.x)/2.0;
        NormalizedPosition hip_middle = new NormalizedPosition();
        hip_middle.x = hip_middleX;
        hip_middle.y = hip_middleY;

        double max = distance(sholder_middle, hip_middle) * 2.5;
        for(NormalizedKeyPoint kp : normalizedKeyPoints){
            double now = distance(hip_middle, new NormalizedPosition(kp.normPosition.x, kp.normPosition.y));
            if(max < now){
                max = now;
            }
        }
        return max;
    }


    public NormalizedPerson(Person person){
        KeyPoint kp_left_hip =  person.keyPoints.get(BodyPart.LEFT_HIP.ordinal());
        KeyPoint kp_right_hip = person.keyPoints.get(BodyPart.RIGHT_HIP.ordinal());
        double centerX = (kp_left_hip.position.x + kp_right_hip.position.x)/2.0;
        double centerY = (kp_left_hip.position.y + kp_right_hip.position.y)/2.0;

        for(KeyPoint kp : person.keyPoints){
            NormalizedKeyPoint nkp = new NormalizedKeyPoint();
            nkp.bodyPart = kp.bodyPart;
            nkp.normPosition.x = (kp.position.x - centerX);
            nkp.normPosition.y = (kp.position.y - centerY);
            normalizedKeyPoints.add(nkp);
        }

        double normFactor = getPoseScale();
        for(NormalizedKeyPoint nkp : normalizedKeyPoints) {
            nkp.normPosition.x/=normFactor;
            nkp.normPosition.y/=normFactor;
        }

    }


}

