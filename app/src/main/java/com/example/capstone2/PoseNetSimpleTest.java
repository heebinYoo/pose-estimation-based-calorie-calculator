package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
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

import com.example.capstone2.model.CalorieEstimator;
import com.example.capstone2.model.Exercise;
import com.example.capstone2.model.util.BitmapResizer;
import com.example.capstone2.model.util.TimestampedBitmap;

import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Posenet;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

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


        new Thread(new ImageMakingRunnable(this)).start();

    }
}


class ImageMakingRunnable implements Runnable {
    private CalorieEstimator calorieEstimator;
    private PoseNetSimpleTest context;

    public ImageMakingRunnable(PoseNetSimpleTest context) {
        this.context = context;
        calorieEstimator = new CalorieEstimator(Exercise.SQURT, context);
    }

    @Override
    public void run() {
        long idx = 0;
        File videoFile = new File(Environment.getExternalStorageDirectory().getPath() + "/heebin.mp4");
        Uri videoFileUri = Uri.parse(videoFile.toString());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.toString());
        MediaPlayer mediaPlayer = MediaPlayer.create(context, videoFileUri);
        int millisecond = mediaPlayer.getDuration();
        for (int i = 0; i < millisecond; i += 200) {
            Bitmap bitmap = retriever.getFrameAtTime(i * 1000, MediaMetadataRetriever.OPTION_CLOSEST);

            final Bitmap finalBitmap = bitmap.copy(bitmap.getConfig(), true);
            context.runOnUiThread(new Runnable() {
                ImageView sampleImageView = context.findViewById(R.id.image);

                @Override
                public void run() {
                    sampleImageView.setImageBitmap(finalBitmap);
                }
            });

            bitmap = BitmapResizer.getResizedBitmap(bitmap, 257, 257);
            calorieEstimator.put(new TimestampedBitmap(idx++, bitmap));

        }

        calorieEstimator.stop();

        retriever.release();

        Log.i("PoseNetSimpleTest", "done");
    }


}