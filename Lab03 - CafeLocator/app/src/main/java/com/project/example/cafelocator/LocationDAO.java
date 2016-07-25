package com.project.example.cafelocator;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Onkar on 7/24/2016.
 */
public class LocationDAO implements Parcelable {
    public double latitude;
    public double longitude;
    public String address,name;
    public double rating;
//    public String name,title,genre,year;

    protected LocationDAO(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
        name = in.readString();
       rating = in.readDouble();
    }

    public static final Creator<LocationDAO> CREATOR = new Creator<LocationDAO>() {
        @Override
        public LocationDAO createFromParcel(Parcel in) {
            return new LocationDAO(in);
        }

        @Override
        public LocationDAO[] newArray(int size) {
            return new LocationDAO[size];
        }
    };

    public String getName() {
        return name;
    }


/*
    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
*/

    public void setName(String name) {
        this.name = name;
    }

/*
    public LocationDAO(String title, String genre, String year) {
        this.title = title;
        this.genre = genre;
        this.year = year;
    }
*/

    public LocationDAO(String name, double latitude, double longitude, String vicinity, double rating) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.address = vicinity;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeDouble(rating);
    }
}
