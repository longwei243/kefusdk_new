package com.moor.imkf.model.entity;

import com.moor.imkf.ormlite.dao.ForeignCollection;
import com.moor.imkf.ormlite.field.DatabaseField;
import com.moor.imkf.ormlite.field.ForeignCollectionField;
import com.moor.imkf.ormlite.table.DatabaseTable;

/**
 * 接收消息的实体类
 * 
 * @author Mr.li
 * 
 */
@DatabaseTable(tableName = "fromToMessage")
public class FromToMessage {
	/**
	 * 消息类型：文本
	 */
	public static final String MSG_TYPE_TEXT = "0";
	/**
	 * 消息类型：图片
	 */
	public static final String MSG_TYPE_IMAGE = "1";
	/**
	 * 消息类型：语音
	 */
	public static final String MSG_TYPE_AUDIO = "2";
	/**
	 * 消息类型：评价
	 */
	public static final String MSG_TYPE_INVESTIGATE = "3";


//	@DatabaseField(generatedId = true)
//	public int id;
	/**
	 * 消息的id
	 */
	@DatabaseField(id = true, unique = true)
	public String _id;
	/**
	 * 消息从哪里来的
	 */
	@DatabaseField
	public String from;
	/**
	 * 消息要通知谁
	 */
	@DatabaseField
	public String tonotify;
	/**
	 * 用来标示对话的两人
	 */
	@DatabaseField
	public String sessionId;
	/**
	 * 消息的类型
	 */
	@DatabaseField
	public String msgType;
	/**
	 * 什么时候发的
	 */
	@DatabaseField
	public Long when;
	/**
	 * 消息文本内容，若是多媒体消息时为URL
	 */
	@DatabaseField
	public String message;
	/**
	 * 设备信息
	 */
	@DatabaseField
	public String deviceInfo;
	/**
	 * 未读标记（0为已读，1为未读）
	 */
	@DatabaseField
	public String unread;
	/**
	 * 发送成功的状态(true说明成功，false说明失败，sending说明正在发送中)
	 */
	@DatabaseField
	public String sendState;
	/**
	 * 多媒体消息本地的文件路径
	 */
	@DatabaseField
	public String filePath;
	/**
	 * 录音的时间
	 */
	@DatabaseField
	public Float recordTime;
	@DatabaseField
	public String voiceSecond;
	/**
	 * 是发送者还是接收者，发送者为0，接收者为1
	 */
	@DatabaseField
	public String userType;

	@DatabaseField
	public Boolean showHtml;

	/**
	 * 是个人消息还是群组消息类型有：User, Group, Discussion
	 */
	@DatabaseField
	public String type;

	@ForeignCollectionField(eager = true)
	public ForeignCollection<MsgInves> investigates;

	public FromToMessage() {

	}


}
