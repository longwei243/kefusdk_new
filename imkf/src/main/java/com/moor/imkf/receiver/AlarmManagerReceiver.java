package com.moor.imkf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moor.imkf.tcpservice.service.IMService;

public class AlarmManagerReceiver extends BroadcastReceiver {
    public AlarmManagerReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent imserviceIntent = new Intent(context, IMService.class);
        context.startService(imserviceIntent);
    }
}
