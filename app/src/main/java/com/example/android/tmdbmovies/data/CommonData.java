package com.example.android.tmdbmovies.data;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static android.graphics.BitmapFactory.decodeFile;

/**
 * Created by Ashok on 6/24/2017.
 */

/**
 * Purpose:
 * 1. Make life simple to adapt to all resolutions and orientation
 * 2. Provide File handling. The methods provided by FileHandler are not enough.
 * For example, saving files on 'Set Favourite' is done from ShowMovieDetailsActivity which does not have
 * access to thumbnails in Main Activity. Further, Picaso does loading in the background and it becomes
 * very complex to activate only when Target is loaded. Hence using Bitmap from ImageView for file storage.
 * 3. Selected Thumbnail tempBitMap is always stored by MainActivity so that it can be saved alongside poster.
 */
public class CommonData {

    public static boolean isPortrait;

    public static int widthDp;
    public static int heightDp;
    public static float displayScale = 1;

    public static int thumbNailSelect;
    public static int posterSelect;

    private static File thumbNailsDir;
    private static File postersDir;
    private static boolean dirsReady;

    private static Bitmap tempThumbNailBitmap;

    private static boolean onlineStatus;

    public static String msg;


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
            thumbNailSelect = 92;
            posterSelect = 154;
        }
        else if (measure < 240) {
            thumbNailSelect = 154;
            posterSelect = 185;
        }
        else if (measure < 360) {
            thumbNailSelect = 185;
            posterSelect = 342;
        }
        else if (measure < 768){
            thumbNailSelect = 300;
            posterSelect = 500;
        }
        else {
            thumbNailSelect = 300;
            posterSelect = 780;
        }

        dirsReady = false;
        File dir = new File(context.getFilesDir(),"Download");
        if (!dir.exists()) {
            if (!dir.mkdirs())
                Toast.makeText(context,"Download dir not created",Toast.LENGTH_LONG).show();
            return;
        }

        dir = new File(dir,"tmdbmovies");
        if (!dir.exists()) {
            if (!dir.mkdirs())
                Toast.makeText(context,"Main dir not created",Toast.LENGTH_LONG).show();
            return;
        }
        // Create directories if not available
        thumbNailsDir = new File(dir,"thumbnails");
        if (!thumbNailsDir.exists()) {
            if (!thumbNailsDir.mkdirs())
                Toast.makeText(context,"Thumbnails dir not created",Toast.LENGTH_LONG).show();
            return;
        }

        postersDir = new File(dir,"posters");
        if (!postersDir.exists()) {
            if (!postersDir.mkdirs())
                Toast.makeText(context,"Posters dir not created",Toast.LENGTH_LONG).show();
            return;
        }
        dirsReady = true;

        msg = "Thumbnails:";
        File[] files = thumbNailsDir.listFiles();
        msg = msg.concat(files.length + " files:");
        for (int i = 0; i < files.length; i++) {
            msg = msg.concat(files[i].getName() + "-" + files[i].length() + ": ");
        }
        msg = msg.concat(":: Posters:");
        files = postersDir.listFiles();
        msg = msg.concat(files.length + " files:");
        for (int i = 0; i < files.length; i++) {
            msg = msg.concat(files[i].getName() + "-" + files[i].length() + ": ");
        }
    }

    public static boolean isDirReady() {
        return dirsReady;
    }

    public static void setOnline(boolean state) {
        onlineStatus = state;
    }

    public static boolean isOnline() {
        return onlineStatus;
    }
    public static boolean savePoster(String id, Bitmap bm) {
        if (!dirsReady)
            return false;
        File file = new File(postersDir,id + ".jpg");
        if (!file.exists()) {
            try {
                FileOutputStream os = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Now, save the thumbnail also{
        file = new File(thumbNailsDir,id + ".jpg");
        if (!file.exists()) {
            try {
                FileOutputStream os = new FileOutputStream(file);
                tempThumbNailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static void deletePoster(String id) {
        if (!dirsReady)
            return;
        File file = new File(postersDir,id + ".jpg");
        if (!file.exists())
            return;
        file.delete();
        file = new File(thumbNailsDir,id + ".jpg");
        if (!file.exists())
            return;
        file.delete();
        return;
    }

    public static Bitmap getPosterAsBitmap(String id) {
        if (!dirsReady)
            return null;
        return BitmapFactory.decodeFile(postersDir + File.separator + id + ".jpg");
    }

    public static Bitmap getThumbNailAsBitmap(String id) {
        if (!dirsReady)
            return null;
        return BitmapFactory.decodeFile(thumbNailsDir + File.separator + id + ".jpg");
    }

    /**
     * Called by MainActivity before launching showMovieDetailsActivity
     * So that  thumbnail is also saved by it even though it does not have the ID or bitmap
     * @param bm
     * @param id
     */
    public static void setTempBitmap(Bitmap bm) {
        tempThumbNailBitmap = bm;
    }
}
