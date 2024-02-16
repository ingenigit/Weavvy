package com.or2go.vendor.weavvy.server;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.or2go.core.ProductInfo;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.manager.ProductManager;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StockOutCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    String mVendorId;

    public StockOutCallback(Context context/*, ProductManager prdmgr*/)
    {
        mContext = context;
        gAppEnv = (AppEnv) mContext.getApplicationContext();

        //mPrdMgr=prdmgr;
    }


    /*public void setViewAdapter(final ArrayList<OrderItem> itemList, OrderDetailsItemAdapter adapter)
    {
        //mAdapter = adapter;
        //mItemList = itemList;
    }*/

    public void setVendorId(String vendid)
    {
        mVendorId =vendid;
    }

    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        Log.i("StockOutCallback" ,"API result is: " + result+ "   server response="+response);

        if (result >0 )
        {
            try {
                //JSONArray jsonarray = new JSONArray(response.toString());
                JSONArray JsonResponse = new JSONArray(response);

                JSONObject resultobject = JsonResponse.getJSONObject(0);
                String result = resultobject.getString("result");
                if (result.equals("ok")) {
                    JSONObject dataobject = JsonResponse.getJSONObject(1);

                    JSONArray itemlist = dataobject.getJSONArray("data");
                    //mItemList.clear();
                    ProductManager prdmgr = gAppEnv.getVendorManager().getProductManager();

                    for (int i = 0; i < itemlist.length(); i++) {
                        JSONObject jsonobject = itemlist.getJSONObject(i);

                        int itemid = jsonobject.getInt("prodid");
                        int packid = jsonobject.getInt("packid");

                        ProductInfo prdinfo = prdmgr.getProductInfo(itemid);
//                        prdinfo.setStock(packid, Float.valueOf("0"));

                        //mItemList.add(orditem);
                    }
                    //mAdapter.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        else
        {
            Toast.makeText(mContext, "StockOutCallback Error!!! -"+result, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    protected StockOutCallback(Parcel in) {
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
            return new StockOutCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
