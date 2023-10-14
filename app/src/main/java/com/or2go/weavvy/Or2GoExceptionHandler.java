package com.or2go.weavvy;

import static com.or2go.core.Or2goConstValues.OR2GO_CRASH_REPORT;
import static com.or2go.weavvy.BuildConfig.DEBUG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.or2go.weavvy.server.CrashReportCallback;

public class Or2GoExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;
    //private Activity app = null;
    private String line;
    private Context context;
    private AppEnv gAppEnv;


    public Or2GoExceptionHandler(Context conx) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        //this.app = app;
        context = conx;
        gAppEnv=(AppEnv)conx;
    }

    public void uncaughtException(Thread t, Throwable e) {


        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++) {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

        Log.e("UnhandledException",
                "Uncaught Exception thread: "+report+ " report size="+report.length());

        try {

            if (!DEBUG)
                postCrashReport(report);

        } catch(Exception ioe) {
            // ...
        }

        defaultUEH.uncaughtException(t, e);
    }

    private void postCrashReport(String report)
    {
        Message msg = new Message();
        msg.what = OR2GO_CRASH_REPORT;
        msg.arg1 = 0;


        CrashReportCallback crashcb = new CrashReportCallback(context);//Callback(mContext);
        Bundle b = new Bundle();
        b.putParcelable("callback", crashcb );
        b.putString("report",report);
        msg.setData(b);

        gAppEnv.getCommMgr().postMessage(msg);
    }


}
