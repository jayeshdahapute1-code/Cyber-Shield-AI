package com.cybershield.ai;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import java.net.URI;
import java.util.List;
import java.util.Locale;

public final class SecurityEngine {
    private SecurityEngine(){}
    public static class ScanResult{public int checked;public int risks;public String details;}
    public static ScanResult scanApps(Context c){
        ScanResult r=new ScanResult(); PackageManager pm=c.getPackageManager();
        List<ApplicationInfo> apps=pm.getInstalledApplications(PackageManager.GET_META_DATA); StringBuilder b=new StringBuilder();
        for(ApplicationInfo a:apps){if((a.flags&ApplicationInfo.FLAG_SYSTEM)!=0)continue;r.checked++;String p=a.packageName.toLowerCase(Locale.ROOT);
            if(p.contains("mod")||p.contains("crack")||p.contains("cheat")||p.contains("hack")||p.contains("spy")){r.risks++;b.append("• ").append(pm.getApplicationLabel(a)).append("\n");}}
        r.details=b.length()==0?"No simple package-name indicators found.":b.toString();return r;
    }
    public static String analyzeUrl(String raw){
        String v=raw==null?"":raw.trim();if(v.isEmpty())return"Enter a URL first.";String l=v.toLowerCase(Locale.ROOT);
        boolean https=l.startsWith("https://"), suspicious=l.contains("@")||l.contains("bit.ly")||l.contains("tinyurl")||l.contains("t.co/")||l.contains("login-")||l.contains("verify-")||l.contains("reward")||l.contains("free-");
        try{URI u=URI.create(v);if(u.getHost()==null)return"Invalid URL format.";return"Domain: "+u.getHost()+"\nHTTPS: "+(https?"YES":"NO")+"\nHeuristic indicators: "+(suspicious?"DETECTED":"NONE")+"\n\n"+(suspicious?"Risk indicators found. Do not enter passwords or payment details.":"No basic phishing indicators found. This is not a malware or phishing guarantee.");}catch(Exception e){return"Invalid URL format.";}
    }
    public static String networkStatus(Context c){
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);if(cm==null)return"Network service unavailable";Network n=cm.getActiveNetwork();if(n==null)return"Offline";NetworkCapabilities x=cm.getNetworkCapabilities(n);if(x==null)return"Network information unavailable";
        String type=x.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":x.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Mobile data":x.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"VPN":"Other";
        boolean valid=x.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),metered=!x.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        return type+" • "+(valid?"Internet validated":"Internet not validated")+" • "+(metered?"Metered":"Unmetered");
    }
}