package com.example.androidaptdecoder2.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.codekidlabs.storagechooser.StorageChooser;
import com.example.androidaptdecoder2.Decoder;
import com.example.androidaptdecoder2.MainActivity;
import com.example.androidaptdecoder2.R;

import java.io.File;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment implements View.OnClickListener {

    TextView chooseFileTextView;
    Button startButton;

    //I don't know what all this stuff does

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public StartFragment newInstance(String param1, String param2) {
        StartFragment fragment = new StartFragment();
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
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        //get ui elements
        chooseFileTextView = view.findViewById(R.id.chooseFileTextView);
        startButton = view.findViewById(R.id.startButton);

        //set click listeners for ui buttons
        view.findViewById(R.id.openFileButton).setOnClickListener(this);
        startButton.setOnClickListener(this);

        //hide start button until input file is selected
        startButton.setVisibility(View.GONE);

        return view;
    }

    //handle button clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openFileButton:
                chooseFile();
                break;

            case R.id.startButton:
                start();
                break;
        }
    }

    //lets the user pick a file
    public void chooseFile() {
        final StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(getActivity())
                .withFragmentManager(getActivity().getFragmentManager())
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
                MainActivity.inputFile = new File(path);
                chooseFileTextView.setText(MainActivity.inputFile.getPath());
                startButton.setVisibility(View.VISIBLE);
            }
        });

        chooser.show();
    }

    //start the decoding process
    public void start() {
        if (MainActivity.inputFile != null) {
            if (MainActivity.outputFolder != null) {

                //start the decoder with an async response
                Decoder decoder = (Decoder) new Decoder(new Decoder.AsyncResponse() {

                    //show bitmap when decoding is finished
                    @Override
                    public void processFinish(Bitmap bitmap) {
                        MainActivity.image = bitmap;

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        ShowImageFragment showImageFragment = new ShowImageFragment();
                        fragmentTransaction.replace(R.id.main_content, showImageFragment);
                        fragmentTransaction.commit();
                    }
                }).execute(MainActivity.inputFile);

                //show the decoding fragment over the current fragment
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DecodingFragment decodingFragment = new DecodingFragment();
                //cant replace because then the processFinish function will throw an error
                fragmentTransaction.add(R.id.main_content, decodingFragment);
                fragmentTransaction.commit();

            } else {
                Toast.makeText(getActivity(), "No output folder selected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "No input file selected", Toast.LENGTH_SHORT).show();
        }
    }
}