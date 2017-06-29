package com.example.android.tmdbmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.tmdbmovies.data.CommonData;
import com.example.android.tmdbmovies.db.MovieContract;
import com.example.android.tmdbmovies.db.MovieDbHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class ShowMovieDetailsActivity extends AppCompatActivity {

    // api.themoviedb.org
    String POSTER_URL_BASE;
    String DB_URL_BASE;
    // Youtube URL
    String YOUTUBE_URL;

    public String errorText = "";
    final String RET_CODE = "cod";
    final String RESULTS_CODE = "results";
    // Videos
    final String NAME_CODE = "name";
    final String KEY_CODE = "key";
    String apiKey;
    // Reviews
    final String AUTHOR_CODE = "author";
    final String  URL_CODE = "url";

    public TextView msgView2;

    // Passed in
    String movieId; String movieTitle; String movieDate;
    String movieSynopsis; String movieRating;String moviePoster;

    // Either passed in or loaded
    // Videos (trailers)
    public ListView mLVTrailers;
    public TextView mTrailerHeader;
    public ArrayList<String> mTrailerUrls;
    public ArrayList<String> mTrailerNames;
    public TrailerArrayAdapter tAdapter;

    View separatorH;

    // Reviews
    public ListView mLVReviews;
    public TextView mReviewHeader;
    public ArrayList<String> mReviewUrls;
    public ArrayList<String> mReviewAuthors;
    public ReviewArrayAdapter rAdapter;


    public static final String SAVED_STATE_MOVIE_ID = "savedMovieId";
    public static final String SAVED_STATE_VIDEO_NAMES = "savedTrailerNames";
    public static final String SAVED_STATE_VIDEO_URLS = "savedTrailerUrls";
    public static final String SAVED_STATE_REVIEW_AUTHORS = "savedReviewAuthors";
    public static final String SAVED_STATE_REVIEW_URLS = "savedReviewUrls";

    boolean loadedTrailers;
    boolean loadedReviews;


    boolean favourite = false;

    public MovieDbHelper movieDbHelper;

    private Button bMarkButton;
    private ImageView ivStarView;

    public ShareActionProvider shareActionProvider;

    Intent myIntent; // Passed on to me
    public Context showContext;

    int computedY = 0; // For s_trailers and s_reviews
    // If s_synopsis is too long and projecting


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_movie_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // String definitions
        POSTER_URL_BASE = getResources().getString(R.string.image_url_base) + "w" + CommonData.posterSelect;
        DB_URL_BASE = getResources().getString(R.string.db_url_base);
        YOUTUBE_URL = getResources().getString(R.string.youtube_url_base);
        apiKey = getResources().getString(R.string.api_key);

        msgView2 = (TextView) findViewById(R.id.s_msgview_2);
        msgView2.setText("");
        msgView2.setVisibility(View.INVISIBLE);
        showContext = getBaseContext();
        separatorH = findViewById(R.id.separator_h);


        myIntent = getIntent();
        movieId = myIntent.getStringExtra(MovieContract.MovieEntry.COLUMN_ID);
        movieTitle = myIntent.getStringExtra(MovieContract.MovieEntry.COLUMN_TITLE);
        movieDate = myIntent.getStringExtra(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieSynopsis = myIntent.getStringExtra(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
        movieRating = myIntent.getStringExtra(MovieContract.MovieEntry.COLUMN_RATING);
        moviePoster = myIntent.getStringExtra(MovieContract.MovieEntry.COLUMN_POSTER_PATH);

        movieDbHelper = new MovieDbHelper(this);

        loadedReviews = false;
        loadedTrailers = false;

        if ((savedInstanceState != null) && (savedInstanceState.get(SAVED_STATE_MOVIE_ID) != null)
                && savedInstanceState.getString(SAVED_STATE_MOVIE_ID).equals(movieId)) {
            if (savedInstanceState.get(SAVED_STATE_VIDEO_NAMES) != null) {
                loadedTrailers = true;
                mTrailerNames = savedInstanceState.getStringArrayList(SAVED_STATE_VIDEO_NAMES);
                mTrailerUrls = savedInstanceState.getStringArrayList(SAVED_STATE_VIDEO_URLS);
            }
            else {
                mTrailerNames = new ArrayList<String>();
                mTrailerUrls = new ArrayList<String>();
            }
            if (savedInstanceState.get(SAVED_STATE_REVIEW_AUTHORS) != null) {
                loadedReviews = true;
                mReviewAuthors = savedInstanceState.getStringArrayList(SAVED_STATE_REVIEW_AUTHORS);
                mReviewUrls = savedInstanceState.getStringArrayList(SAVED_STATE_REVIEW_URLS);
            }
            else {
                mReviewAuthors = new ArrayList<String>();
                mReviewUrls = new ArrayList<String>();
            }

        }
        else {
            mTrailerNames = new ArrayList<String>();
            mTrailerUrls = new ArrayList<String>();
            mReviewAuthors = new ArrayList<String>();
            mReviewUrls = new ArrayList<String>();
        }

        ivStarView = (ImageView) findViewById(R.id.iv_star);
        bMarkButton = (Button) findViewById(R.id.b_favourite);
        bMarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavouriteField();
            }
        });

        /**
         * In Landscape layout, trailers and reviews overlap with synopsis if it is longer
         * This has to be adjusted AFTER the layout is over
         * Hence this listener is added to the separator_h
         * Only if the orientation is landscape
         */
        separatorH.getViewTreeObserver().addOnGlobalLayoutListener(new RefreshOnceListener());

        initViews();
        showDetails();

        favourite = checkFavouriteEntry(movieId);
        updateImages();
    }
    public void reportError(String s) {
        msgView2.setText(s);
        msgView2.setVisibility(View.VISIBLE);
    }
    
    public void updateImages() {
        if (favourite) {
            // It is favourite
            // Set star bright and button as UNMARK FAVOURITE
            ivStarView.setVisibility(View.VISIBLE);
            bMarkButton.setText(getResources().getString(R.string.unmark_favourite));
        }
        else {
            // It is not favourite
            // Set star bright and button as MARK FAVOURITE
            ivStarView.setVisibility(View.GONE);
            bMarkButton.setText(getResources().getString(R.string.mark_favourite));
        }
    }
    public void setFavouriteField() {
        if (!CommonData.isOnline()) {
            Toast.makeText(this,getResources().getString(R.string.offline_na),Toast.LENGTH_LONG).show();
            return;
        }
        if (favourite) {
            Uri uri = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).build();
            int numDeleted = getContentResolver().delete(uri,null,null);
            if (numDeleted > 0) {
                favourite = false;
                // Delete the poster
                CommonData.deletePoster(movieId);
            }
            else {
                reportError("Delete Favourite entry failed");
            }
        }
        else {

            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_ID,movieId);
            values.put(MovieContract.MovieEntry.COLUMN_TITLE,movieTitle);
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,movieDate);
            values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS,movieSynopsis);
            values.put(MovieContract.MovieEntry.COLUMN_RATING,movieRating);
            values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH,moviePoster);

            try {
                getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
                BitmapDrawable drawable = (BitmapDrawable)((ImageView) findViewById(R.id.s_poster)).getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                CommonData.savePoster(movieId,bitmap);
                favourite = true;
            }catch (Exception e1) {
                reportError(e1.getMessage());
            }
        }
        updateImages();
    }

    public boolean checkFavouriteEntry(String id) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).build();
        String[] projection = new String[]{MovieContract.MovieEntry.COLUMN_ID};
        try {
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null, null);
            boolean found = cursor.getCount() > 0;
            return found;
        }catch (Exception e) {
            reportError("Exception ->" + e.getMessage());
            return false;
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_MOVIE_ID,movieId);
        if (loadedTrailers) {
            outState.putStringArrayList(SAVED_STATE_VIDEO_NAMES,mTrailerNames);
            outState.putStringArrayList(SAVED_STATE_VIDEO_URLS,mTrailerUrls);
        }
        if (loadedReviews) {
            outState.putStringArrayList(SAVED_STATE_REVIEW_AUTHORS,mReviewAuthors);
            outState.putStringArrayList(SAVED_STATE_REVIEW_URLS,mReviewUrls);
        }
    }
    private void initViews() {

        mLVTrailers = (ListView)findViewById(R.id.lv_trailers);
        mLVTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onListItemClick((ListView) parent,view,position,id);
            }
        });
        tAdapter = new TrailerArrayAdapter(this,mTrailerNames);
        mLVTrailers.setAdapter(tAdapter);
        mTrailerHeader = (TextView) findViewById(R.id.s_trailers);


        mLVReviews = (ListView)findViewById(R.id.lv_reviews);
        mLVReviews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onListItemClick((ListView) parent,view,position,id);
            }
        });
        rAdapter = new ReviewArrayAdapter(this,mReviewAuthors);
        mLVReviews.setAdapter(rAdapter);

        mReviewHeader = (TextView) findViewById(R.id.s_reviews);
    }

    public void onListItemClick(ListView l, View v, int pos, long id) {
        if (l == null)
            return;
        TextView thisText = (TextView) v.findViewById(R.id.entry_name);
        thisText.setTextColor(ColorStateList.valueOf(Color.CYAN));

        String url;

        if (l == mLVTrailers) {
            url = mTrailerUrls.get(pos);
        }
        else {
            url = mReviewUrls.get(pos);
        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);

    }
    public class RefreshOnceListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {

            separatorH.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            // No action for portrait
            if (CommonData.isPortrait)
                return;

            // Now, cater for synopsis being longer than the poster
            int h1 = findViewById(R.id.s_poster).getHeight();
            int y1 = (int) findViewById(R.id.s_poster).getY();

            int h2 = findViewById(R.id.s_synopsis).getHeight();
            int y2 = (int) findViewById(R.id.s_synopsis).getY();

            int newY = h1 + y1;
            if ((h1 + y1) < (h2 + y2))// Synopsis is sticking further down
                newY = (h2 + y2);


            newY += 20; // For safety

            separatorH.setY(newY);

            ViewGroup vg = (ViewGroup) findViewById(R.id.details_display);
           // vg.invalidate();
        }
    } // RefreshOnceListener

    private void showDetails() {
        ((TextView) findViewById(R.id.s_title)).setText(movieTitle);
        ((TextView) findViewById(R.id.s_synopsis)).setText(getResources().getString(R.string.prefix_synopsis) +  movieSynopsis);
        ((TextView) findViewById(R.id.s_rating)).setText(getResources().getString(R.string.prefix_rate) + movieRating);
        ((TextView) findViewById(R.id.s_release_date)).setText(getResources().getString(R.string.prefix_release_date) + movieDate);
        if (CommonData.isOnline())
            Picasso.with(this).load(POSTER_URL_BASE +  moviePoster)
                    .into((ImageView) findViewById(R.id.s_poster));
        else
            ((ImageView) findViewById(R.id.s_poster)).setImageBitmap(CommonData.getPosterAsBitmap(movieId));

        if (!CommonData.isOnline()) {
            mTrailerHeader.setText(R.string.offline);
            mReviewHeader.setText(R.string.offline);
            return;
        }
        if (!loadedTrailers) {

            loadTrailers();
        }
        else {
            populateTrailerViews();
        }
        if (!loadedReviews) {
             loadReviewDetails();
        }
        else
            populateReviewsViews();
    }


    private void loadTrailers() {
        String[] params = new String[1];
        params[0] = DB_URL_BASE + movieId +  "/videos?api_key=" + apiKey;
        new FetchVideoDataTask().execute(params);
    }

    private void loadReviewDetails() {
        String[] params = new String[1];
        params[0] = DB_URL_BASE + movieId +  "/reviews?api_key=" + apiKey;
        new FetchReviewsDataTask().execute(params);
    }
    public class FetchReviewsDataTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            if (params == null)
                return null;
            String retVal = null;
            URL url = null;
            try {
                Uri uri = Uri.parse(params[0]);
                // Just a double check, will throw Exception
                url = new URL(uri.toString());

                retVal = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (Exception io) {
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
            if (retVals == null) {
                Log.d("REVIEW DATA","Null value returned by NetworkUtils");
                return;
            }
            if (populateReviewsData(retVals[0]))
            {
                populateReviewsViews();
            }
            else
                reportError("PopulateReviews failed");
            return;

        }
    }
    public void populateReviewsViews() {
        if (mReviewAuthors.size() == 0) {
            mReviewHeader.setText("No reviews available");
            mLVReviews.setVisibility(View.INVISIBLE);
            return;
        }
        setListViewHeight(mLVReviews);
        mLVReviews.setVisibility(View.VISIBLE);
        rAdapter.notifyDataSetChanged();
        mReviewHeader.setText(mReviewAuthors.size() + " Reviews, tap on Author to View");
    }
    public boolean populateReviewsData(String data) {

        try {
            JSONObject jsonData = new JSONObject(data);
            if (jsonData.has(RET_CODE)) {
                int errorCode = jsonData.getInt(RET_CODE);

                switch (errorCode) {
                    case HttpURLConnection.HTTP_OK: {
                        Log.d("DATA","RECEIVED HTTP OK");
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                    /* Location invalid */
                        errorText = "HTTP_NOT_FOUND";
                        return false;
                    }
                    default: {
                    /* Server probably down */
                        errorText = "OTHER";
                        return false;
                    }
                }
            }
            // Populate
            // Find array
            JSONArray reviewDataArray = jsonData.getJSONArray("results");
            int num = reviewDataArray.length();
            if (num == 0) {
                return true;
            }
            Log.d("REVIEW DATA","GOT REVIEW ARRAY OF LENGTH " + num);

            mReviewAuthors.clear();
            mReviewUrls.clear();

            for (int i = 0; i < num; i++) {
                JSONObject reviewDataSet = reviewDataArray.getJSONObject(i);
                mReviewAuthors.add(i,reviewDataSet.getString(AUTHOR_CODE));
                mReviewUrls.add(i,reviewDataSet.getString(URL_CODE));
            }
            Log.d("REVIEW DATA","POPULATED DATA");
            loadedReviews = true;
            return true;

        }catch (Exception e) {
            Log.d("REVIEW DATA","ERROR ->" + e.getMessage());
            errorText = "JSON " + e.getMessage();
            return false;
        }
    }

    public class FetchVideoDataTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            if (params == null)
                return null;
            String retVal = null;
            URL url = null;
            try {
                Uri uri = Uri.parse(params[0]);
                // Just a double check
                url = new URL(uri.toString());

                retVal = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (Exception io) {
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
            if (retVals == null) {
                Log.d("VIDEO DATA","Null value returned by NetworkUtils");
                return;
            }
           if (populateTrailerData(retVals[0]))
            {
                populateTrailerViews();
            }
            else
                reportError("PopulateTrailerData failed");


            return;

        }
    }
    public void populateTrailerViews() {
        if (mTrailerNames.size() == 0) {
            mTrailerHeader.setText("No trailers available");
            mLVTrailers.setVisibility(View.INVISIBLE);
            return;
        }
        // Set the height if icon image accordingll
        setListViewHeight(mLVTrailers);


        mLVTrailers.setVisibility(View.VISIBLE);
        tAdapter.notifyDataSetChanged();

        mTrailerHeader.setText(mTrailerNames.size() + " Trailers, tap on name to view");
    }
    public boolean populateTrailerData(String data) {

        try {
            JSONObject jsonData = new JSONObject(data);
            if (jsonData.has(RET_CODE)) {
                int errorCode = jsonData.getInt(RET_CODE);

                switch (errorCode) {
                    case HttpURLConnection.HTTP_OK: {
                        Log.d("DATA","RECEIVED HTTP OK");
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                    /* Location invalid */
                        errorText = "HTTP_NOT_FOUND";
                        return false;
                    }
                    default: {
                    /* Server probably down */
                        errorText = "OTHER";
                        return false;
                    }
                }
            }
            // Populate
            // Find array
            JSONArray trailerDataArray = jsonData.getJSONArray(RESULTS_CODE);
            int num = trailerDataArray.length();

            if (num == 0) {
                return true;
            }

            mTrailerNames.clear();
            mTrailerUrls.clear();

            for (int i = 0; i < num; i++) {
                JSONObject trailerDataSet = trailerDataArray.getJSONObject(i);
                mTrailerNames.add(i,trailerDataSet.getString(NAME_CODE));
                mTrailerUrls.add(i,YOUTUBE_URL + trailerDataSet.getString(KEY_CODE));
            }
            loadedTrailers = true;
            Log.d("VIDEO DATA","POPULATED DATA");

            return true;

        }catch (Exception e) {
            Log.d("VIDEO DATA","ERROR ->" + e.getMessage());
            errorText = "JSON " + e.getMessage();
            return false;
        }
    }
    public void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        if (listAdapter.getCount() == 0)
            return;

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem  = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
            }

            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_trailer, menu);
        shareActionProvider = new ShareActionProvider(this);
        shareActionProvider
                .setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.share_video : {
                if (mTrailerNames.size() == 0)
                    return false;
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/*");
                shareIntent.putExtra(Intent.EXTRA_TEXT, mTrailerUrls.get(0));
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, mTrailerNames.get(0));
                startActivity(Intent.createChooser(shareIntent, "Share using"));
                break;
            }
            case android.R.id.home: {
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                break;
            }
            default : {
                return false;
            }
        }
        return (super.onOptionsItemSelected(item));

    }
}
