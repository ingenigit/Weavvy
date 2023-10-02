package com.or2go.vendor.showstorenearme.storeList;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.vendor.showstorenearme.AppEnv;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StoreInfoCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public StoreInfoCallback(Context mContext) {
        this.mContext = mContext;
        gAppEnv = (AppEnv) mContext;// getApplicationContext();
    }

    @Override
    public Void call() {
        gAppEnv.getCommMgr().releaseApiSyncSem();
        System.out.println(result + "kkmklmk " + response);
        if (result > 0 )
        {
            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(response.toString());


                JSONObject resultobject = jsonarray.getJSONObject(0);

                //tempOTP = response.toString();
                String result = resultobject.getString("result");
                //gAppEnv.getGposLogger().d("Comm Manager: Login result" + result);

                if ((result.contains("Ok")) || (result.contains("ok"))) {
                    //Toast.makeText(mContext, "Loggedin Successfully.", Toast.LENGTH_LONG).show();
                    //gAppEnv.ServerLoginStatus((String) response);

                    JSONObject vendordata = jsonarray.getJSONObject(1);
                    JSONObject dataobject = vendordata.getJSONObject("data");
                    JSONArray storearr = dataobject.getJSONArray("storeinfo");
                    System.out.println("StoreInfoCallBack:  StoreInfo=" +storearr.toString());

                    for (int i = 0; i < storearr.length(); i++) {  // **line 2**
                        JSONObject objstore = storearr.getJSONObject(i);

                        String vid = objstore.getString("storeid");
                        String vname = objstore.getString("storename");
                        String vstoretype = objstore.getString("storetype");
                        String vgeo = objstore.getString("geolocation");
                        String vcont = objstore.getString("contact");

                        if ((vcont == null) || (vcont.equals("null"))) vcont = "";

                        StoreList nstore = new StoreList(vid, vname, vcont, vgeo, false);
                        gAppEnv.getStoreManager().addStoreInfo(nstore);
                    }
                }
                else
                {
                    Toast.makeText(mContext, "Login Error!!! -"+result, Toast.LENGTH_SHORT).show();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else {
            Toast.makeText(mContext, "Vendor List Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    protected StoreInfoCallback(Parcel in) {
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

    public static final Parcelable.Creator<CommApiCallback> CREATOR = new Parcelable.Creator<CommApiCallback>() {
        @Override
        public CommApiCallback createFromParcel(Parcel in) {
            return new StoreInfoCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
