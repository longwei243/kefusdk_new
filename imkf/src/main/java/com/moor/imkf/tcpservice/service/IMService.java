package com.moor.imkf.tcpservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.moor.imkf.IMChatManager;
import com.moor.imkf.db.dao.InfoDao;
import com.moor.imkf.db.dao.MessageDao;
import com.moor.imkf.event.KFLoginEvent;
import com.moor.imkf.eventbus.EventBus;
import com.moor.imkf.http.HttpManager;
import com.moor.imkf.http.HttpResponseListener;
import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.model.parser.HttpParser;
import com.moor.imkf.tcpservice.manager.HeartBeatManager;
import com.moor.imkf.tcpservice.manager.LoginManager;
import com.moor.imkf.tcpservice.manager.SocketManager;
import com.moor.imkf.tcpservice.tcp.SocketManagerStatus;
import com.moor.imkf.utils.LogUtil;
import com.moor.imkf.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * im服务,进行tcp的连接的管理
 * @author LongWei
 *
 */
public class IMService extends Service{

	private Context context;

	private SocketManager socketMgr;
	private LoginManager loginMgr;
	private HeartBeatManager heartBeatMgr;


	@Override
	public void onCreate() {
		super.onCreate();

		EventBus.getDefault().register(this);
		LogUtil.d("IMService", "进入了onCreate方法");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();
		//进行管理类的初始化
		socketMgr = SocketManager.getInstance(context);
		loginMgr = LoginManager.getInstance(context);
		heartBeatMgr = HeartBeatManager.getInstance(context);

		if(socketMgr.getStatus().equals(SocketManagerStatus.BREAK) && NetUtils.hasDataConnection(context) && IMChatManager.getInstance().getAppContext() != null){
			socketMgr.login();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Intent intent = new Intent("com.moor.im.IMServiceDown");
		sendBroadcast(intent);

		EventBus.getDefault().unregister(this);
		heartBeatMgr.reset();
		LogUtil.d("IMService", "进入了onDestroy方法， 重置了管理类");

//		Intent imserviceIntent = new Intent(context, IMService.class);
//		context.startService(imserviceIntent);

		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private List<FromToMessage> fromToMessage;
	private String largeMsgId;

	// EventBus 登录事件驱动,接收到事件后调用manager中的方法进行具体处理
	public void onEventMainThread(KFLoginEvent KFLoginEvent){
		LogUtil.d("IMService", "进入了登录事件驱动的方法中，进行相应的处理");
		switch (KFLoginEvent){
			case LOGIN_SUCCESS:
				onLoginSuccess();
				break;
			case LOGIN_FAILED:
				onLoginFailed();
				break;
			case LOGIN_KICKED:
				onLoginKicked();
				break;
			case LOGIN_OFF:
				onLoginOff();
				break;
			case NEW_MSG:
				onNewMessageReceived();
				break;
			default:
				break;
		}
	}

	/**
	 * 注销，停止Service
	 */
	private void onLoginOff() {
		loginMgr.loginOff();
		SocketManager.getInstance(context).setStatus(SocketManagerStatus.BREAK);
		stopSelf();
	}

	/**
	 * 接收到新消息的处理
	 */
	private void onNewMessageReceived() {
		//获取服务器的新消息
		ArrayList<String> array = MessageDao.getInstance()
				.getUnReadDao();
		HttpManager.getMsg(InfoDao.getInstance().getConnectionId(), array,
				new getMsgResponseHandler(context));

	}


	class getMsgResponseHandler implements HttpResponseListener {
		Context context;
		public getMsgResponseHandler(Context context) {
			this.context = context;
		}

		@Override
		public void onFailed() {
		}

		@Override
		public void onSuccess(String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			boolean isLargeMsg = HttpParser.isLargeMsg(responseString);
			// 获取数据成功并且不是大量数据
			if ("true".equals(succeed)) {
				if(isLargeMsg) {
					//有大量的数据
					LogUtil.d("消息接收器", "有大量消息要来了");
					getLargeMsgsFromNet(largeMsgId);
				}else {
					//没有大量的数据
					fromToMessage = HttpParser.getMsgs(responseString);
					// 判断数据是否被读取、及时更新
					MessageDao.getInstance().updateMsgsIdDao();
					// 存入手机数据库
					MessageDao.getInstance().insertGetMsgsToDao(fromToMessage);

				}
				Intent intnet = new Intent(IMChatManager.NEW_MSG_ACTION);
				context.sendBroadcast(intnet);
			}

		}
	}

	/**
	 * 从网络获取大量消息数据
	 */
	public void getLargeMsgsFromNet(String largeMsgId) {
		LogUtil.d("获取大量消息数据：", "largeMsgId是：" + largeMsgId);
		ArrayList largeMsgIdarray = new ArrayList();
		largeMsgIdarray.add(largeMsgId);
		HttpManager.getLargeMsgs(InfoDao.getInstance().getConnectionId(), largeMsgIdarray, new GetLargeMsgsResponseHandler());
	}

	// 取大量消息
	class GetLargeMsgsResponseHandler implements HttpResponseListener {



		@Override
		public void onFailed() {
		}

		@Override
		public void onSuccess(String responseString) {
			String succeed = HttpParser.getSucceed(responseString);
			String message = HttpParser.getMessage(responseString);
			largeMsgId = HttpParser.getLargeMsgId(responseString);
			boolean hasMore = HttpParser.hasMoreMsgs(responseString);
			fromToMessage.clear();
			if("true".equals(succeed)) {
				fromToMessage = HttpParser.getMsgs(responseString);
				LogUtil.d("获取大量数据", "获取到的消息数为："+fromToMessage.size());

				// 判断数据是否被读取、及时更新
				MessageDao.getInstance().updateMsgsIdDao();
				// 存入手机数据库
				MessageDao.getInstance().insertGetMsgsToDao(fromToMessage);

				if(hasMore) {
					//还有更多的消息，继续去取
					getLargeMsgsFromNet(largeMsgId);
				}else {
					//没有了，刷新界面
					LogUtil.d("获取大量消息数据", "没有更多的数据了");
				}
				Intent intnet = new Intent("com.moor.im.NEW_MSG");
				context.sendBroadcast(intnet);
			}
		}
	}

	/**
	 * 被踢了
	 */
	private void onLoginKicked() {
		LogUtil.d("IMService", "被踢了");
		loginMgr.onKickedOff();
		Intent kickedIntent = new Intent();
		kickedIntent.setAction("kicked");
		context.sendBroadcast(kickedIntent);
	}

	/**
	 * 登录失败
	 */
	private void onLoginFailed() {
		LogUtil.d("IMService", "登录失败");
		loginMgr.setIsStoreUsernamePasswordRight(false);
	}

	/**
	 * 登录成功
	 */
	private void onLoginSuccess() {
		LogUtil.d("IMService", "登录成功");
		loginMgr.setIsStoreUsernamePasswordRight(true);
		loginMgr.setLoginOff(false);
		//登录成功了发送心跳
		heartBeatMgr.onloginSuccess();

	}

}
