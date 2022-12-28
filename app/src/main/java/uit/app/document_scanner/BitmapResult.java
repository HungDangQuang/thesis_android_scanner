package uit.app.document_scanner;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class BitmapResult {
    private Bitmap bitmap;
    private RectF coordinates;

    public BitmapResult(Bitmap bitmap, RectF coordinates){
        this.bitmap = bitmap;
        this.coordinates = coordinates;
    }

    public Bitmap getBitmapResult(){
        return bitmap;
    }

    public RectF getCoordinates(){
        return coordinates;
    }
}
