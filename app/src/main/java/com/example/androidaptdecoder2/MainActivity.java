package com.example.androidaptdecoder2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.codekidlabs.storagechooser.StorageChooser;
import com.example.androidaptdecoder2.fragments.StartFragment;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static File inputFile = null;
    public static File outputFolder = null;
    public static Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start the startFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StartFragment startFragment = new StartFragment();
        fragmentTransaction.replace(R.id.main_content, startFragment);
        fragmentTransaction.commit();

        //check for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1002);
        }

        //temporary assignment until settings are implemented
        outputFolder = Environment.getExternalStorageDirectory();
    }

    //handle permission requests results
    public void onRequestPermissionsResults(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1002:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                }  else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    //lets user pick a folder
    //not in use
    public void chooseFolder(View v) {
        final StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(this)
                .withFragmentManager(getFragmentManager())
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .allowCustomPath(true)
                .withMemoryBar(true)
                .allowAddFolder(true)
                .build();

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                outputFolder = new File(path);
            }
        });

        chooser.show();
    }
}