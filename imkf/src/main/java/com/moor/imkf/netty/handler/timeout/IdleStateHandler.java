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

import static com.moor.imkf.netty.channel.Channels.*;

import java.util.concurrent.TimeUnit;

import com.moor.imkf.netty.channel.Channel;
import com.moor.imkf.netty.channel.ChannelHandler;
import com.moor.imkf.netty.channel.ChannelHandler.Sharable;
import com.moor.imkf.netty.channel.ChannelHandlerContext;
import com.moor.imkf.netty.channel.ChannelPipeline;
import com.moor.imkf.netty.channel.ChannelPipelineFactory;
import com.moor.imkf.netty.channel.ChannelStateEvent;
import com.moor.imkf.netty.channel.Channels;
import com.moor.imkf.netty.channel.LifeCycleAwareChannelHandler;
import com.moor.imkf.netty.channel.MessageEvent;
import com.moor.imkf.netty.channel.SimpleChannelUpstreamHandler;
import com.moor.imkf.netty.channel.WriteCompletionEvent;
import com.moor.imkf.netty.util.ExternalResourceReleasable;
import com.moor.imkf.netty.util.HashedWheelTimer;
import com.moor.imkf.netty.util.Timeout;
import com.moor.imkf.netty.util.Timer;
import com.moor.imkf.netty.util.TimerTask;

