package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
//import android.widget.Toast;
import android.widget.Button;
//import android.Manifest;


//import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.auth.api.signin.GoogleSignInResult;
//import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
//import com.google.android.gms.common.Scopes;
//import com.google.android.gms.common.api.Scope;
//import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.fitness.Fitness;
//import com.google.android.gms.fitness.FitnessOptions;
//import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button toExercise;
    private Button toDailypedometer;
    private Button toExerciseTips;

    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "Oauth2Google";

    private GoogleSignInClient mGoogleSignInClient;


    FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build();

    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 111111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        @SuppressLint("WrongViewCast") SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions);
        } else {
            accessGoogleFit();
        }


/////////////////////
        toExercise = findViewById(R.id.toExercise);
        toExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExerciseDisplay.class);
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

    public void updateUI(GoogleSignInAccount account){
        if(account != null){
            String Name = account.getDisplayName();
            String Email = account.getEmail();
            String Id = account.getId();
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);

        if (reqCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
        if (resCode == Activity.RESULT_OK) {
            if(reqCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
            }
        }
    }


    private void accessGoogleFit() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        long endTime = c.getTimeInMillis();
        c.add(c.YEAR, -1);
        long startTime = c.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate (DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();
                GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response->{
                    Log.d(TAG, "Success()");
                })
                .addOnFailureListener(e->{
                    Log.d(TAG,"Failure()", e);
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> Task){

        try{
            GoogleSignInAccount account = Task.getResult(ApiException.class);
            updateUI(account);

        } catch (ApiException e) {

            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }

    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.sign_in_button:
                signIn();
                break;
        }

    }
    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
}