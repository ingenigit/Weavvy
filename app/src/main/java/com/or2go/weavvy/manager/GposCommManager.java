package com.or2go.weavvy.manager;

import static com.or2go.core.Or2goConstValues.OR2GO_ACTIVE_ORDER_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_APPINFO;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_LOGIN;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_LOGOUT;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_SYNC_API;
import static com.or2go.core.Or2goConstValues.OR2GO_CRASH_REPORT;
import static com.or2go.core.Or2goConstValues.OR2GO_DA_LOCATION;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_LOCATION_INFO;
import static com.or2go.core.Or2goConstValues.OR2GO_DELIVERY_MODEL;
import static com.or2go.core.Or2goConstValues.OR2GO_DISCOUNT_INFO;
import static com.or2go.core.Or2goConstValues.OR2GO_GET_SESSION;
import static com.or2go.core.Or2goConstValues.OR2GO_GET_SPINFO;
import static com.or2go.core.Or2goConstValues.OR2GO_IMAGE_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_ITEM_STOCK_VAL;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_DETAILS;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_HISTORY;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_HISTORY_INFO;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_RATING;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_REQ;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_OUT_OF_STOCK_DATA;
import static com.or2go.core.Or2goConstValues.OR2GO_PAYMENT_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_PREPAYMENT_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_PRICE_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_PRODUCT_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_QUICK_ORDER_CONFIRM;
import static com.or2go.core.Or2goConstValues.OR2GO_REGISTER;
import static com.or2go.core.Or2goConstValues.OR2GO_REGISTER_OTPREQ;
import static com.or2go.core.Or2goConstValues.OR2GO_SKU_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_USER_DELIVERY_ADDR;
import static com.or2go.core.Or2goConstValues.OR2GO_USER_DELIVERY_ADDR_DELETE;
import static com.or2go.core.Or2goConstValues.OR2GO_USER_DELIVERY_ADDR_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_DBVERSION_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_INFO;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_VENDOR_LIST_PUBLIC;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.volleylibrary.HttpVolleyHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.Semaphore;


public class GposCommManager extends Thread {

	private Context mContext;
	AppEnv gAppEnv;
    public Handler mHandler;
    HttpVolleyHelper apiCaller;
//    private Semaphore mApiSyncSemaphore;
    String mSessionId="";

    public GposCommManager(Context context){
        mContext =context;
    	//Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();
//        mApiSyncSemaphore = new Semaphore(1, true);
        apiCaller = new HttpVolleyHelper(mContext);
        start();
    }
	
