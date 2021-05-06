package com.example.capstone2.model.util.dtw;

import com.example.capstone2.model.util.TimestampedPerson;
import com.example.capstone2.model.util.normalized.NormalizedPerson;

import org.tensorflow.lite.examples.posenet.lib.Person;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.abs;

public

class DTWTask {
    private final String TAG = "DTWTask";
    private static final int TERMINATE_THRESH_HOLD = 20;
    private long startTimestamp;
    private ArrayList<NormalizedPerson> GT;
    private boolean terminated = false;
    private ArrayList<timeAndScore> distances = new ArrayList<>();

    private double bestDistance;
    private long bestTermindateTime;

    private ArrayList<double[]> matrix = new ArrayList<>();


    public DTWTask(long startTimestamp, ArrayList<NormalizedPerson> GT) {
        this.startTimestamp = startTimestamp;
        this.GT = GT;
    }

    public double getScore(){
        return bestDistance;
    }
    public long getStart(){
        return startTimestamp;
    }
    public long getEnd() {
        return bestTermindateTime;
    }

    public boolean continueTask(TimestampedPerson timestampedPerson){
        Person current_input = timestampedPerson.person;

        // 이미 끝난 쓰레드라면 그냥 false리턴하고 아무것도 안해
        if(terminated)
            return false;

        // 정상적인 DTW처리
        double[] newRow = new double[GT.size()];
        if(matrix.isEmpty()){
            newRow[0] = d(GT.get(0), current_input);
            for(int j=1; j<GT.size(); j++){
                newRow[j] = newRow[j-1] + d(GT.get(j), current_input);
            }
        }
        else{
            newRow[0] = matrix.get(matrix.size()-1)[0] + d(GT.get(0), current_input);
            for(int j=1; j<GT.size(); j++){
                double num1 = newRow[j-1];
                double num2 = matrix.get(matrix.size()-1)[j];
                double num3 = matrix.get(matrix.size()-1)[j-1];
                double res = (num1 < num2)? num1: num2;
                res = (num3 < res)? num3: res;
                newRow[j] = res + d(GT.get(j), current_input);
            }
        }
        matrix.add(newRow);


        // 첫번째로 입력을 처리한 때가 아니고, 만약 이번 프레임에서 정지자세가 검출되었으면 => 한 반복이 끝났을 거라고 판단
        // 스코어에 등록, 시작시간은 공통으로 등록되어있고, 끝나는 시간과 메트릭스 맨 아래, 오른쪽의 값을 정규화해서 저장
        if(matrix.size() > 1 && current_input.mark){
            distances.add(new timeAndScore(timestampedPerson.timestamp, matrix.get(matrix.size()-1)[GT.size()-1] / (GT.size()+matrix.size()-1)));
            //특정 번 이상 계산되었다면, 그때 thread 중지
            if(distances.size() > TERMINATE_THRESH_HOLD){
                terminated = true;
                timeAndScore ts = Collections.min(distances);
                bestDistance = ts.score;
                bestTermindateTime = ts.time;
                return false;
            }
        }
        return true;

    }


    public void terminate() {
        terminated = true;
        if(!distances.isEmpty()) {
            timeAndScore ts = Collections.min(distances);
            bestDistance = ts.score;
            bestTermindateTime = ts.time;
        }
        else{
            bestDistance = -1;
            bestTermindateTime = startTimestamp;
        }
    }


    private double d(NormalizedPerson normalizedPerson, Person person) {
        double total = 0;
        for(int i=0; i<17; i++){
            total += Math.sqrt(Math.pow(normalizedPerson.keyPoints.get(i).normPosition.x - (double) person.keyPoints.get(i).position.x/257.0, 2));
            total += Math.sqrt(Math.pow(normalizedPerson.keyPoints.get(i).normPosition.y - (double) person.keyPoints.get(i).position.y/257.0, 2));
        }
        return total;
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