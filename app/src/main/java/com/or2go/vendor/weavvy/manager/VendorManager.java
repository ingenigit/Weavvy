package com.or2go.vendor.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_OUT_OF_STOCK_DATA;
import static com.or2go.core.Or2goConstValues.OR2GO_PRICE_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_PRODUCT_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_NONE;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_REQ;
import static com.or2go.core.VendorDBState.OR2GO_VENDOR_DB_DOWNLOAD_REQ;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.or2go.core.Or2goVendorInfo;
import com.or2go.core.VendorDBState;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.db.VendorDBHelper;
import com.or2go.vendor.weavvy.server.PriceListCallback;
import com.or2go.vendor.weavvy.server.StockOutCallback;

import java.util.ArrayList;

public class VendorManager {

    private Context mContext;
    AppEnv gAppEnv;
    String mVendorId;
    VendorDBHelper mVendorDB;
    Or2goVendorInfo mVendorInfo=null;
    //ProductManager mProductMgr;

    //private ProductDBSyncThread mProductDBSyncThread;

    public VendorManager(Context context) {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();
        mVendorId = gAppEnv.gAppSettings.getVendorId();
        gAppEnv.getGposLogger().i("VendorManager : Initializing vendor DB");
        mVendorDB = new VendorDBHelper(mContext);
        int vendcnt = mVendorDB.getItemCount();
        Log.i("VendorManager", "vendor in DB=" + vendcnt);
        if (vendcnt > 0) {
            Log.i("VendorManager", "Initializing vendor details form DB");
            initVendor();
        }
        //mProductDBSyncThread = new ProductDBSyncThread(mContext);
        //mProductDBSyncThread.start();
    }

    private void initVendor() {
        ArrayList<Or2goVendorInfo> vendinfolist = mVendorDB.getVendors();
        int vendcnt = vendinfolist.size();
        Log.i("VendorManager", "updating vendor count="+vendcnt);
        mVendorInfo = vendinfolist.get(0);
        Log.i("VendorManager", "saved vendor id="+mVendorInfo.vId+ "  name="+mVendorInfo.vName);
    }

    public synchronized boolean updateVendor(Or2goVendorInfo vendorinfo) {
        if (mVendorInfo == null) {
            gAppEnv.getGposLogger().i("VendorManager : no vendor exists....adding new vendor  id="+mVendorId);
            mVendorInfo = new Or2goVendorInfo(mVendorId);
            mVendorInfo.updateVendorInfo(vendorinfo);
            mVendorDB.insertVendor(mVendorInfo);
        } else {
            gAppEnv.getGposLogger().i("VendorManager : updating DB status for vendor "+vendorinfo.getName());
            VendorDBState vdbstate = mVendorInfo.getDBState();
            vdbstate.updateState(vendorinfo.getInfoVersion(), vendorinfo.getProductDbVersion(), vendorinfo.getPriceDbVersion());
            if (vdbstate.reqInfoDBDownload()) {
                mVendorInfo.updateVendorInfo(vendorinfo);
                mVendorDB.updateVendorInfo(vendorinfo);
                mVendorDB.updateInfoVersion(mVendorId, vendorinfo.getInfoVersion());
            }
        }
        return true;
    }

    public void setVendorId(String vendid) { mVendorId = vendid;}
    public ProductManager getProductManager(){return null;/*mProductMgr;*/}
    public Or2goVendorInfo getVendorInfo(){return mVendorInfo;}

    public void doPriceDownload(Or2goVendorInfo syncVendorInfo, int api) {
        syncVendorInfo.getDBState().setPriceDownloadState(OR2GO_VENDOR_DB_DOWNLOAD_REQ);
        gAppEnv.getGposLogger().i("Vendor Manager : Downloading Price DB of vendor="+syncVendorInfo.getName());
        Message msg = new Message();
        msg.what = OR2GO_PRICE_LIST;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;

        PriceListCallback pricecb = new PriceListCallback(gAppEnv);//Callback(mContext);
        if (api>0) pricecb.setLinkAPI(api);
        Bundle b = new Bundle();
        b.putParcelable("callback", pricecb);
        b.putString("vendorid", syncVendorInfo.vId);
        b.putString("storedb", syncVendorInfo.vDBName);
        b.putInt("dbver", syncVendorInfo.getDBState().getPriceVer());
        msg.setData(b);

        pricecb.setVendorId(syncVendorInfo.vId);
        gAppEnv.getCommMgr().postMessage(msg);

    }

    public void getOutOfStockData(Or2goVendorInfo syncVendorInfo)
    {
        System.out.println("Vendor Manager : Downloading Out of Stock vendor="+syncVendorInfo.getName());
        Message msg = new Message();
        msg.what = OR2GO_OUT_OF_STOCK_DATA;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;

        StockOutCallback pricecb = new StockOutCallback(gAppEnv);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", pricecb);
        b.putString("dbname", syncVendorInfo.vDBName);
        msg.setData(b);

        pricecb.setVendorId(syncVendorInfo.vId);
        gAppEnv.getCommMgr().postMessage(msg);

    }

    public boolean isSyncDone(Or2goVendorInfo syncVendorInfo)
    {
        if ((syncVendorInfo.getDBState().isProductUpdated()) && (syncVendorInfo.getDBState().isPriceUpdated()))
            return true;
        else
            return false;

    }

    public boolean isDownloadError(Or2goVendorInfo syncVendorInfo)
    {
        if ((syncVendorInfo.getDBState().isProductDownloadError()) || (syncVendorInfo.getDBState().isPriceDownloadError()))
            return true;

        return false;
    }

    public boolean postDBSyncMessage(String vendorid, Integer optype) {
        Handler mProductDBSyncHandler;

        Bundle b = new Bundle();
        Message msg = new Message();
        b.putString("vendorid", vendorid);

        msg.what = optype;   //Product or Price DB sync
        msg.arg1 = 0;
        msg.setData(b);

        /*gAppEnv.getGposLogger().i("Vendor Manager : post DB sync data type="+optype);
        mProductDBSyncHandler = mProductDBSyncThread.getHandler();
        if (mProductDBSyncHandler != null) mProductDBSyncHandler.sendMessage(msg);
        else {
            gAppEnv.getGposLogger().i("ProductDBSyncThread : DB sync handler is null ");
            return false;
        }*/

        return true;

    }

}
