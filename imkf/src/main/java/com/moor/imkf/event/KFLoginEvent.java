package com.moor.imkf.event;
/**
 * 登录事件
 * @author LongWei
 *
 */
public enum KFLoginEvent {
	NONE,
	/**
	 * 正在登录
	 */
	LOGINING,
	/**
	 * 登录成功
	 */
	LOGIN_SUCCESS,
	/**
	 * 登录失败
	 */
	LOGIN_FAILED,
	/**
	 * 被踢了
	 */
	LOGIN_KICKED,
	/**
	 * 注销了
	 */
	LOGIN_OFF,


	NEW_MSG
}
