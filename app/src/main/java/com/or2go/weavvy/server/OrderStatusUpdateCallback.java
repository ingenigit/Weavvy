package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.core.OrderHistoryInfo;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OrderStatusUpdateCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    String mOrderId;

    Integer mStatus;
    Integer mPayStatus;

    public OrderStatusUpdateCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }

    public OrderStatusUpdateCallback(Context context, String orderid, Integer status)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();

        mOrderId = orderid;
        mStatus = status;
        mPayStatus = 0;

    }

    public OrderStatusUpdateCallback(Context context, String orderid, Integer status, Integer paysts)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();

        mOrderId = orderid;
        mStatus = status;
        mPayStatus = paysts;

    }

    public void setOrderId(String id)
    {
        mOrderId = id;
    }


    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        gAppEnv.getGposLogger().d("Callback Or2Go Order Status Update API result is: " + result+ "   server response="+response);

        if (result > 0)
        {
            Toast.makeText(mContext, "Order Status Changed.", Toast.LENGTH_LONG).show();

            try {
                JSONArray JsonResponse = new JSONArray(response);

                JSONObject resultobject = JsonResponse.getJSONObject(0);
                JSONObject dataobject = JsonResponse.getJSONObject(1);

                JSONObject resdata = dataobject.getJSONObject("data");

                String orderid = resdata.getString("orderid");
                Integer ordstatus = resdata.getInt("orderstatus");
                String  desc = resdata.getString("description");
                gAppEnv.getGposLogger().d("Status Update Callback :  OrderId : " + mOrderId+ "  status="+ordstatus);

                if (ordstatus == 21){
                    OrderHistoryInfo historyInfo = gAppEnv.getOrderHistoryManager().getHistoryById(mOrderId);
                    if (historyInfo != null)
                        historyInfo.setStatus(ordstatus);

                }
                gAppEnv.getOrderManager().updateOrderStatus(mOrderId, ordstatus,desc);

                //if (mPayStatus != 0) gAppEnv.getOrderManager().updatePayStatus(mOrderId, mPayStatus);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else
        {
            Toast.makeText(mContext, "Login Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected OrderStatusUpdateCallback(Parcel in) {
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
            return new OrderStatusUpdateCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
