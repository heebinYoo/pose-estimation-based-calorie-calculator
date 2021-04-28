package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.youtube.player.YouTubePlayer;

import kr.co.prnd.YouTubePlayerView;


public class ExerciseTipsDisplay extends AppCompatActivity {

    private Button btn_S;
    private Button btn_d;
    private Button btn_p;
    private View.OnClickListener t_listener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_tips_display);
/*
        btn_S = (Button)findViewById(R.id.squat);
        btn_d = (Button)findViewById(R.id.deadlift);
        btn_p = (Button)findViewById(R.id.plank);

       // LinearLayout linearLayout = (LinearLayout) findViewById(R.id.yt1);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //layoutInflater.inflate(R.layout.for_youtube, linearLayout, true);

        btn_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutInflater.inflate(R.layout.for_youtube, linearLayout, true);
            }
        });
        /*
        btn_S.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
            }
        });

        btn_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */

    }
}
