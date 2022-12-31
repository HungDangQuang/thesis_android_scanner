package uit.app.document_scanner;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class BitmapResult {
    private Bitmap bitmap;
    private RectF coordinates;
    private String category;

    public BitmapResult(Bitmap bitmap, RectF coordinates, String category){
        this.bitmap = bitmap;
        this.coordinates = coordinates;
        this.category = category;
    }

    public Bitmap getBitmapResult(){
        return bitmap;
    }

    public RectF getCoordinates(){
        return coordinates;
    }

    public String getCategory() {
        return category;
    }
}
