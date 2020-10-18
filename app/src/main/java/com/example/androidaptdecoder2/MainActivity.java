package com.example.androidaptdecoder2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Placeholder;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;
import com.jsibbold.zoomage.ZoomageView;

import java.io.*;
import java.util.Arrays;

import static java.lang.System.*;

public class MainActivity extends AppCompatActivity {

    LinearLayout startLayout;
    TextView chooseFileTextView;
    Button chooseFileButton;
    Button startButton;

    LinearLayout decodingLayout;
    public static ProgressBar decoderProgressBar;
    public static TextView progressTextView;

    ConstraintLayout imageLayout;
    ZoomageView imageView;

    ConstraintLayout placeholderBackground;


    File inputFile = null;
    File outputFolder = null;
    public static Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startLayout = findViewById(R.id.startLayout);
        chooseFileButton = findViewById(R.id.openFileButton);
        chooseFileTextView = findViewById(R.id.chooseFileTextView);
        startButton = findViewById(R.id.startButton);

        decodingLayout = findViewById(R.id.decodingLayout);
        decoderProgressBar = findViewById(R.id.decodingProgessBar);
        progressTextView = findViewById(R.id.progressTextView);

        imageLayout = findViewById(R.id.imageLayout);
        imageView = findViewById(R.id.imageView);

        placeholderBackground = findViewById(R.id.placeholderBackground);


        startLayout.setVisibility(View.VISIBLE);
        decodingLayout.setVisibility(View.GONE);
        imageLayout.setVisibility(View.GONE);

        startButton.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1002);
        }

        outputFolder = new File(Environment.getExternalStorageDirectory().toString());
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

    //start the decoding process
    public void start(View v) {
        out.println(inputFile);
        out.println(outputFolder);
        if (inputFile != null) {
            if (outputFolder != null) {

                //start the decoder with an async response
                Decoder decoder = (Decoder) new Decoder(new Decoder.AsyncResponse() {

                    //show bitmap when decoding is finished
                    @Override
                    public void processFinish(Bitmap bitmap) {
                        image = bitmap;

                        out.println(bitmap);

                        //change to image layout
                        imageLayout.setVisibility(View.VISIBLE);
                        decodingLayout.setVisibility(View.GONE);

                        imageView.setImageBitmap(bitmap);
                    }
                }).execute(inputFile);

                //change to decoding layout
                decodingLayout.setVisibility(View.VISIBLE);
                startLayout.setVisibility(View.GONE);

            } else {
                Toast.makeText(this, "No output folder selected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No input file selected", Toast.LENGTH_SHORT).show();
        }
    }

    //lets user pick a folder
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

    //lets the user pick a file
    public void chooseFile(View v) {

        final StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .allowAddFolder(true)
                .customFilter(Arrays.asList("wav"))
                .disableMultiSelect()
                .setDialogTitle("Select Audio File")
                .build();

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                inputFile = new File(path);
                chooseFileTextView.setText(inputFile.getPath());
                startButton.setVisibility(View.VISIBLE);
            }
        });

        chooser.show();
    }

    public void openSettings(MenuItem m) {

    }

    public void showMenuPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_more_menu, popup.getMenu());
        popup.show();
    }

    public void saveImage(View v) {
        out.println(inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().length() - 3));
        File outputFile = new File(inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().length() - 3) + "jpg");
        //overwrite file if it already exists
        if (outputFile.exists()) outputFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            MainActivity.image.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(this, "Saved image to " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
        }
    }

    public void back(View v) {
        inputFile = null;

        startButton.setVisibility(View.GONE);
        chooseFileTextView.setText("choose file");

        decodingLayout.setVisibility(View.GONE);
        imageLayout.setVisibility(View.GONE);
        startLayout.setVisibility(View.VISIBLE);
    }
}