package com.example.signallistener;

import org.json.JSONException;
import org.json.JSONObject;

public class ChannelConfig {
    public static final String ROUTE_NONE = "NONE";
    public static final String ROUTE_MT5  = "MT5";
    public static final String ROUTE_API  = "API";

    public String name;
    public boolean enabled = false;
    public String route = ROUTE_NONE;
    public String mt5Target = "default";   // for future multi-MT5 ("mt5_brokerA", ...)
    public String apiEndpoint = "";
    public String apiKey = "";
    public int signalsReceived = 0;
    public long lastSignalTime = 0L;

    public ChannelConfig(String name) { this.name = name; }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", name);
        o.put("enabled", enabled);
        o.put("route", route);
        o.put("mt5Target", mt5Target);
        o.put("apiEndpoint", apiEndpoint);
        o.put("apiKey", apiKey);
        o.put("signalsReceived", signalsReceived);
        o.put("lastSignalTime", lastSignalTime);
        return o;
    }

    public static ChannelConfig fromJson(JSONObject o) {
        ChannelConfig c = new ChannelConfig(o.optString("name"));
        c.enabled = o.optBoolean("enabled", false);
        c.route = o.optString("route", ROUTE_NONE);
        c.mt5Target = o.optString("mt5Target", "default");
        c.apiEndpoint = o.optString("apiEndpoint", "");
        c.apiKey = o.optString("apiKey", "");
        c.signalsReceived = o.optInt("signalsReceived", 0);
        c.lastSignalTime = o.optLong("lastSignalTime", 0L);
        return c;
    }
}
