package com.gabrielruizm.stalkershop;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gabriel on 07-10-14.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public static final int EXP_NOTIFICATION_ID = 2;
    private NotificationManager mNotificationManager;
    final static String GROUP_KEY = "SHOP_OFFER";
    final static String EXP_GROUP_KEY = "SHOP_EXPRESS";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging. MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging. MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification(extras.getString("message"));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            JSONObject obj = new JSONObject(msg);
            String cloudMessage = obj.getString("message");
            String shop = obj.getString("shop");
            int exp = obj.getInt("express");

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("¡Nueva oferta en StalkerShop!")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(cloudMessage))
                            .setContentText(cloudMessage);

            SharedPreferences shopPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean express = (exp==1 && shopPreferences.getBoolean(shop.toLowerCase(), false));
            if (express) {
                mBuilder.setSound(Settings.System.DEFAULT_ALARM_ALERT_URI)
                        .setOnlyAlertOnce(true)
                        .setGroup(EXP_GROUP_KEY);
            }
            else {
                mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setOnlyAlertOnce(true)
                        .setGroup(GROUP_KEY);
            }
            mBuilder.setContentIntent(contentIntent);
            Notification notification = mBuilder.build();
            if (express)
                mNotificationManager.notify(EXP_NOTIFICATION_ID, notification);
            else
                mNotificationManager.notify(NOTIFICATION_ID, notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
