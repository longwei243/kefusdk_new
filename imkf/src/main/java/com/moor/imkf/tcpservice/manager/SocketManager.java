package com.moor.imkf.tcpservice.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.moor.imkf.IMChatManager;
import com.moor.imkf.event.KFSocketEvent;
import com.moor.imkf.eventbus.EventBus;
import com.moor.imkf.requesturl.RequestUrl;
import com.moor.imkf.tcpservice.tcp.ServerMessageHandler;
import com.moor.imkf.tcpservice.tcp.SocketManagerStatus;
import com.moor.imkf.tcpservice.tcp.SocketThread;
import com.moor.imkf.utils.LogUtil;

/**
 * tcp连接的管理类，这里进行真正的连接等操作
 * @author LongWei
 *
 */
public class SocketManager {
	
	private static SocketManager socketManager;

	private Context context;

	private HeartBeatManager heartBeatManager;

	public boolean isReLogin = false;
	
	private SocketManager(Context context) {
		this.context = context;
		heartBeatManager = HeartBeatManager.getInstance(context);
	}

	private SocketManagerStatus Status = SocketManagerStatus.BREAK;

	public synchronized void setStatus(SocketManagerStatus status){
		this.Status = status;
	}

	public SocketManagerStatus getStatus(){
			return this.Status;
	}

	public static synchronized SocketManager getInstance(Context context){
		if(socketManager == null) {
			socketManager = new SocketManager(context);
			EventBus.getDefault().register(socketManager);
		}
		return socketManager;
	}
	
	private SocketThread socketThread;

	/**
	 * 连接tcp服务器
	 */
	public void login() {
		if(isReLogin) {
			return;
		}
		if (socketThread != null) {
			socketThread.close();
			socketThread = null;
        }
		socketThread = new SocketThread(RequestUrl.baseTcpHost, RequestUrl.baseTcpPort, new ServerMessageHandler(context));
		socketThread.start();
	}

	/**
	 * 断开tcp连接
	 */
	public void onServerDisconn(){
		disconnectServer();
    }
	
	 /**
     * 断开与tcp服务器的链接
     */
    public void disconnectServer() {
        if (socketThread != null) {
        	socketThread.close();
        	socketThread = null;
        }
    }

    /**
     * 向服务器发送数据
     * @param data
     */
    public void sendData(String data) {
		if(socketThread != null) {
			try {
				socketThread.sendData(data);
			}catch (Exception e) {

			}
		}

    }
    
    /**判断链接是否处于断开状态*/
    public boolean isSocketConnect(){
        if(socketThread == null || socketThread.isClose()){
            return false;
        }
        return true;
    }

	public void onEventAsync(KFSocketEvent KFSocketEvent){
    	LogUtil.d("IMService", "进入了socket事件驱动的方法中:"+KFSocketEvent.name());
    	switch (KFSocketEvent){
    	case NONE:
    		//什么也没干呢
    		break;
		case NETWORK_OK:
			handlerNetWorkOk();
			break;
    	case MSG_SERVER_DISCONNECTED:
			handlerDisconnected();
    		break;
		case NETWORK_DOWN:
			handlerNetWorkDown();
			break;
    	default:
    		break;
    	}
    }

	private void handlerNetWorkOk(){
		LogUtil.d("IMService", "网络恢复了，tcp开始重连");
		if (Status.equals(SocketManagerStatus.LOGINED)) {
			LogUtil.d("SocketManager","网络恢复了， 登录状态是成功，不用进行重连");
			return;
		}
		login();
		isReLogin = true;
	}

	private void handlerNetWorkDown(){
		setStatus(SocketManagerStatus.BREAK);
		if(socketThread != null) {
			socketThread.setConnecting(false);
			socketThread = null;
			heartBeatManager.reset();
			isReLogin = false;
		}

	}

	private void handlerDisconnected(){
		isReLogin = false;
	 	if( Status.equals(SocketManagerStatus.CONNECTED) ||
			Status.equals(SocketManagerStatus.LOGINED) ||
//			Status.equals(SocketManagerStatus.WAIT_LOGIN) ||
			Status.equals(SocketManagerStatus.CONNECTING)){
			if(!LoginManager.getInstance(IMChatManager.getInstance().getAppContext()).isLoginOff()
				&& !LoginManager.getInstance(IMChatManager.getInstance().getAppContext()).isKickout()) {
				setStatus(SocketManagerStatus.BREAK);
				heartBeatManager.reset();
				/**检测网络状态*/
				ConnectivityManager nw = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netinfo = nw.getActiveNetworkInfo();
				if (netinfo != null && netinfo.isConnected()) {
					LogUtil.d("IMService", "tcp连接被断开了,但是有网，开始重连");
					login();
					isReLogin = true;

				}
			}
		}
	}

	public SocketThread getSocketThread(){
		return socketThread;
	}

}
