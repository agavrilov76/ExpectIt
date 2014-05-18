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

import net.sf.expectit.echo.EchoOutput;
import net.sf.expectit.matcher.Matcher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.ExpectBuilder.validateDuration;

/**
 * An implementation of the Expect interface which delegates actual work to SingleInputExpect objects.
 */
class ExpectImpl extends AbstractExpectImpl {
    private final OutputStream output;
    private final SingleInputExpect[] inputs;
    private final Charset charset;
    private final EchoOutput echoOutput;
    private final boolean errorOnTimeout;
    private final ExecutorService executor;
    private final String lineSeparator;

    ExpectImpl(long timeout, OutputStream output, SingleInputExpect[] inputs,
               Charset charset, EchoOutput echoOutput, boolean errorOnTimeout, String lineSeparator) {
        super(timeout);
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
    public Expect withTimeout(long duration, TimeUnit unit) {
        validateDuration(duration);
        return new ExpectTimeoutAdapter(this, unit.toMillis(duration));
    }

    @Override
    public Expect withInfinitiveTimeout() {
        return new ExpectTimeoutAdapter(this, -1);
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
            echoOutput.onSend(string);
        }
    }

    @Override
    public void close() throws IOException {
        for (SingleInputExpect input : inputs) {
            input.stop();
        }
        executor.shutdown();
    }
}
