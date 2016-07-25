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
package com.moor.imkf.netty.handler.codec.oneone;

import com.moor.imkf.netty.buffer.ChannelBuffers;
import com.moor.imkf.netty.channel.Channel;
import com.moor.imkf.netty.channel.ChannelDownstreamHandler;
import com.moor.imkf.netty.channel.ChannelEvent;
import com.moor.imkf.netty.channel.ChannelHandlerContext;
import com.moor.imkf.netty.channel.ChannelPipeline;
import com.moor.imkf.netty.channel.MessageEvent;
import com.moor.imkf.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import com.moor.imkf.netty.handler.codec.frame.Delimiters;

import static com.moor.imkf.netty.channel.Channels.*;

/**
 * Transforms a write request into another write request.  A typical setup for
 * TCP/IP would be:
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link DelimiterBasedFrameDecoder}(80, {@link Delimiters#nulDelimiter()}));
 * pipeline.addLast("customDecoder", new {@link OneToOneDecoder}() { ... });
 *
 * // Encoder
 * pipeline.addLast("customEncoder", new {@link OneToOneEncoder}() { ... });
 * </pre>
 *
 * @apiviz.landmark
 */
public abstract class OneToOneEncoder implements ChannelDownstreamHandler {

    protected OneToOneEncoder() {
    }

    public void handleDownstream(
            ChannelHandlerContext ctx, ChannelEvent evt) throws Exception {
        if (!(evt instanceof MessageEvent)) {
            ctx.sendDownstream(evt);
            return;
        }

        MessageEvent e = (MessageEvent) evt;
        if (!doEncode(ctx, e)) {
            ctx.sendDownstream(e);
        }
    }

    protected boolean doEncode(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object originalMessage = e.getMessage();
        Object encodedMessage = encode(ctx, e.getChannel(), originalMessage);
        if (originalMessage == encodedMessage) {
            return false;
        }
        if (encodedMessage != null) {
            write(ctx, e.getFuture(), encodedMessage, e.getRemoteAddress());
        }
        return true;
    }

    /**
     * Transforms the specified message into another message and return the
     * transformed message.  Note that you can not return {@code null}, unlike
     * you can in {@link OneToOneDecoder#decode(ChannelHandlerContext, Channel, Object)};
     * you must return something, at least {@link ChannelBuffers#EMPTY_BUFFER}.
     */
    protected abstract Object encode(
            ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception;
}
