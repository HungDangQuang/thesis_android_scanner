package uit.app.document_scanner;

import android.animation.Animator;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.mlkit.vision.barcode.BarcodeScanner;
//import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
//import com.google.mlkit.vision.barcode.BarcodeScanning;
//import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.objdetect.QRCodeDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uit.app.document_scanner.cropDocument.PolygonView;
import uit.app.document_scanner.openCV.OpenCVUtils;

public class CropImageActivity extends OptionalActivity implements View.OnClickListener{

    private ImageView sourceImageView;
    private PolygonView polygonView;
    private FrameLayout sourceFrame;
    private Bitmap scaledBitmap;
    private Button closeButton;
    private Button rotateLeftButton;
    private Button rotateRightButton;
    private Button zoomButton;
    private Button cropButton;
    private Bitmap bm;
    private Uri imgUri;
    private String TAG = CropImageActivity.class.getSimpleName();
    private AppUtils appUtils = new AppUtils();
    private float screenRatio;
    private Spinner spinner;
    private int rotatedAngle;
    @Override
    protected void init() {
        super.init();

        sourceImageView = findViewById(R.id.sourceImageView);
        closeButton = findViewById(R.id.closeButton);
        rotateLeftButton = findViewById(R.id.rotateLeftButton);
        rotateRightButton = findViewById(R.id.rotateRightButton);
        zoomButton = findViewById(R.id.zoomButton);
        cropButton = findViewById(R.id.okButton);
        sourceFrame = findViewById(R.id.sourceFrame);
        polygonView = findViewById(R.id.polygonView);

        closeButton.setOnClickListener(this);
        rotateLeftButton.setOnClickListener(this);
        rotateRightButton.setOnClickListener(this);
        zoomButton.setOnClickListener(this);
        cropButton.setOnClickListener(this);

        rotatedAngle = 0;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_crop_image;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_crop_image);
        init();

//        if (Build.VERSION.SDK_INT < 16) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//        else {
//            View decorView = getWindow().getDecorView();
//            // Show Status Bar.
//            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
//            decorView.setSystemUiVisibility(uiOptions);
//        }
//        OpenCVLoader.initDebug();
//        init();

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                imgUri = intent.getParcelableExtra("ImagePath");


                try {
                    bm = new AppUtils().getBitmap(imgUri,CropImageActivity.this);

                    bm.setDensity(Bitmap.DENSITY_NONE);
                    if(bm.getWidth() > bm.getHeight()){
                        bm = new OpenCVUtils().rotate(bm,90);
                    }
                    screenRatio = (float) sourceFrame.getWidth() / sourceFrame.getHeight();
                    scaledBitmap = Bitmap.createScaledBitmap(bm,sourceFrame.getWidth(),sourceFrame.getHeight(),false);


                    sourceImageView.setImageBitmap(scaledBitmap);
                    Log.d(TAG, "run: bitmap width and height:" + scaledBitmap.getWidth() +" "+ scaledBitmap.getHeight());

                    Map<Integer, Point> pointFs = new HashMap<>();
                    pointFs = new OpenCVUtils().getEdgePoints(scaledBitmap,polygonView);
                    polygonView.setPoints(pointFs);
                    polygonView.setVisibility(View.VISIBLE);

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sourceFrame.getWidth(), sourceFrame.getHeight());
                    layoutParams.gravity = Gravity.CENTER;
                    polygonView.setLayoutParams(layoutParams);
                    Log.d(TAG, "polygon view: " + polygonView.getX());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: executed");
//        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.closeButton:
                finish();
                break;

            case R.id.rotateRightButton:
            case R.id.rotateLeftButton: {
                int angle = 0;
                if (view.getId() == R.id.rotateRightButton) {
                    angle += -90;
                }
                else {
                    angle += 90;
                }

                int finalAngle = angle;
                sourceImageView.animate().rotationBy(angle).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                        polygonView.animate().rotationBy(finalAngle).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {

                                ViewGroup.LayoutParams polygonViewLayoutParams = polygonView.getLayoutParams();
                                int polygonViewWidth = polygonView.getWidth();
                                int polygonViewHeight = polygonView.getHeight();

                                if ((polygonView.getRotation()/90)%2 != 0) {
                                    polygonViewWidth = (int) (polygonViewWidth * screenRatio);
                                    polygonViewHeight = (int) (polygonViewHeight * screenRatio);
                                    polygonView.setScaledPoint(screenRatio);
                                }
                                else {
                                    polygonViewWidth = (int) (polygonViewWidth / screenRatio);
                                    polygonViewHeight = (int) (polygonViewHeight / screenRatio);
                                    polygonView.setScaledPoint(1/screenRatio);
                                }

                                polygonViewLayoutParams.width = polygonViewWidth;
                                polygonViewLayoutParams.height = polygonViewHeight;

                                polygonView.requestLayout();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        }).start();
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                        ViewGroup.LayoutParams lp = sourceImageView.getLayoutParams();
                        int w  = sourceImageView.getWidth();
                        int h = sourceImageView.getHeight();

                        if ((sourceImageView.getRotation() / 90) % 2 != 0) {
                            lp.width = (int) (w * screenRatio);
                            lp.height = (int) (h * screenRatio);

                        } else {
                            lp.width = (int) (w/screenRatio);
                            lp.height = (int) (h/screenRatio);
                        }
                        sourceImageView.requestLayout();
                        // update angle for the next activity
                        rotatedAngle = finalAngle;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
                break;
            }
            case R.id.zoomButton:
                polygonView.setCornerListPoints();
                polygonView.requestLayout();
                Log.d(TAG, "onClick: zoom button");
                break;

            case R.id.okButton:
                Bitmap croppedBitmap = new OpenCVUtils().cropImageByFourPoints(bm,polygonView.getListPoint(), sourceImageView.getWidth(),sourceImageView.getHeight());
                String savedPath = appUtils.saveBitmapToFile(croppedBitmap,SaveOptions.APP);
                Uri imgUri = Uri.parse( "file://" + savedPath);
                Intent intent = new Intent(CropImageActivity.this, ReviewImageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("croppedImage",imgUri);
                intent.putExtra("rotatedAngle",rotatedAngle);
                appUtils.deleteImage(imgUri);
                startActivity(intent);
//                finish();
                break;

            default:
                break;
        }
    }


}
