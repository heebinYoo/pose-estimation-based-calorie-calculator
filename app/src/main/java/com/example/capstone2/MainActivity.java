package com.example.capstone2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private Button toExercise;
    private Button toDailypedometer;
    private Button toExerciseTips;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE);
        boolean checkFirst = pref.getBoolean("checkFirst", false);
        if(checkFirst == true){
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("checkFirst", true);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        } else {
            toExercise = findViewById(R.id.toExercise);
            toExercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent(MainActivity.this, ExerciseDisplay.class);
                    Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);

                    startActivity(intent);
                }
            });

            toExerciseTips = findViewById(R.id.toExerciseTips);
            toExerciseTips.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ExerciseTipsDisplay.class);
                    startActivity(intent);
                }
            });

            toDailypedometer = findViewById(R.id.toDailypedometer);
            toDailypedometer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DailyPedometerDisplay.class);
                    startActivity(intent);
                }
            });
        }
    }
}