package com.example.android.tmdbmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.tmdbmovies.data.CommonData;
import com.example.android.tmdbmovies.db.MovieContract;
import com.example.android.tmdbmovies.db.MovieDbHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;

import static android.graphics.BitmapFactory.decodeFile;


public class MainActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    boolean TEST_OFFLINE = false;

    boolean LOCAL_DIRS_READY;

    String THUMBNAIL_URL_BASE;

    String DB_URL_BASE;

    String API_KEY;

    /**
     * Need to create keys for saving to Bundle on onSave to be used by onCreate
     * Also need to create columnNames for String array of projection
     * Combine Both!! Done in MovieEntry class
     */

    private static final int ID_MOVIE_LOADER_KEY = 77;

    private static final String SELECT_INDEX_KEY = "selectIndex";

    boolean dataLoaded = false;

    String api_key;

    public MovieData movieData;

    public TextView msgView;

    public int selectIndex = 0;

    public String[] sortBy = new String[3];

    public String errorMsg = "";

    public Cursor favouritesCursor;

    public MovieDbHelper movieDbHelper;

    /**
     * Movie details layout contains title, release date, movie poster, vote average, and plot synopsis.
     */


    ProgressBar pb;

    TNAdapter adapter;
    RecyclerView mMainRecyclerView;
    ArrayList<TNUnit> TNUnits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        msgView = (TextView) findViewById(R.id.msg_view);
       CommonData.setCommonData(this);

        CommonData.setOnline(!TEST_OFFLINE);
        LOCAL_DIRS_READY = CommonData.isDirReady();

        API_KEY = getResources().getString(R.string.string_api_key);
        DB_URL_BASE = getResources().getString(R.string.db_url_base);
        THUMBNAIL_URL_BASE = getResources().getString(R.string.thumbnail_url_base) + "w" + CommonData.thumbNailSelect;

        sortBy[0] = getResources().getString(R.string.string_popular);
        sortBy[1] = getResources().getString(R.string.string_top_rated);
        sortBy[2] = getResources().getString(R.string.string_favourites);

        movieDbHelper = new MovieDbHelper(this);

        movieData = new MovieData(this);
        TNUnits = new ArrayList<>();

        api_key = getResources().getString(R.string.api_key);


        try {
            getSupportLoaderManager().initLoader(ID_MOVIE_LOADER_KEY, null, this);
        } catch (Exception e) {
            msgView.setText("Exception ->" + e.getMessage());
        }
        initViews();

        if ((savedInstanceState != null) && (savedInstanceState.get(SELECT_INDEX_KEY) != null)) {
            selectIndex = Integer.parseInt(savedInstanceState.getString(SELECT_INDEX_KEY));
            movieData.movieId = savedInstanceState.getStringArray(MovieContract.MovieEntry.COLUMN_ID);
            movieData.movieTitle = savedInstanceState.getStringArray(MovieContract.MovieEntry.COLUMN_TITLE);
            movieData.releaseDate = savedInstanceState.getStringArray(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            movieData.posterPath = savedInstanceState.getStringArray(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
            movieData.synopsis = savedInstanceState.getStringArray(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
            movieData.rating = savedInstanceState.getStringArray(MovieContract.MovieEntry.COLUMN_RATING);
            dataLoaded = true;
            populateViews();
            setMsgText();
        }
        else
             loadData();

    }
    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (movieData.movieId.length > 0) {
            outState.putString(SELECT_INDEX_KEY,String.valueOf(selectIndex));
            outState.putStringArray(MovieContract.MovieEntry.COLUMN_ID,movieData.movieId);
            outState.putStringArray(MovieContract.MovieEntry.COLUMN_TITLE,movieData.movieTitle);
            outState.putStringArray(MovieContract.MovieEntry.COLUMN_SYNOPSIS,movieData.synopsis);
            outState.putStringArray(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,movieData.releaseDate);
            outState.putStringArray(MovieContract.MovieEntry.COLUMN_RATING,movieData.rating);
            outState.putStringArray(MovieContract.MovieEntry.COLUMN_POSTER_PATH,movieData.posterPath);
        }
    } // Save needed data for resume
    private void initViews(){
        pb = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mMainRecyclerView = (RecyclerView)findViewById(R.id.rv_thumbnails);
        mMainRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManager;
        if(CommonData.isPortrait)
            layoutManager = new GridLayoutManager(getApplicationContext(),3);
        else
            layoutManager = new GridLayoutManager(getApplicationContext(),5);
        mMainRecyclerView.setLayoutManager(layoutManager);

        mMainRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                (RecyclerItemClickListener.OnItemClickListener) new TNClickHandler()));
        adapter = new TNAdapter(getApplicationContext(),TNUnits);
        mMainRecyclerView.setAdapter(adapter);
    }


    public class TNClickHandler implements RecyclerItemClickListener.OnItemClickListener {
        public void onItemClick(View v,int position) {

            // Save the view bitmap into CommonData temp
            ImageView v1 = (ImageView) v.findViewById(R.id.tn_img);
            BitmapDrawable drawable = (BitmapDrawable) v1.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            CommonData.setTempBitmap(bitmap);

            //Create and fire intent
            Context context = MainActivity.this;
            Class destinationClass = ShowMovieDetailsActivity.class;
            Intent intent = new Intent(context,destinationClass);
            intent.putExtra(MovieContract.MovieEntry.COLUMN_ID,movieData.movieId[position]);
            intent.putExtra(MovieContract.MovieEntry.COLUMN_TITLE,movieData.movieTitle[position]);
            intent.putExtra(MovieContract.MovieEntry.COLUMN_POSTER_PATH,movieData.posterPath[position]);
            intent.putExtra(MovieContract.MovieEntry.COLUMN_SYNOPSIS,movieData.synopsis[position]);
            intent.putExtra(MovieContract.MovieEntry.COLUMN_RATING,movieData.rating[position]);
            intent.putExtra(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,movieData.releaseDate[position]);
            intent.putExtra("api_key",api_key);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ID_MOVIE_LOADER_KEY : {
                // Simple
                Uri allQueryUri = MovieContract.MovieEntry.CONTENT_URI;
                // No Sorting

                String selection = null; // All

                return new CursorLoader(this,
                        allQueryUri,
                        MovieContract.MovieEntry.MOVIES_ALL_FIELDS,
                        selection,
                        null,
                        null);
            }
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);

        }
    } // onCreateLoader
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Set favouritesCursor
        favouritesCursor = data;
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need tojust set favouritesCursor to null
         */
        favouritesCursor = null;
    }
    private void populateViews() {

        TNUnits.clear();

        for(int i=0;i<movieData.movieId.length;i++){

            TNUnit tnUnit = new TNUnit();
            tnUnit.setTN_Text(movieData.movieId[i]);
            tnUnit.setTN_image_url(THUMBNAIL_URL_BASE + movieData.posterPath[i]);
            TNUnits.add(tnUnit);
        }
        if (adapter != null)
            adapter.notifyDataSetChanged();
        setMsgText();

    }

    public void setMsgText() {
        if (selectIndex == 0) {
             msgView.setText(R.string.fetch_success_popular);
        }
        else if (selectIndex == 1){
            msgView.setText(R.string.fetch_success_top_rated);
        }
        else { // Favourites
            msgView.setText(getResources().getString(R.string.fetch_success_favourites));
        }
    }
    @Override
    // Menu of sort selection
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.selection, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.by_popular) {
            selectIndex = 0;
            adapter.setSelectedIndex(selectIndex);
            loadData();
        }
        else if (id == R.id.by_top_rated){
            selectIndex = 1;
            adapter.setSelectedIndex(selectIndex);
            loadData();
        }
        else { // Favourites
            adapter.setSelectedIndex(selectIndex);
            loadLocalData();
        }
        return true;
    }
    private void loadLocalData() {
        pb.setVisibility(View.VISIBLE);
        if ((favouritesCursor == null) || (favouritesCursor.getCount() == 0)) {
            msgView.setText("No favourites to display!");
            return;
        }

        if (!movieData.populateMovieDataFromDB(favouritesCursor)) {
            msgView.setText("Error in loading data from cursor!");
            pb.setVisibility(View.INVISIBLE);
            return;
        }

          pb.setVisibility(View.INVISIBLE);

        populateViews();
    }

    private void loadData() {

        if (!CommonData.isOnline()) {
            selectIndex = 2;
            adapter.setSelectedIndex(selectIndex);
            msgView.setText(R.string.fetch_failed);
            loadLocalData();
            return;
        }
        msgView.setText(R.string.fetching);
        String[] params = new String[2];
        params[0] = sortBy[selectIndex];
        params[1] = api_key;

        pb.setVisibility(View.VISIBLE);
        new FetchDataTask().execute(params);

    }


    public class FetchDataTask extends AsyncTask<String, Void, String[]> {

        URL url;
        @Override
        protected String[] doInBackground(String... params) {


            if (params == null)
                return null;
            String retVal = null;
            url = null;
            try {
                Uri uri = Uri.parse(DB_URL_BASE + params[0]).buildUpon()
                        .appendQueryParameter(API_KEY, params[1]).build();
                url = new URL(uri.toString());
                if (url == null) {
                    return null;
                }

                retVal = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (IOException io) {
                io.printStackTrace();
                return null;
            }


            if (retVal == null)
                return null;

            String[] retVals = new String[1];
            retVals[0] = retVal;
            return retVals;
        }


        @Override
        protected void onPostExecute(String[] retVals) {
            pb.setVisibility(View.INVISIBLE);
            dataLoaded = false;
            if (retVals == null){
                CommonData.setOnline(false);
                msgView.setText(R.string.fetch_failed);
                return;
            }
            else
                CommonData.setOnline(true);
            dataLoaded = movieData.populateMovieData(retVals[0]);
            if (!dataLoaded) {
                msgView.setText(R.string.json_failed);
                return;
            }

            populateViews();
            return;

        }
    }

}
