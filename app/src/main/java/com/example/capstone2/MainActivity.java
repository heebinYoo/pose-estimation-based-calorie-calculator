package com.example.capstone2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.capstone2.util.PreferenceKeys;
import com.example.capstone2.util.PreferenceManager;

import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;


public class MainActivity extends AppCompatActivity {
    private Button toExercise;
    private Button toDailypedometer;
    private Button toExerciseTips;
    private int tutorialRequestCode = 157;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        boolean checkFirst = PreferenceManager.getBoolean(this, PreferenceKeys.checkFirst);
        if(!checkFirst || true){
            PreferenceManager.setBoolean(this, PreferenceKeys.checkFirst, true);
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivityForResult(intent, tutorialRequestCode);
        }

        toExercise = findViewById(R.id.toExercise);
        toExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == tutorialRequestCode) // ??????????????? ??????????????? ??????????????? ??????
        {
            final EditText edittext = new EditText(this);
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("?????? ?????? ?????? ???");
            builder.setMessage("?????????(KG) ??? ??????????????????");
            builder.setView(edittext);
            builder.setPositiveButton("??????",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            int weight = Integer.parseInt(edittext.getText().toString());
                            PreferenceManager.setInt(getApplicationContext(), PreferenceKeys.weight, weight);
                            Toast.makeText( getApplicationContext(), weight+"KG?????? ???????????????.", Toast.LENGTH_LONG).show();
                        }
                    });
            builder.setNegativeButton("??????",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.show();
        }
    }

}