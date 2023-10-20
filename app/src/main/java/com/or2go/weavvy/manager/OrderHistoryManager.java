package com.or2go.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_HISTORY;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderHistoryInfo;
import com.or2go.mylibrary.CompletedOrderDBHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.server.OrderHistoryCallback;

import java.util.ArrayList;


public class OrderHistoryManager {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    CompletedOrderDBHelper completedorderdb=null;
    //private ArrayList<Or2goOrderInfo> gOrderList;
    ArrayList<OrderHistoryInfo> orderHistory;
    LocalBroadcastManager localBroadcastManager;
    Intent historyIntent;

    public OrderHistoryManager(Context context) {
        mContext = context;
        //Get global application
        gAppEnv = (AppEnv) context;
        initCompletedOrderDB();
        orderHistory = new ArrayList<OrderHistoryInfo>();
        orderHistory = getCompletedOrders(0); //get all the completed orders in DB
        getOrderHistory(); //TBF alsways get the order history for now...
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        historyIntent = new Intent("Order_Status_Complete");
    }

    private void initCompletedOrderDB() {
        completedorderdb = new CompletedOrderDBHelper(mContext);
        completedorderdb.InitOrderDB();
    }

    public boolean saveCompletedOrder(Or2goOrderInfo ordinfo) {
        boolean ret;
        if (completedorderdb==null) initCompletedOrderDB();
        ret =completedorderdb.insertCompletedOrder(ordinfo);
        ret =completedorderdb.insertCompletedOrderItems(ordinfo.getId(), ordinfo.getItemList());
        //add to order history list
        addCompletedOrder(ordinfo);
        return ret;
    }

    private boolean addCompletedOrder(Or2goOrderInfo ordinfo) {
        OrderHistoryInfo comporder = new OrderHistoryInfo(ordinfo.getId(), ordinfo.getStoreId(), ordinfo.getOrderTime());
        comporder.setStatus(ordinfo.oStatus);
        orderHistory.add(comporder);
        return true;
    }

    public ArrayList<OrderHistoryInfo> getCompletedOrders(int count) {
        return completedorderdb.getCompletedOrders(count);
    }

    public OrderHistoryInfo getCompletedOrder(String orderid) {
        return completedorderdb.getCompletedOrder(orderid);
    }

    //server API
    public boolean getOrderHistory(){
        Message msg = new Message();
        msg.what = OR2GO_ORDER_HISTORY;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        OrderHistoryCallback historycb = new OrderHistoryCallback(mContext);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", historycb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
        return true;
    }

    public boolean addOrderHistoryId(OrderHistoryInfo historyInfo){
        if (isOrderHistoryExists(historyInfo.oId))
            return false;
        else {
            boolean ret;
            ret = completedorderdb.insertCompletedOrder(historyInfo);
            ret = completedorderdb.insertCompletedOrderItems(historyInfo.oId, historyInfo.getItemList());
            orderHistory.add(historyInfo);
//            localBroadcastManager.sendBroadcast(historyIntent);
            return true;
        }
    }

    public ArrayList<OrderHistoryInfo> getOrderHistories(){
        return orderHistory;
    }

    public int getOrderCount() {
        return orderHistory.size();
    }

    public boolean isOrderHistoryExists(String id) {
        int hissz = orderHistory.size();
        for(int i=0; i<hissz; i++) {
            OrderHistoryInfo hinfo = orderHistory.get(i);
            if ( hinfo.oId.equals(id))
                return true;
        }
        return false;
    }

    public OrderHistoryInfo getHistoryById(String id) {
        int hissz = orderHistory.size();
        for(int i=0; i<hissz; i++) {
            OrderHistoryInfo hinfo = orderHistory.get(i);
            if ( hinfo.oId.equals(id))
                return hinfo;
        }
        return null;
    }

    public void broadcastStatusUpdate() {
        localBroadcastManager.sendBroadcast(historyIntent);
    }
}
