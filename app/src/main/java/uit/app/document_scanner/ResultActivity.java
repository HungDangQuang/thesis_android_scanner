package uit.app.document_scanner;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.squareup.picasso.Picasso;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uit.app.document_scanner.ml.EfficientdetLiteCid;

public class ResultActivity extends OptionalActivity{

    private static String TAG = ResultActivity.class.getSimpleName();
    final Calendar myCalendar= Calendar.getInstance();

    private EditText editableID;
    private EditText editableName;
    private EditText editableDOB;
    private EditText editableHometown;
    private EditText editableAddress;
    private Button confirmButton;
    public static final String TESS_DATA = "/tessdata";
    private String inputImagePath;
    @Override
    protected void init() {
        super.init();
        editableID = findViewById(R.id.editableID);
        editableName = findViewById(R.id.editableName);
        editableDOB = findViewById(R.id.editableDOB);
        editableHometown = findViewById(R.id.editableHometown);
        editableAddress = findViewById(R.id.editableAddress);
        confirmButton = findViewById(R.id.acceptButton);
        editableDOB.setFocusable(false);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
            }
        };

        editableDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ResultActivity.this,date,myCalendar.get(Calendar.DAY_OF_MONTH),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.YEAR));
                datePickerDialog.show();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Person person = new Person(editableID.getText().toString(),
                        editableName.getText().toString(),
                        editableDOB.getText().toString(),
                        editableHometown.getText().toString(),
                        editableAddress.getText().toString());
                new DatabaseHandler().execute(person);
            }
        });

        Intent ocrIntent = getIntent();
        Bundle bundle = ocrIntent.getExtras();
        inputImagePath = bundle.getString("rgbImagePath");
        Bitmap bitmap = BitmapFactory.decodeFile(inputImagePath);
        ocr(bitmap);
