package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.or2go.core.Or2GoStore;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.model.MarkerData;
import com.or2go.weavvy.R;
import com.or2go.weavvy.model.StoreList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapStoreList extends AppCompatActivity {

    Context mContext;
    AppEnv gAppEnv;
    ArrayList<StoreList> storeList;
    private final int REQUEST_CHECK_SETTINGS = 101;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    ArrayList<MarkerData> locationArrayList;
    TextView textViewStoreName, textViewStoreType, textViewStoreAddress;
    Button buttonShow;
    String StoreName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_store_list);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapes);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        textViewStoreName = (TextView) findViewById(R.id.tv_storeName);
        textViewStoreType = (TextView) findViewById(R.id.tv_storeType);
        textViewStoreAddress = (TextView) findViewById(R.id.tv_storeAddress);
        buttonShow = (Button) findViewById(R.id.btn_showStore);

        storeList = gAppEnv.getStoreManager().getAllStoreList();
        //check permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK_SETTINGS);
        } else {
            getCurrentLocation();
        }

        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!StoreName.equals("")) {
                    Or2GoStore store = gAppEnv.getStoreManager().getStoreByName(StoreName);
                    Intent intent = new Intent(MapStoreList.this, StoreProductsActivity.class);
                    intent.putExtra("vendorID", store.getId());
                    startActivity(intent);
                }
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            mMap = googleMap;
                            mMap.clear();
                            //get current location.
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                            MarkerOptions markerOptions = new MarkerOptions();
//                            markerOptions.position(latLng);
//                            markerOptions.title("Your Location");
//                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
//                            mMap.addMarker(markerOptions);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 200f);
                            mMap.animateCamera(cameraUpdate);
                            for (int i = 0; i <storeList.size(); i++){
                                setArrayData(latLng, storeList.get(i));
                            }
                        }
                    });
                }
            }
        });
    }

    private void setArrayData(LatLng latLng, StoreList storeList) {
        String customerloc = latLng.latitude + "," + latLng.longitude;
        String storeloc = storeList.getGeolocation();

        System.out.println(customerloc + "geolocation:" + storeloc );

        String url = Uri.parse("https://maps.googleapis.com/maps/api/distancematrix/json")
                .buildUpon()
                .appendQueryParameter("origins", customerloc)
                .appendQueryParameter("destinations", storeloc)
                .appendQueryParameter("sensor", "false")
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", "AIzaSyAnhTf79xLDcS0zj_cl_rjAVbx-cIBfwa8")
                .toString();
        RequestQueue requestQueue = Volley.newRequestQueue(MapStoreList.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        locationArrayList = new ArrayList<MarkerData>();
                        JSONArray jsonArray = response.getJSONArray("rows");
                        //for 0 index distance only 1 to 2
                        JSONObject jsonObject = jsonArray
                                .getJSONObject(0)
                                .getJSONArray("elements")
                                .getJSONObject(0)
                                .getJSONObject("distance");
                        String firstDistance = jsonObject.getString("text").toString();
                        String[] splited1 = firstDistance.split("\\s+");
                        String distanceValue = (String.valueOf(Double.parseDouble(splited1[0])));
                        String[] arrOfStr = storeloc.split(",");
                        locationArrayList.add(new MarkerData(Double.parseDouble(arrOfStr[0]), Double.parseDouble(arrOfStr[1]), storeList.getStringName(), storeList.getStringType(), distanceValue));
                        CustomerMarker(latLng.latitude, latLng.longitude);
                        AnotherMethod(locationArrayList);

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(@NonNull Marker marker) {
                                textViewStoreName.setText(marker.getTitle());
                                textViewStoreType.setText(marker.getSnippet());
                                StoreName = marker.getTitle();
                                getGeoAddress(marker.getPosition());
                                return false;
                            }
                        });
                    }
                } catch (JSONException e) {
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

    private Marker AnotherMethod(ArrayList<MarkerData> locationArrayList) {
        for (int j = 0; j < locationArrayList.size(); j++){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(locationArrayList.get(j).latitude, locationArrayList.get(j).longitude))
//                .anchor(0.5f, 0.5f)
                    .title(locationArrayList.get(j).title) //name of the delivery boy
                    .snippet(locationArrayList.get(j).type) //actual distance between vendor and devlivery boy. update according to dv travel.
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            marker.showInfoWindow();
            return marker;
        }
        return null;
    }

    private Marker CustomerMarker(double latitude, double longitude) {
        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
//                .anchor(0.5f, 0.5f)
                .title("Your Location") //name of the delivery boy
//                .snippet("0 km") //actual distance between vendor and devlivery boy. update according to dv travel.
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CHECK_SETTINGS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getCurrentLocation();
                }else{
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getGeoAddress(LatLng geoAddress) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(geoAddress.latitude, geoAddress.longitude, 1);
            textViewStoreAddress.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}