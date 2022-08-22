package uit.app.document_scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    static {
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity","OpenCV is loaded");
        }
        else {
            Log.d("MainActivity", "Opencv is not loaded");
        }
    }

    private MaterialButton openCameraButton;
    private static String TAG = MainActivity.class.getSimpleName();
    public static String KEY_RECEIPT_PATH = "RECEIPT_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openCameraButton = findViewById(R.id.openCameraButton);
        
        
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                if(hasCameraPermission()){
                    enableCamera();
                }
                else {
                    requestPermission();
                }
            }
        });
    }

    private boolean hasCameraPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,CAMERA_PERMISSION,CAMERA_REQUEST_CODE);
    }

    private void enableCamera(){
        Intent intent = new Intent(this,CameraActivity.class);
        startActivity(intent);
    }

}