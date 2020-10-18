package com.example.androidaptdecoder2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.io.FileOutputStream;

public class ShowImageActivity extends AppCompatActivity {

    ZoomageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        imageView = findViewById(R.id.imageView);

        imageView.setImageBitmap(MainActivity.image);
    }



    public void back(View v) {
        finish();
    }
}