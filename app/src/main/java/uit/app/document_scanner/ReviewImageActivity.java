package uit.app.document_scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;

import uit.app.document_scanner.openCV.OpenCVUtils;

public class ReviewImageActivity extends AppCompatActivity {

    ImageView reviewImage;
    LinearLayout sourceFrame;
    EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_image);

        sourceFrame = findViewById(R.id.sourceImageView);
        reviewImage = findViewById(R.id.review_image);
        editText = findViewById(R.id.filename);

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                Uri uri = intent.getParcelableExtra("croppedImage");
                File filename = new File(uri.getLastPathSegment());
                editText.setText(filename.toString());
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
