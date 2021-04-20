package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.data.DataSource.TYPE_RAW;
import static com.google.android.gms.fitness.data.Field.NUTRIENT_CALORIES;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 컴퓨터마다 https://goni9071.tistory.com/489 실행
 *
 * gradle - tasks - android - signingReport에서 debug, debug의 sha1키를 얻고
 * 패키지 이름 확인한 후에, 구글에 OAUTH2.0키 등록해야함
 *
 */
public class GoogleLoginActivity extends AppCompatActivity {


    private static final String TAG = "GoogleLoginActivity";


    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 17;


    public static DataType dataType = DataType.TYPE_CALORIES_EXPENDED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }


        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(dataType, FitnessOptions.ACCESS_WRITE)
                .addDataType(dataType, FitnessOptions.ACCESS_READ)
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

    }


    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);

        if (resCode == Activity.RESULT_OK) {
            if(reqCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
            }
        }
    }


    // https://developers.google.com/fit/android/history#java : 출처
    // https://github.com/tutsplus/Android-GoogleFit-HistoryAPI/blob/master/app/src/main/java/com/tutsplus/googlefit/MainActivity.java
    private void accessGoogleFit() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        long endTime = c.getTimeInMillis();
        c.add(c.YEAR, -1);
        long startTime = c.getTimeInMillis();
        c.add(c.YEAR, 1);

        //이미 로그인 되어있으니까, 그냥 바로 땡겨쓰기.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        //TYPE_CALORIES_EXPENDED
        //Note: this total calories number includes BMR calories expended.

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(dataType)
                .setStreamName("$TAG - kcal count")
                .setType(DataSource.TYPE_RAW)
                .build();


//
//        c.add(c.MONTH, -3);
//        long exer_endTime = c.getTimeInMillis();
//        c.add(c.MINUTE, -2);
//        long exer_startTime = c.getTimeInMillis();
//
//        DataPoint dataPoint =DataPoint.builder(dataSource)
//                .setTimeInterval(exer_startTime, exer_endTime, MILLISECONDS)
//                .setField(Field.FIELD_CALORIES, (float) 40.0)
//                .build();
//
//        DataSet insert_dataSet = DataSet.builder(dataSource)
//                .add(dataPoint).build();
//
//        Fitness.getHistoryClient(this, account).insertData(insert_dataSet)
//                .addOnSuccessListener (unused ->
//                    Log.i(TAG, "DataSet added successfully!"))
//                .addOnFailureListener(e ->
//                    Log.w(TAG, "There was an error adding the DataSet", e));
//





        DataReadRequest readRequest = new DataReadRequest.Builder()
                .setTimeRange(startTime, endTime, MILLISECONDS)
                .read(dataType)
                //.aggregate (dataType)
                //.bucketByTime(1, TimeUnit.DAYS)
                .build();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response->{
                    //for (Bucket bucket : response.getBuckets()) {
                        for(DataSet dataSet : response.getDataSets())
                        //for (DataSet dataSet : bucket.getDataSets()) {
                            //Log.i(TAG, "Data returned for Data type: "+ dataSet.getDataSource().getStreamName());
                            //Log.i(TAG, " "+ dataSet.getDataType().getName());
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                Log.i(TAG,"Data point:");
                                Log.i(TAG,"\tType: "+ dp.getDataType().getName());
                                Log.i(TAG,"\tStart: "+  sdf.format(new Date(dp.getStartTime(MILLISECONDS))));
                                Log.i(TAG,"\tEnd: "+  sdf.format(new Date(dp.getEndTime(MILLISECONDS))));
                                for (Field field : dp.getDataType().getFields()) {
                                    Log.i(TAG,"\tField: "+ field.getName() + " Value: " + dp.getValue(field));
                                }
                            }
                        //}
                    //}

                    Log.i(TAG,"done");
                })
                .addOnFailureListener(e->{
                    Log.d(TAG,"Failure()", e);
                });


    }




}