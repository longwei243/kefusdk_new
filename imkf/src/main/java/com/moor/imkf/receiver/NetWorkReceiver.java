package com.moor.imkf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.moor.imkf.event.KFSocketEvent;
import com.moor.imkf.eventbus.EventBus;
import com.moor.imkf.utils.LogUtil;

/**
 * 网络状态监听器
 * @author LongWei
 *
 */
public class NetWorkReceiver extends BroadcastReceiver{

	private boolean isNetConnected = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		int tempStatus = -1;
		if(info != null && info.isConnected()) {
			//网络连接上了
			isNetConnected = true;

			if(info.getType() == ConnectivityManager.TYPE_WIFI) {
				//wifi
			}else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
				//手机网络
				switch(info.getSubtype()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_IDEN:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					//2g
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					//3g
					break;
				case TelephonyManager.NETWORK_TYPE_LTE:
					//4g
					break;
				
				}
			}

			if(isNetConnected) {
				//重新连接网络后启动断线重连
				LogUtil.d("NetWorkReceiver", "网络重新连接上启动断线重连，发送了启动断线重连的事件");

				EventBus.getDefault().postSticky(KFSocketEvent.NETWORK_OK);
			}
			
		}else {
			//网络断了
			isNetConnected = false;
			EventBus.getDefault().postSticky(KFSocketEvent.NETWORK_DOWN);
		}
	}

}
