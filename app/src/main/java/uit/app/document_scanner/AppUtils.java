package uit.app.document_scanner;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.airbnb.lottie.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppUtils {

    public Bitmap getBitmap(Uri selectedImg, Activity activity) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 3;
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor = activity.getContentResolver().openAssetFileDescriptor(selectedImg,"r");
        return BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(),null,options);
    }

    public String saveBitmapToFile(Bitmap bm){
        File file = getOutputMediaFile();
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

    public File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"MyCameraApp");
//        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/SavedImages");
        if(!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}
