package com.cybershield.ai;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

public final class CyberFeatures {
    private CyberFeatures(){}

    public static String notificationStatus(Context c){
        return "Status: not included in Safe Install build.\n\nThe safe build intentionally does not register a notification listener.";
    }

    public static void openNotificationSettings(Activity a){
        Toast.makeText(a,"Message Guard is available in the Full Protection build.",Toast.LENGTH_LONG).show();
    }

    public static void callStatus(Context c, TextView out){
        out.setText("Call screening: available in Full Protection build.");
    }

    public static void callRole(Activity a,int requestCode){
        Toast.makeText(a,"Call Guard is available in the Full Protection build.",Toast.LENGTH_LONG).show();
    }

    public static String variantName(){
        return "Safe Install";
    }
}
