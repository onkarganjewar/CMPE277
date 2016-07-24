package com.project.example.cafelocator;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    ArrayList<HashMap<String, String>> productsList;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected Marker mCurrLocationMarker;
    protected LocationRequest mLocationRequest;
    protected double latitude, longitude;
    private EditText searchTxt;
    private Button searchBtn;
    private Geocoder geocoder;
    private MapViewFragment mapFragment;
    private static final String TAG = "MainActivity";
    private String searchedLocation;
    private OkHttpClient client;
    private Request request;
    private Response response;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_RATING = "rating";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_NAME = "name";
    private HashMap<String, String> placesMap;
//    private static final String TAG_POSTS = "posts";
//    private static final String TAG_TIME = "time";
//    private static final String TAG_ID = "id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapFragment = (MapViewFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        searchTxt = (EditText) findViewById(R.id.searchTxt);
        searchBtn = (Button) findViewById(R.id.searchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchedLocation = searchTxt.getText().toString();

//                String temp = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=37.3525714,-121.893204&radius=500&types=cafe&key=AIzaSyCk6xfq4jFcg6Qlz5Nlhn2iUug8pndfIP8";

                try {
                    queryAPI(searchedLocation);
//                    doGetRequest(temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                onLocationSearch(searchedLocation);
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        handleIntent(getIntent());
        buildGoogleApiClient();
    }


    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            Log.d("DEBUG",query);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    private void startLocationUpdates() {
// Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
        mapFragment = (MapViewFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (mapFragment != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            mapFragment.setLocation(latitude,longitude);
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            MapViewFragment newFragment = new MapViewFragment();
            Bundle args = new Bundle();
            args.putDouble("Latitude",latitude);

            newFragment.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
        if (mLastLocation != null) {
            Log.d("CREATION",String.valueOf(mLastLocation.getLatitude()));
            Log.d("CREATION",String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        Log.d("CREATION","Latitude : "+location.getLatitude());
        Log.d("CREATION","Longitude : "+location.getLongitude());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "onQueryTextSubmit ");
                Log.d(TAG, s);

                onLocationSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "onQueryTextChange ");
                return false;
            }
        });

        return true;
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("CREATION", "Connection failed due to gps");
    }

    /*
     * Sample query
     * https://maps.googleapis.com/maps/api/place/nearbysearch/json?
     * location=37.3525714,-121.893204&radius=500&types=cafe&key=AIzaSyCk6xfq4jFcg6Qlz5Nlhn2iUug8pndfIP8
     */
    public StringBuilder getQueryString (double latitude, double longitude) {

        //use your current location here
        double mLatitude = latitude;
        double mLongitude = longitude;
        /*
        double mLatitude = 37.77657;
        double mLongitude = -122.417506;*/

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius=5000");
        sb.append("&types=" + "cafe");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCk6xfq4jFcg6Qlz5Nlhn2iUug8pndfIP8");

        Log.d("Map", "api: " + sb.toString());

        return sb;
    }

    public void onLocationSearch(String location) {

//        String location = searchTxt.getText().toString();
//        location = searchedLocation;
        Set<String> keys = placesMap.keySet();
        List<android.location.Address> addressList = null;
        for (Map.Entry<String, String> entry : placesMap.entrySet()) {
            if (entry.getValue().equals(location)) {
                System.out.println(entry.getKey());
                Log.d("JSON","ADDDRESSSSS$#######"+entry.getKey());
            }
        }
        Log.d("DEBUG","LOCATION #######"+location);
        if (location != null || !location.equals("")) {
           geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addressList != null && addressList.size()>0) {
                Log.d("JSON","ADDRESS LIST####"+addressList.toString());
                android.location.Address address = addressList.get(0);
                String properAddress = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());


// if (mapFragment != null) {
//                    Log.d("DEBUG","SET MARKERS FOR"+latLng.latitude+"####"+latLng.longitude);
//                    mapFragment.setMarkers(latLng, properAddress);
//                }
            }

        }
    }
    private void queryAPI(String searchedLocation) throws IOException {
        List<android.location.Address> addressList = null;

        if (searchedLocation != null || !searchedLocation.equals("")) {
            geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(searchedLocation, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            android.location.Address address = addressList.get(0);
            StringBuilder queryStr = getQueryString(address.getLatitude(),address.getLongitude());

            doGetRequest(String.valueOf(queryStr));


           /* OkHttpClient client = new OkHttpClient();
            String jsonData = null, name, rating;
            request = new Request.Builder().url(String.valueOf(queryStr)).build();
            try {
                response = client.newCall(request).execute();
//                response.body().string();
                jsonData = response.body().string();
                JSONObject Jobject = new JSONObject(jsonData);
                JSONArray Jarray = Jobject.getJSONArray("results");
                //get the length of the json array
                int limit = Jarray.length();

                //datastore array of size limit
                String dataStore[] = new String[limit];


                for (int i = 0; i < limit; i++) {
                    JSONObject object     = Jarray.getJSONObject(i);
                    name = object.getString("name");
                    rating = object.getString("rating");
                    Log.d("JSON DATA", name + " ## " + rating);
                    //store the data into the array
                    dataStore[i] = name + " ## " + rating;
                }
//prove that the data was stored in the array
                for (String content : dataStore) {
                    Log.d("ARRAY CONTENT", content);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
*/

        }
    }

    void doGetRequest(String url) throws IOException{
        OkHttpClient client = new OkHttpClient();


        request = new Request.Builder()
                .url(url)
                .build();


        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                                Log.d(TAG,"FAILED");
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) {
                        String name,rating=null, vicinity;
                        placesMap = new HashMap<String, String>();

                        try {
                            String res = response.body().string();
                            // Do something with the response
                            // creating new HashMap

                            JSONObject Jobject = new JSONObject(res);
                            JSONArray Jarray = Jobject.getJSONArray("results");
                            Log.d("JSON DATA","This is how array looks like ####"+Jarray);
                            JSONArray geometry ;//get the length of the json array
                            int limit = Jarray.length();
                            Log.d("JSON","LIMIT####"+limit);
                            //datastore array of size limit
//                            String dataStore[] = new String[limit];
                            for (int i = 0; i < limit; i++) {
                                JSONObject object     = Jarray.getJSONObject(i);
                                name = object.getString("name");
                                try {
                                    rating = object.getString("rating");
                                }catch (JSONException e) {
                                    Log.d(TAG,"Rating not found");
//                                    e.printStackTrace();
                                }
                                vicinity = object.getString("vicinity");


                                // adding each child node to HashMap key => value
                                placesMap.put(TAG_NAME, name);
                                placesMap.put(TAG_ADDRESS, vicinity);
                                placesMap.put(TAG_RATING, rating);
                                productsList.add(placesMap);
//                                geometry = Jobject.getJSONArray("geometry");
//                                Log.d("JSONDATA", "Geometry ####"+geometry);
//                                JSONObject o = geometry.getJSONObject(0);
//                                Log.d("JSON DATA","OOOOO $$$$$"+o);
//                                String temp = o.getString("location");
//                                Log.d("JSON DATA","temp #$#$#"+temp);
//                                String t = o.getString("lat");
//                                Log.d("JSON DATA","lat####"+t);
//
                                Log.d("JSON DATA", name + " ## " + rating + "## vicinity ### "+vicinity);
                                onLocationSearch(vicinity);
                                //store the data into the array
//                                dataStore[i] = name + " ## " + rating+ "##"+ vicinity;
                            }
//prove that the data was stored in the array
                          /*  for (String content : dataStore) {
                                Log.d("ARRAY CONTENT", content);
                            }*/

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
