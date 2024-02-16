package com.or2go.vendor.weavvy.server;

import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_STATUS_ASSIGN_REQUEST;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_STATUS_NONE;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_DA_ASSIGN_CANCEL;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_DA_ASSIGN_REQUEST;

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

public class AssignDACallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    String mOrderId;
    String mDAId;

    Integer mRequestType;
    //Integer mStatus;


    public AssignDACallback(Context context, String orderid, String daid, Integer req)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();

        mOrderId = orderid;
        mDAId = daid;

        mRequestType = req;

    }

    public void setOrderId(String id)
    {
        mOrderId = id;
    }


    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        Log.d("Callback", "Assign DA API result is: " + result+ "   server response="+response);

        if (result > 0)
        {
            Toast.makeText(mContext, "Order Status Changed.", Toast.LENGTH_LONG).show();

            try {
                JSONArray JsonResponse = new JSONArray(response);
                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                if (result.equals("ok")) {

                    if (mRequestType == OR2GO_EVENT_DA_ASSIGN_REQUEST) {
                        gAppEnv.getOrderManager().updateDA(mOrderId, mDAId);
                        gAppEnv.getOrderManager().updateDeliveryStatus(mOrderId, OR2GO_DELIVERY_STATUS_ASSIGN_REQUEST);
                    }
                    else if (mRequestType == OR2GO_EVENT_DA_ASSIGN_CANCEL) {
                        gAppEnv.getOrderManager().clearDA(mOrderId);
                        gAppEnv.getOrderManager().updateDeliveryStatus(mOrderId,OR2GO_DELIVERY_STATUS_NONE);
                    }

                }

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

    protected AssignDACallback(Parcel in) {
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
            return new AssignDACallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
