package uit.app.document_scanner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import uit.app.document_scanner.R;
import uit.app.document_scanner.constants.Constants;
import uit.app.document_scanner.ml.EfficientdetLiteCid;
import uit.app.document_scanner.utils.AppUtils;
import uit.app.document_scanner.view.LoadingDialog;

public class ViewDocumentActivity extends OptionalActivity implements View.OnClickListener {

    private static String TAG = ViewDocumentActivity.class.getSimpleName();
    private ImageView imageView;
    private Button addNewDocumentButton;
    private Button ocrButton;
    private Button textDetectionButton;
//    private String filePath;
    private Bitmap detectedBitmap;
    private Bitmap originalBitmap;
    private Bitmap coloredBitmap;
    private LoadingDialog loadingDialog;
    private Boolean isFocused;
    private AppUtils appUtils;
    private Uri modifiedImageURI;
    private String colorImageName;
    private int rotatedAngle;
    @Override
    protected void init() {
        super.init();
        imageView = findViewById(R.id.resultImage);
        addNewDocumentButton = findViewById(R.id.addNewDocumentButton);
        ocrButton = findViewById(R.id.ocrButton);
        textDetectionButton = findViewById(R.id.textDetectionButton);

        // set on click listener
        addNewDocumentButton.setOnClickListener(this);
        ocrButton.setOnClickListener(this);
        textDetectionButton.setOnClickListener(this);

        detectedBitmap = null;
        loadingDialog = new LoadingDialog(this);

        isFocused = false;
        originalBitmap = null;

        appUtils = new AppUtils();

        // show back button on action bar
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_view_document;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        Intent intent = getIntent();

        rotatedAngle = intent.getExtras().getInt("rotatedAngle");

        // converted image
        modifiedImageURI = intent.getParcelableExtra("filePath");
        // color image
        colorImageName = intent.getExtras().getString("originalImageName");
        try {
            // get and set image bitmap
            originalBitmap = appUtils.getBitmap(modifiedImageURI,this);

            coloredBitmap = BitmapFactory.decodeFile(Constants.ORIGINAL_IMAGE_DIR + "/" + colorImageName);

            imageView.setImageBitmap(originalBitmap);
            imageView.setRotation(rotatedAngle);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "onCreate: failed to get bitmap");
        }

//        String postUrl = "http://192.168.1.57:5001";
//        byte[] bytes = createByteArray(filePath);
//        RequestBody requestBody = createImageIntoBody(bytes);
//        postRequest(postUrl,requestBody);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_view,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewDocumentActivity.this);
                builder.setTitle("Document Scanner");
                builder.setMessage("Do you want to delete this document?");
                builder.setCancelable(false);
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = new File(modifiedImageURI.getPath());
                        File originalFile = new File(Constants.ORIGINAL_IMAGE_DIR + "/" + colorImageName);

