package uit.app.document_scanner;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.tensorflow.lite.support.image.TensorImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

            handleOutputs(bm,outputs.getDetectionResultList());
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private void prepareTessData(){
        try{
            File dir = getExternalFilesDir(TESS_DATA);
            if(!dir.exists()){
                if (!dir.mkdir()) {
                    Toast.makeText(getApplicationContext(), "The folder " + dir.getPath() + "was not created", Toast.LENGTH_SHORT).show();
                }
            }

            String pathToDataFile = "/storage/emulated/0/Android/data/uit.app.document_scanner/files/tessdata/vie.traineddata";
            if(!(new File(pathToDataFile)).exists()){
                InputStream in = getAssets().open("vie.traineddata");
                OutputStream out = new FileOutputStream(pathToDataFile);
                byte [] buff = new byte[1024];
                int len ;
                while(( len = in.read(buff)) > 0){
                    out.write(buff,0,len);
                }
                in.close();
                out.close();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void detectText(Bitmap bm){
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        String dataPath = getExternalFilesDir("/").getPath() + "/";
        tessBaseAPI.init(dataPath,"vie");
        tessBaseAPI.setImage(bm);
        Log.d(TAG, "detectText: " + tessBaseAPI.getUTF8Text());
        tessBaseAPI.end();
    }

    private List<TextResult> sortByX(List<TextResult> list){

        Collections.sort(list, new Comparator<TextResult>() {
            @Override
            public int compare(TextResult t0, TextResult t1) {
                return Float.compare(t0.getCoordinates().left, t1.getCoordinates().left);
            }
        });

        return list;
    }

    private float calculateAverageY(List<TextResult> list){
        float sumOfYCoordinates = 0;
        for(TextResult res: list){
            sumOfYCoordinates += res.getCoordinates().top;
        }
        return sumOfYCoordinates/list.size();
    }

    private HashMap<String, List<TextResult>> sortByY(List<TextResult> list){
        float average = calculateAverageY(list);
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





}
