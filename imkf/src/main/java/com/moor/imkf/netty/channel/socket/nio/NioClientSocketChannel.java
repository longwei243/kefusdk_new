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

import static com.moor.imkf.netty.channel.Channels.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.moor.imkf.netty.channel.ChannelException;
import com.moor.imkf.netty.channel.ChannelFactory;
import com.moor.imkf.netty.channel.ChannelFuture;
import com.moor.imkf.netty.channel.ChannelPipeline;
import com.moor.imkf.netty.channel.ChannelSink;
import com.moor.imkf.netty.util.Timeout;

final class NioClientSocketChannel extends NioSocketChannel {

    private static SocketChannel newSocket() {
        SocketChannel socket;
        try {
            socket = SocketChannel.open();
        } catch (IOException e) {
            throw new ChannelException("Failed to open a socket.", e);
        }

        boolean success = false;
        try {
            socket.configureBlocking(false);
            success = true;
        } catch (IOException e) {
            throw new ChannelException("Failed to enter non-blocking mode.", e);
        } finally {
            if (!success) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return socket;
    }

    volatile ChannelFuture connectFuture;
    volatile boolean boundManually;

    // Does not need to be volatile as it's accessed by only one thread.
    long connectDeadlineNanos;

    volatile Timeout timoutTimer;

    NioClientSocketChannel(
            ChannelFactory factory, ChannelPipeline pipeline,
            ChannelSink sink, NioWorker worker) {

        super(null, factory, pipeline, sink, newSocket(), worker);
        fireChannelOpen(this);
    }
}
