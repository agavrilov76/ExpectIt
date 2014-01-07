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

import net.sf.expectit.matcher.Matcher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.sf.expectit.matcher.Matchers.allOf;

/**
 * An implementation of the Expect interface which delegates actual work to SingleInputExpect objects.
 */
class ExpectImpl implements Expect {
    private final long timeout;
    private final OutputStream output;
    private final SingleInputExpect[] inputs;
    private final Charset charset;
    private final Appendable echoOutput;
    private final boolean errorOnTimeout;
    private final ExecutorService executor;
    private final String lineSeparator;

    ExpectImpl(long timeout, OutputStream output, SingleInputExpect[] inputs,
               Charset charset, Appendable echoOutput, boolean errorOnTimeout, String lineSeparator) {
        this.timeout = timeout;
        this.output = output;
        this.inputs = inputs;
        this.charset = charset;
        this.echoOutput = echoOutput;
        this.errorOnTimeout = errorOnTimeout;
        this.lineSeparator = lineSeparator;
        executor = Executors.newFixedThreadPool(inputs.length, new NamedExecutorThreadFactory("expect-"));
    }

    void start() {
        for (SingleInputExpect input : inputs) {
            input.start(executor);
        }
    }

    @Override
    public <R extends Result> R expectIn(int input, long timeoutMs, Matcher<R> matcher) throws IOException {
        R result = inputs[input].expect(timeoutMs, matcher);
        if (errorOnTimeout && !result.isSuccessful()) {
            throw new AssertionError("Expect timeout (" + timeoutMs + " ms) for matcher: " + matcher);
        }
        return result;
    }

    @Override
    public MultiResult expectIn(int input, long timeoutMs, Matcher<?>... matchers) throws IOException {
        return expectIn(input, timeoutMs, allOf(matchers));
    }

    @Override
    public Expect send(String string) throws IOException {
        byte[] bytes = string.getBytes(charset);
        writeBytes(bytes);
        echoString(string);
        return this;
    }

    @Override
    public Expect sendLine() throws IOException {
        return sendLine("");
    }

    @Override
    public Expect sendLine(String string) throws IOException {
        return send(string + lineSeparator);
    }

    @Override
    public Expect sendBytes(byte[] bytes) throws IOException {
        writeBytes(bytes);
        echoString(new String(bytes, charset));
        return this;
    }

    private void writeBytes(byte[] bytes) throws IOException {
        output.write(bytes);
        output.flush();
    }

    private void echoString(String string) throws IOException {
        if (echoOutput != null) {
            echoOutput.append(string);
        }
    }

    @Override
    public <R extends Result> R expectIn(int input, Matcher<R> matcher) throws IOException {
        return expectIn(input, timeout, matcher);
    }

    @Override
    public <R extends Result> R expect(Matcher<R> matcher) throws IOException {
        return expectIn(0, matcher);
    }

    @Override
    public MultiResult expect(Matcher<?>... matchers) throws IOException {
        return expect(0, matchers);
    }

    @Override
    public <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException {
        return expectIn(0, timeoutMs, matcher);
    }

    @Override
    public MultiResult expect(long timeoutMs, Matcher<?>... matchers) throws IOException {
        return expectIn(0, timeoutMs, matchers);
    }

    @Override
    public void close() throws IOException {
        for (SingleInputExpect input : inputs) {
            input.stop();
        }
        executor.shutdown();
    }

    long getTimeout() {
        return timeout;
    }
}
