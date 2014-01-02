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

/**
 * An implementation of the Expect interface which delegates actual work to SingleInput objects.
 */
class ExpectImpl implements Expect {
    private final long timeout;
    private final OutputStream output;
    private final SingleInput[] inputs;
    private final Charset charset;
    private final OutputStream echoOutput;
    private final boolean errorOnTimeout;
    private final ExecutorService executor;

    ExpectImpl(long timeout, OutputStream output, SingleInput[] inputs,
               Charset charset, OutputStream echoOutput, boolean errorOnTimeout) {
        this.timeout = timeout;
        this.output = output;
        this.inputs = inputs;
        this.charset = charset;
        this.echoOutput = echoOutput;
        this.errorOnTimeout = errorOnTimeout;
        executor = Executors.newFixedThreadPool(inputs.length);
    }

    void start() {
        for (SingleInput input : inputs) {
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
    public Expect send(String string) throws IOException {
        return sendBytes(string.getBytes(charset));
    }

    @Override
    public Expect sendLine() throws IOException {
        return sendLine("");
    }

    @Override
    public Expect sendLine(String string) throws IOException {
        return send(string + System.lineSeparator());
    }

    @Override
    public Expect sendBytes(byte[] bytes) throws IOException {
        output.write(bytes);
        output.flush();
        if (echoOutput != null) {
            echoOutput.write(bytes);
            echoOutput.flush();
        }
        return this;
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
    public <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException {
        return expectIn(0, timeoutMs, matcher);
    }

    @Override
    public void close() throws IOException {
        for (SingleInput input : inputs) {
            input.stop();
        }
        executor.shutdown();
    }
}
