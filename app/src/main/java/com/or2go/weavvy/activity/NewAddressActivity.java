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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;
import com.or2go.weavvy.ShortNoticeDialog;

import java.io.IOException;
import java.util.List;

public class NewAddressActivity extends AppCompatActivity {

    Context mContext;
    AppEnv gAppEnv;
    Toolbar mToolbar;
    String sCaller;
    private EditText edName;
    private EditText edhouseno;
    private EditText edlandmark;
    private EditText edAddr;
    private EditText edLocality;
    private EditText edOtherPlace;
    private EditText edZipCode;
    private EditText edContactName;
    private EditText edContactMob;
    AppCompatCheckBox chkContact;
    TextInputLayout llAddName;
    BottomNavigationView btNavigation;
    String storeIDName = "";
    String selectedAddress, selectedLocationAddress, selectedLocationLocality, selectedLocationCity, selectedLocationZipCode;
    Double lati, longi;
    TextView geolocation;
    BottomSheetDialog bottomSheetDialog;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 35;
    boolean GPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_address);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(NewAddressActivity.this, SplashScreen.class)); }
        mToolbar = (Toolbar)  findViewById(R.id.newadddr_toolbar);

        mToolbar.setTitle("New Delivery Address");

        sCaller = getIntent().getStringExtra("caller");
        selectedLocationAddress = getIntent().getStringExtra("myaddress");
        storeIDName = getIntent().getStringExtra("IDName");
        selectedAddress = getIntent().getStringExtra("address");
        selectedLocationLocality = getIntent().getStringExtra("mylocality");
        selectedLocationCity = getIntent().getStringExtra("mycity");
        selectedLocationZipCode = getIntent().getStringExtra("myzipcode");
        lati = getIntent().getDoubleExtra("mylatitude", 0.0);
        longi = getIntent().getDoubleExtra("mylongitute", 0.0);

        edName = (EditText) findViewById(R.id.edaddrname);
        edhouseno = (EditText) findViewById(R.id.edhouseno);
        edlandmark = (EditText) findViewById(R.id.edlandmark);
        edAddr = (EditText) findViewById(R.id.edaddr);
        edLocality = (EditText) findViewById(R.id.edlocality);
        edOtherPlace= (EditText) findViewById(R.id.edotheraddr);
        edZipCode = (EditText) findViewById(R.id.edzipcode);
        edContactName = (EditText) findViewById(R.id.edcontactname);
        edContactMob = (EditText) findViewById(R.id.edcontactmob);
        chkContact = (AppCompatCheckBox) findViewById(R.id.chkProfileContact);
        geolocation = (TextView) findViewById(R.id.geoLocation);
        llAddName = (TextInputLayout) findViewById(R.id.lladdrname);
        //This will capitalize the first letter of each word
        edName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edAddr.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edhouseno.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edlandmark.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edLocality.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edOtherPlace.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        edName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    llAddName.setHint("Address Identification Name");
                else
                    llAddName.setHint("Set a name for address. Ex. Home, Office...");
            }
        });
        chkContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edContactName.setText(gAppEnv.gAppSettings.getUserName());
                    edContactMob.setText(gAppEnv.gAppSettings.getUserId());
                }
                else {
                    edContactName.setText("");
                    edContactMob.setText("");
                }
            }
        });
        //Bottom Navigation
        btNavigation = (BottomNavigationView) findViewById(R.id.navigation_profile);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        OPenBottomSheet();
        SetAddressValue();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.addrnavi_home:
                    startActivity(new Intent(NewAddressActivity.this,MainActivity.class));
                    return true;
                case R.id.addrnavi_save:
                    saveAddress();
                    return true;
                case R.id.addrnavi_geoloc:
                    statusCheck();
                    if (GPS) {
                        if (edAddr.getText().length() > 10) {
                            Intent intent = new Intent(NewAddressActivity.this, LocationPickerActivity.class);
                            intent.putExtra("address", edAddr.getText().toString());
                            intent.putExtra("idname", edName.getText().toString());
                            intent.putExtra("caller", sCaller);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(NewAddressActivity.this, LocationPickerActivity.class);
                            intent.putExtra("address", "");
                            intent.putExtra("idname", edName.getText().toString());
                            intent.putExtra("caller", sCaller);
                            startActivity(intent);
                        }
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(NewAddressActivity.this, LocationPickerActivity.class);
                        intent.putExtra("address", "");
                        intent.putExtra("idname", "");
                        intent.putExtra("caller", sCaller);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Please grant permission to continue", Toast.LENGTH_LONG).show();
                    }
                }
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void saveAddress() {
        String addrname = edName.getText().toString().trim();
        String addr = edAddr.getText().toString().trim();
        String locality = edLocality.getText().toString().trim();
        String sublocality = "";
        String zipcode = edZipCode.getText().toString().trim();
        String houseno = edhouseno.getText().toString().trim();
        String landmark = edlandmark.getText().toString().trim();
        String altcontact = edContactName.getText().toString().trim()+":"+edContactMob.getText().toString().trim();
        String place= edOtherPlace.getText().toString().trim();
        boolean endsWithComma = addr.endsWith(",");
        if (addrname.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please set the Address Name field.", Toast.LENGTH_LONG).show();
            return;
        }
        if (addr.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please set the address field.", Toast.LENGTH_LONG).show();
            return;
        }
        if (houseno.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please add your house/flat/apartment no.", Toast.LENGTH_LONG).show();
            return;
        }
        if (endsWithComma) {
            int lastIndex = addr.lastIndexOf(",");
            addr = addr.substring(0, lastIndex);
        }
        if (locality.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Locality can't be empty....", Toast.LENGTH_LONG).show();
            return;
        }
        if (place.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter your place name?", Toast.LENGTH_LONG).show();
            return;
        }
        if (zipcode.isEmpty() || zipcode.length() < 6) {
            Toast.makeText(getApplicationContext(), "Please add delivery address zipcode.", Toast.LENGTH_LONG).show();
            return;
        }
        if (edContactName.getText().toString().isEmpty() || edContactMob.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter receiver name and mobile no.", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            String address = houseno + ", " + addr;
            address = address + ", " + locality + ", " + place + ", " + zipcode + ", India";
            LatLng latLng = getAddressLL(selectedAddress);
            geolocation.setText(String.valueOf(latLng.latitude)+","+String.valueOf(latLng.longitude));
            String geoloc = geolocation.getText().toString().trim();
            gAppEnv.getDeliveryManager().addDeliveryAddr(addrname, address, place, locality, "", landmark, zipcode, altcontact, geoloc);

            Toast.makeText(getApplicationContext(), "Address saved.", Toast.LENGTH_LONG).show();

            if ((sCaller != null) && (sCaller.equals("cart")))
                startActivity(new Intent(NewAddressActivity.this,OrderCartActivity.class));
            else
                startActivity(new Intent(NewAddressActivity.this,UserProfileActivity.class));
        }
    }
    private LatLng getAddressLL(String svalue) {
        Geocoder coder = new Geocoder(NewAddressActivity.this);
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

    private void OPenBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setContentView(R.layout.new_address_place_picker);
        Button mapAddress = (Button) bottomSheetDialog.findViewById(R.id.btnUseMap);
        Button manualAddress = (Button) bottomSheetDialog.findViewById(R.id.btnSetAddress);
        TextView close = (TextView) bottomSheetDialog.findViewById(R.id.textViewCancel);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(NewAddressActivity.this, UserProfileActivity.class));
            }
        });
        manualAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                customAlertDialog();
            }
        });
        mapAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusCheck();
                if (GPS) {
                    //check permission
                    if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Rect displayRectangle = new Rect();
                        Window window = NewAddressActivity.this.getWindow();
                        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(NewAddressActivity.this);
                        ViewGroup viewGroup = findViewById(android.R.id.content);
                        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_before_loc_permission, viewGroup, false);
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
                                bottomSheetDialog.show();
                            }
                        });
                        alertDialog.show();
                    }
                    else{
                        Intent intent = new Intent(NewAddressActivity.this, LocationPickerActivity.class);
                        intent.putExtra("address", "");
                        intent.putExtra("idname", "");
                        intent.putExtra("caller", sCaller);
                        startActivity(intent);
                    }
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomSheetDialog.show();
    }
    private void customAlertDialog() {
        ShortNoticeDialog shortnotice = new ShortNoticeDialog(this);
        shortnotice.show();
    }
    private void askPermission() {
        ActivityCompat.requestPermissions(NewAddressActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    private void SetAddressValue() {
        if (selectedLocationZipCode == null){
            bottomSheetDialog.show();
            storeIDName = "";
            edAddr.setText("");
            edLocality.setText("");
            edOtherPlace.setText("");
            edZipCode.setText("");
        }
        else {
//            linearLayoutLocality.setVisibility(View.VISIBLE);
//            linearLayoutPlace.setVisibility(View.VISIBLE);
            edName.setText(storeIDName);
            edAddr.setText(selectedLocationAddress);
            edLocality.setText(selectedLocationLocality);
            edOtherPlace.setText(selectedLocationCity);
            edZipCode.setText(selectedLocationZipCode);
            geolocation.setText(String.valueOf(lati) + "," + String.valueOf(longi));
            if (edLocality.getText().toString().length() > 0){
                bottomSheetDialog.dismiss();
            }
        }
    }

}