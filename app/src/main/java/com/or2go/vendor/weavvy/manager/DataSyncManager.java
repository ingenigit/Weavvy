package com.or2go.vendor.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_PRICE_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_PRODUCT_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_SKU_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_INFO;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_PRICE;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_PRODUCT;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_SKU;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_STOCK;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_INFO;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.or2go.core.Or2GoStore;
import com.or2go.core.UnitManager;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.server.StoreDataCallback;

public class DataSyncManager extends Thread{
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    public Handler mDataSyncHandler;
    UnitManager mUnitMgr = new UnitManager();

    public DataSyncManager(Context context) {
        this.mContext = context;
        this.gAppEnv = (AppEnv) context;;
    }

    @Override
    public void run() {
        Looper.prepare();
        mDataSyncHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Integer nMsg = msg.what;
                Bundle b = msg.getData();
                switch (nMsg) {
                    case OR2GO_PRODUCT_LIST:
                        String mVendorId = b.getString("vendorid");
                        break;
                }
                this.removeMessages(msg.what, msg);
            }
        };
        Looper.loop();
    }

    public Handler getHandler() {
        return mDataSyncHandler;
    }

    public synchronized boolean postMessage(Message msg) {
        if (mDataSyncHandler != null) {
            mDataSyncHandler.sendMessage(msg);
            return true;
        }
        else
            return false;
    }

    public void doDataDownload(Or2GoStore syncStore, int datatype) {
        gAppEnv.getGposLogger().i("DataSyncManager: doDataDownload for store="+syncStore.getId());
        Message msg = new Message();
        switch(datatype) {
            case OR2GO_STORE_DATA_INFO:
                msg.what = OR2GO_STORE_INFO;
                break;
            case OR2GO_STORE_DATA_PRODUCT:
                msg.what = OR2GO_PRODUCT_LIST;
                break;
            case OR2GO_STORE_DATA_PRICE:
                msg.what = OR2GO_PRICE_LIST;
                break;
            case OR2GO_STORE_DATA_SKU:
                msg.what = OR2GO_SKU_LIST;
                break;
            case OR2GO_STORE_DATA_STOCK:
                break;
            default:
                msg.what = OR2GO_PRODUCT_LIST;;
        }
        //fixed value for sending sales transaction to server
        msg.arg1 = 0;
        StoreDataCallback datacb = new StoreDataCallback(mContext, gAppEnv, datatype);//Callback(mContext);
        datacb.setStoreId(syncStore.getId());
        Bundle b = new Bundle();
        b.putString("storeid", syncStore.getId());
        b.putParcelable("callback", datacb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
    }
}
