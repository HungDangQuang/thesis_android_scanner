package uit.app.document_scanner.utils;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uit.app.document_scanner.SaveOptions;
import uit.app.document_scanner.constants.Constants;

public class AppUtils {

    private final String TAG = AppUtils.class.getSimpleName();

    public Bitmap getBitmap(Uri selectedImg, Activity activity) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor = activity.getContentResolver().openAssetFileDescriptor(selectedImg,"r");
        return BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(),null,options);
    }

    public String saveBitmapToFile(Bitmap bm, SaveOptions option){

        File file = getOutputMediaFile(option);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error",e.getMessage());
        }
        return file.getAbsolutePath();
    }

    public File getOutputMediaFile(SaveOptions option){

        File mediaStorageDir = null;

        switch (option){
            case APP:
                mediaStorageDir = new File(Constants.APP_DIR);
                break;
            case TEMP:
                mediaStorageDir = new File(Constants.TEMP_DIR);
                break;
            case ORIGINAL:
                mediaStorageDir = new File(Constants.ORIGINAL_IMAGE_DIR);
                break;
//            break;

        }
        if(!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
//        return mediaStorageDir;
//        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/SavedImages");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public void deleteImage(Uri uri){
        File fDelete = new File(String.valueOf(uri));
        if (fDelete.exists()){
            Log.d(TAG, "deleteImage: file exists");
            fDelete.delete();
        }
    }

}
