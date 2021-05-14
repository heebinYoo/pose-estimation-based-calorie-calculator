package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


public class ExerciseTipsDisplay extends AppCompatActivity {

    private Button btn_a;
    private Button btn_s;
    private Button btn_l;
    private View.OnClickListener t_listener;
    private LinearLayout container;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tips_display);
        btn_a = (Button)findViewById(R.id.arm);
        btn_s = (Button)findViewById(R.id.squat);
        btn_l = (Button)findViewById(R.id.leg);

        container = (LinearLayout)findViewById(R.id.container);

        btn_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeAllViews();
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.youtube_a, container, true);

            }
        });

        btn_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeAllViews();
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.youtube_s, container, true);
            }
        });

        btn_l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeAllViews();
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.youtube_l, container, true);
            }
        });
    }
}
