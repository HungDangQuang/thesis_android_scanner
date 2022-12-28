package uit.app.document_scanner;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DigitsDetector {
    private static final String MODEL_PATH = "mnist.tflite";

    private Interpreter tflite;

    private ByteBuffer inputBuffer = null;

    // Output array [batch_size, 10]
    private float[][] mnistOutput = null;

    // Specify the output size
    private static final int NUMBER_LENGTH = 10;

    // Specify the input size
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_IMG_SIZE_X = 28;
    private static final int DIM_IMG_SIZE_Y = 28;
    private static final int DIM_PIXEL_SIZE = 1;

    // Number of bytes to hold a float (32 bits / float) / (8 bits / byte) = 4 bytes / float

    private static final int BYTE_SIZE_OF_FLOAT = 4;

    public DigitsDetector(Activity activity) {
        try {
            // Define the TensorFlow Lite Interpreter with the model
            tflite = new Interpreter(loadModelFile(activity));
            inputBuffer =
                    ByteBuffer.allocateDirect(
                            BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
            inputBuffer.order(ByteOrder.nativeOrder());
            mnistOutput = new float[DIM_BATCH_SIZE][NUMBER_LENGTH];
        } catch (IOException e) {
            Log.e("Detector", "IOException loading the tflite file");
        }
    }
    //...
    /**
     * Load the model file from the assets folder
     */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        String pathToDataFile = "app/src/main/assets/mnist.tflite";
        if ((new File(pathToDataFile).exists()) == true){
            Log.d("Detector", "loadModelFile: ");
        }
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public int detectDigit(Bitmap bitmap) {
        preprocess(bitmap);
        runInference();
        int predictedNumber = postprocess();
        return predictedNumber;
    }

    private void preprocess(Bitmap bitmap) {
        int[] pixels = new int[28 * 28];

        // Load bitmap pixels into the temporary pixels variable
        bitmap.getPixels(pixels, 0, 28, 0, 0, 28, 28);

        for (int i = 0; i < pixels.length; ++i) {
            // Set 0 for white and 255 for black pixels
            int pixel = pixels[i];
            int channel = pixel & 0xff;
            inputBuffer.putFloat(0xff - channel);
        }
    }

    private int postprocess() {
        for (int i = 0; i < mnistOutput[0].length; i++) {
            float value = mnistOutput[0][i];
            Log.d("Detector", "Output for " + Integer.toString(i) + ": " + Float.toString(value));
            // Check if this number is the one we care about. If yes, return the index
            if (value == 1f) {
                return i;
            }
        }
        return -1;
    }

    protected void runInference() {
        tflite.run(inputBuffer, mnistOutput);
    }

}
