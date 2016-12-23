package com.nyc.cloud.park;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nyc.cloud.park.View.MaterialProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.nyc.cloud.park.StartActivity.spreference;

public class MapsActivity extends AppCompatActivity implements RoutingListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback, LocationListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnInfoWindowClickListener {

    public static final String MyPREFERENCES = "Quick Park" ;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 2;

    protected GoogleMap map;
    protected LatLng start;
    protected LatLng end;
    @InjectView(R.id.destination)
    AutoCompleteTextView destination;
    TextView name;
    TextView emailText;

    FloatingActionButton checkProfile;
    FloatingActionButton searchParking;
    FloatingActionButton myLocation;
    FloatingActionButton logout;
    FloatingActionButton parkHere;
    FloatingActionButton unpark;

    CoordinatorLayout coordinatorLayout;

    Location currentLocation;
    Boolean first = true, endSet = false;

    private boolean mPermissionDenied = false;
    LocationManager locationManager;
    public MaterialProgressBar progressBar;
    DrawerLayout drawer;
    BroadcastReceiver receiver;

    private static final String LOG_TAG = "MyActivity";
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutoCompleteAdapter mAdapter;
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};

    private String token;
    public static SharedPreferences spreference;
    private ArrayList<Marker> currentMarkers;

    private static final LatLngBounds BOUNDS_JAMAICA = new LatLngBounds(new LatLng(-57.965341647205726, 144.9987719580531),
            new LatLng(72.77492067739843, -9.998857788741589));

    /**
     * This activity loads a map and then displays the route and pushpins on it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_bar);
        ButterKnife.inject(this);
        spreference = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        token = spreference.getString(getApplicationContext().getString(R.string.token), "");
        String FCMtoken = spreference.getString("FCMToken", "");
        if(FCMtoken.equals("")){
            /*Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);*/
            FCMtoken = FirebaseInstanceId.getInstance().getToken();
            SharedPreferences.Editor edit = spreference.edit();
            edit.putString("FCMToken",FCMtoken);
            edit.commit();
        }
        Log.d("GCM token",FCMtoken);

        progressBar = (MaterialProgressBar)findViewById(R.id.mapProgressbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        checkProfile = (FloatingActionButton) findViewById(R.id.checkProfile);
        checkProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        searchParking = (FloatingActionButton) findViewById(R.id.getParking);
        searchParking.setOnClickListener(this);
        myLocation = (FloatingActionButton) findViewById(R.id.myLocation);
        myLocation.setOnClickListener(this);
        logout = (FloatingActionButton) findViewById(R.id.logout);
        logout.setOnClickListener(this);
        parkHere = (FloatingActionButton) findViewById(R.id.park_here);
        parkHere.setOnClickListener(this);
        unpark = (FloatingActionButton) findViewById(R.id.unpark);
        unpark.setOnClickListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        polylines = new ArrayList<>();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        MapsInitializer.initialize(this);
        mGoogleApiClient.connect();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mAdapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_JAMAICA, null);

        currentMarkers = new ArrayList<>();
        /*
        * Adds auto complete adapter to both auto complete
        * text views.
        * */
        destination.setAdapter(mAdapter);

        name = (TextView) findViewById(R.id.personName);
        name.setText(spreference.getString(getApplicationContext().getString(R.string.fullName), ""));

        emailText = (TextView) findViewById(R.id.personEmail);
        emailText.setText(spreference.getString(getApplicationContext().getString(R.string.email), ""));

        destination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(destination.getWindowToken(), 0);
                    final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
                    final String placeId = String.valueOf(item.placeId);
                    destination.setText(item.description);

                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, placeId);
                    placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (!places.getStatus().isSuccess()) {
                                // Request did not complete successfully
                                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                                places.release();
                                return;
                            }
                            // Get the Place object from the buffer.
                            final Place place = places.get(0);

                            end = place.getLatLng();
                            getParkings(false);
                        }
                    });
                }catch (Exception ex){
                    Toast.makeText(getApplicationContext(),"Please connect to internet or choose address from the list", Toast.LENGTH_LONG).show();
                }

            }
        });

        /*
        These text watchers set the start and end points to null because once there's
        * a change after a value has been selected from the dropdown
        * then the value has to reselected from dropdown to get
        * the correct location.
        * */

        destination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (end != null) {
                    end = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button changePwd = (Button) findViewById(R.id.changePassword);
        changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, ChangePwdActivity.class);
                startActivity(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction("PARKING_DATA_FROM_SERVER");
        receiver = new DataReceiver();
        registerReceiver(receiver, filter);

        toggleParkUnpark();

    }

    private void toggleParkUnpark() {
        Boolean p = spreference.getBoolean(getApplicationContext().getString(R.string.parked), false);
        if (p) {
            unpark.setVisibility(View.VISIBLE);
            parkHere.setVisibility(View.GONE);
        }else{
            unpark.setVisibility(View.GONE);
            parkHere.setVisibility(View.VISIBLE);
        }
    }

    public void sendRequest() {
        if (Util.Operations.isOnline(this)) {
            route();
        } else {
            Toast.makeText(this, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }
    }

    public void route() {
        if (end == null) {
            if (destination.getText().length() > 0) {
                destination.setError("Choose location from dropdown.");
            } else {
                Toast.makeText(this, "Please choose a destination.", Toast.LENGTH_SHORT).show();
            }

        } else {
            progressDialog = ProgressDialog.show(this, "Please wait.",
                    "Fetching route information.", true);
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(start, end)
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed
        progressDialog.dismiss();
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
    }

    @Override
    public void onRoutingSuccess(List<Route> route, int shortestRouteIndex) {
        progressDialog.dismiss();
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        map.moveCamera(center);


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        int i = 0;
        int colorIndex = i % COLORS.length;

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10 + i * 3);
        polyOptions.addAll(route.get(i).getPoints());
        Polyline polyline = map.addPolyline(polyOptions);
        polylines.add(polyline);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Success! Distance: " + route.get(i).getDistanceValue() + ": Duration: " + route.get(i).getDurationValue(), Snackbar.LENGTH_LONG)
                .setAction("HIDE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setVisibility(View.GONE);
                    }
                }).setActionTextColor(getApplicationContext().getResources().getColor(R.color.primary_dark));

        snackbar.getView().setBackgroundColor(Color.WHITE);
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getApplicationContext().getResources().getColor(R.color.primary_dark));
        snackbar.show();

    }

    @Override
    public void onRoutingCancelled() {
        Log.i(LOG_TAG, "Routing was cancelled.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.v(LOG_TAG, connectionResult.toString());
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMyLocationButtonClickListener(this);
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.setOnInfoWindowClickListener(this);
        enableMyLocation();

/*
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(40.72059136642329, -73.99021625518799));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        map.moveCamera(center);
        map.animateCamera(zoom);*/
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, 1,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (map != null) {
            // Access to the location has been granted to the app.
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 1) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0,this);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (first) {
            getParkings(true);
            first = false;
            map.setMaxZoomPreference(21);
            setZoom(currentLocation);
        }
        start = new LatLng(location.getLatitude(), location.getLongitude());

    }

    void setZoom(Location loc) {
        LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());// This methods gets the users current longitude and latitude.
        map.moveCamera(CameraUpdateFactory.newLatLng(latlng));//Moves the camera to users current longitude and latitude
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, (float) 18));//Animates camera and zooms to preferred state on the user's current location.
    }

    void getParkings(final Boolean flag) {

        progressBar.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();

        try {

            if (flag) {

                if(currentLocation == null){
                    return;
                }

                obj.put("lat", currentLocation.getLatitude());
                obj.put("lng", currentLocation.getLongitude());
                setZoom(currentLocation);
                destination.setText("");

            } else {
                if(end == null){
                    return;
                }
                obj.put("lat", end.latitude);
                obj.put("lng", end.longitude);
            }

            obj.put("token", token);
            String GCMToken = spreference.getString("FCMToken","");
            if(GCMToken.equals("")){
                GCMToken = FirebaseInstanceId.getInstance().getToken();
            }
            obj.put("gcm_token", GCMToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v("searchParking",obj.toString());
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                IpConfig.SERVER + "/get_parking_locations_kafka/", obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    //progressBar.setVisibility(View.GONE);
                    String array = response.getString("success");
                    if (array.equals("true")) {/*
                        currentMarkers.clear();
                        map.clear();
                        JSONArray parkings = response.getJSONArray("parking_spots");
                        for(int i=0;i<parkings.length();i++){
                            JSONArray innerParking = parkings.getJSONArray(i);
                            double latitude = Double.parseDouble(innerParking.getString(0));
                            double longitude = Double.parseDouble(innerParking.getString(1));
                            LatLng tempLtlg = new LatLng(latitude,longitude);
                            Marker marker;
                            int no_parkings = Integer.parseInt(innerParking.getString(2));
                            if(no_parkings>=10) {
                                marker = map.addMarker(new MarkerOptions()
                                        .position(tempLtlg)
                                        .snippet(innerParking.getString(2))
                                        .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_med_parking, innerParking.getString(2))))
                                        .title("Parkings Available"));
                            }else{
                                marker = map.addMarker(new MarkerOptions()
                                        .position(tempLtlg)
                                        .snippet(innerParking.getString(2))
                                        .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_parkings, innerParking.getString(2))))
                                        .title("Parkings Available"));
                            }
                            currentMarkers.add(marker);
                        }
                        Location temp = new Location(LocationManager.GPS_PROVIDER);
                        temp.setLatitude(end.latitude);
                        temp.setLongitude(end.longitude);
                        if(!flag){
                            setZoom(temp);
                        }
                        if(spreference.getBoolean(getApplicationContext().getString(R.string.parked), false)){
                            unpark.setVisibility(View.VISIBLE);
                        }else if(parkings.length()>0){
                            unpark.setVisibility(View.GONE);
                            parkHere.setVisibility(View.VISIBLE);
                        }*/
                    } else {
                        JSONArray errorMessage = response.getJSONArray("error_list");
                        Toast.makeText(getApplicationContext(),errorMessage.get(0).toString(),Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    com.nyc.cloud.park.common.logger.Log.v("response error:", "" + e.toString());
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                try {
                    VolleyLog.d(volleyError.getMessage(), "Error: " + volleyError.getMessage());
                }catch (Exception exc){

                }
            }
        });
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjReq);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.getParking:
                getParkings(true);
                break;
            case R.id.myLocation:
                if(currentLocation != null) {
                    setZoom(currentLocation);
                }
                break;
            case R.id.logout:
                userLogout();
                break;
            case R.id.park_here:
                parkMyCar();
                break;
            case R.id.unpark:
                unparkMyCar();
                break;
        }
    }

    private void userLogout() {

        progressBar.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();

        try {

            obj.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                IpConfig.SERVER + "/logout/", obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    //progressBar.setVisibility(View.GONE);
                    String array = response.getString("success");
                    if (array.equals("true")) {
                        SharedPreferences.Editor editor = spreference.edit();
                        editor.putString(getString(R.string.token),"");
                        editor.putString(getString(R.string.fullName),"");
                        editor.putString(getString(R.string.email),"");
                        editor.putString("FCMToken","");
                        editor.putBoolean(getString(R.string.parked),false);
                        editor.commit();
                        Intent intent = new Intent(MapsActivity.this,StartActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        JSONArray errorMessage = response.getJSONArray("error_list");
                        Toast.makeText(getApplicationContext(),errorMessage.get(0).toString(),Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    com.nyc.cloud.park.common.logger.Log.v("response error:", "" + e.toString());
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                try {
                    VolleyLog.d(volleyError.getMessage(), "Error: " + volleyError.getMessage());
                }catch (Exception exc){

                }
            }
        });
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjReq);

    }

    private void parkMyCar() {
        progressBar.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();

        try {

            obj.put("lat", currentLocation.getLatitude());
            obj.put("lng", currentLocation.getLongitude());
            obj.put("token", token);
            obj.put("expected_leave_time", 120);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v("searchParking",obj.toString());
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                IpConfig.SERVER + "/park_vehicle/", obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    Log.v("Parking response",response.toString());
                    //progressBar.setVisibility(View.GONE);
                    String array = response.getString("success");
                    if (array.equals("true")) {
                        SharedPreferences.Editor editor = spreference.edit();
                        editor.putBoolean(getString(R.string.parked),true);
                        editor.commit();
                        getParkings(true);
                        toggleParkUnpark();
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Your Car is Parked successfully!", Snackbar.LENGTH_LONG)
                                .setAction("HIDE", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        view.setVisibility(View.GONE);
                                    }
                                }).setActionTextColor(getApplicationContext().getResources().getColor(R.color.primary_dark));

                        snackbar.getView().setBackgroundColor(Color.WHITE);
                        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(getApplicationContext().getResources().getColor(R.color.primary_dark));
                        snackbar.show();
                    } else {
                        JSONArray errorMessage = response.getJSONArray("error_list");
                        Toast.makeText(getApplicationContext(),errorMessage.get(0).toString(),Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    com.nyc.cloud.park.common.logger.Log.v("response error:", "" + e.toString());
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                try {
                    VolleyLog.d(volleyError.getMessage(), "Error: " + volleyError.getMessage());
                }catch (Exception exc){

                }
            }
        });
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjReq);

    }

    private void unparkMyCar() {
        progressBar.setVisibility(View.VISIBLE);
        JSONObject obj = new JSONObject();

        try {

            obj.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v("searchParking",obj.toString());
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                IpConfig.SERVER + "/unpark_vehicle_by_user/", obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    progressBar.setVisibility(View.GONE);
                    String array = response.getString("success");
                    if (array.equals("true")) {
                        SharedPreferences.Editor editor = spreference.edit();
                        editor.putBoolean(getString(R.string.parked),false);
                        editor.commit();
                        getParkings(true);
                        toggleParkUnpark();
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Your Car is unparked successfully!", Snackbar.LENGTH_LONG)
                                .setAction("HIDE", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        view.setVisibility(View.GONE);
                                    }
                                }).setActionTextColor(getApplicationContext().getResources().getColor(R.color.primary_dark));

                        snackbar.getView().setBackgroundColor(Color.WHITE);
                        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(getApplicationContext().getResources().getColor(R.color.primary_dark));
                        snackbar.show();

                    } else {
                        JSONArray errorMessage = response.getJSONArray("error_list");
                        Toast.makeText(getApplicationContext(),errorMessage.get(0).toString(),Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    com.nyc.cloud.park.common.logger.Log.v("response error:", "" + e.toString());
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                try {
                    VolleyLog.d(volleyError.getMessage(), "Error: " + volleyError.getMessage());
                }catch (Exception exc){

                }
            }
        });
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjReq);

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        end = marker.getPosition();
        sendRequest();
        currentMarkers.clear();
        map.clear();
        int no_parkings = Integer.parseInt(marker.getSnippet());
        if(no_parkings>=10) {
            marker = map.addMarker(new MarkerOptions()
                    .position(marker.getPosition())
                    .snippet(marker.getSnippet())
                    .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_med_parking, no_parkings+"")))
                    .title("Parkings Available"));
        }else{
            marker = map.addMarker(new MarkerOptions()
                    .position(marker.getPosition())
                    .snippet(marker.getSnippet())
                    .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_parkings, no_parkings+"")))
                    .title("Parkings Available"));
        }
        currentMarkers.add(marker);

    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mContents;

        CustomInfoWindowAdapter() {
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            titleUi.setText(title);

            String snippet = marker.getSnippet();
            TextView numbParks = ((TextView) view.findViewById(R.id.numbParks));
            numbParks.setText(snippet);

        }
    }

    private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getApplicationContext(), 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(getApplicationContext(), 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) + 8 ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }

    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONObject jsonObj = new JSONObject(intent.getStringExtra("PARKING_SPOTS"));

                currentMarkers.clear();
                map.clear();
                JSONArray parkings = jsonObj.getJSONArray("parking_spots");

                for(int i=0;i<parkings.length();i++){
                    JSONArray innerParking = parkings.getJSONArray(i);
                    double latitude = Double.parseDouble(innerParking.getString(0));
                    double longitude = Double.parseDouble(innerParking.getString(1));
                    LatLng tempLtlg = new LatLng(latitude,longitude);
                    Marker marker;
                    int no_parkings = Integer.parseInt(innerParking.getString(2));
                    if(no_parkings>=10) {
                        marker = map.addMarker(new MarkerOptions()
                                .position(tempLtlg)
                                .snippet(innerParking.getString(2))
                                .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_med_parking, innerParking.getString(2))))
                                .title("Parkings Available"));
                    }else{
                        marker = map.addMarker(new MarkerOptions()
                                .position(tempLtlg)
                                .snippet(innerParking.getString(2))
                                .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.mipmap.ic_parkings, innerParking.getString(2))))
                                .title("Parkings Available"));
                    }
                    currentMarkers.add(marker);
                }
                Location temp = new Location(LocationManager.GPS_PROVIDER);
                temp.setLatitude(end.latitude);
                temp.setLongitude(end.longitude);
                if(parkings.length() == 0){
                    Toast.makeText(getApplicationContext(),"Sorry no Parkings Available currently!",Toast.LENGTH_LONG).show();
                }else {
                    //if(!flag){
                    setZoom(temp);
                    //}
                }

            }catch (Exception ex){
                Log.d("Broadcast Receiver", ex.toString());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
