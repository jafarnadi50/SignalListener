package com.example.signallistener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignalParser {

    public static class Signal {
        public String symbol;
        public String side;
        public List<Double> entries = new ArrayList<>();
        public Double sl;
        public List<Double> tps = new ArrayList<>();
        public String raw;

        @Override
        public String toString() {
            return "Symbol: " + symbol +
                   "\nSide: " + side +
                   "\nEntries: " + entries +
                   "\nSL: " + sl +
                   "\nTPs: " + tps;
        }
    }

    public static Signal parse(String text) {
        if (text == null) return null;
        String t = normalize(text);
        Signal s = new Signal();
        s.raw = text;

        Matcher sym = Pattern.compile(
                "\\b([A-Z]{2,6}/[A-Z]{2,6}|XAUUSD|GOLD|BTCUSDT|ETHUSDT)\\b",
                Pattern.CASE_INSENSITIVE).matcher(t);
        if (sym.find()) s.symbol = sym.group(1).toUpperCase();

        Matcher side = Pattern.compile(
                "(?:Signal position|position)\\s*[:：]?\\s*(BUY|SELL)",
                Pattern.CASE_INSENSITIVE).matcher(t);
        if (side.find()) s.side = side.group(1).toUpperCase();

        Matcher entry = Pattern.compile(
                "(?:Entry|ورود)\\s*\\d*\\s*[:：]\\s*([\\d.]+)",
                Pattern.CASE_INSENSITIVE).matcher(t);
        while (entry.find()) s.entries.add(Double.parseDouble(entry.group(1)));

        Matcher sl = Pattern.compile(
                "(?:SL|Stop Loss)\\s*[:：]?\\s*([\\d.]+)",
                Pattern.CASE_INSENSITIVE).matcher(t);
        if (sl.find()) s.sl = Double.parseDouble(sl.group(1));

        Matcher tp = Pattern.compile(
                "(?:TP|Take Profit)\\s*\\d*\\s*[:：]?\\s*([\\d.]+)",
                Pattern.CASE_INSENSITIVE).matcher(t);
        while (tp.find()) s.tps.add(Double.parseDouble(tp.group(1)));

        if (s.symbol == null || s.side == null || s.entries.isEmpty() || s.sl == null)
            return null;

        return s;
    }

    private static String normalize(String x) {
        return x.replace('ي', 'ی').replace('ك', 'ک')
                .replace('۰', '0').replace('۱', '1').replace('۲', '2')
                .replace('۳', '3').replace('۴', '4').replace('۵', '5')
                .replace('۶', '6').replace('۷', '7').replace('۸', '8')
                .replace('۹', '9');
    }
}
