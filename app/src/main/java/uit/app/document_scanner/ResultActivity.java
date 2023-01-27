package uit.app.document_scanner;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uit.app.document_scanner.ml.EfficientdetLiteCid;
import uit.app.document_scanner.model.Person;
import uit.app.document_scanner.model.PersonDao;
import uit.app.document_scanner.view.LoadingDialog;

public class ResultActivity extends OptionalActivity{

    private static String TAG = ResultActivity.class.getSimpleName();
    final Calendar myCalendar= Calendar.getInstance();
    private LoadingDialog loadingDialog;

    private EditText editableID;
    private EditText editableName;
    private EditText editableDOB;
    private EditText editableHometown;
    private EditText editableAddress;
    private Button confirmButton;
    private String originalImageName;
    int numOfRes = 5;

    int returnedYear,returnedMonth,returnedDay;

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

        numOfRes = 0;

        returnedYear = 0;
        returnedMonth = 0;
        returnedDay = 0;

        loadingDialog = new LoadingDialog(this);

        // set limit of characters in text edit id: old cid
        editableID.setFilters(new InputFilter[] { new InputFilter.LengthFilter(10) });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
//                myCalendar.set(Calendar.YEAR, year);
//                myCalendar.set(Calendar.MONTH,month);
//                myCalendar.set(Calendar.DAY_OF_MONTH,day);
//                updateLabel();
                returnedDay = day;
                returnedMonth = month + 1;
                returnedYear = year;
                editableDOB.setText(returnedDay + "-" + returnedMonth + "-" + returnedYear);

            }
        };

        editableDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DatePickerDialog datePickerDialog = new DatePickerDialog(ResultActivity.this,date,myCalendar.get(Calendar.DAY_OF_MONTH),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.YEAR));
