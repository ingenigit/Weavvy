package com.or2go.weavvy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.or2go.core.Or2GoStore;
import com.or2go.core.SearchInfo;
import com.or2go.mylibrary.SearchDBHelper;
import com.or2go.weavvy.model.SearchStore;
import com.or2go.weavvy.model.StoreList;

import java.util.ArrayList;

public class StoreListDBHelper extends SQLiteOpenHelper {

    private static SearchDBHelper sInstance;
    public SQLiteDatabase storeDBConn;
    Context mContext;

    public StoreListDBHelper(Context context) {
        super(context, "storedb.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table storedata "+
                "(storename text, " +
                "storeid text, " +
                "type text , " +
                "contact text , " +
                "geo text, " +
                "UNIQUE(storename) ON CONFLICT IGNORE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS storedata");
        onCreate(db);
    }

    public void InitDB() {
        storeDBConn = this.getWritableDatabase();
    }

    public boolean insertStoreData (String id, String name, String type, String contact, String geo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("storeid", id);
        contentValues.put("storename", name);
        contentValues.put("type", type);
        contentValues.put("contact", contact);
        contentValues.put("geo", geo);
        long ret = storeDBConn.insert("storedata", null, contentValues);
        if(ret== -1)
            return false;
        else
            return true;
    }

    public boolean updateStoreData(StoreList storeList){
        ContentValues contentValues = new ContentValues();
        contentValues.put("storeid", storeList.getStringID());
        contentValues.put("storename", storeList.getStringName());
        contentValues.put("type", storeList.getStringType());
        contentValues.put("contact", storeList.getvContact());
        contentValues.put("geo", storeList.getGeolocation());
        long ret = storeDBConn.update("storedata", contentValues, "storeid = ? ", new String[]{storeList.getStringID()});
        if(ret== -1)
            return false;
        else
            return true;
    }

    public ArrayList<StoreList> getStoresList() {
        ArrayList<StoreList> storeLists;
        Cursor cursor;
        int count = 0;
        cursor = storeDBConn.rawQuery("SELECT * FROM storedata", null);
        count = cursor.getCount();
        if (count <= 0)
            return null;
        else {
            storeLists = new ArrayList<StoreList>();
            cursor.moveToFirst();
            for(int i = 0; i < count; i++) {
                String storeid = cursor.getString(cursor.getColumnIndexOrThrow("storeid"));
                String storename = cursor.getString(cursor.getColumnIndexOrThrow("storename"));
                String storetype = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String storecontact = cursor.getString(cursor.getColumnIndexOrThrow("contact"));
                String storegeo = cursor.getString(cursor.getColumnIndexOrThrow("geo"));
                StoreList storeList = new StoreList(storeid, storename, storetype, storecontact, storegeo, false);
                storeLists.add(storeList);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return storeLists;
    }

    public boolean searchStore (String name, ArrayList<StoreList> searchStores){
        if (searchStores == null) {
            Toast.makeText(mContext, "empty array", Toast.LENGTH_SHORT).show();
            return false;
        }
        searchStores.clear();
        //Cursor cursor;
        int count = 0;
        Cursor cursor = storeDBConn.query(true, "storedata", new String[]{ "storeid", "storename", "type", "contact", "geo"},
                "storename LIKE '%" + name + "%'", null, null, null, null, null);
        count = cursor.getCount();
        if (count >0) {
            cursor.moveToFirst();
            for (int i = 0; i < count; i++) {
                StoreList searchStore = new StoreList();
                searchStore.stringID = cursor.getString(cursor.getColumnIndexOrThrow("storeid"));
                searchStore.stringName = cursor.getString(cursor.getColumnIndexOrThrow("storename"));
                searchStore.stringType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                searchStore.vContact = cursor.getString(cursor.getColumnIndexOrThrow("contact"));
                searchStore.geolocation = cursor.getString(cursor.getColumnIndexOrThrow("geo"));
                searchStores.add(searchStore);
                cursor.moveToNext();
            }
        }
        return true;
    }
}
