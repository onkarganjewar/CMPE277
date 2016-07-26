package com.project.example.cafelocator;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // Google Map variables
    public GoogleMap map;
    public MapView mapView;
    protected GoogleApiClient mGoogleApiClient;
    private Geocoder geocoder;
    private CameraUpdate cameraUpdate;

    // Store current location only once
    private double _latitude, _longitude;


    // Variables to get current location
    protected Location mLastLocation;
    protected LocationRequest mLocationRequest;
    protected Marker mCurrLocationMarker;

    // Store information of searched cafe
    private String cafeName, cafeAddress;
    private double rating, latitude, longitude;

    // Location update intervals
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private LocationManager locationManager;

    // List to store all the markers present on the map
    private static List<Marker> globalMarkers = new ArrayList<>();

    // Searchview to search the current location
    private android.support.v7.widget.SearchView searchView;

    // Tag to use for debug
    private static final String TAG = "MAIN_ACTIVITY";

    // okhttp3 library elements --> to make a REST call
    private Request request;
    // NavigationDrawer elements
    private DrawerLayout mDrawerLayout;

    private NavigationView navigationView;

    // List to store retrieved Cafe objects
    public ArrayList<LocationDAO> resultsList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all the elements and layouts
        _init(savedInstanceState);

        // Initialize MapView
        _initMap();

        // Check if the google play services are present
        if (servicesPresent()) {

            // Check for internet connectivity
            if (isNetworkAvailable(this.getBaseContext())) {

                // Check if GPS sensor is enabled
                if(isGPSEnabled(this.getBaseContext())) {
                    navigationViewListeners();
                } else {
                    Snackbar.make(findViewById(R.id.drawer_layout), "GPS is not enabled", Snackbar.LENGTH_LONG).setAction("TURN ON", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(callGPSSettingIntent,0);
                        }
                    }).show();
                }

            } else {
                Snackbar.make(findViewById(R.id.drawer_layout), "Not able to connect to the Internet", Snackbar.LENGTH_LONG).setAction("Exit and Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.exit(1);
                    }
                }).show();
            }
        } else { // Exit the application
            Snackbar.make(findViewById(R.id.drawer_layout), "Google Play Services NOT INSTALLED!! Exiting the application ...", Snackbar.LENGTH_LONG).setAction("DISMISS", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            }).show();
            // Google play services absent; exit the application
            System.exit(1);
        }
    }

    public boolean isGPSEnabled (Context mContext){
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Check if google play services installed
    private boolean servicesPresent() {
        // Getting status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getBaseContext());
        // Showing status
        if(status==ConnectionResult.SUCCESS)
            return true;
        else
            return false;
    }



    // Initialize and build layout
    private void _init(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.mapview);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        mapView.onCreate(savedInstanceState);
        buildGoogleApiClient();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    // OnClick handlers for NavigationView Items
    private void navigationViewListeners() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_images:
                        if (globalMarkers.size() == 0) {
                            Snackbar.make(findViewById(R.id.drawer_layout), "Please search a cafe first", Snackbar.LENGTH_LONG).setAction("DISMISS", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {}
                            }).show();
                        } else {
                            // Remove markers and navigate to new activity
                            globalMarkers.clear();
                            sortRatings();
                            Intent i = new Intent(MainActivity.this, Main2Activity.class);
                            i.putParcelableArrayListExtra("MyObj", resultsList);
                            startActivity(i);
                            removeMarkers();
                        }
                        break;
                    case R.id.navigation_item_location:
                        //Place current location marker
                        LatLng latLng = new LatLng(_latitude,_longitude);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Current Location");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        mCurrLocationMarker = map.addMarker(markerOptions);
                        // Navigate camera to current location
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 17);
                        map.animateCamera(cameraUpdate);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "In default case", Toast.LENGTH_SHORT).show();
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

    }

    // Check if the network is available
    public boolean isNetworkAvailable(Context context)
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null;
        }

    // Initialize and build GoogleMap
    private void _initMap() {
        map = mapView.getMap();
        map.setMyLocationEnabled(true);
//        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Check for the version of android and request permission if lesser
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
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
            cafeName = address.getFeatureName();
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Searched Cafe");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mCurrLocationMarker = map.addMarker(markerOptions);

            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 14);
            map.animateCamera(cameraUpdate);
        }
    }

    public void setMarkers(double lat, double lng, String properAddress) {

        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(lat,lng);
        markerOptions.position(latLng);
        markerOptions.title(properAddress);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        Marker tempMarker = map.addMarker(markerOptions);
        globalMarkers.add(tempMarker);

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            _latitude = mLastLocation.getLatitude();
            _longitude = mLastLocation.getLongitude();
            Log.d(TAG,"LATITUDE VALUE"+_latitude);
            Log.d(TAG,"LONGITUDE VALUE"+_longitude);

        }


        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(_latitude, _longitude), 18);
        map.animateCamera(cameraUpdate);

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
        Log.d(TAG,"Latitude : "+location.getLatitude());
        Log.d(TAG,"Longitude : "+location.getLongitude());

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Check for android version and install searchView
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView= (android.support.v7.widget.SearchView) menu.findItem(R.id.action_websearch).getActionView();
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

            searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    // Clear the map and query the google places API
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
        Log.d(TAG, "Connection failed due to gps");
    }

    // Method to build query url for places API
    public StringBuilder getQueryString (double latitude, double longitude) {

        //use your current location here
        double mLatitude = latitude;
        double mLongitude = longitude;
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius=1500");
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

                        JSONObject geometryObject, locationObject;
                        try {
                            String res = response.body().string();
                            JSONObject Jobject = new JSONObject(res);
                            JSONArray Jarray = Jobject.getJSONArray("results");
                            int limit = Jarray.length();
                            for (int i = 0; i < limit; i++) {
                                JSONObject object     = Jarray.getJSONObject(i);
                                try {
                                    rating = Double.parseDouble(object.getString("rating"));
                                }catch (JSONException e) {
                                    Log.d(TAG,"Rating not found");
//                                    e.printStackTrace();
                                }
                                geometryObject= object.getJSONObject("geometry");
                                locationObject= geometryObject.getJSONObject("location");
                                cafeName = object.getString("name");
                                cafeAddress = object.getString("vicinity");
                                latitude = Double.parseDouble(locationObject.getString("lat"));
                                longitude = Double.parseDouble(locationObject.getString("lng"));

                                resultsList.add(new LocationDAO(cafeName,latitude,longitude,cafeAddress,rating));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Place nearby cafe pins on map
                                    placePins(resultsList);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // Place Markers on Map
    private void placePins(List<LocationDAO> resultsList) {
        for (LocationDAO g: resultsList) {
            setMarkers(g.getLatitude(),g.getLongitude(),g.getName());
            Log.d(TAG,g.getName());
        }
    }

    // Sort the searched cafes by Ratings
    public void sortRatings() {
        Collections.sort(resultsList, new Comparator<LocationDAO>() {
                    public int compare(LocationDAO object1, LocationDAO object2) {
                        return Double.compare(object2.rating, object1.rating);
                    }
                }
        );
        for (LocationDAO l:resultsList) {
            double r = l.getRating();
            Log.d(TAG,"Sorted LIST###"+l.getRating());
        }
    }

    // Remove markers and clear map
    private void removeMarkers() {
        resultsList.clear();
        map.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {  }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // Hamburger icon to toggle NavigationDrawer
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
             }
        return super.onOptionsItemSelected(item);
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
            }
        }

    // Method for searchable interface
    @Override
        protected void onNewIntent(Intent intent) {
            handleIntent(intent);
    }

}
