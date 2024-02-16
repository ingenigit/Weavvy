package com.or2go.vendor.weavvy;

import static com.or2go.core.Or2goConstValues.OR2GO_COMM_APPINFO;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_LOGIN;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_LOGOUT;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_VENDOR_STORE_LIST;
import static com.or2go.core.Or2goConstValues.OR2GO_LOGIN_STATUS_NONE;
import static com.or2go.core.Or2goConstValues.OR2GO_MAX_LOGIN_RETRY_COUNT;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.or2go.vendor.weavvy.manager.DataSyncManager;
import com.or2go.vendor.weavvy.manager.DeliveryManager;
import com.or2go.vendor.weavvy.manager.OrderManager;
import com.or2go.vendor.weavvy.manager.VendorManager;
import com.or2go.vendor.weavvy.notice.Or2goMQManager;
import com.or2go.vendor.weavvy.notice.Or2goMsgHandler;
import com.or2go.vendor.weavvy.notice.Or2goNotificationManager;
import com.or2go.vendor.weavvy.server.CompleteStoreCallback;
import com.or2go.vendor.weavvy.server.Or2goLogoutCallback;
import com.or2go.vendor.weavvy.server.StoreLoginCallback;
import com.or2go.vendor.weavvy.storeList.GposCommManager;
import com.or2go.vendor.weavvy.storeList.StoreInfoCallback;
import com.or2go.vendor.weavvy.storeList.StoreManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppEnv extends Application {

    public AppSetting gAppSettings;
    Boolean serverLoggedIn=false;
    String serverSessionID="";
    Integer gLoginAttemptCount=0;
    GposLogger gLogger = null;
    GposCommManager gCommMgr;
//    Or2goCommManager  gCommMgr;
    StoreManager gStoreMgr;
    Or2goMQManager gMsgManager;
    Or2goNotificationManager gNotificationMgr;
    DataSyncManager gDataSyncMgr;
    VendorManager gVendorMgr;
    OrderManager gOrderMgr;
    DeliveryManager gDeliveryManager;
    Or2goMsgHandler gMsgHandler;
    Integer gOr2goLogin=OR2GO_LOGIN_STATUS_NONE;

    @Override
    public void onCreate() {
        super.onCreate();
        Or2GoAppLifecycleHandler handler = new Or2GoAppLifecycleHandler();
        registerComponentCallbacks(handler);
        handler.setApplicationInfo(this);
    }

    public int startEnv(){
        gLogger = GposLogger.getLogger();
        gAppSettings = new AppSetting(this);
        gCommMgr = new GposCommManager(this);
        gDataSyncMgr = new DataSyncManager(this);
        gDataSyncMgr.start();
        gStoreMgr = new StoreManager(this);
        gVendorMgr = new VendorManager(this);
        gOrderMgr = new OrderManager(this);
        gDeliveryManager = new DeliveryManager(this);
        gNotificationMgr = new Or2goNotificationManager(this);
        gMsgHandler = new Or2goMsgHandler(this);
        gMsgHandler.start();
        gMsgManager = new Or2goMQManager(this);
        InitServerComm();
        return 0;
    }

    public boolean InitServerComm() {
        if (!isInternetOn()) {
            Log.e("AppEnv", "AppEnv error ....no internet connection ");
            return false;
        }
        boolean iscommready = false;
        while (!iscommready) {
            if (gCommMgr.isAlive()) iscommready = true;
            else {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        startNewFunction();
        return true;
    }

    public boolean startMQMaanger() {
        if (gMsgManager == null)
            gMsgManager = new Or2goMQManager(this);
        return true;
    }

    public void postLoginProcess() {
        getGposLogger().i("postLoginProcess called");
        startMQMaanger();
        gOrderMgr.getActiveOrders();
        //gOrderMgr.getAcceptedOrders();
        gDeliveryManager.getDAInfoList();
    }

    public GposLogger getGposLogger() { return gLogger;}
    public GposCommManager getCommMgr() {
        return gCommMgr;
    }
    public StoreManager getStoreManager() {
        return gStoreMgr;
    }
    public VendorManager getVendorManager() { return gVendorMgr;}
    public DeliveryManager getDeliveryManager() {return gDeliveryManager;}
    public OrderManager getOrderManager() { return gOrderMgr;}
    public DataSyncManager 	getDataSyncManager() {return gDataSyncMgr;}
    public Or2goNotificationManager getNotificationManager() { return gNotificationMgr;}

    public Handler getOr2goMsgHandler() {
        if (gMsgHandler == null)
            return null;
        else
            return gMsgHandler.getHandler();
    }

    public boolean isInternetOn() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connec.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            //Log.i("DataSyncTimerThread", "Internet is connected");
            return true;
        } else {
            //Log.i("DataSyncTimerThread", "No Internet connection");
            return false;
        }
    }

    private void startNewFunction() {
        Message msg = new Message();
        msg.what = OR2GO_COMM_APPINFO;//OR2GO_GET_SPINFO;	//fixed value for sending sales transaction to server
        msg.arg1 = 1;

        StoreInfoCallback appinfocb = new StoreInfoCallback(this);//Callback(mContext);
        Bundle b = new Bundle();
        b.putString("vendorid", "Or2Go_Test2");
        b.putParcelable("callback", appinfocb );
        msg.setData(b);
        gCommMgr.postMessage(msg);
    }

    public void getCompleteStoreList() {
        Message msg = new Message();
        msg.what = 358;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        CompleteStoreCallback completeStoreCallback = new CompleteStoreCallback(this, this);
        Bundle b = new Bundle();
        b.putParcelable("callback", completeStoreCallback );
        msg.setData(b);
        gCommMgr.postMessage(msg);
    }

    public void Or2goLogin(String vendid, String storeid, String passwd) {
        if (gLoginAttemptCount >= OR2GO_MAX_LOGIN_RETRY_COUNT) {
            //System.out.println("Login retry limit...");
            return;
        }

        Message msg = new Message();
        msg.what = OR2GO_COMM_LOGIN;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        StoreLoginCallback logincb = new StoreLoginCallback(this,this, vendid,storeid );//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", logincb );
        b.putString("vendorid", vendid);
        b.putString("storeid", storeid);
        b.putString("password", passwd);

        msg.setData(b);
        gCommMgr.postMessage(msg);
        gLoginAttemptCount++;
    }

    public void or2goLogout() {
        Message msg = new Message();
        msg.what = OR2GO_COMM_LOGOUT;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        Or2goLogoutCallback logincb = new Or2goLogoutCallback(this);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", logincb );
        msg.setData(b);
        gCommMgr.postMessage(msg);
    }

    public void setOr2goLoginStatus(Integer sts) {gOr2goLogin = sts;}

    public synchronized boolean setLoginState(boolean val) {
        serverLoggedIn = val;
        return true;
    }
    public synchronized boolean isLoggedIn()
    {
        return serverLoggedIn;
    }

    public synchronized boolean setSessionId(String session) {
        serverSessionID = session;
        return true;
    }
    public String getSessionId() { return serverSessionID; }

    public String getCurTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void appExit() {
        gMsgManager.shutdownMQ();
    }
}
