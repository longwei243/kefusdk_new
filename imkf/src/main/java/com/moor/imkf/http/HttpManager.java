package com.moor.imkf.http;

import android.os.Handler;
import android.os.Looper;

import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.requesturl.RequestUrl;
import com.moor.imkf.utils.JSONWriter;
import com.moor.imkf.utils.Utils;
import com.moor.imkf.okhttp.Call;
import com.moor.imkf.okhttp.Callback;
import com.moor.imkf.okhttp.FormEncodingBuilder;
import com.moor.imkf.okhttp.OkHttpClient;
import com.moor.imkf.okhttp.Request;
import com.moor.imkf.okhttp.RequestBody;
import com.moor.imkf.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 请求服务器方法类
 * 
 */
public class HttpManager {
	public static final OkHttpClient httpClient = new OkHttpClient();
	private static Handler mDelivery;
	static {
		mDelivery = new Handler(Looper.getMainLooper());
	}

	private static void post(String content, final HttpResponseListener listener) {
		RequestBody formBody = new FormEncodingBuilder()
				.add("data", content)
				.build();
		Request request = new Request.Builder()
				.url(RequestUrl.baseHttp1)
				.post(formBody)
				.build();
		final Call call = httpClient.newCall(request);
		call.enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				if (listener != null) {
					mDelivery.post(new Runnable() {
						@Override
						public void run() {
							listener.onFailed();
						}
					});
				}
			}

			@Override
			public void onResponse(final Response response) throws IOException {
				final String st = response.body().string();
				if (listener != null) {
					mDelivery.post(new Runnable() {
						@Override
						public void run() {
							listener.onSuccess(st);
						}
					});
				}
			}
		});
	}
	/**
	 * 发送新消息到服务器
	 * @param connectionId
	 * @param fromToMessage
	 * @param listener
	 */
	public static void newMsgToServer(String connectionId, FromToMessage fromToMessage, final HttpResponseListener listener) {

		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			if(FromToMessage.MSG_TYPE_TEXT.equals(fromToMessage.msgType)) {
				json.put("ContentType", "text");
			}else if(FromToMessage.MSG_TYPE_IMAGE.equals(fromToMessage.msgType)) {
				json.put("ContentType", "image");
			}else if(FromToMessage.MSG_TYPE_AUDIO.equals(fromToMessage.msgType)) {
				json.put("ContentType", "voice");
			}

			json.put("Message", URLEncoder.encode(fromToMessage.message, "utf-8"));
			json.put("VoiceSecond", fromToMessage.voiceSecond);
			json.put("Action", "sdkNewMsg");
			post(json.toString(), listener);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 取消息
	 * 
	 * @param connectionId
	 * @param array
	 * @param listener
	 */
	public static void getMsg(String connectionId, ArrayList array,
							  final HttpResponseListener listener) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("ReceivedMsgIds", array);
		map.put("Action", "sdkGetMsg");
		JSONWriter jw = new JSONWriter();
		post(jw.write(map), listener);

	}
	/**
	 * 取大量的消息
	 * 
	 * @param connectionId
	 * @param largeMsgIdarray
	 * @param listener
	 */
	public static void getLargeMsgs(String connectionId, ArrayList largeMsgIdarray,
									final HttpResponseListener listener) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ConnectionId", Utils.replaceBlank(connectionId));
		map.put("LargeMsgId", largeMsgIdarray);
		map.put("Action", "getLargeMsg");
		JSONWriter jw = new JSONWriter();
		post(jw.write(map), listener);
	}

	/**
	 * 获取7牛的token
	 * 
	 * @param connectionId
	 * @param fileName
	 * @param listener
	 */
	public static void getQiNiuToken(String connectionId, String fileName,
									 final HttpResponseListener listener) {
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "qiniu.getUptoken");
			json.put("fileName", fileName);
			post(json.toString(), listener);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * 获取评价列表数据
	 */
	public static void getInvestigateList(String connectionId,
										  final HttpResponseListener listener) {
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "sdkGetInvestigate");
			post(json.toString(), listener);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 提交评价数据
	 */
	public static void submitInvestigate(String connectionId, String name, String value,
										 final HttpResponseListener listener) {
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "sdkSubmitInvestigate");
			json.put("Name", name);
			json.put("Value", value);
			post(json.toString(), listener);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 通知服务器开始新会话
	 */
	public static void beginNewChatSession(String connectionId, boolean isNewVisitor, String peerId, final HttpResponseListener listener) {
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "sdkBeginNewChatSession");
			json.put("IsNewVisitor", isNewVisitor);
			if(peerId != null && !"".equals(peerId)) {
				json.put("ToPeer", peerId);
			}
			post(json.toString(), listener);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 提交离线消息
	 */
	public static void submitOfflineMessage(String connectionId, String peerId, String content, String phone, String email, final HttpResponseListener listener) {
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Message", content);
			json.put("Phone", phone);
			json.put("Email", email);
			json.put("Action", "sdkSubmitLeaveMessage");
			if(peerId != null && !"".equals(peerId)) {
				json.put("ToPeer", peerId);
			}
			post(json.toString(), listener);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 转人工客服
	 */
	public static void convertManual(String connectionId,
									 final HttpResponseListener listener) {
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "sdkConvertManual");
			post(json.toString(), listener);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取技能组
	 */
	public static void getPeers(String connectionId,
								final HttpResponseListener listener) {
		JSONObject json = new JSONObject();
		try {
			json.put("ConnectionId", Utils.replaceBlank(connectionId));
			json.put("Action", "sdkGetPeers");
			post(json.toString(), listener);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
