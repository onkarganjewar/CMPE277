package com.project.example.cafelocator;

import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Onkar on 7/24/2016.
 */
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.MyViewHolder> {

    private List<LocationDAO> locationList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView address, rating, name;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            address = (TextView) view.findViewById(R.id.address);
            rating = (TextView) view.findViewById(R.id.rating);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LocationAdapter(List<LocationDAO> myDataset) {
        locationList = myDataset;
    }


    @Override
    public LocationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        LocationDAO movie = locationList.get(position);
//        holder.name.setText(movie.getName());
//        holder.address.setText(movie.getAddress());
//        holder.rating.setText((int) movie.getRating());
        holder.name.setText(movie.getName());
        holder.address.setText(movie.getAddress());
        holder.rating.setText(String.valueOf(movie.getRating()) );
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }
}
