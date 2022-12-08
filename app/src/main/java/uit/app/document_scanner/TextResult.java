package uit.app.document_scanner;

import android.graphics.RectF;

import org.opencv.core.Rect;

public class TextResult {
    private String text;
    private RectF coordinates;

    public TextResult(String text, RectF coordinates){
        this.text = text;
        this.coordinates = coordinates;
    }

    public String getText(){
        return text;
    }

    public RectF getCoordinates(){
        return coordinates;
    }
}
