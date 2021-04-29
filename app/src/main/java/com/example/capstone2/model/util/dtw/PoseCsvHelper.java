package com.example.capstone2.model.util.dtw;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;

import org.tensorflow.lite.examples.posenet.lib.BodyPart;
import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Position;

public class PoseCsvHelper {
    private Reader targetReader;
    private ArrayList<Person> poseList = new ArrayList<>();
    PoseCsvHelper(InputStreamReader inputStreamReader) throws IOException {
        this.targetReader = inputStreamReader;

        BodyPart[] bodyParts = BodyPart.values();
        CSVReader reader = new CSVReader(targetReader); // 1
        String[] nextLine;

        while ((nextLine = reader.readNext()) != null) {   // 2
//            for (int i = 0; i < nextLine.length; i++) {
//                System.out.println(i + " " + nextLine[i]);
//            }
            Person person = new Person();
            List<KeyPoint> keyPointList = new ArrayList<>();

            for(int i=0; i<17; i++){
                KeyPoint kp = new KeyPoint();
                kp.position = new Position();
                kp.position.x = (int) Double.parseDouble(nextLine[i]);
                kp.position.y = (int) Double.parseDouble(nextLine[i+1]);
                kp.bodyPart = bodyParts[i];
                keyPointList.add(kp);
            }
            person.keyPoints = keyPointList;
            poseList.add(person);
        }
        targetReader.close();
    }

    public ArrayList<Person> getPoseList() {
        return poseList;
    }
}
