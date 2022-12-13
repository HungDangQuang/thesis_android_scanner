package uit.app.document_scanner;

import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;

import androidx.annotation.Nullable;

public class ResultActivity extends OptionalActivity {

    private EditText editableID;
    private EditText editableName;
    private EditText editableDOB;
    private EditText editableHometown;
    private EditText editableAddress;


    @Override
    protected void init() {
        super.init();
        editableID = findViewById(R.id.editableID);
        editableName = findViewById(R.id.editableName);
        editableDOB = findViewById(R.id.editableDOB);
        editableHometown = findViewById(R.id.editableHometown);
        editableAddress = findViewById(R.id.editableAddress);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
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
