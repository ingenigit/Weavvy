package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;

public class AddressActivity extends AppCompatActivity {

    Context mContext;
    AppEnv gAppEnv;
    GoogleMap mMap;
    private final int REQUEST_CHECK = 101;
    int LOCATION_REFRESH_TIME = 0; // 15 seconds to update
    int LOCATION_REFRESH_DISTANCE = 0; // 500 meters to update
    LocationManager locationManager;
    TextView textViewS, textViewM, textViewC;
    Boolean gps, network;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (gAppEnv.gAppSettings.getAddressPage())
            startActivity(new Intent(AddressActivity.this, MainActivity.class));
        //fused location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        textViewS = (TextView) findViewById(R.id.tv_skip);
        textViewM = (TextView) findViewById(R.id.tv_addressM);
        textViewC = (TextView) findViewById(R.id.tv_addressC);
        //click
        textViewS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddressActivity.this, MainActivity.class));
            }
        });
        textViewM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddressActivity.this, EditAddressActivity.class));
            }
        });
        textViewC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permission
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddressActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK);
                } else {
                    getCurrentLocation();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CHECK:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getCurrentLocation();
                }else{
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddressActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK);
        }
        else{
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        gAppEnv.gAppSettings.setAddressPage(true);
                        gAppEnv.gAppSettings.setGeoAddress(location.getLatitude() + "," + location.getLongitude());
                        startActivity(new Intent(AddressActivity.this, MainActivity.class));
                    }
                }
            });
        }
    }

//    private final LocationListener mLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(@NonNull Location location) {
//            if (location != null){
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                Toast.makeText(mContext, "gps:" + latLng, Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
}