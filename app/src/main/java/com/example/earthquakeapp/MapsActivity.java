package com.example.earthquakeapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.earthquakeapp.Activities.EarthQuakesListActivity;
import com.example.earthquakeapp.Model.EarthQuake;
import com.example.earthquakeapp.UI.CustomInfoWindow;
import com.example.earthquakeapp.Util.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private static GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue requestQueue;
    private AlertDialog.Builder alertdialogBuilder;
    private AlertDialog alertDialog;
    private BitmapDescriptor[] iconColors;
    private Button showListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showListButton = findViewById(R.id.id_showListBTN);

        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, EarthQuakesListActivity.class));
            }
        });

        iconColors = new BitmapDescriptor[]{
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                //BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
        };

        requestQueue = Volley.newRequestQueue(this);

        getEarthQuakes();


    }

    private void getEarthQuakes() {

        EarthQuake earthQuake = new EarthQuake();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL_API,null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray_features = response.getJSONArray("features");
                    for( int i=0;i<jsonArray_features.length();i++){

                        //Get Properties
                        JSONObject jsonObject_properties =  jsonArray_features.getJSONObject(i).getJSONObject("properties");

                        //Get geometry from Json
                        JSONObject jsonObject_geometry = jsonArray_features.getJSONObject(i).getJSONObject("geometry");

                        //Get coordinates array
                        JSONArray coordinates = jsonObject_geometry.getJSONArray("coordinates");
                        double longitude = coordinates.getDouble(0);
                        double latitude = coordinates.getDouble(1);
                        int depth = coordinates.getInt(2); //depth is given in kilometers

                        //Log.d("que",latitude+","+longitude+","+depth);
                        earthQuake.setLat(latitude);
                        earthQuake.setLon(longitude);
                        earthQuake.setPlace(jsonObject_properties.getString("place"));
                        earthQuake.setType(jsonObject_properties.getString("type"));
                        earthQuake.setTime(jsonObject_properties.getLong("time"));
                        earthQuake.setMagnitude(jsonObject_properties.getDouble("mag"));
                        earthQuake.setDetailLink(jsonObject_properties.getString("detail"));

                        java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                        String formatDate = dateFormat.format(new Date(Long.valueOf(jsonObject_properties.getLong("time"))).getTime());

                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.icon(iconColors[new Random().nextInt(iconColors.length)]);
                        markerOptions.title(earthQuake.getPlace());
                        markerOptions.position(new LatLng(latitude, longitude));
                        markerOptions.snippet("Magnitude: "+earthQuake.getMagnitude()+ "\n"+
                                "Date: "+formatDate);

                        //add a circle for marker with magnitude >4
                        if(earthQuake.getMagnitude()>=6){
                            CircleOptions circleOptions = new CircleOptions();
                            circleOptions.center(new LatLng(earthQuake.getLat(),earthQuake.getLon()));
                            circleOptions.radius(9000);
                            circleOptions.strokeWidth(4);
                            circleOptions.fillColor(Color.RED);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                            mMap.addCircle(circleOptions);
                        }
                        Marker marker = mMap.addMarker(markerOptions);
                        marker.setTag(earthQuake.getDetailLink());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),4));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        /*
        int errorcode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (errorcode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, errorcode, errorcode, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            Toast.makeText(MapsActivity.this, "No services", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
            errorDialog.show();
        }
        else {
            Toast.makeText(MapsActivity.this, "All is good", Toast.LENGTH_SHORT).show();
        }*/
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //changing the default window to custom window
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


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
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }



        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(30, 30);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,20));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getDetailsOFQuakes(marker.getTag().toString());
        //Toast.makeText(getApplicationContext(),marker.getSnippet(),Toast.LENGTH_SHORT).show();

    }

    private void getDetailsOFQuakes(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String detailsURL="X";
                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");
                    JSONArray losspager = products.getJSONArray("losspager");

                    JSONObject lossObject = losspager.getJSONObject(0);
                    JSONObject  contentObject = lossObject.getJSONObject("contents");
                    JSONObject jsonCitiesObject = contentObject.getJSONObject("json/cities.json");

                    detailsURL = jsonCitiesObject.getString("url");
                    /*
                    for( int i=0;i<losspager.length();i++){
                        JSONObject lossObject = losspager.getJSONObject(i);
                        JSONObject  contentObject = lossObject.getJSONObject("contents");
                        JSONObject jsonCitiesObject = contentObject.getJSONObject("json/cities.json");

                       detailsURL = jsonCitiesObject.getString("url");
                    }*/
                    //Toast.makeText(MapsActivity.this,"URL2"+detailsURL,Toast.LENGTH_SHORT).show();
                    getMoreDetails(detailsURL);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Sorry no more info here",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    public void getMoreDetails(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                alertdialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                View view =  getLayoutInflater().inflate(R.layout.popup, null);

                Button dismissButton = view.findViewById(R.id.id_dismissPopUp);
                Button closeButton = view.findViewById(R.id.id_closePopUp);
                TextView popupList = (TextView)view.findViewById(R.id.id_popupList);
                WebView htmlPopup =(WebView)view.findViewById(R.id.id_htmlWebView);

                StringBuilder sb = new StringBuilder();
                boolean ok =false;
                try {
                    JSONArray all_cities = response.getJSONArray("all_cities");
                    for(int i=0;i<all_cities.length();i++){
                        JSONObject all_citiesObject = all_cities.getJSONObject(i);

                        sb.append("City: "+all_citiesObject.getString("name")+
                                "\n"+"Population: "+all_citiesObject.getInt("pop")+
                                "\n"+"MMI (Modify Mercali Intensity scale)" + all_citiesObject.getString("mmi")+
                                "\n\n");
                    }

                    popupList.setText(sb);

                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    alertdialogBuilder.setView(view);
                    alertDialog = alertdialogBuilder.create();
                    alertDialog.show();
                    ok=true;

                } catch (JSONException e) {

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}