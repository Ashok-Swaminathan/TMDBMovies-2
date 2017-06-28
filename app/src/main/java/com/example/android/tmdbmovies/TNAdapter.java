package com.example.android.tmdbmovies;

/**
 * Created by Ashok on 5/24/2017.
 */

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.tmdbmovies.data.CommonData;
import com.example.android.tmdbmovies.db.MovieContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;

public class TNAdapter extends RecyclerView.Adapter<TNAdapter.ViewHolder> {
    private ArrayList<TNUnit> tnUnits;
    private Context context;

    private int selectIndex = 0;

    public TNAdapter(Context context,ArrayList<TNUnit> tnUnits) {
        this.tnUnits = tnUnits;
        this.context = context;
    }

    public void setSelectedIndex(int index) {
        selectIndex = index;
    }

    @Override
    public TNAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TNAdapter.ViewHolder viewHolder, int i) {

        viewHolder.tn_text.setText(tnUnits.get(i).getTN_Text());
        if (checkFavouriteEntry(tnUnits.get(i).getTN_Text()))
            viewHolder.tn_text.setTextColor(Color.YELLOW);
        else
            viewHolder.tn_text.setTextColor(Color.WHITE);
        if (selectIndex != 2)
            Picasso.with(context).load(tnUnits.get(i).getTN_image_url()).into(viewHolder.tn_image);
        else
            viewHolder.tn_image.setImageBitmap(CommonData.getThumbNailAsBitmap(tnUnits.get(i).getTN_Text()));

    }
    public boolean checkFavouriteEntry(String id) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(id).build();
        String[] projection = new String[]{MovieContract.MovieEntry.COLUMN_ID};
        try {
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null, null);
            return (cursor.getCount() > 0);
        }catch (Exception e) {
            return false;
        }
    }
    @Override
    public int getItemCount() {
        return tnUnits.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tn_text;
        private ImageView tn_image;
        public ViewHolder(View view) {
            super(view);

            tn_text = (TextView)view.findViewById(R.id.tn_text);
            tn_image = (ImageView) view.findViewById(R.id.tn_img);
        }
    }

}
