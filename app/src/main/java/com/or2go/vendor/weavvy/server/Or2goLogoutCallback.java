package com.or2go.vendor.weavvy.server;

import static com.or2go.core.Or2goConstValues.OR2GO_LOGIN_STATUS_NONE;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Or2goLogoutCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public Or2goLogoutCallback(Context context) {
        mContext = context;
        gAppEnv = (AppEnv) context;
    }

    protected Or2goLogoutCallback(Parcel in) {
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

    @Override
    public Void call() {
        gAppEnv.getGposLogger().i("Server logout result is: " + result + " server response: " + response);
        if (result > 0){
            try {
                JSONArray jsonArrayResponse = new JSONArray(response);
                JSONObject jsonObject = jsonArrayResponse.getJSONObject(0);
                String result = jsonObject.getString("result");
                if (result.contains("Success")){
                    Toast.makeText(mContext, "Logout Successfully", Toast.LENGTH_SHORT).show();
                    gAppEnv.setOr2goLoginStatus(OR2GO_LOGIN_STATUS_NONE);
                }else{
                    Toast.makeText(mContext, "Logout Error!!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(mContext, "Logout Error!!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static final Creator<Or2goLogoutCallback> CREATOR = new Creator<Or2goLogoutCallback>() {
        @Override
        public Or2goLogoutCallback createFromParcel(Parcel in) {
            return new Or2goLogoutCallback(in);
        }

        @Override
        public Or2goLogoutCallback[] newArray(int size) {
            return new Or2goLogoutCallback[size];
        }
    };
}
