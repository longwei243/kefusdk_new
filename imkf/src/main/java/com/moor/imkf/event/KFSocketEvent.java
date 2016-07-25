package com.moor.imkf.event;

public enum KFSocketEvent {

	/**
	 * 什么都没干
	 */
	NONE,
	/**
	 * 正在连接服务器
	 */
	CONNECTING_MSG_SERVER,
	/**
	 * 连接服务器成功了
	 */
    CONNECT_MSG_SERVER_SUCCESS,
    /**
     * 连接服务器失败了
     */
    CONNECT_MSG_SERVER_FAILED,
    /**
     * 连接成功之后被断掉了
     */
    MSG_SERVER_DISCONNECTED,
    /**
     * 有网络链接了
     */
    NETWORK_OK,
    /**
     * 没网了
     */
    NETWORK_DOWN
}
