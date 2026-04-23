package com.temmahadi.packyourbag;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.temmahadi.packyourbag.Data.TripSession;
import com.temmahadi.packyourbag.DataBase.roomDB;

public class PackingReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "packing_reminder_channel";
    private static final int NOTIFICATION_ID = 2104;

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        roomDB database = roomDB.getInstance(context);
        int tripId = TripSession.getOrCreateActiveTripId(context, database);
        int total = safeInt(database.mainDAO().getItemsCount(tripId));
        int packed = safeInt(database.mainDAO().getPackedCount(tripId));
        int pending = Math.max(total - packed, 0);

        String contentText = pending == 0
                ? context.getString(R.string.reminder_notification_done)
                : context.getString(R.string.reminder_notification_pending, pending);

        Intent openAppIntent = new Intent(context, SplashScreen.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_check_circle_outline_24)
                .setContentTitle(context.getString(R.string.reminder_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.reminder_channel_description));

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
