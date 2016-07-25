package com.project.example.cafelocator;

/**
 * Created by Onkar on 7/24/2016.
 */
public class ResultsObject {
    public double latitude;
    public double longitude;
    public String address;
    public float rating;

    public ResultsObject(double latitude, double longitude, String vicinity, String rating) {
    this.latitude = latitude;
        this.longitude = longitude;
        this.rating = Float.parseFloat(rating);
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

    public float getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
