package com.or2go.weavvy.server;

import static com.or2go.weavvy.manager.OrderCartManager.CART_STOCK_CHECK_COMPLETE;
import static com.or2go.weavvy.manager.OrderCartManager.CART_STOCK_CHECK_ERROR;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.or2go.adapter.OrderDetailsItemAdapter;
import com.or2go.core.OrderItem;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.manager.ProductManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StockStatusCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    String mVendorId;

    public StockStatusCallback(Context context/*, ProductManager prdmgr*/)
    {
        mContext = context;
        gAppEnv = (AppEnv) mContext.getApplicationContext();

        //mPrdMgr=prdmgr;
    }


    public void setViewAdapter(final ArrayList<OrderItem> itemList, OrderDetailsItemAdapter adapter)
    {
        //mAdapter = adapter;
        //mItemList = itemList;
    }

    public void setVendorId(String vendid)
    {
        mVendorId =vendid;
    }

    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        Log.i("StockStatusCallback" ,"API result is: " + result+ "   server response="+response);

        if (result >0 ) {
            try {
                //JSONArray jsonarray = new JSONArray(response.toString());
                JSONArray JsonResponse = new JSONArray(response);

                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                ProductManager prdmgr = gAppEnv.getStoreManager().getProductManager(mVendorId);
                if (result.equals("ok")) {

                    JSONObject dataobject = JsonResponse.getJSONObject(1);
                    JSONArray itemlist = dataobject.getJSONArray("data");
                    //mItemList.clear();
                    for (int i = 0; i < itemlist.length(); i++) {
                        JSONObject jsonobject = itemlist.getJSONObject(i);

                        String stockval = jsonobject.getString("stockval");
                        int skuid = jsonobject.getInt("skuid");
                        //int prodid = jsonobject.getInt("prodid");

                        Log.i("StockStatusCallback",  "skuid: " + skuid + " stock==" + stockval);
                        //CartItem oritem = gAppEnv.getCartManager().getOrderPackItemById(prodid,packid);
                        //oritem.setCurStock(Float.valueOf(stockval));
                        gAppEnv.getCartManager().updateSKUStockVal(skuid, Float.valueOf(stockval));
                        prdmgr.updateProSKUStockVal(skuid, Integer.parseInt(stockval));
                    }

                    gAppEnv.getCartManager().setStockCheckStatus(CART_STOCK_CHECK_COMPLETE);
                    prdmgr.setStockCheckStatus(CART_STOCK_CHECK_COMPLETE);

                }
                else {
                    gAppEnv.getCartManager().setStockCheckStatus(CART_STOCK_CHECK_ERROR);
                    prdmgr.setStockCheckStatus(CART_STOCK_CHECK_ERROR);
                }

            } catch(Exception e){
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(mContext, "StockOutCallback Error!!! -"+result, Toast.LENGTH_SHORT).show();
            gAppEnv.getCartManager().setStockCheckStatus(CART_STOCK_CHECK_ERROR);
        }
        return null;
    }

    protected StockStatusCallback(Parcel in) {
        result = in.readInt();
        response = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result);
        dest.writeString(response);
    }

    @SuppressWarnings("unused")
    public static final Creator<CommApiCallback> CREATOR = new Creator<CommApiCallback>() {
        @Override
        public CommApiCallback createFromParcel(Parcel in) {
            return new StockStatusCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
