package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.or2go.core.OrderHistoryInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;
import com.or2go.weavvy.adapter.OrderHistoryAdapter;
import com.or2go.weavvy.manager.OrderHistoryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderHistoryActivity extends AppCompatActivity {

    AppEnv gAppEnv;
    Context mContext;
    OrderHistoryManager orderHisMgr;
    RecyclerView mRecyclerView;
    OrderHistoryAdapter mAdapter;
    OrderHistoryAdapter.RecyclerViewClickListener mListner;
    ArrayList<OrderHistoryInfo> orderList;
    BottomNavigationView btNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        mContext = OrderHistoryActivity.this;
        gAppEnv = (AppEnv)getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrderHistoryActivity.this, SplashScreen.class));
        }
        gAppEnv.getNotificationManager().clearAllNotifications();
        orderHisMgr = gAppEnv.getOrderHistoryManager();
        orderList = new ArrayList<>();
        orderList = orderHisMgr.getOrderHistories();
        mRecyclerView = (RecyclerView) findViewById(R.id.orderhistory_listview);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        Collections.sort(orderList, new Comparator<OrderHistoryInfo>() {
            @Override
            public int compare(OrderHistoryInfo o1, OrderHistoryInfo o2) {
                return o2.oTime.compareTo(o1.oTime);
            }
        });

        mListner = new OrderHistoryAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position >= 0) {
                    final OrderHistoryInfo ordinfo = orderList.get(position);
                    Intent in = new Intent(OrderHistoryActivity.this, OrderHistoryDetailsActivity.class);
                    in.putExtra("orderid", ordinfo.oId);
                    startActivity(in);
                }
            }
        };
        mAdapter = new OrderHistoryAdapter(gAppEnv, mContext, orderList, mListner);
        mRecyclerView.setAdapter(mAdapter);
        //Bottom Navigation
        btNavigation = (BottomNavigationView) findViewById(R.id.orderhistory_navi);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Integer cartcnt = gAppEnv.getCartCaount();//getCartManager().getCartSize();
        if (cartcnt > 0)
            btNavigation.getMenu().getItem(1).setTitle("Cart : (" + cartcnt + ")" );
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_dtl_nav_home:
                    Intent i = new Intent(OrderHistoryActivity.this, MainActivity.class);
                    startActivity(i);
                    return true;
                case R.id.item_dtl_nav_cart:
                    if (gAppEnv.getCartCaount() > 0)
                        startActivity(new Intent(OrderHistoryActivity.this, OrderCartActivity.class));
                    else
                        Toast.makeText(mContext, "Cart is empty. ", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =  intent.getAction();
            orderList = orderHisMgr.getOrderHistories();
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        registerStatusUpdateReceiver();
    }
    private void registerStatusUpdateReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("Order_Status_Complete"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterStatusUpdateReceiver();
    }
    private void unregisterStatusUpdateReceiver() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }catch (IllegalArgumentException e){
            gAppEnv.getGposLogger().e("unregisterStatusUpdateReceiver: " + e);
        }
    }
}