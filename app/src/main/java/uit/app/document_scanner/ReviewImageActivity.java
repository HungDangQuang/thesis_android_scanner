package uit.app.document_scanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uit.app.document_scanner.openCV.OpenCVUtils;

public class ReviewImageActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView reviewImage;
    LinearLayout sourceFrame;
    EditText editText;
    Button removeTextButton;

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
                    reviewImage.setImageBitmap(scaledBitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        HashMap<String, Integer> names = getListOfDocumentNames();

        removeTextButton.setOnClickListener(this);


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
        switch (view.getId()) {
            case R.id.removeTextButton:
                editText.setText("");
                view.setEnabled(false);
                view.setAlpha(0);
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
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
}
