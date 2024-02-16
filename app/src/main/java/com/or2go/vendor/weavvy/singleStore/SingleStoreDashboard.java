package com.or2go.vendor.weavvy.singleStore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.R;

import java.util.ArrayList;
import java.util.List;

public class SingleStoreDashboard extends AppCompatActivity {
    AppEnv gAppEnv;
    Context mContext;
    List<Integer> mOrderCatImage;
    List<String> mOrderCatList;
    List<Integer> mOrderCatCount;
    GridView gridViewSingleStore;
    DashboardAdapter dashboardAdapter;
    LinearLayout layoutSetYourStore;
    Button buttonSetStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_store_dashboard);

        gAppEnv = (AppEnv) getApplicationContext();
        mContext = this;
        initCategoryData();
        layoutSetYourStore = (LinearLayout) findViewById(R.id.layout_setYourStore);
        buttonSetStore = (Button) findViewById(R.id.button_setYourStore);
        gridViewSingleStore = (GridView) findViewById(R.id.gv_singleStore);
        dashboardAdapter = new DashboardAdapter(this, mOrderCatImage, mOrderCatList, mOrderCatCount);
        gridViewSingleStore.setAdapter(dashboardAdapter);
    }

    boolean initCategoryData() {
        mOrderCatImage = new ArrayList<Integer>();
        mOrderCatList = new ArrayList<String>();
        mOrderCatCount = new ArrayList<Integer>();

        mOrderCatImage.add(R.drawable.new_orders);
        mOrderCatImage.add(R.drawable.processing);
        mOrderCatImage.add(R.drawable.ready);
        mOrderCatImage.add(R.drawable.on_delivery);

        mOrderCatList.add("New Orders");
        mOrderCatList.add("Processing");
        mOrderCatList.add("Ready");
        mOrderCatList.add("On Delivery");

        mOrderCatCount.add(gAppEnv.getOrderManager().getPendingOrdersCount());
        mOrderCatCount.add(gAppEnv.getOrderManager().getProcessingingOrdersCount());
        mOrderCatCount.add(gAppEnv.getOrderManager().getReadyOrdersCount());
        mOrderCatCount.add(gAppEnv.getOrderManager().getOnDeliveryOrdersCount());

        return true;
    }
}