package uit.app.document_scanner;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Point;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import uit.app.document_scanner.cropDocument.PolygonView;
import uit.app.document_scanner.openCV.OpenCVUtils;

//public class CropImageActivity extends AppCompatActivity implements View.OnClickListener {
//
//    private Button scanButton = null;
//    private ImageView sourceImageView;
//    private PolygonView polygonView;
//    private FrameLayout sourceFrame;
//    private Bitmap original = null;
//
//    private Uri uri;
//
//    private Bitmap bitmap;
//
//    public Bitmap getBitmap() {
//        Uri uri = this.uri;
//        try {
//            Bitmap bitmap = new AppUtils().getBitmap(uri,this);
//            bitmap.setDensity(Bitmap.DENSITY_NONE);
//            if(bitmap.getWidth() > bitmap.getHeight()){
//                bitmap = new OpenCVUtils().rotate(bitmap,90);
//            }
//            return bitmap;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public void setOriginal(Bitmap original) {
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(original,sourceImageView.getWidth(),sourceImageView.getHeight(),false);
//        sourceImageView.setImageBitmap(scaledBitmap);
//        Map<Integer, Point> pointFs =  new OpenCVUtils().getEdgePoints(scaledBitmap,polygonView);
//        polygonView.setPoints(pointFs);
//        polygonView.setVisibility(View.VISIBLE);
//
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sourceFrame.getWidth(),sourceFrame.getHeight());
//        layoutParams.gravity = Gravity.CENTER;
//        polygonView.setLayoutParams(layoutParams);
//    }
//
//    public void setUri() {
//        uri = new Intent().getParcelableExtra("imgPath");
//    }
//
//    @Override
//    public void onClick(View view) {
//        if (view.getId() == R.id.scanButton){
//            sourceFrame.post(new Runnable() {
//                @Override
//                public void run() {
//                   Bitmap croppedBitmap = new OpenCVUtils().cropReceiptByFourPoints(bitmap,polygonView.getListPoint(),sourceImageView.getMaxWidth(),sourceImageView.getHeight());
//                   String savedPath = new AppUtils().saveBitmapToFile(croppedBitmap);
//
//                }
//            });
//        }
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_crop_image);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        init();
//    }
//
//    private void init(){
//        sourceImageView = findViewById(R.id.sourceImageView);
//        scanButton = findViewById(R.id.scanButton);
//        scanButton.setOnClickListener(this);
//        sourceFrame = findViewById(R.id.sourceFrame);
//        polygonView = findViewById(R.id.polygonView);
//
////        sourceFrame.post(new Runnable() {
////            @Override
////            public void run() {
////
////            }
////        });
////
////        setUri();
////        setOriginal(getBitmap());
//
//    }
//
//}

public class CropImageActivity extends AppCompatActivity{

    private Button scanButton = null;
    private ImageView sourceImageView;
    private PolygonView polygonView;
    private FrameLayout sourceFrame;
    private Bitmap bm;

    private String TAG = CropImageActivity.class.getSimpleName();

    private void init(){
        sourceImageView = findViewById(R.id.sourceImageView);
        scanButton = findViewById(R.id.scanButton);
        sourceFrame = findViewById(R.id.sourceFrame);
        polygonView = findViewById(R.id.polygonView);
    }




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_crop_image);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


//        View decorView = getWindow().getDecorView();
        // Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();
        init();

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                Uri uri = intent.getParcelableExtra("imgPath");
                try {
                    bm = new AppUtils().getBitmap(uri,CropImageActivity.this);
//                    bm.setDensity(Bitmap.DENSITY_NONE);
                    if(bm.getWidth() > bm.getHeight()){
                        bm = new OpenCVUtils().rotate(bm,90);
                    }

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;

                    Log.d(TAG, "screen width:" + width + " height:" + height);
                    Log.d(TAG, "image view width:" + sourceImageView.getWidth() + " height:" + sourceImageView.getHeight());
                    Log.d(TAG,"bitmap width:" + bm.getWidth() + " height: " + bm.getHeight());
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,sourceImageView.getWidth(),sourceImageView.getHeight(),false);
                    sourceImageView.setImageBitmap(scaledBitmap);

                    Map<Integer, Point> pointFs = new OpenCVUtils().getEdgePoints(scaledBitmap,polygonView);
                    polygonView.setPoints(pointFs);
                    polygonView.setVisibility(View.VISIBLE);

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sourceImageView.getWidth(), sourceImageView.getHeight());
                    layoutParams.gravity = Gravity.CENTER;
                    polygonView.setLayoutParams(layoutParams);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bm != null){
                    Log.d(TAG, "onClick: bm is not null");
                }

                if(polygonView.getListPoint() != null){
                    Log.d(TAG, "onClick: listpoint is not null " + polygonView.getListPoint().size());
                }
                
                Bitmap croppedBitmap = new OpenCVUtils().cropImageByFourPoints(bm,polygonView.getListPoint(), sourceImageView.getWidth(),sourceImageView.getHeight());
                
                if (croppedBitmap != null){
                    Log.d(TAG, "onClick: cropped bm is not null");
                }
                String savedPath = new AppUtils().saveBitmapToFile(croppedBitmap);
                Uri imgUri = Uri.parse( "file://" + savedPath);
                Log.d(TAG, "onClick: uri:" + imgUri);
                Intent intent = new Intent(CropImageActivity.this, ReviewImageActivity.class);
                intent.putExtra("croppedImage",imgUri);
                startActivity(intent);
            }
        });


    }
}
