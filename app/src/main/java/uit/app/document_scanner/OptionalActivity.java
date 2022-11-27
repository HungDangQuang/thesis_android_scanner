package uit.app.document_scanner;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import org.opencv.android.OpenCVLoader;

public class OptionalActivity extends BaseActivity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getApplication().registerActivityLifecycleCallbacks(new LifeCycleHandler());
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
    }

    @Override
    protected void init() {

        //Always show status bar

        OpenCVLoader.initDebug();
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();
            // Show Status Bar.
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return 0;
    }
}
