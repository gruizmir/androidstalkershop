package com.gabrielruizm.stalkershop;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    String TAG = "StalkerShop";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(),false);
            } else if (GoogleCloudMessaging. MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(), false);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging. MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.getString("express")=="1")
                    Log.i(TAG, "OK");
                else
                    Log.i(TAG, "No");
                sendNotification(extras.getString("message"), false);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, Boolean express) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        try {
            JSONObject obj = new JSONObject(msg);
            String cloudMessage = obj.getString("message");
            int exp = obj.getInt("express");

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("¡Nueva oferta en StalkerShop!")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(cloudMessage))
                            .setContentText(cloudMessage);

            if (exp==1)
                mBuilder.setSound(Settings.System.DEFAULT_ALARM_ALERT_URI);
            else
                mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
