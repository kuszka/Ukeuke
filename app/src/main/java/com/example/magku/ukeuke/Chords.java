package com.example.magku.ukeuke;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by magku on 30.05.2017.
 */

public class Chords extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view =  inflater.inflate(R.layout.fragment_chords, container, false);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(getActivity()));
        return view;

    }
    public static Chords newInstance() {
        return new Chords();
    }
}
