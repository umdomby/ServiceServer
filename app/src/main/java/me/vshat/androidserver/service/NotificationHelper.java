package me.vshat.androidserver.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper {
    public static final int NOTIFICATION_ID = 1;

    private static final String CHANNEL_PREFIX = NotificationHelper.class.getCanonicalName();
    private static final String CHANNEL_ID = CHANNEL_PREFIX + "default_id";
    private static final String CHANNEL_NAME = CHANNEL_PREFIX + "default_name";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    public Notification createNotification(String text) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setChannelId(CHANNEL_ID)
                .setContentText(text)
                .setContentTitle("AndroidServer")
                .setSound(null)
                .setOngoing(true)
                .build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableLights(false);
        notificationChannel.enableVibration(false);
        notificationChannel.setSound(null, null);
        notificationManager.createNotificationChannel(notificationChannel);
    }


}
