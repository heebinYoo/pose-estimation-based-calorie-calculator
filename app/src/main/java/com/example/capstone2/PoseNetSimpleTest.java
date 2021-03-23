package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Posenet;

public class PoseNetSimpleTest extends AppCompatActivity {



    private Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap = Bitmap.createBitmap(257, 257, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pose_net_simple_test);
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
        for (KeyPoint keypoint : person.keyPoints){
            canvas.drawCircle(
                    keypoint.position.x,
                    keypoint.position.y, size, paint
            );
        }
        sampleImageView.setAdjustViewBounds(true);
        sampleImageView.setImageBitmap(mutableBitmap);
    }



}