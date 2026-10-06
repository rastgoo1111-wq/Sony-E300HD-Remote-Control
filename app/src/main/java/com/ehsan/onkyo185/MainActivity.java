package com.ehsan.onkyo185;

import android.app.Activity;
import android.content.*;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import java.util.*;

public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private int carrierHz = 38000;
    private android.content.SharedPreferences prefs;
    private static final String PREF_DB = "ir_code_memory_v9_rasta";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF080A0D);
        getWindow().setNavigationBarColor(0xFF080A0D);
        ir=(ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
        carrierHz=select(38000);
        prefs=getSharedPreferences(PREF_DB, MODE_PRIVATE);
        WebView w=new WebView(this);
        w.setBackgroundColor(0xFF080A0D);
        WebSettings s=w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        w.setWebViewClient(new WebViewClient());
        w.addJavascriptInterface(new Bridge(),"AudioIR");
        try { w.loadUrl("file:///android_asset/index.html"); } catch(Exception e) { w.loadData("<h1>Error loading UI</h1>", "text/html", "utf-8"); }
        setContentView(w);
    }

    private boolean has(){return ir!=null&&ir.hasIrEmitter();}

    private int select(int want){
        if(!has())return 0;
        ConsumerIrManager.CarrierFrequencyRange[] r=ir.getCarrierFrequencies();
        if(r==null||r.length==0)return want;
        int best=-1,d=Integer.MAX_VALUE;
        for(ConsumerIrManager.CarrierFrequencyRange x:r){
            int lo=x.getMinFrequency(),hi=x.getMaxFrequency();
            if(want>=lo&&want<=hi)return want;
            int c=Math.max(lo,Math.min(want,hi)),q=Math.abs(c-want);
            if(q<d){d=q;best=c;}
        }
        return best==-1?want:best;
    }

    private void add(ArrayList<Integer>p,int b){
        for(int i=0;i<8;i++){
            p.add(560);
            p.add(((b>>i)&1)!=0?1690:560);
        }
    }

    private int[] nec(int a,int b,int c){
        ArrayList<Integer>p=new ArrayList<>();
        p.add(9000);p.add(4500);
        add(p,a);add(p,b);add(p,c);add(p,(~c)&255);
        p.add(560);
        return arr(p);
    }

    private int[] sirc(int cmd,int addr,int bits){
        ArrayList<Integer>p=new ArrayList<>();
        int n=bits==12?12:bits==20?20:15;
        int word;
        if(n==12)word=(cmd&0x7F)|((addr&0x1F)<<7);
        else if(n==15)word=(cmd&0x7F)|((addr&0xFF)<<7);
        else word=(cmd&0xFF)|((addr&0x1F)<<8);
        p.add(2400);p.add(600);
        for(int i=0;i<n;i++){p.add(((word>>i)&1)!=0?1200:600);p.add(600);}
        p.add(10000);
        return arr(p);
    }

    private int[] jvc(int custom,int data){
        ArrayList<Integer>p=new ArrayList<>();
        p.add(8440);p.add(4220);
        int w=((custom&255)<<8)|(data&255);
        for(int i=0;i<16;i++){p.add(527);p.add(((w>>i)&1)!=0?2110:1055);}
        p.add(527);p.add(45000);
        return arr(p);
    }

    private int[] arr(ArrayList<Integer>p){
        int[]a=new int[p.size()];
        for(int i=0;i<a.length;i++)a[i]=p.get(i);
        return a;
    }

    private String tx(String proto,int a,int b,int c,int hz,int bits){
        if(!has())return "NO_IR_EMITTER";
        try{
            int f=select(hz);
            if(f<=0)return "CARRIER_NOT_SUPPORTED";
            int[]p=proto.equals("NEC")?nec(a,b,c):proto.equals("SONY")?sirc(c,a,bits):jvc(a,c);
            ir.transmit(f,p);
            return "SENT@"+f+"Hz";
        }catch(Throwable t){
            return "ERROR:"+t.getClass().getSimpleName();
        }
    }

    private String db(){return prefs.getString("db","{}");}
    private void setDb(String json){prefs.edit().putString("db",json==null?"{}":json).apply();}
    private void share(String text){
        Intent i=new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT,"RASTA IR Code Database");
        i.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(Intent.createChooser(i,"Export IR codes"));
    }

    public class Bridge{
        @JavascriptInterface public boolean hasIR(){return has();}
        @JavascriptInterface public int getCarrierHz(){return carrierHz;}
        @JavascriptInterface public String send(String proto,int a,int b,int c,int hz,int bits){return tx(proto,a,b,c,hz,bits);}
        @JavascriptInterface public String loadDb(){return db();}
        @JavascriptInterface public void saveDb(String json){setDb(json);}
        @JavascriptInterface public void exportDb(String text){share(text);}
        @JavascriptInterface public void copyDb(String text){
            ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("RASTA IR Code Database",text));
            Toast.makeText(MainActivity.this,"کدها کپی شدند",Toast.LENGTH_SHORT).show();
        }
        @JavascriptInterface public void clearDb(){setDb("{}");}
    }
}
