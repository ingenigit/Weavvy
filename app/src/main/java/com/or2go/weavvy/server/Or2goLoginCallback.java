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


 public class Or2goLoginCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public Or2goLoginCallback(Context context) {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }

    @Override
    public Void call () {
        gAppEnv.getGposLogger().i( "Server Login API result is: " + result+ "   server response="+response);
        if (result >0 ) {
            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(response.toString());
                JSONObject resultobject = jsonarray.getJSONObject(0);
                String result = resultobject.getString("result");
                gAppEnv.getGposLogger().d("Comm Manager: Login result" + result);
                if ((result.contains("Ok")) || (result.contains("ok"))) {
                    Toast.makeText(mContext, "Loggedin Successfully.", Toast.LENGTH_LONG).show();
                    JSONObject sesobject = jsonarray.getJSONObject(1);
                    JSONArray  sesarr = sesobject.getJSONArray("custsessionid");
                    //JSONObject sesobj = spinfoarr.getJSONObject(0);
                    String session = sesarr.getString(0);
                    gAppEnv.getGposLogger().d("Login Callback: session" + session);
                    gAppEnv.setSessionId(session);
                    gAppEnv.setLoginState(true);
                    gAppEnv.postLoginProcess();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(mContext, "Login Error Retrying login!!! -"+result, Toast.LENGTH_SHORT).show();
            gAppEnv.or2goLogin();
        }
        return null;
    }

    protected Or2goLoginCallback(Parcel in) {
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
            return new Or2goLoginCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
