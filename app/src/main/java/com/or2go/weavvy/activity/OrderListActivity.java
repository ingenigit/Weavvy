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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.OrderListDividerDecoration;
import com.or2go.weavvy.R;
import com.or2go.weavvy.adapter.OrderListAdapter;
import com.or2go.weavvy.manager.OrderManager;

import java.util.ArrayList;

public class OrderListActivity extends AppCompatActivity {

    // Get Application super class for global data
    AppEnv gAppEnv;
    Context mContext;
    OrderManager orderMgr;
    RecyclerView mRecyclerView;
    OrderListAdapter mAdapter;
    OrderListAdapter.RecyclerViewClickListener mListner;
    ArrayList<Or2goOrderInfo> orderList;
    BottomNavigationView btNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        mContext = OrderListActivity.this;
        gAppEnv = (AppEnv)getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrderListActivity.this, SplashScreen.class));
        }
        gAppEnv.getNotificationManager().clearAllNotifications();
        orderMgr = gAppEnv.getOrderManager();
        orderList = orderMgr.getOrderList();
        mRecyclerView = (RecyclerView) findViewById(R.id.order_listview);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new OrderListDividerDecoration(this, LinearLayoutManager.VERTICAL, 16));
        mListner = new OrderListAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position >= 0) {
                    Intent billingintent;
                    Or2goOrderInfo selorder = orderList.get(position);
//                    if (selorder.isActionRequired())
//                        billingintent = new Intent(OrderListActivity.this, OrderActionActivity.class);
//                    else
                        billingintent = new Intent(OrderListActivity.this, OrderDetailsActivity.class);
                    billingintent.putExtra("orderid", selorder.getId());
                    startActivity(billingintent);
                }
            }
        };
        mAdapter = new OrderListAdapter(gAppEnv, mContext, orderList, mListner);
        mRecyclerView.setAdapter(mAdapter);
        //Bottom Navigation
        btNavigation = (BottomNavigationView) findViewById(R.id.orderlist_navi);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Integer cartcnt = gAppEnv.getCartCaount();//getCartManager().getCartSize();
        if (cartcnt > 0) {
            btNavigation.getMenu().getItem(1).setTitle("Cart : (" + cartcnt + ")" );
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_dtl_nav_home:
                    startActivity(new Intent(OrderListActivity.this, MainActivity.class));
                    return true;
                case R.id.item_dtl_nav_cart:
                    if (gAppEnv.getCartCaount() > 0)
                        startActivity(new Intent(OrderListActivity.this,OrderCartActivity.class));
                    else
                        Toast.makeText(mContext,"Cart is empty. ", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };

    final BroadcastReceiver mReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action =  intent.getAction();
            gAppEnv.getGposLogger().d("OrderList Broadcast Receiver: message from Order Manager - Action="+action);
            orderList = orderMgr.getOrderList();
            mAdapter.notifyDataSetChanged();
            gAppEnv.getNotificationManager().clearAllNotifications();//clearNotification(curorderid);
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        registerStatusUpdateReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterStatusUpdateReceiver();
    }

    private void registerStatusUpdateReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("ORDER_STATUS_UPDATE"));
    }

    private void unregisterStatusUpdateReceiver() {
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
        catch (IllegalArgumentException e) {

        }
    }
}