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

import java.net.Socket;
import java.util.Map;

import com.moor.imkf.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import com.moor.imkf.netty.channel.ChannelException;
import com.moor.imkf.netty.channel.ReceiveBufferSizePredictor;
import com.moor.imkf.netty.channel.ReceiveBufferSizePredictorFactory;
import com.moor.imkf.netty.channel.socket.DefaultSocketChannelConfig;
import com.moor.imkf.netty.util.internal.ConversionUtil;

/**
 * The default {@link NioSocketChannelConfig} implementation.
 */
class DefaultNioSocketChannelConfig extends DefaultSocketChannelConfig implements NioSocketChannelConfig {

	private static final ReceiveBufferSizePredictorFactory DEFAULT_PREDICTOR_FACTORY = new AdaptiveReceiveBufferSizePredictorFactory();

	private volatile int writeBufferHighWaterMark = 64 * 1024;
	private volatile int writeBufferLowWaterMark = 32 * 1024;
	private volatile ReceiveBufferSizePredictor predictor;
	private volatile ReceiveBufferSizePredictorFactory predictorFactory = DEFAULT_PREDICTOR_FACTORY;
	private volatile int writeSpinCount = 16;

	DefaultNioSocketChannelConfig(Socket socket) {
		super(socket);
	}

	@Override
	public void setOptions(Map<String, Object> options) {
		super.setOptions(options);
		if (getWriteBufferHighWaterMark() < getWriteBufferLowWaterMark()) {
			// Recover the integrity of the configuration with a sensible value.
			setWriteBufferLowWaterMark0(getWriteBufferHighWaterMark() >>> 1);

		}
	}

	@Override
	public boolean setOption(String key, Object value) {
		if (super.setOption(key, value)) {
			return true;
		}

		if ("writeBufferHighWaterMark".equals(key)) {
			setWriteBufferHighWaterMark0(ConversionUtil.toInt(value));
		} else if ("writeBufferLowWaterMark".equals(key)) {
			setWriteBufferLowWaterMark0(ConversionUtil.toInt(value));
		} else if ("writeSpinCount".equals(key)) {
			setWriteSpinCount(ConversionUtil.toInt(value));
		} else if ("receiveBufferSizePredictorFactory".equals(key)) {
			setReceiveBufferSizePredictorFactory((ReceiveBufferSizePredictorFactory) value);
		} else if ("receiveBufferSizePredictor".equals(key)) {
			setReceiveBufferSizePredictor((ReceiveBufferSizePredictor) value);
		} else {
			return false;
		}
		return true;
	}

	public int getWriteBufferHighWaterMark() {
		return writeBufferHighWaterMark;
	}

	public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
		if (writeBufferHighWaterMark < getWriteBufferLowWaterMark()) {
			throw new IllegalArgumentException("writeBufferHighWaterMark cannot be less than " + "writeBufferLowWaterMark (" + getWriteBufferLowWaterMark()
					+ "): " + writeBufferHighWaterMark);
		}
		setWriteBufferHighWaterMark0(writeBufferHighWaterMark);
	}

	private void setWriteBufferHighWaterMark0(int writeBufferHighWaterMark) {
		if (writeBufferHighWaterMark < 0) {
			throw new IllegalArgumentException("writeBufferHighWaterMark: " + writeBufferHighWaterMark);
		}
		this.writeBufferHighWaterMark = writeBufferHighWaterMark;
	}

	public int getWriteBufferLowWaterMark() {
		return writeBufferLowWaterMark;
	}

	public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
		if (writeBufferLowWaterMark > getWriteBufferHighWaterMark()) {
			throw new IllegalArgumentException("writeBufferLowWaterMark cannot be greater than " + "writeBufferHighWaterMark (" + getWriteBufferHighWaterMark()
					+ "): " + writeBufferLowWaterMark);
		}
		setWriteBufferLowWaterMark0(writeBufferLowWaterMark);
	}

	private void setWriteBufferLowWaterMark0(int writeBufferLowWaterMark) {
		if (writeBufferLowWaterMark < 0) {
			throw new IllegalArgumentException("writeBufferLowWaterMark: " + writeBufferLowWaterMark);
		}
		this.writeBufferLowWaterMark = writeBufferLowWaterMark;
	}

	public int getWriteSpinCount() {
		return writeSpinCount;
	}

	public void setWriteSpinCount(int writeSpinCount) {
		if (writeSpinCount <= 0) {
			throw new IllegalArgumentException("writeSpinCount must be a positive integer.");
		}
		this.writeSpinCount = writeSpinCount;
	}

	public ReceiveBufferSizePredictor getReceiveBufferSizePredictor() {
		ReceiveBufferSizePredictor predictor = this.predictor;
		if (predictor == null) {
			try {
				this.predictor = predictor = getReceiveBufferSizePredictorFactory().getPredictor();
			} catch (Exception e) {
				throw new ChannelException("Failed to create a new " + ReceiveBufferSizePredictor.class.getSimpleName() + '.', e);
			}
		}
		return predictor;
	}

	public void setReceiveBufferSizePredictor(ReceiveBufferSizePredictor predictor) {
		if (predictor == null) {
			throw new NullPointerException("predictor");
		}
		this.predictor = predictor;
	}

	public ReceiveBufferSizePredictorFactory getReceiveBufferSizePredictorFactory() {
		return predictorFactory;
	}

	public void setReceiveBufferSizePredictorFactory(ReceiveBufferSizePredictorFactory predictorFactory) {
		if (predictorFactory == null) {
			throw new NullPointerException("predictorFactory");
		}
		this.predictorFactory = predictorFactory;
	}
}
