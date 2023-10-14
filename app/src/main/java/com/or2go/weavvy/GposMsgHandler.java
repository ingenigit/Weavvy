 package com.or2go.weavvy;

import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ORDER_STATUS_UPDATE;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_DA_ORDER_DELIVERY_STATUS_UPDATE;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_STORE_ORDER_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_DELIVERY;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_PICKUP;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CHARGE_CONFIRM_REQUEST;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CONFIRMED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_FORCE_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_PICKED_UP;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_READY;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_REJECTED;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.or2go.core.Or2goOrderInfo;
import com.or2go.mylibrary.OrderDeliveryInfoDBHelper;
import com.or2go.weavvy.activity.OrderDetailsActivity;
import com.or2go.weavvy.activity.OrderHistoryDetailsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 public class GposMsgHandler extends Thread{

    public Handler mHandler;
    private Context mContext;
    Class activity;
    AppEnv gAppEnv;
     OrderDeliveryInfoDBHelper mOrderDeliInfoDB;

    GposMsgHandler(Context context) {
        mContext =context;

        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
        mOrderDeliInfoDB = new OrderDeliveryInfoDBHelper(mContext);
        mOrderDeliInfoDB.InitDB();
    }

    @Override
    public void run(){
        Looper.prepare();

        ////Toast.makeText(mContext, "DBSync Thread running", Toast.LENGTH_SHORT).show();
        //Log.i("checkout sale id","sale id tb update status = "+updatevalue);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Integer msgType=-1;
                Bundle bundle = msg.getData();
                String msgstring = bundle.getString("GPOS");
                gAppEnv.getGposLogger().i("Got Gpos Msg  = "+ msgstring);
                try {
                    JSONObject jsonPkt=null;
                    JSONObject pktHeader=null;
                    //String pktType="";
                        jsonPkt = new JSONObject(msgstring);
                        pktHeader= jsonPkt.getJSONObject("Header");
                        //pktType = pktHeader.getString("pktType");
                        msgType = pktHeader.getInt("pktType");
                    if (msgType==OR2GO_MSG_CUSTOMER_ORDER_STATUS_UPDATE) {
                        Integer ordsts = jsonPkt.getInt("orderstatus");
                        String ordid = pktHeader.getString("ordId");
                        JSONArray itemlist = jsonPkt.getJSONArray("itemdetails");
                        gAppEnv.getGposLogger().i("Order item list= " + itemlist);
                        Intent  orderIntent = new Intent(mContext, OrderDetailsActivity.class);
                        gAppEnv.getGposLogger().i("Order Action Required message ordid= " + ordid);
                        orderIntent.putExtra("orderid", Integer.parseInt(ordid));
                        String notmsg = "Order ID:" + ordid + " " + "Please confimr the order detils.";
                        gAppEnv.gNotificationMgr.setNotification("Order Update", notmsg, orderIntent);
                    }
                    else if (msgType==OR2GO_MSG_STORE_ORDER_STATUS_UPDATE) {
                        //Integer ordsts = pktHeader.getInt("ordStatus");
                        Integer ordsts = jsonPkt.getInt("orderstatus");
                        String ordid = pktHeader.getString("ordId");
                        //String ordreqid =  pktHeader.getString("ordReqId");
                        String ordupdatemsg = pktHeader.getString("ordStatusDescription");
                        String ordrejectreason = null;
                        Or2goOrderInfo ordinfo = gAppEnv.getOrderManager().findOrder(ordid);
                        if (ordinfo!= null) {
                            ordinfo.setStatus(ordsts);
                            ordinfo.setStatusDescription(ordupdatemsg);
                            if (ordsts == ORDER_STATUS_CONFIRMED) {
                                gAppEnv.gAppSettings.setPropertyBool("FTU", false);
                                activity = OrderDetailsActivity.class;
                            }
                            if (ordsts == ORDER_STATUS_REJECTED) {
                                ordrejectreason = pktHeader.getString("messgae");
                                gAppEnv.getGposLogger().i( "Order DA Reject= " + ordrejectreason);
                                activity = OrderHistoryDetailsActivity.class;
                                gAppEnv.getOrderManager().completeOrder(ordinfo);
                                gAppEnv.getOrderHistoryManager().broadcastStatusUpdate();
                            }
                            if (ordsts == ORDER_STATUS_CHARGE_CONFIRM_REQUEST) {
                                String delcharge = pktHeader.getString("ordDeliveryCharge");
                                ordinfo.setDeliveryCharge(delcharge);
                                activity = OrderDetailsActivity.class;
                            }
                            else if (ordsts == ORDER_STATUS_PICKED_UP) {
                                gAppEnv.getGposLogger().i("Order PICKUP Message  ");
                                String orderID = pktHeader.getString("ordId");
                                Integer deliverySts = jsonPkt.getInt("orderstatus");
                                String daID = pktHeader.getString("ordDAId");
                                String daName = pktHeader.getString("ordDAName");
                                String daContact = pktHeader.getString("ordDAContact");
                                ordinfo.setDAName(daName);
                                ordinfo.setDAContact(daContact);
                                ordinfo.setDAId(daID);
                                mOrderDeliInfoDB.insertDeliveryInfo(orderID, deliverySts, daID, daName, daContact, "", "");
                                activity = OrderDetailsActivity.class;
                                //gAppEnv.getGposLogger().i("Order delivery time= " + ordinfo.oDeliveryTime);
                                gAppEnv.getGposLogger().i( "Order DA name= " + ordinfo.getDAName() + " DA Id="+ordinfo.getDAId());
                                gAppEnv.getGposLogger().i( "Order DA contact " + ordinfo.getDAContact());
                            }
                            else if (ordsts == ORDER_STATUS_READY) {
//                                if (ordinfo.oType == OR2GO_ORDERTYPE_PICKUP) {
                                    activity = OrderDetailsActivity.class;
                                    String otp = jsonPkt.getString("orderotp");
                                    gAppEnv.getGposLogger().i( "Pickup Order is READY !!  OTP:" + otp);
                                    ordinfo.setPickupOTP(otp);
//                                }
                            }
                            else if (ordsts == ORDER_STATUS_FORCE_CANCELLED) {
                                //ordinfo.setCancelCode(pktHeader.getInt("cancelcode"));
                                activity = OrderHistoryDetailsActivity.class;
                                ordinfo.setCancelCode(jsonPkt.getInt("cancelcode"));
                                gAppEnv.getOrderManager().completeOrder(ordinfo);
                                gAppEnv.getOrderHistoryManager().broadcastStatusUpdate();
                                //pktHeader= jsonPkt.getJSONObject("Header");
                            }
                            else if (ordsts == ORDER_STATUS_COMPLETE){
                                gAppEnv.getGposLogger().i( "Order statues completed in pickup mode");
                                activity = OrderHistoryDetailsActivity.class;
                                gAppEnv.getOrderManager().completeOrder(ordinfo);
                                gAppEnv.getOrderHistoryManager().broadcastStatusUpdate();
                            }
                            //gAppEnv.getOrderManager().updateOrderStatus(ordid, ordsts);
                            gAppEnv.getOrderManager().boradcastStatusUpdate();
//                            gAppEnv.getOrderHistoryManager().broadcastStatusUpdate();
                            gAppEnv.getGposLogger().i( "Order DA Reject= " + ordrejectreason);
                            //Intent orderIntent = new Intent(mContext, OrderListActivity.class);
                            Intent moveIntent = new Intent(mContext, activity);
                            //orderIntent.putExtra("orderid", Integer.parseInt(ordid));
                            moveIntent.putExtra("orderid", ordid);
                            moveIntent.putExtra("rejres", ordrejectreason); //null
//                            orderIntent.putExtra("rejectreason", ordrejectreason);
                            String notmsg = "Order ID:" + ordid + " " + ordinfo.getNotificationMessage();
                            gAppEnv.gNotificationMgr.setNotification("Order Update", notmsg, moveIntent);
                        }
                    }
                    else if (msgType==OR2GO_MSG_DA_ORDER_DELIVERY_STATUS_UPDATE) {
                        Integer ordsts = jsonPkt.getInt("orderstatus");
                        String ordid = jsonPkt.getString("orderid");
                        String ordupdatemsg = jsonPkt.getString("statusdescription");
                        if (ordsts == ORDER_STATUS_COMPLETE) {
                            Or2goOrderInfo ordinfo = gAppEnv.getOrderManager().findOrder(ordid);
                            if (ordinfo!= null) {
                                ordinfo.setStatus(ordsts);
                                ordinfo.setStatusDescription(ordupdatemsg);
                                //ordinfo.updateOrderStatus();
                                gAppEnv.getOrderManager().boradcastStatusUpdate();
                                gAppEnv.getOrderManager().completeOrder(ordinfo);
                                gAppEnv.getOrderHistoryManager().broadcastStatusUpdate();
                                //Intent orderIntent = new Intent(mContext, OrderListActivity.class);
                                Intent orderIntent = new Intent(mContext, OrderHistoryDetailsActivity.class);
                                //orderIntent.putExtra("orderid", Integer.parseInt(ordid));
                                orderIntent.putExtra("orderid", ordid);
                                String notmsg="";
                                if (ordinfo.getType() ==OR2GO_ORDERTYPE_DELIVERY) {
                                    notmsg = "Order ID: " + ordid + " : is delivered. Please give us your valuable feedback." + ordinfo.getNotificationMessage();
                                }
                                else if (ordinfo.getType() ==OR2GO_ORDERTYPE_PICKUP) {
                                    notmsg = "Order ID: " + ordid + " : is picked up. Please give us your valuable feedback." + ordinfo.getNotificationMessage();
                                }
                                gAppEnv.gNotificationMgr.setNotification("Order"+ordid+" Complete", notmsg, orderIntent);

                            }
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                this.removeMessages(msg.what);
            }
        };
        Looper.loop();
    }

    public Handler getHandler()
    {
        return mHandler;
    }

    public void StopThread() {
        this.interrupt();
        //join();
    }


    private void updatePaymentStatus(JSONObject jsonPkt) throws JSONException {
        if (jsonPkt == null) return;

        JSONObject pktHeader;
        //int ordid;
        String onlineordid;
        Integer ordstatus, ordpaystatus, ordpaymode;
        String custid;
        String  ordpaymentid;
        Or2goOrderInfo orditem;
        try {
            pktHeader= jsonPkt.getJSONObject("Header");
            String ordid = pktHeader.getString("ordId");
            //ordstatus = pktHeader.getInt("ordStatus");
            ordpaystatus = pktHeader.getInt("ordPayStatus");
            ordpaymode =  pktHeader.getInt("ordPayMode");
            ordpaymentid = pktHeader.getString("ordPayId");
            gAppEnv.getGposLogger().i("GposMsgHandler :Order payment status update for id= "+ ordid+ " status="+ordpaystatus);
            gAppEnv.getOrderManager().updatePayStatus(ordid, ordpaystatus);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
