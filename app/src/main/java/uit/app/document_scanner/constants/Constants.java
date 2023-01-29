package uit.app.document_scanner.constants;

import android.os.Environment;

public class Constants {
    public static final String APP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MyCameraApp";
    public static final String TEMP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Temp";
    public static final String ORIGINAL_IMAGE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/OriginalImages";
    public static int K_SIZE_BLUR = 3;
    public static int KSIZE_CLOSE = 10;
    public static int CANNY_THRESH_L = 85;
    public static int CANNY_THRESH_U = 185;
    public static int TRUNC_THRESH = 150;
    public static int CUTOFF_THRESH = 155;
    public static final String URL = "http://192.168.3.197:5002";
    public static final float NAME_RATIO = (float) (2.1/5.7);
    public static final float HOMETOWN_RATIO = (float) (3.85/5.7);
    public static final float ADDRESS_RATIO = (float) (4.85/5.7);
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;
    public static final int PERMISSION_REQUEST_CAMERA = 101;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

}