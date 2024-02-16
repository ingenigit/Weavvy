package com.or2go.vendor.weavvy.storeList;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.vendor.weavvy.AppEnv;
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
        System.out.println(result + " storeInfoCallback  " + response);
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
                    JSONArray storeobject = dataobject.getJSONArray("storeinfo");

                    for (int i = 0; i < storeobject.length(); i++) {  // **line 2**
                        JSONObject objstore = storeobject.getJSONObject(i);
                        String vid = objstore.getString("vendorid");
                        String sid = objstore.getString("storeid");
                        String vname = objstore.getString("storename");
                        String vtype = objstore.getString("servicetype");
                        String vstoretype = objstore.getString("storetype");
                        String vdesc = objstore.getString("description");
                        String tag =   objstore.getString("featured_tags");
                        Integer rate =   objstore.getInt("rating");
                        String vplace = objstore.getString("city");
                        String vlocality = objstore.getString("locality");
                        String vstate = objstore.getString("state");
                        String vpin = objstore.getString("pincode");
                        Integer vstatus = objstore.getInt("salestatus");
                        String vminord = objstore.getString("minordcost");
                        String voptime = objstore.getString("working_time");
                        String vclosed = objstore.getString("closedon");
                        Integer vproddbver = objstore.getInt("productdbversion");
                        Integer infover = objstore.getInt("infoversion");
                        Integer skudbver = objstore.getInt("skudbversion");
                        String shutfrom = objstore.getString("closedfrom");
                        String shuttill = objstore.getString("closedtill");
                        Integer payopt = objstore.getInt("payoption");
                        Integer orderopt = objstore.getInt("manageorder");
                        String vfav = objstore.getString("favfive");
                        String vgeo = objstore.getString("geolocation");
                        String vcont = objstore.getString("contact");
                        Integer invctl = objstore.getInt("inventorycontrol");

                        if ((vcont == null) || (vcont.equals("null"))) vcont = "";

                        gAppEnv.getStoreManager().updateStoreInfo(vid, vname, vtype, vstoretype, vdesc,
                                tag, vpin, vplace, vlocality, vstate, vpin, vstatus, vminord, voptime, vclosed, vfav,
                                vproddbver, infover, skudbver, vgeo, "", payopt, orderopt, invctl);

                        StoreList nstore = new StoreList(vid, vname, vtype, vcont, vgeo, false);
                        gAppEnv.getStoreManager().addStoreInfo(nstore);
                    }
                    gAppEnv.getCompleteStoreList();
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

    public static final Creator<CommApiCallback> CREATOR = new Creator<CommApiCallback>() {
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
