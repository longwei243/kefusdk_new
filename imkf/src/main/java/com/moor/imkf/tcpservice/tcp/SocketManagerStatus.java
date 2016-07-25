package com.moor.imkf.tcpservice.tcp;

public enum SocketManagerStatus {

	/**
	 * 断开
	 */
	BREAK,
	/**
	 * 正在连接服务器
	 */
	CONNECTING,
	/**
	 * 连接服务器成功了
	 */
    CONNECTED,
    /**
     * 等待登陆结果
     */
    WAIT_LOGIN,
    /**
     * 已登陆
     */
    LOGINED

}
