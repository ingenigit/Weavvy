package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.adapter.Or2goItemListAdapter;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderItem;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderInfoDetailCallback extends CommApiCallback implements Parcelable{

    private Context mContext;
    Or2goOrderInfo mOrderInfo;
    Or2goItemListAdapter mAdapter;

    public OrderInfoDetailCallback(Context context) {
        mContext = context;
    }

    public void setViewAdapter(Or2goOrderInfo mOrderInfos, Or2goItemListAdapter adapter) {
        mAdapter = adapter;
        mOrderInfo = mOrderInfos;
    }

    public boolean getDataStatus() {
        if (mOrderInfo!= null)
            return true;
        else
            return false;
    }

    public Or2goOrderInfo gerOrderInfo()
    {
        return mOrderInfo;
    }


    @Override
    public Void call () {
        System.out.println("result: " + result + "response: " + response);
        if (result >0 ) {
            try {
                JSONArray JsonResponse = new JSONArray(response);
                JSONObject dataobject = JsonResponse.getJSONObject(1);
                JSONArray dataarray = dataobject.getJSONArray("data");
                JSONObject childJSONObject = dataarray.getJSONObject(0);
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
                String address = childJSONObject.getString("address");
                String custreq = childJSONObject.getString("custreq");
                String completetime = childJSONObject.getString("completiontime");
                Integer paymode = childJSONObject.getInt("paymode");
                Integer paystatus = childJSONObject.getInt("paystatus");
                Integer deliverystatus = childJSONObject.getInt("deliverystatus");
                String payid = childJSONObject.getString("payid");
                String daid = childJSONObject.getString("daid");
                ///generate Or2GoOrderInfo from json data
                mOrderInfo = new Or2goOrderInfo(orderid, type, storeid, "",
                        status, ordertime,
                        subtotal, charge, total, discount, address, "",
                        paymode, custreq);
                mOrderInfo.setDeliveryStatus(deliverystatus);
                mOrderInfo.setPayStatus(paystatus);
                mOrderInfo.setTax(tax);
                mOrderInfo.setDAId(daid);
                mOrderInfo.setCompletionTime(completetime);

                String itemlist = childJSONObject.getString("itemlist");
                //Item List
                JSONArray orderDetails = new JSONArray(itemlist);//jsonPkt.getJSONArray("OrderDetails");

                for (int j = 0; j < orderDetails.length(); j++) {  // **line 2**
                    JSONObject orderobject = orderDetails.getJSONObject(j);
                    int itemid = orderobject.getInt("itemid");
                    String itemname = orderobject.getString("itemname");
                    String itembrandname = orderobject.getString("brandname");
                    String itemprice = orderobject.getString("price");
                    String itemdiscount = orderobject.getString("discount");
                    String itemqnty = orderobject.getString("quantity");
                    Integer orderunit = orderobject.getInt("unit");
                    Integer skuid = orderobject.getInt("skuid");
                    OrderItem orditem = new OrderItem(itemid, itemname, Float.parseFloat(itemprice), Float.parseFloat(itemqnty),
                                orderunit, skuid);
                    mOrderInfo.addOrderItem(orditem);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(mContext, "Order History Error on Info!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected OrderInfoDetailCallback(Parcel in) {
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
            return new OrderInfoDetailCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
