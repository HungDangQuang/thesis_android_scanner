package uit.app.document_scanner;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import org.apache.commons.io.FileUtils;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import uit.app.document_scanner.constants.Constants;

public class LifeCycleHandler implements Application.ActivityLifecycleCallbacks {

    private static String TAG = LifeCycleHandler.class.getSimpleName();

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log.d(TAG, "onActivityCreated: created new activity");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

        File dir = new File(Constants.TEMP_DIR);

        if(dir.exists()){
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
