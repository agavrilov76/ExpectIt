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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A class used to construct {@link Expect} instances.
 */
public class ExpectBuilder {
    /**
     * The default expect timeout in milliseconds.
     */
    public static final int DEFAULT_TIMEOUT_MS = 30000;

    private InputStream[] inputs;
    private Filter filter;
    private OutputStream output;
    private long timeout = DEFAULT_TIMEOUT_MS;
    private OutputStream echoOutput;
    private Charset charset = Charset.defaultCharset();
    private boolean errorOnTimeout;

    /**
     * Default constructor.
     */
    public ExpectBuilder() {
    }

    /**
     * Sets the output stream where {@link Expect} sends command to. Required.
     *
     * @param output the output stream
     * @return this
     */
    public final ExpectBuilder withOutput(OutputStream output) {
        this.output = output;
        return this;
    }

    /**
     * Sets the input streams for expect operations. Required.
     *
     * @param inputs the input stream
     * @return this
     */
    public final ExpectBuilder withInputs(InputStream... inputs) {
        this.inputs = inputs;
        return this;
    }

    /**
     * Sets timeout in milliseconds for expect operations. Optional, the default value is {@link #DEFAULT_TIMEOUT_MS}.
     *
     * @param timeoutMs the timeout
     * @return this
     * @throws java.lang.IllegalArgumentException if the timeout {@code <= 0}
     */
    public final ExpectBuilder withTimeout(long timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Timeout <= 0");
        }
        this.timeout = timeoutMs;
        return this;
    }

    /**
     * Sets the echo output stream to print all sent and received data. Useful for debugging to monitor I/O activity.
     * Optional, default is unset.
     *
     * @param echoOutput the output stream
     * @return this
     */
    public final ExpectBuilder withEchoOutput(OutputStream echoOutput) {
        this.echoOutput = echoOutput;
        return this;
    }

    /**
     * Sets the character encoding used to covert bytes when working with byte streams. Optional, the default is
     * {@link Charset#defaultCharset}.
     *
     * @param charset the charset
     * @return this
     */
    public final ExpectBuilder withCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Sets a filter for the input.
     * <p/>
     * Filters can be used to modify the input before performing expect operations. For example, to remove
     * non-printable characters.
     *
     * @param filter the filter instance.
     * @return this
     */
    public final ExpectBuilder withInputFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Enables throwing an {@link java.lang.AssertionError} if any expect operation times out.
     * Optional, disabled by default.
     *
     * @param errorOnTimeout the error flag
     * @return this
     */
    public final ExpectBuilder withErrorOnTimeout(boolean errorOnTimeout) {
        this.errorOnTimeout = errorOnTimeout;
        return this;
    }

    /**
     * Creates a read to use {@link Expect} instance.
     * <p/>
     * This method creates an instance and starts background threads that receive input data through NIO pipes. The
     * instance is not thread safe and intended to be used in a single thread.
     *
     * @return the instance
     * @throws IOException if I/O error occurs
     */
    public final Expect build() throws IOException {
        checkArguments();
        SingleInput[] inputs = new SingleInput[this.inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new SingleInput(this.inputs[i], charset, echoOutput, filter);
        }
        ExpectImpl instance = new ExpectImpl(timeout, output, inputs, charset, echoOutput, errorOnTimeout);
        instance.start();
        return instance;
    }

    private void checkArguments() {
        if (output == null) {
            throw new IllegalArgumentException("Output is null");
        }
        if (inputs == null || inputs.length == 0) {
            throw new IllegalArgumentException("Inputs are null or empty");
        }
    }


}
