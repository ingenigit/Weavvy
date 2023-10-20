package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserRegistrationCallback extends CommApiCallback implements Parcelable {

    private Context mContext;
    AppEnv gAppEnv;
    String custid, name, email, password, place ,addr;
    Integer regStatus=0;

    public UserRegistrationCallback(Context context, AppEnv appenv, String sid, String sname, String smail, String passwd) {
        mContext = context;
        gAppEnv = appenv;
        custid = sid;
        name = sname;
        email = smail;
        password = passwd;
        regStatus=0;
    }

    public int getStatus(){ return regStatus;}

    @Override
    public Void call () {
        gAppEnv.getGposLogger().d("Callback Server Login API result is: " + result+ "   server response="+response);

        if (result >0 ) {
            try {
                JSONArray jsonarray = new JSONArray(response.toString());
                JSONObject resultobject = jsonarray.getJSONObject(0);
                String result = resultobject.getString("result");
                gAppEnv.getGposLogger().d("Comm Manager: Signup result" + result);
                if ((result.contains("Ok")) || (result.contains("ok"))) {
                    gAppEnv.gAppSettings.setUserId(custid);
                    gAppEnv.gAppSettings.setUserName(name);
                    gAppEnv.gAppSettings.setUserEmail(email);
                    gAppEnv.gAppSettings.setPassword(password);
                    gAppEnv.gAppSettings.setPropertyBool("Pref_FTU", true);
                    regStatus = 1;
                    gAppEnv.or2goLogin();
                }
                else if (result.contains("User Exist")) {
                    gAppEnv.getGposLogger().d("UserRegistrationCallback: User exists using existing settings..");
                    JSONObject dataobject = jsonarray.getJSONObject(1);
                    JSONObject addrobject = jsonarray.getJSONObject(2);
                    JSONArray dataarr = dataobject.getJSONArray("customerInfo");
                    JSONArray  addrlist = addrobject.getJSONArray("DeliveryAddress");
                    gAppEnv.getGposLogger().d("Comm UserRegistrationCallback: MemberInfo="+dataarr.toString());
                    gAppEnv.getGposLogger().d("Comm UserRegistrationCallback: AddressList="+addrlist.toString());
                    //not allowing to access onetime coupon
                    gAppEnv.gAppSettings.setExistPersonCoupon(true);
                    JSONObject userdata = dataarr.getJSONObject(0);
                    String name = userdata.getString("name");
                    String custid = userdata.getString("mobileno");
                    String email = userdata.getString("emailid");
                    String passwd = userdata.getString("password");
                    gAppEnv.gAppSettings.setUserId(custid);
                    gAppEnv.gAppSettings.setUserName(name);
                    gAppEnv.gAppSettings.setUserEmail(email);
                    gAppEnv.gAppSettings.setPassword(passwd);
                    gAppEnv.gAppSettings.setPropertyBool("Pref_FTU", false);
                    for (int i = 0; i < addrlist.length(); i++) {
                        JSONObject addrlistobject = addrlist.getJSONObject(i);
                        String addrname = addrlistobject.getString("contact_name");
                        String delimobile = addrlistobject.getString("contact_mobile");
                        String deliplace = addrlistobject.getString("contact_location");
                        String delilandmark = addrlistobject.getString("contact_landmark");
                        String deliaddr = addrlistobject.getString("contact_address");
                        String delipin = addrlistobject.getString("contact_pincode");
                        String delilastorder = addrlistobject.getString("datetime");
                        String deliloc = addrlistobject.getString("locality");
                        String desubloc = addrlistobject.getString("sublocality");
                        String degeoloc = addrlistobject.getString("geocoordinates");
                        gAppEnv.getGposLogger().d("UserRegistrationCallback: adding address="+addrname);
                        boolean addraddres = gAppEnv.getDeliveryManager().dbSyncDeliveryAddr(addrname, deliaddr, deliplace, deliloc, desubloc, delilandmark, delipin, delimobile, degeoloc);
                        if (addraddres == false)
                            gAppEnv.getGposLogger().d("UserRegistrationCallback: failed to add existing address");
                        else
                            gAppEnv.getGposLogger().d("UserRegistrationCallback: added existing delivery address");
                    }
                    regStatus = 1;
                    gAppEnv.or2goLogin();
                }
                else if (result.contains("Invalid OTP")) {
                    regStatus = -1;
                }
                else {
                    regStatus = -2;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            regStatus = -3;
            Toast.makeText(mContext, "Registration API Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    protected UserRegistrationCallback(Parcel in) {
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
            return new UserRegistrationCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
