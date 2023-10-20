package com.or2go.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_VENDORLIST_DONE;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDORLIST_NONE;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_INFO;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_EXIST;
import static com.or2go.core.VendorDBState.OR2GO_VENDOR_DBSTATUS_NONE;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.or2go.core.Or2GoStore;
import com.or2go.core.SearchInfo;
import com.or2go.mylibrary.SearchDBHelper;
import com.or2go.mylibrary.StoreDBHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.ProductDBSyncThread;
import com.or2go.weavvy.db.StoreListDBHelper;
import com.or2go.weavvy.model.SearchStore;
import com.or2go.weavvy.model.StoreList;
import com.or2go.weavvy.VendorProductSyncThread;
import com.or2go.weavvy.server.StoreInfoCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class StoreManager {

    private Context mContext;
    AppEnv gAppEnv;
    StoreList storeinfo;
    LinkedHashMap<String, Or2GoStore> mapStore;
    ArrayList<Or2GoStore> lActiveStoreList;
    ArrayList<StoreList> storeName;
    ArrayList<StoreList> storelist;
    StoreDBHelper mStoreDB;
    ArrayList<String> lStoreUpdateList;
    HashMap<String, ProductManager> mapProductMgr;
    int nVendorIdx = 0;
    HashMap<String, List<String>> mapVendorTags;
    LinkedHashMap<String, List<String>> mapSortedVendorTags;
    int stsVendorList;
    private VendorProductSyncThread mProductSyncThread;
    Handler mProductSyncHandler;
    private ProductDBSyncThread mProductDBSyncThread;
    Handler mProductDBSyncHandler;
    StoreListDBHelper storedb;

    public StoreManager(Context context) {
        this.mContext = context;
        gAppEnv = (AppEnv) context;
        storedb = new StoreListDBHelper(context);
        storedb.InitDB();
        gAppEnv.getGposLogger().i("VendorManager : Initializing vendor DB");
        mStoreDB = new StoreDBHelper(mContext);
        mStoreDB.initStoreDB();
        stsVendorList = OR2GO_VENDORLIST_NONE;
        lActiveStoreList = new ArrayList<Or2GoStore>();
        lStoreUpdateList = new ArrayList<String>();
        mapStore = new LinkedHashMap<String, Or2GoStore>();
        mapProductMgr = new HashMap<String, ProductManager>();
        mapVendorTags = new HashMap<String, List<String>>();
        mapSortedVendorTags = new LinkedHashMap<String, List<String>>();

        storeName = new ArrayList<StoreList>();
        storelist = new ArrayList<StoreList>();

        int scnt = mStoreDB.getItemCount();
        Log.i("StoreManager", "Store DB Count="+scnt);
        if (scnt > 0) {
            Log.i("StoreManager ", "Initializing store details form DB");
            initStores();
        }
        mProductSyncThread = new VendorProductSyncThread(mContext);
        mProductSyncThread.start();
        mProductDBSyncThread = new ProductDBSyncThread(mContext);
        mProductDBSyncThread.start();
    }

    private void initStores() {
        ArrayList<Or2GoStore> storelist = mStoreDB.getStores();
        int vendcnt = storelist.size();
        Log.i("VendorManager", "updating stores...start" + vendcnt);
        for (int i = 0; i < vendcnt; i++) {
            Or2GoStore storeinfo = storelist.get(i);
            storeinfo.processFavItemsList();
            //showFavItems(storeinfo);
            mapStore.put(storeinfo.vId, storeinfo);
            createVendorProductManager(storeinfo.vId);
            storeinfo.setProductStatus(OR2GO_VENDOR_PRODUCTLIST_EXIST);
            //nVendorUpdateCount++;
        }
        Log.i("VendorManager", "updating stores...end");
    }

    public boolean createVendorProductManager(String vendid) {
        ProductManager prdMgr = new ProductManager(mContext, vendid);
        mapProductMgr.put(vendid, prdMgr);
        int dbprdcnt = prdMgr.getDbProductCount();
        Log.i("VendorManager", "vendor="+vendid+"  product count="+dbprdcnt);
        if (dbprdcnt > 0) {
            prdMgr.initProductsFromDB();
        }
        return true;
    }

    public ProductManager getProductManager(String vid) {
        return mapProductMgr.get(vid);
    }

    public void addDiscountInfo(String storeid, String disc, int type) {
        Or2GoStore storeinfo = getStoreById(storeid);
        if (storeinfo != null) {
            storeinfo.vDiscType = type;
            storeinfo.vDiscValue = disc;
        }
    }

    public ArrayList<StoreList> getAllStoreList() {
//        return storelist;
        return storedb.getStoresList();
    }

    public boolean addStoreData(String storeid, String storename, String storetype, String storecontact, String storegeo) {
        return storedb.insertStoreData(storeid, storename, storetype, storecontact, storegeo);
    }

    public synchronized boolean addStoreInfo(StoreList store){
        storeinfo = new StoreList(store.stringID, store.stringName, store.stringType, store.vContact, store.geolocation, false);
        storelist.add(storeinfo);
        return true;
    }

    public boolean postDBSyncMessage(String vendorid, Integer optype) {
        Bundle b = new Bundle();
        Message msg = new Message();
        b.putString("vendorid", vendorid);
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

    public synchronized boolean addStoreUpdateList(String storeid) {
        gAppEnv.getGposLogger().i("Vendor Manager : add update list storeid="+storeid);
        return lStoreUpdateList.add(storeid);
    }

    public synchronized boolean removeStoreUpdateList(String storeid) {
        gAppEnv.getGposLogger().i("Vendor Manager : remove update list storeid="+storeid);
        return lStoreUpdateList.remove(storeid);
    }

    public synchronized void setVendorListStatus(int sts) {
        stsVendorList = sts;
    }

    public synchronized void downloadStoreInfoDone(String storeid, Or2GoStore newstore) {
        removeStoreUpdateList(storeid);
        Or2GoStore storeinfo = getStoreById(storeid);

        if (storeinfo ==null){
            //new vendor
            gAppEnv.getGposLogger().i("Vendor Manager : new store="+storeid);
            //set product/price DB status as NONE
            newstore.getProductDBState().setState(OR2GO_VENDOR_DBSTATUS_NONE);
            //newstore.getPriceDBState().setState(OR2GO_VENDOR_DBSTATUS_NONE);
            newstore.getSKUDBState().setState(OR2GO_VENDOR_DBSTATUS_NONE);
            newstore.isActive = true;
            mapStore.put(storeid, newstore);

            gAppEnv.getGposLogger().i("Vendor Manager : new store="+storeid);
            gAppEnv.getGposLogger().i("Vendor Manager : new store="+newstore.getName()+"  Fav items="+newstore.getFavItems());
            mStoreDB.insertStore(newstore);
            createVendorProductManager(storeid);
            lActiveStoreList.add(newstore);
        }
        else {
            storeinfo.updateVendorInfo(newstore);
            //update Vendor DB
            mStoreDB.updateStoreInfo(newstore);
        }
        //Check and download if more to be downloaded
        downloadStoreInfo();
    }

    public synchronized boolean updateStoreInfo(String vid, Or2GoStore store) {
        Or2GoStore storeinfo = getStoreById(vid);
        if (storeinfo == null) {
            gAppEnv.getGposLogger().i("VendorManager : no vendor exists....adding new vendor ID="+vid);
            //Create new vendor
            //storeinfo = new Or2GoStore(vid, name);
            storeinfo = new Or2GoStore(vid, store.vName, store.vServiceType, store.vStoreType, store.vDescription,
                    store.vTagInfo, store.vAddress, store.vPlace, store.vLocality, store.vState, store.vPIN,
                    store.vStatus, store.vMinOrd, store.vWorkTime, store.vClosedOn,
                    0/*store.getProductDBVersion()*/, store.getInfoVersion(), 0/*store.getSKUDBVersion()*/,
                    store.geolocation, store.vContact, store.vOrderPayOption, store.vOrderControl, store.vInventoryControl);
            mapStore.put(vid, storeinfo);
            mStoreDB.insertStore(storeinfo);
            createVendorProductManager(vid);
            storeinfo.isActive = true;
            lActiveStoreList.add(storeinfo);
            //Set required version of Product and SKU DB
            storeinfo.getProductDBState().updateVersion(store.getProductDBVersion());
            storeinfo.getSKUDBState().updateVersion(store.getSKUDBVersion());
            addStoreUpdateList(vid);//lStoreUpdateList.add(vid);
        } else {
            //storeinfo.updateVendorInfo(store);
            if (store.getInfoVersion() > storeinfo.getInfoVersion()) {
                gAppEnv.getGposLogger().i("VendorManager : updating DB status for vendor "+storeinfo.getName());
                System.out.print("VendorManager : Info ver="+storeinfo.getInfoVersion()+ " new ver="+store.getInfoVersion());
                storeinfo.updateStoreInfo(store.vName, store.vServiceType, store.vStoreType, store.vDescription, store.vTagInfo,
                        store.vAddress, store.vPlace, store.vLocality, store.vState, store.vPIN,
                        store.vStatus, store.vMinOrd, store.vWorkTime, store.vClosedOn,
                        store.getInfoVersion(), store.geolocation, store.vContact, store.getFavItems(),
                        store.vOrderPayOption, store.vOrderControl, store.vInventoryControl);
                mStoreDB.updateStoreInfo(storeinfo);
            }
            storeinfo.getProductDBState().updateVersion(store.getProductDBVersion());
            storeinfo.getSKUDBState().updateVersion(store.getSKUDBVersion());
            //if (storeinfo.getInfoDBState().isRequiredDBDownload())  addStoreUpdateList(vid);//lStoreUpdateList.add(vid);
            System.out.print("VendorManager : Product DB  cur ver="+storeinfo.getInfoVersion()+ " new ver="+store.getInfoVersion());
            if (storeinfo.getProductDBState().isRequiredDBDownload()) {
                System.out.print("VendorManager : Clearing Product DB  cur ver="+storeinfo.getInfoVersion()+ " new ver="+store.getInfoVersion());
                getProductManager(storeinfo.vId).clearProductData();
                addStoreUpdateList(vid);
            }
            System.out.print("VendorManager : SKU DB cur ver="+storeinfo.getSKUDBVersion()+ " new ver="+store.getSKUDBVersion());
            if (storeinfo.getSKUDBState().isRequiredDBDownload()) {
                System.out.print("VendorManager : Clearing SKU DB cur ver="+storeinfo.getSKUDBVersion()+ " new ver="+store.getSKUDBVersion());
                getProductManager(storeinfo.vId).clearSKUData();
                if (!lStoreUpdateList.contains(vid)) {
                    addStoreUpdateList(vid);
                }
            }
            //if (storeinfo.getPriceDBState().isRequiredDBDownload()) {getProductManager(storeinfo.vId).clearPriceData();}
            storeinfo.setFavItems(store.getFavItems());
            storeinfo.isActive = true;
            lActiveStoreList.add(storeinfo);
        }
        return true;
    }

    public synchronized boolean updateProductDbVersion(String storeid, Integer ver) {
        System.out.print("VendorManager : updating Store product DB version = "+ver);
        return mStoreDB.updateProductDBVersion(storeid, ver);
    }

    public synchronized boolean updateSKUDbVersion(String storeid, Integer ver) {
        System.out.print("VendorManager : updating Store price DB version = "+ver);
        return mStoreDB.updateSKUDBVersion(storeid, ver);
    }

    public ArrayList<Or2GoStore> getStoreList() {
        return lActiveStoreList;
    }

    public Or2GoStore getStoreById(String id) {
        return mapStore.get(id);
    }

    public Or2GoStore getStoreByName(String vendname) {
        int vlistsz = lActiveStoreList.size();
        for (int i = 0; i < vlistsz; i++) {
            Or2GoStore vinfo = lActiveStoreList.get(i);

            if (vinfo.vName.equals(vendname)) {
                return vinfo;
            }
        }

        return null;
    }

    public boolean isVendorListDone() {
        if (stsVendorList == OR2GO_VENDORLIST_DONE)
            return true;
        else
            return false;
    }

    public synchronized void downloadStoreData() {
        int updatestorecnt = lStoreUpdateList.size();
        gAppEnv.getGposLogger().i("Vendor Manager : updating data of stores="+updatestorecnt);
        if (lStoreUpdateList.size() > 0) {
            String updstoreid = lStoreUpdateList.get(0);
            gAppEnv.getGposLogger().i("Vendor Manager : getting data of store="+updstoreid);
            gAppEnv.getDataSyncManager().startDataDownload(getStoreById(updstoreid));
        }
        else
            setVendorListStatus(OR2GO_VENDORLIST_DONE);
    }

    public synchronized void downloadStoreDataDone(String storeid) {
        removeStoreUpdateList(storeid);
        //Check and download if more to be downloaded
        downloadStoreData();

    }

    public synchronized void downloadStoreInfo() {
        if (lStoreUpdateList.size() > 0) {
            String storid = lStoreUpdateList.get(0);
            gAppEnv.getGposLogger().i("Vendor Manager : getting info of store="+storid);

            Message msg = new Message();
            msg.what = OR2GO_VENDOR_INFO;    //fixed value for sending sales transaction to server
            msg.arg1 = 0;

            StoreInfoCallback cb = new StoreInfoCallback(mContext);//Callback(mContext);
            cb.setVendorId(storid);
            Bundle b = new Bundle();
            b.putString("vendorid", storid);
            b.putParcelable("callback", cb);
            msg.setData(b);
            gAppEnv.getCommMgr().postMessage(msg);
        }
        else {
            setVendorListStatus(OR2GO_VENDORLIST_DONE);
        }
    }

    public synchronized void postProductData(Message msg) {
        gAppEnv.getGposLogger().i("Vendor Manager : post product data ");
        mProductSyncHandler = mProductSyncThread.getHandler();
        if (mProductSyncHandler != null)
            mProductSyncHandler.sendMessage(msg);
        else
            gAppEnv.getGposLogger().i("ProductSyncThread : Product sync handler is null ");
    }

    public boolean getSearchInfo(String name,  ArrayList<StoreList>list ) {
        return storedb.searchStore(name, list);
    }

}
