package com.example.capstone2;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone2.database.dao.WorkTimeAndCalorieDao;
import com.example.capstone2.database.database.WorkTimeAndCalorieDatabase;
import com.example.capstone2.database.vo.WorkTimeAndCalorie;
import com.example.capstone2.model.CalorieEstimator;
import com.example.capstone2.model.Exercise;
import com.example.capstone2.model.util.TimestampedBitmap;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ExerciseDisplay extends AppCompatActivity {
/*
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    //카메라 관련
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;

    private CaptureRequest.Builder captureRequestBuilder;

    //이미지 저장 관련
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private TextureView textureView;

    private CalorieEstimator calorieEstimator;


    //타이머관련
    TextView workingoutTimeTextView;
    TimerTask timerTask;
    Timer timer = new Timer();
    static long counter = 0;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_display);

        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        workingoutTimeTextView = (TextView) findViewById(R.id.workingouttimetext);

       Button exerciseStartButton = (Button) findViewById(R.id.start);
        assert exerciseStartButton != null;
        exerciseStartButton.setOnClickListener(v -> {
            startTimerTask();
            exerciseStartButton.setText("started");
            exerciseStartButton.setEnabled(false);
        });

        Button donePictureButton = (Button) findViewById(R.id.doneButton);
        donePictureButton.setOnClickListener(v ->
                {
                    stopTimerTask();
                    WorkTimeAndCalorie workTimeAndCalorie =  calorieEstimator.stop();

                    workTimeAndCalorie.datetime = System.currentTimeMillis();


                    WorkTimeAndCalorieDatabase db = WorkTimeAndCalorieDatabase.getInstance(this);
                    new Thread(() -> {
                        WorkTimeAndCalorieDao dao = db.workTimeAndCalorieDao();
                        dao.insert(workTimeAndCalorie);
                    }).start();

                    donePictureButton.setEnabled(false);


                }
        );

        calorieEstimator = new CalorieEstimator(Exercise.SQURT, this);
    }

    //카메라 퍼미션 부분
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                try {
                    openCamera();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //카메라 부분
    @Override
    protected void onResume() {
        super.onResume();


        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        if (textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }





    private void openCamera() throws CameraAccessException {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            cameraId = manager.getCameraIdList()[0];

            manager.openCamera(cameraId, stateCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    //카메라 캡쳐부분



    //캡쳐 실행
    private void takePicture() throws CameraAccessException {
        if(cameraDevice==null)
            return;


        //넣어도 지 맘대로 나와서 뭐, 그냥 뒀는데, 무의미한 값임
        int width = 257;
        int height = 257;



        ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);
        List<Surface> outputSurfaces = new ArrayList<>(2);
        outputSurfaces.add(reader.getSurface());

        outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

        final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));


        //이미지리더
        ImageReader.OnImageAvailableListener readerListener = reader1 -> {
            Image image = reader1.acquireLatestImage();

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            //jpeg를 bitmap으로 변환해야함.
            //BitmapFactory.decodeByteArray를 이용하여 Bitmap을 생성해준다.
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            Matrix rotateMatrix = new Matrix();
            rotateMatrix.postRotate(90); //-360~360
            Bitmap sideInversionImg = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);
            bitmap.recycle();
            image.close();

            calorieEstimator.put(new TimestampedBitmap(System.currentTimeMillis(), sideInversionImg));
        };

        reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                try {
                    createCameraPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        };

        cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                try {
                    session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, mBackgroundHandler);

    }


    private void createCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        Surface surface = new Surface(texture);

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }

                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Toast.makeText(getApplicationContext(), "Configuration Changed", Toast.LENGTH_LONG).show();
            }
        }, null);
    }

    private void updatePreview() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);

    }


    // 리스너 콜백
    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                createCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };





    //타이머 부분
    protected void onDestroy(){
        timer.cancel();
        super.onDestroy();
    }

    private void startTimerTask(){

        timerTask = new TimerTask() {
            @Override
            public void run() {
                counter++;
                workingoutTimeTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        workingoutTimeTextView.setText(String.format("Time : %ds", counter/30));
                    }
                });
                try {
                    takePicture();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, 0, 500);
    }

    private void stopTimerTask(){
        if(timerTask != null){
            ((TextView)findViewById(R.id.calorietext)).setText("종료되었습니다.");
            timerTask.cancel();
            timerTask = null;
        }
    }
    //타이머부분 끝

*/
}

