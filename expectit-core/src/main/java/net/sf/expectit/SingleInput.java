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

import net.sf.expectit.filter.Filter;
import net.sf.expectit.matcher.Matcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Represents a single output.
 */
class SingleInput {
    public static final int BUFFER_SIZE = 1024;

    private final InputStream input;
    private final Pipe pipe;
    private final StringBuilder buffer;
    private final Charset charset;
    private final OutputStream echoOutput;
    private final Filter filter;
    private Future<Object> copierFuture;

    protected SingleInput(InputStream input, Charset charset,
                          OutputStream echoOutput, Filter filter) throws IOException {
        this.input = input;
        this.charset = charset;
        this.echoOutput = echoOutput;
        this.filter = filter;
        this.pipe = Pipe.open();
        pipe.source().configureBlocking(false);
        buffer = new StringBuilder();
    }

    public void start(ExecutorService executor) {
        copierFuture = executor.submit(new InputStreamCopier(input, pipe.sink()));
    }

    public <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException {
        long timeToStop = System.currentTimeMillis() + timeoutMs;
        long timeElapsed = timeoutMs;
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        Selector selector = Selector.open();
        pipe.source().register(selector, SelectionKey.OP_READ);
        R result = matcher.matches(buffer.toString());
        while (!result.isSuccessful() && timeElapsed > 0) {
            int keys = selector.select(timeElapsed);
            timeElapsed = timeToStop - System.currentTimeMillis();
            if (keys == 0) {
                continue;
            }
            selector.selectedKeys().clear();
            int len = pipe.source().read(byteBuffer);
            if (len == -1) {
                throw new IOException("Input closed");
            }
            String string = new String(byteBuffer.array(), 0, len, charset);
            processString(string);
            byteBuffer.clear();
            result = matcher.matches(buffer.toString());
        }
        if (result.isSuccessful()) {
            buffer.delete(0, result.end());
        }
        return result;
    }

    private void processString(String string) throws IOException {
        if (filter != null) {
            string = filter.filter(string, buffer);
        }
        if (string != null) {
            if (echoOutput != null) {
                echoOutput.write(string.getBytes(charset));
                echoOutput.flush();
            }
            buffer.append(string);
        }
    }

    public void stop() {
        copierFuture.cancel(true);
    }

    StringBuilder getBuffer() {
        return buffer;
    }
}
