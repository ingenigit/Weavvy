package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.or2go.weavvy.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity {

    Context mContext;
    private GoogleMap mMap;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;
    String Address = "";
    String myAddress = "";
    String myCity = "";
    String myLocality = "";
    String zipCode = "";
    Double myLatitude = 0.0;
    Double myLongitude = 0.0;
    TextView local, adress;
    Button confirm;
    String storeName = "";
    String calledFrom;
    String houseNO = "", landMark = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        mContext = LocationPickerActivity.this;
        //fused location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //check permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AskforConfirmLocationAccess();
        } else {
            Intent intent = getIntent();
            String entryAddress = intent.getStringExtra("address");
            calledFrom = intent.getStringExtra("caller");
            String houseno = intent.getStringExtra("houseNO");
            String landmark = intent.getStringExtra("landMark");
            String name = intent.getStringExtra("idname");
            if (name.equals(null))
                storeName = "";
            else {
                storeName = name;
                houseNO = houseno;
                landMark = landmark;
            }
            if (entryAddress.equals("")) {
                getCurrentLocation();
            }
            else {
                LatLng latLng = getAddressLL(entryAddress);
                setAMarker(latLng.latitude, latLng.longitude);
            }
        }

        local = (TextView) findViewById(R.id.addressLocation);
        adress = (TextView) findViewById(R.id.addressAddress);
        confirm = (Button) findViewById(R.id.addressConfirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calledFrom == "editAddress"){
                    Intent intent = new Intent(LocationPickerActivity.this, EditAddressActivity.class);
                    intent.putExtra("IDName", storeName);
                    intent.putExtra("address", Address);
                    intent.putExtra("myaddress", myAddress);
                    intent.putExtra("mycity", myCity);
                    intent.putExtra("mylocality", myLocality);
                    intent.putExtra("myzipcode", zipCode);
                    intent.putExtra("mylatitude", myLatitude);
                    intent.putExtra("mylongitute", myLongitude);
                    intent.putExtra("house_no", houseNO);
                    intent.putExtra("land_mark", landMark);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(LocationPickerActivity.this, NewAddressActivity.class);
                    intent.putExtra("IDName", storeName);
                    intent.putExtra("address", Address);
                    intent.putExtra("myaddress", myAddress);
                    intent.putExtra("mycity", myCity);
                    intent.putExtra("mylocality", myLocality);
                    intent.putExtra("myzipcode", zipCode);
                    intent.putExtra("mylatitude", myLatitude);
                    intent.putExtra("mylongitute", myLongitude);
                    intent.putExtra("caller", calledFrom);
                    startActivity(intent);
                }
            }
        });
    }
    private void AskforConfirmLocationAccess() {
        Rect displayRectangle = new Rect();
        Window window = LocationPickerActivity.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LocationPickerActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_before_loc_permission, viewGroup, false);
        dialogView.setMinimumWidth((int)(displayRectangle.width() * 1f));
        dialogView.setMinimumHeight((int)(displayRectangle.height() * 1f));
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        TextView buttonOk=dialogView.findViewById(R.id.buttonAccept);
        TextView buttonCancel=dialogView.findViewById(R.id.buttonDeny);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                askPermission();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent  = new Intent(LocationPickerActivity.this, NewAddressActivity.class);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    private LatLng getAddressLL(String entryAddress) {
        Geocoder coder = new Geocoder(LocationPickerActivity.this);
        List<android.location.Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(entryAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            p1 = new LatLng((double) (location.getLatitude()), (double) (location.getLongitude()));
            return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocation();
                    } else {
                        Toast.makeText(this, "Please grant permission to continue", Toast.LENGTH_LONG).show();
                    }

                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getCurrentLocation() {
        // Initialize Location manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        setAMarker(location.getLatitude(), location.getLongitude());
                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);
                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                setAMarker(location1.getLatitude(), location1.getLongitude());
                            }
                        };
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        }else{
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
//        Task<Location> task = fusedLocationProviderClient.getLastLocation();
//        task.addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                if (location != null) {
//                    setAMarker(location.getLatitude(), location.getLongitude());
//                }
//            }
//        });
    }

    private void setAMarker(double latitude, double longitude) {
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                mMap.clear();
                if (ActivityCompat.checkSelfPermission(LocationPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                LatLng latLng = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(getAddress(latLng));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        latLng, 15f
                );
                mMap.animateCamera(cameraUpdate);
                mMap.moveCamera(cameraUpdate);
                mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        local.setText("Please wait...");
                        adress.setText("Address...");
                        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                            @Override
                            public void onCameraIdle() {
                                MarkerOptions markerOptions = new MarkerOptions().position(mMap.getCameraPosition().target).title(getAddress(mMap.getCameraPosition().target));
                                LatLng ll = markerOptions.getPosition();
                            }
                        });
                    }
                });
            }
        });
    }

    private String getAddress(LatLng target) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(target.latitude, target.longitude, 1);
            String fullAddress = finalDistance(
                    addresses.get(0).getAddressLine(0),
                    addresses.get(0).getFeatureName(),
                    addresses.get(0).getSubLocality(),
                    addresses.get(0).getLocality(),
                    addresses.get(0).getAdminArea(),
                    addresses.get(0).getPostalCode(),
                    addresses.get(0).getCountryName());
            String address = addresses.get(0).getFeatureName() + "," + addresses.get(0).getSubLocality() + "," + addresses.get(0).getLocality() + "," + addresses.get(0).getAdminArea() + "," + addresses.get(0).getPostalCode() + "," + addresses.get(0).getCountryName() ;
            String city = addresses.get(0).getLocality();
            String locality = addresses.get(0).getSubLocality();
            String zip = addresses.get(0).getPostalCode();

            String myaddress = addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
            double lon = addresses.get(0).getLongitude();
            double lng = addresses.get(0).getLatitude();
            local.setText(city);
            adress.setText(addresses.get(0).getAddressLine(0));
            Address = addresses.get(0).getAddressLine(0);
            myCity = city;
            myLocality = locality;
            zipCode = zip;
            myAddress = fullAddress;
            myLatitude = lng;
            myLongitude = lon;
            return fullAddress;
        } catch (Exception e) {
            e.printStackTrace();
            return "No Address Found";
        }
    }

    private String finalDistance(String addressLine, String featureName, String subLocality, String locality, String adminArea, String postalCode, String countryName) {
        String msg[] = addressLine.split(",");
        String new_str = "";
        int i = 1;
        for (String words : msg) {
            if (i++ == msg.length){
                if (!words.trim().equals(countryName)) {
                }
            }
            else {
                if(words.trim().equals(featureName) || words.trim().equals(adminArea + " " + postalCode) || words.trim().equals(adminArea) || words.trim().equals(locality) || words.trim().equals(subLocality)){
                }else{
                    new_str += words + ", ";
                }
            }

        }
        return new_str;
    }

}