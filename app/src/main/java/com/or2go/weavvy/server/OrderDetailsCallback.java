package com.or2go.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.or2go.adapter.OrderDetailsItemAdapter;
import com.or2go.core.OrderItem;
import com.or2go.core.ProductInfo;
import com.or2go.core.ProductSKU;
import com.or2go.core.UnitManager;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.manager.ProductManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrderDetailsCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    AppEnv gAppEnv;
    ArrayList<OrderItem> mItemList;
    OrderDetailsItemAdapter mAdapter;
    ProductManager mPrdMgr;
    UnitManager mUnitMgr = new UnitManager();

    public OrderDetailsCallback(Context context, ProductManager prdmgr) {
        mContext = context;
        gAppEnv = (AppEnv) mContext.getApplicationContext();
        mPrdMgr=prdmgr;
    }

    public void setViewAdapter(final ArrayList<OrderItem> itemList, OrderDetailsItemAdapter adapter) {
        mAdapter = adapter;
        mItemList = itemList;
    }

    @Override
    public Void call () {
        Log.i("OrderDetailsCallback" ,"API result is: " + result+ "   server response="+response);
        if (result >0 ) {
            try {
                JSONArray JsonResponse = new JSONArray(response);
                JSONObject dataobject = JsonResponse.getJSONObject(1);
                JSONArray itemlist = dataobject.getJSONArray("data");
                mItemList.clear();
                for (int i = 0; i < itemlist.length(); i++) {
                    JSONObject jsonobject = itemlist.getJSONObject(i);
                    int itemid = jsonobject.getInt("itemid");
                    String itemname = jsonobject.getString("itemname");
                    String itemqnty = jsonobject.getString("quantity");
                    Integer unit = jsonobject.getInt("unit");
                    String orderprice = jsonobject.getString("price");
                    int packid = jsonobject.getInt("priceid");
                    OrderItem orditem = new OrderItem(itemid, itemname, Float.valueOf(orderprice), Float.parseFloat(itemqnty), unit,packid);
                    ProductInfo prod = mPrdMgr.getProductInfo(itemid);
                    ProductSKU skuinfo = prod.getSKU(packid);
                    mItemList.add(orditem);
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        else
        {
            Toast.makeText(mContext, "Order History Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected OrderDetailsCallback(Parcel in) {
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
            return new OrderDetailsCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
