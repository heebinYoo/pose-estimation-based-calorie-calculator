package com.example.capstone2.model.util.dtw;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.example.capstone2.model.util.normalized.NormalizedKeyPoint;
import com.example.capstone2.model.util.normalized.NormalizedPerson;
import com.example.capstone2.model.util.normalized.NormalizedPosition;
import com.opencsv.CSVReader;

import org.tensorflow.lite.examples.posenet.lib.BodyPart;
import org.tensorflow.lite.examples.posenet.lib.Person;

public class PoseCsvHelper {
    private Reader targetReader;
    private ArrayList<NormalizedPerson> poseList = new ArrayList<>();
    PoseCsvHelper(InputStreamReader inputStreamReader) throws IOException {
        this.targetReader = inputStreamReader;

        BodyPart[] bodyParts = BodyPart.values();
        CSVReader reader = new CSVReader(targetReader); // 1
        String[] nextLine;

        while ((nextLine = reader.readNext()) != null) {   // 2
//            for (int i = 0; i < nextLine.length; i++) {
//                System.out.println(i + " " + nextLine[i]);
//            }
            NormalizedPerson normalizedPerson = new NormalizedPerson();
            List<NormalizedKeyPoint> keyPointList = new ArrayList<>();

            for(int i=0; i<17; i++){
                NormalizedKeyPoint kp = new NormalizedKeyPoint();
                kp.normPosition = new NormalizedPosition();
                kp.normPosition.x = Double.parseDouble(nextLine[i]);
                kp.normPosition.y = Double.parseDouble(nextLine[i+1]);
                kp.bodyPart = bodyParts[i];
                keyPointList.add(kp);
            }
            normalizedPerson.keyPoints = keyPointList;
            poseList.add(normalizedPerson);
        }
        targetReader.close();
    }

    public ArrayList<NormalizedPerson> getPoseList() {
        return poseList;
    }
}
