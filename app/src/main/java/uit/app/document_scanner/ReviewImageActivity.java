package uit.app.document_scanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.AsyncTask;
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
import com.google.android.gms.tasks.OnCompleteListener;
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
//import org.checkerframework.checker.nullness.qual.NonNull;
//import org.checkerframework.checker.nullness.qual.NonNull;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    String str = "";

    public static final String TESS_DATA = "/tessdata";
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

                // tesseract testing
                // prepare tess data
//                prepareTessData();
                try {

                    Bitmap bm = new AppUtils().getBitmap(uri,ReviewImageActivity.this);

//                    if(bm.getWidth() > bm.getHeight()){
//                        bm = new OpenCVUtils().rotate(bm,90);
//                    }
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,reviewImage.getWidth(),reviewImage.getHeight(),false);
                    Log.d(TAG, "run: " + reviewImage.getHeight() + " w:" + reviewImage.getWidth());
                    originalBitmap = scaledBitmap;
//                    detectText(scaledBitmap);
//                    recognizeTextUsingMLKit(scaledBitmap);

                    try {
                        EfficientdetLiteCid model = EfficientdetLiteCid.newInstance(getApplicationContext());

                        // Creates inputs for reference.
                        TensorImage image = TensorImage.fromBitmap(scaledBitmap);

                        // Runs model inference and gets result.
                        EfficientdetLiteCid.Outputs outputs = model.process(image);

                        reviewImage.setImageBitmap(drawDetectionResult(scaledBitmap,outputs.getDetectionResultList()));
//                        reviewImage.setImageBitmap(scaledBitmap);
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


    private void prepareTessData(){
        try{
            File dir = getExternalFilesDir(TESS_DATA);
            if(!dir.exists()){
                if (!dir.mkdir()) {
                    Toast.makeText(getApplicationContext(), "The folder " + dir.getPath() + "was not created", Toast.LENGTH_SHORT).show();
                }
            }

            String pathToDataFile = "/storage/emulated/0/Android/data/uit.app.document_scanner/files/tessdata/vie.traineddata";
            if(!(new File(pathToDataFile)).exists()){
                InputStream in = getAssets().open("vie.traineddata");
                OutputStream out = new FileOutputStream(pathToDataFile);
                byte [] buff = new byte[1024];
                int len ;
                while(( len = in.read(buff)) > 0){
                    out.write(buff,0,len);
                }
                in.close();
                out.close();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void detectText(Bitmap bm){
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        String dataPath = getExternalFilesDir("/").getPath() + "/";
        tessBaseAPI.init(dataPath,"vie");
        tessBaseAPI.setImage(bm);
        Log.d(TAG, "detectText: " + tessBaseAPI.getUTF8Text());
        tessBaseAPI.end();
    }

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
        handleOutputs(bm,detectionResults);

        Bitmap output = bm.copy(Bitmap.Config.ARGB_8888,true);

        return output;
    }

    private void recognizeTextUsingMLKit(Bitmap bm, int index, RectF location,  HashMap<String, TextResult> hashMap){
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bm, 0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
//                                Log.d(TAG, "onSuccess: " + visionText.getText());
                                TextResult textResult = new TextResult(visionText.getText(),location);
                                hashMap.put(String.valueOf(index), textResult);

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

    private List<TextResult> sortByX(List<TextResult> list){

        Collections.sort(list, new Comparator<TextResult>() {
            @Override
            public int compare(TextResult t0, TextResult t1) {
                return Float.compare(t0.getCoordinates().left, t1.getCoordinates().left);
            }
        });

        return list;
    }

    private float calculateAverageY(List<TextResult> list){
        float sumOfYCoordinates = 0;
        for(TextResult res: list){
            sumOfYCoordinates += res.getCoordinates().top;
        }
        return sumOfYCoordinates/list.size();
    }

    private HashMap<String, List<TextResult>> sortByY(List<TextResult> list){
        float average = calculateAverageY(list);
        HashMap<String,List<TextResult>> hashMap = new HashMap<>();
        List<TextResult> line1 = new ArrayList<>();
        List<TextResult> line2 = new ArrayList<>();

        for (TextResult res : list){
            if(res.getCoordinates().top < average){
                line1.add(res);
            }
            else {
                line2.add(res);
            }
        }

        hashMap.put("line1",line1);
        hashMap.put("line2",line2);
        return hashMap;
    }

    private void handleOutputs(Bitmap bm, List<EfficientdetLiteCid.DetectionResult> list){

        List<TextResult> id = new ArrayList<>();
        List<TextResult> name = new ArrayList<>();
        List<TextResult> dob = new ArrayList<>();
        List<TextResult> hometown = new ArrayList<>();
        List<TextResult> address = new ArrayList<>();
        for (int i = 0; i  < list.size(); i++){

            int index = i;

            RectF location = list.get(index).getLocationAsRectF();
            String category = list.get(index).getCategoryAsString();
            Bitmap croppedBm = Bitmap.createBitmap(bm,Math.round(location.left),Math.round(location.top),Math.round(location.width()),Math.round(location.height()));
            Bitmap scaledBm = Bitmap.createScaledBitmap(croppedBm,500,500,false);

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            InputImage image = InputImage.fromBitmap(scaledBm, 0);
            Task<Text> result =
                    recognizer.process(image);



            result.addOnCompleteListener(new OnCompleteListener<Text>() {
                @Override
                public void onComplete(Task<Text> task) {
                    String result = task.getResult().getText();
                    TextResult textResult = new TextResult(result,location);

                    switch (category){

                        case "id":
                            id.add(textResult);
//                            Log.d(TAG, "onComplete: id size: " + id.size());
                            break;

                        case "name":
                            name.add(textResult);
                            break;

                        case "dob":
                            dob.add(textResult);
                            break;

                        case "hometown":
                            hometown.add(textResult);
                            Log.d(TAG, "home town: " + hometown.size());
                            break;

                        case "address":
                            address.add(textResult);
                            break;
                    }
                    int sumOfTexts = id.size() + name.size() + dob.size() + hometown.size() + address.size();

                    if (sumOfTexts == list.size()) {
                        // create hash map to store key-category and value-list result
                        InputParam inpId = new InputParam("id",id);
                        InputParam inpName = new InputParam("name",name);
                        InputParam inpDob = new InputParam("dob",dob);
                        InputParam inpHometown = new InputParam("hometown",hometown);
                        InputParam inpAddress = new InputParam("address",address);

                        new ReorderTextTask().execute(inpId);
                        new ReorderTextTask().execute(inpName);
                        new ReorderTextTask().execute(inpDob);
                        new ReorderTextTask().doInBackground(inpHometown);
                        new ReorderTextTask().doInBackground(inpAddress);
                    }
                }
            });

            

        }
    }

    private String sortText(List<TextResult> list){


        if(list.size() == 0){
            return null;
        }
        else if (list.size() == 1){
            return list.get(0).getText();
        }

        else {
            HashMap<String,List<TextResult>> hashMap = sortByY(list);
            List<TextResult> line1 = hashMap.get("line1");
            List<TextResult> line2 = hashMap.get("line2");

            line1 = sortByX(line1);
            line2 = sortByX(line2);

            line1.addAll(line2);

            String str = "";

            for(TextResult res : line1) {
                str = str + " " + res.getText();
            }
            return str;
        }
    }

//    private class TaskRecognition extends AsyncTask<>{}

    private class ReorderTextTask extends AsyncTask<InputParam,Void, Void>{

        @Override
        protected Void doInBackground(InputParam... inputParams) {

            InputParam inputParam = inputParams[0];
            String str = sortText(inputParam.getTextResultList());

            SharedPreferences sharedPreferences = ReviewImageActivity.this.getSharedPreferences("ordered text", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(inputParam.getKeyName(), str);
            Log.d(TAG, "doInBackground: " + inputParam.getKeyName() + " :" + str);

            return null;
        }
    }

}