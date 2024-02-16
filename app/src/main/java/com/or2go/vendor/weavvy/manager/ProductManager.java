package com.or2go.vendor.weavvy.manager;

import android.content.Context;

import com.or2go.core.ProductInfo;
import com.or2go.core.ProductPriceInfo;
import com.or2go.core.ProductSKU;
import com.or2go.core.SalesSelectInfo;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.db.ProductDBHelper;
import com.or2go.vendor.weavvy.storeList.StoreList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductManager {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    String mStoreId;

    ProductDBHelper mProductDb;


    //private DBHelper mProductDB;
    ///SQLiteDatabase mProductDBConn;

    // private HSNCodeDBHelper mHSNdb;

    ArrayList<String> listProductTypes;
    HashMap<String, List<String>> listProductSubType;
    HashMap<Integer, ProductInfo> mapProductInfo;

    boolean mInventoryMgmt;
    ArrayList<SalesSelectInfo> salesItems;

    //HashMap<Integer, ArrayList<ProductPackInfo>> mapProductPackInfo;

    public ProductManager(Context context, String storeid)
    {
        mContext =context;

        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();

        mStoreId = storeid;

        ///must be set
        mProductDb = new ProductDBHelper(context, storeid);

        //mProductDB = gAppEnv.getProductDB();

        // mHSNdb = gAppEnv.getHSNDB();

        mapProductInfo = new HashMap<Integer, ProductInfo>();
        listProductTypes = new ArrayList<String>();
        listProductSubType = new HashMap<String, List<String>>();

        //mapProductPackInfo = new HashMap<Integer, ArrayList<ProductPackInfo>>();
        salesItems = new ArrayList<>();

        //listSaleProductTypes = new ArrayList<String>();
        //listInventoryProductTypes = new ArrayList<String>();

    }

    //public void setDBHelper(ProductDBHelper dbhelper) { mProductDb = dbhelper;}
	/*
	void InitProductData()
	{
		int typecnt, subtypecnt;

		SQLiteDatabase sqldb=null;
		//mProductDB = gAppEnv.getProductDB();
		//sqldb = mProductDB.getProductDBConn();////gAppEnv.getMainDB();

		sqldb = mProductDB.getProductDBConn();


		initProducts(sqldb);




	}


	boolean initProducts(SQLiteDatabase sqldb)
	{
		//boolean bResult = false;
		int cnt = 0;

		Cursor  itemcursor = sqldb.query(false, "goodsmgnt1", new String[]{ "id","itemname","icode","itemtype","itemsubtype", "barcode", "gstcode"}, null,null,null, null, null, null);
		cnt = itemcursor.getCount();

		System.out.println("Product Manager: init products db query products count="+cnt);

		if ( cnt < 1) {

            itemcursor.close();
            return false;

		}
		else
		{
			itemcursor.moveToFirst();

			for(int i=0;i<cnt;i++)
    		{
				ProductInfo prod = new ProductInfo();

				prod.id = itemcursor.getInt(itemcursor.getColumnIndex("id"));
				prod.name = itemcursor.getString(itemcursor.getColumnIndex("itemname"));
				prod.type = itemcursor.getString(itemcursor.getColumnIndex("itemtype"));
				prod.subtype = itemcursor.getString(itemcursor.getColumnIndex("itemsubtype"));
				prod.gstcode = itemcursor.getString(itemcursor.getColumnIndex("gstcode"));
				prod.barcode = itemcursor.getString(itemcursor.getColumnIndex("barcode"));
				prod.code = itemcursor.getString(itemcursor.getColumnIndex("icode"));

        		//get product price and unit
        		Cursor itemdetailscursor;
        		itemdetailscursor = sqldb.query(false, "goodsmgnt2", new String[]{"itemprice", "priceunit", "taxincl", "discrate","disctype"}, "id=?", new String[]{String.valueOf(prod.id)},
                        null, null, null, null);
        		if (itemdetailscursor.getCount() < 1) {


                    prod.price = "";
        			prod.unit = "";
        			prod.taxincl = 0;
        			prod.discrate = "";
        			prod.disctype = "";

                }
        		else
        		{
        			itemdetailscursor.moveToFirst();

        			prod.price = itemdetailscursor.getString(itemdetailscursor.getColumnIndex("itemprice"));
        			prod.unit = itemdetailscursor.getString(itemdetailscursor.getColumnIndex("priceunit"));
        			prod.taxincl = itemdetailscursor.getInt(itemdetailscursor.getColumnIndex("taxincl"));
        			prod.discrate = itemdetailscursor.getString(itemdetailscursor.getColumnIndex("discrate"));;
        			prod.disctype = itemdetailscursor.getString(itemdetailscursor.getColumnIndex("disctype"));;

        		}
        		itemdetailscursor.close();

        		Cursor itemimagecursor;
        		//get product image
        		itemimagecursor = sqldb.query(false, "productimg", new String[]{ "id", "itemimage"}, "id=?", new String[]{String.valueOf(prod.id)},
                        null, null, null, null);
        		if (itemimagecursor.getCount() < 1) {

        			//prod.imgdata = null;
        			prod.itemimg = null;

                }
        		else
        		{
        			itemimagecursor.moveToFirst();

        			//prod.imgdata = itemimagecursor.getBlob(itemimagecursor.getColumnIndex("itemimage"));
        			byte[] itemimgblob = itemimagecursor.getBlob(itemimagecursor.getColumnIndex("itemimage"));
        			if (itemimgblob != null)
        				prod.itemimg = BitmapFactory.decodeByteArray(itemimgblob, 0, itemimgblob.length);
        			else
        				prod.itemimg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.blankitem);//null;
        		}
        		itemimagecursor.close();

        		////System.out.println("Product Manager: Adding product info to hashmap: id="+prod.id+" name:"+prod.name);
        		//add product info object to products hash map
        		mapProductInfo.put(prod.id, prod);

                itemcursor.moveToNext();



    		} //loop end

			itemcursor.close();

		}

		return true;



	}
	*/

    boolean DeinitProductsData()
    {
        mapProductInfo.clear();
        listProductTypes.clear();
        //listProductSubType.clear();

        return true;
    }


    int getProductsCount()
    {
        return (mapProductInfo.size());
    }
	/*
	ProductInfo getProductInfo(int id)
	{
		ProductInfo prod = new ProductInfo();

		prod = mapProductInfo.get(id);

		return prod;

	}
	*/

    public int getDbProductCount()
    {
        return mProductDb.getItemCount();
    }

    public boolean initProductsFromDB()
    {
		/*
		ArrayList<ProductInfo> prdDbList = mProductDb.getAllProducts();
		int prdcnt = prdDbList.size();

		for (int i=0; i<prdcnt; i++)
		{
			mapProductInfo.put(prdDbList.get(i).id, prdDbList.get(i));
		}*/

        mInventoryMgmt = mProductDb.getAllProducts(mapProductInfo);
        if (mInventoryMgmt) {
            //System.out.println("Requires inventory management vendorid=" + mVendiorId);
        }

        mProductDb.getProductsCategories(listProductTypes);

        //add subcategories
        Iterator<String> catIterator = listProductTypes.iterator();
        while(catIterator.hasNext())
        {
            String cat = catIterator.next();
            ArrayList<String> subcatlist = mProductDb.getSubCategories(cat);
            if (subcatlist != null)  listProductSubType.put(cat, subcatlist);

        }

        Iterator hmIterator = mapProductInfo.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            ProductInfo prod = ((ProductInfo)mapElement.getValue());
//            prod.mPriceInfoList = mProductDb.getProductPriceData(prod.id);

            prod.mSKUList = mProductDb.getSKUData(prod.id);

        }

        return true;
    }

    public ProductInfo getProductInfo(int id)
    {
        return mapProductInfo.get(id);

    }


    public boolean addProductInfo(ProductInfo newprod)
    {
        ///int id = newprod.id;

        mapProductInfo.put(newprod.id, newprod);

		/*mProductDb.addproduct(newprod.name, newprod.shortname, newprod.desc, newprod.code, newprod.gstcode, newprod.barcode, newprod.type,
				newprod.category, newprod.subcategory, "", newprod.unit, newprod.pricetype, newprod.price,
				newprod.taxincl, newprod.taxrate, newprod.tag);*/

        return true;

    }

    public synchronized boolean addProductInfoToDB(ProductInfo prod)
    {
        return mProductDb.addProduct(prod);
    }

    public synchronized boolean addProductsToDB()
    {
        // Getting an iterator
        Iterator hmIterator = mapProductInfo.entrySet().iterator();

        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();

            ProductInfo prod = ((ProductInfo)mapElement.getValue());
            mProductDb.addProduct(prod);
        }

        //save category list to DB
        int catcnt = listProductTypes.size();
        for(int i =0; i<catcnt; i++)
        {
            mProductDb.addCategory(listProductTypes.get(i));
        }

        //save subcategory list to DB
        for (Map.Entry<String, List<String>> entry : listProductSubType.entrySet()) {
            listProductSubType.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            String category = entry.getKey();
            List<String> subcatlist = entry.getValue();
            int subcatcnt = subcatlist.size();
            for(int i =0; i<subcatcnt; i++)
            {
                mProductDb.addSubCategory(category, subcatlist.get(i));
            }

        }

        return true;
    }

    public synchronized boolean addPricesToDB()
    {
        // Getting an iterator
        Iterator hmIterator = mapProductInfo.entrySet().iterator();

        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();

            ProductInfo prod = ((ProductInfo)mapElement.getValue());
//            if (prod.mPriceInfoList!=null) {
//
//                int packcnt = prod.mPriceInfoList.size();
//                for(int i=0; i<packcnt; i++) {
//                    ProductPriceInfo pkinfo = prod.mPriceInfoList.get(i);
//                    mProductDb.addPriceData(pkinfo);
//                }
//            }
        }

        return true;
    }

    public synchronized boolean addSKUToDB()
    {
        // Getting an iterator
        Iterator hmIterator = mapProductInfo.entrySet().iterator();

        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)hmIterator.next();

            ProductInfo prod = ((ProductInfo)mapElement.getValue());
            if (prod.mSKUList!=null) {

                int packcnt = prod.mSKUList.size();
                for(int i=0; i<packcnt; i++) {
                    ProductSKU pkinfo = prod.mSKUList.get(i);
                    mProductDb.addSKUData(pkinfo);
                }
            }
        }

        return true;
    }

    /*
    public boolean addProductPriceInfo(ProductPackInfo packdata)
    {
        ArrayList<ProductPackInfo> packlist = mapProductPackInfo.get(packdata.mProdId);
        if (packlist==null) {
            ArrayList<ProductPackInfo> npacklist = new ArrayList<ProductPackInfo>();
            npacklist.add(packdata);
            mapProductPackInfo.put(packdata.mProdId, npacklist);
        }
        else
            packlist.add(packdata);

        return true;

    }*/
    public boolean addProductPriceInfo(int prodid, ProductPriceInfo pricedata)
    {
        ProductInfo prodinfo = mapProductInfo.get(prodid);

//        if (prodinfo != null) prodinfo.addPriceInfo(pricedata);

        return true;
    }

    public boolean addProductSKU(int prodid, ProductSKU skudata)
    {
        ProductInfo prodinfo = mapProductInfo.get(prodid);

        if (prodinfo != null) prodinfo.addSKUInfo(skudata);

        return true;
    }

    public boolean setCategoryList(ArrayList<String> clist)
    {
        //listProductTypes = clist;
        listProductTypes.clear();

        int cnt = clist.size();
        for (int i = 0; i < cnt ; i++) {
            listProductTypes.add(clist.get(i));
            //mProductDb.addCategory(clist.get(i));
        }
        return true;
    }

    public boolean setSubCVategoryList(HashMap<String, List<String>> subcatdata)
    {
        listProductSubType.clear();

        //copy the subcategory hashmap
        for (Map.Entry<String, List<String>> entry : subcatdata.entrySet()) {
            listProductSubType.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return true;

    }
	/*
	boolean addCategory(String cat)
	{
		listProductTypes.add(cat);

		return true;
	}*/

    public synchronized boolean clearProductData()
    {
        mapProductInfo.clear();
        listProductTypes.clear();
        listProductSubType.clear();

        return mProductDb.deleteProductData();
    }

    synchronized boolean clearPriceData()
    {
        //mapProductInfo.clear();
        Integer prodid;
        ProductInfo prod;
        for (Map.Entry<Integer, ProductInfo> entry : mapProductInfo.entrySet())
        {
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            prodid = entry.getKey();
            prod = entry.getValue();

//            if (prod.mPriceInfoList!= null) prod.mPriceInfoList.clear();
        }

        return mProductDb.deletePriceData();
    }

    public synchronized boolean clearSKUData()
    {
        //mapProductInfo.clear();
        Integer prodid;
        ProductInfo prod;
        for (Map.Entry<Integer, ProductInfo> entry : mapProductInfo.entrySet())
        {
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            prodid = entry.getKey();
            prod = entry.getValue();

            if (prod.mSKUList!= null) prod.mSKUList.clear();
        }

        return mProductDb.deleteSKUData();
    }
	/*
	boolean deleteProductInfo(int id)
	{
		if (mapProductInfo.containsKey(id))
		{
			mapProductInfo.remove(id);

			return true;
		}
		else
			return false;
	}

	Bitmap getProductImage(int id)
	{
		ProductInfo prdata = mapProductInfo.get(id);
		if (prdata == null)
			return null;
		else
		{
			Bitmap itemimg;

	    	itemimg =  prdata.itemimg;

	    	if (itemimg == null)
	    	{
	    		Bitmap blankimage = BitmapFactory.decodeResource(gAppEnv.getResources(), R.drawable.blankitem);

	    		return blankimage;
	    	}
	    	else
	    		return itemimg;
		}
	}
	*/

    Integer getProductUnit(int id)
    {
        ProductInfo prdata = mapProductInfo.get(id);
        if (prdata == null)
            return null;
        else
            return  prdata.unit;
    }

    String getProductPrice(int id)
    {

        return null;
    }

    String getQuantityByAmount(int id, String amnt)
    {
        String samnt  = "0";

        return samnt;
    }

    String getDiscountRate(int id)
    {
        return "0";
    }


	/*
	String getGstRate(int id)
	{
		String gstrate;

		SQLiteDatabase mProductDBConn = mProductDB.getProductDBConn();
		//SQLiteDatabase mHsnDBConn = mHSNdb.getHSNDBConn();

	        ////SQLiteDatabase db = this.getReadableDatabase();
	    Cursor curres =  mProductDBConn.rawQuery( "select * from goodsmgnt1 where id="+id+"", null );
	    if(curres.getCount() < 0)
	    {
	    	return null;
	    }
	    curres.moveToFirst();

	    String hsncode = curres.getString(curres.getColumnIndex("gstcode"));

	    if (hsncode.equals("0000"))
	    {
	    	gstrate = gAppEnv.getAppSetting("global_tax_preference");;

	    }
	    else
	    {
	    	gstrate = mHSNdb.findRate(hsncode);

	    	if (gstrate.equals("NIL"))
	    	{
	    		gstrate = "0";
	    	}
	    	else
	    	{
	    		gstrate = gstrate.replace("%","");

	    	}
	    }
		return gstrate;
	}
*/



    public HashMap<Integer, ProductInfo> getProductList()
    {
        return mapProductInfo;
    }

    public List<String> getProductTypes()
    {
        return listProductTypes;
    }
    public HashMap<String, List<String>> getProductSubCategories() { return listProductSubType;}


	/*
	public HashMap<String, List<String>> getProductSubTypes()
	{
		return listProductSubType;

	}

	public List<String> getSubtypes(String type)
	{
		List<String> sublist = listProductSubType.get(type);

		return sublist;

	}
	*/




    public boolean getProductTaxInclSetting(int id)
    {
        ProductInfo prod = getProductInfo(id);

        if (prod.taxincl == 1)
            return true;
        else
            return false;
    }

    /***************************************************************/

    public boolean getAllItems(ArrayList<SalesSelectInfo> itemlist) {
        itemlist.clear();
        // Getting an iterator
        Iterator hmIterator = mapProductInfo.entrySet().iterator();
        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) hmIterator.next();
            ProductInfo prod = ((ProductInfo) mapElement.getValue());
            SalesSelectInfo ssinfo = new SalesSelectInfo(prod);
            System.out.println("ProductManager GetAllItems : product name=" + prod.name);
            itemlist.add(ssinfo);
        }
        return true;
    }

    public Integer getTotalProductsCount(){
        ArrayList<StoreList> storeLists = gAppEnv.getStoreManager().getStoreName();
        getAllItems(salesItems);
        return salesItems.size();
    }

}
