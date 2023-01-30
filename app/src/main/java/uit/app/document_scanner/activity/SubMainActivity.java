package uit.app.document_scanner.activity;

import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uit.app.document_scanner.adapter.Adapter;
import uit.app.document_scanner.adapter.FolderAdapter;
import uit.app.document_scanner.constants.Constants;

public class SubMainActivity extends MainActivity{

    String folderName;

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        folderName = intent.getExtras().getString("folderName");

    }

    @Override
    protected void onResume() {
        super.onResume();
        File folder = new File(Constants.FOLDER_DIR + "/" + folderName);
        if(folder.exists()){

            List<File> images = Arrays.asList(folder.listFiles());

            Adapter adapter = new Adapter(this, images);
            recyclerView.setAdapter(adapter);

            List<String> folderList = new ArrayList<>();
            FolderAdapter folderAdapter = new FolderAdapter(folderList);

            folderRecyclerView.setAdapter(folderAdapter);
        }
        else {

            List<File> images = new ArrayList<>();
            Adapter adapter = new Adapter(this, images);
            recyclerView.setAdapter(adapter);

            List<String> folderList = new ArrayList<>();
            FolderAdapter folderAdapter = new FolderAdapter(folderList);
            folderRecyclerView.setAdapter(folderAdapter);
        }
    }
}