	@Override
	public void run() {
        Looper.prepare();
        gAppEnv.getGposLogger().i("CommManager : Comm message handler ready = " );
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Integer nMsg = msg.what;
                Integer nSync = msg.arg1;
//                if (nSync == OR2GO_COMM_SYNC_API)
//                    acquireApiSyncSem();

                Bundle b;
                CommApiCallback apicb;
                switch(nMsg) {
//                    case OR2GO_COMM_APPINFO:
//                        b = msg.getData();
//                        apicb = b.getParcelable("callback");
//                        or2goAppInfoPub(apicb);
//                        break;
                    case 358:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        or2goAllStore(apicb);
                        break;
                    case OR2GO_COMM_LOGIN:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        or2goLogin(apicb);
                        break;
                    case OR2GO_COMM_LOGOUT:
                        b = msg.getData();
                        String userid = b.getString("USER");
                        or2goLogout();
                        break;
                    case OR2GO_GET_SESSION:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getSessionId(apicb);
                        break;
                    case OR2GO_GET_SPINFO:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getSPInfo(apicb);
                        break;
                    case OR2GO_REGISTER_OTPREQ:
                        b = msg.getData();
                        String mobno = b.getString("mobno");
                        genRegisterOtp(mobno);
                        break;
                    case OR2GO_REGISTER:
                        b = msg.getData();
                        String otp = b.getString("otp");
                        String custid = b.getString("custid");
                        String passwd = b.getString("password");
                        String regmobno = b.getString("mobno");
                        String name = b.getString("name");
                        String email = b.getString("email");
                        String place = b.getString("place");
                        String addr = b.getString("addr");
                        apicb = b.getParcelable("callback");
                        or2goRegister(otp, custid, passwd, name, email, place, addr, apicb);
                        break;
                    case OR2GO_ACTIVE_ORDER_LIST:
                        gAppEnv.getGposLogger().i("Or2Go Active Order List Request session id="+gAppEnv.getSessionId());
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getActiveOrders(apicb);
                        break;
                    case OR2GO_ORDER_DETAILS:
                        b = msg.getData();
                        String orderid = b.getString("orderid");
                        apicb = b.getParcelable("callback");
                        getOrderDetails(orderid,apicb);
                        break;
                    case OR2GO_VENDOR_LIST:
                        gAppEnv.getGposLogger().i( "Or2Go Vendor List Request session id="+gAppEnv.getSessionId());
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getVendorList(apicb);
                        break;
                    case OR2GO_VENDOR_LIST_PUBLIC:
                        gAppEnv.getGposLogger().i( "Or2Go Vendor List Request session id="+gAppEnv.getSessionId());
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getVendorListPublic(apicb);
                        break;
                    case OR2GO_VENDOR_DBVERSION_LIST:
                        gAppEnv.getGposLogger().i( "Or2Go Vendor DB Version List Request session id="+gAppEnv.getSessionId());
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getVendorDBVersionList(apicb);
                        break;
                    case OR2GO_VENDOR_INFO:
                        b = msg.getData();
                        String vid = b.getString("vendorid");
                        apicb = b.getParcelable("callback");
                        //getVendorInfo(vid,  apicb);
                        getStoreInfo(vid,  apicb);
                        break;
                    case OR2GO_PRODUCT_LIST:
                        b = msg.getData();
                        String storeid = b.getString("storeid");
                        apicb = b.getParcelable("callback");
                        getProductList(storeid,  apicb);
                        break;
                    case OR2GO_PRICE_LIST:
                        b = msg.getData();
                        String pricestoreid = b.getString("storeid");
                        apicb = b.getParcelable("callback");
                        getPriceList(pricestoreid, apicb);
                        break;
                    case OR2GO_SKU_LIST:
                        b = msg.getData();
                        String skustoreid = b.getString("storeid");
                        apicb = b.getParcelable("callback");
                        getPriceList(skustoreid, apicb);
                        break;
                    case OR2GO_OUT_OF_STOCK_DATA:
                        b = msg.getData();
                        String stkbnname = b.getString("dbname");
                        apicb = b.getParcelable("callback");
                        getOutOfStockData(stkbnname, apicb);
                        break;
                    case OR2GO_ITEM_STOCK_VAL:
                        b = msg.getData();
                        String stkstoreid = b.getString("storeid");
                        String stkskulist = b.getString("skuidlist");
                        apicb = b.getParcelable("callback");
                        getStockStatus(stkskulist, stkstoreid, apicb);
                        break;
                    case OR2GO_ORDER_REQ:
                        b = msg.getData();
                        String orderpkt = b.getString("orderpkt");
                        String ordervendid = b.getString("storeid");
                        apicb = b.getParcelable("callback");
                        placeOrder(orderpkt,  ordervendid, apicb);
                        break;
                    case OR2GO_ORDER_STATUS_UPDATE:
                        b = msg.getData();
                        String stsorderid = b.getString("orderid");
                        String stsstoreid = b.getString("storeid");
                        Integer ordevent = b.getInt("orderevent");
                        Integer status =  b.getInt("status");
                        String description = b.getString("description");
                        String pktheader =  b.getString("pktheader");
                        apicb = b.getParcelable("callback");
                        orderStatusUpdate(stsorderid, stsstoreid, ordevent, description, pktheader,  apicb);
                        break;
                    case OR2GO_QUICK_ORDER_CONFIRM:
                        b = msg.getData();
                        String qupdpktheader =  b.getString("pktheader");
                        apicb = b.getParcelable("callback");
                        orderQuickItemsConfirm(qupdpktheader, apicb);
                        break;
                    case OR2GO_PAYMENT_STATUS_UPDATE:
                        b = msg.getData();
                        String updorderid = b.getString("orderid");
                        Integer updordstatus = b.getInt("orderstatus");
                        Integer payevent =  b.getInt("paymentevent");
                        Integer paymode = b.getInt("paymode");
                        String paymentinfo =  b.getString("paymentinfo");
                        String updpktheader =  b.getString("pktheader");
                        apicb = b.getParcelable("callback");
                        orderPaymentStatusUpdate(updorderid, updordstatus, payevent, paymode, paymentinfo, updpktheader, apicb);
                        break;
                    case OR2GO_PREPAYMENT_STATUS_UPDATE:
                        b = msg.getData();
                        String pupdorderid = b.getString("orderid");
                        Integer pstatus =  b.getInt("status");
                        Integer ppaystatus =  b.getInt("paymentstatus");
                        Integer ppaymode = b.getInt("paymode");
                        String ppaymentid =  b.getString("paymentid");
                        String pupdpktheader =  b.getString("pktheader");
                        apicb = b.getParcelable("callback");
                        orderPrePaymentStatusUpdate(pupdorderid, pstatus, ppaystatus, ppaymode, ppaymentid, pupdpktheader, apicb);
                        break;
                    case OR2GO_IMAGE_STATUS_UPDATE:
                        b = msg.getData();
                        String imgorderid = b.getString("orderid");
                        Integer imgstatus =  b.getInt("imagestatus");
                        String imgpktheader =  b.getString("pktheader");
                        apicb = b.getParcelable("callback");
                        orderImageStatusUpdate(imgorderid, imgstatus,  imgpktheader,  apicb);
                        break;
                    case OR2GO_ORDER_HISTORY:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getOrderHistory(apicb);
                        break;
                    case OR2GO_ORDER_HISTORY_INFO:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        String histinfoorderid = b.getString("orderid");
                        getOrderHistoryInfo(histinfoorderid, apicb);
                        break;
                    case OR2GO_ORDER_RATING:
                        b = msg.getData();
                        String ordid = b.getString("orderid");
                        String vendorid = b.getString("storeid");
                        String rating = b.getString("ratingdata");
                        postOrderRating(ordid, vendorid, rating);
                        break;
                    case OR2GO_DELIVERY_LOCATION_INFO:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getLocationInfo(apicb);
                        break;
                    case OR2GO_DELIVERY_MODEL:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getDeliveryModel(apicb);
                        break;
                    case OR2GO_DISCOUNT_INFO:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getDiscountInfo(apicb);
                        break;
                    case OR2GO_USER_DELIVERY_ADDR:
                        b = msg.getData();
                        String addrname = b.getString("addrname");
                        String deliaddr = b.getString("addr");
                        String deliplace = b.getString("place");
                        String delilocality = b.getString("locality");
                        String delisublocality = b.getString("sublocality");
                        String landmark = b.getString("landmark");
                        String zipcode = b.getString("zipcode");
                        String altcontact = b.getString("altcontact");
                        String geolocation = b.getString("geoLoc");
                        postUserDeliveryAddress(addrname, deliaddr, deliplace, delilocality, delisublocality, landmark,zipcode,altcontact, geolocation);
                        break;
                    case OR2GO_USER_DELIVERY_ADDR_UPDATE:
                        b = msg.getData();
                        String updname = b.getString("addrname");
                        String updaddr = b.getString("addr");
                        String updplace = b.getString("place");
                        String updlocality = b.getString("locality");
                        String updsublocality = b.getString("sublocality");
                        String updlandmark = b.getString("landmark");
                        String updzipcode = b.getString("zipcode");
                        String updaltcontact = b.getString("altcontact");
                        String upgeolocation = b.getString("geoLoc");
                        postUpdateDeliveryAddress(updname, updaddr, updplace, updlocality, updsublocality, updlandmark,updzipcode,updaltcontact, upgeolocation);
                        break;
                    case OR2GO_USER_DELIVERY_ADDR_DELETE:
                        b = msg.getData();
                        String deladdrname = b.getString("addrname");
                        postDeleteDeliveryAddress(deladdrname);
                        break;
                    case OR2GO_DA_LOCATION:
                        b = msg.getData();
                        String daid = b.getString("daid");
                        apicb = b.getParcelable("callback");
                        getDALocation(daid, apicb);
                        break;
                    case OR2GO_CRASH_REPORT:
                        b = msg.getData();
                        String report = b.getString("report");
                        apicb = b.getParcelable("callback");
                        postCrashReport(report, apicb);
                        break;
                }
                this.removeMessages(msg.what, msg);
            }
        };
        Looper.loop();
	}

    public Handler getHandler() {
        return mHandler;
    }

