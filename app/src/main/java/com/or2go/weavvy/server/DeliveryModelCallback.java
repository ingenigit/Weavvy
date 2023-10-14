package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.core.DeliveryModel;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeliveryModelCallback extends CommApiCallback implements Parcelable {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public DeliveryModelCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }

    @Override
    public Void call () {
        // Release Sync Semaphore
//        gAppEnv.getCommMgr().releaseApiSyncSem();

        // the result variable is available right away:
        gAppEnv.getGposLogger().i( "Delivery Model API result is: " + result+ "   server response="+response);

        if (result >0 )
        {
            try {

                JSONArray JsonResponse = new JSONArray(response);
                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                if (result.equals("ok")) {

                    JSONObject modeldata = JsonResponse.getJSONObject(1);
                    JSONArray  models = modeldata.getJSONArray("locationchargedata");
                    for (int i = 0; i < models.length(); i++) {  // **line 2**
                        JSONObject childJSONObject = models.getJSONObject(i);

                        Integer mid = childJSONObject.getInt("id");
                        String mname = childJSONObject.getString("name");
                        Integer mtype = childJSONObject.getInt("type");
                        String mstate = childJSONObject.getString("state");
                        String mplace = childJSONObject.getString("place");
                        String  param = childJSONObject.getString("parameters");
                        gAppEnv.getGposLogger().i( "Delivery Model ID:" + mid+ " Type="+mtype+" Name="+mname);
                        gAppEnv.getGposLogger().i( "Delivery Model Parameters:" + param);

                        gAppEnv.getDeliveryManager().addDeliveryModel(new DeliveryModel(mid, mtype, mname, mstate, mplace, param));

                    }

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(mContext, "Delivery Model API Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected DeliveryModelCallback(Parcel in) {
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
            return new DeliveryModelCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
