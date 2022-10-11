package uit.app.document_scanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import org.apache.commons.io.FilenameUtils;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    int flag = 0;
    

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_image);

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
                Uri uri = intent.getParcelableExtra("croppedImage");
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
                    reviewImage.setImageBitmap(scaledBitmap);

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
                Log.d(TAG, "onClick: backbutton is clicked");
                finish();
                break;

            case R.id.colorModeButton:
                Log.d(TAG, "onClick: color mode button is clicked");
                reviewImage.setImageBitmap(originalBitmap);
//                Mat convertedMat = convertImage(reviewImage,Imgproc.);
                break;

            case R.id.binaryModeButton:
                Log.d(TAG, "onClick: binary mode button is clicked");
                Mat convertedMat = convertImage(originalBitmap,Imgproc.COLOR_RGB2GRAY);
                Imgproc.threshold(convertedMat,convertedMat,0,255,Imgproc.THRESH_OTSU);
                Bitmap result = Bitmap.createBitmap(originalBitmap.getWidth(),originalBitmap.getHeight(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(convertedMat,result);
                reviewImage.setImageBitmap(result);
                break;
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
}
