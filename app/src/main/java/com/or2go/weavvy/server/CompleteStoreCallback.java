package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.or2go.core.Or2GoStore;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.model.StoreList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CompleteStoreCallback extends CommApiCallback implements Parcelable {

    Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    String vendId;
    int regStatus = 0;
    ArrayList<StoreList> storeArrayList = new ArrayList<>();

    public CompleteStoreCallback(Context context, AppEnv gAppEnv) {
        this.mContext = context;
        this.gAppEnv = gAppEnv;
    }

    @Override
    public Void call() {
        System.out.println(result + " k145465kml " + response);
        if (result > 0 ){
            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(response.toString());
                JSONObject resultobject = jsonarray.getJSONObject(0);
                String result = resultobject.getString("result");
                if ((result.contains("Ok")) || (result.contains("ok"))) {
                    JSONObject vendordata = jsonarray.getJSONObject(1);
                    JSONArray dataobject = vendordata.getJSONArray("data");
                    for (int i = 0; i < dataobject.length(); i++) {
                        JSONObject objstore = dataobject.getJSONObject(i);
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

                        String favlist;
                        if ((vfav == null) || (vfav.equals("null")))
                            favlist = "";
                        else
                            favlist = vfav;

                        Or2GoStore nstore = new Or2GoStore(sid, vname, vtype, vstoretype, vdesc,
                                tag, "", vplace, vlocality, vstate, vpin,
                                vstatus, vminord, voptime, vclosed,
                                vproddbver, infover, skudbver,
                                vgeo, vcont, payopt, orderopt, invctl);
                        nstore.setFavItems(favlist);
                        gAppEnv.getStoreManager().updateStoreInfo(sid, nstore);
                        gAppEnv.getStoreManager().addStoreData(sid, vname, vtype, vcont, vgeo);
                        StoreList storeList = new StoreList(sid, vname, vtype, vcont, vgeo, false);
                        gAppEnv.getStoreManager().addStoreInfo(storeList);
                    }
//                    gAppEnv.getStoreManager().downloadStoreData();
                }
                else
                    Toast.makeText(mContext, "Login Error!!! -"+result, Toast.LENGTH_SHORT).show();
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    protected CompleteStoreCallback(Parcel in) {
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

    public static final Creator<CommApiCallback> CREATOR = new Creator<CommApiCallback>() {
        @Override
        public CommApiCallback createFromParcel(Parcel in) {
            return new CompleteStoreCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
