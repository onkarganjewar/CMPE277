package com.project.example.cafelocator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private List<LocationDAO> locationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LocationAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        Intent i = getIntent();
        locationList =  i.getParcelableArrayListExtra("MyObj");

        for (LocationDAO l: locationList) {
            Double rating = l.getRating();
            Log.d("DEBUG","RECEIVED LIST"+rating);
        }
        mAdapter = new LocationAdapter(locationList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

//        prepareMovieData();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                // User chose the "Settings" item, show the app settings UI...
                System.exit(0);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    private void prepareMovieData () {
        List<LocationDAO> location = new ArrayList<>();

        for (LocationDAO l: locationList) {
         location.add(l);
        };

        mAdapter.notifyDataSetChanged();
    }
   /* private void prepareMovieData1() {

        LocationDAO movie = new LocationDAO("Mad Max: Fury Road", "Action & Adventure", "2015");
        movieList.add(movie);

        movie = new LocationDAO("Inside Out", "Animation, Kids & Family", "2015");
        movieList.add(movie);

        movie = new LocationDAO("Star Wars: Episode VII - The Force Awakens", "Action", "2015");
        movieList.add(movie);

        movie = new LocationDAO("Shaun the Sheep", "Animation", "2015");
        movieList.add(movie);

        movie = new LocationDAO("The Martian", "Science Fiction & Fantasy", "2015");
        movieList.add(movie);

        movie = new LocationDAO("Mission: Impossible Rogue Nation", "Action", "2015");
        movieList.add(movie);

        movie = new LocationDAO("Up", "Animation", "2009");
        movieList.add(movie);

        movie = new LocationDAO("Star Trek", "Science Fiction", "2009");
        movieList.add(movie);

        movie = new LocationDAO("The LEGO Movie", "Animation", "2014");
        movieList.add(movie);

        movie = new LocationDAO("Iron Man", "Action & Adventure", "2008");
        movieList.add(movie);

        movie = new LocationDAO("Aliens", "Science Fiction", "1986");
        movieList.add(movie);

        movie = new LocationDAO("Chicken Run", "Animation", "2000");
        movieList.add(movie);

        movie = new LocationDAO("Back to the Future", "Science Fiction", "1985");
        movieList.add(movie);

        movie = new LocationDAO("Raiders of the Lost Ark", "Action & Adventure", "1981");
        movieList.add(movie);

        mAdapter.notifyDataSetChanged();
    }*/
}
