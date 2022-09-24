package uit.app.document_scanner;

import android.os.Environment;

public class Constants {
    public static final String APP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "MyCameraApp";
    public static final String TEMP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MyCameraApp/Temp";
}
