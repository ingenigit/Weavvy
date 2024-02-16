package com.or2go.vendor.weavvy.server;

import static com.or2go.core.Or2goConstValues.OR2GO_LOGIN_STATUS_FAILED;
import static com.or2go.core.Or2goConstValues.OR2GO_LOGIN_STATUS_SUCCESS;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StoreLoginCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    String vendid, storeid;

    Integer regStatus=0;

    //public UserRegistrationCallback(Context context, AppEnv appenv, String sid, String sname, String smail, String splace, String saddr)
    public StoreLoginCallback(Context context, AppEnv appenv, String vid, String sid)
    {
        mContext = context;
        gAppEnv = (AppEnv) appenv;

        vendid = vid;
        storeid = sid;


        regStatus=0;
    }

    public int getStatus(){ return regStatus;}

    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        gAppEnv.getGposLogger().d("Callback Registration API result is: " + result+ "   response="+response);

        if (result >0 )
        {
            //Toast.makeText(mContext, "Loggedin Successfully.", Toast.LENGTH_LONG).show();
            try {
                JSONArray jsonarray = new JSONArray(response.toString());

                JSONObject resultobject = jsonarray.getJSONObject(0);

                //tempOTP = response.toString();
                String result = resultobject.getString("result");
                gAppEnv.getGposLogger().d("Comm Manager: Signup result" + result);

                if ((result.contains("Ok")) || (result.contains("ok"))) {

                    JSONObject dataarr = jsonarray.getJSONObject(1);
                    JSONObject dataobject = dataarr.getJSONObject("data");
                    JSONArray sesarr = dataobject.getJSONArray("sessionid");
                    String sessionid = sesarr.getString(0);

                    gAppEnv.getGposLogger().d("LoginCallback:  session=" +sessionid);

                    //JSONObject infoobject = jsonarray.getJSONObject(2);
                    JSONArray  infoarr = dataobject.getJSONArray("storeinfo");
                    JSONObject objstore= infoarr.getJSONObject(0);

                    gAppEnv.getGposLogger().d("LoginCallback:  store info=" +objstore.toString());


                    /*String vendorid = infodata.getString("vendorid");
                    String storeid = infodata.getString("storeid");
                    String vname = infodata.getString("storename");
                    String vservicetype = infodata.getString("servicetype");
                    String vstoretype = infodata.getString("storetype");
                    //String vdesc = infodata.getString("description");
                    //String tag =   infodata.getString("featured_tags");
                    //String vplace = infodata.getString("city");
                    //String vlocality = infodata.getString("locality");
                    //String vstate = infodata.getString("state");
                    //String vstatus = infodata.getString("salestatus");
                    //String vminord = infodata.getString("minordcost");
                    //String voptime = infodata.getString("working_time");
                    //String vclosed = infodata.getString("closedon");

                    Integer ordrrmgmt = infodata.getInt("manageorder");
                    //Integer ordpayopt = infodata.getInt("payoption");

                    Integer infover = infodata.getInt("infoversion");
                    //Integer pricever = infodata.getInt("pricedbversion");
                    Integer prodbver = infodata.getInt("productdbversion");
                    Integer skuver = infodata.getInt("skudbversion");
                    */

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
                    Integer invopt = objstore.getInt("inventorycontrol");
                    String vcontact = objstore.getString("contact");


                    String vfav = objstore.getString("favfive");
                    String vgeo = objstore.getString("geolocation");

                    gAppEnv.getStoreManager().updateStoreInfo(vid, vname, vtype, vstoretype, vdesc,
                            tag, vpin, vplace, vlocality, vstate, vpin, vstatus, vminord, voptime, vclosed, vfav,
                            vproddbver, infover, skudbver, vgeo, vcontact, payopt, orderopt, invopt);

                    gAppEnv.gAppSettings.setStoreId(storeid);
                    //gAppEnv.gAppSettings.setVendorId(vendorid);
                    //gAppEnv.getVendorManager().setVendorId(vendid);
                    //gAppEnv.getVendorManager().updateVendor(vendinfo);

                    gAppEnv.setSessionId(sessionid);
                    gAppEnv.setOr2goLoginStatus(OR2GO_LOGIN_STATUS_SUCCESS);
                    gAppEnv.setLoginState(true);

                    gAppEnv.getStoreManager().updateStoreDBVersions(storeid, vproddbver, skudbver );


                    /*if (gAppEnv.getStoreManager().getStore(storeid).isDownloadRequired())
                    {
                        Integer downloadtype = gAppEnv.getStoreManager().getStore(storeid).getDownloadDataType();
                        if (downloadtype>0)
                        {
                            System.out.println("Store Data Download Type="+downloadtype);
                            gAppEnv.getDataSyncManager().doDataDownload(gAppEnv.getStoreManager().getStore(storeid), downloadtype);
                            gAppEnv.getStoreManager().getStore(storeid).setDBSate(downloadtype, OR2GO_DBSTATUS_DOWNLOAD_REQ);
                        }
                    }
                    else*/

                    gAppEnv.getStoreManager().syncStoreDB();

                    gAppEnv.postLoginProcess();

                    //regStatus = 1;

                }
                else if (result.contains("Invalid OTP"))
                {
                    regStatus = -1;
                }
                else //any other response is failure...
                {
                    Toast.makeText(mContext, "Id or Password didn't match.", Toast.LENGTH_SHORT).show();
                    gAppEnv.setOr2goLoginStatus(OR2GO_LOGIN_STATUS_FAILED);
                    regStatus = -2;
                }



                /*
                Intent intent = new Intent();
                intent.putExtra("cmdreq", "addaddress");
                intent.setClass(mContext, AppMainActivity.class);
                ((Activity)mContext).startActivity(intent);
                */

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else
        {
            regStatus = -3;
            Toast.makeText(mContext, "Registration API Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected StoreLoginCallback(Parcel in) {
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
            return new StoreLoginCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
