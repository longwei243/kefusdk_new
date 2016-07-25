package com.moor.imkf.qiniu.http;

import com.moor.imkf.qiniu.utils.AsyncRun;
import com.moor.imkf.okhttp.MediaType;
import com.moor.imkf.okhttp.RequestBody;

import java.io.IOException;

import com.moor.imkf.okio.Buffer;
import com.moor.imkf.okio.BufferedSink;
import com.moor.imkf.okio.ForwardingSink;
import com.moor.imkf.okio.Okio;
import com.moor.imkf.okio.Sink;

/**
 * Created by bailong on 16/1/8.
 */
public class CountingRequestBody extends RequestBody {

    private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

    private final RequestBody body;
    private final ProgressHandler progress;
    private final CancellationHandler cancellationHandler;

    public CountingRequestBody(RequestBody body, ProgressHandler progress,
                               CancellationHandler cancellationHandler) {
        this.body = body;
        this.progress = progress;
        this.cancellationHandler = cancellationHandler;
    }

    @Override
    public long contentLength() throws IOException {
        return body.contentLength();
    }

    @Override
    public MediaType contentType() {
        return body.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink;

        CountingSink countingSink = new CountingSink(sink);
        bufferedSink = Okio.buffer(countingSink);

        body.writeTo(bufferedSink);

        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {

        private int bytesWritten = 0;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (cancellationHandler == null && progress == null) {
                super.write(source, byteCount);
                return;
            }
            if (cancellationHandler != null && cancellationHandler.isCancelled()) {
                throw new CancellationHandler.CancellationException();
            }
            super.write(source, byteCount);
            bytesWritten += byteCount;
            if (progress != null) {
                AsyncRun.run(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progress.onProgress(bytesWritten, (int) contentLength());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
