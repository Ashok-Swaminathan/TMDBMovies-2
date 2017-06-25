package com.example.android.tmdbmovies;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ashok on 6/19/2017.
 */

public class TrailerArrayAdapter extends ArrayAdapter<String> {

    int entryWidth;

    ArrayList<String> mNames;
    public final Context context;
    public TrailerArrayAdapter(Context context, ArrayList<String> asp) {
        super(context,R.layout.entry_layout,asp);
        this.context = context;
        mNames = asp;
        setWidthOnOrientation();
    }

    public void setWidthOnOrientation() {
        DisplayMetrics dMetrics = new DisplayMetrics();
        float density = dMetrics.density;
        int w = Math.round(dMetrics.widthPixels / density);
        int h = Math.round(dMetrics.heightPixels / density);
        if (w < h) { // portrait
            entryWidth = w - 10;
        }
        else { // landscape
            entryWidth = w / 2 - 10;
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.entry_layout, parent, false);
        TextView textView1 = (TextView) rowView.findViewById(R.id.entry_name);
        textView1.setText(mNames.get(position));
        ViewGroup.LayoutParams lp = rowView.getLayoutParams();
        lp.width = entryWidth;
        return rowView;
    }
    @Override
    public int getCount() {
        return mNames.size();
    }
    @Override
    public String getItem(int i) {
        return mNames.get(i);
    }

}
