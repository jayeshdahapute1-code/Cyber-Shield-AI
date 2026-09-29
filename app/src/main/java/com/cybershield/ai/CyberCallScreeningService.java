package com.cybershield.ai;

import android.content.SharedPreferences;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CyberCallScreeningService extends CallScreeningService{
    @Override public void onScreenCall(Call.Details d){
        String number="";
        Uri h=d.getHandle();
        if(h!=null) number=SecurityEngine.normalizeNumber(h.getSchemeSpecificPart());
        SharedPreferences p=getSharedPreferences("security",MODE_PRIVATE);
        String blocked=p.getString("blocked_numbers","");
        boolean shouldBlock=false;
        if(!number.isEmpty()){
            for(String n:blocked.split(",")){
                if(!n.trim().isEmpty()&&number.equals(SecurityEngine.normalizeNumber(n.trim()))){shouldBlock=true;break;}
            }
        }
        if(shouldBlock)p.edit().putInt("blocked_calls",p.getInt("blocked_calls",0)+1).apply();
        CallResponse r=new CallResponse.Builder().setDisallowCall(shouldBlock).setRejectCall(shouldBlock).setSkipNotification(shouldBlock).setSkipCallLog(false).build();
        respondToCall(d,r);
    }
}