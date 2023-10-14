package com.or2go.weavvy;

import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRICE_DBSYNC;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCT_DBSYNC;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_SKU_DBSYNC;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.or2go.core.Or2GoStore;
import com.or2go.weavvy.manager.ProductManager;


public class ProductDBSyncThread extends Thread{

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public Handler mProductDBSyncHandler;

    public ProductDBSyncThread(Context context)
    {
        mContext =context;
        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
    }

    @Override
    public void run() {

        Looper.prepare();

        //gAppEnv.getGposLogger().i("ProductSyncThread : Product sync message handler ready = ");
        mProductDBSyncHandler = new Handler() {
            public void handleMessage(Message msg) {

                Integer nMsg = msg.what;
                Bundle b = msg.getData();
                String mVendorId = b.getString("vendorid");

                switch(nMsg) {
                    case OR2GO_VENDOR_PRODUCT_DBSYNC:
                        gAppEnv.getGposLogger().i("ProductDBSyncThread : updating DB product for vendor = " + mVendorId);

                        try {
                            ProductManager prdmgr = gAppEnv.getStoreManager().getProductManager(mVendorId);
                            prdmgr.addProductsToDB();

                            Or2GoStore vendinfo = gAppEnv.getStoreManager().getStoreById(mVendorId);
                            gAppEnv.getGposLogger().i("ProductDBSyncThread : updating VendorDB for vendor product DB version = " + mVendorId + " DB version=" + vendinfo.getProductDBState().getVer());
                            //vendinfo.getDBState().setInfoVersion(vendinfo.vReqProdDbVersion);
                            gAppEnv.getStoreManager().updateProductDbVersion(mVendorId, vendinfo.getProductDBVersion());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case OR2GO_VENDOR_PRICE_DBSYNC:
                        gAppEnv.getGposLogger().i("ProductDBSyncThread : updating DB price for vendor = " + mVendorId);

                        /*try {
                            ProductManager prdmgr = gAppEnv.getVendorManager().getProductManager(mVendorId);
                            prdmgr.addPricesToDB();

                            Or2GoStore vendinfo = gAppEnv.getVendorManager().getStoreById(mVendorId);
                            gAppEnv.getGposLogger().i("ProductDBSyncThread : updating VendorDB for vendor price DB version = " + mVendorId + " DB version=" + vendinfo.getPriceDBState().getVer());
                            //vendinfo.getDBState().setInfoVersion(vendinfo.vReqProdDbVersion);
                            gAppEnv.getVendorManager().updatePriceDbVersion(mVendorId, vendinfo.getPriceDBState().getVer());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/
                        break;
                    case OR2GO_VENDOR_SKU_DBSYNC:
                        gAppEnv.getGposLogger().i("ProductDBSyncThread : updating DB SKU for vendor = " + mVendorId);

                        try {
                            ProductManager prdmgr = gAppEnv.getStoreManager().getProductManager(mVendorId);
                            prdmgr.addSKUToDB();

                            Or2GoStore vendinfo = gAppEnv.getStoreManager().getStoreById(mVendorId);
                            gAppEnv.getGposLogger().i("ProductDBSyncThread : updating VendorDB for vendor SKU DB version = " + mVendorId + " DB version=" + vendinfo.getSKUDBState().getVer());
                            //vendinfo.getDBState().setInfoVersion(vendinfo.vReqProdDbVersion);
                            gAppEnv.getStoreManager().updateSKUDbVersion(mVendorId, vendinfo.getSKUDBState().getVer());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }


                //this.removeMessages(msg.what);
                this.removeMessages(msg.what, msg);
            }
        };

        Looper.loop();
    }

    public Handler getHandler() {
        return mProductDBSyncHandler;
    }

    /*public boolean isStarted()
    {
        return this.isAlive();
    }*/

    public void StopThread() {
        this.interrupt();
        //join();
    }

    public synchronized boolean postMessage(Message msg)
    {

        if (mProductDBSyncHandler != null) {
            mProductDBSyncHandler.sendMessage(msg);
            return true;
        }
        else
            return false;
    }

}
