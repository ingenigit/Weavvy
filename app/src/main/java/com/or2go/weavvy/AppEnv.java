package com.or2go.weavvy;

import static com.or2go.core.Or2goConstValues.OR2GO_INIT_COMPLETE;
import static com.or2go.core.Or2goConstValues.OR2GO_INIT_NONE;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.or2go.weavvy.manager.DataSyncManager;
import com.or2go.weavvy.manager.DeliveryManager;
import com.or2go.weavvy.manager.DiscountManager;
import com.or2go.weavvy.manager.GposCommManager;
import com.or2go.weavvy.manager.GposMQManager;
import com.or2go.weavvy.manager.GposNotificationManager;
import com.or2go.weavvy.manager.OrderCartManager;
import com.or2go.weavvy.manager.OrderHistoryManager;
import com.or2go.weavvy.manager.OrderManager;
import com.or2go.weavvy.manager.ProductManager;
import com.or2go.weavvy.manager.SPManager;
import com.or2go.weavvy.manager.SearchManager;
import com.or2go.weavvy.manager.StoreManager;
import com.or2go.weavvy.server.CompleteStoreCallback;
import com.or2go.weavvy.service.OrderUpdateWorker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AppEnv extends Application {

    boolean SPCloseNoticeDone = false;
    Boolean serverLoggedIn = false;
    String serverSessionID = "";
    String serverConnStatus = "LoggedOut";
    Integer gLoginAttemptCount = 0;
    String sDeviceID;
    String curDate;
    boolean gEnvInitialized = false;
    Integer gEnvInitStatus = OR2GO_INIT_NONE;

    GposLogger gLogger = null;
    GposCommManager gCommMgr;
    public StoreManager gStoreMgr;
    OrderHistoryManager gOrderHistoryManager;
    ProductManager gProductMgr;
    SearchManager gSearchMgr;
    OrderCartManager gCartMgr;
    OrderManager gOrderMgr;
    DeliveryManager gDeliveryMgr;
    DiscountManager gDiscountMgr;
    GposNotificationManager gNotificationMgr;
    GposMQManager gMsgManager;
    GposMsgHandler gMsgHandler;
    DataSyncManager gDataSyncMgr;
    SPManager gSPMgr;
    public AppSetting gAppSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        Or2GoAppLifecycleHandler handler = new Or2GoAppLifecycleHandler();
        registerComponentCallbacks(handler);
        registerActivityLifecycleCallbacks(handler);
        handler.setApplicationInfo(this);

        Thread.setDefaultUncaughtExceptionHandler(new Or2GoExceptionHandler(this));
    }

    public int startEnv(){
        gLogger = GposLogger.getLogger();
        gAppSettings = new AppSetting(this);
        sDeviceID = gAppSettings.getServerID();
        gSPMgr = new SPManager(this);
        gNotificationMgr = new GposNotificationManager(this);
        gCommMgr = new GposCommManager(this);
        gStoreMgr = new StoreManager(this);
        curDate = getCurDate();
        gSearchMgr = new SearchManager(this);
        gOrderMgr = new OrderManager(this);
        gDeliveryMgr = new DeliveryManager(this);
        gDiscountMgr = new DiscountManager(this);
        gCartMgr = new OrderCartManager(this);
        gProductMgr = new ProductManager(this);
        gMsgHandler = new GposMsgHandler(this);
        gMsgHandler.start();
        InitServerComm();
        gDataSyncMgr = new DataSyncManager(this);
        gEnvInitialized = true;
        return 0;
    }

    public boolean getEnvStatus() {
        return gEnvInitialized;
    }

    public String getCurDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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
        or2goGetStoreList();
        //for registration dialog
//        if (isRegistered()) {
//            if (gAppSettings.getUserType().equals(""))
//                or2goLogin();
//            else{
//                setLoginState(true);
//                postLoginProcess();
//            }
//        }
        return true;
    }

    private void or2goGetStoreList() {
        Message msg = new Message();
        msg.what = 358;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;

        CompleteStoreCallback completeStoreCallback = new CompleteStoreCallback(this, this);
        Bundle b = new Bundle();
        b.putParcelable("callback", completeStoreCallback );
        msg.setData(b);
        gCommMgr.postMessage(msg);
    }

    public GposLogger getGposLogger() {
        return gLogger;
    }
    public GposCommManager getCommMgr() {
        return gCommMgr;
    }
    public StoreManager getStoreManager() {
        return gStoreMgr;
    }
    public SPManager getSPManager() {
        return gSPMgr;
    }
    public ProductManager getProductManager() {
        return gProductMgr;
    }
    public SearchManager getSearchManager() {
        return gSearchMgr;
    }
    public OrderCartManager getCartManager() {
        return gCartMgr;
    }
    public OrderManager getOrderManager() {
        return gOrderMgr;
    }
    public DeliveryManager getDeliveryManager() {
        return gDeliveryMgr;
    }
    public DiscountManager getDiscountManager() {
        return gDiscountMgr;
    }
    public DataSyncManager getDataSyncManager() {return gDataSyncMgr;}
    public OrderHistoryManager getOrderHistoryManager(){return gOrderHistoryManager;}
    public GposNotificationManager getNotificationManager() {return gNotificationMgr;}
    public boolean initCartManager() { gCartMgr = new OrderCartManager(this); return true;}

    public Handler getGposMsgHandler() {
        if (gMsgHandler == null)
            return null;
        else
            return gMsgHandler.getHandler();
    }

    public boolean isRegistered() {
        String userid = gAppSettings.getUserId();
        if ((userid == null) || (userid.isEmpty()))
            return false;
        else
            return true;
    }

    public void startBackgroundMessageCheck() {
        System.out.println("AppEnv : starting backgroundOrderCheck");
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        Data data = new Data.Builder()
                .putString(OrderUpdateWorker.USERID, gAppSettings.getUserId())
                .build();

        final PeriodicWorkRequest periodicWorkRequest2
                = new PeriodicWorkRequest.Builder(OrderUpdateWorker.class, 15, TimeUnit.MINUTES)
                .addTag("Or2goMessageCheck")
                .setConstraints(constraints)
                .setInputData(data)
                .build();

        //WorkManager.getInstance().enqueue(periodicWorkRequest2);
        WorkManager.getInstance().enqueueUniquePeriodicWork("Or2goMessageCheck", ExistingPeriodicWorkPolicy.KEEP , periodicWorkRequest2);


    }

    public synchronized boolean setLoginState(boolean val) {
        serverLoggedIn = val;
        return true;
    }

    public synchronized boolean isLoggedIn() {
        return serverLoggedIn;
    }

    public boolean setSessionId(String session) {
        serverSessionID = session;
        return true;
    }

    public String getSessionId() { return serverSessionID; }

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

    public String getCurTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public boolean isAppInitializationComplete() {
        if (gEnvInitStatus==OR2GO_INIT_COMPLETE) return true;
        else
            return false;
    }

    public void setAppInitializationStatus(Integer sts) { gEnvInitStatus=sts; }

    public boolean ShutdownAppEnv() {
        if (gEnvInitialized == false) return false;
        gEnvInitialized = false;
        if (gMsgManager != null) gMsgManager.shutdownMQ();
        return true;
    }

    public void appExit() {
        ShutdownAppEnv();
    }
}
