package uit.app.document_scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;

import com.google.android.material.button.MaterialButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uit.app.document_scanner.view.LoadingDialog;

public class MainActivity extends OptionalActivity implements View.OnClickListener {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
    private RecyclerView recyclerView;
    List<File> images;
    AppUtils appUtils = new AppUtils();
    Adapter adapter;

    private MaterialButton openCameraButton;
    private static String TAG = MainActivity.class.getSimpleName();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();


    }

    @Override
    protected void init() {
        super.init();

        recyclerView = findViewById(R.id.datalist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setNestedScrollingEnabled(false);
        PreCachingLayoutManager preCachingLayoutManager = new PreCachingLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(preCachingLayoutManager);

        images = new ArrayList<>();

        openCameraButton = findViewById(R.id.openCameraButton);
        openCameraButton.setOnClickListener(this);

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity","OpenCV is loaded");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else {
            Log.d("MainActivity", "Opencv is not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,mLoaderCallback);
        }

        try {
            loadDocument();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        adapter = new Adapter(this, images);
        recyclerView.setAdapter(adapter);
    }

    private boolean hasCameraPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,CAMERA_PERMISSION,CAMERA_REQUEST_CODE);
    }

    private void enableCamera(){
        Intent intent = new Intent(this,CameraActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void loadDocument() throws FileNotFoundException {
        String path = Constants.APP_DIR;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        images = Arrays.asList(directory.listFiles());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.openCameraButton:
                if(hasCameraPermission()){
                    enableCamera();
                }
                else {
                    requestPermission();
                }
                break;

            default:
                break;
        }
    }
}