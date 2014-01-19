package net.sf.expectit;

/*
 * #%L
 * net.sf.expectit
 * %%
 * Copyright (C) 2014 Alexey Gavrilov
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

import net.sf.expectit.echo.EchoOutput;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.matcher.Matcher;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Represents a single inputs.
 */
class SingleInputExpect {
    public static final int BUFFER_SIZE = 1024;

    private final InputStream input;
    private final StringBuilder buffer;
    private final Charset charset;
    private final EchoOutput echoOutput;
    private final Filter filter;
    private Future<Object> copierFuture;
    private final Pipe.SourceChannel source;
    private final Pipe.SinkChannel sink;
    private final int number;

    protected SingleInputExpect(int number, InputStream input, Charset charset,
                                EchoOutput echoOutput, Filter filter) throws IOException {
        this.number = number;
        this.input = input;
        this.charset = charset;
        this.echoOutput = echoOutput;
        this.filter = filter;
        Pipe pipe = Pipe.open();
        source = pipe.source();
        sink = pipe.sink();
        source.configureBlocking(false);
        buffer = new StringBuilder();
    }

    public void start(ExecutorService executor) {
        copierFuture = executor.submit(new InputStreamCopier(input, sink));
    }

    public <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException {
        if (copierFuture == null) {
            throw new IllegalStateException("Not started");
        }
        long timeToStop = System.currentTimeMillis() + timeoutMs;
        long timeElapsed = timeoutMs;
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        Selector selector = Selector.open();
        source.register(selector, SelectionKey.OP_READ);

        R result = matcher.matches(buffer.toString(), copierFuture.isDone());
        while (!result.isSuccessful() && timeElapsed > 0) {
            int keys = selector.select(timeElapsed);
            timeElapsed = timeToStop - System.currentTimeMillis();
            if (keys == 0) {
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
        }

        if (result.isSuccessful()) {
            buffer.delete(0, result.end());
        } else if (copierFuture.isDone() && buffer.length() == 0) {
            throw new EOFException("Input closed");
        }
        return result;
    }

    private void processString(String string) throws IOException {
        if (filter != null) {
            string = filter.beforeAppend(string, buffer);
        }

        if (string != null) {
            if (echoOutput != null) {
                echoOutput.onReceive(number, string);
            }
            buffer.append(string);
            if (filter != null) {
                filter.afterAppend(buffer);
            }
        }
    }

    public void stop() {
        if (copierFuture != null) {
            copierFuture.cancel(true);
        }
    }

    StringBuilder getBuffer() {
        return buffer;
    }
}
