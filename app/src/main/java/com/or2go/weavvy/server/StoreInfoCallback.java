package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.or2go.core.Or2GoStore;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StoreInfoCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    String mVendorId;

    public StoreInfoCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }

    public void setVendorId(String vendid)
    {
        mVendorId =vendid;
    }

    @Override
    public Void call () {

        // Release API call semaphore
//        gAppEnv.getCommMgr().releaseApiSyncSem();

        // the result variable is available right away:
        gAppEnv.getGposLogger().i( "StoreInfo API result is: " + result+ "   server response="+response);

        if (result >0 )
        {
            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(response.toString());


                JSONObject resultobject = jsonarray.getJSONObject(0);

                //tempOTP = response.toString();
                String result = resultobject.getString("result");

                if ((result.contains("Ok")) || (result.contains("ok"))) {

                    JSONObject spinfoobject = jsonarray.getJSONObject(1);
                    JSONArray dataarray = spinfoobject.getJSONArray("data");
                    JSONObject vendinfoobj = dataarray.getJSONObject(0);

                    String storeid = vendinfoobj.getString("storeid");
                    String vname = vendinfoobj.getString("storename");
                    String vservicetype = vendinfoobj.getString("servicetype");
                    String vstoretype = vendinfoobj.getString("storetype");
                    String vdesc = vendinfoobj.getString("description");
                    String tag =   vendinfoobj.getString("featured_tags");
                    String vplace = vendinfoobj.getString("city");
                    String vlocality = vendinfoobj.getString("locality");
                    String vstate = vendinfoobj.getString("state");
                    String vpin = vendinfoobj.getString("pincode");
                    Integer vstatus = vendinfoobj.getInt("salestatus");
                    String vminord = vendinfoobj.getString("minordcost");
                    String voptime = vendinfoobj.getString("working_time");
                    String vclosed = vendinfoobj.getString("closedon");

                    Integer ordrrmgmt = vendinfoobj.getInt("manageorder");
                    Integer ordpayopt = vendinfoobj.getInt("payoption");
                    Integer invopt = vendinfoobj.getInt("inventorycontrol");

                    Integer infover = vendinfoobj.getInt("infoversion");
                    Integer pricever = vendinfoobj.getInt("pricedbversion");
                    Integer prodbver = vendinfoobj.getInt("productdbversion");
                    Integer skuver = vendinfoobj.getInt("skudbversion");
                    String vgeolocation = vendinfoobj.getString("geolocation");
                    String vcontact = vendinfoobj.getString("contact");

                    String shutfrom = vendinfoobj.getString("closedfrom");
                    String shuttill = vendinfoobj.getString("closedtill");
                    //String shutres = childJSONObject.getString("storelogo");
                    //Integer shutype = childJSONObject.getInt("dbversion");

                    String favitems = vendinfoobj.getString("favfive");
                    if (vclosed.equals("null"))
                        vclosed = "";

                    //System.out.println("Vendor List Callback :  Vendor Logo : " +vlogopath);

                    String vaddr = vlocality + " , " + vplace;


                    //Or2goVendorInfo(String id, String name, String type, String desc, String addr, String place, String locality, String otime, String ctime)
                    Or2GoStore storeinfo = new Or2GoStore(storeid, vname, vservicetype, vstoretype, vdesc, tag,
                                    vaddr, vplace, vlocality, vstate, vpin,
                            vstatus,vminord, voptime, vclosed,
                            prodbver,infover, skuver, vgeolocation, vcontact, ordpayopt, ordrrmgmt, invopt);
                    storeinfo.setShutdownInfo(shutfrom,shuttill,"",0);
                    //vendinfo.getInfoDBState().setVersion(infover);
                    //storeinfo.setOrderControl(ordrrmgmt);
                    //storeinfo.setPayOption(ordpayopt);
                    if ((!favitems.equals("null")) && (!favitems.isEmpty())) {
                        storeinfo.setFavItems(favitems);
                        storeinfo.processFavItemsList();
                        ///processFavItemsList(storeinfo.getFavItems());
                    }

                    ///TBF
                    gAppEnv.getGposLogger().i( "StoreInfo API Callback: " + "   calling downloadStoreInfoDone for storeid="+mVendorId);
                    gAppEnv.getStoreManager().downloadStoreInfoDone(storeid, storeinfo);
                    ///gAppEnv.getVendorManager().downloadStoreInfo();  //called inside downloadStoreInfoDone function

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else
        {

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

    @SuppressWarnings("unused")
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

    //test
    void processFavItemsList(String favlist)
    {
        if ((favlist.equals("null")) || favlist.isEmpty()) return;

        ArrayList<Integer> inttucker = new ArrayList<Integer>();

        String rawlist1 = favlist.replace("[","");
        String rawlist2 = rawlist1.replace("]","");

        String fitems[] = rawlist2.split(",");

        int len = fitems.length;
        for(int i=0; i<len;i++)
        {
            inttucker.add(Integer.parseInt(fitems[i]));
        }
    }

}
