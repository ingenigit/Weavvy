package com.or2go.weavvy.server;

import static com.or2go.weavvy.manager.OrderCartManager.CART_STOCK_CHECK_COMPLETE;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class OrderRequestCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public OrderRequestCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }


    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        gAppEnv.getGposLogger().i( "Order Request API result is: " + result+ "   server response="+response);

        if (result >0 )
        {
            try {

                JSONArray JsonResponse = new JSONArray(response);

                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                if (result.equals("ok")){
                    JSONObject dataobject = JsonResponse.getJSONObject(1);
                    JSONObject res = dataobject.getJSONObject("data");
                    String orderid = res.getString("orderid");
                    String reqid = res.getString("requestid");
                    String ordtime = res.getString("ordTime");
                    Integer ordsts = res.getInt("ordStatus");
                    gAppEnv.getGposLogger().i( "Order Request orderid:" + orderid+ "   requestid="+reqid+ " order time"+ordtime);

                    //gAppEnv.getOrderManager().updateOrderId(Integer.parseInt(reqid),orderid, ordtime);
                    gAppEnv.getCartManager().completeCartOrder(Integer.parseInt(reqid),orderid, ordsts, ordtime);

                    //gAppEnv.getVendorManager().reqNextVendorProducts();
                }
                else if(result.equals("Error")){
                    JSONObject dataobject = JsonResponse.getJSONObject(1);
                    JSONArray resData = dataobject.getJSONArray("data");
                    for(int i = 0; i < resData.length(); i++){
                        JSONArray array = resData.getJSONArray(i);
                        JSONObject object = array.getJSONObject(0);
                        Integer skuid = object.getInt("skuid");
                        Integer stkvlu = object.getInt("stockval");
                    }
                    gAppEnv.getCartManager().setStockCheckStatus(CART_STOCK_CHECK_COMPLETE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(mContext, "Order Request Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected OrderRequestCallback(Parcel in) {
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
            return new OrderRequestCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
