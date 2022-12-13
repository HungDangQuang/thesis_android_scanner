package uit.app.document_scanner;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.Nullable;

public class ResultActivity extends OptionalActivity {

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_view,menu);

        return super.onCreateOptionsMenu(menu);

    }
}
