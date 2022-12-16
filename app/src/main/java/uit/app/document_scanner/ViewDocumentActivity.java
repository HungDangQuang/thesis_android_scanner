package uit.app.document_scanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class ViewDocumentActivity extends OptionalActivity implements View.OnClickListener {

    private static String TAG = ViewDocumentActivity.class.getSimpleName();
    private ImageView imageView;
    private Button addNewDocumentButton;
    private Button ocrButton;
    private Button shareDocumentButton;
    private String filePath;

    @Override
    protected void init() {
        super.init();
        imageView = findViewById(R.id.resultImage);
        addNewDocumentButton = findViewById(R.id.addNewDocumentButton);
        ocrButton = findViewById(R.id.ocrButton);
        shareDocumentButton = findViewById(R.id.shareButton);

        // set on click listener
        addNewDocumentButton.setOnClickListener(this);
        ocrButton.setOnClickListener(this);
        shareDocumentButton.setOnClickListener(this);

        // show back button on action bar
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_view_document;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        filePath = bundle.getString("filePath");
        File image = new File(filePath);
        Picasso.get().load(image).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_view,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {



        switch (item.getItemId()){
            case R.id.delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewDocumentActivity.this);
                builder.setTitle("Document Scanner");
                builder.setMessage("Do you want to delete this document?");
                builder.setCancelable(false);
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = new File(filePath);
                        file.delete();
                        if(file.exists()){
                            try {
                                file.getCanonicalFile().delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(file.exists()){
                                getApplicationContext().deleteFile(file.getName());
                            }
                        }
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                break;
            case R.id.rename:
                File originalFile = new File(filePath);
                String dir = originalFile.getParent();

                AlertDialog.Builder renameBuilder = new AlertDialog.Builder(ViewDocumentActivity.this);
                renameBuilder.setTitle("Document Scanner");
                renameBuilder.setCancelable(false);

//                final EditText input = new EditText(ViewDocumentActivity.this);
                String fileName = originalFile.getName();
                String extension = fileName.substring(fileName.lastIndexOf("."));
                Log.d(TAG, "extension: " + extension);


                LayoutInflater layoutInflater = getLayoutInflater();
                View dialogLayout = layoutInflater.inflate(R.layout.edit_text_layout,null);
                EditText input = dialogLayout.findViewById(R.id.textEdit);
                input.setText(fileName.substring(0,fileName.lastIndexOf(".")));
                input.setTextColor(getResources().getColor(R.color.black));
                renameBuilder.setView(dialogLayout);
                renameBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String newName = input.getText().toString() + extension;
                        File renamedFile = new File(dir,newName);
                        Log.d(TAG, "renamed file: " + renamedFile);
                        originalFile.renameTo(renamedFile);

                        // update the file path
                        filePath = renamedFile.getAbsolutePath();
                        Log.d(TAG, "file path name: " + filePath);
                        dialogInterface.dismiss();
                    }
                });
                renameBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


                AlertDialog renameDialog = renameBuilder.create();
                renameDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.addNewDocumentButton:
                Intent addDocumentIntent = new Intent(ViewDocumentActivity.this,CameraActivity.class);
                startActivity(addDocumentIntent);
                break;

            case R.id.ocrButton:
                // for testing
                Intent ocrIntent = new Intent(ViewDocumentActivity.this, ResultActivity.class);
                startActivity(ocrIntent);
                break;

            case R.id.shareButton:
                Log.d(TAG, "onClick: display share pop up");
                break;

            default:
                break;
        }
    }

}
