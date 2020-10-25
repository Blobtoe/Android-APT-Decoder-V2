package com.example.androidaptdecoder2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.androidaptdecoder2.R;


//stuff I dont understand
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DecodingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DecodingFragment extends Fragment {

    static TextView progressTextView;
    static ProgressBar progressBar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DecodingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DecodingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DecodingFragment newInstance(String param1, String param2) {
        DecodingFragment fragment = new DecodingFragment();
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
        View view = inflater.inflate(R.layout.fragment_decoding, container, false);

        //get ui elements
        progressTextView = view.findViewById(R.id.progressTextView);
        progressBar = view.findViewById(R.id.decodingProgessBar);

        return view;
    }

    //connecting function from the background decoding process to fragment ui
    public static void update(String updateText, Integer progressValue) {
        progressTextView.setText(updateText);
        progressBar.setProgress(progressValue);
    }
}