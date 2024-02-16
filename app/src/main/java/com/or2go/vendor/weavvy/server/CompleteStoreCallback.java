package com.or2go.vendor.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.storeList.StoreList;
import com.or2go.volleylibrary.CommApiCallback;

import java.util.ArrayList;

public class CompleteStoreCallback extends CommApiCallback implements Parcelable {

    Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    String vendId;
    int regStatus = 0;
    ArrayList<StoreList> storeArrayList = new ArrayList<>();

    public CompleteStoreCallback(Context mContext, AppEnv gAppEnv) {
        this.mContext = mContext;
        this.gAppEnv = gAppEnv;
    }

    @Override
    public Void call() {
        System.out.println(result + " k145465kml " + response);
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
