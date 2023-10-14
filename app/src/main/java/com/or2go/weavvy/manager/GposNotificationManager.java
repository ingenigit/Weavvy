package com.or2go.weavvy.manager;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.R;

public class GposNotificationManager {

    Context mContext;
    AppEnv gAppEnv;
    final int NOTIFY_ID = 0;
    final int PUBLIC_NOTIFY_ID = 1;
    String channelid = "Or2Go Service";
    NotificationManager mNotificationManager;
    NotificationChannel mChannel = null;

    public GposNotificationManager(Context context) {
        mContext = context;
        gAppEnv = (AppEnv)context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;//IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(channelid, "Or2Go Service Notification", importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    public boolean setNotification(String title, String msg, Intent handler) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, channelid);
        mBuilder.setSmallIcon(R.drawable.ic_baseline_notifications_24);
        mBuilder.setContentTitle(BuildConfig.APP_NAME);
        mBuilder.setContentText(title);
        mBuilder.setAutoCancel(true);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_baseline_deletable_24));
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));

        mBuilder.setAutoCancel(true);
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri);

        if (handler != null) {
            handler.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                pendingIntent = PendingIntent.getActivity(mContext, 0, handler, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            else
                pendingIntent = PendingIntent.getActivity(mContext, 0, handler, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int idx1 = msg.indexOf(":");
            String s1 = msg.substring(idx1+1);
            int idx2 = s1.indexOf(" ");
            //String s2 = s1.substring(0,idx2);
            String ordid = s1.substring(0,idx2);
            //int ordid = Integer.parseInt(s2);
            gAppEnv.getGposLogger().d("GposNotificationManager: notify with id="+ordid);
            Notification notification = mBuilder.build();
            mNotificationManager.notify(NOTIFY_ID, notification);
        }
        else {
            Notification notification = mBuilder.build();
            mNotificationManager.notify(NOTIFY_ID, notification);
        }
        return true;
    }

    public boolean clearAllNotifications() {
        gAppEnv.getGposLogger().d("Notification Manager : Clearing all notifications !!!!!");
        mNotificationManager.cancelAll();
        return true;
    }
}
