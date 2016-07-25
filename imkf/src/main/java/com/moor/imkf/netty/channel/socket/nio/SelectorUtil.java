/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.moor.imkf.netty.channel.socket.nio;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Selector;
import java.util.concurrent.TimeUnit;

import com.moor.imkf.netty.util.internal.SystemPropertyUtil;

final class SelectorUtil {

	static final int DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	static final long DEFAULT_SELECT_TIMEOUT = 500;
	static final long SELECT_TIMEOUT = SystemPropertyUtil.getLong("com.moor.imkf.netty.selectTimeout", DEFAULT_SELECT_TIMEOUT);
	static final long SELECT_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(SELECT_TIMEOUT);
	static final boolean EPOLL_BUG_WORKAROUND = SystemPropertyUtil.getBoolean("com.moor.imkf.netty.epollBugWorkaround", false);

	// Workaround for JDK NIO bug.
	//
	// See:
	// - http://bugs.sun.com/view_bug.do?bug_id=6427854
	// - https://github.com/netty/netty/issues/203
	static {
		String key = "sun.nio.ch.bugLevel";
		try {
			String buglevel = System.getProperty(key);
			if (buglevel == null) {
				System.setProperty(key, "");
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	static int select(Selector selector) throws IOException {
		try {
			return selector.select(SELECT_TIMEOUT);
		} catch (CancelledKeyException e) {
			// if (logger.isDebugEnabled()) {
			// logger.debug(
			// CancelledKeyException.class.getSimpleName() +
			// " raised by a Selector - JDK bug?", e);
			// }
			e.printStackTrace();
			// Harmless exception - log anyway
		}
		return -1;
	}

	private SelectorUtil() {
		// Unused
	}
}
