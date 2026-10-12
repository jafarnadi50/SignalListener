package com.example.signallistener;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChannelStore {
    private static final String PREFS = "signal_listener_channels";
    private static final String KEY   = "channels_json";

    private static ChannelStore instance;

    public interface Listener { void onChannelsChanged(); }

    private final SharedPreferences prefs;
    private final List<ChannelConfig> channels = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();

    private ChannelStore(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    public static synchronized ChannelStore get(Context ctx) {
        if (instance == null) instance = new ChannelStore(ctx);
        return instance;
    }

    public synchronized List<ChannelConfig> getAll() {
        return new ArrayList<>(channels);
    }

    public synchronized ChannelConfig find(String name) {
        for (ChannelConfig c : channels) if (c.name.equals(name)) return c;
        return null;
    }

    public synchronized ChannelConfig findOrCreate(String name) {
        ChannelConfig c = find(name);
        if (c == null) {
            c = new ChannelConfig(name);
            channels.add(c);
            save();
            notifyListeners();
        }
        return c;
    }

    public synchronized void incrementSignal(String name) {
        ChannelConfig c = findOrCreate(name);
        c.signalsReceived++;
        c.lastSignalTime = System.currentTimeMillis();
        save();
        notifyListeners();
    }

    public synchronized void update(ChannelConfig c) {
        save();
        notifyListeners();
    }

    public synchronized void remove(String name) {
        ChannelConfig c = find(name);
        if (c != null) {
            channels.remove(c);
            save();
            notifyListeners();
        }
    }

    private void load() {
        String json = prefs.getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            channels.clear();
            for (int i = 0; i < arr.length(); i++)
                channels.add(ChannelConfig.fromJson(arr.getJSONObject(i)));
        } catch (Exception ignored) { }
    }

    private void save() {
        try {
            JSONArray arr = new JSONArray();
            for (ChannelConfig c : channels) arr.put(c.toJson());
            prefs.edit().putString(KEY, arr.toString()).apply();
        } catch (Exception ignored) { }
    }

    public void addListener(Listener l)    { listeners.add(l);    }
    public void removeListener(Listener l) { listeners.remove(l); }

    private void notifyListeners() {
        for (Listener l : new ArrayList<>(listeners)) l.onChannelsChanged();
    }
}