                        file.delete();
                        if(file.exists()){
                            try {
                                file.getCanonicalFile().delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(file.exists()){
                                getApplicationContext().deleteFile(file.getName());
                            }
                        }

                        originalFile.delete();
                        if(originalFile.exists()){
                            try {
                                originalFile.getCanonicalFile().delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(originalFile.exists()){
                                getApplicationContext().deleteFile(originalFile.getName());
                            }
                        }


                        dialogInterface.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                break;
            case R.id.rename:
                File modifiedFile = new File(modifiedImageURI.getPath());
                String modifiedFileParent = modifiedFile.getParent();

                AlertDialog.Builder renameBuilder = new AlertDialog.Builder(ViewDocumentActivity.this);
                renameBuilder.setTitle("Document Scanner");
                renameBuilder.setCancelable(false);

//                final EditText input = new EditText(ViewDocumentActivity.this);
                String fileName = modifiedFile.getName();
                String extension = fileName.substring(fileName.lastIndexOf("."));
                Log.d(TAG, "extension: " + extension);

                // change name of original file
                File originalFile = new File(Constants.ORIGINAL_IMAGE_DIR + "/" + colorImageName);


                LayoutInflater layoutInflater = getLayoutInflater();
                View dialogLayout = layoutInflater.inflate(R.layout.edit_text_layout,null);
                EditText input = dialogLayout.findViewById(R.id.textEdit);
                input.setText(fileName.substring(0,fileName.lastIndexOf(".")));
                input.setTextColor(getResources().getColor(R.color.black));
                renameBuilder.setView(dialogLayout);
                renameBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String newName = input.getText().toString() + extension;
                        colorImageName = newName;

                        File renamedFile = new File(modifiedFileParent,newName);
                        File renamedOriginalFile = new File(Constants.ORIGINAL_IMAGE_DIR,newName);

                        Log.d(TAG, "renamed file: " + renamedFile);
                        modifiedFile.renameTo(renamedFile);

                        originalFile.renameTo(renamedOriginalFile);

                        // update the file path
                        modifiedImageURI = Uri.fromFile(renamedFile);
//                        filePath = renamedFile.getAbsolutePath();
//                        Log.d(TAG, "file path name: " + filePath);
                        dialogInterface.dismiss();
                    }
                });
                renameBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


                AlertDialog renameDialog = renameBuilder.create();
                renameDialog.show();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.addNewDocumentButton:
                Intent addDocumentIntent = new Intent(ViewDocumentActivity.this, CameraActivity.class);
                startActivity(addDocumentIntent);
                break;

            case R.id.ocrButton:
                // for testing
                Intent ocrIntent = new Intent(ViewDocumentActivity.this, ResultActivity.class);
                ocrIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                ocrIntent.putExtra("originalImageName", colorImageName);
                startActivity(ocrIntent);
                break;

            case R.id.textDetectionButton:
                loadingDialog.startLoadingDialog();

                if (isFocused == false) {
                    new TextDetectionTask().execute(coloredBitmap);
                    isFocused = true;
                }

                else {
                    imageView.setImageBitmap(originalBitmap);
                    isFocused = false;
                }

                changeButtonTintColor(textDetectionButton, isFocused);
                loadingDialog.dismissDialog();

                break;

            default:
                break;
        }
    }

    private void changeButtonTintColor(View view, boolean isFocused){
        Drawable unwrappedDrawable = view.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);

        if(isFocused == true) {
            DrawableCompat.setTint(wrappedDrawable, getColor(R.color.teal_font));
        }

        else {
            DrawableCompat.setTint(wrappedDrawable, getColor(R.color.white_font));
        }
    }

    private List<EfficientdetLiteCid.DetectionResult> detectText(Bitmap bm){
        try {
            EfficientdetLiteCid model = EfficientdetLiteCid.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bm);

            // Runs model inference and gets result.
            EfficientdetLiteCid.Outputs outputs = model.process(image);

            // Releases model resources if no longer used.
            model.close();

            return outputs.getDetectionResultList();

//            detectText(bm,outputs.getDetectionResultList());
        } catch (IOException e) {
            // TODO Handle the exception
            return null;
        }
    }

    private Bitmap drawDetectionResult(Bitmap bm, List<EfficientdetLiteCid.DetectionResult> detectionResults){


        Bitmap output = bm.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2f);
        for (EfficientdetLiteCid.DetectionResult res : detectionResults){
            float score = res.getScoreAsFloat();
            if (score > 0.3) {
                RectF location = res.getLocationAsRectF();
                canvas.drawRect(location,paint);
//                canvas.drawLine(location.left,0,location.left,location.top,paint);
            }
        }
        return output;
    }

    private class TextDetectionTask extends AsyncTask<Bitmap,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {

            if (detectedBitmap == null) {
                Bitmap inputBitmap = bitmaps[0];
                List<EfficientdetLiteCid.DetectionResult> list = detectText(inputBitmap);
                detectedBitmap = drawDetectionResult(originalBitmap, list);
            }
            return detectedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageView.setImageBitmap(bitmap);
            imageView.setRotation(rotatedAngle);
//            loadingDialog.dismissDialog();
        }
    }

}
