package com.moor.imkf.model.parser;

import com.moor.imkf.gson.Gson;
import com.moor.imkf.gson.reflect.TypeToken;
import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.model.entity.Investigate;
import com.moor.imkf.model.entity.Peer;
import com.moor.imkf.utils.LogUtil;
import com.moor.imkf.utils.NullUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 解析类
 *
 */
public class HttpParser {

	/**
	 * 获取返回成功状态
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getSucceed(String responseString) {
		String succeed = "";
		try {
			JSONObject o = new JSONObject(responseString);
			succeed = o.getString("Succeed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return succeed;
	}


	public static String getLeaveMessage(String responseString) {
			String succeed = "";
			try {
				JSONObject o = new JSONObject(responseString);
				succeed = o.getString("LeaveMessage");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return succeed;
	}

	public static String getRobotEnable(String responseString) {
		String succeed = "";
		try {
			JSONObject o = new JSONObject(responseString);
			succeed = o.getString("RobotEnable");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return succeed;
	}

	/**
	 * 获取返回消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getMessage(String responseString) {
		String message = "";
		try {
			JSONObject o = new JSONObject(responseString);
			message = o.getString("Message");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}

	/**
	 * 获取服务器返回的发送消息成功的时间
	 * @param responseString
	 * @return
	 */
	public static long getWhen(String responseString) {
		long message = 0L;
		try {
			JSONObject o = new JSONObject(responseString);
			message = o.getLong("when");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}
	/**
	 * 获取大量消息的id
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getLargeMsgId(String responseString) {
		String largeMsgId = "";
		try {
			JSONObject o = new JSONObject(responseString);
			largeMsgId = o.getString("LargeMsgId");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return largeMsgId;
	}


	public static List<Investigate> getInvestigates(String responseString) {
		List<Investigate> investigates = new ArrayList<Investigate>();

		try {
			JSONObject o = new JSONObject(responseString);
			JSONArray o1 = o.getJSONArray("List");

			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			investigates = gson.fromJson(o1.toString(),
					new TypeToken<List<Investigate>>() {
					}.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return investigates;
	}

	/**
	 * 取消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static List<FromToMessage> getMsgs(String responseString) {
		List<FromToMessage> newMessage = new ArrayList<FromToMessage>();
		LogUtil.i("取消息的返回数据:", responseString);
		try {
			JSONObject o = new JSONObject(responseString);
			JSONArray o1 = o.getJSONArray("data");
			for (int i=0; i<o1.length(); i++) {
				FromToMessage message = new FromToMessage();

				JSONObject jb = o1.getJSONObject(i);
				String _id = jb.getString("_id");
				String sid = jb.getString("sid");
				Long when = jb.getLong("when");
				String content = jb.getString("content");
				String msgType = "0";
				message.message = content;
				if(jb.getString("contentType") != null) {
					String type = jb.getString("contentType");
					if("text".equals(type)) {
						msgType = "0";
					}else if("image".equals(type)) {
						msgType = "1";
					}else if("file".equals(type)) {
						msgType = "4";
						String[] str = content.split("\\?fileName=");
						if(str.length == 2) {
							message.message = str[0];
							String nameAndSizeStr = str[1];
							String[] ns = nameAndSizeStr.split("\\?fileSize=");
							if(ns.length == 2) {
								message.fileName = ns[0];
								message.fileSize = ns[1];
								message.fileProgress = 0;
								message.fileDownLoadStatus = "failed";
							}
						}
					}else if("iframe".equals(type)) {
						msgType = "5";

						int width = 0;
						int height = 0;

						try{
							String iframeWidth = jb.getString("iframeWidth");
							String iframeHeight = jb.getString("iframeHeight");

							width = Integer.parseInt(iframeWidth);
							height = Integer.parseInt(iframeHeight);
						}catch (Exception e){}

						message.iframeWidth = width;
						message.iframeHeight = height;
					}
				}
				boolean showHtml = jb.getBoolean("showHtml");

				String exten = jb.getString("exten");
				String displayName = jb.getString("displayName");
				String im_icon = jb.getString("im_icon");

				message._id = NullUtil.checkNull(_id);
				message.sessionId = NullUtil.checkNull(sid);
				message.when = NullUtil.checkNull(when);
				message.msgType = NullUtil.checkNull(msgType);
				message.showHtml = showHtml;

				message.exten = NullUtil.checkNull(exten);
				message.displayName = NullUtil.checkNull(displayName);
				message.im_icon = NullUtil.checkNull(im_icon);

				newMessage.add(message);

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newMessage;
	}
	/**
	 * 是否有大量的消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static boolean isLargeMsg(String responseString) {
		try {
			JSONObject o = new JSONObject(responseString);
			boolean isLargeMsg = o.getBoolean("HasLargeMsgs");
			if(isLargeMsg) {
				return true;
			}
			
		} catch (JSONException e) {
			return false;
		}
		
		return false;
	}
	/**
	 * 是否还有大量的消息
	 * 
	 * @param responseString
	 * @return
	 */
	public static boolean hasMoreMsgs(String responseString) {
		try {
			JSONObject o = new JSONObject(responseString);
			boolean isHasMore = o.getBoolean("HasMore");
			if(isHasMore) {
				return true;
			}
			
		} catch (JSONException e) {
			return false;
		}
		
		return false;
	}

	/**
	 * 获取uptoken
	 * 
	 * @param responseString
	 * @return
	 */
	public static String getUpToken(String responseString) {
		String s = "";
		try {
			JSONObject o = new JSONObject(responseString);
			s = o.getString("uptoken");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;

	}

	public static List<Peer> getPeers(String responseString) {
		List<Peer> peers = new ArrayList<Peer>();

		try {
			JSONObject o = new JSONObject(responseString);
			JSONArray o1 = o.getJSONArray("Peers");

			Gson gson = new Gson();
			// TypeToken<Json>--他的参数是根节点【】或{}-集合或对象
			peers = gson.fromJson(o1.toString(),
					new TypeToken<List<Peer>>() {
					}.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return peers;
	}

}
