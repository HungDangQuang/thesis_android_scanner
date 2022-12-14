package uit.app.document_scanner;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultActivity extends OptionalActivity {

    private static String TAG = ResultActivity.class.getSimpleName();
    final Calendar myCalendar= Calendar.getInstance();

    private EditText editableID;
    private EditText editableName;
    private EditText editableDOB;
    private EditText editableHometown;
    private EditText editableAddress;

    @Override
    protected void init() {
        super.init();
        editableID = findViewById(R.id.editableID);
        editableName = findViewById(R.id.editableName);
        editableDOB = findViewById(R.id.editableDOB);
        editableHometown = findViewById(R.id.editableHometown);
        editableAddress = findViewById(R.id.editableAddress);
        editableDOB.setFocusable(false);

        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
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
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        editableID.post(new Runnable() {
            @Override
            public void run() {
                String id = getValueFromSharedPreferences("id");
                editableID.setText(id);
            }
        });

        editableName.post(new Runnable() {
            @Override
            public void run() {
                String name = getValueFromSharedPreferences("name");
                editableName.setText(name);
            }
        });

        editableDOB.post(new Runnable() {
            @Override
            public void run() {
                String dob = getValueFromSharedPreferences("dob");
                editableDOB.setText(dob);
            }
        });

        editableHometown.post(new Runnable() {
            @Override
            public void run() {
                String hometown = getValueFromSharedPreferences("hometown");
                editableHometown.setText(hometown);
            }
        });

        editableAddress.post(new Runnable() {
            @Override
            public void run() {
                String address = getValueFromSharedPreferences("address");
                editableAddress.setText(address);
            }
        });

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
}
