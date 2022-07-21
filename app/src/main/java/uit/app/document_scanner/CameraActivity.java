package uit.app.document_scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2
{
    private static String TAG = CameraActivity.class.getSimpleName();
    JavaCameraView javaCameraView;
    Mat mRGBA,mGrey;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    private MaterialButton exitButton;
    private MaterialButton openGalleryButton;

    private LottieAnimationView captureImage;

    private int takeImage = 0;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(CameraActivity.this) {
        @Override
        public void onManagerConnected(int status)
        {
            if (status == BaseLoaderCallback.SUCCESS) {
                javaCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    static
    {
        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "OpenCV is Configured or Connected successfully.");
        }
        else
        {
            Log.d(TAG, "OpenCV not Working or Loaded.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        javaCameraView = findViewById(R.id.frame_surface);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)  {
            Log.d(TAG, "Permissions granted");
            javaCameraView.setCameraPermissionGranted();
            javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
            javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
            javaCameraView.setCvCameraViewListener(this);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)  {
            Log.d(TAG, "Permissions granted");
        }
        else {
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)  {
            Log.d(TAG, "Permissions granted");
        }
        else {
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        }

        exitButton = findViewById(R.id.exitButton);
        captureImage = findViewById(R.id.captureimage);
        openGalleryButton = findViewById(R.id.galleryButton);

        // back to the previous activity
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // capture
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage.playAnimation();
                if(takeImage == 0){
                    takeImage = 1;
                }
                else {
                    takeImage = 0;
                }
            }
        });

    }

    public CameraActivity(){
        Log.i(TAG,"Instantiated new " + this.getClass());
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        mRGBA = new Mat(height,width, CvType.CV_8UC4);
        mGrey = new Mat(height,width,CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped()
    {
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        mRGBA = inputFrame.rgba();
        mGrey = inputFrame.gray();

        // if input = 1
        // then input = 0
        // next frame input will be 0
        // take picture and save it

        takeImage = takePicture(takeImage,mRGBA);
        return mRGBA;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "OpenCV is configured or connected successfully.");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
        else
        {
            Log.d(TAG, "OpenCV not Working or Loaded.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,baseLoaderCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            // camera can be turned on
            Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            javaCameraView.setCameraPermissionGranted();
            javaCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
            javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
            javaCameraView.setCvCameraViewListener(this);
        } else {
            //camera will stay off
            Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
        }
    }

    private int takePicture(int takeImage, Mat mRGBA){

        if(takeImage == 1){

            Mat picture = new Mat();
            Core.flip(mRGBA.t(), picture,1);
            Imgproc.cvtColor(picture,picture,Imgproc.COLOR_RGBA2BGRA);
            File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/SavedImages");

            Boolean isSuccess = true;
            if (!folder.exists()){
                isSuccess = folder.mkdirs();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateAndTime = sdf.format(new Date());
            String fileName = Environment.getExternalStorageDirectory().getPath() + "/SavedImages/" + currentDateAndTime + ".jpg";

            Imgcodecs.imwrite(fileName,picture);
            takeImage = 0;
        }
        return 0;
    }

}