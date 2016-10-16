package com.aki.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.aki.popularmovies.data.MovieContract;
import com.aki.popularmovies.data.MovieDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    static MovieAdapter movieAdapter;
    ArrayList<Movie> movieArrayList = new ArrayList<>();
    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            movieArrayList = savedInstanceState.getParcelableArrayList("movies");
        }
        super.onCreate(savedInstanceState);
    }


    public interface Callback{
        void onItemClicked(Movie m);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridView);
        movieAdapter = new MovieAdapter(getActivity(),movieArrayList);
        gridview.setAdapter(movieAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie m = (Movie) movieAdapter.getItem(position);
                ((Callback) getActivity()).onItemClicked(m);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        updateMovies();
        super.onStart();
    }

    public void updateMovies(){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sorting = sharedPreferences.getString(getString(R.string.pref_sorting_order_key), getString(R.string.pref_sorting_default));
            movieArrayList.clear();
            new FetchMovieTask().execute(sorting);
    }

    public class FetchMovieTask extends AsyncTask<String,Void,Movie[]> {

        boolean askedForFavs = false;
        private Movie[] getFavMovies(){
            askedForFavs = true;
            MovieDbHelper database = new MovieDbHelper(getContext());
            Cursor movieCursor = getContext().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            int count  = movieCursor.getCount();
            Movie[] movies;
            if (count < 1)
                movies = null;
            else {
                movies = new Movie[count];
                int i = 0;
                while (movieCursor.moveToNext()){
                    movies[i] = new Movie();
                    movies[i].title = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
                    movies[i].poster = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER));
                    movies[i].blobData = movieCursor.getBlob(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_BLOB));
                    movies[i].user_rating = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING));
                    movies[i].release_date = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
                    movies[i].synopsis = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS));
                    movies[i].id = movieCursor.getString(movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                    movies[i].isFav = true;
                    ++i;
                }
            }
            database.close();
            movieCursor.close();
            return movies;
        }

        private Movie[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {
            askedForFavs = false;

            if (movieJsonStr == null)
                return null;
            // These are the names of the JSON objects that need to be extracted.
            final String RESULTS_LIST = "results";

            final String TITLE = "title";
            final String POSTER = "poster_path";
            final String SYNOPSIS = "overview";
            final String USER_RATING = "vote_average";
            final String RELEASE_DATE = "release_date";
            final String MOVIE_ID = "id";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(RESULTS_LIST);
            Movie[] movies = new Movie[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                movies[i] = new Movie();
                JSONObject movieInfo = movieArray.getJSONObject(i);
                movies[i].title = movieInfo.getString(TITLE);
                movies[i].poster = movies[i].poster+ movieInfo.getString(POSTER);
                movies[i].synopsis = movieInfo.getString(SYNOPSIS);
                movies[i].user_rating = movieInfo.getString(USER_RATING);
                movies[i].release_date = movieInfo.getString(RELEASE_DATE);
                movies[i].id = movieInfo.getString(MOVIE_ID);
            }
            return movies;
        }

        @Override
        protected Movie[] doInBackground(String... params) {

            final String SORT_BY = params[0];
            if(SORT_BY.equals("favourites"))
            {
                return getFavMovies();
            }
            else {
                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String movieJsonStr = null;

                try {
                    // Construct the URL for the MovieDB query

                    final String MOVIE_BASE_URL =
                            "https://api.themoviedb.org/3/movie/";
                    final String APPID_PARAM = "api_key";
                    Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendPath(SORT_BY)
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
                try {
                    return getMovieDataFromJson(movieJsonStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Movie[] list) {
            if (list == null){
                if (!askedForFavs)
                Toast.makeText(getActivity(), "Failed to get data from the server!", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(getActivity(), "No movies added as favourites!", Toast.LENGTH_SHORT).show();
                    movieAdapter.notifyDataSetChanged();
                }
            }
            else {
                    //movieArrayList.add(list[i]);
                    Collections.addAll(movieArrayList, list);
                    movieAdapter.notifyDataSetChanged();
                if (MainActivity.twoPane)
                    ((Callback) getActivity()).onItemClicked((Movie) movieAdapter.getItem(0));
            }
        }

    }
}
