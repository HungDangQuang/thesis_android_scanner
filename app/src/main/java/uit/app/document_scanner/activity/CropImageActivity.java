package uit.app.document_scanner.activity;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

//import com.google.mlkit.vision.barcode.BarcodeScanner;
//import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
//import com.google.mlkit.vision.barcode.BarcodeScanning;
//import com.google.mlkit.vision.barcode.common.Barcode;

import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import uit.app.document_scanner.R;
import uit.app.document_scanner.SaveOptions;
import uit.app.document_scanner.constants.Constants;
import uit.app.document_scanner.cropDocument.PolygonView;
import uit.app.document_scanner.openCV.OpenCVUtils;
import uit.app.document_scanner.utils.AppUtils;
import uit.app.document_scanner.view.LoadingDialog;

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
    private TextView documentNameTextView;
    private Bitmap bm;
    private Uri imgUri;
    private String TAG = CropImageActivity.class.getSimpleName();
    private AppUtils appUtils = new AppUtils();
    private float screenRatio;
    private LoadingDialog loadingDialog;
    int angle = 0;
    private String folderName;
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
        documentNameTextView = findViewById(R.id.documentNameTextView);

        closeButton.setOnClickListener(this);
        rotateLeftButton.setOnClickListener(this);
        rotateRightButton.setOnClickListener(this);
        zoomButton.setOnClickListener(this);
        cropButton.setOnClickListener(this);

        loadingDialog = new LoadingDialog(this);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_crop_image;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                imgUri = intent.getParcelableExtra("ImagePath");
                folderName = intent.getExtras().getString("folderName");

                File filename = new File(imgUri.getLastPathSegment());
                String str = filename.toString();
                str = FilenameUtils.removeExtension(str);
                documentNameTextView.setText(str);

                try {
                    bm = new AppUtils().getBitmap(imgUri,CropImageActivity.this);

                    bm.setDensity(Bitmap.DENSITY_NONE);
                    if(bm.getWidth() > bm.getHeight()){
                        bm = new OpenCVUtils().rotate(bm,90);
                    }
                    screenRatio = (float) sourceFrame.getWidth() / sourceFrame.getHeight();
                    scaledBitmap = Bitmap.createScaledBitmap(bm,sourceFrame.getWidth(),sourceFrame.getHeight(),false);


                    sourceImageView.setImageBitmap(scaledBitmap);

                    Map<Integer, Point> pointFs = new HashMap<>();
                    pointFs = new OpenCVUtils().getEdgePoints(scaledBitmap,polygonView);
                    polygonView.setPoints(pointFs);
                    polygonView.setVisibility(View.VISIBLE);

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sourceFrame.getWidth(), sourceFrame.getHeight());
                    layoutParams.gravity = Gravity.CENTER;
                    polygonView.setLayoutParams(layoutParams);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
//        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.closeButton:
                finish();
                break;

            case R.id.rotateRightButton: {
                rotateRightButton.setEnabled(false);
                final int rotatedAngle = -90;
                angle += rotatedAngle;

                sourceImageView.animate().rotationBy(rotatedAngle).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                        polygonView.animate().rotationBy(rotatedAngle).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
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
                                rotateRightButton.setEnabled(true);
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
            case R.id.rotateLeftButton: {
                rotateLeftButton.setEnabled(false);
                final int rotatedAngle = 90;
                angle += rotatedAngle;

                sourceImageView.animate().rotationBy(rotatedAngle).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                        polygonView.animate().rotationBy(rotatedAngle).setDuration(200).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
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
                                rotateLeftButton.setEnabled(true);
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
                break;

            case R.id.okButton:
                loadingDialog.startLoadingDialog();
                new ImageCroppingTask().execute();
//                finish();
                break;

            default:
                break;
        }
    }

    private class ImageCroppingTask extends AsyncTask<Void,Void,Intent> {

        @Override
        protected Intent doInBackground(Void... voids) {
            Bitmap croppedBitmap = new OpenCVUtils().cropImageByFourPoints(bm,polygonView.getListPoint(), sourceImageView.getWidth(),sourceImageView.getHeight());
            String savedPath = appUtils.saveBitmapToFile(croppedBitmap, Constants.TEMP_DIR);
            Uri imgUri = Uri.parse( "file://" + savedPath);
            Intent intent = new Intent(CropImageActivity.this, ReviewImageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("croppedImage",imgUri);
            intent.putExtra("rotatedAngle",angle);
            intent.putExtra("folderName",folderName);
            appUtils.deleteImage(imgUri);
            return intent;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            super.onPostExecute(intent);
            startActivity(intent);
            loadingDialog.dismissDialog();
        }
    }

}
