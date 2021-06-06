package com.example.capstone2


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capstone2.database.database.WorkTimeAndCalorieDatabase
import com.example.capstone2.model.CalorieEstimator
import com.example.capstone2.model.Exercise
import com.example.capstone2.model.util.TimestampedBitmap
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


//https://github.com/owahltinez/camerax-tflite/tree/master/app/src/main/java/com/android/example/camerax/tflite
class ExerciseActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var poseAnalyzer: PoseAnalyzer
    private lateinit var timerTask: Timer
    private var time : Int = 0

    private var BUZZER_TYPE_1 : Int = 1
    private var BUZZER_TYPE_2 : Int = 2

    private lateinit var exerciseType : Exercise



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("운동을 선택하세요.")
            .setItems(R.array.exericse) { _, pos ->
                val items = resources.getStringArray(R.array.exericse)
                Toast.makeText(applicationContext, items[pos],Toast.LENGTH_SHORT).show()
                when (pos) {
                    0 -> {
                        exerciseType = Exercise.LEG
                    }
                    1 -> {
                        exerciseType = Exercise.ARM
                    }
                    2 -> {
                        exerciseType = Exercise.SQURT
                    }
                }


            }.create()
        alertDialog.show()




        var startstopbtn = findViewById<Button>(R.id.startstop)
        startstopbtn.setOnClickListener {
            if(startstopbtn.text.equals("start")) {

                var ac:AppCompatActivity = this
                timerTask = kotlin.concurrent.timer(period = 1000) {
                    time++

                    if (time == 4) {
                        runOnUiThread {
                            buzzer(BUZZER_TYPE_2)
                            timerTask.cancel()
                            startstopbtn.text = "stop"
                            poseAnalyzer = PoseAnalyzer(ac, exerciseType)
                            imageAnalyzer.setAnalyzer(cameraExecutor, poseAnalyzer)
                        }
                    } else {
                        runOnUiThread {
                            buzzer(BUZZER_TYPE_1)
                            startstopbtn.text = (4 - time).toString()
                        }
                    }
                }
            }
            else {
                startstopbtn.isEnabled = false
                startstopbtn.text = "정리중"
                Toast.makeText(this, "정리 작업중입니다. 곧 자동으로 꺼집니다.", Toast.LENGTH_LONG).show()
                imageAnalyzer.clearAnalyzer()
                Toast.makeText(this, "1단계 / 총 2단계 완료", Toast.LENGTH_LONG).show()
                poseAnalyzer.stop()
                Toast.makeText(this, "마무리 완료", Toast.LENGTH_LONG).show()
                finish()
            }
        }



        cameraExecutor = Executors.newSingleThreadExecutor()
    }




    private fun buzzer(type:Int){
        if(type==BUZZER_TYPE_1) {
            try {
                val path: Uri =
                    Uri.parse("")
                val r: Ringtone = RingtoneManager.getRingtone(this, path)
                r.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        else if(type==BUZZER_TYPE_2){
            try {
                val path: Uri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val r: Ringtone = RingtoneManager.getRingtone(this, path)
                r.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview:Preview = Preview.Builder()
                    .build()
                    .also {
                        var viewFinder = findViewById<PreviewView>(R.id.viewFinder)
                        it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                    }



            imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()


            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalyzer)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }




    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

private class PoseAnalyzer(activityContext: AppCompatActivity, exerciseType: Exercise) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L
    private var calorieEstimator : CalorieEstimator? = null
    private var converter : YuvToRgbConverter? =null
    private lateinit var bitmapBuffer: Bitmap

    private var activityContext: AppCompatActivity = activityContext
    private val db: WorkTimeAndCalorieDatabase

    init{
        this.calorieEstimator = CalorieEstimator(exerciseType, activityContext)
        this.converter = YuvToRgbConverter(activityContext)
        this.db = WorkTimeAndCalorieDatabase.getInstance(activityContext)
    }

    fun stop(){
        //TerminatingTask(activityContext).execute(calorieEstimator)
        val workTimeAndCalorie = calorieEstimator!!.stop()

        workTimeAndCalorie.datetime = System.currentTimeMillis()

        Thread {
            val dao = db.workTimeAndCalorieDao()
            dao.insert(workTimeAndCalorie)
        }.start()
    }



    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (!::bitmapBuffer.isInitialized) {
            // The image rotation and RGB image buffer are initialized only once
            bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
        }



        // TODO : 디바이스 속도 맞춰서 잘 해보세요.
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= frameDuration) {
            //Log.i("td", "analyze: good$currentTimestamp")
            var image : Image? = imageProxy.image
            converter!!.yuvToRgb(image!!, this.bitmapBuffer)


            //TODO : check device compatibility
            val rotateMatrix = Matrix()
            rotateMatrix.preRotate(-90f) // rotate
            rotateMatrix.postScale(-1.0f, 1.0f) // flip
            val sideInversionImg = Bitmap.createBitmap(
                bitmapBuffer, 0, 0,
                    bitmapBuffer.width, bitmapBuffer.height, rotateMatrix, false)

            this.calorieEstimator!!.put(TimestampedBitmap(currentTimestamp, sideInversionImg!!.copy(sideInversionImg.config, true)))
            sideInversionImg.recycle()

            lastAnalyzedTimestamp = currentTimestamp
        }

        imageProxy.close()
    }
}

// 33 : 30 FPS
// 90 : 10 FPS
const val frameDuration:Long = 90//ms