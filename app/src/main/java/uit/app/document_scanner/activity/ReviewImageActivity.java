package uit.app.document_scanner.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;


//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.text.Text;
//import com.google.mlkit.vision.text.TextRecognition;
//import com.google.mlkit.vision.text.TextRecognizer;
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.apache.commons.io.FilenameUtils;
//import org.checkerframework.checker.nullness.qual.NonNull;
//import org.checkerframework.checker.nullness.qual.NonNull;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
//import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp;
//import org.tensorflow.lite.task.vision.detector.Detection;
//import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import uit.app.document_scanner.R;
import uit.app.document_scanner.constants.Constants;
import uit.app.document_scanner.ml.EfficientdetLiteCid;
import uit.app.document_scanner.openCV.OpenCVUtils;
import uit.app.document_scanner.utils.AppUtils;
import uit.app.document_scanner.view.LoadingDialog;

public class ReviewImageActivity extends OptionalActivity implements View.OnClickListener {
    
    private static String TAG = ReviewImageActivity.class.getSimpleName();
    private ImageView reviewImage;
    private LinearLayout sourceFrame;
    private EditText editText;
    private Bitmap originalBitmap;
    private Button removeTextButton;
    private Button backButton;
    private Button rgbModeButton;
    private Button binaryModeButton;
    private Button grayscaleModeButton;
    private Button confirmButton;
    private Uri uri;
    private int flag = 0;
    private int rotatedAngle;
    private OpenCVUtils utils;
    private AppUtils appUtils;
    private LoadingDialog loadingDialog;
    private String folderName;
    public static final String TESS_DATA = "/tessdata";
//    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Tess";


    @Override
    protected void init() {
        super.init();
        getSupportActionBar().hide();

        sourceFrame = findViewById(R.id.sourceImageView);
        reviewImage = findViewById(R.id.review_image);
        editText = findViewById(R.id.filename);
        editText.setFocusable(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        removeTextButton = findViewById(R.id.removeTextButton);
        backButton = findViewById(R.id.backButton);
        rgbModeButton = findViewById(R.id.colorModeButton);
        binaryModeButton = findViewById(R.id.binaryModeButton);
        grayscaleModeButton = findViewById(R.id.grayModeButton);
        confirmButton = findViewById(R.id.confirmButton);
        flag = rgbModeButton.getId();
        changeIconTintColorToFocusedColor(rgbModeButton);
        utils = new OpenCVUtils();
        appUtils = new AppUtils();

        removeTextButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        rgbModeButton.setOnClickListener(this);
        grayscaleModeButton.setOnClickListener(this);
        binaryModeButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);

        loadingDialog = new LoadingDialog(this);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                editText.setCursorVisible(true);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editText.getText().length() == 0) {
                    removeTextButton.setAlpha(0);
                    removeTextButton.setEnabled(false);
                }
                else {
                    removeTextButton.setAlpha(1);
                    removeTextButton.setEnabled(true);
                }
            }
        });

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_review_image;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                uri = intent.getParcelableExtra("croppedImage");
                folderName = intent.getExtras().getString("folderName");
                File filename = new File(uri.getLastPathSegment());
                String str = filename.toString();
                str = FilenameUtils.removeExtension(str);
                editText.setText(str);
                rotatedAngle = intent.getExtras().getInt("rotatedAngle");

                try {

                    Bitmap bm = appUtils.getBitmap(uri,ReviewImageActivity.this);
//                    bm = utils.rotate(bm,rotatedAngle);
//                    int x = (int) (0.75 * bm.getWidth());
//                    int y = (int) (0.05 * bm.getHeight());
//                    int width = (int) (0.2 * bm.getWidth());
//                    Bitmap croppedQRCodeZone = Bitmap.createBitmap(bm,x,y,width,width);
//                    Mat mat = new Mat();
//                    Utils.bitmapToMat(croppedQRCodeZone,mat);
//                    Mat points = new Mat();
//                    QRCodeDetector detector = new QRCodeDetector();
//                    boolean data = detector.detect(mat,points);
//                    Log.d(TAG, "qrcode detection: " + data);
                    int newHeight =  reviewImage.getWidth() * bm.getHeight() / bm.getWidth();
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,reviewImage.getWidth(), newHeight,false);
//                    Bitmap croppedQRCodeZone = Bitmap.createBitmap(bm,x,y,width,width);
//                    bm = utils.rotate(bm,rotatedAngle);
//                    Mat mat = new Mat();
//                    Utils.bitmapToMat(scaledBitmap,mat);
//                    Mat points = new Mat();
//                    QRCodeDetector detector = new QRCodeDetector();
//                    boolean data = detector.detect(mat,points);
//                    Log.d(TAG, "qrcode detection: " + data);

