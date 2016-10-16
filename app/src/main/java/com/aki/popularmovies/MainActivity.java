package com.aki.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback{

    public static boolean twoPane;
    String MD_FRAG_TAG = "movie_details_frag_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_fragment)!=null){
            twoPane = true;
            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_fragment, new MovieDetailsActivityFragment(), MD_FRAG_TAG).commit();
            }
        }
        else
            twoPane = false;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected)
            Toast.makeText(getApplicationContext(), "Network not available.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(Movie m) {

        if(twoPane){
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailsActivityFragment.MOVIE_ARG,m);

            MovieDetailsActivityFragment frag = new MovieDetailsActivityFragment();
            frag.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_fragment,frag).commit();
        }
        else {
            Intent intent = new Intent(getApplicationContext(),MovieDetailsActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, m);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
