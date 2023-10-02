package com.or2go.vendor.showstorenearme.storeList;

import android.content.Context;

import com.or2go.vendor.showstorenearme.AppEnv;

import java.util.ArrayList;

public class StoreManager {
    private Context mContext;
    AppEnv gAppEnv;
    StoreList storeinfo;
    ArrayList<StoreList> lActiveStoreList;

    public StoreManager(Context context) {
        this.mContext = context;
        gAppEnv = (AppEnv) context;
        lActiveStoreList = new ArrayList<StoreList>();
    }

    public synchronized boolean addStoreInfo(StoreList store){
        storeinfo = new StoreList(store.stringID, store.stringName, store.vContact,
                store.geolocation, false);
        lActiveStoreList.add(storeinfo);
        return true;
    }

    public ArrayList<StoreList> getStoreList() {
        return lActiveStoreList;
    }

}
