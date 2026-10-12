package com.example.signallistener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {

    private static TextView txtLog;
    private static final StringBuilder logBuffer = new StringBuilder();

    private ChannelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request POST_NOTIFICATIONS on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{ android.Manifest.permission.POST_NOTIFICATIONS }, 1001);
            }
        }

        txtLog = findViewById(R.id.txtLog);
        txtLog.setMovementMethod(new ScrollingMovementMethod());

        Button btnGrant = findViewById(R.id.btnGrant);
        btnGrant.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));

        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(v -> {
            String sample = "XAU/USD\n" +
                    "Signal position: BUY\n" +
                    "Entry 1: 4137\n" +
                    "Entry 2: 4131\n" +
                    "SL: 4126\n" +
                    "TP1: 4146\n" +
                    "TP2: 4156\n" +
                    "TP3: 4166\n";
            SignalParser.Signal s = SignalParser.parse(sample);
            appendLog(s != null ? "OK:\n" + s : "FAIL");
        });

        ChannelStore store = ChannelStore.get(this);

        RecyclerView rv = findViewById(R.id.recyclerChannels);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChannelAdapter(store.getAll());
        rv.setAdapter(adapter);

        store.addListener(() ->
                runOnUiThread(() -> adapter.refresh(store.getAll())));

        checkAccess();
        appendLog("Channels loaded: " + store.getAll().size());
    }

    private void checkAccess() {
        String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        boolean ok = flat != null && flat.contains(getPackageName());
        appendLog(ok ? "Access granted." : "Access NOT granted.");
    }

    public static void appendLog(String s) {
        logBuffer.append(s).append("\n\n");
        if (txtLog != null) {
            txtLog.post(() -> txtLog.setText(logBuffer.toString()));
        }
    }
}