@Sharable
public class IdleStateHandler extends SimpleChannelUpstreamHandler
                             implements LifeCycleAwareChannelHandler,
                                        ExternalResourceReleasable {

    final Timer timer;

    final long readerIdleTimeMillis;
    final long writerIdleTimeMillis;
    final long allIdleTimeMillis;

    /**
     * Creates a new instance.
     *
     * @param timer
     *        the {@link Timer} that is used to trigger the scheduled event.
     *        The recommended {@link Timer} implementation is {@link HashedWheelTimer}.
     * @param readerIdleTimeSeconds
     *        an {@link IdleStateEvent} whose state is {@link IdleState#READER_IDLE}
     *        will be triggered when no read was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param writerIdleTimeSeconds
     *        an {@link IdleStateEvent} whose state is {@link IdleState#WRITER_IDLE}
     *        will be triggered when no write was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param allIdleTimeSeconds
     *        an {@link IdleStateEvent} whose state is {@link IdleState#ALL_IDLE}
     *        will be triggered when neither read nor write was performed for
     *        the specified period of time.  Specify {@code 0} to disable.
     */
    public IdleStateHandler(
            Timer timer,
            int readerIdleTimeSeconds,
            int writerIdleTimeSeconds,
            int allIdleTimeSeconds) {

        this(timer,
             readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds,
             TimeUnit.SECONDS);
    }

    /**
     * Creates a new instance.
     *
     * @param timer
     *        the {@link Timer} that is used to trigger the scheduled event.
     *        The recommended {@link Timer} implementation is {@link HashedWheelTimer}.
     * @param readerIdleTime
     *        an {@link IdleStateEvent} whose state is {@link IdleState#READER_IDLE}
     *        will be triggered when no read was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param writerIdleTime
     *        an {@link IdleStateEvent} whose state is {@link IdleState#WRITER_IDLE}
     *        will be triggered when no write was performed for the specified
     *        period of time.  Specify {@code 0} to disable.
     * @param allIdleTime
     *        an {@link IdleStateEvent} whose state is {@link IdleState#ALL_IDLE}
     *        will be triggered when neither read nor write was performed for
     *        the specified period of time.  Specify {@code 0} to disable.
     * @param unit
     *        the {@link TimeUnit} of {@code readerIdleTime},
     *        {@code writeIdleTime}, and {@code allIdleTime}
     */
    public IdleStateHandler(
            Timer timer,
            long readerIdleTime, long writerIdleTime, long allIdleTime,
            TimeUnit unit) {

        if (timer == null) {
            throw new NullPointerException("timer");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        this.timer = timer;
        if (readerIdleTime <= 0) {
            readerIdleTimeMillis = 0;
        } else {
            readerIdleTimeMillis = Math.max(unit.toMillis(readerIdleTime), 1);
        }
        if (writerIdleTime <= 0) {
            writerIdleTimeMillis = 0;
        } else {
            writerIdleTimeMillis = Math.max(unit.toMillis(writerIdleTime), 1);
        }
        if (allIdleTime <= 0) {
            allIdleTimeMillis = 0;
        } else {
            allIdleTimeMillis = Math.max(unit.toMillis(allIdleTime), 1);
        }
    }

    /**
     * Return the readerIdleTime that was given when instance this class in milliseconds.
     *
     */
    public long getReaderIdleTimeInMillis() {
        return readerIdleTimeMillis;
    }

    /**
     * Return the writerIdleTime that was given when instance this class in milliseconds.
     *
     */
    public long getWriterIdleTimeInMillis() {
        return writerIdleTimeMillis;
    }

    /**
     * Return the allIdleTime that was given when instance this class in milliseconds.
     *
     */
    public long getAllIdleTimeInMillis() {
        return allIdleTimeMillis;
    }

    /**
     * Stops the {@link Timer} which was specified in the constructor of this
     * handler.  You should not call this method if the {@link Timer} is in use
     * by other objects.
     */
    public void releaseExternalResources() {
        timer.stop();
    }

    public void beforeAdd(ChannelHandlerContext ctx) throws Exception {
        if (ctx.getPipeline().isAttached()) {
            // channelOpen event has been fired already, which means
            // this.channelOpen() will not be invoked.
            // We have to initialize here instead.
            initialize(ctx);
        } else {
            // channelOpen event has not been fired yet.
            // this.channelOpen() will be invoked and initialization will occur there.
        }
    }

    public void afterAdd(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    public void beforeRemove(ChannelHandlerContext ctx) throws Exception {
        destroy(ctx);
    }

    public void afterRemove(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        // This method will be invoked only if this handler was added
        // before channelOpen event is fired.  If a user adds this handler
        // after the channelOpen event, initialize() will be called by beforeAdd().
        initialize(ctx);
        ctx.sendUpstream(e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        destroy(ctx);
        ctx.sendUpstream(e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        State state = (State) ctx.getAttachment();
        state.lastReadTime = System.currentTimeMillis();
        ctx.sendUpstream(e);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
            throws Exception {
        if (e.getWrittenAmount() > 0) {
            State state = (State) ctx.getAttachment();
            state.lastWriteTime = System.currentTimeMillis();
        }
        ctx.sendUpstream(e);
    }

    private void initialize(ChannelHandlerContext ctx) {
        State state = state(ctx);

        // Avoid the case where destroy() is called before scheduling timeouts.
        // See: https://github.com/netty/netty/issues/143
        synchronized (state) {
            switch (state.state) {
            case 1:
            case 2:
                return;
            }
            state.state = 1;
        }

        state.lastReadTime = state.lastWriteTime = System.currentTimeMillis();
        if (readerIdleTimeMillis > 0) {
            state.readerIdleTimeout = timer.newTimeout(
                    new ReaderIdleTimeoutTask(ctx),
                    readerIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
        if (writerIdleTimeMillis > 0) {
            state.writerIdleTimeout = timer.newTimeout(
                    new WriterIdleTimeoutTask(ctx),
                    writerIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
        if (allIdleTimeMillis > 0) {
            state.allIdleTimeout = timer.newTimeout(
                    new AllIdleTimeoutTask(ctx),
                    allIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
    }

    private static void destroy(ChannelHandlerContext ctx) {
        State state = state(ctx);
        synchronized (state) {
            if (state.state != 1) {
                return;
            }
            state.state = 2;
        }

        if (state.readerIdleTimeout != null) {
            state.readerIdleTimeout.cancel();
            state.readerIdleTimeout = null;
        }
        if (state.writerIdleTimeout != null) {
            state.writerIdleTimeout.cancel();
            state.writerIdleTimeout = null;
        }
        if (state.allIdleTimeout != null) {
            state.allIdleTimeout.cancel();
            state.allIdleTimeout = null;
        }
    }

    private static State state(ChannelHandlerContext ctx) {
        State state;
        synchronized (ctx) {
            // FIXME: It could have been better if there is setAttachmentIfAbsent().
            state = (State) ctx.getAttachment();
            if (state != null) {
                return state;
            }
            state = new State();
            ctx.setAttachment(state);
        }
        return state;
    }

    private void fireChannelIdle(
            final ChannelHandlerContext ctx, final IdleState state, final long lastActivityTimeMillis) {
       ctx.getPipeline().execute(new Runnable() {

            public void run() {
                try {
                    channelIdle(ctx, state, lastActivityTimeMillis);
                } catch (Throwable t) {
                    fireExceptionCaught(ctx, t);
                }
            }
        });
    }

    protected void channelIdle(
            ChannelHandlerContext ctx, IdleState state, long lastActivityTimeMillis) throws Exception {
        ctx.sendUpstream(new DefaultIdleStateEvent(ctx.getChannel(), state, lastActivityTimeMillis));
    }

    private final class ReaderIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled() || !ctx.getChannel().isOpen()) {
                return;
            }

            State state = (State) ctx.getAttachment();
            long currentTime = System.currentTimeMillis();
            long lastReadTime = state.lastReadTime;
            long nextDelay = readerIdleTimeMillis - (currentTime - lastReadTime);
            if (nextDelay <= 0) {
                // Reader is idle - set a new timeout and notify the callback.
                state.readerIdleTimeout =
                    timer.newTimeout(this, readerIdleTimeMillis, TimeUnit.MILLISECONDS);
                fireChannelIdle(ctx, IdleState.READER_IDLE, lastReadTime);
            } else {
                // Read occurred before the timeout - set a new timeout with shorter delay.
                state.readerIdleTimeout =
                    timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    private final class WriterIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled() || !ctx.getChannel().isOpen()) {
                return;
            }

            State state = (State) ctx.getAttachment();
            long currentTime = System.currentTimeMillis();
            long lastWriteTime = state.lastWriteTime;
            long nextDelay = writerIdleTimeMillis - (currentTime - lastWriteTime);
            if (nextDelay <= 0) {
                // Writer is idle - set a new timeout and notify the callback.
                state.writerIdleTimeout =
                    timer.newTimeout(this, writerIdleTimeMillis, TimeUnit.MILLISECONDS);
                fireChannelIdle(ctx, IdleState.WRITER_IDLE, lastWriteTime);
            } else {
                // Write occurred before the timeout - set a new timeout with shorter delay.
                state.writerIdleTimeout =
                    timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    private final class AllIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        AllIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled() || !ctx.getChannel().isOpen()) {
                return;
            }

            State state = (State) ctx.getAttachment();
            long currentTime = System.currentTimeMillis();
            long lastIoTime = Math.max(state.lastReadTime, state.lastWriteTime);
            long nextDelay = allIdleTimeMillis - (currentTime - lastIoTime);
            if (nextDelay <= 0) {
                // Both reader and writer are idle - set a new timeout and
                // notify the callback.
                state.allIdleTimeout =
                    timer.newTimeout(this, allIdleTimeMillis, TimeUnit.MILLISECONDS);
                fireChannelIdle(ctx, IdleState.ALL_IDLE, lastIoTime);
            } else {
                // Either read or write occurred before the timeout - set a new
                // timeout with shorter delay.
                state.allIdleTimeout =
                    timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    private static final class State {
        // 0 - none, 1 - initialized, 2 - destroyed
        int state;

        volatile Timeout readerIdleTimeout;
        volatile long lastReadTime;

        volatile Timeout writerIdleTimeout;
        volatile long lastWriteTime;

        volatile Timeout allIdleTimeout;

        State() {
        }
    }
}
