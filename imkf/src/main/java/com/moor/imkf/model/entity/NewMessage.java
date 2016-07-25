package com.moor.imkf.model.entity;

import com.moor.imkf.ormlite.field.DatabaseField;
import com.moor.imkf.ormlite.table.DatabaseTable;

/**
 * 每个人最新消息的实体类
 * 
 * @author Mr.li
 * 
 */
@DatabaseTable(tableName = "newMessage")
public class NewMessage {
	public NewMessage() {

	}

	// 主键 id 自增长
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField
	public String _id;
	@DatabaseField
	public String sessionId;
	@DatabaseField
	public String from;
	@DatabaseField
	public String message;
	@DatabaseField
	public Long time;
	@DatabaseField
	public String fromName;
	@DatabaseField
	public String img;
	@DatabaseField
	public String msgType;
	@DatabaseField
	public String type;
	@DatabaseField
	public int unReadCount;

}
