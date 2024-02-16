package com.or2go.vendor.weavvy;

import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCT_DBSYNC;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_SKU_DBSYNC;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.or2go.core.Or2GoStore;
import com.or2go.vendor.weavvy.manager.ProductManager;

public class ProductDBSyncThread extends Thread {
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
                String storeid = b.getString("storeid");

                switch(nMsg) {
                    case OR2GO_VENDOR_PRODUCT_DBSYNC:
                        gAppEnv.getGposLogger().i("ProductDBSyncThread : updating product DB Store="+storeid);

                        try {
                            ProductManager prdmgr = gAppEnv.getStoreManager().getProductManager(storeid);
                            prdmgr.addProductsToDB();

                            Or2GoStore storeinfo = gAppEnv.getStoreManager().getStore(storeid);
                            storeinfo.mProductDBState.doneDBUpdate();
                            gAppEnv.getStoreManager().updateProductDbVersion(storeid, storeinfo.mProductDBState.getVer());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    /*case OR2GO_VENDOR_PRICE_DBSYNC:
                        gAppEnv.getGposLogger().i("ProductDBSyncThread : updating DB price ");

                        try {
                            ProductManager prdmgr = gAppEnv.getStoreManager().getProductManager(storeid);
                            prdmgr.addPricesToDB();

                            Or2GoStore storeinfo = gAppEnv.getStoreManager().getStore(storeid);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;*/
                    case OR2GO_VENDOR_SKU_DBSYNC:
                        gAppEnv.getGposLogger().i("ProductDBSyncThread : updating DB SKU " );

                        try {
                            ProductManager prdmgr = gAppEnv.getStoreManager().getProductManager(storeid);
                            prdmgr.addSKUToDB();

                            Or2GoStore storeinfo = gAppEnv.getStoreManager().getStore(storeid);
                            storeinfo.mSKUDBState.doneDBUpdate();
                            gAppEnv.getStoreManager().updateSKUDbVersion(storeid, storeinfo.mSKUDBState.getVer());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }

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
