package com.example.capstone2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class ExerciseDisplay extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera camera;
    private MediaRecorder mediaRecorder;
    private Button deadlift;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_display);

        TedPermission.with(this)
                .setPermissionListener(permission)
                .setRationaleMessage("촬영를 위하여 권한을 허용해주세요.")
                .setDeniedMessage("권한이 거부되었습니다. 권한을 거부하시면 촬영을 하실수 없습니다.")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .check();

        deadlift = (Button)findViewById(R.id.deadlift);
        //squat = (Button)findViewById(R.id.squat);
        //plank = (Button)findViewById(R.id.plank);

        deadlift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (recording){
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        camera.lock();
                        recording = false;
                    } else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ExerciseDisplay.this, "촬영이 시작되었습니다.", Toast.LENGTH_SHORT).show();
                                try {
                                    mediaRecorder = new MediaRecorder();
                                    camera.unlock();
                                    mediaRecorder.setCamera(camera);
                                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                                    mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                                    mediaRecorder.setOrientationHint(90);
                                    mediaRecorder.setOutputFile("/sdcard/test.mp4");
                                    //일단 경로는 임시로 정함.
                                    mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                                    mediaRecorder.prepare();
                                    mediaRecorder.start();
                                    recording = true;

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mediaRecorder.release();
                                }

                            }
                        });
                    }

            }
        });




    }

    PermissionListener permission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(ExerciseDisplay.this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {

        }
    };

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    private void refreshCamera(Camera camera) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        try {
            camera.startPreview();
        }catch (Exception e) {
            e.printStackTrace();
        }

        setCamera(camera);
    }

    private void setCamera(Camera cam) {
        camera = cam;

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(camera);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}
