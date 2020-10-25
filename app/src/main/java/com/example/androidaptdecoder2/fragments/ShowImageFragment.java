package com.example.androidaptdecoder2.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.androidaptdecoder2.MainActivity;
import com.example.androidaptdecoder2.R;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowImageFragment #newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowImageFragment extends Fragment implements View.OnClickListener {


    //stuff I dont understand
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ShowImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShowImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowImageFragment newInstance(String param1, String param2) {
        ShowImageFragment fragment = new ShowImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    //stuff I understand starts here

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_image, container, false);

        //set button click listeners
        view.findViewById(R.id.saveButton).setOnClickListener(this);
        view.findViewById(R.id.newButton).setOnClickListener(this);

        //show the decoded image in a image view
        ((ZoomageView) view.findViewById(R.id.imageView)).setImageBitmap(MainActivity.image);

        return view;
    }

    //hande button clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton:
                saveImage();
                break;

            case R.id.newButton:
                home();
                break;
        }
    }

    //saves the decoded image to the same path as as the input file
    public void saveImage() {
        //create output file path from input file
        File outputFile = new File(MainActivity.inputFile.getAbsolutePath().substring(0, MainActivity.inputFile.getAbsolutePath().length() - 3) + "jpg");
        //overwrite file if it already exists
        if (outputFile.exists()) outputFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            MainActivity.image.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(getActivity(), "Successfully saved image to " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to save image to "  + outputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
    }

    //show the start fragment
    public void home() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StartFragment startFragment = new StartFragment();
        fragmentTransaction.replace(R.id.main_content, startFragment);
        fragmentTransaction.commit();
    }
}