package uit.app.document_scanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;


//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.text.Text;
//import com.google.mlkit.vision.text.TextRecognition;
//import com.google.mlkit.vision.text.TextRecognizer;
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.apache.commons.io.FilenameUtils;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.AgastFeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
//import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
//import org.tensorflow.lite.task.vision.detector.Detection;
//import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uit.app.document_scanner.ml.EfficientdetLiteCid;
import uit.app.document_scanner.openCV.OpenCVUtils;

public class ReviewImageActivity extends AppCompatActivity implements View.OnClickListener {
    
    private static String TAG = ReviewImageActivity.class.getSimpleName();
    ImageView reviewImage;
    LinearLayout sourceFrame;
    EditText editText;
    Bitmap originalBitmap;
    Button removeTextButton;
    Button backButton;
    Button rgbModeButton;
    Button binaryModeButton;
    Button grayscaleModeButton;
    Button confirmButton;
    Uri uri;
    int flag = 0;
    int imageSize = 224;

//    public static final String TESS_DATA = "/tessdata";
//    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Tess";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_image);
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

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                uri = intent.getParcelableExtra("croppedImage");
                File filename = new File(uri.getLastPathSegment());
                String str = filename.toString();
                str = FilenameUtils.removeExtension(str);
                editText.setText(str);

                try {

                    Bitmap bm = new AppUtils().getBitmap(uri,ReviewImageActivity.this);

//                    if(bm.getWidth() > bm.getHeight()){
//                        bm = new OpenCVUtils().rotate(bm,90);
//                    }
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,reviewImage.getWidth(),reviewImage.getHeight(),false);
                    originalBitmap = scaledBitmap;

                    recognizeTextUsingMLKit(scaledBitmap);

                    try {
                        EfficientdetLiteCid model = EfficientdetLiteCid.newInstance(getApplicationContext());

                        // Creates inputs for reference.
                        TensorImage image = TensorImage.fromBitmap(scaledBitmap);

                        // Runs model inference and gets result.
                        EfficientdetLiteCid.Outputs outputs = model.process(image);

                        reviewImage.setImageBitmap(drawDetectionResult(scaledBitmap,outputs.getDetectionResultList()));
                        // Releases model resources if no longer used.
                        model.close();
                    } catch (IOException e) {
                        // TODO Handle the exception
                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });


        HashMap<String, Integer> names = getListOfDocumentNames();

        removeTextButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        rgbModeButton.setOnClickListener(this);
        grayscaleModeButton.setOnClickListener(this);
        binaryModeButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);


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


//    private void prepareTessData(){
//        try{
//            File dir = getExternalFilesDir(TESS_DATA);
//            if(!dir.exists()){
//                if (!dir.mkdir()) {
//                    Toast.makeText(getApplicationContext(), "The folder " + dir.getPath() + "was not created", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            String pathToDataFile = "/storage/emulated/0/Android/data/uit.app.document_scanner/files/tessdata/vie.traineddata";
//            if(!(new File(pathToDataFile)).exists()){
//                InputStream in = getAssets().open("vie.traineddata");
//                OutputStream out = new FileOutputStream(pathToDataFile);
//                byte [] buff = new byte[1024];
//                int len ;
//                while(( len = in.read(buff)) > 0){
//                    out.write(buff,0,len);
//                }
//                in.close();
//                out.close();
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }
//
//    private void detectText(Bitmap bm){
//        TessBaseAPI tessBaseAPI = new TessBaseAPI();
//        String dataPath = getExternalFilesDir("/").getPath() + "/";
//        tessBaseAPI.init(dataPath,"vie");
//        tessBaseAPI.setImage(bm);
//        Log.d(TAG, "detectText: " + tessBaseAPI.getUTF8Text());
//        tessBaseAPI.end();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {

        if(flag != 0) {
            changeIconTintColorToOriginalColor(flag);
        }

        if (view.getId() != R.id.removeTextButton) {
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
//                Intent intent = new Intent(ReviewImageActivity.this,DetectTextActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                intent.putExtra("imageToDetect",uri);
//                startActivity(intent);

        }
    }

    private HashMap<String, Integer> getListOfDocumentNames(){
        String path = Constants.APP_DIR;
        File directory = new File(path);
        File[] files = directory.listFiles();

        HashMap<String, Integer> names = new HashMap<String, Integer>();

        for (int i = 0; i < files.length; i++)
        {
            names.put(files[i].getName(), i);
        }

        return names;
    }

    private void changeIconTintColorToOriginalColor(int id){
        View view = findViewById(id);
        Drawable unwrappedDrawable = view.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColor(R.color.smoke_white));
    }

    private void changeIconTintColorToFocusedColor(View view){
        Drawable unwrappedDrawable = view.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, getColor(R.color.teal_200));
    }

    private Mat convertImage(Bitmap bm, int code){
        Mat result = new Mat();
        Utils.bitmapToMat(bm,result);
        Imgproc.cvtColor(result,result,code);
        return result;
    }

    private Bitmap drawDetectionResult(Bitmap bm, List<EfficientdetLiteCid.DetectionResult> detectionResults){

//        prepareTessData();

        Bitmap output = bm.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8f);
//        prepareTessData();
        for (EfficientdetLiteCid.DetectionResult res : detectionResults){
            float score = res.getScoreAsFloat();
            RectF location = res.getLocationAsRectF();
            String category = res.getCategoryAsString();
            canvas.drawRect(location,paint);
            Bitmap croppedBm = Bitmap.createBitmap(bm,Math.round(location.left),Math.round(location.top),Math.round(location.width()),Math.round(location.height()));
            Bitmap scaledBm = Bitmap.createScaledBitmap(croppedBm,300,300,false);
            recognizeTextUsingMLKit(scaledBm);
//            ImageProcessor imageProcessor = new ImageProcessor.Builder()
//                                                .add(new ResizeOp(31,200, ResizeOp.ResizeMethod.BILINEAR))
//                                                .add(new TransformToGrayscaleOp()).build();
//
//            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
//            tensorImage.load(croppedBm);
//            tensorImage = imageProcessor.process(tensorImage);


//            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
//
//            InputImage image = InputImage.fromBitmap(scaledBm, 0);


//            Task<Text> result =
//                    recognizer.process(image)
//                            .addOnSuccessListener(new OnSuccessListener<Text>() {
//                                @Override
//                                public void onSuccess(Text visionText) {
//
//                                    Log.d("onSuccess",visionText.getText());
//                                    // Task completed successfully
//                                    // ...
//                                }
//                            })
//                            .addOnFailureListener(
//                                    new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//
//                                            Log.d("onFail","fail to load");
//                                            // Task failed with an exception
//                                            // ...
//                                        }
//                                    });
        }

        return output;
    }

    private void recognizeTextUsingMLKit(Bitmap bm){
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bm, 0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                                Log.d(TAG, "onSuccess: " + visionText.getText());
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.d(TAG, "onFailure: failed to implement");
                                    }
                                });

    }
}
