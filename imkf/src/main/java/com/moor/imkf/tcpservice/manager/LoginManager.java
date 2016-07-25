package com.moor.imkf.tcpservice.manager;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.moor.imkf.IMChatManager;
import com.moor.imkf.db.dao.InfoDao;
import com.moor.imkf.tcpservice.tcp.SocketManagerStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 登录管理
 * @author LongWei
 *
 */
public class LoginManager {

	SocketManager socketManager;
	
	private static LoginManager loginManager;
	
	private boolean isKickout = false;
	private boolean isLoginOff = false;
	private boolean isStoreUsernamePasswordRight = true;

	private Context context;


	
	private LoginManager(Context context) {
		this.context = context;
		socketManager = SocketManager.getInstance(context);
	}
	
	public static LoginManager getInstance(Context context) {
		if(loginManager == null) {
			loginManager = new LoginManager(context);
		}
		return loginManager;
	}
	/**
	 * 登录tcp服务器
	 */
	public void login() {
		/**
		 * 连接tcp服务器成功再去登录
		 */
		if(SocketManager.getInstance(IMChatManager.getInstance().getAppContext()).getStatus().equals(SocketManagerStatus.CONNECTED)) {
			String name = InfoDao.getInstance().getLoginName();
			String userId = InfoDao.getInstance().getUserId();
			String accessId = InfoDao.getInstance().getAccessId();

			JSONObject jb = new JSONObject();
			try {
				String username = URLEncoder.encode(name, "utf-8");
				String id = URLEncoder.encode(userId, "utf-8");
				jb.put("Action", "sdkLogin");
				jb.put("UserName", username);
				jb.put("UserId", id);
				jb.put("AccessId", accessId);
				jb.put("Platform", "android");
				jb.put("DeviceId", getDeviceId());
				jb.put("NewVersion", "true");
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String str = "1" + jb.toString() + "\n";

			socketManager.sendData(str);
			SocketManager.getInstance(IMChatManager.getInstance().getAppContext()).setStatus(SocketManagerStatus.WAIT_LOGIN);
			isKickout = false;
			isLoginOff = false;
			isStoreUsernamePasswordRight = false;
		}else {
			SocketManager.getInstance(IMChatManager.getInstance().getAppContext()).login();
		}
	}


	/**
	 * 获取设备id
	 * @return
	 */
	private String getDeviceId() {
		TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String szImei = TelephonyMgr.getDeviceId();
		return szImei;
	}
	/**
	 * 注销
	 */
	public void loginOff() {
		this.setLoginOff(true);
		socketManager.sendData("quit\n");
		socketManager.disconnectServer();
	}
	
	/**
	 * 被踢了
	 */
	public void onKickedOff() {
		isKickout=true;
		socketManager.sendData("quit\n");
        socketManager.onServerDisconn();
	}
	
	public boolean isKickout() {
        return isKickout;
    }

	public boolean isLoginOff() {
		return isLoginOff;
	}
	
	public void setLoginOff(boolean isLoginOff) {
		this.isLoginOff = isLoginOff;
	}

	public boolean isStoreUsernamePasswordRight() {
		return isStoreUsernamePasswordRight;
	}

	public void setIsStoreUsernamePasswordRight(boolean isStoreUsernamePasswordRight) {
		this.isStoreUsernamePasswordRight = isStoreUsernamePasswordRight;
	}
}
