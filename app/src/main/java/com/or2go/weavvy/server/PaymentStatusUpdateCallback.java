package com.or2go.weavvy.server;

import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_PAYMENT_COMPLETE_COND_PREPAY;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PaymentStatusUpdateCallback extends CommApiCallback implements Parcelable {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    String mOrderId;

    Integer mEvent;

    public PaymentStatusUpdateCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }

    public PaymentStatusUpdateCallback(Context context, String orderid, Integer event)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();

        mOrderId = orderid;
        mEvent = event;

    }

    public void setOrderId(String id)
    {
        mOrderId = id;
    }


    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        gAppEnv.getGposLogger().d("Callback : Or2Go Payment Status Update API result is: " + result+ "   server response="+response);

        if (result > 0)
        {
            Toast.makeText(mContext, "Order Status Changed.", Toast.LENGTH_LONG).show();

            try {
                JSONArray JsonResponse = new JSONArray(response);

                JSONObject resultobject = JsonResponse.getJSONObject(0);
                JSONObject dataobject = JsonResponse.getJSONObject(1);

                JSONObject resdata = dataobject.getJSONObject("data");

                String orderid = resdata.getString("orderid");
                Integer paystatus = resdata.getInt("paystatus");
                Integer ordstatus = resdata.getInt("orderstatus");
                String  desc = resdata.getString("description");
                gAppEnv.getGposLogger().d("Status Update Callback :  OrderId : " + orderid+ "  status="+paystatus);

                gAppEnv.getOrderManager().updatePayStatus(orderid, paystatus);

                if (mEvent==OR2GO_EVENT_PAYMENT_COMPLETE_COND_PREPAY)
                    gAppEnv.getOrderManager().updateOrderStatus(orderid,ordstatus,"");


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

    protected PaymentStatusUpdateCallback(Parcel in) {
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
            return new PaymentStatusUpdateCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
