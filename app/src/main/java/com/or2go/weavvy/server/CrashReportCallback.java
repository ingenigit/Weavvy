package com.or2go.weavvy.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.activity.ExceptionHandlerActivity;

public class CrashReportCallback extends CommApiCallback implements Parcelable {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public CrashReportCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
    }


    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        gAppEnv.getGposLogger().i( "CrashReport API result is: " + result+ "   server response="+response);

        {

            PendingIntent myActivity = PendingIntent.getActivity(mContext,
                    192837, new Intent(mContext, ExceptionHandlerActivity.class),
                    PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, myActivity);

            System.exit(2);

        }

        if (result <= 0 )
        {
            Toast.makeText(mContext, "Crash Report Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected CrashReportCallback(Parcel in) {
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
            return new CrashReportCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
