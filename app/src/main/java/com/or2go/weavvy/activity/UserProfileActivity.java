package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.or2go.adapter.AddressListAdapter;
import com.or2go.core.DeliveryAddrInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.OrderListDividerDecoration;
import com.or2go.weavvy.R;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    Context mContext;
    AppEnv gAppEnv;
    TextView uName, uDetail;
    MaterialButton btEdit;
    RecyclerView mRecyclerView;
    AddressListAdapter mAdapter;
    ArrayList<DeliveryAddrInfo> mAddressList;
    BottomNavigationView btNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UserProfileActivity.this, SplashScreen.class));
        }
        uName = (TextView)  findViewById(R.id.tvcustname);
        uDetail = (TextView)  findViewById(R.id.tvcustdetail);
        btEdit = (MaterialButton) findViewById(R.id.btEditProfile);
        mRecyclerView = (RecyclerView) findViewById(R.id.address_listview);
        mAddressList = gAppEnv.getDeliveryManager().getAddrList();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new OrderListDividerDecoration(this, LinearLayoutManager.VERTICAL, 16));

        AddressListAdapter.RecyclerViewClickListener addrlistener = new AddressListAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position >= 0) {
                    if (view.getId() == R.id.btAddrEdit) {
                        Intent editeddractivity = new Intent(UserProfileActivity.this, EditAddressActivity.class);
                        editeddractivity.putExtra("addrname", mAddressList.get(position).getAddrName() );//, value)("orderid", test[0]);
                        startActivity(editeddractivity);
                    }
                    else if (view.getId() == R.id.btAddrDelete){
                        boolean ret = gAppEnv.getDeliveryManager().deleteAddr( mAddressList.get(position).getAddrName());
                        if (ret) {
                            Toast.makeText(mContext, "Address deleted:", Toast.LENGTH_SHORT).show();
                            mAddressList.clear();
                            mAddressList = gAppEnv.getDeliveryManager().getAddrList();
                            AddressListAdapter adapter = (AddressListAdapter)mAdapter;
                            adapter.updateList(mAddressList);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        };
        mAdapter = new AddressListAdapter(mContext, mAddressList, R.layout.listview_address, addrlistener);
        mRecyclerView.setAdapter(mAdapter);
        String userid = gAppEnv.gAppSettings.getUserId();
        String username = gAppEnv.gAppSettings.getUserName();
        String useremail = gAppEnv.gAppSettings.getUserEmail();
        uName.setText(username);
        uDetail.setText(userid + " | " + useremail);
        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Comming Soon", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserProfileActivity.this, EditProfileActivity.class));
            }
        });
        //Bottom Navigation
        btNavigation = (BottomNavigationView) findViewById(R.id.navigation_profile);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.profilenavi_home:
                            startActivity(new Intent(UserProfileActivity.this, MainActivity.class));
                            return true;
                        case R.id.profilenavi_order_history:
                            int ordcnt = gAppEnv.getOrderHistoryManager().getOrderCount();
                            if (ordcnt > 0)
                                startActivity(new Intent(UserProfileActivity.this, OrderHistoryActivity.class));
                            else
                                Toast.makeText(mContext,"Didn't have any Order History.. ", Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.profilenavi_newaddr:
                            startActivity(new Intent(UserProfileActivity.this, NewAddressActivity.class));
                            return true;
                    }
                    return false;
                }
            };
}