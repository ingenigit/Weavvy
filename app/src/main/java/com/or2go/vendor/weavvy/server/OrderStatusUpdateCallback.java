package com.or2go.vendor.weavvy.server;

import static com.or2go.core.Or2goConstValues.ORDER_STATUS_READY;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderStatusUpdateCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    AppEnv gAppEnv;
    String mOrderId;
    Integer mStatus;
    boolean mIsOTP=false;

    public OrderStatusUpdateCallback(Context context, String orderid, Integer status) {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
        mOrderId = orderid;
        mStatus = status;
    }

    public void setOrderId(String id)
    {
        mOrderId = id;
    }
    public void setOTPRequired(boolean isotp) {mIsOTP = isotp;}

    @Override
    public Void call () {
        // this is the actual callback function
        // the result variable is available right away:
        Log.d("Callback", "Or2Go Order Confirm API result is: " + result+ "   server response="+response);
        if (result > 0) {
            Toast.makeText(mContext, "Order Status Changed.", Toast.LENGTH_LONG).show();
            try {
                JSONArray JsonResponse = new JSONArray(response);
                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                if (result.equals("ok")) {
                    JSONObject data = JsonResponse.getJSONObject(1);
                    JSONObject dataobject = data.getJSONObject("data");
                    String resordid = dataobject.getString("orderid");
                    Integer resordsts = dataobject.getInt("orderstatus");
                    //JSONObject dataobject = JsonResponse.getJSONObject(1);
                    //gAppEnv.getOr2goManager().getOrder(mOrderId).setStatus(mStatus);
                    if (mIsOTP && (resordsts==ORDER_STATUS_READY)) {
                        String pickupotp =  dataobject.getString("orderotp");
                        gAppEnv.getOrderManager().updateOrderStatusOTP(mOrderId, resordsts, pickupotp);
                    }
                    else {
                        gAppEnv.getOrderManager().updateOrderStatus(mOrderId, resordsts, "");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
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
