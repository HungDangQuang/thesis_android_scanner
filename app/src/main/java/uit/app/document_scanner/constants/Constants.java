package uit.app.document_scanner.constants;

import android.os.Environment;

public class Constants {
    public static final String APP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DocumentScanner/Images";
    public static final String TEMP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DocumentScanner/Temp";
    public static final String ORIGINAL_IMAGE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DocumentScanner/OriginalImages";
    public static final String FOLDER_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DocumentScanner/Folders";

    public static int K_SIZE_BLUR = 3;
    public static int CANNY_THRESH_L = 85;
    public static int CANNY_THRESH_U = 185;
    public static int TRUNC_THRESH = 150;
    public static final String URL = "http://172.20.10.3:5002";
    public static final float NAME_RATIO = (float) (2.1/5.7);
    public static final float HOMETOWN_RATIO = (float) (3.85/5.7);
    public static final float ADDRESS_RATIO = (float) (4.85/5.7);
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;
    public static final int PERMISSION_REQUEST_CAMERA = 101;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

}
