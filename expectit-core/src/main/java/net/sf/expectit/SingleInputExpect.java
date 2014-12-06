package net.sf.expectit;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 Alexey Gavrilov and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static net.sf.expectit.Utils.toDebugString;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.matcher.Matcher;

/**
 * Represents a single inputs.
 */
class SingleInputExpect {
    private static final Logger LOG = Logger.getLogger(SingleInputExpect.class.getName());

    public static final int BUFFER_SIZE = 1024;

    private final InputStream input;
    private final StringBuilder buffer;
    private final Charset charset;
    private final Appendable echoInput;
    private final Filter filter;
    private Future<Object> copierFuture;
    private final Pipe.SourceChannel source;
    private final Pipe.SinkChannel sink;
    private final int bufferSize;

    protected SingleInputExpect(
            final InputStream input,
            final Charset charset,
            final Appendable echoInput,
            final Filter filter,
            final int bufferSize) throws IOException {
        this.input = input;
        this.charset = charset;
        this.echoInput = echoInput;
        this.filter = filter;
        this.bufferSize = bufferSize;
        Pipe pipe = Pipe.open();
        source = pipe.source();
        sink = pipe.sink();
        source.configureBlocking(false);
        buffer = new StringBuilder();
    }

    public void start(ExecutorService executor) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(
                    String.format(
                            "Starting expect thread: input=%s, charset=%s, echoInput=%s, "
                                    + "filter=%s, bufferSize=%d",
                            input,
                            charset,
                            echoInput,
                            filter,
                            bufferSize
                    )
            );
        }
        copierFuture = executor.submit(
                new InputStreamCopier(
                        sink,
                        input,
                        bufferSize,
                        echoInput,
                        charset));
    }

    public <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException {
        if (copierFuture == null) {
            throw new IllegalStateException("Not started");
        }

        final long timeToStop = System.currentTimeMillis() + timeoutMs;
        final boolean isInfiniteTimeout = timeoutMs == ExpectImpl.INFINITE_TIMEOUT;
        long timeElapsed = timeoutMs;
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        Selector selector = Selector.open();

        try {
            source.register(selector, SelectionKey.OP_READ);
            R result = matcher.matches(buffer.toString(), copierFuture.isDone());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(
                        String.format(
                                "Initial matcher %s result: %s",
                                toDebugString(matcher),
                                toDebugString(result)));
            }
            while (!(result.isSuccessful() || result.canStopMatching())
                    && (isInfiniteTimeout || timeElapsed > 0)) {
                int keys = isInfiniteTimeout ? selector.select() : selector.select(timeElapsed);
                // if thread was interrupted the selector returns immediately
                // and keep the thread status, so we need to check it
                if (Thread.currentThread().isInterrupted()) {
                    LOG.fine("Thread was interrupted");
                    throw new ClosedByInterruptException();
                }

                if (!isInfiniteTimeout) {
                    timeElapsed = timeToStop - System.currentTimeMillis();
                }

                if (keys == 0) {
                    LOG.fine("Selector returns 0 key");
                    continue;
                }

                selector.selectedKeys().clear();
                int len = source.read(byteBuffer);

                if (len > 0) {
                    String string = new String(byteBuffer.array(), 0, len, charset);
                    processString(string);
                    byteBuffer.clear();
                }

                result = matcher.matches(buffer.toString(), len == -1);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(
                            String.format(
                                    "Matcher %s result: %s. Operation time: %d ms",
                                    toDebugString(matcher),
                                    toDebugString(result),
                                    timeoutMs - timeElapsed));
                }
            }

            if (result.isSuccessful()) {
                buffer.delete(0, result.end());
            } else if (copierFuture.isDone() && buffer.length() == 0) {
                throw new EOFException("Input closed");
            }
            return result;
        } finally {
            selector.close();
        }
    }

    private void processString(String string) throws IOException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Processing string: " + toDebugString(string));
        }
        if (filter != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Before append filter: " + toDebugString(filter));
            }
            string = filter.beforeAppend(string, buffer);
        }

        if (string != null) {
            buffer.append(string);
            if (filter != null) {
                LOG.fine("After append filter: " + toDebugString(filter));
                filter.afterAppend(buffer);
            }
        }
    }

    public void stop() throws IOException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Releasing resources for input: " + this.input);
        }
        if (copierFuture != null) {
            copierFuture.cancel(true);
        }
        sink.close();
        source.close();
    }

    StringBuilder getBuffer() {
        return buffer;
    }
}
