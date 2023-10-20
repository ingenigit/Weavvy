package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.or2go.core.SearchInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.CustomDailogView;
import com.or2go.weavvy.OrderListDividerDecoration;
import com.or2go.weavvy.R;
import com.or2go.weavvy.model.SearchStore;
import com.or2go.weavvy.model.StoreList;
import com.or2go.weavvy.adapter.StoreListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Context context;
    AppEnv gAppEnv;
    TextView textViewAddress, textViewCity, textViewUser;
    RelativeLayout relativeLayoutAddress;
    LinearLayout linearLayoutUser;
    private RecyclerView recyclerView;
    SearchView searchViewSearch;
    ImageView imageViewClear;
    ProgressBar progressBar;
    FloatingActionButton fabOpenMap;
    ArrayList<StoreList> storeList;
    StoreListAdapter storeListAdapter;
    SkeletonScreen skeletonScreen;
    boolean doubleBackToExitPressedOnce = false;
    BottomNavigationView navigation;
    String[] permission = new String[]{ Manifest.permission.POST_NOTIFICATIONS };
    CustomDailogView customDailogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (!gAppEnv.isInternetOn()) {
            InternetCheckDialog();
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission[0]) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, permission, 101);
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(context,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, SplashScreen.class));
        }
        relativeLayoutAddress = (RelativeLayout) findViewById(R.id.relativelayout_address);
        textViewCity = (TextView) findViewById(R.id.tv_selectedAddressCity);
        textViewAddress = (TextView) findViewById(R.id.tv_selectedAddress);
        textViewUser = (TextView) findViewById(R.id.textviewuserName);
        searchViewSearch = (SearchView) findViewById(R.id.searchView_search);
        imageViewClear = (ImageView) findViewById(R.id.img_clear);
        progressBar = (ProgressBar) findViewById(R.id.progrssbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_StoreList);
        linearLayoutUser = (LinearLayout) findViewById(R.id.linearLayour_user);
        fabOpenMap = (FloatingActionButton) findViewById(R.id.fab_open_map);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        storeList = gAppEnv.gStoreMgr.getAllStoreList();
        for (int i = 0; i < storeList.size(); i++)
            System.out.println("StoreName: " + storeList.get(i).getStringName());

        storeListAdapter = new StoreListAdapter(context, storeList);
        recyclerView.setAdapter(storeListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new OrderListDividerDecoration(this, LinearLayoutManager.VERTICAL, 16));
        //shimmer
        skeletonScreen = Skeleton.bind(recyclerView)
                .adapter(storeListAdapter)
                .count(6)
                .load(R.layout.skeleton_sales_select_item)
                .duration(500)
                .shimmer(true)
                .angle(30)
                .frozen(false)
                .show();
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

        navigation = (BottomNavigationView) findViewById(R.id.appmain_navi);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //bottom Cart
        int ordcnt = gAppEnv.getOrderManager().getOrderList().size();
        if (ordcnt > 0) {
            navigation.getMenu().getItem(2).setTitle("Orders : (" + ordcnt + ")" );
        }
        int cartcnt = gAppEnv.getCartCaount();//getCartManager().getCartSize();
        if (cartcnt > 0) {
            navigation.getMenu().getItem(3).setTitle("Cart : (" + cartcnt + ")" );
        }

        searchViewSearch.onActionViewExpanded();
        searchViewSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println("qwertyuiop" + query);
                gAppEnv.getStoreManager().getSearchInfo(query, storeList);
                storeListAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println("asdfghjkl" + newText);
                ArrayList<StoreList> storeLists = gAppEnv.gStoreMgr.getAllStoreList();
                if (newText.length() > 0)
                    gAppEnv.getStoreManager().getSearchInfo(newText, storeList);
                else {
                    storeList.clear();
                    storeList.addAll((ArrayList<StoreList>) storeLists);
//                    storeList = gAppEnv.gStoreMgr.getAllStoreList();
                }
                storeListAdapter.notifyDataSetChanged();
                return false;
            }
        });

        if (!gAppEnv.isRegistered()) {
            textViewUser.setText("User1001");
        }
        else
            textViewUser.setText(gAppEnv.gAppSettings.getUserName());

        if (storeList.size() > 0)
            skeletonScreen.hide();
        linearLayoutUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gAppEnv.isRegistered())
                    RegisterDialog();
                else
                    startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.mainnavi_user:
                    if ( gAppEnv.isRegistered())
                        startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                    else
                        startActivity(new Intent(MainActivity.this, UserRegisterActivity.class));
                    return true;
                case R.id.mainnavi_search:
                    startActivity(new Intent(MainActivity.this, SearchActivity.class));
                    return true;

                case R.id.mainnavi_order:
                    int ordcnt = gAppEnv.getOrderManager().getOrderCount();
                    if (ordcnt > 0)
                        startActivity(new Intent(MainActivity.this,OrderListActivity.class));
                    else
                        Toast.makeText(context,"No pending Order at present. ", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.mainnavi_cart:
                    if (!gAppEnv.isRegistered())
                        RegisterDialog();
                    else if (gAppEnv.getCartCaount() > 0)
                        startActivity(new Intent(MainActivity.this,OrderCartActivity.class));
                    else
                        Toast.makeText(context,"Cart is empty. ", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };

    private void RegisterDialog() {
        final Dialog dialog = new Dialog(context, R.style.OtpDialog);
        dialog.setContentView(R.layout.dialog_register_msg);
        dialog.setTitle("Register");
        dialog.setCancelable(false);
        Button okButton = (Button) dialog.findViewById(R.id.btRegOk);
        Button cancelButton = (Button) dialog.findViewById(R.id.btRegCancel);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserRegisterActivity.class));
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void InternetCheckDialog(){
        String title = "Internet Connection";
        String body = "No Internet Connection. Please connect to internet for online ordering.";
        String positive = "OK";
        boolean visible = false;
        CustomDailogView.onClickButton onclick = new CustomDailogView.onClickButton() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.positive_Btn:
                        customDailogView.dismiss();
                        break;
                }
            }
        };
        customDailogView = new CustomDailogView(MainActivity.this, title, body, positive, "", visible, onclick);
        customDailogView.show();
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

    @Override
    protected void onResume() {
        super.onResume();
        searchViewSearch.setQuery("", false);
        searchViewSearch.clearFocus();
    }
}