package com.or2go.vendor.weavvy;

import static com.or2go.core.Or2goConstValues.OR2GO_ACTIVE_DA_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_ACTIVE_ORDER_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_ASSIGN_DA;
import static com.or2go.core.Or2goConstValues.OR2GO_CANCEL_DA_ASSIGN;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_LOGIN;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_LOGOUT;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_VENDOR_LOGIN;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_VENDOR_STORE_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_DA_ASSIGN_CANCEL;
import static com.or2go.core.Or2goConstValues.OR2GO_EVENT_DA_ASSIGN_REQUEST;
import static com.or2go.core.Or2goConstValues.OR2GO_ITEM_STOCK_VAL;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_DETAILS;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_STATUS_UPDATE;
import static com.or2go.core.Or2goConstValues.OR2GO_OUT_OF_STOCK_DATA;
import static com.or2go.core.Or2goConstValues.OR2GO_PRICE_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_PRODUCT_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_REGISTER;
import static com.or2go.core.Or2goConstValues.OR2GO_REGISTER_OTPREQ;
import static com.or2go.core.Or2goConstValues.OR2GO_SKU_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_STORE_INFO;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.volleylibrary.HttpVolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Or2goCommManager extends Thread {
    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;

    public Handler mHandler;

    //RequestQueue mRequstQueue;
    HttpVolleyHelper apiCaller;

    Or2goCommManager(Context context){
        //dstAddress = addr;
        //dstPort = port;
        ///this.textResponse = textResponse;

        mContext =context;

        //Get global application
        gAppEnv = (AppEnv)context;// getApplicationContext();

        apiCaller = new HttpVolleyHelper(mContext);

        //gVolleyImageMgr = new VolleyImageManager();
        start();

    }

    @Override
    public void run() {

        Looper.prepare();

        //gAppEnv.getGposLogger().i("CommManager : Comm message handler ready = ");
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                // Act on the message
                ////Toast.makeText(mContext, "SalesDBSync Got message", Toast.LENGTH_SHORT).show();
                Integer nMsg = msg.what;
                gAppEnv.getGposLogger().i("Message = " + msg.what+"   API No="+nMsg);
                Bundle b;
                CommApiCallback apicb;
                switch (nMsg) {
                    case OR2GO_COMM_LOGIN:
                        System.out.println("CommManager Or2Go Login ...");
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        String vendid = b.getString("vendorid");
                        String storeid = b.getString("storeid");
                        String passwd = b.getString("password");
                        or2goLogin(vendid, storeid, passwd, apicb);
                        break;
                    case OR2GO_COMM_VENDOR_STORE_LIST:
                        System.out.println("CommManager Or2Go Multi Login ...");
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        String vendorId = b.getString("vendorid");
                        or2goMultiLogin(vendorId, apicb);
                        break;
                    case OR2GO_COMM_VENDOR_LOGIN:
                        System.out.println("CommManager Or2Go Vendor Login ...");
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        String vendorid = b.getString("vendorid");
                        String vendpassword = b.getString("password");
                        or2goVendorLogin(vendorid, vendpassword, apicb);
                        break;
                    case OR2GO_COMM_LOGOUT:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        or2goLogout(apicb);
                        break;
                    case OR2GO_REGISTER_OTPREQ:
                        b = msg.getData();
                        String mobno = b.getString("mobno");

                        genRegisterOtp(mobno);
                        break;
                    case OR2GO_REGISTER:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");

                        String vid = b.getString("vendorid");
                        String appcode = b.getString("appcode");
                        String regmobno = b.getString("mobileno");
                        String otp = b.getString("otp");


                        //String otp, final String custid, final String name, final String email, final String place, final String addr
                        or2goRegister(otp, vid, appcode, regmobno, apicb);
                        break;

                    case OR2GO_STORE_INFO:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getStoreInfo(apicb);
                        break;
                    case OR2GO_PRODUCT_LIST:
                        b = msg.getData();
                        //String dbname = b.getString("storedb");
                        String pstoreid = b.getString("storeid");
                        apicb = b.getParcelable("callback");

                        getProductData(pstoreid, apicb);
                        break;
                    case OR2GO_PRICE_LIST:
                        b = msg.getData();
                        //String pricedbnname = b.getString("storedb");
                        //String pricevendid = b.getString("vendorid");
                        Integer pricever = b.getInt("dbver");
                        apicb = b.getParcelable("callback");

                        getPriceData(apicb);
                        break;
                    case OR2GO_SKU_LIST:
                        b = msg.getData();
                        //String pricedbnname = b.getString("storedb");
                        String skustoreid = b.getString("storeid");
                        //Integer pricever = b.getInt("dbver");
                        apicb = b.getParcelable("callback");

                        getSKUData(skustoreid, apicb);
                        break;
                    case OR2GO_OUT_OF_STOCK_DATA:
                        b = msg.getData();
                        String stkbnname = b.getString("dbname");
                        apicb = b.getParcelable("callback");

                        //getOutOfStockData(stkbnname, apicb);
                        break;
                    case OR2GO_ITEM_STOCK_VAL:
                        b = msg.getData();
                        String stkdbnname = b.getString("dbname");
                        String packidlist = b.getString("packidlist");
                        apicb = b.getParcelable("callback");

                        //getStockStatus(packidlist, stkdbnname, apicb);
                        break;
                    case OR2GO_ACTIVE_ORDER_LIST:
//                        gAppEnv.getGposLogger().i("Or2Go Active Order List Request session id="+gAppEnv.getSessionId());

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
                    case OR2GO_ORDER_STATUS_UPDATE:
                        b = msg.getData();

                        String stscustid = b.getString("customerid");
                        String stsstoreid = b.getString("storeid");
                        //String stsspcode = b.getString("spcode");
                        String stsorderid = b.getString("orderid");
                        Integer ordevent = b.getInt("orderevent");
                        String desc = b.getString("description");
                        String stspktheader =  b.getString("pktheader");
                        apicb = b.getParcelable("callback");

                        updateOrderStatus(stscustid, stsstoreid, stsorderid, ordevent, desc, stspktheader, apicb);
                        break;
                    case OR2GO_ACTIVE_DA_LIST:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        getActiveDAList(apicb);
                        break;
                    case OR2GO_ASSIGN_DA:
                        b = msg.getData();
                        String ordid = b.getString("orderid");
                        String daid = b.getString("daid");
                        String storid = b.getString("storeid");
                        String pkthader1 = b.getString("pktHeader");
                        apicb = b.getParcelable("callback");
                        assignDA(daid, ordid, storid, OR2GO_EVENT_DA_ASSIGN_REQUEST, pkthader1, apicb);
                        break;

                    case OR2GO_CANCEL_DA_ASSIGN:
                        b = msg.getData();
                        String cordid = b.getString("orderid");
                        String cdaid = b.getString("daid");
                        String cstorid = b.getString("storeid");
                        String pkthader2 = b.getString("pktHeader");
                        apicb = b.getParcelable("callback");
                        assignDA(cdaid, cordid, cstorid, OR2GO_EVENT_DA_ASSIGN_CANCEL, pkthader2, apicb);
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

    /*public boolean isStarted()
    {
        return this.isAlive();
    }*/

    public void StopThread() {
        this.interrupt();
        //join();
    }

    public synchronized boolean postMessage(Message msg)
    {
        gAppEnv.getGposLogger().i("CommManager post message called");
        if (mHandler != null) {
            mHandler.sendMessage(msg);
            return true;
        }
        else
            return false;
    }


    ////User Registration APIs
    public boolean genRegisterOtp(String mobno)
    {
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/vendorsignupotp/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mobileno", mobno);

        apiCaller.PostArrayRequest(URL, params, null);

        return true;

    }


    public boolean or2goRegister(String otp, final String vendid, final String appcode, final String mob, final CommApiCallback callback)
    {
        System.out.println("Registration parameters OTP="+otp+"  vendor="+vendid+" mobile="+mob+"  password"+appcode);
        final String URL = BuildConfig.OR2GO_SERVER+"api/appvendorsignup/";

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("vendorid", vendid);
        params.put("password", appcode);
        params.put("mobileno", mob);
        params.put("otpno", otp);

        apiCaller.PostArrayRequest(URL, params, callback);

        return true;

    }

    public void or2goLogin(String vendid, String storeid, String passwd, final CommApiCallback callback){

        gAppEnv.getGposLogger().i("CommManager : Calling com login storeid="+ storeid+"  passwd"+passwd );

        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstorelogin/";

        gAppEnv.getGposLogger().i("CommManager : Calling com login URL="+ URL );

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("vendorid", vendid);
        params.put("storeid", storeid);
        params.put("password", passwd);

        apiCaller.PostArrayRequest(URL, params, callback);

    }
    private void or2goMultiLogin(String vendorId, final CommApiCallback apicb) {
        // Define the web service URL
//        final String URL = BuildConfig.OR2GO_SERVER+"api/appstoreactivestorelist/";
        final String URL = BuildConfig.OR2GO_SERVER+"api/custstoredbvrsionlistpub/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("vendorid", vendorId);
        params.put("accesskey", "TKO135nrt246");

        apiCaller.PostArrayRequest(URL, params, apicb);
    }

    private void or2goVendorLogin(String vendorid, String password, final CommApiCallback apicb) {
        // Define the web service URL
//        final String URL = BuildConfig.OR2GO_SERVER+"api/appstoreactivestorelist/";
        //final String URL = BuildConfig.OR2GO_SERVER+"api/custstoredbvrsionlistpub/";
        final String URL = BuildConfig.OR2GO_SERVER+"api/appvendorloginv2/";

        gAppEnv.getGposLogger().i("Vendor Login Vendor="+vendorid +"password="+password);

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("vendorid", vendorid);
        params.put("password", password);

        apiCaller.PostArrayRequest(URL, params, apicb);
    }

    boolean getStoreInfo(final CommApiCallback callback)
    {
        final String URL = BuildConfig.OR2GO_SERVER+"api/appvendoractivestorelist/";

        gAppEnv.getGposLogger().i("Store Product Data for "+ gAppEnv.gAppSettings.getStoreId()+"Request session="+gAppEnv.getSessionId());

        if(gAppEnv.isLoggedIn()) {
            HashMap<String, String> params = new HashMap<String, String>();
//            params.put("storeid", gAppEnv.gAppSettings.getStoreId());
            params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
            params.put("vendorsessionid", gAppEnv.getSessionId());

            apiCaller.PostArrayRequest(URL, params, callback);
        }

        return true;
    }

    //DB APIs
    boolean getProductData(String storeid, final CommApiCallback callback)
    {
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstoreproducts/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/appvendorproducts/";

        gAppEnv.getGposLogger().i("Store Product Data for "+ storeid+" Vendor="+gAppEnv.gAppSettings.getVendorId()+" Request session="+gAppEnv.getSessionId());

        if(gAppEnv.isLoggedIn()) {
            HashMap<String, String> params = new HashMap<String, String>();

            if (gAppEnv.gAppSettings.getVendorType().equals("multi"))
            {
                params.put("storeid", storeid);
                params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
                params.put("vendorsessionid", gAppEnv.getSessionId());

                apiCaller.PostArrayRequest(AURL, params, callback);
            }
            else
            {
                params.put("storeid", gAppEnv.gAppSettings.getStoreId());
                params.put("storesessionid", gAppEnv.getSessionId());

                apiCaller.PostArrayRequest(URL, params, callback);
            }


        }

        return true;

    }

    boolean getPriceData(final CommApiCallback callback)
    {
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstorepricedata/";

        gAppEnv.getGposLogger().i("Or2Go Price Data Request");

        if(gAppEnv.isLoggedIn()) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("storeid", gAppEnv.gAppSettings.getStoreId());
            params.put("storesessionid", gAppEnv.getSessionId());

            apiCaller.PostArrayRequest(URL, params, callback);
        }

        return true;
    }

    boolean getSKUData(String storeid, final CommApiCallback callback)
    {
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstorepricedata/";//appstoreskudata/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/appvendorpricedata/";

        gAppEnv.getGposLogger().i("Or2Go SKU Data Request");

        if(gAppEnv.isLoggedIn()) {
            HashMap<String, String> params = new HashMap<String, String>();

            if (gAppEnv.gAppSettings.getVendorType().equals("multi"))
            {
                params.put("storeid", storeid);
                params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
                params.put("vendorsessionid", gAppEnv.getSessionId());

                apiCaller.PostArrayRequest(AURL, params, callback);
            }
            else {
                params.put("storeid", storeid);
                params.put("storesessionid", gAppEnv.getSessionId());

                apiCaller.PostArrayRequest(URL, params, callback);
            }
        }

        return true;
    }

    public boolean getActiveOrders(final CommApiCallback apiCallback)
    {

        gAppEnv.getGposLogger().i("Active Orders  vendid="+gAppEnv.gAppSettings.getVendorId()+ "spcode="+gAppEnv.gAppSettings.getSPID());
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstoreactiveorders/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/vendorappidactiveorder/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();

        if (gAppEnv.gAppSettings.getVendorType().equals("multi")) {
            params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
            params.put("vendorsessionid", gAppEnv.getSessionId());

            apiCaller.PostArrayRequest(AURL, params, apiCallback);
        }
        else {
            params.put("storeid", gAppEnv.gAppSettings.getStoreId());
            params.put("storesessionid", gAppEnv.getSessionId());

            apiCaller.PostArrayRequest(URL, params, apiCallback);
        }

        return true;
    }

    public boolean getOrderDetails(String orderid, final CommApiCallback apiCallback)
    {

        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/vendorapporderdetails/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/appvendororderdetails/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        if (gAppEnv.gAppSettings.getVendorType().equals("multi")) {
            params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
            params.put("vendorsessionid", gAppEnv.getSessionId());
            params.put("orderid", orderid);

            apiCaller.PostArrayRequest(AURL, params, apiCallback);
        }
        else {
            params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
            params.put("vsessionid", gAppEnv.getSessionId());
            params.put("orderid", orderid);

            apiCaller.PostArrayRequest(URL, params, apiCallback);
        }

        return true;
    }

    boolean getActiveDAList(final CommApiCallback callback)
    {
        gAppEnv.getGposLogger().i("Active DA List API called");
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstoreactivedalist/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/appvendoractivedalist/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();

        if (gAppEnv.gAppSettings.getVendorType().equals("multi")) {
            params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
            params.put("vendorsessionid", gAppEnv.getSessionId());
            apiCaller.PostArrayRequest(AURL, params, callback);
        }
        else {
            params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
            params.put("storeid", gAppEnv.gAppSettings.getStoreId());
            params.put("storesessionid", gAppEnv.getSessionId());
            apiCaller.PostArrayRequest(URL, params, callback);
        }

        return true;
    }

    boolean assignDA (String daid, String orderid, String storeid, Integer event, String header, final CommApiCallback callback)
    {
        gAppEnv.getGposLogger().i("Assign DA API called  Event="+event);
        // Define the web service URL
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstoredeliveryrequest/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/appvendordeliveryrequest/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
        params.put("storeid", storeid);

        //params.put("customerid", gAppEnv.gAppSettings.getVendorId());
        params.put("daid", daid);
        params.put("orderid", orderid);
        params.put("deliveryevent", event.toString());
        //params.put("Header", header);

        JSONObject postparams = new JSONObject(params);
        try {
            JSONObject pktHeader = new JSONObject(header);
            postparams.put("Header", pktHeader);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (gAppEnv.gAppSettings.getVendorType().equals("multi")) {
            try {
                postparams.put("vendorsessionid", gAppEnv.getSessionId());
            }catch(JSONException e){
                System.out.println("Error MUlti store: " + e);
            }
            apiCaller.PostArrayRequest(AURL, postparams, callback);
        }
        else {
            try {
                postparams.put("storesessionid", gAppEnv.getSessionId());
            }catch(JSONException e){
                System.out.println("Error Single store: " + e);
            }
            apiCaller.PostArrayRequest(URL, postparams, callback);
        }
        return true;
    }


    /*
    boolean getOutOfStockData(String dbname,  final CommApiCallback callback)
    {
        final String URL = OR2GO_SERVER+"api/appoutofstockbydb/";

        gAppEnv.getGposLogger().i("Or2Go OutOfStock Data DB name="+dbname);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        params.put("storedb", dbname);

        apiCaller.PostArrayRequest(URL, params, callback);

        return true;
    }

    boolean getStockStatus(String packidlist, String dbname,  final CommApiCallback callback)
    {
        final String URL = OR2GO_SERVER+"api/appstockbypackid/";

        gAppEnv.getGposLogger().i("Or2Go StockStatus DB name="+dbname+ "  packid list="+packidlist);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("memberid", gAppEnv.gAppSettings.getUserId());
        params.put("smemberid", gAppEnv.getSessionId());
        params.put("storedb", dbname);

        JSONObject postparams = new JSONObject(params);

        try {

            JSONArray packidarr = new JSONArray(packidlist);
            postparams.put("packid", packidarr);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        gAppEnv.getGposLogger().d("Comm Manager StockStatus JSON packet:"+ postparams);

        apiCaller.PostArrayRequest(URL, postparams, callback);

        return true;
    }*/

    boolean updateOrderStatus(String stscustid, String stsstoreid, String stsorderid, Integer updstatus, String desc, String header,  final CommApiCallback callback) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstoreactiveostatusupd/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/appvendoractiveostatusupd/";

        gAppEnv.getGposLogger().i("Or2Go Update Order Status  Order="+stsorderid+ "  status="+updstatus+"  cust="+stscustid);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("vendorid", gAppEnv.gAppSettings.getVendorId());
        params.put("storeid", stsstoreid /*gAppEnv.gAppSettings.getStoreId()*/);
        params.put("orderevent", updstatus.toString());
        params.put("orderid", stsorderid);
        params.put("orderstatus", "0");  ///this is a dummy value as the value will be filled by Order FSM
        params.put("deliverystatus", "0");  ///this is a dummy value as the value will be filled by Order FSM
        params.put("paymentstatus", "0");  ///this is a dummy value as the value will be filled by Order FSM
        params.put("customerid", stscustid);
        params.put("description", desc);
        params.put("updatetime", gAppEnv.getCurTime());

        JSONObject postparams = new JSONObject(params);

        try {
            JSONObject pktHeader = new JSONObject(header);
            postparams.put("Header", pktHeader);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("Comm Manager Order Status Update JSON packet:"+ postparams);

        if (gAppEnv.gAppSettings.getVendorType().equals("multi")) {
            try {
                postparams.put("vendorsessionid", gAppEnv.getSessionId());
            }catch(JSONException e){
                System.out.println("Error MUlti store: " + e);
            }
            apiCaller.PostArrayRequest(AURL, postparams, callback);
        }
        else {
            try {
                postparams.put("storesessionid", gAppEnv.getSessionId());
            }catch(JSONException e){
                System.out.println("Error Single store: " + e);
            }
            apiCaller.PostArrayRequest(URL, postparams, callback);
        }

        return true;
    }

    boolean or2goLogout( final CommApiCallback apicb) {
        final String URL = BuildConfig.OR2GO_SERVER+"api/appstorelogout/";
        final String AURL = BuildConfig.OR2GO_SERVER+"api/appvendorlogout/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();

        if (gAppEnv.gAppSettings.getVendorType().equals("multi")) {
            params.put("vendorsessionid", gAppEnv.getSessionId());
            params.put("vendorid", gAppEnv.gAppSettings.getVendorId());

            apiCaller.PostArrayRequest(AURL, params, apicb);
        }
        else {

            params.put("storeid", gAppEnv.gAppSettings.getStoreId());
            params.put("storesessionid", gAppEnv.getSessionId());

            apiCaller.PostArrayRequest(URL, params, apicb);
        }
        return true;
    }
}