//    public boolean acquireApiSyncSem() {
//        try {
//            mApiSyncSemaphore.acquire();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
//
//    public boolean releaseApiSyncSem() {
//        mApiSyncSemaphore.release();
//        return true;
//    }

    public synchronized boolean postMessage(Message msg) {
        if (mHandler != null) {
            mHandler.sendMessage(msg);
            return true;
        }
        else
            return false;
    }

//    public void or2goAppInfoPub(final CommApiCallback callback) {
//        final String URL = BuildConfig.OR2GO_SERVER+"api/customerappinfopub/";
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("accesskey", "TKO135nrt246");
//        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
//        apiCaller.PostArrayRequest(URL, params, callback);
//    }

    private void or2goAllStore(CommApiCallback apicb) {
        final String URL = "https://or2go.in/api/allstorelistpub/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("accesskey", "TKO135nrt246");
        apiCaller.PostArrayRequest(URL, params, apicb);
    }

    public void or2goLogin(final CommApiCallback callback){
        final String URL = BuildConfig.OR2GO_SERVER+"api/custapplogin/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("password", gAppEnv.gAppSettings.getPassword());
        apiCaller.PostArrayRequest(URL, params, callback);
    }

    public void or2goLogout(){
        final String URL = BuildConfig.OR2GO_SERVER+"api/spmemberapplogout/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        apiCaller.PostArrayRequest(URL, params, null);
    }

    boolean getSessionId(final CommApiCallback callback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/memberlog1/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId()/*"8895269273"*/);
        apiCaller.PostArrayRequest(URL, params, callback);
        return true;
    }

    public boolean isLoggedIn() {
        if (mSessionId.isEmpty())
            return false;
        else
            return true;
    }


    ////User Registration APIs
    public boolean genRegisterOtp(String mobno) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custsignupotp/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", mobno);
        params.put("mobileno", mobno);
        params.put("appname", BuildConfig.APP_NAME);
        apiCaller.PostArrayRequest(URL, params, null);
        return true;
    }

    public boolean or2goRegister(String otp, final String custid, final String passwd, final String name, final String email, final String place, final String addr, final CommApiCallback callback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custsignup/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
        params.put("otpno", otp);
        params.put("name", name);
        params.put("mobileno", custid);
        params.put("phone", "");
        params.put("emailid", email);
        params.put("place", place);
        params.put("address", addr);
        params.put("customerid", custid);
        params.put("password", passwd);
        apiCaller.PostArrayRequest(URL, params, callback);
        return true;
    }

    boolean getSPInfo(final CommApiCallback callback) {
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/appmemberspinfo/";
        final String PUBURL = BuildConfig.OR2GO_SERVER+"api/appmemberspinfopub/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        if (gAppEnv.isLoggedIn()) {
            params.put("memberid", gAppEnv.gAppSettings.getUserId());
            params.put("smemberid", gAppEnv.getSessionId());
            params.put("spcode", BuildConfig.OR2GO_SP_CODE);
            apiCaller.PostArrayRequest(URL, params, callback);
        }
        else {
            params.put("accesskey", "SIN579cgi680");
            params.put("spcode", BuildConfig.OR2GO_SP_CODE);
            apiCaller.PostArrayRequest(PUBURL, params, callback);
        }
        return true;
    }

    ////Vendor Info APIs
    boolean getVendorList(final CommApiCallback callback) {
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/testappvendorlist/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        params.put("spcode", BuildConfig.OR2GO_SP_CODE);
        apiCaller.PostArrayRequest(URL, params, callback);
        return true;
    }

    boolean getVendorListPublic(final CommApiCallback callback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/appvendorlistpublic/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mypass", "aix678nml333");
        params.put("spcode", BuildConfig.OR2GO_SP_CODE);
        apiCaller.PostArrayRequest(URL, params, callback);
        return true;
    }

    boolean getVendorDBVersionList(final CommApiCallback callback) {
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/custstoredbvrsionlist/";
        final String PUBURL = BuildConfig.OR2GO_SERVER+"api/custstoredbvrsionlistpub/";
        //if (gAppEnv.isRegistered()) {
        if (gAppEnv.isLoggedIn()) {
            // POST params to be sent to the server
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("customerid", gAppEnv.gAppSettings.getUserId());
            params.put("custsessionid", gAppEnv.getSessionId());
            params.put("vendorid", BuildConfig.OR2GO_VENDORID);
            apiCaller.PostArrayRequest(URL, params, callback);
        }
        else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("accesskey", "TKO135nrt246");
            params.put("vendorid", BuildConfig.OR2GO_VENDORID);
            apiCaller.PostArrayRequest(PUBURL, params, callback);
        }
        return true;
    }

    boolean getStoreInfo(String storeid, final CommApiCallback callback)
    {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custstoreinfo/";
        final String PUBURL = BuildConfig.OR2GO_SERVER+"api/custstoreinfopub/";

        gAppEnv.getGposLogger().i("Info Request Store="+storeid);

        if(gAppEnv.isLoggedIn()) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("customerid", gAppEnv.gAppSettings.getUserId());
            params.put("custsessionid", gAppEnv.getSessionId());
            params.put("storeid", storeid);

            apiCaller.PostArrayRequest(URL, params, callback);
        }
        else
        {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("accesskey", "DEL579igi680");
            params.put("storeid", storeid);

            apiCaller.PostArrayRequest(PUBURL, params, callback);

        }

        return true;

    }

    boolean getProductList(String storeid, final CommApiCallback callback)
    {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custstoreproducts/";
        final String PUBURL = BuildConfig.OR2GO_SERVER+"api/custstoreproductspub/";

        gAppEnv.getGposLogger().i("Or2Go Product List Store="+storeid);
        if(gAppEnv.isLoggedIn()) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("customerid", gAppEnv.gAppSettings.getUserId());
            params.put("custsessionid", gAppEnv.getSessionId());
            params.put("storeid", storeid);

            apiCaller.PostArrayRequest(URL, params, callback);
        }
        else{
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("accesskey", "BOS357log468");
            params.put("storeid", storeid);

            apiCaller.PostArrayRequest(PUBURL, params, callback);
        }
        return true;

    }

    boolean getPriceList(String storeid, final CommApiCallback callback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custstorepricedata/";
        final String PUBURL = BuildConfig.OR2GO_SERVER+"api/custstorepricedatapub/";

        gAppEnv.getGposLogger().i("Or2Go Price List Request Store="+storeid);
        if(gAppEnv.isLoggedIn()) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("customerid", gAppEnv.gAppSettings.getUserId());
            params.put("custsessionid", gAppEnv.getSessionId());
            params.put("storeid", storeid);
            //params.put("dbversion", ver.toString());

            apiCaller.PostArrayRequest(URL, params, callback);
        }
        else{
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("accesskey", "BOS357log468");
            params.put("storeid", storeid);
            apiCaller.PostArrayRequest(PUBURL, params, callback);
        }
        return true;
    }

    boolean getOutOfStockData(String dbname,  final CommApiCallback callback) {
        if(!gAppEnv.isLoggedIn()) return false;

        final String URL = BuildConfig.OR2GO_SERVER+"api/appoutofstockbydb/";
        gAppEnv.getGposLogger().i("Or2Go OutOfStock Data DB name="+dbname);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        params.put("storedb", dbname);
        apiCaller.PostArrayRequest(URL, params, callback);
        return true;
    }

    boolean getStockStatus(String skuidlist, String storeid,  final CommApiCallback callback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custstockstatusbyskuid/";
        gAppEnv.getGposLogger().i("Or2Go StockStatus Store="+storeid+ "  packid list="+skuidlist);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("storeid", storeid);
        JSONObject postparams = new JSONObject(params);
        try {
            JSONArray packidarr = new JSONArray(skuidlist);
            postparams.put("skuids", packidarr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().d("Comm Manager StockStatus JSON packet:"+ postparams);
        apiCaller.PostArrayRequest(URL, postparams, callback);
        return true;
    }

    public boolean placeOrder( String orderpktstr, String storeid, final CommApiCallback callback) {
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("customerid", gAppEnv.gAppSettings.getUserId());
        params1.put("custsessionid", gAppEnv.getSessionId());
        params1.put("ordStoreId", storeid);
        JSONObject postparams = new JSONObject(params1);
        try {
            JSONObject orderPkt = new JSONObject(orderpktstr);
            postparams.put("orderinfo", orderPkt);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custapporderpost/";
        apiCaller.PostArrayRequest(URL, postparams, callback);
        return true;
    }

    boolean orderStatusUpdate(String orderid, String stsstoreid, Integer event, String desc, String header, final CommApiCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("storeid", stsstoreid);
        params.put("orderid", orderid);
        params.put("orderstatus", "3");  ///this is a dummy value as the value will be filled by Order FSM
        params.put("deliverystatus", "0");  ///this is a dummy value as the value will be filled by Order FSM
        params.put("paymentstatus", "0");  ///this is a dummy value as the value will be filled by Order FSM
        params.put("updatetime", gAppEnv.getCurTime());
        params.put("orderevent", event.toString());
        params.put("description", desc);
        JSONObject postparams = new JSONObject(params);
        try {
            JSONObject pktHeader = new JSONObject(header);
            postparams.put("Header", pktHeader);
            //postparams.p(pktHeader);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custorderstatusupd/";
        apiCaller.PostArrayRequest(URL, postparams, callback);
        return true;
    }

    boolean orderQuickItemsConfirm(String orderpktstr, final CommApiCallback callback) {
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("memberid", gAppEnv.gAppSettings.getUserId());
        params1.put("mobileno", gAppEnv.gAppSettings.getUserId());
        params1.put("smemberid", gAppEnv.getSessionId());
        params1.put("spcode", BuildConfig.OR2GO_SP_CODE);
        JSONObject postparams = new JSONObject(params1);
        try {
            JSONObject orderPkt = new JSONObject(orderpktstr);
            postparams.put("orderdata", orderPkt);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/spappplacetextorder/";
        apiCaller.PostArrayRequest(URL, postparams, callback);
        return true;
    }

    boolean orderImageStatusUpdate(String orderid, Integer status, String header, final CommApiCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        params.put("spcode", BuildConfig.OR2GO_SP_CODE);
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("orderid", orderid);
        params.put("datetime", gAppEnv.getCurTime());
        params.put("imagestatus", status.toString());
        JSONObject postparams = new JSONObject(params);
        try {
            JSONObject pktHeader = new JSONObject(header);
            postparams.put("Header", pktHeader);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().d("Comm Manager Order Image Status Update JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/appimagestatusupd/";
        apiCaller.PostArrayRequest(URL, postparams, callback);
        return true;
    }

    boolean orderPaymentStatusUpdate(String orderid, Integer ordstatus, Integer payevent, Integer paymode, String payinfo,  String header, final CommApiCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("orderid", orderid);
        params.put("orderstatus", ordstatus.toString());
        params.put("paymentevent", payevent.toString());
        params.put("paystatus", "0"); //dummy data .... to be set by server
        params.put("paymode", paymode.toString());
        params.put("paymentinfo", payinfo);
        JSONObject postparams = new JSONObject(params);
        try {
            JSONObject pktHeader = new JSONObject(header);
            postparams.put("Header", pktHeader);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custpaymentstatusupd/";
        apiCaller.PostArrayRequest(URL, postparams, callback);
        return true;
    }

    boolean orderPrePaymentStatusUpdate(String orderid, Integer status, Integer paystatus, Integer paymode, String paymentid,  String header, final CommApiCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        params.put("spcode", BuildConfig.OR2GO_SP_CODE);
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("orderid", orderid);
        params.put("status", status.toString());
        params.put("paymentstatus", paystatus.toString());
        params.put("paymode", paymode.toString());
        params.put("paymentid", paymentid);
        JSONObject postparams = new JSONObject(params);
        try {
            JSONObject pktHeader = new JSONObject(header);
            postparams.put("Header", pktHeader);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/apponlinepaystatus/";
        apiCaller.PostArrayRequest(URL, postparams, callback);
        return true;
    }


    private boolean postOrderRating(String ordid, String vendorid, String rating){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("store", BuildConfig.OR2GO_SP_CODE);
        params.put("time", gAppEnv.getCurTime());
        params.put("FeedbackDetails", rating);

        JSONObject postparams = new JSONObject(params);
        gAppEnv.getGposLogger().d("Comm Manager Rating JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custpostfeedback/";
        apiCaller.PostArrayRequest(URL, postparams, null);
        return true;
    }

    public boolean getActiveOrders(final CommApiCallback apiCallback) {
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/custactiveorders/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("appid", BuildConfig.OR2GO_APPID);
        apiCaller.PostArrayRequest(URL, params, apiCallback);
        return true;
    }

    public boolean getDeliveryModel(final CommApiCallback apiCallback) {
//        gAppEnv.getGposLogger().d("Comm Manager getDeliveryModel Vendorid:"+ BuildConfig.OR2GO_VENDORID);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custappdeliverylocations/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
        apiCaller.PostArrayRequest(URL, params, apiCallback);
        return true;
    }

    public boolean getLocationInfo(final CommApiCallback apiCallback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custappdeliverylocations/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        //params.put("storeid", BuildConfig.OR2GO_);
        apiCaller.PostArrayRequest(URL, params, apiCallback);
        return true;
    }

    public boolean getDiscountInfo(final CommApiCallback apiCallback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/custglobalcoupons/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
        apiCaller.PostArrayRequest(URL, params, apiCallback);
        return true;
    }

    public boolean getOrderDetails(String orderid, final CommApiCallback apiCallback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/spappporderdetails/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        params.put("spcode", BuildConfig.OR2GO_SP_CODE);
        params.put("orderid", orderid);
        apiCaller.PostArrayRequest(URL, params, apiCallback);
        return true;
    }


    public boolean getOrderHistory(final CommApiCallback apiCallback) {
        // Define the web service URL
        //final String URL = OR2GO_SERVER+"api/spappporderids/";
        final String URL = BuildConfig.OR2GO_SERVER+"api/custcompleteoids/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("appid", BuildConfig.OR2GO_APPID);
        //params.put("requestcount", "0");
         apiCaller.PostArrayRequest(URL, params, apiCallback);
        return true;
    }

    public boolean getOrderHistoryInfo(String orderid, final CommApiCallback apiCallback) {
        System.out.println("Comm Manager : gettting order history details for :"+orderid);
        //final String URL = OR2GO_SERVER+"api/spappmoanditemsbyid/";
        final String URL = BuildConfig.OR2GO_SERVER+"api/custcompleteodetails/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("orderid", orderid);
        apiCaller.PostArrayRequest(URL, params, apiCallback);
        return true;
    }

    private boolean postUserDeliveryAddress(String addrname, String deliaddr, String deliplace, String delilocality, String delisublocality, String landmark, String zipcode, String altcontact, String geoloc) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
        params.put("contact_name", addrname);
        params.put("contact_mobile", altcontact);
        params.put("contact_location", deliplace);
        params.put("contact_landmark", landmark);
        params.put("contact_address", deliaddr);
        params.put("contact_pincode", zipcode);
        params.put("locality", delilocality);
        params.put("sublocality", delisublocality);
        params.put("geocoordinates", geoloc);
        params.put("datetime", gAppEnv.getCurTime());
        JSONObject postparams = new JSONObject(params);
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custappaddresspost/";
        apiCaller.PostArrayRequest(URL, postparams, null);
        return true;
    }

    private boolean postUpdateDeliveryAddress(String addrname, String deliaddr, String deliplace,String locality, String sublocality, String landmark, String zipcode, String altcontact, String geoloc) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
        params.put("contact_name", addrname);
        params.put("contact_mobile", altcontact);
        params.put("contact_location", deliplace);
        params.put("contact_landmark", landmark);
        params.put("contact_address", deliaddr);
        params.put("contact_pincode", zipcode);
        params.put("locality", locality);
        params.put("sublocality", sublocality);
        params.put("geocoordinates", geoloc);
        params.put("datetime", gAppEnv.getCurTime());
        JSONObject postparams = new JSONObject(params);
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custappaddressedit/";
        apiCaller.PostArrayRequest(URL, postparams, null);
        return true;
    }

    private boolean postDeleteDeliveryAddress(String deladdrname) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("vendorid", BuildConfig.OR2GO_VENDORID);
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("contact_name", deladdrname);
        JSONObject postparams = new JSONObject(params);
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custappaddressdelete/";
        apiCaller.PostArrayRequest(URL, postparams, null);
        return true;
    }

    private boolean getDALocation(String daid, final CommApiCallback apiCallback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("daid", daid);
        params.put("accesskey", "BBI678bpi345");
        params.put("spcode", BuildConfig.OR2GO_SP_CODE);
        JSONObject postparams = new JSONObject(params);
        gAppEnv.getGposLogger().d("Comm Manager Order Palce JSON packet:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/dalocationviewpub/";
        apiCaller.PostArrayRequest(URL, postparams, apiCallback);
        return true;
    }

    private boolean postCrashReport(String report, final CommApiCallback apiCallback) {
        Integer versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        String buildVer = Build.VERSION.RELEASE;
        String buildDev = Build.DEVICE;
        String buildHardware = Build.HARDWARE;
        String devUser= Build.USER;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("customerid", gAppEnv.gAppSettings.getUserId());
        params.put("custsessionid", gAppEnv.getSessionId());
        params.put("appCode", BuildConfig.OR2GO_APPID);
        params.put("appType", "Or2Go Customer");
        params.put("appVersion", versionName+" "+versionCode.toString());
        params.put("appDeviceInfo", buildVer+ " " +buildDev+" "+ buildHardware+" "+devUser);
        params.put("appUserId", gAppEnv.gAppSettings.getUserId());
        params.put("crashInfo", report);
        JSONObject postparams = new JSONObject(params);
        gAppEnv.getGposLogger().d("CommManager crash report JSON:"+ postparams);
        final String URL = BuildConfig.OR2GO_SERVER+"api/custappcrashrptpost/";
        apiCaller.PostArrayRequest(URL, postparams, apiCallback);
        return true;
    }
}
