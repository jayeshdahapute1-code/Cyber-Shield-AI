package com.cybershield.ai;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

public final class CyberFeatures {
    private CyberFeatures(){}

    public static String notificationStatus(Context c){
        boolean enabled=false;
        if(Build.VERSION.SDK_INT>=27){
            android.app.NotificationManager nm=(android.app.NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
            enabled=nm!=null && nm.isNotificationListenerAccessGranted(
                new ComponentName(c,CyberNotificationService.class));
        }
        android.content.SharedPreferences p=c.getSharedPreferences("security",Context.MODE_PRIVATE);
        return "Access: "+(enabled?"ENABLED":"NOT ENABLED")+
            "\nNotifications analyzed: "+p.getInt("notification_scanned",0)+
            "\nRisk indicators: "+p.getInt("notification_risks",0);
    }

    public static void openNotificationSettings(Activity a){
        try{
            a.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }catch(Exception e){
            a.startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    public static void callStatus(Context c, TextView out){
        if(Build.VERSION.SDK_INT>=29){
            RoleManager r=(RoleManager)c.getSystemService(Context.ROLE_SERVICE);
            out.setText("Call screening role: "+
                (r!=null && r.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)?"ACTIVE":"NOT ACTIVE"));
        }else{
            out.setText("Call screening requires Android 10+.");
        }
    }

    public static void callRole(Activity a,int requestCode){
        if(Build.VERSION.SDK_INT>=29){
            RoleManager r=(RoleManager)a.getSystemService(Context.ROLE_SERVICE);
            if(r!=null && r.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)){
                a.startActivityForResult(
                    r.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),requestCode);
            }else{
                Toast.makeText(a,"Call screening is not available.",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(a,"Call screening requires Android 10+.",Toast.LENGTH_LONG).show();
        }
    }

    public static String variantName(){
        return "Full Protection";
    }
}
