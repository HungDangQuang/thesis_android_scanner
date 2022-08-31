package uit.app.document_scanner;

import android.graphics.Bitmap;

public interface IScanner {
    void displayHint(ScanHint scanHint);
    void onPictureClicked(Bitmap bitmap);
}
