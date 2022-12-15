package uit.app.document_scanner;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class ViewDocumentActivity extends OptionalActivity {

    private ImageView imageView;

    @Override
    protected void init() {
        super.init();
        imageView = findViewById(R.id.resultImage);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_view_document;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }
}
