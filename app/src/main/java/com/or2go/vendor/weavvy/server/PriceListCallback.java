package com.or2go.vendor.weavvy.server;

import static com.or2go.core.Or2goConstValues.OR2GO_OUT_OF_STOCK_DATA;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_PRICE_DBSYNC;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.or2go.core.Or2goVendorInfo;
import com.or2go.core.ProductPackInfo;
import com.or2go.core.UnitManager;
import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.manager.ProductManager;
import com.or2go.volleylibrary.CommApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PriceListCallback extends CommApiCallback implements Parcelable {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    String mVendorId;
    //String mVendorName;

    UnitManager mUnitMgr;

    Integer mLinkAPI;

    public PriceListCallback(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();

        mUnitMgr = new UnitManager();
        mLinkAPI=0;
    }

    public PriceListCallback(Context context, AppEnv appenv)
    {
        mContext = context;
        gAppEnv = appenv;// getApplicationContext();

        mUnitMgr = new UnitManager();
    }

    public void setVendorId(String vendid)
    {
        mVendorId =vendid;
    }
    public void setLinkAPI(Integer api) {mLinkAPI=api;}

    @Override
    public Void call () {
        // this is the actual callback function

        // the result variable is available right away:
        gAppEnv.getGposLogger().i( "PriceList API result for: "+mVendorId+" result  :"+ result+ "   response="+response);

        if (result >0 )
        {
            try {

                JSONArray JsonResponse = new JSONArray(response);

                JSONObject resultobject = JsonResponse.getJSONObject(0);

                String result = resultobject.getString("result");

                if (result.equals("ok")) {

                    ProductManager prdMgr = gAppEnv.getVendorManager().getProductManager();
                    Or2goVendorInfo vendInfo = gAppEnv.getVendorManager().getVendorInfo();

                    JSONObject dataobject = JsonResponse.getJSONObject(1);

                    JSONArray pricearr = dataobject.getJSONArray("data");
                    for (int i = 0; i < pricearr.length(); i++) {  // **line 2**
                        JSONObject pricebject = pricearr.getJSONObject(i);

                        //":[{"packid":4,"prodid":9,"unit":2,"unitcount":1,"unitamount":0,"amount":1,"saleprice":25,"maxprice":30,"packname":"1kg rice","packtype":"Pouch","packdesc":"","imageurl":"","dbver":5},{"
                        Integer packid = pricebject.getInt("packid");
                        Integer prodid = pricebject.getInt("prodid");
                        Integer unit = pricebject.getInt("unit");
                        Integer ucnt = pricebject.getInt("unitcount");
                        Integer uamnt = pricebject.getInt("unitamount");
                        String pkamnt = pricebject.getString("amount");
                        String sprice = pricebject.getString("saleprice");
                        String smrp = pricebject.getString("maxprice");
                        String packname = pricebject.getString("packname");
                        String packtype = pricebject.getString("packtype");
                        String packdesc = pricebject.getString("packdesc");
                        String imgurl = pricebject.getString("imageurl");
                        Integer ver = pricebject.getInt("dbver");


                        ProductPackInfo packinfo = new ProductPackInfo(packid, prodid, unit, ucnt, uamnt,
                                Float.parseFloat(pkamnt), Float.parseFloat(sprice), Float.parseFloat(smrp),
                                packname, packtype, packdesc, imgurl, ver);

                        ///prdMgr.addProductPriceInfo(prodid, packinfo);
                    }

                    vendInfo.getDBState().donePriceDBUpdate();
                    vendInfo.getDBState().setPriceDownloadDone();

                    gAppEnv.getVendorManager().postDBSyncMessage(mVendorId, OR2GO_VENDOR_PRICE_DBSYNC);

                    if (mLinkAPI==OR2GO_OUT_OF_STOCK_DATA) gAppEnv.getVendorManager().getOutOfStockData(vendInfo);
                }
                else {
                    gAppEnv.getVendorManager().getVendorInfo().getDBState().setPriceDownloadError();
                    //gAppEnv.getVendorManager().setServerProductListDownloadDone(mVendorId);
                    gAppEnv.getVendorManager().getVendorInfo().getDBState().donePriceDBUpdate();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(mContext, "Product List Error!!! -"+result, Toast.LENGTH_SHORT).show();
            gAppEnv.getVendorManager().getVendorInfo().getDBState().setProductDownloadError();


        }

        return null;
    }

    protected PriceListCallback(Parcel in) {
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
            return new PriceListCallback(in);
        }

        @Override
        public CommApiCallback[] newArray(int size) {
            return new CommApiCallback[size];
        }
    };
}
