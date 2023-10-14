package com.or2go.weavvy.manager;

import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ONLINE_PAY_COMPLETE;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ONLINE_PREPAY_COMPLETE;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ORDER_CANCEL;
import static com.or2go.core.Or2goConstValues.OR2GO_ACTIVE_ORDER_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_ASYNC_API;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_SYNC_API;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ONLINE_PAYMENT_COMPLETE;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ONLINE_PAYMENT_FAILURE;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_ORDER_CANCEL;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_PAYMENT_COMPLETE_COND_PREPAY;
import static com.or2go.core.Or2goConstValues.OR2GO_IMAGE_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_DELIVERY;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_RATING;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAYMENT_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_MODE_ONLINE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_FAILED_ONLINE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_ONLINE_COMPLETE_APP;
import static com.or2go.core.Or2goConstValues.OR2GO_PREPAYMENT_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_QUICK_ORDER_CONFIRM;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_ACCEPT_CHARGE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CONFIRMED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CONFIRM_COND_PREPAYMENT;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CONFIRM_REQUEST;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_DECLINE_CHARGE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_ON_DELIVERY;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.or2go.core.CartItem;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderDeliveryInfo;
import com.or2go.core.OrderItem;
import com.or2go.core.ProductInfo;
import com.or2go.core.UnitManager;
import com.or2go.mylibrary.OrderDeliveryInfoDBHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.server.ActiveOrderCallback;
import com.or2go.weavvy.server.OrderStatusUpdateCallback;
import com.or2go.weavvy.server.PaymentStatusUpdateCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class OrderManager {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    private ArrayList<Or2goOrderInfo> gOrderList;
    LocalBroadcastManager localBroadcastManager;
    Intent ordintent;
    OrderDeliveryInfoDBHelper mOrderDeliInfoDB;
    UnitManager mUnitMgr = new UnitManager();

    public OrderManager(Context context) {
        mContext =context;
        //Get global application
        gAppEnv = (AppEnv)context;
        gOrderList = new ArrayList <Or2goOrderInfo>();
        mOrderDeliInfoDB = new OrderDeliveryInfoDBHelper(mContext);
        mOrderDeliInfoDB.InitDB();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        ordintent=new Intent("ORDER_STATUS_UPDATE");
    }

    public ArrayList <Or2goOrderInfo> getOrderList()
    {
        return gOrderList;
    }

    public synchronized boolean addActiveOrder(Or2goOrderInfo ordr) {
        if (ordr == null) System.out.println(" OrderInfo Null...cant be added");

        if (getOrder(ordr.oOr2goId) == null) {
            //check order delivery status
            OrderDeliveryInfo deliinfo = mOrderDeliInfoDB.getOrderDeliveryInfo(ordr.getId());
            if (deliinfo != null) {
                ordr.setDAName(deliinfo.getDAName());
                ordr.setDAContact(deliinfo.getDAContact());
            }
            gOrderList.add(ordr);
        }
        return true;
    }

    private Or2goOrderInfo getOrder(String oOr2goId) {
        int pendordsz = gOrderList.size();
        for(int i=0;i<pendordsz;i++) {
            Or2goOrderInfo ordinfo =  gOrderList.get(i);
            if (ordinfo.oOr2goId.equals(oOr2goId))
                return ordinfo;
        }
        return null;
    }

    public synchronized boolean addCartOrder(Or2goOrderInfo ordr, ArrayList<CartItem> orderList) {
        int itemcnt = orderList.size();
        ProductManager prdMgr = gAppEnv.getStoreManager().getProductManager(ordr.oStoreId);
        ordr.clearItemList();
        for(int i=0;i<itemcnt;i++) {
            CartItem citem = orderList.get(i);
            OrderItem oitem = new OrderItem(citem.getId(), citem.getName(),
                                            citem.getPrice(), citem.getQntyVal(),
                                            citem.getOrderUnit(), citem.getSKUId());
            ProductInfo prdInfo = prdMgr.getProductInfo(citem.getId());
            if (prdInfo == null) System.out.println(" ProductInfo Null...id="+citem.getId());
            oitem.setProductInfo(prdInfo);
            ordr.addOrderItem(oitem);
        }
        gOrderList.add(0,ordr);
        return true;
    }

    public Or2goOrderInfo findOrder(String orderid) {
        int n;
        Or2goOrderInfo ordr;
        for (n=0;n < gOrderList.size();n++) {
            ordr = gOrderList.get(n);
            if (ordr.oOr2goId.equals(orderid))
                return ordr;
        }
        return null;
    }

    public void removeOrder(Or2goOrderInfo ordr) {
        //Check order type and order status
        if ((ordr.getType()==OR2GO_ORDERTYPE_DELIVERY) && (ordr.getStatus() == ORDER_STATUS_ON_DELIVERY))
            mOrderDeliInfoDB.deleteDeliveryInfo(ordr.getId());
        gOrderList.remove(ordr);
    }

    public boolean completeOrder(Or2goOrderInfo order) {
        gAppEnv.getOrderHistoryManager().saveCompletedOrder(order);
        removeOrder(order);
        return true;
    }

    public int getOrderCount()
    {
        return gOrderList.size();
    }

    public boolean updateOrderStatus(String ordid, Integer status, String desc) {
        int olistsz = gOrderList.size();
        for(int i=0; i< olistsz; i++) {
            Or2goOrderInfo orinfo = gOrderList.get(i);
            if (orinfo.getId().equals(ordid)) {
                gAppEnv.getGposLogger().d("Order Manager: Updating order id:"+ordid+" Status:"+status);
                orinfo.setStatus(status);
                orinfo.setStatusDescription(desc);
                ordintent.putExtra("update", "status");
                localBroadcastManager.sendBroadcast(ordintent);
                return true;
            }
        }
        return false;
    }

    public boolean updatePayStatus(String ordid, Integer status) {
        gAppEnv.getGposLogger().d("Order Manager: Updating order id:"+ordid+" Status:"+status);
        int olistsz = gOrderList.size();
        for(int i=0; i< olistsz; i++) {
            Or2goOrderInfo orinfo = gOrderList.get(i);
            if (orinfo.getId().equals(ordid)) {
                gAppEnv.getGposLogger().d("Order Manager: found order to update pay status");
                orinfo.setPayStatus(status);
                ordintent.putExtra("update", "paystatus");
                localBroadcastManager.sendBroadcast(ordintent);
                return true;
            }
        }
        return false;
    }

    public boolean isOrderItemsStockAvailable(String orderid) {
        Or2goOrderInfo qorderinfo = findOrder(orderid) ;
        ArrayList<OrderItem> itemList = qorderinfo.getItemList();
        int icnt = itemList.size();
        for(int i=0; i<icnt;i++) {
            OrderItem item = itemList.get(i);
            Log.i("OrderManager" , "Item="+item.getName()+" Qnty ="+item.getQntyVal()+"Stock="+item.getCurStock());
            if (item.getInvControl()>0) {
                if ((item.getCurStock() == 0) || (item.getCurStock() < item.getQntyVal()))
                    return false;
            }
        }
        return true;
    }

    public void boradcastStatusUpdate()
    {
        localBroadcastManager.sendBroadcast(ordintent);
    }

    public boolean getActiveOrders() {
        Message msg = new Message();
        msg.what = OR2GO_ACTIVE_ORDER_LIST;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_SYNC_API;

        ActiveOrderCallback logincb = new ActiveOrderCallback(mContext);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", logincb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean cancelOrder(String orderid, String vId) {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", OR2GO_MSG_CUSTOMER_ORDER_CANCEL);
            pktHeader.accumulate("ordCustomerId", gAppEnv.gAppSettings.getUserId());
            pktHeader.accumulate("ordAppId", BuildConfig.OR2GO_APPID);
            pktHeader.accumulate("ordStoreId", vId);
            pktHeader.accumulate("ordStatus", ORDER_STATUS_CANCELLED);
            pktHeader.accumulate("ordStatusDescription", "Order Canceled.");
            pktHeader.accumulate("ordId", orderid);
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        OrderStatusUpdateCallback ordercb = new OrderStatusUpdateCallback(mContext,orderid,ORDER_STATUS_CANCELLED);//Callback(mContext);
        Bundle b = new Bundle();

        //b.putString("vendid", vinfo.vId);
        b.putString("orderid", orderid);
        b.putString("storeid", vId);
        b.putString("customerid", gAppEnv.gAppSettings.getUserId());
        b.putInt("orderevent", OR2GO_EVENT_ORDER_CANCEL);
        b.putInt("status", ORDER_STATUS_CANCELLED);
        b.putString("description", " Order Cancel");
        b.putString("pktheader", jsonString);
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }


    public boolean orderRespondDeliveryCharge(String orderid, boolean acceptcharge) {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        Integer orderStatus;
        if (acceptcharge)
            orderStatus = ORDER_STATUS_ACCEPT_CHARGE;//"ORDER_USER_ACCEPT_CHARGE";
        else
            orderStatus = ORDER_STATUS_DECLINE_CHARGE;//"ORDER_USER_DECLINE_CHARGE";
        try {
            pktHeader.accumulate("pktType", "OrderStatusUpdate");
            pktHeader.accumulate("ordCust", gAppEnv.gAppSettings.getUserId());
            pktHeader.accumulate("ordStatus", orderStatus);
            pktHeader.accumulate("ordId", orderid);

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_ORDER_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        OrderStatusUpdateCallback ordercb = new OrderStatusUpdateCallback(mContext,orderid,orderStatus);//Callback(mContext);
        Bundle b = new Bundle();

        //b.putString("vendid", vinfo.vId);
        b.putString("orderid", orderid);
        b.putInt("status", orderStatus);
        b.putString("description", "");
        b.putString("pktheader", jsonString);
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean orderPaymentComplete(String or2goid, Integer paymode, String paymentid) {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", "PaymentStatusUpdate");
            pktHeader.accumulate("ordCust", gAppEnv.gAppSettings.getUserId());
            pktHeader.accumulate("ordPayStatus", OR2GO_PAY_STATUS_COMPLETE);
            pktHeader.accumulate("ordPayMode", paymode);
            pktHeader.accumulate("ordPayId", paymentid);
            pktHeader.accumulate("ordId", or2goid);
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_PAYMENT_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        PaymentStatusUpdateCallback ordercb = new PaymentStatusUpdateCallback(mContext,or2goid,OR2GO_PAY_STATUS_COMPLETE);//Callback(mContext);
        Bundle b = new Bundle();

        b.putString("orderid", or2goid);
        b.putInt("paymentstatus", OR2GO_PAY_STATUS_COMPLETE);
        b.putInt("paymode", paymode);
        b.putString("paymentid", paymentid);
        b.putString("pktheader", jsonString);
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean orderPrePaymentComplete(String or2goid, Integer paymode, String paymentid) {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";
        try {
            pktHeader.accumulate("pktType", "PaymentStatusUpdate");
            pktHeader.accumulate("ordCust", gAppEnv.gAppSettings.getUserId());
            pktHeader.accumulate("ordStatus", ORDER_STATUS_CONFIRM_REQUEST);
            pktHeader.accumulate("ordPayStatus", OR2GO_PAY_STATUS_COMPLETE);
            pktHeader.accumulate("ordPayMode", paymode);
            pktHeader.accumulate("ordPayId", paymentid);
            pktHeader.accumulate("ordId", or2goid);
            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_PREPAYMENT_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        OrderStatusUpdateCallback ordercb = new OrderStatusUpdateCallback(mContext,or2goid,ORDER_STATUS_CONFIRM_REQUEST, OR2GO_PAY_STATUS_COMPLETE);//Callback(mContext);
        Bundle b = new Bundle();

        //b.putString("vendid", vinfo.vId);
        b.putString("orderid", or2goid);
        b.putInt("status", ORDER_STATUS_CONFIRM_REQUEST);
        b.putInt("paymentstatus", OR2GO_PAY_STATUS_COMPLETE);
        b.putInt("paymode", paymode);
        b.putString("paymentid", paymentid);
        b.putString("pktheader", jsonString);
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean orderOnlinePaymentComplete(Or2goOrderInfo ordinfo, String info)
    {
        boolean res;
        ordinfo.setPayStatus(OR2GO_PAY_STATUS_ONLINE_COMPLETE_APP);
        if (ordinfo.getStatus() == ORDER_STATUS_CONFIRM_COND_PREPAYMENT)
            res =orderPaymentstatusUpdate(ordinfo.getId(), ordinfo.getStatus(), OR2GO_EVENT_PAYMENT_COMPLETE_COND_PREPAY, OR2GO_PAY_MODE_MODE_ONLINE, info);
        else
            res = orderPaymentstatusUpdate(ordinfo.getId(), ordinfo.getStatus(), OR2GO_EVENT_ONLINE_PAYMENT_COMPLETE, OR2GO_PAY_MODE_MODE_ONLINE, info);
        return res;
    }

    public boolean orderOnlinePaymentFailure(Or2goOrderInfo ordinfo, String info)
    {
        ordinfo.setPayStatus(OR2GO_PAY_STATUS_FAILED_ONLINE);
        boolean res =orderPaymentstatusUpdate(ordinfo.getId(), ordinfo.getStatus(), OR2GO_EVENT_ONLINE_PAYMENT_FAILURE, OR2GO_PAY_MODE_MODE_ONLINE, info);
        return res;
    }

    public boolean orderPaymentstatusUpdate(String or2goid, Integer ordstatus, Integer payevent, Integer paymode, String paymentinfo)
    {
        JSONObject pktHeader = new JSONObject();
        String jsonString = "";

        try {

            if (ordstatus == ORDER_STATUS_CONFIRM_COND_PREPAYMENT)
                pktHeader.accumulate("pktType", OR2GO_MSG_CUSTOMER_ONLINE_PREPAY_COMPLETE);
            else
                pktHeader.accumulate("pktType", OR2GO_MSG_CUSTOMER_ONLINE_PAY_COMPLETE);
            pktHeader.accumulate("ordCust", gAppEnv.gAppSettings.getUserId());
            pktHeader.accumulate("ordId", or2goid);

            // 4. convert JSONObject to JSON to String
            jsonString = pktHeader.toString();
        }
        catch(Exception e)
        {}

        Message msg = new Message();
        msg.what = OR2GO_PAYMENT_STATUS_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        PaymentStatusUpdateCallback ordercb = new PaymentStatusUpdateCallback(mContext,or2goid,payevent);//Callback(mContext);
        Bundle b = new Bundle();


        //b.putString("vendid", vinfo.vId);
        b.putString("orderid", or2goid);
        b.putInt("orderstatus", ordstatus);
        b.putInt("paymentevent", payevent);
        b.putInt("paymode", paymode);
        b.putString("paymentinfo", paymentinfo);
        b.putString("pktheader", jsonString);
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;

    }

    public boolean postRating(String orderid, String vendorid, String ratingdata)
    {
        Message msg = new Message();
        msg.what = OR2GO_ORDER_RATING;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        //ActiveOrderCallback logincb = new ActiveOrderCallback(mContext);//Callback(mContext);
        Bundle b = new Bundle();
        //b.putParcelable("callback", logincb );
        b.putString("orderid", orderid);
        b.putString("storeid", vendorid);
        b.putString("ratingdata", ratingdata);
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public synchronized Integer addOrderItemDetails(Or2goOrderInfo ordinfo)
    {
        String orderid = ordinfo.getId();
        JSONObject orderPkt = new JSONObject();
        //JSONArray jsonArrPktType = new JSONArray();
        JSONArray jsonArrOrderDetails = new JSONArray();

        try
        {
            JSONObject pktHeader = new JSONObject();

            pktHeader.accumulate("pktType", "OrderItemUpdate");
            pktHeader.accumulate("ordStatus", ORDER_STATUS_CONFIRMED);
            pktHeader.accumulate("ordId", ordinfo.getId());
            pktHeader.accumulate("ordSubtotal", ordinfo.getSubTotal());
            pktHeader.accumulate("ordDiscount", ordinfo.getDiscount());
            pktHeader.accumulate("ordTaxAmount", ordinfo.getTax());
            pktHeader.accumulate("ordGrandTotal", ordinfo.getTotal());


            orderPkt.put("OrderInfo", pktHeader);

            gAppEnv.getGposLogger().d("Order Header Json String: " + pktHeader.toString());
        }

        catch(Exception e)
        {
            gAppEnv.getGposLogger().d("Exception: "+e.getMessage());
        }

        ArrayList<OrderItem> itemlist= ordinfo.getItemList();
        for(int i=0;i<itemlist.size();i++) {

            OrderItem item = itemlist.get(i);
            JSONObject jsonObject = new JSONObject();
            try {
                //gAppEnv.getGposLogger().d("QuickOrderItems  Name:" + item.getName() + " Brand:" + item.getBrandName() + " Packid;" + item.getPackId() + " Pack Type" + item.getPackType());
                jsonObject.accumulate("itemid", item.getId());
                jsonObject.accumulate("itemname", item.getName());
                jsonObject.accumulate("brandname", ""/*item.getBrandName()*/);
                jsonObject.accumulate("price", item.getPrice());
                jsonObject.accumulate("discount", "");
                jsonObject.accumulate("quantity", item.getQnty());
                jsonObject.accumulate("unit", item.getOrderUnit());
                ///jsonObject.accumulate("packtype", item.getPackType());
                jsonObject.accumulate("priceid", item.getSKUId());

                jsonArrOrderDetails.put(jsonObject);

                orderPkt.put("OrderDetails", jsonArrOrderDetails);
            } catch (Exception e) {
                gAppEnv.getGposLogger().d("Exception: " + e.getMessage());
            }
        }

        gAppEnv.getGposLogger().d("Quick Order Items Confirm Json String: " + orderPkt.toString());

        Message msg = new Message();
        msg.what = OR2GO_QUICK_ORDER_CONFIRM;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        OrderStatusUpdateCallback ordercb = new OrderStatusUpdateCallback(mContext,orderid,ORDER_STATUS_CONFIRMED);//Callback(mContext);
        Bundle b = new Bundle();
        //b.putString("orderid", orderid);
        b.putString("pktheader", orderPkt.toString());
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
        return 0;
    }

}
