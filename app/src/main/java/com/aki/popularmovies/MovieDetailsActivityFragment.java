package com.aki.popularmovies;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aki.popularmovies.data.MovieContract;
import com.aki.popularmovies.data.MovieDbHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {
    static final String MOVIE_ARG = "MOVIE";
    TextView review;
    Button trailer1,trailer2;
    Movie temp;
    MovieDbHelper database;
    boolean connectedToNetwork;
    Context mContext;
    private String shareTrailer;


    public MovieDetailsActivityFragment() {
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_details, container, false);
        Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();
        setHasOptionsMenu(false);
        if ((arguments != null) ||(intent!=null && intent.hasExtra(Intent.EXTRA_TEXT))) {
            if (arguments != null)
            temp = arguments.getParcelable(MOVIE_ARG);
            else
            temp = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            connectedToNetwork = MovieDetailsActivity.isConnected;
            review = (TextView) rootView.findViewById(R.id.textMovieExtra);
            trailer1 = (Button) rootView.findViewById(R.id.trailer1);
            trailer2 = (Button) rootView.findViewById(R.id.trailer2);
            database = new MovieDbHelper(getContext());
            Cursor movieCursor = getContext().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{temp.id},
                    null);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageMoviePoster);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
            Picasso.with(getActivity()).load(temp.poster).into(imageView);
            TextView title = (TextView) rootView.findViewById(R.id.textMovieTitle);
            TextView rating = (TextView) rootView.findViewById(R.id.textMovieRating);
            TextView release = (TextView) rootView.findViewById(R.id.textMovieRelease);
            TextView synopsis = (TextView) rootView.findViewById(R.id.textMovieSynopsis);
            final Button fav = (Button) rootView.findViewById(R.id.favButton);
            if (movieCursor.moveToFirst()) {
                temp.isFav = true;
                if (connectedToNetwork)
                    new FetchMovieExtraTask().execute(temp);
                else {
                    setVisibilitiesGone();
                    title.setText(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME)));
                    String ratingText = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING)) + getString(R.string.rating_out_of);
                    rating.setText(ratingText);
                    temp.blobData = movieCursor.getBlob(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_BLOB));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(temp.blobData, 0, temp.blobData.length);
                    imageView.setImageBitmap(bitmap);
                    release.setText(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                    synopsis.setText(movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS)));
                }
            } else {
                new FetchMovieExtraTask().execute(temp);
            }

            movieCursor.close();
            fav.setText(mContext.getString(R.string.set_fav));
            if (!temp.isFav)
                fav.setText(mContext.getString(R.string.set_fav));
            else
                fav.setText(mContext.getString(R.string.remove_fav));
            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!temp.isFav) {
                        final ContentValues values = new ContentValues();
                        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, temp.id);
                        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_NAME, temp.title);
                        values.put(MovieContract.MovieEntry.COLUMN_POSTER, temp.poster);
                        values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, temp.synopsis);
                        values.put(MovieContract.MovieEntry.COLUMN_USER_RATING, temp.user_rating);
                        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, temp.release_date);
                        Picasso.with(getActivity())
                                .load(temp.poster)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                                        temp.blobData = outputStream.toByteArray();
                                        values.put(MovieContract.MovieEntry.COLUMN_POSTER_BLOB, outputStream.toByteArray());
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                        getContext().getContentResolver().insert(
                                MovieContract.MovieEntry.CONTENT_URI,
                                values);
                        fav.setText(mContext.getString(R.string.remove_fav));
                        Toast.makeText(getContext(), "Movie Added to favourites", Toast.LENGTH_SHORT).show();
                    } else {
                        getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", new String[]{temp.id});
                        Toast.makeText(getContext(), "Movie removed from favourites", Toast.LENGTH_SHORT).show();
                        fav.setText(mContext.getString(R.string.set_fav));
                        if (MainActivity.twoPane)
                        MainActivityFragment.movieAdapter.notifyDataSetChanged();

                    }
                    temp.isFav = !temp.isFav;
                }
            });

            title.setText(temp.title);
            String ratingText = temp.user_rating + getString(R.string.rating_out_of);
            rating.setText(ratingText);
            release.setText(temp.release_date);
            synopsis.setText(temp.synopsis);
        }

        return rootView;
    }

    public class FetchMovieExtraTask extends AsyncTask<Movie,Void,Movie> {

        @Override
        protected void onPreExecute() {
            setVisibilitiesGone();
        }


        private Movie getExtraFromJson(String[] movieJsonStr , Movie m)
                throws JSONException {

            //[0] is for reviews
            //[1] is for trailers
            final String RESULTS_LIST = "results";

            final String TRAILER = "key";
            final String NAME = "author";
            final String REVIEW = "content";

            JSONObject reviewJson = new JSONObject(movieJsonStr[0]);
            JSONObject trailerJson = new JSONObject(movieJsonStr[1]);
            JSONArray reviewrArray = reviewJson.getJSONArray(RESULTS_LIST);
            JSONArray trailerArray = trailerJson.getJSONArray(RESULTS_LIST);

            for (int i = 0 ; i<reviewrArray.length();i++) {
                JSONObject movieReview = reviewrArray.getJSONObject(i);
                String reivewerName = movieReview.getString(NAME);
                String review = movieReview.getString(REVIEW);
                m.reviewerName.add(reivewerName);
                m.reviewContent.add(review);
            }
            for (int i = 0 ; i<trailerArray.length();i++) {
                JSONObject movieTrailer = trailerArray.getJSONObject(i);
                String trailer = "https://www.youtube.com/watch?v=" + movieTrailer.getString(TRAILER);
                m.trailers.add(trailer);
            }

            return m;
        }

        @Override
        protected Movie doInBackground(Movie... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            Movie incomingList = params[0];
            // Construct the URL for the MovieDB query
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";
            final String[] REQUEST_FOR =  {"reviews","videos"};

                String[] jsonStrings = new String[2];
                for (int j = 0; j < REQUEST_FOR.length; j++) {
                    try {

                        Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                                .appendPath(incomingList.id).appendPath(REQUEST_FOR[j])
                                .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_DB_API_KEY)
                                .build();

                        URL url = new URL(builtUri.toString());

                        // Create the request to MovieDB, and open the connection
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        // Read the input stream into a String
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            movieJsonStr = null;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                            // But it does make debugging a *lot* easier if you print out the completed
                            // buffer for debugging.
                            buffer.append(line + "\n");
                        }

                        if (buffer.length() == 0) {
                            // Stream was empty.  No point in parsing.
                            movieJsonStr = null;
                        }
                        movieJsonStr = buffer.toString();
                    } catch (IOException e) {
                        Log.e("PlaceholderFragment", "Error ", e);
                        // If the code didn't successfully get the movie data, there's no point in attempting
                        // to parse it.
                        movieJsonStr = null;
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (final IOException e) {
                                Log.e("PlaceholderFragment", "Error closing stream", e);
                            }
                        }
                    }
                    jsonStrings[j] = movieJsonStr;
                    if (j == 1) {
                        try {
                            return getExtraFromJson(jsonStrings,incomingList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            // because the program will only reach here if there was a movie in the list, and if there is a movie there will be a trailer for it.
            return null;
        }

        @Override
        protected void onPostExecute(Movie result) {

            if (result.trailers.size()==1){
                trailer1.setText(mContext.getString(R.string.trailer1));
                trailer1.setVisibility(View.VISIBLE);
                trailer2.setVisibility(View.GONE);
            }
            if (result.trailers.size()>1){
                trailer2.setVisibility(View.VISIBLE);
                trailer1.setVisibility(View.VISIBLE);
                trailer1.setText(mContext.getString(R.string.trailer1));
                trailer2.setText(mContext.getString(R.string.trailer2));
            }

            trailer1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp.trailers.get(0)));
                    startActivity(trailerIntent);
                }
            });

            trailer2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(temp.trailers.get(1)));
                    startActivity(trailerIntent);
                }
            });


            if (result.trailers.size()==0)
                shareTrailer = null;
            else {
                shareTrailer = result.trailers.get(0);
                setHasOptionsMenu(true);
            }

            review.setVisibility(View.VISIBLE);
            if (result.reviewerName.isEmpty())
                review.setText(mContext.getString(R.string.no_reviews_found));
            else {
                // references :
                //http://stackoverflow.com/questions/4623508/how-to-set-the-font-style-to-bold-italic-and-underlined-in-an-android-textview
                String reviewString = "<b><u>REVIEWS:</b></u><br><br>";
                for (int i = 0; i<temp.reviewerName.size();i++)
                    reviewString = reviewString + "<u><b>" + temp.reviewerName.get(i) + "</b></u>" + "<br>" + temp.reviewContent.get(i) + "<br><br>";
                review.setText(Html.fromHtml(reviewString));
            }
        }
    }

    public void setVisibilitiesGone(){
        review.setVisibility(View.GONE);
        trailer1.setVisibility(View.GONE);
        trailer2.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_movie_details, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_trailer) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareSubject = getString(R.string.sharing_trailer_info);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareTrailer);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
//            Toast.makeText(getContext(), "daba pencho", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