//                datePickerDialog.show();
                new DatePickerDialog(ResultActivity.this, date,returnedYear,returnedMonth-1,returnedDay).show();

            }
        });

        PersonDao personDao = new PersonDao();
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = editableID.getText().toString();
                String fullName = editableName.getText().toString();
                String dob = editableDOB.getText().toString();
                String hometown = editableHometown.getText().toString();
                String address = editableAddress.getText().toString();
                Person person = new Person(id,fullName,dob,hometown,address);

                if (id.length() == 0){
                    Toast.makeText(ResultActivity.this,"ID is empty",Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isDigitsOnly(id) == false){
                    Toast.makeText(ResultActivity.this,"ID is not valid",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (isValidName(fullName) == false){
                    Toast.makeText(ResultActivity.this,"Name is not valid",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(dob.length() == 0 ){
                    Toast.makeText(ResultActivity.this,"Dob is empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(hometown.length() == 0){
                    Toast.makeText(ResultActivity.this,"Hometown is empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(address.length() == 0){
                    Toast.makeText(ResultActivity.this,"Address is empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                else {

                    Task<Void> allTask;
                    allTask = Tasks.whenAll(personDao.add(person));
                    allTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ResultActivity.this,"new person added successfully",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResultActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ResultActivity.this,"fail to add new person",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
//                new AddPerson().execute(person);
            }
        });

        Intent ocrIntent = getIntent();
        Bundle bundle = ocrIntent.getExtras();
        originalImageName = bundle.getString("originalImageName");
        Bitmap bitmap = BitmapFactory.decodeFile(Constants.ORIGINAL_IMAGE_DIR + "/" + originalImageName);
        new OCR().execute(bitmap);
    }



    private Boolean isValidName(String str){

        if (str.length() == 0){
            return false;
        }

        if (str.matches(".*[0-9].*")){
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        loadingDialog.startLoadingDialog();

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_result;
    }

    private void updateLabel(){
//        String myFormat="dd-MM-yyyy";
//        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        editableDOB.setText(returnedDay + "-" + returnedMonth +"-" + returnedYear);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public List<EfficientdetLiteCid.DetectionResult> detectText(Bitmap bm){
        try {
            EfficientdetLiteCid model = EfficientdetLiteCid.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bm);

            // Runs model inference and gets result.
            EfficientdetLiteCid.Outputs outputs = model.process(image);

            // Releases model resources if no longer used.
            model.close();

            return outputs.getDetectionResultList();

//            detectText(bm,outputs.getDetectionResultList());
        } catch (IOException e) {
            // TODO Handle the exception
            return null;
        }
    }

    private HashMap<String,List<BitmapResult>> sortByYUsingRatio(List<BitmapResult> list, float ratio, int bitmapHeight){

        HashMap<String,List<BitmapResult>> hashMap = new HashMap<>();
        List<BitmapResult> line1 = new ArrayList<>();
        List<BitmapResult> line2 = new ArrayList<>();

        for (BitmapResult res : list){
            float resRatio = (float) (res.getCoordinates().top / bitmapHeight);
            if(resRatio < ratio){
                Log.d(TAG, "sortByYUsingRatio: res added: " + resRatio);
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

    // OCR using VietOCR
    private void extractText(Bitmap bm, List<EfficientdetLiteCid.DetectionResult> list){
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
                Bitmap scaledBm = Bitmap.createScaledBitmap(croppedBm, croppedBm.getWidth() * 6, croppedBm.getHeight() * 6, false);

                switch (category){
                    case "id":
                        id.add(new BitmapResult(scaledBm,location,category));
                        break;

                    case "name":
                        name.add(new BitmapResult(scaledBm,location,category));
                        break;

                    case "dob":
                        dob.add(new BitmapResult(scaledBm,location,category));
                        break;

                    case "hometown":
                        hometown.add(new BitmapResult(scaledBm,location,category));
                        break;

                    case "address":
                        address.add(new BitmapResult(scaledBm,location,category));
                        break;
                }

            }
        }

        name = sortResultsUsingRatio(name,Constants.NAME_RATIO,bm.getHeight());
        hometown = sortResultsUsingRatio(hometown,Constants.HOMETOWN_RATIO,bm.getHeight());
        address = sortResultsUsingRatio(address,Constants.ADDRESS_RATIO,bm.getHeight());


        new CallAPI().execute(id);
        new CallAPI().execute(name);
        new CallAPI().execute(dob);
        new CallAPI().execute(hometown);
        new CallAPI().execute(address);
    }

    private void convertToInputBody(List<BitmapResult> list){
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        int i = 0;
        String category = list.get(0).getCategory();
        Log.d(TAG, "convertToInputBody: " + list.size());
        for (BitmapResult bmRes : list) {
            Bitmap bm = bmRes.getBitmapResult();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG,0,stream);
            byte[] bytes = stream.toByteArray();
            Log.d(TAG, "convertToInputBody: " + "image" + i + "_Android_Flask_" + i + ".jpg" );
            multipartBodyBuilder.addFormDataPart("image_" + i, "Android_Flask_" + i + "_" + list.getClass().getSimpleName() + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), bytes));
            i++;
        }

        RequestBody postBodyImage = multipartBodyBuilder.build();
        String postUrl = Constants.URL;
        postRequest(postUrl,postBodyImage,category);
    }

    void postRequest(String postUrl, RequestBody postBody, String category) {

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
                Log.d(TAG, "onResponse: category " + category);

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                try {
                    String responseData = response.body().string();
                    switch (category){
                        case "id":
                            if (responseData.length() > 9) {
                                responseData = responseData.substring(1);
                            }
                            String finalId = responseData;
                            editableID.post(new Runnable() {
                                @Override
                                public void run() {
                                    editableID.setText(finalId);
                                }
                            });
                            break;

                        case "name":
                            String finalName = responseData;
                            editableName.post(new Runnable() {
                                @Override
                                public void run() {
                                    editableName.setText(finalName);
                                }
                            });
                            break;

                        case "dob":
                            responseData = responseData.replace(".","-");
                            String finalDob = responseData;

                            String[] separated = responseData.split("-");
                            Log.d(TAG, "separated: " + separated[0]);

                            editableDOB.post(new Runnable() {
                                @Override
                                public void run() {
                                    editableDOB.setText(finalDob);
                                }
                            });
                            String strYear = separated[2];
                            Integer iYear = new Integer(strYear);
                            returnedYear = Integer.parseInt(iYear.toString());

                            String strMonth = separated[1];
                            Integer iMonth = new Integer(strMonth);
                            returnedMonth = Integer.parseInt(iMonth.toString());

                            String strDay = separated[0];
                            Integer iDay = new Integer(strDay);
                            returnedDay = Integer.parseInt(iDay.toString());

                            break;

                        case "hometown":
                            String finalHometown = responseData;
                            editableHometown.post(new Runnable() {
                                @Override
                                public void run() {
                                    editableHometown.setText(finalHometown);
                                }
                            });
                            break;

                        case "address":
                            String finalAddress = responseData;
                            editableAddress.post(new Runnable() {
                                @Override
                                public void run() {
                                    editableAddress.setText(finalAddress);
                                }
                            });
                            break;

                        default:
                            break;
                    }
                }
                catch (Exception e){

                }
                numOfRes++;
                if (numOfRes == 5){
                    loadingDialog.dismissDialog();
                }
            }
        });
    }

    private List<BitmapResult> sortResultsUsingRatio(List<BitmapResult> list, float ratio, int bitmapHeight){
        if (list.size() == 0 || list.size() == 1){
            return list;
        }

        else {
            HashMap<String,List<BitmapResult>> hashMap = sortByYUsingRatio(list,ratio,bitmapHeight);
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

    private class CallAPI extends AsyncTask<List<BitmapResult>,Void,Void>{
        @Override
        protected Void doInBackground(List<BitmapResult>... lists) {
            List<BitmapResult> res = lists[0];
            convertToInputBody(res);
            return null;
        }
    }

    private class OCR extends AsyncTask<Bitmap,Void,Void>{

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            Bitmap bm = bitmaps[0];
            List<EfficientdetLiteCid.DetectionResult> detectionResultList = detectText(bm);
            extractText(bm,detectionResultList);
            return null;
        }
    }

}
