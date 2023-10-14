package com.or2go.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_COMM_SYNC_API;
import static com.or2go.core.Or2goConstValues.OR2GO_OUT_OF_STOCK_DATA;
import static com.or2go.core.Or2goConstValues.OR2GO_PRICE_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_PRODUCT_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_SKU_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_PRICE;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_PRODUCT;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_SKU;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_STOCK;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_NONE;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_REQ;
import static com.or2go.core.StoreDBState.OR2GO_DBSTATUS_DOWNLOAD_REQ;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.or2go.core.Or2GoStore;
import com.or2go.core.Or2goVendorInfo;
import com.or2go.core.UnitManager;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.server.StoreDataCallback;

public class DataSyncManager {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    public Handler mDataSyncHandler;
    UnitManager mUnitMgr = new UnitManager();

    public DataSyncManager(Context context) {
        mContext =context;
        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
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
        Message msg = new Message();
        switch(datatype) {
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
        msg.arg1 = 0;

        StoreDataCallback datacb = new StoreDataCallback(mContext, gAppEnv, datatype);//Callback(mContext);
        datacb.setVendorId(syncStore.vId);
        Bundle b = new Bundle();

        b.putString("storeid", syncStore.vId);
        b.putParcelable("callback", datacb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

    }

    public void startDataDownload(Or2GoStore store) {
        String storeid = store.vId;
        int reqdatatype = store.getDownloadDataType();
        Message msg = new Message();
        switch(reqdatatype) {
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
        msg.arg1 = 0;

        StoreDataCallback datacb = new StoreDataCallback(mContext, gAppEnv, reqdatatype);//Callback(mContext);
        datacb.setVendorId(storeid);
        Bundle b = new Bundle();

        b.putString("storeid", storeid);
        b.putParcelable("callback", datacb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

    }

    public boolean isSyncDone(Or2GoStore syncStoreInfo) {
        if ((syncStoreInfo.getProductDBState().isUpdated()) && (syncStoreInfo.getSKUDBState().isUpdated()))
            return true;
        else
            return false;

    }

    public boolean isDownloadError(Or2GoStore syncStoreInfo) {
        if ((syncStoreInfo.getProductDBState().isDownloadError()) || (syncStoreInfo.getProductDBState().isDownloadError()))
            return true;
        return false;
    }
}
