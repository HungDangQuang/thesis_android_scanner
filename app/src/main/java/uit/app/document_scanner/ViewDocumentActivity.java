package uit.app.document_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewDocumentActivity extends OptionalActivity implements View.OnClickListener {

    private static String TAG = ViewDocumentActivity.class.getSimpleName();
    private ImageView imageView;
    private Button addNewDocumentButton;
    private Button ocrButton;
    private Button shareDocumentButton;

    @Override
    protected void init() {
        super.init();
        imageView = findViewById(R.id.resultImage);
        addNewDocumentButton = findViewById(R.id.addNewDocumentButton);
        ocrButton = findViewById(R.id.ocrButton);
        shareDocumentButton = findViewById(R.id.shareButton);

        // set on click listener
        addNewDocumentButton.setOnClickListener(this);
        ocrButton.setOnClickListener(this);
        shareDocumentButton.setOnClickListener(this);

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
        Bundle bundle = intent.getExtras();
        String filePath = bundle.getString("filePath");
        File image = new File(filePath);
        Picasso.get().load(image).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_view,menu);
        return super.onCreateOptionsMenu(menu);
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
                Intent addDocumentIntent = new Intent(ViewDocumentActivity.this,CameraActivity.class);
                startActivity(addDocumentIntent);
                break;

            case R.id.ocrButton:
                // for testing
                Intent ocrIntent = new Intent(ViewDocumentActivity.this, ResultActivity.class);
                startActivity(ocrIntent);
                break;

            case R.id.shareButton:
                Log.d(TAG, "onClick: display share pop up");
                break;

            default:
                break;
        }
    }
}
