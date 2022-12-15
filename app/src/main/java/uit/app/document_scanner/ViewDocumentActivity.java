package uit.app.document_scanner;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class ViewDocumentActivity extends OptionalActivity {

    private ImageView imageView;

    @Override
    protected void init() {
        super.init();
        imageView = findViewById(R.id.resultImage);

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
}
