package com.example.android.tmdbmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ashok on 6/19/2017.
 */

public class ReviewArrayAdapter extends ArrayAdapter<String> {

    ArrayList<String> mAuthors;
    public final Context context;
    public ReviewArrayAdapter(Context context, ArrayList<String> asp) {
        super(context,R.layout.entry_layout,asp);
        this.context = context;
        mAuthors = asp;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.entry_layout, parent, false);
        TextView textView1 = (TextView) rowView.findViewById(R.id.entry_name);
        textView1.setText(mAuthors.get(position));
        return rowView;
    }
    @Override
    public int getCount() {
        return mAuthors.size();
    }
    @Override
    public String getItem(int i) {
        return mAuthors.get(i);
    }
}
