package com.or2go.vendor.weavvy.db;

import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRODUCTLIST_EXIST;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.or2go.core.Or2goVendorInfo;

import java.util.ArrayList;

public class VendorDBHelper extends SQLiteOpenHelper {

    public SQLiteDatabase vendorDBConn;
    Context mContext;

    public VendorDBHelper(Context context) {
        super(context, "vendorDB.db", null, 5);
        mContext = context;
        initVendorDB();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table vendortbl "+
                "(vendorid text, name text, type text, description text, tags text, address text, place text, locality text, state text, " +
                "status text, worktime text, closedon text, logopath text, dbname text, infoversion integer, dbversion integer, minorder text, policy text," +
                "shuttype text, shutfrom text, shuttill text, shutreason text, pricedbversion integer, storetype text "+
                ",  UNIQUE(vendorid) ON CONFLICT IGNORE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN minorder text");
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN policy text");
        }
        if ((oldVersion<=2) && (newVersion==3)) {
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN shuttype text");
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN shutfrom text");
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN shuttill text");
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN shutreason text");
        }
        if ((oldVersion<=3) && (newVersion==4)) {
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN pricedbversion text");
        }
        if ((oldVersion<=4) && (newVersion==5)) {
            db.execSQL("ALTER TABLE vendortbl ADD COLUMN storetype text");
        }
    }

    public void initVendorDB() {
        vendorDBConn = this.getWritableDatabase();
    }

    public int getItemCount() {
        Cursor cursor;
        int count=0;
        cursor = vendorDBConn.rawQuery("SELECT * FROM vendortbl", null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean insertVendor (Or2goVendorInfo vinfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("vendorid", vinfo.getId());
        contentValues.put("name", vinfo.getName());
        contentValues.put("type", vinfo.getType());
        contentValues.put("storetype", vinfo.getStoreType());
        contentValues.put("description", vinfo.getDescription());
        contentValues.put("tags", vinfo.getTags());
        contentValues.put("address", vinfo.getAddress());
        contentValues.put("place", vinfo.getPlace());
        contentValues.put("locality", vinfo.getLocality());
        contentValues.put("state", vinfo.getState());
        contentValues.put("status", vinfo.getStatus());
        contentValues.put("minorder", vinfo.getMinOrder());
        contentValues.put("worktime", vinfo.getWorkTime());
        contentValues.put("closedon", vinfo.getClosedon());
        //contentValues.put("policy", vinfo.getPolicy());
        contentValues.put("logopath", vinfo.getLogoPath());
        contentValues.put("dbname", vinfo.getDbName());
        //contentValues.put("dbversion", vinfo.getDbVersion());
        //contentValues.put("infoversion",vinfo.getInfoVersion());
        contentValues.put("dbversion", vinfo.getDBState().getProductVer());
        contentValues.put("infoversion", vinfo.getDBState().getInfoVer());
        contentValues.put("pricedbversion", vinfo.getDBState().getPriceVer());
        contentValues.put("shuttype", vinfo.getShutDownType());
        contentValues.put("shutfrom", vinfo.getShutDownFrom());
        contentValues.put("shuttill", vinfo.getShutDownTill());
        contentValues.put("shutreason", vinfo.getShutDownReason());

        long ret = vendorDBConn.insert("vendortbl", null, contentValues);
        if(ret== -1)
            return false;
        else
            return true;
    }

    public boolean updateVendorInfo(Or2goVendorInfo vinfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("description", vinfo.getDescription());
        contentValues.put("type", vinfo.getType());
        contentValues.put("storetype", vinfo.getStoreType());
        contentValues.put("tags", vinfo.getTags());
        contentValues.put("address", vinfo.getAddress());
        contentValues.put("place", vinfo.getPlace());
        contentValues.put("locality", vinfo.getLocality());
        contentValues.put("status", vinfo.getStatus());
        contentValues.put("minorder", vinfo.getMinOrder());
        contentValues.put("worktime", vinfo.getWorkTime());
        contentValues.put("closedon", vinfo.getClosedon());
        contentValues.put("shuttype", vinfo.getShutDownType());
        contentValues.put("shutfrom", vinfo.getShutDownFrom());
        contentValues.put("shuttill", vinfo.getShutDownTill());
        contentValues.put("shutreason", vinfo.getShutDownReason());
        contentValues.put("infoversion", vinfo.getDBState().getInfoVer());

        long ret = vendorDBConn.update("vendortbl", contentValues, "vendorid = ? ", new String[]{String.valueOf(vinfo.getId())});
        if(ret== -1)
            return false;
        else
            return true;
    }

    public boolean updateInfoVersion (String id, int version) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("infoversion", version);
        vendorDBConn.update("vendortbl", contentValues, "vendorid = ? ", new String[]{String.valueOf(id)});
        return true;
    }

    //////
    public ArrayList<Or2goVendorInfo> getVendors() {

        ArrayList<Or2goVendorInfo> vendList;
        Cursor cursor;
        int count = 0;
        cursor = vendorDBConn.rawQuery("SELECT * FROM vendortbl", null);
        count = cursor.getCount();
        if (count <=0)
            return null;
        else {
            vendList = new ArrayList<Or2goVendorInfo>();
            cursor.moveToFirst();
            for(int i=0;i<count;i++) {
                //orderid text, itemid text, itemname text, price text, priceunit, quantity text, orderunit text, discount text, itemtotal text
                String vid = cursor.getString(cursor.getColumnIndexOrThrow("vendorid"));
                String vname = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String vtype = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String vstoretype = cursor.getString(cursor.getColumnIndexOrThrow("storetype"));
                String vdesc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow("tags"));
                String vaddr = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String vplace = cursor.getString(cursor.getColumnIndexOrThrow("place"));
                String vlocality = cursor.getString(cursor.getColumnIndexOrThrow("locality"));
                String vstate = cursor.getString(cursor.getColumnIndexOrThrow("state"));
                String vstatus = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String vminord = cursor.getString(cursor.getColumnIndexOrThrow("minorder"));
                String voptime = cursor.getString(cursor.getColumnIndexOrThrow("worktime"));
                String vclosed = cursor.getString(cursor.getColumnIndexOrThrow("closedon"));
                String logopath = cursor.getString(cursor.getColumnIndexOrThrow("logopath"));
                String dbname = cursor.getString(cursor.getColumnIndexOrThrow("dbname"));
                Integer proddbver = cursor.getInt(cursor.getColumnIndexOrThrow("dbversion"));
                Integer infover = cursor.getInt(cursor.getColumnIndexOrThrow("infoversion"));
                Integer pricever = cursor.getInt(cursor.getColumnIndexOrThrow("pricedbversion"));
                String shutfrom = cursor.getString(cursor.getColumnIndexOrThrow("shutfrom"));
                String shuttill = cursor.getString(cursor.getColumnIndexOrThrow("shuttill"));
                String shutres = cursor.getString(cursor.getColumnIndexOrThrow("shutreason"));
                Integer shuttype = cursor.getInt(cursor.getColumnIndexOrThrow("shuttype"));

                if (vminord == null) vminord="0";
                Or2goVendorInfo vendinfo = new Or2goVendorInfo(vid, vname, vtype, vstoretype, vdesc, tag, vaddr, vplace, vlocality, vstate, vstatus,
                        vminord, voptime, vclosed, logopath, dbname,proddbver,infover,pricever);
                vendinfo.setShutdownInfo(shutfrom,shuttill,shutres,shuttype);
                vendinfo.setProductStatus(OR2GO_VENDOR_PRODUCTLIST_EXIST);
                vendList.add(vendinfo);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return vendList;
    }
}
