package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.or2go.core.OrderHistoryInfo;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrderHistoryCallback extends CommApiCallback implements Parcelable {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public OrderHistoryCallback(Context context) {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }

    @Override
    public Void call() {
        // Release Sync Semaphore
//        gAppEnv.getCommMgr().releaseApiSyncSem();
        // the result variable is available right away:
        gAppEnv.getGposLogger().i("Active Order History list API result is: " + result+ "   response="+response);
        if (result > 0){

            try {
                JSONArray JsonResponse = new JSONArray(response);
                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");

                if (result.equals("ok")){
                    JSONObject dataobject = JsonResponse.getJSONObject(1);
                    JSONArray arrayData = dataobject.getJSONArray("data");
                    for (int i = 0; i < arrayData.length(); i++){
                        JSONObject childJSONObject = arrayData.getJSONObject(i);
                        String orderid = childJSONObject.getString("orderid");
                        String ordertime = childJSONObject.getString("ordertime");
                        String orderstore = childJSONObject.getString("storeid");
                        Integer orderstatus = childJSONObject.getInt("status");
                        OrderHistoryInfo historyInfo = new OrderHistoryInfo(
                                orderid, orderstore, ordertime
                        );
                        historyInfo.setStatus(orderstatus);
                        gAppEnv.getOrderHistoryManager().addOrderHistoryId(historyInfo);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }else{
            Toast.makeText(mContext, "History list error !!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    protected OrderHistoryCallback(Parcel in) {
        result = in.readInt();
        response = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(result);
        dest.writeString(response);
    }

    public static final Creator<OrderHistoryCallback> CREATOR = new Creator<OrderHistoryCallback>() {
        @Override
        public OrderHistoryCallback createFromParcel(Parcel in) {
            return new OrderHistoryCallback(in);
        }

        @Override
        public OrderHistoryCallback[] newArray(int size) {
            return new OrderHistoryCallback[size];
        }
    };




}
