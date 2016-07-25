package com.project.example.cafelocator;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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

    private static List<Marker> globalMarkers = new ArrayList<>();
    private android.support.v7.widget.SearchView searchView;
    public MapView mapView;

    public GoogleMap map;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected Marker mCurrLocationMarker;
    protected LocationRequest mLocationRequest;
    protected double latitude, longitude,rating;
    private EditText searchTxt;
    private Button searchBtn;
    private Geocoder geocoder;

    private static final String TAG = "MainActivity";
    private String searchedLocation;
    private Request request;

    public ArrayList<LocationDAO> resultsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);
//        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 14);
        map.animateCamera(cameraUpdate);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        handleIntent(getIntent());
        buildGoogleApiClient();
    }

    private void getLocation(String searchedLocation) {

        List<android.location.Address> addressList = null;

        if (searchedLocation != null || !searchedLocation.equals("")) {
            geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(searchedLocation, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            android.location.Address address = addressList.get(0);
            String properAddress = String.format("%s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getCountryName());
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Searched Cafe");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mCurrLocationMarker = map.addMarker(markerOptions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 8);
            map.animateCamera(cameraUpdate);
        }
    }

    public void setMarkers(double lat, double lng, String properAddress) {
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(lat,lng);
        markerOptions.position(latLng);
        markerOptions.title(properAddress);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        Marker tempMarker = map.addMarker(markerOptions);
        globalMarkers.add(tempMarker);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 15);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
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


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            searchView= (android.support.v7.widget.SearchView) menu.findItem(R.id.search).getActionView();

            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

            searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    boolean flag = StringUtils.isBlank(s);
                    Log.d("DEBUG","Value of flag OUTSIDE"+flag);
                    if(StringUtils.isBlank(s)) {
                        Log.d("DEBUG","Value of flag INSIDE"+flag);
                        Toast.makeText(MainActivity.this, "Please enter a location", Toast.LENGTH_LONG).show();
                    }
                    removeMarkers();
                    try {
                        getLocation(s);
                        queryAPI(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
            searchView.setSubmitButtonEnabled(true);
            searchView.setIconified(true);
        }
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
        sb.append("&radius=500");
        sb.append("&types=" + "cafe");
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyCk6xfq4jFcg6Qlz5Nlhn2iUug8pndfIP8");

        Log.d("Map", "api: " + sb.toString());

        return sb;
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
        }
    }

    private void doGetRequest(String url) {
        OkHttpClient client = new OkHttpClient();
        request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                                Log.d("JSON","FAILED");
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) {
                        String name,vicinity;

                        JSONObject geometryObject, locationObject;
                        try {
                            String res = response.body().string();
                            JSONObject Jobject = new JSONObject(res);
                            JSONArray Jarray = Jobject.getJSONArray("results");
                            int limit = Jarray.length();
                            Log.d("JSON","LIMIT####"+limit);

                            for (int i = 0; i < limit; i++) {
                                JSONObject object     = Jarray.getJSONObject(i);
                                name = object.getString("name");
                                try {
                                    rating = Double.parseDouble(object.getString("rating"));
                                }catch (JSONException e) {
                                    Log.d(TAG,"Rating not found");
//                                    e.printStackTrace();
                                }
                                vicinity = object.getString("vicinity");
                                geometryObject= object.getJSONObject("geometry");
                                locationObject= geometryObject.getJSONObject("location");
                                latitude = Double.parseDouble(locationObject.getString("lat"));
                                longitude = Double.parseDouble(locationObject.getString("lng"));
//                                rating = Double.parseDouble(String.valueOf((rating)));
                                resultsList.add(new LocationDAO(name,latitude,longitude,vicinity,rating));

                                Log.d("JSON", name + " ## " + rating + "## vicinity ### "+vicinity+"### Geometry ###"+geometryObject+"LOCATION OBJECT #####"+locationObject+"Latitude#####"+latitude);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("FINALLY","Executing ResultList");
                                    displayList(resultsList);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void displayList(List<LocationDAO> resultsList) {

        for (LocationDAO g: resultsList) {
            System.out.print(g.toString() + "\n   ");
            setMarkers(g.getLatitude(),g.getLongitude(),g.getAddress());
            System.out.println(g.getAddress());
            Log.d("JSONLIST", "Address of object $$$$$$"+g.getAddress());
            System.out.println();
        }
        Log.d("JSON","Size of Markers"+globalMarkers.size());

    }

    public void sortRatings() {

        Collections.sort(resultsList, new Comparator<LocationDAO>() {
                    public int compare(LocationDAO object1, LocationDAO object2) {
                        return Double.compare(object2.rating, object1.rating);
                    }
                }
        );

        for (LocationDAO l:resultsList) {
            double r = l.getRating();
            Log.d("DEBUG","Sorted LIST###"+l.getRating());
        }
    }
    private void removeMarkers() {
        resultsList.clear();
        map.clear();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                sortRatings();
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                i.putParcelableArrayListExtra("MyObj",resultsList);
                startActivity(i);
            return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
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
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    //use the query to search your data somehow
        //            doMyIntent();
                    Log.d("DEBUG","My query $$$$$$$$ query"+query);
            }
        }
       @Override
        protected void onNewIntent(Intent intent) {
            handleIntent(intent);
    }

}
