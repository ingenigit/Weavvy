package com.or2go.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_DISCOUNT_INFO;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.or2go.core.DiscountInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.server.DiscountInfoCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class  DiscountManager {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    ArrayList<DiscountInfo> mDiscountInfoList;
    HashMap<Integer, DiscountInfo>  mapDiscountInfo;

    public DiscountManager(Context context) {
        mContext =context;
        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
        mDiscountInfoList = new ArrayList<DiscountInfo>();
        mapDiscountInfo = new HashMap<Integer, DiscountInfo>();
    }

    public boolean getGlobalDiscountData() {
        Message msg = new Message();
        msg.what = OR2GO_DISCOUNT_INFO;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        DiscountInfoCallback locinfocb = new DiscountInfoCallback(mContext);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", locinfocb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
        return true;
    }

    public boolean addDiscountInfo(DiscountInfo discinfo) {
        mDiscountInfoList.add(discinfo);
        mapDiscountInfo.put(discinfo.mdId, discinfo);
        return true;
    }

    public boolean addDiscountProperty(int id, float minval, float maxamnt, float minitem, float freeitem, int ftu, int vendony, String vendid) {
        DiscountInfo discinfo = mapDiscountInfo.get(id);

        if (discinfo == null) return false;

        discinfo.minsaleamnt = minval;
        discinfo.maxdiscamnt = maxamnt;
        discinfo.FTUOnly = ftu;

        discinfo.VendOnly = vendony;
        discinfo.DiscVendId = vendid;

        return true;
    }

    public DiscountInfo getDiscountInfo(int id)
    {
        return mapDiscountInfo.get(id);
    }

    public ArrayList<DiscountInfo> getAvailableCoupons(String vendid) {
        if (mDiscountInfoList.size() ==0) return null;
        ArrayList<DiscountInfo> listCoupons = new ArrayList<DiscountInfo>();
        for(int i=0; i< mDiscountInfoList.size() ; i++) {
            DiscountInfo discinfo = mDiscountInfoList.get(i);
            if (discinfo.isValid()) {
                if (discinfo.VendOnly == 1) {
                   if (discinfo.DiscVendId.equals(vendid)) {
                       listCoupons.add(discinfo);
                   }
                }
                else if (Objects.equals(discinfo.StoreId, vendid)) {
                    if (gAppEnv.gAppSettings.getExitPersonCoupon()){
                        if (discinfo.mdScope != 2)
                            listCoupons.add(discinfo);
                    }
                    else if (gAppEnv.gAppSettings.getCouponUsed() != null){
                        for (int j = 0; j < gAppEnv.gAppSettings.getCouponUsed().size(); j++){
                            if (!Objects.equals(gAppEnv.gAppSettings.getCouponUsed().get(j).couponname, discinfo.mdName) && Objects.equals(gAppEnv.gAppSettings.getCouponUsed().get(j).storeid, discinfo.StoreId)){
                                listCoupons.add(discinfo);
                            }
                        }
                    }else {
                        listCoupons.add(discinfo);
                    }
                }
                else if(discinfo.FTUOnly == 1) {
                    if (gAppEnv.gAppSettings.getBoolProperty("Pref_FTU"))
                        listCoupons.add(discinfo);
                }
            }
        }
        if (listCoupons.size() > 0)
            return listCoupons;
        else
            return null;
    }

    public void processVendorDiscounts() {
        if (mDiscountInfoList.size() ==0) return;
        ArrayList<DiscountInfo> listCoupons = new ArrayList<DiscountInfo>();
        for(int i=0; i< mDiscountInfoList.size() ; i++) {
            DiscountInfo discinfo = mDiscountInfoList.get(i);
            if (discinfo.isValid()) {
                if (discinfo.VendOnly == 1) {
                    gAppEnv.getStoreManager().addDiscountInfo(discinfo.DiscVendId, discinfo.mdValue.toString(), discinfo.mdAmntType);
                }
            }
        }
    }
}


