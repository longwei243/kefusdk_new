package com.moor.imkf.db.dao;

import com.moor.imkf.ormlite.dao.Dao;
import com.moor.imkf.IMChat;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.db.DataBaseHelper;
import com.moor.imkf.model.entity.FromToMessage;
import com.moor.imkf.utils.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 所有消息的dao方法
 *
 */
public class MessageDao {
	private Dao<FromToMessage, Integer> fromToMessageDao = null;
	private DataBaseHelper helper = DataBaseHelper.getHelper(IMChatManager.getInstance().getAppContext());
	private static MessageDao instance;

	private MessageDao() {
		try {
			fromToMessageDao = helper.getFromMessageDao();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static MessageDao getInstance() {
		if (instance == null) {
			instance = new MessageDao();
		}
		return instance;
	}

	/**
	 * 删除所有消息
	 */
	public void deleteAllMsgs() {
		try {
			fromToMessageDao.delete(fromToMessageDao.queryForAll());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteMsg(FromToMessage msg) {
		try {
			fromToMessageDao.delete(msg);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取已经获取到消息的id
	 **/
	public ArrayList<String> getUnReadDao() {
		List<FromToMessage> list = null;
		ArrayList<String> array = new ArrayList<String>();
		try {
			list = fromToMessageDao.queryBuilder().where().eq("unread", "1")
					.query();
			for (int i = 0; i < list.size(); i++) {
				array.add(list.get(i)._id);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}

	/**
	 * 把上一次取的消息标为已取 1未读，0已读
	 **/
	public void updateMsgsIdDao() {
		List<FromToMessage> msgs = new ArrayList<FromToMessage>();
		try {
			msgs = fromToMessageDao.queryBuilder().where().eq("unread", "1")
					.query();
			for (int i = 0; i < msgs.size(); i++) {
				msgs.get(i).unread = "0";
				fromToMessageDao.update(msgs.get(i));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 插入服务器获取的消息
	 *
	 */
	public void insertGetMsgsToDao(List<FromToMessage> fromToMessage) {

		for (int i = 0; i < fromToMessage.size(); i++) {
			try {
				fromToMessage.get(i).unread = "1";
				fromToMessage.get(i).userType = "1";
				fromToMessage.get(i).sendState = "true";
				fromToMessageDao.create(fromToMessage.get(i));

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 把发送的消息存到数据库中
	 * @param fromToMessage
	 */
	public void insertSendMsgsToDao(FromToMessage fromToMessage) {
		
//		SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
//		String time = sDateFormat.format(new java.util.Date());
//		SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMddhhmmss");
//		String date = sFormat.format(new java.util.Date());
//
//		Random random = new Random();
//		int x = random.nextInt(899999);
//		int y = x + 100000;
//		String s = date + y;
		//id是自己来生成的吗？ 是的
		fromToMessage._id = UUID.randomUUID().toString();
		try {
			fromToMessageDao.create(fromToMessage);
			LogUtil.d("MessageDao", "新消息插入到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新数据库中的消息
	 * @param message
	 */

	public void updateMsgToDao(FromToMessage message) {
		//先将该消息从数据库中查出来
		try {
			FromToMessage msg = fromToMessageDao.queryBuilder().where().eq("_id", message._id).query().get(0);
			//更新该数据
			msg = message;
			fromToMessageDao.update(msg);
			LogUtil.i("fromToMessageDao", "消息更新到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 便捷方法，更新数据库，该消息发送成功了
	 * @param message
	 */
	public void updateSucceedMsgToDao(FromToMessage message, long when) {
		//先将该消息从数据库中查出来
		try {
			FromToMessage msg = fromToMessageDao.queryBuilder().where().eq("_id", message._id).query().get(0);
			msg.sendState = "true";
			msg.when = when;
			//更新该数据
			fromToMessageDao.update(msg);
			LogUtil.i("fromToMessageDao", "消息发送成功更新到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 便捷方法，更新数据库，该消息发送失败
	 * @param message
	 */
	public void updateFailedMsgToDao(FromToMessage message) {
		//先将该消息从数据库中查出来
		try {
			FromToMessage msg = fromToMessageDao.queryBuilder().where().eq("_id", message._id).query().get(0);
			msg.sendState = "false";
			//更新该数据
			fromToMessageDao.update(msg);
			LogUtil.i("fromToMessageDao", "消息发送失败更新到了数据库中");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 从数据库中获取消息，供聊天界面中使用
	 * @param i 第几页数据
	 * @return
	 */
	public List<FromToMessage> getMessages(int i) {
		List<FromToMessage> fromToMessage = new ArrayList<FromToMessage>();
		try {
			fromToMessage = fromToMessageDao.queryBuilder()
					.orderBy("when", false).limit(i * 15).where().eq("sessionId", IMChat.getInstance().getSessionId())
					.query();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fromToMessage;
	}

	/**
	 * 查最新一条的消息
	 * 
	 * @param _id
	 * @return
	 */
	public List<FromToMessage> getFirstMessage(String _id) {
		List<FromToMessage> fromToMessage = new ArrayList<FromToMessage>();
		try {
			fromToMessage = fromToMessageDao.queryBuilder()
					.orderBy("id", false).limit(1).where().eq("from", _id)
					.query();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fromToMessage;
	}

	/**
	 * 是否查到最底端
	 * 
	 * @param i
	 * @return
	 */
	public Boolean isReachEndMessage(int i) {
		List<FromToMessage> fromToMessage = new ArrayList<FromToMessage>();
		Boolean flag = false;
		try {
			fromToMessage = fromToMessageDao.queryBuilder().where()
					.eq("sessionId", IMChat.getInstance().getSessionId()).query();
			if (i >= fromToMessage.size()) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 获取所以消息的Id
	 * @return
	 */
	public ArrayList<String> getAllMsgId() {
		List<FromToMessage> list = null;
		ArrayList<String> array = new ArrayList<String>();
		try {
			list = fromToMessageDao.queryForAll();
			for (int i = 0; i < list.size(); i++) {
				array.add(list.get(i)._id);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}
}
