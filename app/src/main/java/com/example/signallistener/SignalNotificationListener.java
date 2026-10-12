package com.example.signallistener;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class SignalNotificationListener extends NotificationListenerService {

    public static final String TAG = "SignalListener";
    private static final String TELEGRAM_PKG = "org.telegram.messenger";

    public static SignalParser.Signal lastSignal = null;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!TELEGRAM_PKG.equals(sbn.getPackageName())) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        CharSequence titleCs = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence convCs  = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE);
        CharSequence textCs  = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence bigCs   = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        String channelName = (convCs != null && convCs.length() > 0)
                ? convCs.toString()
                : (titleCs != null ? titleCs.toString() : "");
        String full = bigCs != null ? bigCs.toString()
                    : (textCs != null ? textCs.toString() : "");

        if (channelName.isEmpty() || full.isEmpty()) return;

        Log.d(TAG, "Channel: " + channelName + " | Text: " + full);

        // Only register channels that carry a valid parsed signal
        SignalParser.Signal sig = SignalParser.parse(full);
        if (sig == null) return;

        ChannelStore store = ChannelStore.get(this);
        ChannelConfig cfg = store.findOrCreate(channelName);
        store.incrementSignal(channelName);

        MainActivity.appendLog("SIGNAL from [" + channelName + "]:\n" + sig);

        if (!cfg.enabled) {
            MainActivity.appendLog("(channel disabled — ignored)");
            return;
        }

        lastSignal = sig;

        // Always alert with sound/vibration, regardless of route
        AlertNotifier.show(this, channelName, sig);

        switch (cfg.route) {
            case ChannelConfig.ROUTE_MT5:
                MainActivity.appendLog("→ route MT5 (stub)");
                // TODO phase 5: MT5AutomationService.dispatch(sig, cfg);
                break;
            case ChannelConfig.ROUTE_API:
                MainActivity.appendLog("→ route API (stub)");
                // TODO phase 6: HttpSender.send(sig, cfg);
                break;
            default:
                MainActivity.appendLog("→ route NONE (log only)");
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }
}
