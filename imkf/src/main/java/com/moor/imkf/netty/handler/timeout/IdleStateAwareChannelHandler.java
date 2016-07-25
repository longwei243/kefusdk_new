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
package com.moor.imkf.netty.handler.timeout;

import com.moor.imkf.netty.channel.Channel;
import com.moor.imkf.netty.channel.ChannelEvent;
import com.moor.imkf.netty.channel.ChannelHandlerContext;
import com.moor.imkf.netty.channel.SimpleChannelHandler;

/**
 * An extended {@link SimpleChannelHandler} that adds the handler method for
 * an {@link IdleStateEvent}.
 * @apiviz.uses com.moor.imkf.netty.handler.timeout.IdleStateEvent
 */
public class IdleStateAwareChannelHandler extends SimpleChannelHandler {

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        if (e instanceof IdleStateEvent) {
            channelIdle(ctx, (IdleStateEvent) e);
        } else {
            super.handleUpstream(ctx, e);
        }
    }

    /**
     * Invoked when a {@link Channel} has been idle for a while.
     */
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        ctx.sendUpstream(e);
    }
}
