package com.or2go.weavvy.service;

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

import com.or2go.weavvy.R;
import com.or2go.weavvy.SplashScreen;

public class PublicNotificationManager {

    int notificationid = 1;
    String channelid = "Or2GoPublicNotificationService";
    NotificationManager mNotificationManager;
    NotificationChannel mChannel = null;

    Context mContext;

    public PublicNotificationManager(Context context, String channel, int notifyid)
    {
        mContext = context;
        notificationid = notifyid;
        channelid = channel;
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        //mNotificationManager = NotificationManagerCompat.from(mContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;//IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(channelid, "Or2GoPublicNotificationService", importance);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);

            //mNotificationManager.createNotificationChannel(mChannel);
        }

        mNotificationManager.cancelAll();
    }

    public boolean setNotification(String title, String msg)
    {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, channelid);

        mBuilder.setSmallIcon(R.drawable.ic_baseline_store_24);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText("Order Status Update");
        mBuilder.setAutoCancel(true);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_baseline_store_24));
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));


        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri);


        Intent handler = new Intent(mContext, SplashScreen.class);
        handler.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, handler, 0);

        //To be fixed...
        mBuilder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification notification = mBuilder.build();
            mNotificationManager.notify(notificationid, notification);

        }
        else {
            Notification notification = mBuilder.build();
            mNotificationManager.notify(notificationid, notification);
        }

        return true;
    }
}

