package com.or2go.vendor.weavvy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.or2go.core.ProductInfo;
import com.or2go.core.ProductPriceInfo;
import com.or2go.core.ProductSKU;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductDBHelper extends SQLiteOpenHelper {
    public SQLiteDatabase productDBConn;

    public ProductDBHelper(Context context, String storeid) {
        super(context, storeid+"Products", null, 1);
        InitProductDB();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //V5  : added coulumn incontrol to table prodinfo
        db.execSQL("create table prodinfo "+
                "(id INTEGER PRIMARY KEY, name text, shortname text, description text,"+
                "prodcode text, hsncode text, barcode text, prodtype integer, category text , subcategory text, brand text,"+
                "priceunit integer , pricetype integer, price REAL, maxprice REAL, packtype Integer, taxinclusion INTEGER DEFAULT 1, taxrate REAL, tag text, dbver Integer, invcontrol INTEGER DEFAULT 0, imgpath DEFAULT 0)");
        /*,UNIQUE(name) ON CONFLICT IGNORE*/
        db.execSQL("create table category "+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, category text, refundable INTEGER DEFAULT 0, exchangeable INTEGER DEFAULT 0,  usestock INTEGER DEFAULT 0, UNIQUE(category) ON CONFLICT IGNORE)");
        //db.execSQL("create table priceinfo "+
        //        "(priceid INTEGER PRIMARY KEY AUTOINCREMENT, prodid INTEGER, skuid INTEGER, unit INTEGER, amount REAL, saleprice REAL, maxprice REAL, taxincl Integer, manualprice Integer, dbver Integer)");
        db.execSQL("create table skuinfo "+
                "(skuid INTEGER PRIMARY KEY AUTOINCREMENT, prodid INTEGER, name text, description text, unit INTEGER, unitamount INTEGER, unitcount INTEGER,amount REAL, size text, color text, model text, dimension text, weight Integer, pkgtype text, stckstus Integer, dbver Integer)");
        db.execSQL("create table subcategory "+
                "(id INTEGER PRIMARY KEY   AUTOINCREMENT, category text, subcategory text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void InitProductDB() {
        productDBConn = this.getWritableDatabase();
        ///productDBConn = this.getReadableDatabase();
    }

    public SQLiteDatabase getProductDBConn()
    {
        return productDBConn;
    }

    public boolean addProduct(ProductInfo prod) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", prod.id);
        contentValues.put("name", prod.name);
        contentValues.put("shortname", prod.brandname);
        contentValues.put("description", prod.desc);
        contentValues.put("prodcode", prod.code);
        contentValues.put("hsncode", prod.gstcode);
        contentValues.put("barcode", prod.barcode);
//        contentValues.put("prodtype", prod.type);
        contentValues.put("category", prod.category);
        contentValues.put("subcategory", prod.subcategory);
        contentValues.put("brand", "");
        contentValues.put("priceunit", prod.unit);
        contentValues.put("pricetype", prod.pricetype);
        contentValues.put("price", prod.price);
        contentValues.put("taxinclusion", prod.taxincl);
        contentValues.put("taxrate", prod.taxrate);
        contentValues.put("packtype", prod.packtype);
        contentValues.put("maxprice", prod.maxprice);
        contentValues.put("tag", prod.tag);
        contentValues.put("invcontrol", prod.invcontrol);
        contentValues.put("imgpath", prod.imagepath);

        long ret = productDBConn.insert("prodinfo", null, contentValues);
        if (ret > 0) return true;
        else return false;
    }

    public int getItemCount() {
        Cursor cursor;
        int count=0;
        cursor = productDBConn.rawQuery("SELECT * FROM prodinfo", null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean deleteProductData() {
        productDBConn.delete("prodinfo", null, null);
        productDBConn.delete("category", null, null);
        productDBConn.delete("subcategory", null, null);
        return true;
    }

    public boolean deletePriceData() {
        productDBConn.delete("priceinfo", null, null);
        return true;
    }

    public boolean deleteSKUData() {
        productDBConn.delete("skuinfo", null, null);
        return true;
    }

    public Integer deleteproduct (Integer id) {
        int ret=0;
        ret = productDBConn.delete("prodinfo", "id = ? ",new String[] { Integer.toString(id) });
        return ret;
    }

    public boolean getAllProducts(HashMap<Integer, ProductInfo> mapprdinfo){
        ArrayList<ProductInfo>  prdlist = new  ArrayList<ProductInfo> ();
        Cursor cr;
        int count;
        boolean invcontrol=false;

        SQLiteDatabase userDBConn = this.getWritableDatabase();
        cr = userDBConn.rawQuery("SELECT * FROM prodinfo ", null);
        count = cr.getCount();

        if (count >0) {
            cr.moveToFirst();
            for (int i = 0; i < count; i++) {
                ProductInfo iteminfo = new ProductInfo();
                iteminfo.id = cr.getInt(cr.getColumnIndexOrThrow("id"));
                iteminfo.name = cr.getString(cr.getColumnIndexOrThrow("name"));
                iteminfo.brandname = cr.getString(cr.getColumnIndexOrThrow("shortname"));
                iteminfo.desc = cr.getString(cr.getColumnIndexOrThrow("description"));
//                iteminfo.type = cr.getInt(cr.getColumnIndexOrThrow("category"));
                iteminfo.category = cr.getString(cr.getColumnIndexOrThrow("category"));
                iteminfo.subcategory = cr.getString(cr.getColumnIndexOrThrow("subcategory"));
                iteminfo.code = cr.getString(cr.getColumnIndexOrThrow("prodcode"));
                iteminfo.barcode = cr.getString(cr.getColumnIndexOrThrow("barcode"));
                iteminfo.gstcode = cr.getString(cr.getColumnIndexOrThrow("hsncode"));
                iteminfo.packtype = cr.getInt(cr.getColumnIndexOrThrow("packtype"));
                iteminfo.price =cr.getFloat(cr.getColumnIndexOrThrow("price"));
                iteminfo.unit = cr.getInt(cr.getColumnIndexOrThrow("priceunit"));
                iteminfo.taxincl = cr.getInt(cr.getColumnIndexOrThrow("taxinclusion"));
                iteminfo.taxrate =cr.getFloat(cr.getColumnIndexOrThrow("taxrate"));
                iteminfo.tag = cr.getString(cr.getColumnIndexOrThrow("tag"));
                iteminfo.invcontrol= cr.getInt(cr.getColumnIndexOrThrow("invcontrol"));
                iteminfo.imagepath= cr.getInt(cr.getColumnIndexOrThrow("imgpath"));
                if ((!invcontrol) && (iteminfo.invcontrol==1)) invcontrol=true;
                mapprdinfo.put(iteminfo.id, iteminfo);
                cr.moveToNext();
            }
        }
        return invcontrol;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //SKU APIs
    ////////////////////////////////////////////////////////////////////////////////////////////
    public boolean addSKUData(ProductSKU skudata) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("skuid", skudata.mSKUId);
        contentValues.put("prodid", skudata.mProdId);
        contentValues.put("unit", skudata.mUnit);
//        contentValues.put("unitamount", skudata.mUnitAmount);
//        contentValues.put("unitcount", skudata.mUnitCount);
        contentValues.put("amount", skudata.mAmount);
        contentValues.put("size", skudata.mSize);
        contentValues.put("color", skudata.mColor);
        contentValues.put("model", skudata.mModel);
        contentValues.put("dimension", skudata.mDimension);
        contentValues.put("weight", skudata.mWeight);
        contentValues.put("pkgtype", skudata.mPkgType);
        contentValues.put("stckstus", skudata.mStockStatus);
        contentValues.put("dbver", skudata.mDBVer);

        long ret = productDBConn.insert("skuinfo", null, contentValues);
        if (ret > 0) {
            System.out.println("ProductDBHelper: SKU insert successful"+skudata.mName);
            return true;
        }
        else {
            System.out.println("ProductDBHelper: SKU insert failed"+skudata.mName);
            return false;
        }
    }


    public ArrayList<ProductSKU> getSKUData(Integer prodid) {
        //new String[]{ "packid", "prodid", "unit" , "unitcount" , "unitamout", "packamount"}
        Cursor cr = productDBConn.query(false, "skuinfo", null,
                "prodid=?",new String[]{ prodid.toString()},
                null, null, null, null);

        int cnt = cr.getCount();
        if(cnt>0 ) {
            ArrayList<ProductSKU> itemlist = new ArrayList<ProductSKU>();
            cr.moveToFirst();
            for (int i=0; i < cnt; i++)
            {
                Integer skuid = cr.getInt(cr.getColumnIndexOrThrow("skuid"));
                String name = cr.getString(cr.getColumnIndexOrThrow("name"));
                String desc = cr.getString(cr.getColumnIndexOrThrow("description"));
                Integer unit = cr.getInt(cr.getColumnIndexOrThrow("unit"));
                Integer unitamount = cr.getInt(cr.getColumnIndexOrThrow("unitamount"));
                Integer unitcount = cr.getInt(cr.getColumnIndexOrThrow("unitcount"));
                String amnt = cr.getString(cr.getColumnIndexOrThrow("amount"));
                String size = cr.getString(cr.getColumnIndexOrThrow("size"));
                String color = cr.getString(cr.getColumnIndexOrThrow("color"));
                String model = cr.getString(cr.getColumnIndexOrThrow("model"));
                String dimen = cr.getString(cr.getColumnIndexOrThrow("dimension"));
                String weight = cr.getString(cr.getColumnIndexOrThrow("weight"));
                String pkg = cr.getString(cr.getColumnIndexOrThrow("pkgtype"));
                Integer stckSts = cr.getInt(cr.getColumnIndexOrThrow("stckstus"));
                Integer ver = cr.getInt(cr.getColumnIndexOrThrow("dbver"));
                ProductSKU packinfo = new ProductSKU(skuid, prodid, name, desc,
                        unit, Float.parseFloat(amnt), Float.parseFloat(amnt), Float.parseFloat(amnt),
                        size, color, model, weight, dimen, pkg, stckSts);

                itemlist.add(packinfo);
                cr.moveToNext();
            }
            cr.close();
            return itemlist;
        }
        else {
            cr.close();
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //category APIs
    ////////////////////////////////////////////////////////////////////////////////////////////
    public int addCategory(String cat) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", cat);
        long ret = productDBConn.insert("category", null, contentValues);
        return ((int)ret);
    }

    public boolean addSubCategory(String category, String subcategory) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("category", category);
        contentValues.put("subcategory", subcategory);
        long ret = productDBConn.insert("subcategory", null, contentValues);
        if (ret > 0)
            return true;
        else
            return false;
    }

    public boolean getProductsCategories(ArrayList<String> typelist) {
        Cursor  cursor = productDBConn.rawQuery("select * from category",null);

        int typecnt = cursor.getCount();
        if ( typecnt <= 0) {
            cursor.close();
            return false;
        }

        cursor.moveToFirst();
        //ArrayList<String> typelist = new ArrayList();

        for(int i=0; i< typecnt; i++)
        {
            String itemtype = cursor.getString(cursor.getColumnIndexOrThrow("category"));

            typelist.add(itemtype);

            cursor.moveToNext();
        }

        cursor.close();

        return true;
    }

    public ArrayList<String> getSubCategories(String category) {
        Cursor cursor = productDBConn.query(false, "subcategory", new String[]{ "subcategory"}, "category=?",new String[]{ category},
                null, null, null, null);
        int typecnt = cursor.getCount();
        if ( typecnt <= 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        ArrayList<String> typelist = new ArrayList<String>();

        for(int i=0; i< typecnt; i++) {
            String subcategory = cursor.getString(cursor.getColumnIndexOrThrow("subcategory"));
            typelist.add(subcategory);
            cursor.moveToNext();
        }
        cursor.close();
        return typelist;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //MISC APIs
    ///////////////////////////////////////////////////////////////////////////////////////
    private ArrayList<Integer> getProductsByType(String category) {
        Cursor cr = productDBConn.query(false, "prodinfo", new String[]{ "id"}, "category=?",new String[]{ category},
                null, null, null, null);

        int cnt = cr.getCount();
        if(cnt>0 ) {
            ArrayList<Integer> itemlist = new ArrayList<Integer>();
            cr.moveToFirst();

            for (int i=0; i < cnt; i++) {
                Integer id = cr.getInt(cr.getColumnIndexOrThrow("id"));
                itemlist.add(id);
                cr.moveToNext();
            }
            cr.close();
            return itemlist;
        }
        else {
            cr.close();
            return null;
        }
    }

    private ArrayList<Integer> getProductsBySubtype(String subcategory) {
        Cursor cr = productDBConn.query(false, "prodinfo", new String[]{ "id"}, "subcategory=?",new String[]{ subcategory},
                null, null, null, null);

        int cnt = cr.getCount();
        if(cnt>0 ) {
            ArrayList<Integer> itemlist = new ArrayList<Integer> ();
            cr.moveToFirst();
            for (int i=0; i < cnt; i++) {
                Integer id = cr.getInt(cr.getColumnIndexOrThrow("id"));
                itemlist.add(id);
                cr.moveToNext();
            }
            cr.close();
            return itemlist;
        }
        else {
            cr.close();
            return null;
        }
    }
}
