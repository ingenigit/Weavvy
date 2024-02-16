package com.or2go.vendor.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.core.Or2GoDAInfo;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActiveDAListCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    boolean updateStatus;
    ArrayList<Or2GoDAInfo> mDAList;

    public ActiveDAListCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();

        updateStatus = false;
    }

    public boolean setDataList(ArrayList<Or2GoDAInfo> dalist)
    {
        mDAList = dalist;

        return true;
    }

    public boolean getUpdateStatus() {
        if (updateStatus==true) System.out.println("DA Callback Status is true");
        else
            System.out.println("DA Callback Status is false");
        return updateStatus;}

    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        //gAppEnv.getGposLogger().i("Active DA List API result is: " + result + "   server response=" + response);
        System.out.println("Active DA List API result is: " + result + "   server response=" + response);

        if (result > 0) {
            try {
                //Toast.makeText(mContext, "Loggedin Successfully.", Toast.LENGTH_LONG).show();
                //gAppEnv.ServerLoginStatus((String) response);
                JSONArray JsonResponse = new JSONArray(response);


                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                if (result.equals("ok")) {
                    //gAppEnv.getGposLogger().i("Active Order List API: adding active orders ");
                    JSONObject dataobject = JsonResponse.getJSONObject(1);
                    JSONArray dalist = dataobject.getJSONArray("data");
                    for (int i = 0; i < dalist.length(); i++) {  // **line 2**
                        JSONObject childJSONObject = dalist.getJSONObject(i);
                        System.out.println("  DA Info=" + childJSONObject.toString());
                        String id = childJSONObject.getString("daid");
                        String name = childJSONObject.getString("name");
                        String contact = childJSONObject.getString("contact");
                        String sts = childJSONObject.getString("status");
                        String address = childJSONObject.getString("address");
                        String pin = childJSONObject.getString("pin");
                        String location = childJSONObject.getString("location");

                        Or2GoDAInfo dainfo = new Or2GoDAInfo(id, name, contact, address,pin);
                        dainfo.setLocation(location);
                        mDAList.add(dainfo);

                        //gAppEnv.getDeliveryManager().addDAInfo(dainfo);

                    }
                    updateStatus = true;
                } else {
                    Toast.makeText(mContext, "Non of delivery boys are ACTIVE", Toast.LENGTH_SHORT).show();
                }

                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateStatus = true;

        return null;
    }

    protected ActiveDAListCallback(Parcel in) {
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
            return new ActiveDAListCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
