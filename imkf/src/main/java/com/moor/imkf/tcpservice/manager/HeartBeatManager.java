package com.moor.imkf.tcpservice.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

/**
 * 心跳管理器
 * @author LongWei
 *
 */
public class HeartBeatManager {
	
	private static HeartBeatManager heartBeatManager;

	/**
	 * 心跳间隔
	 */
	public static int heartInterval = 4 * 60 * 1000;
    
	public void setHeartInterval(int heartInterval) {
		this.heartInterval = heartInterval;
	}

	private final String ACTION_SENDING_HEARTBEAT = "com.moor.im.manager.heartbeatmanager";
    private PendingIntent pendingIntent;
	
    private Context context;
    
    private HeartBeatManager(Context context) {
		this.context = context;
	}
    
    public static HeartBeatManager getInstance(Context context) {
    	if(heartBeatManager == null) {
    		heartBeatManager = new HeartBeatManager(context);
    	}
    	return heartBeatManager;
    }
    
    // 登陆成功之后供service中调用,来启动心跳
    public void onloginSuccess(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        context.registerReceiver(imReceiver, intentFilter);
        //获取AlarmManager系统服务
        scheduleHeartbeat(heartInterval);
    }
    
    /**
     * 重置心跳
     */
    public void reset() {
        try {
        	context.unregisterReceiver(imReceiver);
            cancelHeartbeatTimer();
        }catch (Exception e){
        }
    }
    
    // ServerHandler tcp连接断了直接调用，重置心跳
    public void onServerDisconn(){
    	reset();
    }
    
    /**
     * 取消心跳
     */
    private void cancelHeartbeatTimer() {
        if (pendingIntent == null) {
            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }


    private void scheduleHeartbeat(int seconds){
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_SENDING_HEARTBEAT);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            if (pendingIntent == null) {
                return;
            }
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds, seconds, pendingIntent);
    }
    
    private BroadcastReceiver imReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_SENDING_HEARTBEAT)) {
                sendHeartBeatPacket();
            }
        }
    };
    /**
     * 向服务器发送心跳包
     */
    public void sendHeartBeatPacket(){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "7moor_heartBeat_wakelock");
        wl.acquire();
        try {
            SocketManager.getInstance(context).sendData("3\n");
        } finally {
            wl.release();
        }
    }

}
