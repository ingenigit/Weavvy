package com.or2go.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_COMM_ASYNC_API;
import static com.or2go.core.Or2goConstValues.OR2GO_ITEM_STOCK_VAL;
import static com.or2go.core.Or2goConstValues.OR2GO_SEARCH_PRODUCT_NAME;
import static com.or2go.weavvy.manager.OrderCartManager.CART_STOCK_CHECK_NONE;
import static com.or2go.weavvy.manager.OrderCartManager.CART_STOCK_CHECK_REQUEST;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.or2go.core.CartItem;
import com.or2go.core.Or2GoStore;
import com.or2go.core.ProductInfo;
import com.or2go.core.ProductSKU;
import com.or2go.core.ProductSalesViewInfo;
import com.or2go.core.SalesSelectInfo;
import com.or2go.core.SearchInfo;
import com.or2go.mylibrary.ProductDBHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.server.StockStatusCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProductManager {

	private Context mContext;
	 // Get Application super class for global data
	AppEnv gAppEnv;
	String mVendiorId;
	ProductDBHelper mProductDb;
	ArrayList<String> listProductTypes;
	HashMap<String, List<String>> listProductSubType;
	HashMap<Integer, ProductInfo> mapProductInfo;
	boolean mInventoryMgmt;
	Integer mStockCheckStatus;
	ArrayList<ProductSKU> mSKUlist;

	public ProductManager (Context context){
		mContext =context;
		//Get global application
		gAppEnv = (AppEnv)context;
		mSKUlist = new ArrayList<ProductSKU>();
		mStockCheckStatus = CART_STOCK_CHECK_NONE;
	}

	public ProductManager(Context context, String vendorid) {
    	mContext =context;
    	//Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
		mVendiorId = vendorid;
		mProductDb = new ProductDBHelper(context, vendorid);
		mSKUlist = new ArrayList<ProductSKU>();
        mapProductInfo = new HashMap<Integer, ProductInfo>();
        listProductTypes = new ArrayList<String>();
		listProductSubType = new HashMap<String, List<String>>();
    }

	int getDbProductCount()
	{
		return mProductDb.getItemCount();
	}

	boolean initProductsFromDB() {
		mInventoryMgmt = mProductDb.getAllProducts(mapProductInfo);
		if (mInventoryMgmt) {
			//System.out.println("Requires inventory management vendorid=" + mVendiorId);
		}

		mProductDb.getProductsCategories(listProductTypes);
		//add subcategories
		Iterator<String> catIterator = listProductTypes.iterator();
		while(catIterator.hasNext()) {
			String cat = catIterator.next();
			ArrayList<String> subcatlist = mProductDb.getSubCategories(cat);
			if (subcatlist != null)  listProductSubType.put(cat, subcatlist);
		}

		Iterator hmIterator = mapProductInfo.entrySet().iterator();
		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry)hmIterator.next();
			ProductInfo prod = ((ProductInfo)mapElement.getValue());
			///prod.mPriceInfoList = mProductDb.getProductPriceData(prod.id);
			///if (prod.mPriceInfoList == null) Log.i("ProductManager" , "price info list NULL for id="+prod.id);
			prod.mSKUList = mProductDb.getSKUData(prod.id);
			if (prod.mSKUList == null) Log.i("ProductManager" , "SKU list NULL for id="+prod.id);
		}
		return true;
	}

	public ProductInfo getProductInfo(int id) {
		return mapProductInfo.get(id);
	}

	public boolean addProductInfo(ProductInfo newprod) {
		mapProductInfo.put(newprod.id, newprod);
		return true;
	}

	public synchronized boolean addProductsToDB() {
		// Getting an iterator
		Iterator hmIterator = mapProductInfo.entrySet().iterator();

		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry)hmIterator.next();
			ProductInfo prod = ((ProductInfo)mapElement.getValue());
			mProductDb.addProduct(prod);
		}

		//save category list to DB
		int catcnt = listProductTypes.size();
		for(int i =0; i<catcnt; i++) {
			mProductDb.addCategory(listProductTypes.get(i));
		}

		//save subcategory list to DB
		for (Map.Entry<String, List<String>> entry : listProductSubType.entrySet()) {
			listProductSubType.put(entry.getKey(), new ArrayList<>(entry.getValue()));
			String category = entry.getKey();
			List<String> subcatlist = entry.getValue();
			int subcatcnt = subcatlist.size();
			for(int i =0; i<subcatcnt; i++) {
				mProductDb.addSubCategory(category, subcatlist.get(i));
			}
		}
		return true;
	}

	public synchronized boolean addSKUToDB() {
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

	public boolean addProductSKU(int prodid, ProductSKU skudata) {
		ProductInfo prodinfo = mapProductInfo.get(prodid);
		if (prodinfo != null) prodinfo.addSKUInfo(skudata);
		return true;
	}

	public boolean setCategoryList(ArrayList<String> clist) {
        //listProductTypes = clist;
		listProductTypes.clear();
		int cnt = clist.size();
		for (int i = 0; i < cnt ; i++) {
			listProductTypes.add(clist.get(i));
			//mProductDb.addCategory(clist.get(i));
		}
        return true;
    }

    public boolean setSubCVategoryList(HashMap<String, List<String>> subcatdata) {
		listProductSubType.clear();
		//copy the subcategory hashmap
		for (Map.Entry<String, List<String>> entry : subcatdata.entrySet()) {
			listProductSubType.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		return true;
	}

	synchronized boolean clearProductData() {
		mapProductInfo.clear();
		listProductTypes.clear();
		listProductSubType.clear();
		return mProductDb.deleteProductData();
	}

	synchronized boolean clearSKUData() {
		//mapProductInfo.clear();
		Integer prodid;
		ProductInfo prod;
		for (Map.Entry<Integer, ProductInfo> entry : mapProductInfo.entrySet()) {
			//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			prodid = entry.getKey();
			prod = entry.getValue();

			if (prod.mSKUList!= null) prod.mSKUList.clear();
		}
		return mProductDb.deleteSKUData();
	}

	public List<String> getProductTypes()
	{
		return listProductTypes;		
	}

	public HashMap<String, List<String>> getProductSubCategories() { return listProductSubType;}

	public boolean getAllItems(ArrayList<SalesSelectInfo> itemlist) {
		boolean cartcheck = false;
		OrderCartManager cartMgr = gAppEnv.getCartManager();
		itemlist.clear();

		if ((cartMgr.getCurrentVendor().equals(mVendiorId)) && (cartMgr.getCartSize()>0))
			cartcheck= true;

		// Getting an iterator
		Iterator hmIterator = mapProductInfo.entrySet().iterator();

		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry) hmIterator.next();
			ProductInfo prod = ((ProductInfo) mapElement.getValue());
			SalesSelectInfo ssinfo = new SalesSelectInfo(prod);
			ArrayList<ProductSKU> skuList = prod.mSKUList;
			if ((skuList==null) || (skuList.size() ==0))
				continue;

			if (cartcheck == true) {
				if (skuList != null) {
					int pkcnt = skuList.size();
					for (int i = 0; i < pkcnt; i++) {
						//check if the product SKU/price is in cart
						ProductSKU isku = skuList.get(i);
						CartItem cartp = cartMgr.getOrderPackItemById(prod.id, isku.mSKUId);
						if (cartp != null) {
							System.out.println("Adding priceid- quantity to ss map priceid=" + isku.mSKUId + " Qnty=" + cartp.getQnty());
							ssinfo.mapQuantity.put(isku.mSKUId, cartp.getQntyVal());
							//ssinfo.setQuantity(cartp.getQntyVal());
							ssinfo.mSKUSelectId = cartp.getSKUId();
							ssinfo.setSelSKU(cartp.getSKUId());
						}
					}
				}
			}
			itemlist.add(ssinfo);
		}
		return true;
	}

	public boolean getCategoryItems(String category, ArrayList<SalesSelectInfo> itemlist) {
		boolean cartcheck = false;
		OrderCartManager cartMgr = gAppEnv.getCartManager();
		if ((cartMgr.getCurrentVendor().equals(mVendiorId)) && (cartMgr.getCartSize()>0))
			cartcheck= true;
		itemlist.clear();
		Iterator hmIterator = mapProductInfo.entrySet().iterator();
		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry)hmIterator.next();
			ProductInfo prod = ((ProductInfo)mapElement.getValue());
			if (prod.category.equals(category)) {
				SalesSelectInfo ssinfo = new SalesSelectInfo(prod);
				ArrayList<ProductSKU> skuList = prod.mSKUList;
				if ((skuList==null) || (skuList.size() ==0))
					continue;

				if (cartcheck == true) {
					if (skuList != null) {
						int pkcnt = skuList.size();
						for (int i = 0; i < pkcnt; i++) {
							//check if the product SKU/price is in cart
							ProductSKU isku = skuList.get(i);
							CartItem cartp = cartMgr.getOrderPackItemById(prod.id, isku.mSKUId);
							if (cartp != null) {
								System.out.println("Adding priceid- quantity to ss map priceid=" + isku.mSKUId + " Qnty=" + cartp.getQnty());
								ssinfo.mapQuantity.put(isku.mSKUId, cartp.getQntyVal());
								//ssinfo.setQuantity(cartp.getQntyVal());
								ssinfo.mSKUSelectId = cartp.getSKUId();
								ssinfo.setSelSKU(cartp.getSKUId());
							}
						}
					}
				}
				itemlist.add(ssinfo);
			}
		}
		return true;
	}

	public boolean getSubCategoryItems(String category, String subcategory, ArrayList<SalesSelectInfo> itemlist) {
		boolean cartcheck = false;
		OrderCartManager cartMgr = gAppEnv.getCartManager();
		if ((cartMgr.getCurrentVendor().equals(mVendiorId)) && (cartMgr.getCartSize()>0))
			cartcheck= true;

		itemlist.clear();;

		Iterator hmIterator = mapProductInfo.entrySet().iterator();
		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry)hmIterator.next();
			ProductInfo prod = ((ProductInfo)mapElement.getValue());
			if ((prod.category.equals(category)) && (prod.subcategory.equals(subcategory))) {
				SalesSelectInfo ssinfo = new SalesSelectInfo(prod);
				ArrayList<ProductSKU> skuList = prod.mSKUList;
				if ((skuList==null) || (skuList.size() ==0))
					continue;

				if (cartcheck == true) {
					if (skuList != null) {
						int pkcnt = skuList.size();
						for (int i = 0; i < pkcnt; i++) {
							//check if the product SKU/price is in cart
							ProductSKU isku = skuList.get(i);
							CartItem cartp = cartMgr.getOrderPackItemById(prod.id, isku.mSKUId);
							if (cartp != null) {
								System.out.println("Adding priceid- quantity to ss map priceid=" + isku.mSKUId + " Qnty=" + cartp.getQnty());
								ssinfo.mapQuantity.put(isku.mSKUId, cartp.getQntyVal());
								//ssinfo.setQuantity(cartp.getQntyVal());
								ssinfo.mSKUSelectId = cartp.getSKUId();
								ssinfo.setSelSKU(cartp.getSKUId());
							}
						}
					}
				}
				itemlist.add(ssinfo);
			}
		}
		return true;
	}

	public boolean getSearchedProduct(String category, String subcategory, int proid, ArrayList<SalesSelectInfo> itemlist) {
		boolean cartcheck = false;
		OrderCartManager cartMgr = gAppEnv.getCartManager();
		if ((cartMgr.getCurrentVendor().equals(mVendiorId)) && (cartMgr.getCartSize()>0))
			cartcheck= true;
		itemlist.clear();
		Iterator hmIterator = mapProductInfo.entrySet().iterator();
		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry)hmIterator.next();
			ProductInfo prod = ((ProductInfo)mapElement.getValue());
			if ((prod.category.equals(category)) && (prod.subcategory.equals(subcategory)) && (prod.id == proid)) {
				SalesSelectInfo ssinfo = new SalesSelectInfo(prod);
				if (cartcheck == true) {
					//ArrayList<ProductPriceInfo> priceList = prod.mPriceInfoList;
					ArrayList<ProductSKU> skuList = prod.mSKUList;
					if (skuList != null) {
						int pkcnt = skuList.size();
						for (int i = 0; i < pkcnt; i++) {
							//check if the product SKU/price is in cart
							ProductSKU isku = skuList.get(i);
							CartItem cartp = cartMgr.getOrderPackItemById(prod.id, isku.mSKUId);
							if (cartp != null) {
								System.out.println("Adding priceid- quantity to ss map priceid=" + isku.mSKUId + " Qnty=" + cartp.getQnty());
								ssinfo.mapQuantity.put(isku.mSKUId, cartp.getQntyVal());
								//ssinfo.setQuantity(cartp.getQntyVal());
								ssinfo.mSKUSelectId = cartp.getSKUId();
								ssinfo.setSelSKU(cartp.getSKUId());
							}
						}
					}
				}
				itemlist.add(ssinfo);
			}
		}
		return true;
	}

	public boolean getProductSearchedItems(String querry_string, ArrayList<SearchInfo> itemlist) {
		Cursor itemcursor=null;
		SQLiteDatabase dbconn = mProductDb.getProductDBConn();
		itemcursor = dbconn.query(true, "prodinfo", new String[] { "id", "name","category","subcategory"},  "name" + " LIKE" + "'%" + querry_string + "%'",
				null, null, null, null, null);

		if (itemcursor.getCount() < 1) {
			itemcursor.close();
			return false;
		}
		else {
			itemlist.clear();
			itemcursor.moveToFirst();
			for(int i=0;i<itemcursor.getCount();i++) {
				int prodid = itemcursor.getInt(itemcursor.getColumnIndexOrThrow("id"));
				String name = itemcursor.getString(itemcursor.getColumnIndexOrThrow("name"));
//				String cat = itemcursor.getString(itemcursor.getColumnIndexOrThrow("category"));
//				String subcat = itemcursor.getString(itemcursor.getColumnIndexOrThrow("subcategory"));
				SearchInfo item = new SearchInfo();
				item.searchtype=OR2GO_SEARCH_PRODUCT_NAME;
				item.name = name;
//				item.type = cat;
//				item.subtype =subcat;
				item.prodid=prodid;
				itemlist.add(item);
				itemcursor.moveToNext();
			}
		}
		return true;
	}

	public boolean updateProSKUStockVal(int skuid, int val) {
		int listsz = mSKUlist.size();
		System.out.println("SKU Size" + mSKUlist.size());
		for(int i=0; i< listsz;i++) {
			ProductSKU ori = mSKUlist.get(i);
			if (ori.mSKUId == skuid) {
				ori.mStockStatus = val;
				return true;
			}
		}
		return false;
	}

	public void setStockCheckStatus(Integer sts) {mStockCheckStatus=sts;}

	public boolean validateStockAvailability(String vendorid, ArrayList<ProductSKU> mSKUList) {
		mSKUlist.addAll(mSKUList);
		System.out.println("SKU size" + mSKUlist.size());
		Or2GoStore storeinfo = gAppEnv.getStoreManager().getStoreById(vendorid);
		JSONArray packidarr = new JSONArray();
		for(int i=0;i<mSKUlist.size();i++) {
			ProductSKU skuinfo = mSKUlist.get(i);
			if (storeinfo.getInventoryControl() >0) {
				try {
					JSONObject sku = new JSONObject();
					sku.put("skuid", skuinfo.mSKUId);
					packidarr.put(sku);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Stock Check SKU ID List="+packidarr.toString());
		if (packidarr.length() <= 0)
			return false;
		//
		Message msg = new Message();
		msg.what = OR2GO_ITEM_STOCK_VAL;    //fixed value for sending sales transaction to server
		msg.arg1 = OR2GO_COMM_ASYNC_API;


		StockStatusCallback stockcb = new StockStatusCallback(gAppEnv);//Callback(mContext);
		stockcb.setVendorId(storeinfo.vId);

		Bundle b = new Bundle();
		b.putParcelable("callback", stockcb);
		b.putString("storeid", vendorid);
		b.putString("skuidlist", packidarr.toString());
		msg.setData(b);

		gAppEnv.getCommMgr().postMessage(msg);
        mStockCheckStatus=CART_STOCK_CHECK_REQUEST;
		return  true;
	}
}
