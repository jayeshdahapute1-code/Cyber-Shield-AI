package com.cybershield.ai;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CyberCallScreeningService extends CallScreeningService{
    @Override public void onScreenCall(Call.Details d){
        CallResponse r=new CallResponse.Builder().setDisallow(false).setReject(false).setSkipNotification(false).setSkipCallLog(false).build();
        respondToCall(d,r);
    }
}