package uit.app.document_scanner;

import android.os.Environment;

public class Constants {
    public static final String APP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MyCameraApp";
    public static final String TEMP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Temp";
    public static int K_SIZE_BLUR = 3;
    public static int KSIZE_CLOSE = 10;
    public static int CANNY_THRESH_L = 85;
    public static int CANNY_THRESH_U = 185;
    public static int TRUNC_THRESH = 150;
    public static int CUTOFF_THRESH = 155;
    public static final String URL = "";
}
