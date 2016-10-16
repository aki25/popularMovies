package com.aki.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class MovieAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Movie> movies = new ArrayList<>();

    public MovieAdapter(Context c, ArrayList<Movie> m) {
        mContext = c;
        movies = m;
    }

    public int getCount() {
        if (movies==null)
        return 0;
        else
            return movies.size();
    }

    public Object getItem(int position) {
        return movies.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }
        if (movies.get(position).blobData!=null) {
             Bitmap bitmap = BitmapFactory.decodeByteArray(movies.get(position).blobData, 0, movies.get(position).blobData.length);
            imageView.setImageBitmap(bitmap);
        }
        else
        Picasso.with(mContext).load(movies.get(position).poster).into(imageView);
        return imageView;
    }
}