//        detectText(bitmap);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        editableID.post(new Runnable() {
//            @Override
//            public void run() {
//                String id = getValueFromSharedPreferences("id");
//                editableID.setText(id);
//            }
//        });
//
//        editableName.post(new Runnable() {
//            @Override
//            public void run() {
//                String name = getValueFromSharedPreferences("name");
//                editableName.setText(name);
//            }
//        });
//
//        editableDOB.post(new Runnable() {
//            @Override
//            public void run() {
//                String dob = getValueFromSharedPreferences("dob");
//                editableDOB.setText(dob);
//            }
//        });
//
//        editableHometown.post(new Runnable() {
//            @Override
//            public void run() {
//                String hometown = getValueFromSharedPreferences("hometown");
//                editableHometown.setText(hometown);
//            }
//        });
//
//        editableAddress.post(new Runnable() {
//            @Override
//            public void run() {
//                String address = getValueFromSharedPreferences("address");
//                editableAddress.setText(address);
//            }
//        });

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_view,menu);

        return super.onCreateOptionsMenu(menu);

    }

    private void updateLabel(){
        String myFormat="dd-MM-yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        editableDOB.setText(dateFormat.format(myCalendar.getTime()));
    }

    private String getValueFromSharedPreferences(String key){
        SharedPreferences mPrefs = getSharedPreferences("ordered text", Context.MODE_PRIVATE);
        String str = mPrefs.getString(key, "");
        return str;
    }

    private class DatabaseHandler extends AsyncTask<Person,Void,Void>{

        @Override
        protected Void doInBackground(Person... people) {
            Person person = people[0];
            PersonRoomDatabase personDatabase = PersonRoomDatabase.getInstance(ResultActivity.this);
            List<Person> list = personDatabase.personDao().getListOfPeople();
            Log.d(TAG, "doInBackground: " + list.get(0).personName);
            personDatabase.personDao().insertPerson(person);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Context context = getApplicationContext();
            CharSequence text = "Information is saved";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void ocr(Bitmap bm){
        try {
            EfficientdetLiteCid model = EfficientdetLiteCid.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bm);

            // Runs model inference and gets result.
            EfficientdetLiteCid.Outputs outputs = model.process(image);

            // Releases model resources if no longer used.
            model.close();

//            handleOutputs(bm,outputs.getDetectionResultList());
            detectText(bm,outputs.getDetectionResultList());
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

//    private void prepareTessData(){
//        try{
//            File dir = getExternalFilesDir(TESS_DATA);
//            if(!dir.exists()){
//                if (!dir.mkdir()) {
//                    Toast.makeText(getApplicationContext(), "The folder " + dir.getPath() + "was not created", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            String pathToDataFile = "/storage/emulated/0/Android/data/uit.app.document_scanner/files/tessdata/vie.traineddata";
//            if(!(new File(pathToDataFile)).exists()){
//                InputStream in = getAssets().open("vie.traineddata");
//                OutputStream out = new FileOutputStream(pathToDataFile);
//                byte [] buff = new byte[1024];
//                int len ;
//                while(( len = in.read(buff)) > 0){
//                    out.write(buff,0,len);
//                }
//                in.close();
//                out.close();
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }

//    private void detectText(Bitmap bm){
//        TessBaseAPI tessBaseAPI = new TessBaseAPI();
//        String dataPath = getExternalFilesDir("/").getPath() + "/";
//        tessBaseAPI.init(dataPath,"vie");
//        tessBaseAPI.setImage(bm);
//        Log.d(TAG, "detectText: " + tessBaseAPI.getUTF8Text());
//        tessBaseAPI.end();
//    }

    private List<TextResult> sortByX(List<TextResult> list){

        Collections.sort(list, new Comparator<TextResult>() {
            @Override
            public int compare(TextResult t0, TextResult t1) {
                return Float.compare(t0.getCoordinates().left, t1.getCoordinates().left);
            }
        });

        return list;
    }

    private float calculateAverage(List<TextResult> list){
        float sumOfYCoordinates = 0;
        for(TextResult res: list){
            sumOfYCoordinates += res.getCoordinates().top;
        }
        return sumOfYCoordinates/list.size();
    }



    private HashMap<String, List<TextResult>> sortByY(List<TextResult> list){
        float average = calculateAverage(list);
        HashMap<String,List<TextResult>> hashMap = new HashMap<>();
        List<TextResult> line1 = new ArrayList<>();
        List<TextResult> line2 = new ArrayList<>();

        for (TextResult res : list){
            if(res.getCoordinates().top < average){
                line1.add(res);
            }
            else {
                line2.add(res);
            }
        }

        hashMap.put("line1",line1);
        hashMap.put("line2",line2);
        return hashMap;
    }

    // OCR using Google ML Kit

    private void handleOutputs(Bitmap bm, List<EfficientdetLiteCid.DetectionResult> list){

        List<TextResult> id = new ArrayList<>();
        List<TextResult> name = new ArrayList<>();
        List<TextResult> dob = new ArrayList<>();
        List<TextResult> hometown = new ArrayList<>();
        List<TextResult> address = new ArrayList<>();
        for (int i = 0; i  < list.size(); i++){

            int index = i;

            RectF location = list.get(index).getLocationAsRectF();
            String category = list.get(index).getCategoryAsString();

            RectF validLocation = createValidLocation(bm,location);

            Bitmap croppedBm = Bitmap.createBitmap(bm,Math.round(validLocation.left),Math.round(validLocation.top),Math.round(validLocation.width()),Math.round(validLocation.height()));
            Bitmap scaledBm = Bitmap.createScaledBitmap(croppedBm,500,500,false);

            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            InputImage image = InputImage.fromBitmap(scaledBm, 0);
            Task<Text> result =
                    recognizer.process(image);



            result.addOnCompleteListener(new OnCompleteListener<Text>() {
                @Override
                public void onComplete(Task<Text> task) {
                    String result = task.getResult().getText();
                    TextResult textResult = new TextResult(result,location);

                    switch (category){

                        case "id":
                            id.add(textResult);
//                            Log.d(TAG, "onComplete: id size: " + id.size());
                            break;

                        case "name":
                            name.add(textResult);
                            break;

                        case "dob":
                            dob.add(textResult);
                            break;

                        case "hometown":
                            hometown.add(textResult);
                            Log.d(TAG, "home town: " + hometown.size());
                            break;

                        case "address":
                            address.add(textResult);
                            break;
                    }
                    int sumOfTexts = id.size() + name.size() + dob.size() + hometown.size() + address.size();

                    if (sumOfTexts == list.size()) {
                        // create hash map to store key-category and value-list result
                        InputParam inpId = new InputParam("id",id);
                        InputParam inpName = new InputParam("name",name);
                        InputParam inpDob = new InputParam("dob",dob);
                        InputParam inpHometown = new InputParam("hometown",hometown);
                        InputParam inpAddress = new InputParam("address",address);

                        new ResultActivity.ReorderTextTask().execute(inpId);
                        new ResultActivity.ReorderTextTask().execute(inpName);
                        new ResultActivity.ReorderTextTask().execute(inpDob);
                        new ResultActivity.ReorderTextTask().execute(inpHometown);
                        new ResultActivity.ReorderTextTask().execute(inpAddress);
                    }
                }
            });
        }
    }

    // OCR using VietOCR
    private void detectText(Bitmap bm, List<EfficientdetLiteCid.DetectionResult> list){
        List<BitmapResult> id = new ArrayList<>();
        List<BitmapResult> name = new ArrayList<>();
        List<BitmapResult> dob = new ArrayList<>();
        List<BitmapResult> hometown = new ArrayList<>();
        List<BitmapResult> address = new ArrayList<>();

        for (int i = 0; i  < list.size(); i++) {

            int index = i;

            RectF location = list.get(index).getLocationAsRectF();
            String category = list.get(index).getCategoryAsString();
            float score = list.get(index).getScoreAsFloat();
            Log.d(TAG, "detectText: " + category +  "_" + score);

            if (score > 0.45) {

                RectF validLocation = createValidLocation(bm, location);

                Bitmap croppedBm = Bitmap.createBitmap(bm, Math.round(validLocation.left), Math.round(validLocation.top), Math.round(validLocation.width()), Math.round(validLocation.height()));
                Bitmap scaledBm = Bitmap.createScaledBitmap(croppedBm, 300, 300, false);

                switch (category){
                    case "id":
                        id.add(new BitmapResult(scaledBm,location));
                        break;

                    case "name":
                        name.add(new BitmapResult(scaledBm,location));
                        break;

                    case "dob":
                        dob.add(new BitmapResult(scaledBm,location));
                        break;

                    case "hometown":
                        hometown.add(new BitmapResult(scaledBm,location));
                        break;

                    case "address":
                        address.add(new BitmapResult(scaledBm,location));
                        break;
                }

            }
        }

        List<BitmapResult> newName = sortBitmap(name);
        List<BitmapResult> newHometown = sortBitmap(hometown);
        List<BitmapResult> newAddress = sortBitmap(address);

        new CallAPI().execute(id);
        new CallAPI().execute(newName);
        new CallAPI().execute(dob);
        new CallAPI().execute(newHometown);
        new CallAPI().execute(newAddress);
    }

    private void convertToInputBody(List<BitmapResult> list){
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        int i = 0;
        Log.d(TAG, "convertToInputBody: " + list.size());
        for (BitmapResult bmRes : list) {
            Bitmap bm = bmRes.getBitmapResult();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG,100,stream);
            byte[] bytes = stream.toByteArray();
            Log.d(TAG, "convertToInputBody: " + "image" + i + "_Android_Flask_" + i + ".jpg" );
            multipartBodyBuilder.addFormDataPart("image_" + i, "Android_Flask_" + i + "_" + list.getClass().getSimpleName() + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), bytes));
            i++;
        }

        RequestBody postBodyImage = multipartBodyBuilder.build();
        String postUrl = Constants.URL;
        postRequest(postUrl,postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                try {
                    final String responseData = response.body().string();
                    Log.d(TAG, "run: SUCCESS " + responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private byte[] createByteArray(String filePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bm = BitmapFactory.decodeFile(filePath,options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100,stream);
        return stream.toByteArray();
    }

    private RequestBody createImageIntoBody(byte[] byteArray){
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("image", "Android_Flask_" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        RequestBody postBodyImage = multipartBodyBuilder.build();
        return postBodyImage;
    }

    private List<BitmapResult> sortBitmap(List<BitmapResult> list){
        if (list.size() == 0 || list.size() == 1){
            return list;
        }

        else {
            HashMap<String,List<BitmapResult>> hashMap = sortBimapByY(list);
            List<BitmapResult> line1 = hashMap.get("line1");
            List<BitmapResult> line2 = hashMap.get("line2");

            line1 = sortBimapByX(line1);
            line2 = sortBimapByX(line2);

            line1.addAll(line2);
            return line1;
        }
    }

    private List<BitmapResult> sortBimapByX(List<BitmapResult> list){
        Collections.sort(list, new Comparator<BitmapResult>() {
            @Override
            public int compare(BitmapResult t0, BitmapResult t1) {
                return Float.compare(t0.getCoordinates().left, t1.getCoordinates().left);
            }
        });

        return list;
    }

    private float calculateAverageY(List<BitmapResult> list){
        float sumOfYCoordinates = 0;
        for(BitmapResult res: list){
            sumOfYCoordinates += res.getCoordinates().top;
        }
        return sumOfYCoordinates/list.size();
    }

    private HashMap<String, List<BitmapResult>> sortBimapByY(List<BitmapResult> list){

        float average = calculateAverageY(list);
        HashMap<String,List<BitmapResult>> hashMap = new HashMap<>();
        List<BitmapResult> line1 = new ArrayList<>();
        List<BitmapResult> line2 = new ArrayList<>();

        for (BitmapResult res : list){
            if(res.getCoordinates().top < average){
                line1.add(res);
            }
            else {
                line2.add(res);
            }
        }

        hashMap.put("line1",line1);
        hashMap.put("line2",line2);
        return hashMap;

    }



    private String sortText(List<TextResult> list){


        if(list.size() == 0){
            return null;
        }
        else if (list.size() == 1){
            return list.get(0).getText();
        }

        else {
            HashMap<String,List<TextResult>> hashMap = sortByY(list);
            List<TextResult> line1 = hashMap.get("line1");
            List<TextResult> line2 = hashMap.get("line2");

            line1 = sortByX(line1);
            line2 = sortByX(line2);

            line1.addAll(line2);

            String str = "";

            for(TextResult res : line1) {
                str = str + " " + res.getText();
            }
            return str;
        }
    }

    private RectF createValidLocation(Bitmap bm, RectF location){
        float x = location.left;
        float y = location.top;
        float right = location.right;
        float bottom = location.bottom;

        if(x < 0){
            x = 0;
        }

        if(y < 0){
            y = 0;
        }

        if (right > bm.getWidth()){
            right = bm.getWidth();
        }

        if(bottom > bm.getHeight()){
            bottom = bm.getHeight();
        }

        return new RectF(x,y,right,bottom);
    }

    private class ReorderTextTask extends AsyncTask<InputParam,Void, List<String>>{

        @Override
        protected List<String> doInBackground(InputParam... inputParams) {

            InputParam inputParam = inputParams[0];
            String str = sortText(inputParam.getTextResultList());

            SharedPreferences sharedPreferences = ResultActivity.this.getSharedPreferences("ordered text", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(inputParam.getKeyName(), str);
            editor.commit();
            List<String> list = new ArrayList<>();
            Log.d(TAG, "doInBackground: " + inputParam.getKeyName() + " :" + str);
            list.add(inputParam.getKeyName());
            list.add(str);
            return list;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            String destination = strings.get(0);
            String content = strings.get(1);
            switch (destination){
                case "id":
                    editableID.setText(content);
                    break;

                case "name":
                    editableName.setText(content);
                    break;

                case "dob":
                    editableDOB.setText(content);
                    break;

                case "hometown":
                    editableHometown.setText(content);
                    break;

                case "address":
                    editableAddress.setText(content);
                    break;

                default:
                    break;
            }
        }
    }

    private class CallAPI extends AsyncTask<List<BitmapResult>,Void,Void>{
        @Override
        protected Void doInBackground(List<BitmapResult>... lists) {
            List<BitmapResult> res = lists[0];
            convertToInputBody(res);
            return null;
        }
    }
}
