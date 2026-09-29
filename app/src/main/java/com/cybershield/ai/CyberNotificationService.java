package com.cybershield.ai;

import android.app.Notification;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class CyberNotificationService extends NotificationListenerService{
    @Override public void onNotificationPosted(StatusBarNotification s){
        if(s==null||s.getNotification()==null)return;
        Notification n=s.getNotification();
        CharSequence a=n.extras==null?null:n.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence b=n.extras==null?null:n.extras.getCharSequence(Notification.EXTRA_TEXT);
        String x=((a==null?"":a.toString())+" "+(b==null?"":b.toString())).toLowerCase();
        boolean risk=x.contains("otp")||x.contains("verify")||x.contains("winner")||x.contains("reward")||x.contains("urgent")||x.contains("password")||x.contains("click here")||x.contains("claim now");
        SharedPreferences p=getSharedPreferences("security",MODE_PRIVATE);
        int total=p.getInt("notification_scanned",0)+1;
        int risks=p.getInt("notification_risks",0)+(risk?1:0);
        p.edit().putInt("notification_scanned",total).putInt("notification_risks",risks).putString("last_notification",((a==null?"":a.toString())+" "+(b==null?"":b.toString()))).apply();
    }
}