package com.or2go.vendor.weavvy.db;

import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_EXIST;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.or2go.core.Or2GoStore;

import java.util.ArrayList;

public class StoreDBHelper extends SQLiteOpenHelper {

    public SQLiteDatabase storeDBConn;
    Context mContext;

    public StoreDBHelper(Context context) {
        super(context, "storeDB.db", null, 1);
        mContext = context;
        initStoreDB();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table storetbl "+
                "(storeid text, name text, servicetype text, storetype text, description text, tags text, " +
                "address text, place text, locality text, state text, pincode text," +
                "status integer, worktime text, closedon text, infoversion integer, productdbversion integer, minorder text, policy text," +
                "skudbversion integer, orderoption integer, payoption integer, geolocation text, contact text, inventoryoption integer"+
                ",  UNIQUE(storeid) ON CONFLICT IGNORE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void initStoreDB()
    {
        storeDBConn = this.getWritableDatabase();
    }

    public int getItemCount() {
        Cursor cursor;
        int count=0;
        cursor = storeDBConn.rawQuery("SELECT * FROM storetbl", null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean insertStore (Or2GoStore vinfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("storeid", vinfo.getId());
        contentValues.put("name", vinfo.getName());
        contentValues.put("servicetype", vinfo.getServiceType());
        contentValues.put("storetype", vinfo.getStoreType());
        contentValues.put("description", vinfo.getDescription());
        contentValues.put("tags", vinfo.getTags());
        contentValues.put("address", vinfo.getAddress());
        contentValues.put("place", vinfo.getPlace());
        contentValues.put("locality", vinfo.getLocality());
        contentValues.put("state", vinfo.getState());
        contentValues.put("pincode", vinfo.vPIN);
        contentValues.put("status", vinfo.getStatus());
        contentValues.put("minorder", vinfo.getMinOrder());
        contentValues.put("worktime", vinfo.getWorkTime());
        contentValues.put("closedon", vinfo.getClosedon());
        contentValues.put("policy", vinfo.getPolicy());
        contentValues.put("contact", vinfo.getContact());
        contentValues.put("orderoption", vinfo.getOrderControl());
        contentValues.put("payoption", vinfo.getPayOption());
        contentValues.put("geolocation", vinfo.getGeoLoc());
        contentValues.put("inventoryoption", vinfo.getInventoryControl());
        contentValues.put("productdbversion", vinfo.getProductDBVersion());
        contentValues.put("infoversion", vinfo.getInfoVersion());
//        contentValues.put("pricedbversion", vinfo.getPriceDBVersion());
        contentValues.put("skudbversion", vinfo.getSKUDBVersion());
        ///contentValues.put("shuttype", vinfo.getShutDownType());
        ///contentValues.put("shutfrom", vinfo.getShutDownFrom());
        ///contentValues.put("shuttill", vinfo.getShutDownTill());
        ///contentValues.put("shutreason", vinfo.getShutDownReason());

        long ret = storeDBConn.insert("storetbl", null, contentValues);
        if(ret== -1) {
            System.out.println("StoreDBHelper: Store insert failed for store"+vinfo.getId());
            return false;
        }
        else {
            System.out.println("StoreDBHelper: Store insert successful for store"+vinfo.getId());
            return true;
        }
    }

    public boolean updateStoreInfo(Or2GoStore vinfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", vinfo.getName());
        contentValues.put("description", vinfo.getDescription());
        //contentValues.put("type", vinfo.getType());
        contentValues.put("storetype", vinfo.getStoreType());
        contentValues.put("tags", vinfo.getTags());
        contentValues.put("address", vinfo.getAddress());
        contentValues.put("place", vinfo.getPlace());
        contentValues.put("locality", vinfo.getLocality());
        contentValues.put("state", vinfo.getState());
        contentValues.put("pincode", vinfo.vPIN);
        contentValues.put("status", vinfo.getStatus());
        contentValues.put("minorder", vinfo.getMinOrder());
        contentValues.put("worktime", vinfo.getWorkTime());
        contentValues.put("closedon", vinfo.getClosedon());
        ///contentValues.put("shuttype", vinfo.getShutDownType());
        ///contentValues.put("shutfrom", vinfo.getShutDownFrom());
        ///contentValues.put("shuttill", vinfo.getShutDownTill());
        ///contentValues.put("shutreason", vinfo.getShutDownReason());
        contentValues.put("infoversion", vinfo.getInfoDBState().getVer());
        contentValues.put("contact", vinfo.getContact());
        contentValues.put("orderoption", vinfo.getOrderControl());
        contentValues.put("payoption", vinfo.getPayOption());
        contentValues.put("geolocation", vinfo.getGeoLoc());
        contentValues.put("inventoryoption", vinfo.getInventoryControl());

        //Product DB version and Price DB version should be uipdated afer their update, separately form info updade
        //contentValues.put("dbversion", vinfo.getDBState().getProductVer());
        //contentValues.put("pricedbversion", vinfo.getDBState().getPriceVer());
        long ret = storeDBConn.update("storetbl", contentValues, "storeid = ? ", new String[]{String.valueOf(vinfo.getId())});
        if(ret== -1)
            return false;
        else
            return true;
    }

    public boolean updateProductDBVersion (String id, int version) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("productdbversion", version);
        int ret = storeDBConn.update("storetbl", contentValues, "storeid = ? ", new String[]{id});
        if(ret> 0 ) {
            System.out.println("StoreDBHelper : product DB version update success store="+id+"  version="+version);
            return true;
        }
        else {
            System.out.println("StoreDBHelper : product DB version update failed!!! store="+id+"  version="+version);
            return false;
        }
    }

    public boolean updateSKUDBVersion (String id, int version) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("skudbversion", version);
        int ret = storeDBConn.update("storetbl", contentValues, "storeid = ? ", new String[]{id});
        //storeDBConn.update("storetbl", contentValues, "storeid = ? ", new String[]{String.valueOf(id)});
        if(ret> 0 ) {
            System.out.println("StoreDBHelper : SKU DB version update success store="+id+"  version="+version);
            return true;
        }
        else {
            System.out.println("StoreDBHelper : product DB version update failed store="+id+"  version="+version);
            return false;
        }
    }

    //////
    public boolean getStores(ArrayList<Or2GoStore> storelist) {
        Cursor cursor;
        int count = 0;
        if (storelist==null) return false;
        cursor = storeDBConn.rawQuery("SELECT * FROM storetbl", null);
        count = cursor.getCount();

        if (count <=0)
            return false;
        else {
            storelist.clear();
            cursor.moveToFirst();
            for(int i=0;i<count;i++) {
                String vid = cursor.getString(cursor.getColumnIndexOrThrow("storeid"));
                String vname = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String  vservice= cursor.getString(cursor.getColumnIndexOrThrow("servicetype"));
                String vstoretype = cursor.getString(cursor.getColumnIndexOrThrow("storetype"));
                String vdesc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow("tags"));
                String vaddr = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String vplace = cursor.getString(cursor.getColumnIndexOrThrow("place"));
                String vlocality = cursor.getString(cursor.getColumnIndexOrThrow("locality"));
                String vstate = cursor.getString(cursor.getColumnIndexOrThrow("state"));
                String vpin = cursor.getString(cursor.getColumnIndexOrThrow("pincode"));
                Integer vstatus = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
                String vminord = cursor.getString(cursor.getColumnIndexOrThrow("minorder"));
                String voptime = cursor.getString(cursor.getColumnIndexOrThrow("worktime"));
                String vclosed = cursor.getString(cursor.getColumnIndexOrThrow("closedon"));
                Integer proddbver = cursor.getInt(cursor.getColumnIndexOrThrow("productdbversion"));
                Integer infover = cursor.getInt(cursor.getColumnIndexOrThrow("infoversion"));
                //Integer pricever = cursor.getInt(cursor.getColumnIndexOrThrow("pricedbversion"));
                Integer skuver = cursor.getInt(cursor.getColumnIndexOrThrow("skudbversion"));
                Integer ordcontrol = cursor.getInt(cursor.getColumnIndexOrThrow("orderoption"));
                Integer payoption = cursor.getInt(cursor.getColumnIndexOrThrow("payoption"));
                String geolocation = cursor.getString(cursor.getColumnIndexOrThrow("geolocation"));
                Integer invcontrol = cursor.getInt(cursor.getColumnIndexOrThrow("inventoryoption"));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow("contact"));
                //String shutfrom = cursor.getString(cursor.getColumnIndexOrThrow("shutfrom"));
                //String shuttill = cursor.getString(cursor.getColumnIndexOrThrow("shuttill"));
                //String shutres = cursor.getString(cursor.getColumnIndexOrThrow("shutreason"));
                //Integer shuttype = cursor.getInt(cursor.getColumnIndexOrThrow("shuttype"));

                if (vminord == null) vminord="0";
                Or2GoStore storeinfo = new Or2GoStore(vid, vname, vservice, vstoretype, vdesc, tag,
                        vaddr, vplace, vlocality, vstate, vpin, vstatus,
                        vminord, voptime, vclosed, proddbver,infover,skuver, geolocation,
                        contact, payoption, ordcontrol, invcontrol);
                //storeinfo.setShutdownInfo(shutfrom,shuttill,shutres,shuttype);
                storeinfo.setProductStatus(OR2GO_VENDOR_PRODUCTLIST_EXIST);
                storelist.add(storeinfo);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return true;
    }
}
