package com.or2go.vendor.weavvy.manager;

import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_STORE_ORDER_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_ACTIVE_ORDER_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_STATUS_DELIVERY_FAIL;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_CONFIRM;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_CONFIRM_COND_PREPAY;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_PICKUP_CUSTOMER;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_PICKUP_DA;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_READY_DELIVERY;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_READY_PICKUP;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_REJECT;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_DELIVERY;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_PICKUP;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_CARD;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_COD;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_EXT_CARD;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_EXT_NEFT;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_EXT_UPI;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_EXT_WALLET;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_MODE_CASHLESS;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_MODE_ONLINE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_UPI;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_WALLET;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_NONE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CONFIRMED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CONFIRM_COND_PREPAYMENT;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_PICKED_UP;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_PLACED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_READY;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_REJECTED;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.or2go.core.Or2GoDAInfo;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.server.ActiveOrderCallback;
import com.or2go.vendor.weavvy.server.OrderStatusUpdateCallback;
import com.or2go.vendor.weavvy.singleStore.OrderDetailActivity;
import com.or2go.vendor.weavvy.singleStore.SingleStoreDashboard;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderManager {
    private Context mContext;
    AppEnv gAppEnv; // Get Application super class for global data

    ArrayList<Or2goOrderInfo> mOrderList;
    ArrayList<Or2goOrderInfo> mCompletedOrderList;
    //ArrayList<Or2goOrderInfo> mProcessingOrderList;
    //ArrayList<Or2goOrderInfo> mReadyOrderList;

    LocalBroadcastManager localBroadcastManager;
    Intent ordpendingintent;
    Intent ordstatusintent;

    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat outputFormat = new SimpleDateFormat("dd ");

    public OrderManager(Context context)
    {
        mContext =context;
        gAppEnv = (AppEnv)context;

        mOrderList = new ArrayList<Or2goOrderInfo>();
        mCompletedOrderList = new ArrayList<Or2goOrderInfo>();
        //mProcessingOrderList = new ArrayList<Or2goOrderInfo>();
        //mReadyOrderList = new ArrayList<Or2goOrderInfo>();

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        ordpendingintent=new Intent("OR2GO_STORE_PENDING_ORDER");
        ordstatusintent= new Intent("OR2GO_STORE_ORDER_STATUS_UPDATE");
    }

    public synchronized boolean addOrder(Or2goOrderInfo order)
    {
        if (getOrder(order.oOr2goId) == null)
            mOrderList.add(order);

        boolean brresult = localBroadcastManager.sendBroadcast(ordpendingintent/*ordstatusintent*/);
        System.out.println("AddPendingOrder BroadcastSend : result="+brresult);
        return true;
    }

    /*public synchronized boolean addProcessingOrder(Or2goOrderInfo order)
    {
        mProcessingOrderList.add(order);

        return true;
    }

    public synchronized boolean addReadyOrder(Or2goOrderInfo order)
    {
        mPendingOrderList.add(order);

        return true;
    }

    public synchronized boolean deletePendingOrder(Or2goOrderInfo order)
    {
        mPendingOrderList.add(order);

        return true;
    }*/

    boolean getPendingOrderList(ArrayList<Or2goOrderInfo> list) {
        list.clear();
        int ordsz = mOrderList.size();
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_PLACED)
                list.add(ordinfo);
        }
        return true;
    }

    public boolean getProcessingingOrderList(ArrayList<Or2goOrderInfo> list) {
        list.clear();
        int ordsz = mOrderList.size();
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_CONFIRMED)
                list.add(ordinfo);
        }
        return true;
    }
    public boolean getReadyOrderList(ArrayList<Or2goOrderInfo> list) {
        list.clear();
        int ordsz = mOrderList.size();
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_READY)
                list.add(ordinfo);
        }
        return true;
    }

    public boolean getOnDeliveryOrderList(ArrayList<Or2goOrderInfo> list) {
        list.clear();
        int ordsz = mOrderList.size();
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_PICKED_UP)
                list.add(ordinfo);
        }
        return true;
    }
    public boolean getCompleteOrderList(ArrayList<Or2goOrderInfo> list) {
        list.clear();
        int ordsz = mCompletedOrderList.size();
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mCompletedOrderList.get(i);
            //System.out.println("kkmml" + ordinfo.oTime);
            Date date = new Date();
            Date date1 = null;
            try {
                date1 = inputFormat.parse(ordinfo.oTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (ordinfo.oPayMode == OR2GO_PAY_MODE_COD || ordinfo.oPayMode == OR2GO_PAY_STATUS_NONE && outputFormat.format(date1) == String.valueOf(date.getDate())) {//paymode is not fixed.
                list.add(ordinfo);
            }
        }
        return true;
    }

    public boolean getPickUpOrderList(ArrayList<Or2goOrderInfo> list, String id, String otp) {
        list.clear();
        int ordsz = mOrderList.size();
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_READY && ordinfo.oType != 1) {
                if (ordinfo.oCustomer.equals(id) && ordinfo.oPickupOTP.equals(otp)) {
                    list.add(ordinfo);
                    return true;
                }
            }
        }
        return true;
    }

    public synchronized Integer getCompleteOrdersCount() {
        int ordsz = mCompletedOrderList.size();
        int listcnt=0;
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mCompletedOrderList.get(i);
            Date date = new Date();
            Date date1 = null;
            try {
                date1 = inputFormat.parse(ordinfo.oTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (ordinfo.oPayMode == OR2GO_PAY_MODE_COD || ordinfo.oPayMode == OR2GO_PAY_STATUS_NONE && outputFormat.format(date1) == String.valueOf(date.getDate()))
                listcnt += Integer.parseInt(ordinfo.oTotal);
        }
        return listcnt;
    }

    public synchronized Integer getPendingOrdersCount() {
        int ordsz = mOrderList.size();
        int listcnt=0;
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_PLACED)
                listcnt++;
        }
        return listcnt;
    }
    public synchronized Integer getProcessingingOrdersCount() {
        int ordsz = mOrderList.size();
        int listcnt=0;
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_CONFIRMED)
                listcnt++;
        }
        return listcnt;
    }
    public synchronized Integer getReadyOrdersCount() {
        int ordsz = mOrderList.size();
        int listcnt=0;
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_READY)
                listcnt++;
        }
        return listcnt;
    }

    public synchronized Integer getOnDeliveryOrdersCount() {
        int ordsz = mOrderList.size();
        int listcnt=0;
        for(int i=0;i<ordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oStatus==ORDER_STATUS_PICKED_UP)
                listcnt++;
        }
        return listcnt;
    }

    public Integer getActiveOrderCount() {
        return mOrderList.size();
    }

    public Or2goOrderInfo getOrder(String orderid) {
        int pendordsz = mOrderList.size();
        for(int i=0;i<pendordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mOrderList.get(i);
            if (ordinfo.oOr2goId.equals(orderid))
                return ordinfo;
        }

        return null;

    }

    /*public Or2goOrderInfo getPendingOrder(String orderid) {

        int pendordsz = mPendingOrderList.size();
        for(int i=0;i<pendordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mPendingOrderList.get(i);
            if (ordinfo.getId().equals(orderid))
                return ordinfo;
        }

        return null;

    }

    public Or2goOrderInfo getProcessingOrder(String orderid) {
        int pendordsz = mProcessingOrderList.size();
        for(int i=0;i<pendordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mProcessingOrderList.get(i);
            if (ordinfo.getId().equals(orderid))
                return ordinfo;
        }

        return null;
    }

    public Or2goOrderInfo getReadyOrder(String orderid) {
        int pendordsz = mReadyOrderList.size();
        for(int i=0;i<pendordsz;i++)
        {
            Or2goOrderInfo ordinfo =  mProcessingOrderList.get(i);
            if (ordinfo.getId().equals(orderid))
                return ordinfo;
        }

        return null;
    }*/

    public synchronized boolean setOrderStatus(String orderid, Integer sts)
    {
        Or2goOrderInfo ordinfo = getOrder(orderid);

        if (ordinfo != null) {
            ordinfo.setStatus(sts);
            return true;
        }else
            return false;

    }

    public synchronized boolean updateOrderStatus(String orderid, Integer sts, String desc)
    {
        Or2goOrderInfo ordinfo = getOrder(orderid);

        if (ordinfo == null) return false;

        ordinfo.setStatus(sts);
        /*if (sts == ORDER_STATUS_CONFIRMED)
        {
            mPendingOrderList.remove(ordinfo);
            mProcessingOrderList.add(ordinfo);
        }
        else*/
        if (sts == ORDER_STATUS_REJECTED)
            mOrderList.remove(ordinfo);
        else if (sts == ORDER_STATUS_CANCELLED){
            mOrderList.remove(ordinfo);
            Intent orderIntent = new Intent(mContext, SingleStoreDashboard.class);
            String notmsg = "Order ID:" + orderid + " " + desc;
            gAppEnv.getNotificationManager().setNotification("Order Canceled", notmsg, orderIntent);
        }
        else if (sts == ORDER_STATUS_COMPLETE){
            mCompletedOrderList.add(ordinfo);
            Intent intent = new Intent(mContext, OrderDetailActivity.class);
            intent.putExtra("orderid", orderid);
            intent.putExtra("orderstatus", sts);
            String notmsg = "Order ID:" + orderid + " " + desc;
            gAppEnv.getNotificationManager().setNotification("Order Completed", notmsg, intent);
        }
        else if (sts == OR2GO_DELIVERY_STATUS_DELIVERY_FAIL){
            mCompletedOrderList.add(ordinfo);
            Intent intent = new Intent(mContext, OrderDetailActivity.class);
            intent.putExtra("orderid", orderid);
            intent.putExtra("orderstatus", sts);
            String notmsg = "Order ID:" + orderid + " " + desc;
            gAppEnv.getNotificationManager().setNotification("Delivery Attempt Failed.", notmsg, intent);
        }

        boolean brresult = localBroadcastManager.sendBroadcast(ordstatusintent);
        System.out.println("AddPendingOrder BroadcastSend : result="+brresult);
        return true;

    }

    public synchronized boolean updateOrderStatusOTP(String orderid, Integer sts, String otp) {
        Or2goOrderInfo ordinfo = getOrder(orderid);
        if (ordinfo == null) return false;
        ordinfo.setStatus(sts);
        ordinfo.setPickupOTP(otp);

        boolean brresult = localBroadcastManager.sendBroadcast(ordstatusintent);
        System.out.println("AddPendingOrder BroadcastSend : result="+brresult);
        return true;
    }

    public synchronized boolean updateDA(String orderid, String daid) {
        Or2goOrderInfo ordinfo = getOrder(orderid);
        if (ordinfo == null) return false;

        Or2GoDAInfo dainfo = gAppEnv.getDeliveryManager().getDAInfo(daid);
        if (dainfo==null){
            ordinfo.setDAId("");
            ordinfo.setDAName("");
            ordinfo.setDAContact("");
        }else{
            ordinfo.setDAId(daid);
            ordinfo.setDAName(dainfo.daName);
            ordinfo.setDAContact(dainfo.daContact);
        }
        return true;
    }

    public synchronized boolean clearDA(String orderid){
        Or2goOrderInfo ordinfo = getOrder(orderid);
        if (ordinfo == null) return false;

        ordinfo.setDAId("");
        ordinfo.setDAName("");
        ordinfo.setDAContact("");

        return true;
    }

    public synchronized boolean updateDeliveryStatus(String orderid, Integer sts)
    {
        Or2goOrderInfo ordinfo = getOrder(orderid);
        if (ordinfo == null) return false;

        ordinfo.setDeliveryStatus(sts);

        /*if (sts == OR2GO_DELIVERY_STATUS_ASSIGN_ACCEPT)
        {
            ordinfo.setDeliveryStatus(OR2GO_DELIVERY_STATUS_ASSIGNED);
        }
        else if (sts == OR2GO_DELIVERY_STATUS_ASSIGN_REJECT)
        {
            ordinfo.setDeliveryStatus(OR2GO_DELIVERY_STATUS_NONE);
        }*/


        boolean brresult = localBroadcastManager.sendBroadcast(ordstatusintent);
        System.out.println("AddPendingOrder BroadcastSend : result="+brresult);

        return true;
    }

    public synchronized boolean updatePaymentStatus(String orderid, Integer sts, Integer mode, String info)
    {
        Or2goOrderInfo ordinfo = getOrder(orderid);
        if (ordinfo == null) return false;

        ordinfo.setPayStatus(sts);
        ordinfo.oPayMode = mode;

        //push notification
        if(mode == OR2GO_PAY_MODE_COD) {
            Intent orderIntent = new Intent(mContext, SingleStoreDashboard.class);
            String notmsg = "Order ID: " + orderid + " " + ordinfo.getPayStatusText();
            gAppEnv.getNotificationManager().setNotification("COD Payment Done", notmsg, orderIntent);
        }
        else if(mode == OR2GO_PAY_MODE_MODE_ONLINE || mode == OR2GO_PAY_MODE_MODE_CASHLESS){
            Intent orderIntent = new Intent(mContext, SingleStoreDashboard.class);
            String notmsg = "Order ID: " + orderid + " " + ordinfo.getPayStatusText();
            gAppEnv.getNotificationManager().setNotification("Online Payment Done", notmsg, orderIntent);
        }
        else if(mode == OR2GO_PAY_MODE_CARD){
            Intent orderIntent = new Intent(mContext, SingleStoreDashboard.class);
            String notmsg = "Order ID: " + orderid + " " + ordinfo.getPayStatusText();
            gAppEnv.getNotificationManager().setNotification("Card Payment Done", notmsg, orderIntent);
        }
        else if(mode == OR2GO_PAY_MODE_UPI){
            Intent orderIntent = new Intent(mContext, SingleStoreDashboard.class);
            String notmsg = "Order ID: " + orderid + " " + ordinfo.getPayStatusText();
            gAppEnv.getNotificationManager().setNotification("UPI Payment Done", notmsg, orderIntent);
        }
        else if(mode == OR2GO_PAY_MODE_WALLET){
            Intent orderIntent = new Intent(mContext, SingleStoreDashboard.class);
            String notmsg = "Order ID: " + orderid + " " + ordinfo.getPayStatusText();
            gAppEnv.getNotificationManager().setNotification("Wallet Payment Done", notmsg, orderIntent);
        }
        else if(mode == OR2GO_PAY_MODE_EXT_UPI)
            Toast.makeText(mContext, "upi", Toast.LENGTH_SHORT).show();
        else if(mode == OR2GO_PAY_MODE_EXT_NEFT)
            Toast.makeText(mContext, "upi", Toast.LENGTH_SHORT).show();
        else if(mode == OR2GO_PAY_MODE_EXT_WALLET)
            Toast.makeText(mContext, "upi", Toast.LENGTH_SHORT).show();
        else if(mode == OR2GO_PAY_MODE_EXT_CARD)
            Toast.makeText(mContext, "upi", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, "Sorry", Toast.LENGTH_SHORT).show();

        boolean brresult = localBroadcastManager.sendBroadcast(ordstatusintent);
        System.out.println("AddPendingOrder BroadcastSend : result="+brresult);

        return true;
    }

    public boolean globalOrderReady(Or2goOrderInfo orderInfo)
    {
        if (orderInfo.getType() == OR2GO_ORDERTYPE_DELIVERY)
            gAppEnv.getOrderManager().globalOrderStatusUpdate(orderInfo, ORDER_STATUS_READY, OR2GO_EVENT_ORDER_READY_DELIVERY);
        else if (orderInfo.getType() == OR2GO_ORDERTYPE_PICKUP)
            gAppEnv.getOrderManager().globalOrderStatusUpdate(orderInfo, ORDER_STATUS_READY, OR2GO_EVENT_ORDER_READY_PICKUP);
        else
            return false;

        return true;

    }

    public boolean globalOrderPickup(Or2goOrderInfo orderInfo)
    {
        if (orderInfo.getType() == OR2GO_ORDERTYPE_DELIVERY)
            gAppEnv.getOrderManager().pickupOrder(orderInfo, ORDER_STATUS_PICKED_UP, OR2GO_EVENT_ORDER_PICKUP_DA);
        else if (orderInfo.getType() == OR2GO_ORDERTYPE_PICKUP)
            gAppEnv.getOrderManager().pickupOrder(orderInfo, ORDER_STATUS_PICKED_UP, OR2GO_EVENT_ORDER_PICKUP_CUSTOMER);
        else
            return false;

        return true;
    }


    ///Server API
    public boolean getActiveOrders()
    {
        Message msg = new Message();
        msg.what = OR2GO_ACTIVE_ORDER_LIST;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        ActiveOrderCallback logincb = new ActiveOrderCallback(mContext);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", logincb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean confirmOrder(Or2goOrderInfo ordinfo)
    {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", OR2GO_MSG_STORE_ORDER_STATUS_UPDATE);
            pktHeader.accumulate("ordAppId", ordinfo.oAppId);
            pktHeader.accumulate("ordCust",ordinfo.oCustomer);
            pktHeader.accumulate("ordId", ordinfo.getId());
            pktHeader.accumulate("ordStatus", ORDER_STATUS_CONFIRMED);
            pktHeader.accumulate("ordStatusDescription", "Order Confirmed.");

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        OrderStatusUpdateCallback ordstscb = new OrderStatusUpdateCallback(mContext, ordinfo.getId(), ORDER_STATUS_CONFIRMED);//Callback(mContext);
        Bundle b = new Bundle();

        b.putString("customerid", ordinfo.oCustomer);
        b.putString("orderid", ordinfo.getId());
        b.putInt("orderevent", OR2GO_EVENT_ORDER_CONFIRM);
        b.putString("description", " Order confirmed");
        b.putString("pktheader", jsonString);

        b.putParcelable("callback", ordstscb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean confirmOrderCondPrepayment(Or2goOrderInfo ordinfo)
    {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", OR2GO_MSG_STORE_ORDER_STATUS_UPDATE);
            pktHeader.accumulate("ordAppId", ordinfo.oAppId);
            pktHeader.accumulate("ordCust",ordinfo.oCustomer);
            pktHeader.accumulate("ordId", ordinfo.getId());
            pktHeader.accumulate("ordStatus", ORDER_STATUS_CONFIRM_COND_PREPAYMENT);
            pktHeader.accumulate("ordStatusDescription", "Order Confirmed.");

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        OrderStatusUpdateCallback ordstscb = new OrderStatusUpdateCallback(mContext, ordinfo.getId(), ORDER_STATUS_CONFIRMED);//Callback(mContext);
        Bundle b = new Bundle();

        b.putString("customerid", ordinfo.oCustomer);
        b.putString("orderid", ordinfo.getId());
        b.putInt("orderevent", OR2GO_EVENT_ORDER_CONFIRM_COND_PREPAY);
        b.putString("description", " Order confirmed");
        b.putString("pktheader", jsonString);

        b.putParcelable("callback", ordstscb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }


    //public boolean rejectOrder(Or2goOrderInfo ordinfo, String pName, String rReject, String statusmsg)
    public boolean rejectOrder(Or2goOrderInfo ordinfo, String statusmsg)
    {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", OR2GO_MSG_STORE_ORDER_STATUS_UPDATE);
            pktHeader.accumulate("ordAppId", ordinfo.oAppId);
            pktHeader.accumulate("ordCust",ordinfo.oCustomer);
            pktHeader.accumulate("ordId", ordinfo.getId());
            //pktHeader.accumulate("itemName", pName);  //removed
            //pktHeader.accumulate("rejectReason", rReject); //removed
            pktHeader.accumulate("messgae", statusmsg);
            pktHeader.accumulate("ordStatus", ORDER_STATUS_REJECTED);
            pktHeader.accumulate("ordStatusDescription", "Order Rejected.");

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        OrderStatusUpdateCallback ordstscb = new OrderStatusUpdateCallback(mContext, ordinfo.getId(), ORDER_STATUS_REJECTED);//Callback(mContext);
        Bundle b = new Bundle();

        b.putString("customerid", ordinfo.oCustomer);
        b.putString("orderid", ordinfo.getId());
        b.putInt("orderevent", OR2GO_EVENT_ORDER_REJECT);
        b.putInt("status", ORDER_STATUS_REJECTED);
        b.putString("messgae", statusmsg);
        b.putString("pktheader", jsonString);

        b.putParcelable("callback", ordstscb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean globalOrderStatusUpdate(Or2goOrderInfo ordinfo, Integer sts, Integer event)
    {
        System.out.println("globalOrderStatusUpdate :  Order Event="+event+ "  Status=="+sts);

        //String sDAId="";
        //if (ordinfo.getDAId())

        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", OR2GO_MSG_STORE_ORDER_STATUS_UPDATE);
            pktHeader.accumulate("ordAppId", ordinfo.oAppId);
            pktHeader.accumulate("ordCust",ordinfo.oCustomer);
            pktHeader.accumulate("ordId", ordinfo.getId());
            pktHeader.accumulate("ordStatus", sts);
            pktHeader.accumulate("ordStatusDescription", "Order Ready.");
            pktHeader.accumulate("ordDAId", ordinfo.getDAId());

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        OrderStatusUpdateCallback ordstscb = new OrderStatusUpdateCallback(mContext, ordinfo.getId(), sts);//Callback(mContext);

        if (ordinfo.oType == OR2GO_ORDERTYPE_PICKUP) ordstscb.setOTPRequired(true);

        Bundle b = new Bundle();
        b.putString("customerid", ordinfo.oCustomer);
        b.putString("storeid", ordinfo.oStoreId);
        b.putString("orderid", ordinfo.getId());
        b.putInt("orderevent", event);
        b.putString("description", " Order Ready");
        b.putString("pktheader", jsonString);

        b.putParcelable("callback", ordstscb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean pickupOrder(Or2goOrderInfo ordinfo, Integer sts, Integer event)
    {
        System.out.println("globalOrderStatusUpdate :  Order Event="+event+ "  Status=="+sts);
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", OR2GO_MSG_STORE_ORDER_STATUS_UPDATE);
            pktHeader.accumulate("ordAppId", ordinfo.oAppId);
            pktHeader.accumulate("ordCust",ordinfo.oCustomer);
            pktHeader.accumulate("ordId", ordinfo.getId());
            pktHeader.accumulate("ordStatus", sts);
            pktHeader.accumulate("ordDAId", ordinfo.getDAId());
            pktHeader.accumulate("ordDAName", ordinfo.getDAName());
            pktHeader.accumulate("ordDAContact", ordinfo.getDAContact());
            pktHeader.accumulate("ordStatusDescription", "Order PickUp");

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
            System.out.println("OrderManager :  Order PICKEDUP Header");
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        OrderStatusUpdateCallback ordstscb = new OrderStatusUpdateCallback(mContext, ordinfo.getId(), sts);//Callback(mContext);
        Bundle b = new Bundle();

        b.putString("customerid", ordinfo.oCustomer);
        b.putString("orderid", ordinfo.getId());
        b.putInt("orderevent", event);
        b.putString("description", " Order PickUp");
        b.putString("pktheader", jsonString);

        b.putParcelable("callback", ordstscb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    /*public boolean cancelOrder(String orderid)
    {
        //JSONObject jsonObject = new JSONObject();
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {

            pktHeader.accumulate("pktType", "OrderCancelRequest");
            pktHeader.accumulate("ordCust", gAppEnv.gAppSettings.getUserId());
            pktHeader.accumulate("ordStatus", ORDER_STATUS_CANCEL_REQUEST);
            pktHeader.accumulate("ordId", orderid);

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        OrderStatusUpdateCallback ordercb = new OrderStatusUpdateCallback(mContext,orderid,ORDER_STATUS_CANCEL_REQUEST);//Callback(mContext);
        Bundle b = new Bundle();

        //b.putString("vendid", vinfo.vId);
        b.putString("orderid", orderid);
        b.putInt("status", ORDER_STATUS_CANCEL_REQUEST);
        b.putString("description", "");
        b.putString("pktheader", jsonString);
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }*/
    void removeOrder(Or2goOrderInfo ordr)
    {
        //remove form database
        mOrderList.remove(ordr);
    }

}
