package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.example.capstone2.database.dao.WorkTimeAndCalorieDao;
import com.example.capstone2.database.database.WorkTimeAndCalorieDatabase;
import com.example.capstone2.model.CalorieEstimator;
import com.example.capstone2.model.Exercise;
import com.example.capstone2.database.vo.WorkTimeAndCalorie;
import com.example.capstone2.model.util.TimestampedBitmap;
import com.example.capstone2.util.PreferenceKeys;
import com.example.capstone2.util.PreferenceManager;

import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Posenet;

import java.io.File;

import static java.lang.Thread.sleep;

public class PoseNetSimpleTest extends AppCompatActivity {


    static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(257, 257, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pose_net_simple_test);

        ActivityCompat.requestPermissions(PoseNetSimpleTest.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);


        ImageView sampleImageView = findViewById(R.id.image);

        Drawable drawedImage = ResourcesCompat.getDrawable(super.getResources(), R.drawable.grace_hopper, null);
        Bitmap imageBitmap = drawableToBitmap(drawedImage);

        sampleImageView.setImageBitmap(imageBitmap);
        Posenet posenet = new Posenet(getApplicationContext());

        Person person = posenet.estimateSinglePose(imageBitmap);

        // Draw the keypoints over the image.
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        float size = 2.0f;

        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        for (KeyPoint keypoint : person.keyPoints) {
            canvas.drawCircle(
                    keypoint.position.x,
                    keypoint.position.y, size, paint
            );
        }
        sampleImageView.setAdjustViewBounds(true);
        sampleImageView.setImageBitmap(mutableBitmap);

//TODO
        int weight = PreferenceManager.getInt(this, PreferenceKeys.weight);
        if (weight == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(this, PreferenceKeys.weight, 75);
        }

        int height = PreferenceManager.getInt(this, PreferenceKeys.height);
        if (height == PreferenceManager.DEFAULT_VALUE_INT) {
            PreferenceManager.setInt(this, PreferenceKeys.height, 175);
        }




        new Thread(new ImageMakingRunnable(this)).start();

    }
}


class ImageMakingRunnable implements Runnable {
    private CalorieEstimator calorieEstimator;
    private AppCompatActivity activityContext;

    public ImageMakingRunnable(AppCompatActivity activityContext) {
        this.activityContext = activityContext;
        calorieEstimator = new CalorieEstimator(Exercise.LEG, activityContext);
    }

    @Override
    public void run() {
        //File videoFile = new File(Environment.getExternalStorageDirectory().getPath() + "/heebin.mp4");
        //File videoFile = new File(Environment.getExternalStorageDirectory().getPath() + "/shortheebin.mp4");
        File videoFile = new File(Environment.getExternalStorageDirectory().getPath() + "/legwork.MOV");
        Uri videoFileUri = Uri.parse(videoFile.toString());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.toString());

        MediaPlayer mediaPlayer = MediaPlayer.create(activityContext, videoFileUri);
        // the duration in milliseconds
        int millisecond = mediaPlayer.getDuration();

        //30프레임 동영상을 넣었기 때문에 1000 / 30 = 33
        for (int i = 0; i < millisecond; i += 33) {
            // takes in microseconds (1/1000000th of a second) instead of milliseconds
            //TODO : too slow : 300ms
            Bitmap bitmap = retriever.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
            if(bitmap==null){
                continue;
            }
            calorieEstimator.put(new TimestampedBitmap(i, bitmap.copy(bitmap.getConfig(), true)));

            //UI work
            activityContext.runOnUiThread(new Runnable() {
                ImageView sampleImageView = activityContext.findViewById(R.id.image);

                @Override
                public void run() {
                    ((BitmapDrawable)sampleImageView.getDrawable()).getBitmap().recycle();
                    sampleImageView.setImageBitmap(bitmap);
                }
            });


        }

        WorkTimeAndCalorie workTimeAndCalorie =  calorieEstimator.stop();

        workTimeAndCalorie.datetime = System.currentTimeMillis();


        WorkTimeAndCalorieDatabase db = WorkTimeAndCalorieDatabase.getInstance(activityContext);
        new Thread(() -> {
            WorkTimeAndCalorieDao dao = db.workTimeAndCalorieDao();
            dao.insert(workTimeAndCalorie);
        }).start();




        retriever.release();

        Log.i("PoseNetSimpleTest", "done kcal : " + workTimeAndCalorie.calorie + " time : " + workTimeAndCalorie.mills/(1000*60));
    }


}