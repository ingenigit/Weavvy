package com.or2go.weavvy.server;

import static com.or2go.core.Or2goConstValues.OR2GO_INIT_COMPLETE;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.core.DiscountInfo;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DiscountInfoCallback extends CommApiCallback implements Parcelable {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public DiscountInfoCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }

    @Override
    public Void call () {
        // Release Sync Semaphore
//        gAppEnv.getCommMgr().releaseApiSyncSem();

        // the result variable is available right away:
        gAppEnv.getGposLogger().i("Discount Info API result is: " + result+ "   server response="+response);

        if (result >0 )
        {
            try {
                //Toast.makeText(mContext, "Loggedin Successfully.", Toast.LENGTH_LONG).show();
                //gAppEnv.ServerLoginStatus((String) response);
                JSONArray JsonResponse = new JSONArray(response);


                JSONObject resultobject = JsonResponse.getJSONObject(0);

                String result = resultobject.getString("result");

                if (result.equals("ok")) {
                    JSONObject responseobject = JsonResponse.getJSONObject(1);

                    //JSONArray orderlist = dataobject.getJSONArray("data");
                    JSONObject dataobject = responseobject.getJSONObject("data");

                    JSONArray discdata = dataobject.getJSONArray("discountids");
                    gAppEnv.getGposLogger().i("Discount  Data: " + discdata.toString());

                    for (int i = 0; i < discdata.length(); i++) {  // **line 2**
                        JSONObject childJSONObject = discdata.getJSONObject(i);

                        //gAppEnv.getGposLogger().i("Discount Object Data: " + childJSONObject.toString());

                        int id = childJSONObject.getInt("id");
                        String storeid = childJSONObject.getString("storeid");
                        String name = childJSONObject.getString("name");
                        String desc = childJSONObject.getString("description");
                        int type = childJSONObject.getInt("type");
                        int scope = childJSONObject.getInt("scope");
                        float amnt = childJSONObject.getLong("amount");
                        int amnttype = childJSONObject.getInt("amounttype");
                        String stdate = childJSONObject.getString("startdate");
                        String enddate = childJSONObject.getString("enddate");
                        int sts = childJSONObject.getInt("status");
                        int usgcnt = childJSONObject.getInt("usagecount");
                        float usgamount = childJSONObject.getLong("usageamount");

                        DiscountInfo discinfo = new DiscountInfo(id, name, desc, type, scope, amnt, amnttype, stdate, enddate, sts,usgcnt, usgamount, storeid);

                        gAppEnv.getDiscountManager().addDiscountInfo(discinfo);

                    }

                    JSONArray discountpropdata = dataobject.getJSONArray("dicountproperty");

                    gAppEnv.getGposLogger().i("Discount Property Data: " + discountpropdata.toString());

                    for (int j = 0; j < discountpropdata.length(); j++) {  // **line 2**
                        JSONObject optionJSONObject = discountpropdata.getJSONObject(j);

                        int id = optionJSONObject.getInt("id");
                        float minval = optionJSONObject.getInt("minvalue");
                        float maxamnt = optionJSONObject.getInt("maxamount");
                        float minitem = optionJSONObject.getInt("minitem");
                        float freeitem = optionJSONObject.getInt("freeitem");
                        int ftu = optionJSONObject.getInt("ftu");
                        int vendonly = optionJSONObject.getInt("vendoronly");
                        String vendid = optionJSONObject.getString("vendorid");

                        gAppEnv.getDiscountManager().addDiscountProperty(id, minval,maxamnt,minitem, freeitem, ftu, vendonly, vendid);

                        //gAppEnv.getGposLogger().i("Delivery Charge Option Array  Data: " + optionJSONObject.toString());


                    }

                    gAppEnv.getDiscountManager().processVendorDiscounts();

                }

                gAppEnv.setAppInitializationStatus(OR2GO_INIT_COMPLETE);

            } catch(JSONException e){
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(mContext, "Discount List Error!!! -"+result, Toast.LENGTH_SHORT).show();

            gAppEnv.setAppInitializationStatus(OR2GO_INIT_COMPLETE);
        }

        return null;
    }

    protected DiscountInfoCallback(Parcel in) {
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
            return new DiscountInfoCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
