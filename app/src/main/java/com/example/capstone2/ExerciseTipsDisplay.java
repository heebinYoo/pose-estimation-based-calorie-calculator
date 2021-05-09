package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


public class ExerciseTipsDisplay extends AppCompatActivity {

    private Button btn_s;
    private Button btn_d;
    private Button btn_p;
    private View.OnClickListener t_listener;
    private LinearLayout container;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tips_display);
       // btn_d = (Button)findViewById(R.id.deadlift);
        btn_s = (Button)findViewById(R.id.squat);
        //btn_p = (Button)findViewById(R.id.plank);

        container = (LinearLayout)findViewById(R.id.container);

//        btn_d.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                inflater.inflate(R.layout.youtube_d, container, true);
//            }
//        });

        btn_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.youtube_d, container, true);
            }
        });

//        btn_p.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                inflater.inflate(R.layout.youtube_d, container, true);
//            }
//        });
    }
}
