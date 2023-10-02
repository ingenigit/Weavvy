package com.or2go.vendor.weavvy.storeList;

import static com.or2go.core.Or2goConstValues.*;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.volleylibrary.CommApiCallback;
import com.or2go.volleylibrary.HttpVolleyHelper;

import java.util.HashMap;
import java.util.concurrent.Semaphore;


public class GposCommManager extends Thread {
    private Context mContext;
    AppEnv gAppEnv;
    public Handler mHandler;
    HttpVolleyHelper apiCaller;
    private Semaphore mApiSyncSemaphore;


    public GposCommManager(Context mContext) {
        this.mContext = mContext;
        gAppEnv = (AppEnv)mContext;
        apiCaller = new HttpVolleyHelper(mContext);
        mApiSyncSemaphore = new Semaphore(1, true);
        start();
    }

    @Override
    public void run() {
        Looper.prepare();
        Log.i("CommManager","CommManager : Comm message handler ready = " );
        mHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Integer nMsg = msg.what;
                Integer nSync = msg.arg1;
                if (nSync == OR2GO_COMM_SYNC_API) acquireApiSyncSem();
                Bundle b;
                CommApiCallback apicb;
                switch(nMsg) {
                    case OR2GO_COMM_APPINFO:
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        or2goAppInfoPub(apicb);
                        break;
                    case OR2GO_VENDOR_INFO:
                        b = msg.getData();
                        String vid = b.getString("vendorid");
                        apicb = b.getParcelable("callback");
                        //getVendorInfo(vid,  apicb);
                        getStoreInfo(vid,  apicb);
                        break;
                    case OR2GO_COMM_VENDOR_STORE_LIST:
                        System.out.println("CommManager Or2Go Multi Stores ...");
                        b = msg.getData();
                        apicb = b.getParcelable("callback");
                        String vendorId = b.getString("vendorid");
                        or2goMultiLogin(vendorId, apicb);
                        break;
                }
                this.removeMessages(msg.what, msg);
            }
        };
        Looper.loop();
    }

    private void or2goMultiLogin(String vendorId, CommApiCallback apicb) {
        final String URL = "http://139.144.15.150:3000/api/custstoredbvrsionlistpub/";
        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("vendorid", vendorId);
        params.put("accesskey", "TKO135nrt246");
        apiCaller.PostArrayRequest(URL, params, apicb);
    }

    private void getStoreInfo(String vid, CommApiCallback apicb) {
        final String URL = "http://139.144.15.150:3000/api/custstoreinfopub/";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("accesskey", "DEL579igi680");
        params.put("storeid", vid);
        apiCaller.PostArrayRequest(URL, params, apicb);
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void or2goAppInfoPub(final CommApiCallback callback) {
        final String URL = "http://139.144.15.150:3000/api/customerappinfopub/";

        // POST params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("accesskey", "TKO135nrt246");
        params.put("vendorid", "Or2Go_Test2");
        apiCaller.PostArrayRequest(URL, params, callback);
    }

    public boolean acquireApiSyncSem() {
        try {
            mApiSyncSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
    public boolean releaseApiSyncSem() {
        mApiSyncSemaphore.release();
        return true;
    }

    public synchronized boolean postMessage(Message msg) {
        if (mHandler != null) {
            mHandler.sendMessage(msg);
            return true;
        }
        else
            return false;
    }
}
