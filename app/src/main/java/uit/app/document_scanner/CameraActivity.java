package uit.app.document_scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import uit.app.document_scanner.view.LoadingDialog;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private ImageCapture imageCapture;
    private Button btnImageCapture;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private LoadingDialog loadingDialog = new LoadingDialog(CameraActivity.this);
    private String TAG = CameraActivity.class.getSimpleName();
    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        btnImageCapture = findViewById(R.id.imageCapture);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    startCameraX(cameraProvider);
                }
                catch (ExecutionException | InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

        btnImageCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                loadingDialog.startLoadingDialog();
                view.setEnabled(false);
                capturePhoto();
            }
        });
    }

    private void startCameraX(ProcessCameraProvider cameraProvider){

        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build();
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,preview,imageCapture);
    }

    private void capturePhoto(){

        imageCapture.takePicture(getMainExecutor(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Log.d(TAG,"image is captured");
                Bitmap bm = imageProxyToBitmap(image);
                Uri imgUri = Uri.parse("file://" + new AppUtils().saveBitmapToFile(bm));
                Intent intent = new Intent(CameraActivity.this,CropImageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("imgPath",imgUri);
                loadingDialog.dismissDialog();
                btnImageCapture.setEnabled(true);
                image.close();
                startActivity(intent);
                super.onCaptureSuccess(image);
            }
        });
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);
    }
}