package com.mm.myandroidchat;

import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.mm.myandroidchat.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


//TODO NIE DZIALA HEHE :D
        NotificationCompat.Builder newMessageNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New Friend Request")
                .setContentText("Youjkglkjhg");


        int notificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, newMessageNotification.build());







    }



}
