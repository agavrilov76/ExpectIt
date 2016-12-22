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

import static net.sf.expectit.ExpectBuilder.validateDuration;
import static net.sf.expectit.Utils.toDebugString;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.expectit.interact.InteractBuilder;
import net.sf.expectit.matcher.Matcher;

/**
 * An implementation of the Expect interface which delegates actual work to SingleInputExpect
 * objects.
 */
class ExpectImpl extends AbstractExpectImpl {
    private static final Logger LOG = Logger.getLogger(ExpectImpl.class.getName());

    static final int INFINITE_TIMEOUT = -1; // value representing infinite timeout

    private final OutputStream output;
    private final SingleInputExpect[] inputs;
    private final Charset charset;
    private final Appendable echoOutput;
    @Deprecated
    private final boolean errorOnTimeout;
    private final ExecutorService executor;
    private final String lineSeparator;
    private final boolean exceptionOnFailure;
    private final boolean autoFlushEcho;
    private final boolean useInternalExecutor;

    ExpectImpl(
            final long timeout,
            final OutputStream output,
            final SingleInputExpect[] inputs,
            final Charset charset,
            final Appendable echoOutput,
            final boolean errorOnTimeoutOld,
            final String lineSeparator,
            final boolean exceptionOnFailure,
            final boolean autoFlushEcho,
            final ExecutorService executor) {
        super(timeout);
        this.output = output;
        this.inputs = inputs;
        this.charset = charset;
        this.echoOutput = echoOutput;
        this.errorOnTimeout = errorOnTimeoutOld;
        this.exceptionOnFailure = exceptionOnFailure;
        this.lineSeparator = lineSeparator;
        this.autoFlushEcho = autoFlushEcho;
        this.executor = executor == null
                ? Executors.newFixedThreadPool(
                        inputs.length,
                        new NamedExecutorThreadFactory("expect-"))
                : executor;
        this.useInternalExecutor = executor == null;
    }

    void start() {
        for (SingleInputExpect input : inputs) {
            input.start(executor);
        }
    }

    @Override
    public <R extends Result> R expectIn(int input, long timeoutMs, Matcher<R> matcher)
            throws IOException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(
                    String.format(
                            "Expect matcher '%s' with timeout %d (ms) in input #%d",
                            toDebugString(matcher),
                            timeoutMs,
                            input));
        }
        R result = inputs[input].expect(timeoutMs, matcher);
        if (exceptionOnFailure && !result.isSuccessful()) {
            final String inputBuffer = inputs[input].getBuffer().toString();
            throw new ExpectIOException(
                    "Expect operation fails (timeout: "
                            + timeoutMs + " ms) for matcher: " + matcher, inputBuffer);
        }
        if (errorOnTimeout && !result.isSuccessful()) {
            throw new AssertionError(
                    "Expect timeout (" + timeoutMs + " ms) for matcher: " + matcher);
        }
        return result;
    }

    @Override
    public Expect withTimeout(long duration, TimeUnit unit) {
        validateDuration(duration);
        return new ExpectTimeoutAdapter(this, unit.toMillis(duration));
    }

    @Deprecated
    @Override
    public Expect withInfiniteTimeout() {
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
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Writing bytes: " + toDebugString(bytes, bytes.length, charset));
        }
        output.write(bytes);
        output.flush();
    }

    private void echoString(String string) throws IOException {
        if (echoOutput != null) {
            echoOutput.append(string);
            if (autoFlushEcho) {
                Utils.flushAppendable(echoOutput);
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (SingleInputExpect input : inputs) {
            input.stop();
        }

        if (useInternalExecutor) {
            executor.shutdown();
        }
        if (autoFlushEcho) {
            Utils.flushAppendable(echoOutput);
        }
    }

    @Override
    public InteractBuilder interact() {
        return interactWith(0);
    }

    @Override
    public InteractBuilder interactWith(final int input) {
        return interactWithInternal(this, input);
    }

    InteractBuilder interactWithInternal(final AbstractExpectImpl expect, final int input) {
        if (input >= inputs.length || input < 0) {
            throw new IllegalArgumentException("Input index is out of bounds: " + input);
        }
        return new InteractBuilderImpl(expect, input);
    }

    @Override
    SingleInputExpect[] getInputs() { return inputs; }
}
