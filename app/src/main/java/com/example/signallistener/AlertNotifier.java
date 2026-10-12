package com.example.signallistener;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlertNotifier {

    private static final String CHANNEL_ID = "signal_alerts";
    private static final String CHANNEL_NAME = "Signal Alerts";
    private static final String CHANNEL_DESC = "هشدار دریافت سیگنال معتبر";

    private static boolean channelCreated = false;

    /** Create the system notification channel once per process. */
    private static void ensureChannel(Context ctx) {
        if (channelCreated) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            channelCreated = true;
            return;
        }
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm == null) return;

        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription(CHANNEL_DESC);

            // Sound (default notification sound)
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            ch.setSound(
                    Uri.parse("android.resource://" + ctx.getPackageName()
                            + "/" + android.provider.Settings.System.DEFAULT_NOTIFICATION_URI),
                    attrs);

            // Vibration pattern: wait, buzz, pause, buzz
            ch.enableVibration(true);
            ch.setVibrationPattern(new long[]{ 0L, 400L, 200L, 400L, 200L, 400L });
            ch.setLights(0xFF00FF00, 500, 500);

            nm.createNotificationChannel(ch);
        }
        channelCreated = true;
    }

    /**
     * Show the alert.
     * @param channelName  telegram channel name (for title)
     * @param sig          parsed signal
     */
    public static void show(Context ctx, String channelName, SignalParser.Signal sig) {
        ensureChannel(ctx);

        // Tap opens MainActivity
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                ctx,
                (int) (System.currentTimeMillis() & 0x7FFFFFFF),
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = "📡 " + sig.side + "  " + sig.symbol;
        String body = channelName + "\n"
                + "Entry: " + sig.entries + "\n"
                + "SL: " + sig.sl + "\n"
                + "TP: " + sig.tps;

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(sig.symbol + " " + sig.side
                        + " · Entry " + (sig.entries.isEmpty() ? "?" : sig.entries.get(0))
                        + " · SL " + sig.sl)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setWhen(System.currentTimeMillis())
                .setVibrate(new long[]{ 0L, 400L, 200L, 400L, 200L, 400L });

        Notification notif = b.build();
        notif.flags |= Notification.FLAG_INSISTENT; // repeat sound until user dismisses

        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        int id = (int) (System.currentTimeMillis() & 0x7FFFFFFF);
        try {
            nm.notify(id, notif);
        } catch (SecurityException e) {
            MainActivity.appendLog("! Notifications permission missing: " + e.getMessage());
        }
    }
}
