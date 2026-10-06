package com.ehsan.onkyo185;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private int carrierHz = 38000;
    private SharedPreferences prefs;
    private static final String PREF_DB = "ir_code_memory_v9_rasta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFF080A0D);
        getWindow().setNavigationBarColor(0xFF080A0D);

        ir = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
        carrierHz = selectCarrier(38000);
        prefs = getSharedPreferences(PREF_DB, MODE_PRIVATE);

        WebView webView = new WebView(this);
        webView.setBackgroundColor(0xFF080A0D);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Bridge(), "AudioIR");
        webView.loadUrl("file:///android_asset/index.html");

        setContentView(webView);
    }

    private boolean hasIRTransmitter() {
        return ir != null && ir.hasIrEmitter();
    }

    private int selectCarrier(int requested) {
        if (!hasIRTransmitter()) return 0;
        ConsumerIrManager.CarrierFrequencyRange[] ranges = ir.getCarrierFrequencies();
        if (ranges == null || ranges.length == 0) return requested;

        int best = requested;
        int bestDelta = Integer.MAX_VALUE;

        for (ConsumerIrManager.CarrierFrequencyRange range : ranges) {
            int lo = range.getMinFrequency();
            int hi = range.getMaxFrequency();
            if (requested >= lo && requested <= hi) return requested;
            int candidate = Math.max(lo, Math.min(requested, hi));
            int delta = Math.abs(candidate - requested);
            if (delta < bestDelta) {
                bestDelta = delta;
                best = candidate;
            }
        }
        return best;
    }

    private int[] makeNec(int a, int b, int c) {
        ArrayList<Integer> pulses = new ArrayList<>();
        pulses.add(9000);
        pulses.add(4500);
        for (int value : new int[]{a, b, c, (~c) & 0xFF}) {
            for (int i = 0; i < 8; i++) {
                pulses.add(560);
                pulses.add(((value >> i) & 1) != 0 ? 1690 : 560);
            }
        }
        pulses.add(560);
        int[] arr = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) arr[i] = pulses.get(i);
        return arr;
    }

    private int[] makeSony(int command, int address, int bits) {
        ArrayList<Integer> pulses = new ArrayList<>();
        int n = bits == 12 ? 12 : bits == 20 ? 20 : 15;
        int word;
        if (n == 12) word = (command & 0x7F) | ((address & 0x1F) << 7);
        else if (n == 15) word = (command & 0x7F) | ((address & 0xFF) << 7);
        else word = (command & 0xFF) | ((address & 0x1F) << 8);

        pulses.add(2400);
        pulses.add(600);
        for (int i = 0; i < n; i++) {
            pulses.add(((word >> i) & 1) != 0 ? 1200 : 600);
            pulses.add(600);
        }
        pulses.add(10000);

        int[] arr = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) arr[i] = pulses.get(i);
        return arr;
    }

    private int[] makeJvc(int custom, int data) {
        ArrayList<Integer> pulses = new ArrayList<>();
        pulses.add(8440);
        pulses.add(4220);
        int word = ((custom & 0xFF) << 8) | (data & 0xFF);
        for (int i = 0; i < 16; i++) {
            pulses.add(527);
            pulses.add(((word >> i) & 1) != 0 ? 2110 : 1055);
        }
        pulses.add(527);
        pulses.add(45000);

        int[] arr = new int[pulses.size()];
        for (int i = 0; i < pulses.size(); i++) arr[i] = pulses.get(i);
        return arr;
    }

    private String transmit(String protocol, int a, int b, int c, int hz, int bits) {
        if (!hasIRTransmitter()) return "NO_IR_EMITTER";
        try {
            int carrier = selectCarrier(hz);
            if (carrier <= 0) return "CARRIER_NOT_SUPPORTED";

            int[] pattern;
            if (protocol.equals("NEC")) pattern = makeNec(a, b, c);
            else if (protocol.equals("SONY")) pattern = makeSony(c, a, bits);
            else pattern = makeJvc(a, c);

            ir.transmit(carrier, pattern);
            return "SENT@" + carrier + "Hz";
        } catch (Throwable t) {
            return "ERROR:" + t.getClass().getSimpleName();
        }
    }

    private String loadDB() {
        return prefs.getString("db", "{}");
    }

    private void saveDB(String value) {
        prefs.edit().putString("db", value == null ? "{}" : value).apply();
    }

    private void exportDb(String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "RASTA IR Database");
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, "Export IR Database"));
    }

    public class Bridge {
        @JavascriptInterface
        public boolean hasIR() { return hasIRTransmitter(); }

        @JavascriptInterface
        public int getCarrierHz() { return carrierHz; }

        @JavascriptInterface
        public String send(String protocol, int a, int b, int c, int hz, int bits) {
            return transmit(protocol, a, b, c, hz, bits);
        }

        @JavascriptInterface
        public String loadDb() { return loadDB(); }

        @JavascriptInterface
        public void saveDb(String json) { saveDB(json); }

        @JavascriptInterface
        public void exportDb(String text) { exportDb(text); }

        @JavascriptInterface
        public void copyDb(String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("RASTA IR Database", text));
            Toast.makeText(MainActivity.this, "کدها کپی شدند", Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void clearDb() { saveDB("{}"); }
    }
}
