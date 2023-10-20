package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.or2go.core.Or2GoStore;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppInfoCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    boolean isPublicApi=false;

    public AppInfoCallback(Context context) {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
        isPublicApi=false;
    }

    public void setPublic() {
        isPublicApi=true;
    }


    @Override
    public Void call () {
        Log.i("Callback", "App Info API result is: " + result+ "  response="+response);
        if (result >0 ) {
            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(response.toString());
                JSONObject resultobject = jsonarray.getJSONObject(0);
                String result = resultobject.getString("result");
                if ((result.contains("Ok")) || (result.contains("ok"))) {
                    JSONObject vendordata = jsonarray.getJSONObject(1);
                    JSONObject dataobject = vendordata.getJSONObject("data");
                    JSONArray apparr = dataobject.getJSONArray("appinfo");
                    JSONObject appinfo = apparr.getJSONObject(0);
                    String appname = appinfo.getString("appname");
                    String appcontact = appinfo.getString("contact");
                    String appemail = appinfo.getString("emailid");
                    String appdesc = appinfo.getString("description");
                    Integer apptheme = appinfo.getInt("theme");
                    gAppEnv.gAppSettings.setAppName(appname);
                    gAppEnv.gAppSettings.setAppContact(appcontact);
                    gAppEnv.gAppSettings.setAppEmail(appemail);
                    gAppEnv.gAppSettings.setAppDescription(appdesc);
                    gAppEnv.gAppSettings.setAppTheme(apptheme);
                    JSONArray storearr = dataobject.getJSONArray("storeinfo");
                    for (int i = 0; i < storearr.length(); i++) {  // **line 2**
                        JSONObject objstore = storearr.getJSONObject(i);
                        String vid = objstore.getString("storeid");
                        String vname = objstore.getString("storename");
                        String vtype = objstore.getString("servicetype");
                        String vstoretype = objstore.getString("storetype");
                        String vdesc = objstore.getString("description");
                        String tag =   objstore.getString("featured_tags");
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

                        String favlist;
                        if ((vfav == null) || (vfav.equals("null")))
                            favlist = "";
                        else
                            favlist = vfav;

                        if ((vcont == null) || (vcont.equals("null"))) vcont = "";

                        Or2GoStore nstore = new Or2GoStore(vid, vname, vtype, vstoretype, vdesc,
                                tag, "", vplace, vlocality, vstate, vpin,
                                vstatus, vminord, voptime, vclosed,
                                vproddbver, infover, skudbver,
                                vgeo, vcont, payopt, orderopt, invctl);
                        nstore.setFavItems(favlist);
                        gAppEnv.getStoreManager().updateStoreInfo(vid, nstore);
                    }
                    gAppEnv.getStoreManager().downloadStoreData();
                }
                else {
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

    protected AppInfoCallback(Parcel in) {
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
            return new AppInfoCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
