package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderItem;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.manager.ProductManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActiveOrderCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public ActiveOrderCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }


    @Override
    public Void call () {
        // Release Sync Semaphore
//        gAppEnv.getCommMgr().releaseApiSyncSem();

        // the result variable is available right away:
        gAppEnv.getGposLogger().i("Active Order List API result is: " + result+ "   server response="+response);
        if (result >0 )
        {
            try {
                //Toast.makeText(mContext, "Loggedin Successfully.", Toast.LENGTH_LONG).show();
                //gAppEnv.ServerLoginStatus((String) response);
                JSONArray JsonResponse = new JSONArray(response);


                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                if (result.equals("ok")) {


                    gAppEnv.getGposLogger().i("Active Order List API: adding active orders ");
                    JSONObject dataobject = JsonResponse.getJSONObject(1);

                    JSONArray orderlist = dataobject.getJSONArray("data");

                    for (int i = 0; i < orderlist.length(); i++) {  // **line 2**
                        JSONObject childJSONObject = orderlist.getJSONObject(i);

                        String orderid = childJSONObject.getString("orderid");
                        String ordertime = childJSONObject.getString("ordertime");
                        Integer status = childJSONObject.getInt("status");
                        Integer type = childJSONObject.getInt("type");
                        String customerid = childJSONObject.getString("customerid");
                        String storeid = childJSONObject.getString("storeid");
                        String subtotal = childJSONObject.getString("subtotal");
                        String total = childJSONObject.getString("grandtotal");
                        String discount = childJSONObject.getString("discount");
                        String charge = childJSONObject.getString("deliverycharge");
                        String tax = childJSONObject.getString("tax");
                        //String sacpt = childJSONObject.getString("acceptcharge");
                        ///String place = childJSONObject.getString("place");
                        String address = childJSONObject.getString("address");
                        String custreq = childJSONObject.getString("custreq");
                        String picktime = childJSONObject.getString("reqtime");
                        Integer paymode = childJSONObject.getInt("paymode");
                        Integer paystatus = childJSONObject.getInt("paystatus");

                        //String feedback = childJSONObject.getString("feedback");
                        //String rating = childJSONObject.getString("rating");
                        String payid = childJSONObject.getString("payid");
                        String daid = childJSONObject.getString("daid");
                        //String daname = childJSONObject.getString("daname");
                        //String dacontact = childJSONObject.getString("dacontact");

                        String pickupotp = childJSONObject.getString("orderotp");

                        //boolean accptcharge = ((sacpt.equals("true")) ? true : false);

                        ///TBF
                       // String vendorid = "";//gAppEnv.getVendorManager().getVendorInfoByName(vendorname).getId();

                        /*if (storeid!= null) {
                            Or2goOrderInfo ordinfo = new Or2goOrderInfo(orderid, type, vendorid, status, ordertime,
                                    subtotal, charge, total, discount, address, place, paymode, custreq, accptcharge);
                            ordinfo.setTax(tax);
                            ordinfo.setVendorName(vendorname);
                            ordinfo.setPayStatus(paystatus);
                            ordinfo.setDAId(daid);
                            ordinfo.setDAName(daname);
                            ordinfo.setDAContact(dacontact);

                            gAppEnv.getGposLogger().i("Active Order List API: adding order #"+orderid+ " cust req="+custreq);
                            gAppEnv.getOrderManager().addActiveOrder(ordinfo);
                        }*/

                        String itemlist = childJSONObject.getString("itemlist");

                        //boolean accptcharge = ((sacpt.equals("true")) ? true : false);

                        //String vendorid = gAppEnv.getStoreManager().getS().getId();
                        if (status < 20)
                        {
                            Or2goOrderInfo ordinfo = new Or2goOrderInfo(orderid, type, storeid, "", status, ordertime,
                                    subtotal, charge, total, discount, address, "", 0, custreq);
                            ordinfo.setTax(tax);
                            //ordinfo.setVendorName(vendorname);
                            ordinfo.setPayStatus(paystatus);
                            ordinfo.setPickupOTP(pickupotp);
                            ordinfo.setDAId(daid);
                            //ordinfo.setDAName(daname);
                            //ordinfo.setDAContact(dacontact);

                            //Item List
                            JSONArray orderDetails = new JSONArray(itemlist);//jsonPkt.getJSONArray("OrderDetails");

                            for (int j = 0; j < orderDetails.length(); j++) {  // **line 2**
                                JSONObject orderobject = orderDetails.getJSONObject(j);
                                System.out.println("ClientRecv Json Order Item: " + childJSONObject.toString());
                                int itemid = orderobject.getInt("itemid");
                                String itemname = orderobject.getString("itemname");
                                String itemprice = orderobject.getString("price");
                                String itemqnty = orderobject.getString("quantity");
                                Integer orderunit = orderobject.getInt("unit");
                                //Integer priceid = orderobject.getInt("priceid");
                                Integer skuid = orderobject.getInt("skuid");

                                OrderItem orditem = new OrderItem(itemid, itemname, Float.parseFloat(itemprice), Float.parseFloat(itemqnty),
                                            orderunit, skuid);

                                ProductManager productmgr = gAppEnv.getStoreManager().getProductManager(storeid);
                                orditem.setProductInfo(productmgr.getProductInfo(itemid));
                                ordinfo.addOrderItem(orditem);

                            }

                            //gAppEnv.getGposLogger().i("Active Order List API: adding order #" + orderid + " cust req=" + custreq);
                            gAppEnv.getOrderManager().addActiveOrder(ordinfo);//addOrder(ordinfo);

                        }

                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(mContext, "Vendor List Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected ActiveOrderCallback(Parcel in) {
        result = in.readInt();
        response = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result);
        dest.writeString(response);
    }

    @SuppressWarnings("unused")
    public static final Creator<CommApiCallback> CREATOR = new Creator<CommApiCallback>() {
        @Override
        public CommApiCallback createFromParcel(Parcel in) {
            return new ActiveOrderCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
