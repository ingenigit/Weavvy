package com.or2go.vendor.showstorenearme;

import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ONLINE_PAY_COMPLETE;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ONLINE_PREPAY_COMPLETE;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ORDER_CANCEL;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ORDER_PLACE;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_DA_ORDER_ASSIGN_ACCEPT;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_DA_ORDER_ASSIGN_REJECT;
import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_DA_ORDER_DELIVERY_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_STATUS_ASSIGNED;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_STATUS_NONE;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_DELIVERY;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_COD;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderItem;
import com.or2go.core.ProductInfo;
import com.or2go.core.UnitManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Or2goMsgHandler extends Thread{
    public Handler mHandler;
    private Context mContext;
    AppEnv gAppEnv;

    Or2goMsgHandler(Context context) {
        mContext =context;
        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
    }

    @Override
    public void run(){
        Looper.prepare();

        ////Toast.makeText(mContext, "DBSync Thread running", Toast.LENGTH_SHORT).show();
        //Log.i("checkout sale id","sale id tb update status = "+updatevalue);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                Bundle bundle = msg.getData();
                String msgstring = bundle.getString("Or2Go");
                String msgkey = bundle.getString("RoutingKey");
                System.out.println("MessageHandler: message for store..."+msgkey);
                Integer pkttype;
                try {
                    JSONObject jsonPkt = null;
                    JSONObject pktHeader = null;
                    String pktType = "";

                    jsonPkt = new JSONObject(msgstring);

                    pktHeader = jsonPkt.getJSONObject("Header");
                    pkttype = pktHeader.getInt("pktType");
                    Log.i("MsgHandle","message type = "+pkttype);

                    if (pkttype==OR2GO_MSG_CUSTOMER_ORDER_PLACE)//(pktType.equals("Order"))
                    {
//                        processOnlineOrder(jsonPkt);
                        Intent orderIntent = new Intent(mContext, MainActivity.class);
                        String notmsg = " !! New Order !!";
                        gAppEnv.getNotificationManager().setNotification("Order Placed !!", notmsg, orderIntent);
                    }
                    else if (pkttype == OR2GO_MSG_CUSTOMER_ORDER_CANCEL)
                    {
                        Log.i("MsgHandle","Customer Cancel");
                        Integer ordsts =pktHeader.getInt("ordStatus");
                        String ordid = pktHeader.getString("ordId");
                        String desc = pktHeader.getString("ordStatusDescription");
//                        gAppEnv.gOrderMgr.updateOrderStatus(ordid,ordsts, desc);
                    }
                    else if (pkttype==OR2GO_MSG_DA_ORDER_DELIVERY_STATUS_UPDATE){
                        Log.i("MsgHandle","Order Complete");
                        String ordid = pktHeader.getString("ordId");
                        String orsts = jsonPkt.getString("orderstatus");
                        String ordesc = jsonPkt.getString("statusdescription");
//                        gAppEnv.getOrderManager().updateDeliveryStatus(ordid, ORDER_STATUS_COMPLETE);
//                        gAppEnv.gOrderMgr.updateOrderStatus(ordid, Integer.valueOf(orsts), ordesc);
                    }
                    else if (pkttype==OR2GO_MSG_DA_ORDER_ASSIGN_ACCEPT)
                    {
                        Log.i("MsgHandle","DA assign accept");
                        String ordid = pktHeader.getString("ordId");
                        String orsts = pktHeader.getString("ordStatus");
                        String ordesc = pktHeader.getString("ordStatusDescription");
                        String ordaId = pktHeader.getString("daId");
//                        gAppEnv.getOrderManager().updateDeliveryStatus(ordid, OR2GO_DELIVERY_STATUS_ASSIGNED);
//                        gAppEnv.getOrderManager().updateDA(ordid, ordaId);
                        Intent intent = new Intent(mContext, MainActivity.class);
//                        intent.putExtra("orderid", ordid);
//                        intent.putExtra("orderstatus", orsts);
                        String notmsg = "Order ID:" + ordid + " " + ordesc;
                        gAppEnv.getNotificationManager().setNotification("Order Accepted", notmsg, intent);

                    }
                    else if (pkttype==OR2GO_MSG_DA_ORDER_ASSIGN_REJECT)
                    {
                        Log.i("MsgHandle","DA assign reject");
                        String ordid = pktHeader.getString("ordId");
//                        gAppEnv.getOrderManager().updateDeliveryStatus(ordid, OR2GO_DELIVERY_STATUS_NONE);
//                        gAppEnv.getOrderManager().clearDA(ordid);
                        Intent intent = new Intent(mContext, MainActivity.class);
//                        intent.putExtra("orderid", ordid);
                        String notmsg = "Order ID:" + ordid + " DA: " + "Reject Assign Order";
                        gAppEnv.getNotificationManager().setNotification("Order Rejected", notmsg, intent);
                    }
                    else if (pkttype==OR2GO_MSG_CUSTOMER_ONLINE_PAY_COMPLETE)
                    {
                        Log.i("MsgHandle","Pay Complete");
                        String ordid = pktHeader.getString("ordId");
                        Integer paystatus = jsonPkt.getInt("paystatus");
                        Integer paymode = jsonPkt.getInt("paymode");
                        String payinfo = jsonPkt.getString("paymentinfo");
                        //gAppEnv.getOrderManager().updatePaymentStatus(ordid, OR2GO_PAY_STATUS_COMPLETE);
//                        gAppEnv.getOrderManager().updatePaymentStatus(ordid, paystatus, paymode, payinfo);
                    }
                    else if (pkttype==OR2GO_MSG_CUSTOMER_ONLINE_PREPAY_COMPLETE)
                    {
                        Log.i("MsgHandle","Pre Pay Complete");
                        String ordid = pktHeader.getString("ordId");
                        Integer ordstatus = jsonPkt.getInt("orderstatus");
                        Integer paystatus = jsonPkt.getInt("paystatus");
                        Integer paymode = jsonPkt.getInt("paymode");
                        String payinfo = jsonPkt.getString("paymentinfo");
//                        gAppEnv.gOrderMgr.updateOrderStatus(ordid, ordstatus, "");
                        //gAppEnv.getOrderManager().updatePaymentStatus(ordid, OR2GO_PAY_STATUS_COMPLETE);
//                        gAppEnv.getOrderManager().updatePaymentStatus(ordid, paystatus, paymode, payinfo);
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


    ///////Message Handlin gAPI
    private void processOnlineOrder(JSONObject jsonPkt)
    {
        if (jsonPkt == null) return;

        JSONObject pktHeader;

        //int ordid;
        String onlineordid;
        String ordappid;
        Integer ordstatus;
        String ordvendor = "";
        String ordtime = "";
        Integer ordtype;
        String ordsubtotal="";
        String deliverycharge;
        String ordtaxamnt;
        String ordtotal="";
        String orddiscount="";
        String custid="";
        String deliveraddr="";
        String deliveryplace = "";
        String picktime="";
        Integer paymode;
        Integer paystatus;
        String acptcharge = "";
        String custreq = "";
        String ordstoreid = "";

        String ordItemList = "";

        OrderItem orditem;


        try {
            pktHeader= jsonPkt.getJSONObject("Header");

            //get the common fields for all messages
            String msgtype = pktHeader.getString("pktType");//", "Order")
            ordtype = pktHeader.getInt("ordType");
            ordappid = pktHeader.getString("ordAppId");
            custid = pktHeader.getString("ordCustomerId");
            onlineordid = pktHeader.getString("ordId");
            ordstatus = pktHeader.getInt("ordStatus");
            //ordvendor = pktHeader.getString("ordStoreName");
            ordstoreid = pktHeader.getString("ordStoreId");

            System.out.println("Store New Order  Type:" + ((ordtype==OR2GO_ORDERTYPE_DELIVERY)? "Delivery":"Pickup"));

            if (!ordstoreid.isEmpty())
            {
//                ProductManager prmgr = gAppEnv.getStoreManager().getProductManager(ordstoreid);

                ordtime = pktHeader.getString("ordTime");
                ordsubtotal =  pktHeader.getString("ordSubtotal");
                ordsubtotal =  pktHeader.getString("ordDiscount");
                deliverycharge = pktHeader.getString("ordDeliveryCharge");
                ordtaxamnt =  pktHeader.getString("ordTaxAmount");
                ordtotal = pktHeader.getString("ordGrandTotal");
                orddiscount = pktHeader.getString("ordDiscount");
                deliveraddr = pktHeader.getString("ordDeliveryAddress");
                deliveryplace = pktHeader.getString("ordDeliveryPlace");

                ordItemList = pktHeader.getString("ordItemList");

                picktime = pktHeader.getString("ordRequestTime");
                //paystatus = pktHeader.getInt("ordPaymentMode");
                custreq = pktHeader.getString("ordCustRequest");
                //acptcharge = pktHeader.getString("ordAgreeDeyr5v mliveryCharge");
                //paymode = pktHeader.getInt("ordPaymentMode");

                ///Or2goInfo(String id, String cust, String vend, String type, String status, String time, String total, String addr, String paymode)
                boolean bacpt = false;
                if (acptcharge.equals("true")) bacpt = true;
                else if (acptcharge.equals("false")) bacpt = false;

                Or2goOrderInfo ooinfo = new Or2goOrderInfo(onlineordid, ordtype, ordstoreid, custid,  ordstatus, ordtime,
                        ordsubtotal, deliverycharge, ordtotal,orddiscount,
                        deliveraddr, deliveryplace, OR2GO_PAY_MODE_COD, custreq);
                ooinfo.setAppId(ordappid);
//                ooinfo.oStorePayOption = gAppEnv.getStoreManager().getStore(ordstoreid).getPayOption();

                JSONArray orderDetails = new JSONArray(ordItemList);//jsonPkt.getJSONArray("OrderDetails");

                for (int i = 0; i < orderDetails.length(); i++) {  // **line 2**
                    JSONObject childJSONObject = orderDetails.getJSONObject(i);
                    int itemid = childJSONObject.getInt("itemid");
                    String itemname = childJSONObject.getString("itemname");
                    String itemprice = childJSONObject.getString("price");
                    String itemqnty = childJSONObject.getString("quantity");
                    Integer orderunit = childJSONObject.getInt("unit");
                    //Integer priceid = childJSONObject.getInt("priceid");
                    Integer skuid = childJSONObject.getInt("skuid");

                    //System.out.println("Tablor Client Order : id"+itemid+"  item"+itemname+"  Qunatity"+itemqnty);

                    //orderDetailsItem orditem = new orderDetailsItem(itemid, item_name, itemprice, priceunit, itemqnty, orderunit);
//                    ProductInfo iteminfo = prmgr.getProductInfo(itemid);
//                    //System.out.println("Tablor Client Order : id"+itemid+"  item price"+iteminfo.price);
//
//                    if (iteminfo != null) {
//                        orditem = new OrderItem(itemid, itemname, Float.parseFloat(itemprice),Float.parseFloat(itemqnty),
//                                orderunit, skuid);
//
//                        ooinfo.addOrderItem(orditem);
//                        orditem.setProductInfo(iteminfo);
//
//                    }



                }

                boolean res = true;
//                boolean res = gAppEnv.getOrderManager().addOrder(ooinfo);
                if (res) {
                    try {
                        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100000);
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 10000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    Intent orderIntent = new Intent(mContext, MainActivity.class);
                    //orderIntent.putExtra("orderid", Integer.parseInt(ordid));
                    String notmsg = " !! New Order !!";
                    gAppEnv.getNotificationManager().setNotification("Order Placed !!", notmsg, orderIntent);
                }
            }
	 		   /*
	 		   else if (msgtype.equals("OrderStatusUpdate"))
	 		   {

	 			   SaleOrder saleod =  gAppEnv.getOrderManager().GetOrderByOr2goId(onlineordid);
	 			  Or2goInfo ooinfo = saleod.getOr2goInfo();
	 			  ooinfo.setStatus(ordstatus);

	 		   }*/



        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
