package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.or2go.core.DeliveryAddrInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;

import java.io.IOException;
import java.util.List;

public class EditAddressActivity extends AppCompatActivity {

    Context mContext;
    AppEnv gAppEnv;
    Toolbar mToolbar;
    String sName;
    DeliveryAddrInfo editAddrInfo;
    private EditText edAddr;
    private EditText edName;
    private EditText edhouseno;
    private EditText edandmark;
    private EditText edLocality;
    private EditText edZipCode;
    private EditText edContactMob;
    private EditText edContactName;
    private EditText edPlace;
    AppCompatCheckBox chkContact;
    BottomNavigationView btNavigation;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 35;
    TextInputLayout layoutAddress, layoutHome, layoutLocality, layoutPlace, layoutPin;
    String selectedAddress, selectedLocationAddress, selectedLocationLocality, selectedLocationCity, selectedLocationZipCode;
    Double lati, longi;
    TextView geolocation;
    boolean GPS;
    String oldAddress;
    String sCaller, addressName, house_no, land_mark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();

        mToolbar = (Toolbar)  findViewById(R.id.newadddr_toolbar);
        mToolbar.setTitle("Edit Delivery Address");
        sName = getIntent().getStringExtra("addrname");
        editAddrInfo = gAppEnv.getDeliveryManager().getAddrInfo(sName);
        //pick location
        sCaller = getIntent().getStringExtra("caller");
        addressName = getIntent().getStringExtra("IDName");
        house_no = getIntent().getStringExtra("house_no");
        land_mark = getIntent().getStringExtra("land_mark");
        selectedAddress = getIntent().getStringExtra("address");
        selectedLocationAddress = getIntent().getStringExtra("myaddress");
        selectedLocationLocality = getIntent().getStringExtra("mylocality");
        selectedLocationCity = getIntent().getStringExtra("mycity");
        selectedLocationZipCode = getIntent().getStringExtra("myzipcode");
        lati = getIntent().getDoubleExtra("mylatitude", 0.0);
        longi = getIntent().getDoubleExtra("mylongitute", 0.0);

        edName = (EditText) findViewById(R.id.edaddrname);
        edAddr = (EditText) findViewById(R.id.edaddr);
        edhouseno = (EditText) findViewById(R.id.edhouseno);
        edandmark = (EditText) findViewById(R.id.edlandmark);
        edLocality = (EditText) findViewById(R.id.edlocality);
        edContactName = (EditText) findViewById(R.id.edcontactname);
        edContactMob = (EditText) findViewById(R.id.edcontactmob);
        edZipCode = (EditText) findViewById(R.id.edzipcode);
        layoutAddress = (TextInputLayout) findViewById(R.id.lladdraddress);
        layoutHome = (TextInputLayout) findViewById(R.id.textinputlayouthouseno);
        layoutLocality = (TextInputLayout) findViewById(R.id.lladdrlandmark);
        layoutPlace = (TextInputLayout) findViewById(R.id.lladdrplacect);
        layoutPin = (TextInputLayout) findViewById(R.id.lladdrpin);
        edPlace= (EditText) findViewById(R.id.edotheraddr);
        chkContact = (AppCompatCheckBox) findViewById(R.id.chkProfileContact);
        geolocation = (TextView) findViewById(R.id.geoLocation);

        if (selectedLocationAddress == null){
            edName.setText(editAddrInfo.getAddrName());
            edPlace.setText(editAddrInfo.getPlace());
            geolocation.setText(editAddrInfo.geoposition);
            oldAddress = editAddrInfo.getAddress();
            String[] Addressparts = oldAddress.split(", ");
            String subAddress = oldAddress
                    .replace(Addressparts[0] + ", ", "")
                    .replace(", " + editAddrInfo.getLocality(), "")
                    .replace(", " + editAddrInfo.getZipCode(), "")
                    .replace(", " + editAddrInfo.getPlace(), "")
                    .replace(", India", "");
            edAddr.setText(subAddress);
            edhouseno.setText(Addressparts[0]);
            edandmark.setText(editAddrInfo.getLandmark());
            edLocality.setText(editAddrInfo.getLocality());
            edZipCode.setText(editAddrInfo.getZipCode());
        }
        else{
            edName.setEnabled(false);
            edhouseno.setText(house_no);
            edandmark.setText(land_mark);
            edName.setText(addressName);
            edAddr.setText(selectedLocationAddress);
            edLocality.setText(selectedLocationLocality);
            edPlace.setText(selectedLocationCity);
            edZipCode.setText(selectedLocationZipCode);
            geolocation.setText(String.valueOf(lati) + "," + String.valueOf(longi));
        }

        String contactstr = editAddrInfo.getAltcontact();
        if (contactstr.isEmpty()) {
            edContactName.setText("");
            edContactMob.setText("");
        }
        else {
            int idx = contactstr.indexOf(":");
            if (idx >0) {
                String s1 = contactstr.substring(0, idx);
                String s2 = contactstr.substring(idx + 1);
                chkContact.setChecked(true);
                edContactName.setText(s1);
                edContactMob.setText(s2);
            }
            else {
                edContactName.setText("");
                edContactMob.setText("");
            }
        }
        edName.setEnabled(false);
        edName.setHint("Address Identification Name");

        chkContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    edContactName.setText(gAppEnv.gAppSettings.getUserName());
                    edContactMob.setText(gAppEnv.gAppSettings.getUserId());
                }
                else
                {
                    edContactName.setText("");
                    edContactMob.setText("");
                }
            }
        });
        //Bottom Navigation
        btNavigation = (BottomNavigationView) findViewById(R.id.navigation_profile);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        edAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                layoutAddress.setErrorEnabled(false);
                layoutAddress.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        edhouseno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                layoutHome.setErrorEnabled(false);
                layoutHome.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        edLocality.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                layoutLocality.setErrorEnabled(false);
                layoutLocality.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        edPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                layoutPlace.setErrorEnabled(false);
                layoutPlace.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        edZipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                layoutPin.setErrorEnabled(false);
                layoutPin.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.addrnavi_home:
                    startActivity(new Intent(EditAddressActivity.this, MainActivity.class));
                    return true;
                case R.id.addrnavi_save:
                    saveAddress();
                    return true;
                case R.id.addrnavi_geoloc:
                    statusCheck();
                    if (GPS) {
                        //check permission
                        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Rect displayRectangle = new Rect();
                            Window window = EditAddressActivity.this.getWindow();
                            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                            final AlertDialog.Builder builder = new AlertDialog.Builder(EditAddressActivity.this);
                            ViewGroup viewGroup = findViewById(android.R.id.content);
                            View dialogView = LayoutInflater.from(EditAddressActivity.this).inflate(R.layout.dialog_before_loc_permission, viewGroup, false);
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
                                }
                            });
                            alertDialog.show();
                        }
                        else if (edAddr.getText().length() > 5) {
                            Intent intent = new Intent(EditAddressActivity.this, LocationPickerActivity.class);
                            intent.putExtra("address", oldAddress);
                            intent.putExtra("idname", edName.getText().toString());
                            intent.putExtra("caller", "editAddress");
                            intent.putExtra("houseNO", edhouseno.getText().toString());
                            intent.putExtra("landMark", edandmark.getText().toString());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(EditAddressActivity.this, LocationPickerActivity.class);
                            intent.putExtra("idname", edName.getText().toString());
                            intent.putExtra("caller", "editAddress");
                            intent.putExtra("houseNO", edhouseno.getText().toString());
                            intent.putExtra("landMark", edandmark.getText().toString());
                            startActivity(intent);
                        }
                    }
                    return true;
            }
            return false;
        }
    };

    private void askPermission() {
        ActivityCompat.requestPermissions(EditAddressActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (edAddr.getText().length() > 15) {
                            Intent intent = new Intent(EditAddressActivity.this, LocationPickerActivity.class);
                            intent.putExtra("address", oldAddress);
                            intent.putExtra("idname", edName.getText().toString());
                            intent.putExtra("caller", "editAddress");
                            intent.putExtra("houseNO", edhouseno.getText().toString());
                            intent.putExtra("landMark", edandmark.getText().toString());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(EditAddressActivity.this, LocationPickerActivity.class);
                            intent.putExtra("idname", edName.getText().toString());
                            intent.putExtra("caller", "editAddress");
                            intent.putExtra("houseNO", edhouseno.getText().toString());
                            intent.putExtra("landMark", edandmark.getText().toString());
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Please grant permission to continue", Toast.LENGTH_LONG).show();
                    }

                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void statusCheck() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GPS = false;
            buildAlertMessageNoGps();
        }else
            GPS = true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("To pick address from Google Map you have to turn your location \"ON\".")
                .setTitle("Turn location \"ON\"")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveAddress() {
        String addrname = edName.getText().toString().trim();
        String addr = edAddr.getText().toString().trim();
        String houseno = edhouseno.getText().toString().trim();
        String landmark = edandmark.getText().toString().trim();
        String locality = edLocality.getText().toString().trim();
        String zipcode = edZipCode.getText().toString().trim();
        String geoloc = geolocation.getText().toString().trim();
        String altcontact = edContactName.getText().toString().trim()+":"+edContactMob.getText().toString().trim();
        String place= edPlace.getText().toString().trim();;
        boolean endsWithComma = addr.endsWith(",");

        if (addr.isEmpty()) {
            layoutAddress.setError("Empty Address!");
            return;
        }
        if (houseno.isEmpty()) {
            layoutHome.setError("Empty Home No!");
            return;
        }
        if (locality.isEmpty()) {
            layoutLocality.setError("Empty Locality!");
            return;
        }
        if (place.isEmpty()) {
            layoutPlace.setError("Empty City!");
            return;
        }
        if (zipcode.isEmpty() || zipcode.length() < 6) {
            layoutPin.setError("Pin Code Error!");
            return;
        }
        if (edContactName.getText().toString().isEmpty() || edContactMob.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter receiver name and mobile no.", Toast.LENGTH_LONG).show();
            return;
        }
        if (endsWithComma) {
            int lastIndex = addr.lastIndexOf(",");
            addr = addr.substring(0, lastIndex);
        }
        else {
            String address = houseno + ", " + addr;
            address = address + ", " + locality + ", " + place + ", " + zipcode + ", India";
            if (selectedAddress == null){
                selectedAddress = address;
            }
            LatLng latLng = getAddressLL(selectedAddress);
            geolocation.setText(String.valueOf(latLng.latitude)+","+String.valueOf(latLng.longitude));
            String geoloction = geolocation.getText().toString().trim();
            gAppEnv.getDeliveryManager().updateDeliveryAddr(addrname, address, place, locality, "", landmark, zipcode, altcontact, geoloction);
            Toast.makeText(getApplicationContext(), "Address saved.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(EditAddressActivity.this,UserProfileActivity.class));
        }
    }
    private LatLng getAddressLL(String svalue) {
        Geocoder coder = new Geocoder(EditAddressActivity.this);
        List<Address> address;
        LatLng p1 = null;
        try{
            address = coder.getFromLocationName(svalue,1);
            if (address == null){
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            p1 = new LatLng((double)(location.getLatitude()), (double)(location.getLongitude()));
            return p1;
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}