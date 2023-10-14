package com.or2go.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_DA_LOCATION;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_CHARGE_MODEL_DISTANCE;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_CHARGE_MODEL_LOCATION;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_MODEL;
import static com.or2go.core.Or2goConstValues.OR2GO_USER_DELIVERY_ADDR;
import static com.or2go.core.Or2goConstValues.OR2GO_USER_DELIVERY_ADDR_DELETE;
import static com.or2go.core.Or2goConstValues.OR2GO_USER_DELIVERY_ADDR_UPDATE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.or2go.core.DeliveryAddrInfo;
import com.or2go.core.DeliveryModel;
import com.or2go.mylibrary.DeliveryAddressDBHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.DeliveryChargeViewModel;
import com.or2go.weavvy.DistanceCalculation;
import com.or2go.weavvy.server.DeliveryModelCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeliveryManager {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    private DeliveryAddressDBHelper mUserAddrDB;
    HashMap<String, DeliveryAddrInfo> mapUserDeliveryAddress;
    ArrayList<DeliveryModel> mDeliveryModelList;
    ArrayList<DeliveryModel> mLocalityModelList;
    ArrayList<DeliveryModel> mPincodeModelList;
    ArrayList<DeliveryModel> mPlaceModelList;
    public ArrayList<DeliveryModel> mDistanceModelList;
    HashMap<String, String> mapDALocation;
    String delirate="-1";
    String getDistacne = "";
    DistanceCalculation distanceCalculation;
    LocalBroadcastManager localBroadcastManager;
    Intent intent;

    public DeliveryManager(Context context) {
        mContext = context;
        //Get global application
        gAppEnv = (AppEnv) context;// getApplicationContext();
        mUserAddrDB = new DeliveryAddressDBHelper(context);
        mUserAddrDB.InitDB();
        mapUserDeliveryAddress = new HashMap<String, DeliveryAddrInfo>();
        initSavedAddressInfo();
        mDeliveryModelList = new ArrayList<DeliveryModel>();
        mLocalityModelList = new ArrayList<DeliveryModel>();
        mPincodeModelList = new ArrayList<DeliveryModel>();
        mPlaceModelList= new ArrayList<DeliveryModel>();
        mDistanceModelList = new ArrayList<DeliveryModel>();
        mapDALocation = new HashMap<String, String>();
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        intent = new Intent("UsedGeoCode");
    }

    //initialize address info from DB
    private void initSavedAddressInfo() {
        ArrayList<DeliveryAddrInfo> addrlist = mUserAddrDB.getAddrList();
        int cnt = addrlist.size();
        for (int i = 0; i < cnt; i++) {
            DeliveryAddrInfo deliinfo = addrlist.get(i);
            mapUserDeliveryAddress.put(deliinfo.nickname, deliinfo);
        }
    }

    public boolean addDeliveryModel(DeliveryModel dm) {
        mDeliveryModelList.add(dm);
        if (dm.mType == OR2GO_DELIVERY_CHARGE_MODEL_LOCATION) {
            String loctype = getLocationType(dm);
            if (loctype.equals("Locality"))
                mLocalityModelList.add(dm);
            else if (loctype.equals("Pincode"))
                mPincodeModelList.add(dm);
            if (loctype.equals("Place"))
                mPlaceModelList.add(dm);
        }
        else if (dm.mType == OR2GO_DELIVERY_CHARGE_MODEL_DISTANCE)
            mDistanceModelList.add(dm);

        return true;
    }

    //server data initialization
    ///Server API
    public boolean getDeliveryModel() {
        Message msg = new Message();
        msg.what = OR2GO_DELIVERY_MODEL;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;

        DeliveryModelCallback locinfocb = new DeliveryModelCallback(mContext);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", locinfocb);
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        return true;
    }

    public boolean addDeliveryAddr(String nickname, String addr, String place, String locality, String sublocality, String landmark, String zipcode, String altcontact, String geoloc) {
        if (mUserAddrDB.insertaddr(nickname, addr, place,locality, sublocality, landmark, zipcode, altcontact, geoloc)) {
            DeliveryAddrInfo deliinfo = new DeliveryAddrInfo(nickname, addr, place,locality, sublocality, landmark, zipcode, altcontact);
            deliinfo.setGeoPosition(geoloc);
            mapUserDeliveryAddress.put(nickname, deliinfo);
            postUserDeliveryAddress(nickname, addr, place, locality, sublocality, landmark, zipcode, altcontact, geoloc);
        }
        return true;
    }

    public boolean dbSyncDeliveryAddr(String nickname, String addr, String place, String locality, String sublocality, String landmark, String zipcode, String altaddr, String geoloc) {
        DeliveryAddrInfo deliinfo = new DeliveryAddrInfo(nickname, addr, place, locality, sublocality, landmark, zipcode, altaddr);
        deliinfo.setGeoPosition(geoloc);
        mapUserDeliveryAddress.put(nickname, deliinfo);
        return mUserAddrDB.insertaddr(nickname, addr, place, locality, sublocality, landmark, zipcode, altaddr, geoloc);
    }

    public boolean updateDeliveryAddr(String nickname, String addr, String place, String locality, String sublocality, String landmark, String zipcode, String altaddr, String geoloc) {
        DeliveryAddrInfo deliinfo = mapUserDeliveryAddress.get(nickname);
        if (deliinfo != null) {
            if (mUserAddrDB.updateProfileAddr(nickname, addr, place, locality, sublocality, landmark, zipcode, altaddr,"")) {
                deliinfo.updateAddressInfo(nickname, addr, place, locality, sublocality, landmark, zipcode, altaddr);
                postUpdateDeliveryAddress(nickname, addr, place, locality, sublocality, landmark, zipcode, altaddr, geoloc);
                return true;
            }
        }
        return false;
    }

    public boolean deleteAddr(String name) {
        if (mUserAddrDB.deleteAddr(name)) {
            mapUserDeliveryAddress.remove(name);
            postDeleteDeliveryAddress(name);
        }
        return true;
    }

    public ArrayList<DeliveryAddrInfo> getAddrList() {
        ArrayList<DeliveryAddrInfo> useraddrlist = new ArrayList<DeliveryAddrInfo>();
        for(Map.Entry mapElement :mapUserDeliveryAddress.entrySet()) {
            String key = (String) mapElement.getKey();
            DeliveryAddrInfo addrinfo = ((DeliveryAddrInfo) mapElement.getValue() );
            useraddrlist.add(addrinfo);
        }
        return useraddrlist;
    }

    public DeliveryAddrInfo getAddrInfo(String name) {
        return mapUserDeliveryAddress.get(name);
    }

    public String getAddrNameFromAddr(String addr) {
        ArrayList<DeliveryAddrInfo> addrlist = getAddrList();
        int addrcnt = addrlist.size();
        for(int i=0; i<addrcnt;i++) {
            DeliveryAddrInfo addrinfo = addrlist.get(i);
            if (addrinfo.addr.equals(addr))
                return addrinfo.getAddrName();
        }
        return "";
    }

    ////With Live Data view model implementation
    DeliveryChargeViewModel mDeliModel;
    public String getDeliveryCharge(DeliveryChargeViewModel delimodel, String storeGeo, DeliveryAddrInfo addr, Integer modelid) {
        getDistacne = "";
        mDeliModel = delimodel;
        int modcnt = mDeliveryModelList.size();
        //check locality models
        System.out.println("Checking locality models");
        String charge = getLocalityCharge(addr.place, addr.locality);
        //check pincode models
        if (charge.equals("-1")) {
            System.out.println("Checking pincode models");
            charge = getPincodeCharge(addr.zipcode);
        }
        //check place models
        if (charge.equals("-1")) {
            System.out.println("Checking place models");
            charge = getPlaceCharge(addr.place);
        }
        if (charge.equals("-1")) {
            System.out.println(addr.geoposition + "Checking distance models   geoloc="+storeGeo);
            charge = getDistanceCharge(delimodel, addr, storeGeo);
        }
        //Find Model
        //Match Location
        //return Delivery Charge
        delirate =  charge;
        delimodel.getDeliveryCharge().setValue(delirate);
        System.out.println("DeliveryManager : delivery charge for addreess "+addr.nickname+" = "+delirate);
        return delirate;
    }

    String findLocationCharge(String addrloc, String locparam) {
        String retval = "-1";
        //String param = locparam.replace("\"","");
        try {
            JSONObject modelparam = new JSONObject(locparam);
            //String loctype = modelparam.getString("LocationType");
            JSONArray chargearr = modelparam.getJSONArray("Charges");
            //String addrloc;//= getAddrLocationData(loctype, addr);
            for (int i = 0; i < chargearr.length(); i++) {
                String arrobj = chargearr.getString(i);
                JSONObject childJSONObject = new JSONObject(arrobj);
                Iterator<String> listKEY = childJSONObject.keys();
                String loc = listKEY.next();
                String val = childJSONObject.getString(loc);
                System.out.println("Charge Location KEY="+loc+"charge="+val + addrloc.trim());

                addrloc= addrloc.trim();
                if (addrloc.equals(loc)) {
                    gAppEnv.gAppSettings.setUseGeoDistance(false);
                    System.out.println("Charge Location Matched!!!!=");
                    return val;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retval;
    }

    String getLocalityCharge(String place, String locality) {
        String rate = "-1";
        int modcnt = mLocalityModelList.size();
        if (modcnt==0) return rate;
        for(int i=0; i<modcnt; i++) {
            DeliveryModel dmodel = mLocalityModelList.get(i);
            //if the model Place matches address Place
            if (dmodel.mPlace.equals(place)) {
                rate = findLocationCharge(locality, dmodel.mParams);
                if (!rate.equals("-1"))
                    break; //found delivery charge
            }
        }
        return rate;
    }

    String getPincodeCharge(String pincode) {
        String rate = "-1";
        int modcnt = mPincodeModelList.size();
        if (modcnt==0) return rate;
        for(int i=0; i<modcnt; i++) {
            DeliveryModel dmodel = mPincodeModelList.get(i);
            rate = findLocationCharge(pincode, dmodel.mParams);
            if (!rate.equals("-1"))
                break; //found delivery charge
        }
        return rate;
    }

    String getPlaceCharge(String place) {
        String rate = "-1";
        int modcnt = mPlaceModelList.size();
        if (modcnt==0) return rate;
        for(int i=0; i<modcnt; i++) {
            DeliveryModel dmodel = mPlaceModelList.get(i);
            rate = findLocationCharge(place, dmodel.mParams);
            if (!rate.equals("-1"))
                break; ////found delivery charge
        }
        return rate;
    }

    String getDistanceCharge(DeliveryChargeViewModel deliviewmodel, DeliveryAddrInfo addr, String storeGeo) {
        String rate = "-1";
        int modcnt = mDistanceModelList.size();
        if (modcnt==0) return rate;
        for(int i=0; i<modcnt; i++) {
            DeliveryModel dmodel = mDistanceModelList.get(i);
            distanceCalculation = new DistanceCalculation(mContext, storeGeo, addr.getGeoposition());
            distanceCalculation.setDeliveryInfo(addr, dmodel.mParams, deliviewmodel);
            distanceCalculation.run();
        }
        gAppEnv.gAppSettings.setUseGeoDistance(true);
        localBroadcastManager.sendBroadcast(intent);
        return rate;
    }

    String getAddrLocationData(String type, DeliveryAddrInfo addr) {
        if (type.equals("Place"))
            return addr.place;
        else if (type.equals("PIN"))
            return addr.zipcode;
        else if (type.equals("Locality"))
            return addr.locality;
        else return "";
    }


    public String getLocationType(DeliveryModel delimodel) {
        String loctype = "";
        if (delimodel.mType != OR2GO_DELIVERY_CHARGE_MODEL_LOCATION)
            return loctype;
        try {
            JSONObject modelparam = new JSONObject(delimodel.mParams);
            loctype = modelparam.getString("LocationType");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return loctype;
    }

    private void postUserDeliveryAddress(String nickname, String addr, String place, String locality, String sublocality, String landmark, String zipcode, String altcont, String geoloc) {
        Message msg = new Message();
        msg.what = OR2GO_USER_DELIVERY_ADDR;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        Bundle b = new Bundle();
        b.putString("addrname", nickname);
        b.putString("addr", addr);
        b.putString("place", place);
        b.putString("locality", locality);
        b.putString("sublocality", sublocality);
        b.putString("landmark", landmark);
        b.putString("zipcode", zipcode );
        b.putString("altcontact", altcont );
        b.putString("geoLoc", geoloc );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
    }

    private void postUpdateDeliveryAddress(String nickname, String addr, String place, String locality, String sublocality, String landmark, String zipcode, String altcont, String geoloc) {
        Message msg = new Message();
        msg.what = OR2GO_USER_DELIVERY_ADDR_UPDATE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        Bundle b = new Bundle();
        b.putString("addrname", nickname);
        b.putString("addr", addr);
        b.putString("place", place);
        b.putString("locality", locality);
        b.putString("sublocality", sublocality);
        b.putString("landmark", landmark);
        b.putString("zipcode", zipcode );
        b.putString("altcontact", altcont );
        b.putString("geoLoc", geoloc );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
    }

    private void postDeleteDeliveryAddress(String nickname) {
        Message msg = new Message();
        msg.what = OR2GO_USER_DELIVERY_ADDR_DELETE;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        Bundle b = new Bundle();
        b.putString("addrname", nickname);
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
    }


    ///DA Location
    public void setDALocation(String daid, String location)
    {
        mapDALocation.put(daid, location);
    }

}
