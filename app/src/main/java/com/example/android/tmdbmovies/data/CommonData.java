package com.example.android.tmdbmovies.data;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by Ashok on 6/24/2017.
 */

public class CommonData {

    public static boolean isPortrait;

    public static int widthDp;
    public static int heightDp;
    public static float displayScale = 1;

    public static int thumbNailSelect;
    public static int posterSelect;

    public static void setCommonData(Context context) {
        heightDp = context.getResources().getConfiguration().screenHeightDp;
        widthDp =  context.getResources().getConfiguration().screenWidthDp;
        displayScale = context.getResources().getDisplayMetrics().density;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            isPortrait = true;
        else
            isPortrait = false;

        int measure = (heightDp > widthDp) ? heightDp : widthDp;

        if (measure < 200) {
            thumbNailSelect = 45;
            posterSelect = 154;
        }
        else if (measure < 240) {
            thumbNailSelect = 92;
            posterSelect = 185;
        }
        else if (measure < 360) {
            thumbNailSelect = 154;
            posterSelect = 342;
        }
        else if (measure < 768){
            thumbNailSelect = 185;
            posterSelect = 500;
        }
        else {
            thumbNailSelect = 300;
            posterSelect = 780;
        }


    }
}
