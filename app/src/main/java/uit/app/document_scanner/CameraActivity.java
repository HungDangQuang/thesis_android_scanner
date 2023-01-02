package uit.app.document_scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import uit.app.document_scanner.view.LoadingDialog;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private ImageCapture imageCapture;
    private Button btnImageCapture;
    private Button btnImageGallery;
    private Button btnFlashMode;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
    private LoadingDialog loadingDialog = new LoadingDialog(CameraActivity.this);
    private String TAG = CameraActivity.class.getSimpleName();
    private static int PICK_PHOTO_FROM_GALLERY = 5;
    private static int CAPTURING_CAMERA = 10;
    int flashMode = ImageCapture.FLASH_MODE_OFF;
    Preview preview;
    private AppUtils appUtils = new AppUtils();

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        btnImageCapture = findViewById(R.id.imageCapture);
        btnImageGallery = findViewById(R.id.imageGallery);
        btnFlashMode = findViewById(R.id.flash);

        btnImageCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                stopCamera();
                loadingDialog.startLoadingDialog();
                view.setEnabled(false);
                capturePhoto();
                view.setEnabled(true);
            }
        });

        btnImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromGallery();
            }
        });

        btnFlashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFlashMode();
            }
        });
    }


    private void startCameraX(ProcessCameraProvider cameraProvider){

        cameraProvider.unbindAll();
        preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).setFlashMode(flashMode).build();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private void capturePhoto(){

        imageCapture.takePicture(getMainExecutor(), new ImageCapture.OnImageCapturedCallback() {

            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);

                Bitmap bm = imageProxyToBitmap(image);
                new SaveCapturedImage().execute(bm);
                image.close();
            }
        });

    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);
    }

    private void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_PHOTO_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == Activity.RESULT_OK){
            if (data == null){
                Log.d(TAG, "onActivityResult: cannot open image because data is null");
                return;
            }
            Uri uri = data.getData();
            startCropImageActivity(uri);
        }

        else if(requestCode == CAPTURING_CAMERA){
            Log.d(TAG, "onActivityResult: case 2");
            startActivity(data);
        }
    }

    private void startCropImageActivity(Uri uri){
        Intent intent = new Intent(CameraActivity.this,CropImageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("ImagePath",uri);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!(flashMode == ImageCapture.FLASH_MODE_OFF)){
            changeFlashMode();
        }

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
    }

    private void changeFlashMode() {

        switch (flashMode){
            case ImageCapture.FLASH_MODE_OFF:
                Log.d(TAG, "setFlashMode: flash is off");
                flashMode = ImageCapture.FLASH_MODE_ON;
                btnFlashMode.setBackgroundResource(R.drawable.ic_baseline_flash_on_24);
                break;

            case ImageCapture.FLASH_MODE_ON:
                Log.d(TAG, "setFlashMode: flash is on");
                flashMode = ImageCapture.FLASH_MODE_OFF;
                btnFlashMode.setBackgroundResource(R.drawable.ic_baseline_flash_off_24);
                break;
        }

        imageCapture.setFlashMode(flashMode);
    }

    private void stopCamera(){
        try {
            ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
            cameraProvider.unbind(preview);
        }
        catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private class SaveCapturedImage extends AsyncTask<Bitmap,Void,Uri>{


        @Override
        protected Uri doInBackground(Bitmap... bitmaps) {
            Uri imgUri = Uri.parse("file://" + appUtils.saveBitmapToFile(bitmaps[0], SaveOptions.TEMP));
            return imgUri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            loadingDialog.dismissDialog();
            startCropImageActivity(uri);
        }
    }

}


