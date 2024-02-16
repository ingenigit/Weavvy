package com.or2go.vendor.weavvy.storeList;

import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_EXIST;
import static com.or2go.core.StoreDBState.OR2GO_DBSTATUS_DOWNLOAD_REQ;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.or2go.core.Or2GoStore;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.ProductDBSyncThread;
import com.or2go.vendor.weavvy.StoreProductSyncThread;
import com.or2go.vendor.weavvy.db.StoreDBHelper;
import com.or2go.vendor.weavvy.manager.ProductManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class StoreManager {
    private Context mContext;
    AppEnv gAppEnv;
    StoreList storeinfo;
    LinkedHashMap<String, Or2GoStore> mapStore;
    HashMap<String, ProductManager> mapProductMgr;
    ArrayList<Or2GoStore> mStoreList;
    ArrayList<StoreList> storeName;
    ArrayList<StoreList> lActiveStoreList;
    StoreDBHelper mStoreDB;
    boolean isUpdateRequired=false;
    StoreProductSyncThread mProductSyncThread;
    Handler mProductSyncHandler;
    ProductDBSyncThread mProductDBSyncThread;

    public StoreManager(Context context) {
        this.mContext = context;
        gAppEnv = (AppEnv) context;
        mStoreList = new ArrayList<Or2GoStore>();
        mapStore = new LinkedHashMap<String, Or2GoStore>();
        mapProductMgr = new HashMap<String, ProductManager>();
        storeName = new ArrayList<StoreList>();
        lActiveStoreList = new ArrayList<StoreList>();
        mStoreDB = new StoreDBHelper(context);
        int scnt = mStoreDB.getItemCount();
        Log.i("StoreManager", "Store DB Count="+scnt);
        if (scnt > 0) {
            Log.i("StoreManager ", "Initializing store details form DB");
            initStores();
        }
        mProductSyncThread = new StoreProductSyncThread(mContext);
        mProductSyncThread.start();
        mProductDBSyncThread = new ProductDBSyncThread(mContext);
        mProductDBSyncThread.start();
    }

    private void initStores() {
        boolean dbres = mStoreDB.getStores(mStoreList);
        if (dbres==false) return;
        int cnt = mStoreList.size();
        Log.i("StoreManager", "store count=" + cnt);
        if (cnt>0) {
            Log.i("StoreManager", "initializing stores from list=");
            for(int i=0;i<cnt;i++)
            {
                Or2GoStore istore = mStoreList.get(i);
                Log.i("StoreManager", "initializing store=" + istore.getId()+ " Product DB Ver="+istore.getProductDBState().getVer());
                ProductManager prdmgr = new ProductManager(mContext, istore.getId());
                int dbprdcnt = prdmgr.getDbProductCount();
                Log.i("StoreManager", "store=" + istore.getId() + "  product count=" + dbprdcnt);
                if (dbprdcnt > 0) {
                    prdmgr.initProductsFromDB();
                }
                istore.setProductStatus(OR2GO_VENDOR_PRODUCTLIST_EXIST);

                mapStore.put(istore.getId(), istore);
                mapProductMgr.put(istore.getId(),prdmgr);
            }
        }
    }

    public ArrayList<StoreList> getStoreList() {
        return lActiveStoreList;
    }
    public ProductManager getProductManager(String storeid) {return mapProductMgr.get(storeid);}
    public Or2GoStore getStore(String id) {return mapStore.get(id);}
    public ArrayList<StoreList> getStoreName(){
        return storeName;
    }

    public synchronized boolean updateStoreInfo(String id, String name, String svctype, String storetype, String desc, String tags,
                                                     String addr, String place, String locality, String state, String pin, Integer status,
                                                     String minorder, String worktime, String closedon, String favlist,
                                                     Integer proddbver, Integer infodbver, Integer skudbver, String geoLocation,
                                                     String contact, Integer payopt, Integer orderopt, Integer inventoryopt){
        Or2GoStore istore = mapStore.get(id);
        if (istore == null) {
            gAppEnv.getGposLogger().i("StoreManager : no store exists....creating new store id=" + id);

            //Create new store  Product and SKU DB versions updated after data download
            istore = new Or2GoStore(id, name, svctype, storetype, desc, tags,
                    addr, place, locality, state, pin, status,
                    minorder, worktime, closedon,
                    0, infodbver, 0, geoLocation, contact, payopt, orderopt, inventoryopt);
            //istore.setPayOption(payopt);
            //istore.setOrderControl(orderopt);
            mapStore.put(id, istore);
            mStoreDB.insertStore(istore);
            mapProductMgr.put(id, new ProductManager(mContext, id));
            isUpdateRequired = true;
            storeName.add(new StoreList(id, name));
        }
        else{
            storeName.add(new StoreList(id, name));
            gAppEnv.getGposLogger().i("StoreManager : store exists....checking DB versions id="+id);
            if (istore.getInfoVersion() < infodbver)
            {
                istore.updateStoreInfo(name, svctype, storetype, desc, tags,
                        addr, place, locality, state, pin,
                        status, minorder, worktime, closedon,
                        infodbver, geoLocation, contact, favlist,
                        payopt, orderopt, inventoryopt);

            }
//            updateStoreDBVersions(id, proddbver,skudbver);
        }
        return true;
    }

    public synchronized boolean updateStoreDBVersions(String storeid, int prodver, int skuver) {
        Or2GoStore ostore = mapStore.get(storeid);
        if (ostore == null) {
            return false;
        }
        else {
            gAppEnv.getGposLogger().i("StoreManager : updating DB status ");
            ostore.getProductDBState().updateVersion(prodver);
            ostore.getSKUDBState().updateVersion(skuver);
            if (ostore.getProductDBState().isRequiredDBDownload() || ostore.getSKUDBState().isRequiredDBDownload())
                isUpdateRequired= true;
            if (ostore.getProductDBState().isRequiredDBDownload()) {mapProductMgr.get(storeid).clearProductData();}
            if (ostore.getSKUDBState().isRequiredDBDownload()) {mapProductMgr.get(storeid).clearSKUData();}
        }
        return true;
    }

    public synchronized boolean addStoreInfo(StoreList store){
        storeinfo = new StoreList(store.stringID, store.stringName, store.stringType, store.vContact, store.geolocation, false);
        lActiveStoreList.add(storeinfo);
        return true;
    }

    public synchronized void postProductData(Message msg) {
        gAppEnv.getGposLogger().i("Vendor Manager : post product data ");
        mProductSyncHandler = mProductSyncThread.getHandler();
        if (mProductSyncHandler != null) mProductSyncHandler.sendMessage(msg);
        else
            gAppEnv.getGposLogger().i("ProductSyncThread : Product sync handler is null ");

    }

    public boolean postDBSyncMessage(String storeid, Integer optype) {
        Handler mProductDBSyncHandler;
        Bundle b = new Bundle();
        Message msg = new Message();
        b.putString("storeid", storeid);
        msg.what = optype;   //Product or Price DB sync
        msg.arg1 = 0;
        msg.setData(b);

        gAppEnv.getGposLogger().i("Vendor Manager : post DB sync data type="+optype);
        mProductDBSyncHandler = mProductDBSyncThread.getHandler();
        if (mProductDBSyncHandler != null) mProductDBSyncHandler.sendMessage(msg);
        else {
            gAppEnv.getGposLogger().i("ProductDBSyncThread : DB sync handler is null ");
            return false;
        }
        return true;
    }

    public synchronized boolean updateProductDbVersion(String storeid, Integer ver) {
        System.out.print("VendorManager : updating Store product DB version = "+ver);
        return mStoreDB.updateProductDBVersion(storeid, ver);
    }

    public synchronized boolean updateSKUDbVersion(String storeid, Integer ver) {
        System.out.print("VendorManager : updating Store SKU DB version = "+ver);
        return mStoreDB.updateSKUDBVersion(storeid, ver);
    }

    public synchronized boolean syncStoreDB(){
        for(int i=0; i< mStoreList.size(); i++) {
            Or2GoStore istore = mStoreList.get(i);
            if (istore.isDownloadRequired()) {
                Integer downloadtype = istore.getDownloadDataType();
                if (downloadtype>0) {
                    System.out.println("Store Data Download Type="+downloadtype);
                    gAppEnv.getDataSyncManager().doDataDownload(istore, downloadtype);
                    istore.setDBSate(downloadtype, OR2GO_DBSTATUS_DOWNLOAD_REQ);
                }
            }
        }
        return true;
    }
}
