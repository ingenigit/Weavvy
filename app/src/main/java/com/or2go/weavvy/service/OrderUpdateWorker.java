package com.or2go.weavvy.service;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.R;
import com.or2go.weavvy.SplashScreen;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.List;

public class OrderUpdateWorker extends Worker {

    public static final String TASK_DESC = "Or2GoOrderUpdate";
    public static final String USERID = "UserId";

    Context mContext;
    AppEnv gAppEnv;

    Connection gposConn;

    public OrderUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        mContext = context;
        gAppEnv = (AppEnv)context;
    }


    @NonNull
    @Override
    public Result doWork() {

        //getting the input data
        String userid = getInputData().getString(USERID);

        //if (!isAppActive(mContext,"genipos.customer"))
        //if (isAppInActive(mContext,"genipos.customer"))
        {

            //System.out.println("OrderUpdateWorker: or2go app inactive...checking messages");
            if (!userid.isEmpty()) {
                if (checkMessageQueue(userid) > 0) {

                    //System.out.println("OrderUpdateWorker: Setting notification");
                    setNotification(mContext, BuildConfig.APP_NAME, "Your order has a status update.");
                }
            }
            //else
            //    System.out.println("OrderUpdateWorker: user not registered");
        }
        //displayNotification("My Worker", taskDesc);
        return Result.success();
    }

    public boolean setNotification(Context context, String title, String msg)
    {
        final int NOTIFY_ID = 2;
        String channelid = "Or2GoOrderUpdateBackground";
        NotificationManager mNotificationManager;
        NotificationChannel mChannel = null;

        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        //mNotificationManager = NotificationManagerCompat.from(mContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(channelid, "Or2GoOrderUpdateBackground", importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);

            //mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelid);

        mBuilder.setSmallIcon(R.drawable.ic_baseline_notifications_24);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(msg);
        mBuilder.setAutoCancel(true);
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri);


        Intent handler = new Intent(mContext, SplashScreen.class);
        handler.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, handler, FLAG_IMMUTABLE);
        //To be fixed...
        mBuilder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification notification = mBuilder.build();
            mNotificationManager.notify(NOTIFY_ID, notification);

        }
        else {
            Notification notification = mBuilder.build();
            mNotificationManager.notify(NOTIFY_ID, notification);
        }

        return true;
    }

    private int checkMessageQueue(String userid)
    {

        //Connection gposConn;
        Channel gposMsgChannel=null;
        long msgcnt=0;

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("139.144.15.150");
        factory.setUsername("otgadmin");
        factory.setPassword("Ing3ni*Ortogo");
        factory.setRequestedHeartbeat(10);
        factory.setConnectionTimeout(5000);
        factory.setNetworkRecoveryInterval(5000);
        factory.setAutomaticRecoveryEnabled(true);

        //factory.setTopologyRecoveryEnabled(true);


        try {
            gposConn = factory.newConnection();
            gposMsgChannel = gposConn.createChannel();

            //msgcnt = gposMsgChannel.messageCount("or2go."+userid+".queue");
            //System.out.println("OrderUpdateWorker: pending message count="+msgcnt);
            boolean autoAck = false;
            /*GetResponse response = gposMsgChannel.basicGet(BuildConfig.OR2GO_APPID +"." +userid + ".queue", true);
            if (response == null) {
                // No message retrieved.
            } else {
                AMQP.BasicProperties props = response.getProps();
                byte[] body = response.getBody();
                String message = new String(body, "UTF-8");

                if (gAppEnv == null)
                {
                    System.out.println("Or2Go OrderUpdateWorker : AppEnv is null !!!");
                }
                else
                {
                    if (gAppEnv.getEnvStatus() == false)
                    {
                        System.out.println("Or2Go OrderUpdateWorker : Initializing AppEnv !!!");
                        gAppEnv.InitGeniposEnv();
                    }

                    Handler mHandler = gAppEnv.getGposMsgHandler();
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("GPOS", message);
                        msg.setData(bundle);
                        //msg.what = getMqmMsgNo();
                        mHandler.sendMessage(msg);
                    }
                }
            }*/
            msgcnt = gposMsgChannel.messageCount(BuildConfig.OR2GO_APPID +"." +userid + ".queue");

            if (msgcnt > 0) {
                PublicNotificationManager notifyMgr = new PublicNotificationManager(mContext, "publicnotice", 10);
                notifyMgr.setNotification(BuildConfig.APP_NAME, "Order Status Update");
            }

            gposMsgChannel.close();

            shutdownMQ();//gposConn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (int)msgcnt;
    }

    private boolean isAppActive(Context context,String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = appPackageName;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            //if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName))
            //if (appProcess.processName.equals(packageName))
            {
                //                Log.e("app",appPackageName);
                return true;
            }
        }
        return false;
    }

    private boolean isAppInActive(Context context,String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return true;
        }
        final String packageName = appPackageName;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            //if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
            System.out.println("Active Process:"+appProcess.processName+ " process importance"+appProcess.importance );
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE && appProcess.processName.equals(packageName))
            {
                return true;
            }
            /*else if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName))
            {
                return false;
            }*/
        }
        return false;
    }

    void shutdownMQ()
    {
        Thread mqStopThread = new Thread() {
            @Override
            public void run() {
                try {
                    if (gposConn!= null) gposConn.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        mqStopThread.start();

    }
}
