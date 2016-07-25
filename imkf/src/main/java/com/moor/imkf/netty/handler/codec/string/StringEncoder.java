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
package com.moor.imkf.netty.handler.codec.string;

import com.moor.imkf.netty.buffer.ChannelBuffer;
import com.moor.imkf.netty.channel.Channel;
import com.moor.imkf.netty.channel.ChannelHandler.Sharable;
import com.moor.imkf.netty.channel.ChannelHandlerContext;
import com.moor.imkf.netty.channel.ChannelPipeline;
import com.moor.imkf.netty.channel.MessageEvent;
import com.moor.imkf.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import com.moor.imkf.netty.handler.codec.frame.Delimiters;
import com.moor.imkf.netty.handler.codec.oneone.OneToOneEncoder;

import java.nio.charset.Charset;

import static com.moor.imkf.netty.buffer.ChannelBuffers.*;

/**
 * Encodes the requested {@link String} into a {@link ChannelBuffer}.
 * A typical setup for a text-based line protocol in a TCP/IP socket would be:
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link DelimiterBasedFrameDecoder}({@link Delimiters#lineDelimiter()}));
 * pipeline.addLast("stringDecoder", new {@link StringDecoder}(CharsetUtil.UTF_8));
 *
 * // Encoder
 * pipeline.addLast("stringEncoder", new {@link StringEncoder}(CharsetUtil.UTF_8));
 * </pre>
 * and then you can use a {@link String} instead of a {@link ChannelBuffer}
 * as a message:
 * <pre>
 * void messageReceived({@link ChannelHandlerContext} ctx, {@link MessageEvent} e) {
 *     String msg = (String) e.getMessage();
 *     ch.write("Did you say '" + msg + "'?\n");
 * }
 * </pre>
 *
 * @apiviz.landmark
 */
@Sharable
public class StringEncoder extends OneToOneEncoder {

    // TODO Use CharsetEncoder instead.
    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public StringEncoder() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public StringEncoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    /**
     * @deprecated Use {@link #StringEncoder(Charset)} instead.
     */
    @Deprecated
    public StringEncoder(String charsetName) {
        this(Charset.forName(charsetName));
    }

    @Override
    protected Object encode(
            ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof String) {
            return copiedBuffer(
                    ctx.getChannel().getConfig().getBufferFactory().getDefaultOrder(), (String) msg, charset);
        }

        return msg;
    }
}
