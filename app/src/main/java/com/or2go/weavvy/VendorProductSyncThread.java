package com.or2go.weavvy;

import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_PRICE;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_PRODUCT;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_DATA_SKU;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCT_DBSYNC;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_SKU_DBSYNC;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.or2go.core.Or2GoStore;
import com.or2go.core.ProductInfo;
import com.or2go.core.ProductSKU;
import com.or2go.core.UnitManager;
import com.or2go.weavvy.manager.ProductManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class VendorProductSyncThread extends Thread{

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public Handler mProductSyncHandler;

    UnitManager mUnitMgr = new UnitManager();

    public VendorProductSyncThread(Context context) {
        mContext =context;
        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
    }

    @Override
    public void run() {

        Looper.prepare();

        //gAppEnv.getGposLogger().i("ProductSyncThread : Product sync message handler ready = ");
        mProductSyncHandler = new Handler() {
            public void handleMessage(Message msg) {

                Integer nMsg = msg.what;
                Bundle b = msg.getData();

                String storedata = b.getString("storedata");
                String storeid = b.getString("storeid");

                ProductManager prdMgr = gAppEnv.getStoreManager().getProductManager(storeid);
                Or2GoStore vendInfo = gAppEnv.getStoreManager().getStoreById(storeid);

                switch(nMsg) {
                    case OR2GO_STORE_DATA_PRODUCT /*OR2GO_PRODUCT_LIST*/:
                        gAppEnv.getGposLogger().i("ProductSyncThread : updating product for vendor = "+storeid);
                        ///gAppEnv.getGposLogger().i("ProductSyncThread : product data = "+productdata);
                        try {
                            JSONObject dataobject = new JSONObject(storedata);

                            JSONObject data = dataobject.getJSONObject("data");
                            JSONArray catlist = data.getJSONArray("category");
                            JSONArray products = data.getJSONArray("products");
                            JSONArray subcatlist = data.getJSONArray("subcategory");
                            ArrayList<String> vendcatlist = new ArrayList<String>();
                            for (int i = 0; i < catlist.length(); i++) {  // **line 2**
                                String catname = catlist.getString(i).trim();//catlist.getJSONObject(i).toString();
                                vendcatlist.add(catname);
                            }
                            prdMgr.setCategoryList(vendcatlist);
                            if ((subcatlist != null) && (subcatlist.length() > 0)) {
                                HashMap<String, List<String>> prdsubcatdata = new HashMap<String, List<String>>();
                                int subcatlen = subcatlist.length();
                                for(int i=0; i< subcatlen; i++) {
                                    JSONObject subcatobject = subcatlist.getJSONObject(i);
                                    String cat = subcatobject.getString("category").trim();
                                    String subcat = subcatobject.getString("subcategory").trim();
                                    if ((subcat != null) && (!subcat.isEmpty())) {
                                        List<String> prdsubcatlist = prdsubcatdata.get(cat);
                                        if (prdsubcatlist == null) {
                                            ArrayList<String> nsubcatlist = new ArrayList<String>();
                                            nsubcatlist.add(subcat);
                                            prdsubcatdata.put(cat, nsubcatlist);
                                        }
                                        else{
                                            prdsubcatlist.add(subcat);
                                        }
                                    }
                                }
                                prdMgr.setSubCVategoryList(prdsubcatdata);
                            }
                            for (int i = 0; i < products.length(); i++) {  // **line 2**
                                JSONObject childJSONObject = products.getJSONObject(i);
                                boolean newprod=false;
                                ProductInfo prdinfo;
                                Integer prdid = childJSONObject.getInt("id");
                                prdinfo = prdMgr.getProductInfo(prdid);
                                if (prdinfo==null) {
                                    prdinfo = new ProductInfo();
                                    newprod=true;
                                    prdinfo.setId(prdid);
                                }
                                prdinfo.setName(childJSONObject.getString("name"));
                                prdinfo.setBrandName(childJSONObject.getString("brand"));
                                prdinfo.setDescription(childJSONObject.getString("description"));
                                prdinfo.setCategory(childJSONObject.getString("category"));
                                prdinfo.setSubCategory(childJSONObject.getString("subcategory"));
                                prdinfo.setProductCode(childJSONObject.getString("code"));
                                prdinfo.setHSNCode(childJSONObject.getString("hsncode"));
                                prdinfo.setTaxIncl(childJSONObject.getInt("taxinclusive"));
                                prdinfo.setTaxRate((float) childJSONObject.getInt("taxrate"));
                                prdinfo.setBarCode(childJSONObject.getString("barcode"));
                                prdinfo.setProperty(childJSONObject.getString("property"));
                                prdinfo.setTags(childJSONObject.getString("tags"));
                                prdinfo.setSaleStatus(childJSONObject.getInt("availability"));
                                prdinfo.setInventoryControl(childJSONObject.getInt("inventorycontrol"));
                                prdinfo.setImagepath(childJSONObject.getInt("imagepath"));
                                if (newprod)
                                    prdMgr.addProductInfo(prdinfo);
                                gAppEnv.getSearchManager().addSearchData(vendInfo.vName, childJSONObject.getString("name"),
                                        childJSONObject.getString("brand"), prdinfo.getTags(), prdid);
                            }
                            vendInfo.getProductDBState().doneDBUpdate();
                            gAppEnv.getStoreManager().postDBSyncMessage(storeid, OR2GO_VENDOR_PRODUCT_DBSYNC);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case OR2GO_STORE_DATA_SKU:
                        gAppEnv.getGposLogger().i("ProductSyncThread : updating SKU");
                        try {
                            JSONObject dataobj = new JSONObject(storedata);
                            JSONArray pricearr = dataobj.getJSONArray("data");
                            for (int i = 0; i < pricearr.length(); i++) {
                                JSONObject pricebject = pricearr.getJSONObject(i);

                                Integer skuid = pricebject.getInt("skuid");
                                Integer prodid = pricebject.getInt("prodid");
                                String name = pricebject.getString("sku");
                                ///String desc = pricebject.getString("description");
                                Integer unit = pricebject.getInt("unit");
                                String amnt = pricebject.getString("amount");
                                String price = pricebject.getString("saleprice");
                                String mrp = pricebject.getString("maxprice");
                                String size = pricebject.getString("size");
                                String color = pricebject.getString("color");
                                String model = pricebject.getString("model");
                                String dimen = pricebject.getString("dimension");
                                String weight = pricebject.getString("weight");
                                String pkgtype = pricebject.getString("packagetype");
                                Integer stckSts = pricebject.getInt("stockstatus");
                                Integer ver = pricebject.getInt("dbver");
                                gAppEnv.getGposLogger().i("ProductSyncThread : SKU id="+skuid+ " prodid"+prodid);
                                ProductSKU skuinfo = new ProductSKU(skuid, prodid, name, "",
                                        unit, Float.parseFloat(amnt),Float.parseFloat(price), Float.parseFloat(mrp),
                                        size, color, model, weight, dimen, pkgtype, stckSts);
                                prdMgr.addProductSKU(prodid, skuinfo);
                            }
                            vendInfo.getSKUDBState().doneDBUpdate();
                            gAppEnv.getStoreManager().postDBSyncMessage(storeid, OR2GO_VENDOR_SKU_DBSYNC);
                        } catch (JSONException e) {
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
        return mProductSyncHandler;
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

        if (mProductSyncHandler != null) {
            mProductSyncHandler.sendMessage(msg);
            return true;
        }
        else
            return false;
    }
}
