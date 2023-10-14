package com.or2go.weavvy.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;
import com.or2go.weavvy.model.StoreList;
import com.or2go.weavvy.adapter.StoreListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Context context;
    AppEnv gAppEnv;
    TextView textViewAddress, textViewCity;
    RelativeLayout relativeLayoutAddress;
    RecyclerView recyclerView;
    EditText editTextSearch;
    ImageView imageViewClear;
    ProgressBar progressBar;
    FloatingActionButton fabOpenMap;
    ArrayList<StoreList> storeList;
    StoreListAdapter storeListAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        gAppEnv = (AppEnv) getApplicationContext();

        relativeLayoutAddress = (RelativeLayout) findViewById(R.id.relativelayout_address);
        textViewCity = (TextView) findViewById(R.id.tv_selectedAddressCity);
        textViewAddress = (TextView) findViewById(R.id.tv_selectedAddress);
        editTextSearch = (EditText) findViewById(R.id.ed_search);
        imageViewClear = (ImageView) findViewById(R.id.img_clear);
        progressBar = (ProgressBar) findViewById(R.id.progrssbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_StoreList);
        fabOpenMap = (FloatingActionButton) findViewById(R.id.fab_open_map);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        storeList = gAppEnv.gStoreMgr.getAllStoreList();
        for (int i = 0; i < storeList.size(); i++)
            System.out.println("StoreName: " + storeList.get(i).getStringName());
        storeListAdapter = new StoreListAdapter(context, storeList);
        recyclerView.setAdapter(storeListAdapter);
        fabOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapStoreList.class));
            }
        });
        if (!gAppEnv.gAppSettings.getAddressPage())
            relativeLayoutAddress.setVisibility(View.GONE);
        else
            getGeoAddress(gAppEnv.gAppSettings.getGeoAddress());
    }

    private void getGeoAddress(String geoAddress) {
        String[] address = geoAddress.split(",");
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(address[0]), Double.parseDouble(address[1]), 1);
            textViewAddress.setText(addresses.get(0).getAddressLine(0));
            textViewCity.setText(addresses.get(0).getLocality());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //press double back button to exit
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}