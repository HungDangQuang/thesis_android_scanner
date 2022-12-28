package uit.app.document_scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionBarOverlayLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uit.app.document_scanner.view.LoadingDialog;

public class MainActivity extends OptionalActivity implements View.OnClickListener {

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private LoadingDialog loadingDialog;
    private RecyclerView recyclerView;
    private List<File> images;
    private AppUtils appUtils;
    private Adapter adapter;
    private List<String> folderList;
    private FolderAdapter folderAdapter;
    private RecyclerView folderRecyclerView;
    private SearchView searchView;

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


        // init debug of openCV

        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"OpenCV is loaded");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else {
            Log.d(TAG, "Opencv is not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,mLoaderCallback);
        }

        init();
    }

    @Override
    protected void init() {
        super.init();

        // Set up for loading of recyclerView
        recyclerView = findViewById(R.id.datalist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);

        PreCachingLayoutManager preCachingLayoutManager = new PreCachingLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(preCachingLayoutManager);

        images = new ArrayList<>();

        // Set up camera button
        openCameraButton = findViewById(R.id.openCameraButton);
        openCameraButton.setOnClickListener(this);

        loadingDialog = new LoadingDialog(MainActivity.this);
        appUtils = new AppUtils();

        openOptionsMenu();

        // Set up folder recycler view
        folderList = createSampleFolders();
        folderRecyclerView = findViewById(R.id.folderList);
        folderAdapter = new FolderAdapter(folderList);
        LinearLayoutManager folderLayoutManager = new LinearLayoutManager(getApplicationContext());
        folderLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        folderRecyclerView.setLayoutManager(folderLayoutManager);
//        folderRecyclerView.setItemAnimator(new DefaultItemAnimator());
        folderRecyclerView.setAdapter(folderAdapter);
        folderRecyclerView.setFocusable(false);
        folderRecyclerView.setNestedScrollingEnabled(false);
    }

    private List<String> createSampleFolders(){
//        for(int i = 0; i < 10; i++){
//            File dir = new File(Constants.APP_DIR + "/" + i);
//
//            try {
//                if(dir.mkdir()){
//                    Log.d(TAG,"new folder created");
//                }
//                else {
//                    Log.d(TAG,"failed to create folder");
//                }
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            names.add(String.valueOf(i));
        }
        return names;
    }
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view,menu);
//        MenuItem menuItem = menu.findItem(R.id.searchIcon);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.searchIcon).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                folderAdapter.getFilter().filter(query);
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                folderAdapter.getFilter().filter(newText);
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;

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