//                    try {
//                        EfficientdetLiteCid model = EfficientdetLiteCid.newInstance(getApplicationContext());
//
//                        // Creates inputs for reference.
//                        TensorImage image = TensorImage.fromBitmap(scaledBitmap);
//
//                        // Runs model inference and gets result.
//                        EfficientdetLiteCid.Outputs outputs = model.process(image);
//
//                        reviewImage.setImageBitmap(drawDetectionResult(scaledBitmap,outputs.getDetectionResultList()));
//                        reviewImage.setRotation(rotatedAngle);
//                        // Releases model resources if no longer used.
//                        model.close();
//                    } catch (IOException e) {
//                        // TODO Handle the exception
//                    }
                    reviewImage.setImageBitmap(scaledBitmap);
                    reviewImage.setRotation(rotatedAngle);
                    originalBitmap = scaledBitmap;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public void onClick(View view) {

        if(flag != 0) {
            changeIconTintColorToOriginalColor(flag);
        }

        if (view.getId() != R.id.removeTextButton && view.getId() != R.id.confirmButton) {
            changeIconTintColorToFocusedColor(view);
            flag = view.getId();
        }


        switch (view.getId()) {
            case R.id.removeTextButton:
                editText.setText("");
                view.setEnabled(false);
                view.setAlpha(0);
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                break;

            case R.id.backButton:
                finish();
                break;

            case R.id.colorModeButton:
                reviewImage.setImageBitmap(originalBitmap);
                break;

            case R.id.binaryModeButton:
                Mat convertedMat = convertImage(originalBitmap,Imgproc.COLOR_RGB2GRAY);
                Imgproc.threshold(convertedMat,convertedMat,0,255,Imgproc.THRESH_OTSU);
                Bitmap result = Bitmap.createBitmap(originalBitmap.getWidth(),originalBitmap.getHeight(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(convertedMat,result);
                reviewImage.setImageBitmap(result);
                break;

            case R.id.grayModeButton:
                Mat grayscaleMat = convertImage(originalBitmap,Imgproc.COLOR_RGB2GRAY);
                Bitmap grayscaleBitmap = Bitmap.createBitmap(originalBitmap.getWidth(),originalBitmap.getHeight(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(grayscaleMat,grayscaleBitmap);
                reviewImage.setImageBitmap(grayscaleBitmap);
                break;

            case R.id.confirmButton:
                loadingDialog.startLoadingDialog();
                new SaveDocument().execute(editText.getText().toString());
        }
    }

//    private List<String> getListOfDocuments(){
//
//        String path = Constants.APP_DIR;
//        File directory = new File(path);
//        List<File> list = Arrays.asList(directory.listFiles());
//
//        List<String> listName = new ArrayList<>();
//        for(File file : list){
//            String name = file.getName();
//            name = name.substring(0,name.lastIndexOf("."));
//            listName.add(name);
//        }
//        return listName;
//    }


    private void changeIconTintColorToOriginalColor(int id){
        View view = findViewById(id);
        Drawable unwrappedDrawable = view.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColor(R.color.smoke_white));
    }

    private void changeIconTintColorToFocusedColor(View view){
        Drawable unwrappedDrawable = view.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColor(R.color.teal_font));
    }

    private Mat convertImage(Bitmap bm, int code){
        Mat result = new Mat();
        Utils.bitmapToMat(bm,result);
        Imgproc.cvtColor(result,result,code);
        return result;
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

    private class SaveDocument extends AsyncTask<String,Void,Intent>{

        @Override
        protected Intent doInBackground(String... strings) {
            String fileName = strings[0];

            String path = null;
            if (folderName == null){
                path = Constants.APP_DIR;
            }
            else {
                path = Constants.FOLDER_DIR + "/" + folderName;
            }

            File appFolder = new File(path);

            if (!appFolder.exists()){
                appFolder.mkdirs();
            }

            String originalImagePath = Constants.ORIGINAL_IMAGE_DIR;

            File originalFolder = new File(originalImagePath);

            if(!originalFolder.exists()){
                originalFolder.mkdirs();
            }

            OutputStream fOutOriginal = null;

            OutputStream fOut = null;

            Integer counter = 0;

            File file = new File(path, fileName + ".jpg");

            File originalFile = new File(originalImagePath, fileName + ".jpg");

            while (file.exists()){
                file = new File(path, fileName + "_" + counter + ".jpg");
                originalFile = new File(originalImagePath,fileName + "_" + counter + ".jpg");
                counter ++;
            }
            reviewImage.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) reviewImage.getDrawable();
            Bitmap bitmap =  drawable.getBitmap();

            try {
                fOutOriginal = new FileOutputStream(originalFile);
                originalBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOutOriginal);
                fOutOriginal.flush();
                fOutOriginal.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut);
                fOut.flush();
                fOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String filePath = file.getAbsolutePath();
            Intent intent = new Intent(ReviewImageActivity.this, ViewDocumentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("filePath",Uri.parse("file://" + filePath));
            intent.putExtra("originalImageName",file.getName());
            intent.putExtra("rotatedAngle",rotatedAngle);
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