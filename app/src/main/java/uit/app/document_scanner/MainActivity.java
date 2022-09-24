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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;

import com.google.android.material.button.MaterialButton;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uit.app.document_scanner.view.LoadingDialog;

public class MainActivity extends AppCompatActivity {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);
    private RecyclerView recyclerView;
    List<File> images;
    AppUtils appUtils = new AppUtils();
    Adapter adapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getApplication().registerActivityLifecycleCallbacks(new LifeCycleHandler());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.datalist);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setNestedScrollingEnabled(false);
        PreCachingLayoutManager preCachingLayoutManager = new PreCachingLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(preCachingLayoutManager);

        images = new ArrayList<>();
        try {
            loadDocument();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        adapter = new Adapter(this, images);
        recyclerView.setAdapter(adapter);

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
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void loadDocument() throws FileNotFoundException {
        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures/MyCameraApp";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        images = Arrays.asList(directory.listFiles());
    }


}