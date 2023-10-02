package com.or2go.vendor.showstorenearme;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class Or2goNotificationManager {
    Context mContext;
    AppEnv gAppEnv;

    final int NOTIFY_ID = 0;
    String channelid = "Or2Go Service";
    NotificationManager mNotificationManager;
    NotificationChannel mChannel = null;


    Or2goNotificationManager(Context context)
    {
        mContext = context;
        gAppEnv = (AppEnv)context;

        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        //mNotificationManager = NotificationManagerCompat.from(mContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;//IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(channelid, "Or2Go Service Notification", importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);

            //mNotificationManager.createNotificationChannel(mChannel);
        }

        mNotificationManager.cancelAll();

    }

    public boolean setNotification(String title, String msg, Intent handler)
    {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, channelid);

        mBuilder.setSmallIcon(R.drawable.ic_baseline_notifications_24);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(msg);
        mBuilder.setAutoCancel(true);
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri);

        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        //PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, handler, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        //handler.putExtra("notification", msg);

        handler.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, handler, 0);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            pendingIntent = PendingIntent.getActivity(mContext, 0, handler, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        else
            pendingIntent = PendingIntent.getActivity(mContext, 0, handler, PendingIntent.FLAG_UPDATE_CURRENT);

        //To be fixed...
        mBuilder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int idx1 = msg.indexOf(":");
            String s1 = msg.substring(idx1+1);
            int idx2 = s1.indexOf(" ");
            String ordid = s1.substring(0,idx2);
            //int ordid = Integer.parseInt(s2);
            ///String ordid = s2

            Notification notification = mBuilder.build();
            mNotificationManager.notify(1, notification);

        }
        else {
            Notification notification = mBuilder.build();
            mNotificationManager.notify(NOTIFY_ID, notification);
        }

        return true;
    }
    /*
    public boolean setNotification(String title, String msg, Intent handler)
    {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, "0");

        mBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(msg);
        mBuilder.setAutoCancel(true);
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri);

        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        //PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, handler, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        handler.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, handler, 0);

        mBuilder.setContentIntent(pendingIntent);

        //NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(0, mBuilder.build());

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, mBuilder.build());

        return true;
    }
    */


    public boolean clearNotification(int id)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.cancel(id);
        }
        else
            mNotificationManager.cancelAll();

        return true;
    }

    public boolean clearAllNotifications() {
        mNotificationManager.cancelAll();
        return true;
    }
}
