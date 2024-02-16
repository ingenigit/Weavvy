package com.or2go.vendor.weavvy.manager;

import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_STORE_ORDER_ASSIGN_CANCEL;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_STORE_ORDER_ASSIGN_REQUEST;
import static com.or2go.core.Or2goConstValues.OR2GO_ACTIVE_DA_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_ASSIGN_DA;
import static com.or2go.core.Or2goConstValues.OR2GO_CANCEL_DA_ASSIGN;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_DA_ASSIGN_CANCEL;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_DA_ASSIGN_REQUEST;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.or2go.core.Or2GoDAInfo;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.server.ActiveDAListCallback;
import com.or2go.vendor.weavvy.server.AssignDACallback;

import org.json.JSONObject;

import java.util.ArrayList;

public class DeliveryManager {
    AppEnv gAppEnv;
    Context mContext;
    private ArrayList<Or2GoDAInfo> gDAList;

    ActiveDAListCallback mDAListCallback;
    private Integer mAPIStatus;

    public DeliveryManager(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv)context;
        gDAList = new ArrayList<Or2GoDAInfo>();
        mAPIStatus=0;
    }

    public boolean addDAInfo(Or2GoDAInfo dainfo) { gDAList.add(dainfo); return  true;}

    //public ArrayList<Or2GoDAInfo> getDAList() {return gDAList;}

    public Or2GoDAInfo getDAInfo(String daid)
    {
        Or2GoDAInfo dainfo;
        int dacnt= gDAList.size();
        for (int i=0;i<dacnt;i++)
        {
            dainfo=gDAList.get(i);
            if (dainfo.daId.equals(daid)) return dainfo;
        }

        return null;
    }

    public ArrayList<Or2GoDAInfo> getDAList() {
        if (mDAListCallback.getUpdateStatus() == true)
            return gDAList;
        else
            return null;
    }


    public void getDAInfoList()
    {
        Message msg = new Message();
        msg.what = OR2GO_ACTIVE_DA_LIST;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;

        mDAListCallback = new ActiveDAListCallback(mContext);
        mDAListCallback.setDataList(gDAList);
        //ordercb.setViewAdapter(itemlist, itemlistadapter);
        Bundle b = new Bundle();

        //b.putString("orderid", mOrderInfo.getId());

        b.putParcelable("callback", mDAListCallback);
        msg.setData(b);

        gAppEnv.getCommMgr().postMessage(msg);
    }

    public synchronized void getCurrentDAInfoList()
    {
        //check if already a call is in progress
        ///if (mAPIStatus!= 0) return;

        Message msg = new Message();
        msg.what = OR2GO_ACTIVE_DA_LIST;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;

        mDAListCallback = new ActiveDAListCallback(mContext);
        gDAList.clear();
        mDAListCallback.setDataList(gDAList);
        Bundle b = new Bundle();

        b.putParcelable("callback", mDAListCallback);
        msg.setData(b);

        ///mAPIStatus=1;
        gAppEnv.getCommMgr().postMessage(msg);
    }


    public void assignDA(Or2GoDAInfo daInfo, Or2goOrderInfo ordinfo)
    {
        Message msg = new Message();
        msg.what = OR2GO_ASSIGN_DA;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;

        AssignDACallback assigndacb = new AssignDACallback(mContext, ordinfo.getId(), daInfo.daId, OR2GO_EVENT_DA_ASSIGN_REQUEST);
        //ordercb.setViewAdapter(itemlist, itemlistadapter);

        Bundle b = new Bundle();
        b.putString("daid", daInfo.daId);
        b.putString("orderid", ordinfo.getId());

        //Or2goOrderInfo ordinfo = gAppEnv.getOrderManager().getOrder(orderid);
        JSONObject pktHeader = new JSONObject();
        try
        {
            pktHeader.accumulate("pktType", OR2GO_MSG_STORE_ORDER_ASSIGN_REQUEST);
            pktHeader.accumulate("daId", daInfo.daId);
            pktHeader.accumulate("ordId", ordinfo.getId());
            pktHeader.accumulate("ordStoreId", ordinfo.getStoreId()/*gAppEnv.gAppSettings.getStoreId()*/);
            pktHeader.accumulate("ordCustomerId", ordinfo.oCustomer);
            pktHeader.accumulate("ordDeliveryAddress", ordinfo.getAddress());
            pktHeader.accumulate("ordDeliveryPlace", ordinfo.getDeliveryLocation());
            pktHeader.accumulate("ordStatusDescription", "Order Assigned");


            gAppEnv.getGposLogger().d("Order Header Json String: " + pktHeader.toString());
        }
        catch(Exception e)
        {
            gAppEnv.getGposLogger().d("Exception: "+e.getMessage());
        }

        b.putString("pktHeader", pktHeader.toString());
        b.putParcelable("callback", assigndacb);
        msg.setData(b);

        gAppEnv.getCommMgr().postMessage(msg);
    }


    public boolean cancelDAAssignRequest(Or2goOrderInfo ordinfo)
    {
        String daid = ordinfo.getDAId();

        if (daid.isEmpty()) return false;

        Or2GoDAInfo dainfo = gAppEnv.getDeliveryManager().getDAInfo(daid);

        Message msg = new Message();
        msg.what = OR2GO_CANCEL_DA_ASSIGN;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;

        AssignDACallback assigndacb = new AssignDACallback(mContext, ordinfo.getId(), daid, OR2GO_EVENT_DA_ASSIGN_CANCEL);
        //ordercb.setViewAdapter(itemlist, itemlistadapter);

        Bundle b = new Bundle();
        b.putString("daid", daid);
        b.putString("orderid", ordinfo.getId());

        //Or2goOrderInfo ordinfo = gAppEnv.getOrderManager().getOrder(orderid);
        JSONObject pktHeader = new JSONObject();
        try
        {
            pktHeader.accumulate("pktType", OR2GO_MSG_STORE_ORDER_ASSIGN_CANCEL);
            pktHeader.accumulate("daId", daid);
            pktHeader.accumulate("ordId", ordinfo.getId());
            pktHeader.accumulate("ordStoreId", ordinfo.getStoreId()/*gAppEnv.gAppSettings.getStoreId()*/);
            pktHeader.accumulate("ordStatusDescription", "Order Assign Cancelled");


            gAppEnv.getGposLogger().d("Order Header Json String: " + pktHeader.toString());
        }
        catch(Exception e)
        {
            gAppEnv.getGposLogger().d("Exception: "+e.getMessage());
        }

        b.putString("pktHeader", pktHeader.toString());
        b.putParcelable("callback", assigndacb);
        msg.setData(b);

        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

}
