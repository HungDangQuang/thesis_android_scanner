package uit.app.document_scanner;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
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

public class CropImageActivity extends AppCompatActivity{

    private Button scanButton = null;
    private ImageView sourceImageView;
    private PolygonView polygonView;
    private FrameLayout sourceFrame;
    private Bitmap bm;
    private Uri imgUri;
    private String TAG = CropImageActivity.class.getSimpleName();
    private AppUtils appUtils = new AppUtils();

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

        init();

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

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,sourceImageView.getWidth(),sourceImageView.getHeight(),false);
                    sourceImageView.setImageBitmap(scaledBitmap);
                    Log.d(TAG, "run: bitmap width and height:" + scaledBitmap.getWidth() +" "+ scaledBitmap.getHeight());

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

                Bitmap croppedBitmap = new OpenCVUtils().cropImageByFourPoints(bm,polygonView.getListPoint(), sourceImageView.getWidth(),sourceImageView.getHeight());

                String savedPath = appUtils.saveBitmapToFile(croppedBitmap);
                Uri imgUri = Uri.parse( "file://" + savedPath);
                Intent intent = new Intent(CropImageActivity.this, ReviewImageActivity.class);
                intent.putExtra("croppedImage",imgUri);

                appUtils.deleteImage(imgUri);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: executed");
        finish();
    }
}
