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
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * A class used to construct {@link Expect} instances.
 */
public class ExpectBuilder {
    /**
     * The default timeout value of 30000 milliseconds.
     */
    public static final int DEFAULT_TIMEOUT_MS = 30000;

    private InputStream[] inputs;
    private Filter[] filters;
    private OutputStream output;
    private long timeout = DEFAULT_TIMEOUT_MS;
    private Writer echoOutput;
    private Charset charset = Charset.defaultCharset();
    private boolean errorOnTimeout;
    private String lineSeparator = System.getProperty("line.separator");

    /**
     * Default constructor.
     */
    public ExpectBuilder() {
    }

    /**
     * Sets the output stream where {@link Expect} sends command to. Optional, if not set then all the
     * send method are expected to throw {@link NullPointerException}.
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
     * Sets the default timeout in the given unit for expect operations. Optional, the default value is 30 seconds.
     *
     * @param duration the timeout value
     * @param unit the time unit
     * @return this
     * @throws java.lang.IllegalArgumentException if the timeout {@code <= 0}
     */
    public final ExpectBuilder withTimeout(long duration, TimeUnit unit) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration <= 0");
        }
        this.timeout = unit.toMillis(duration);
        return this;
    }

    /**
     * Sets the echo output to print all the sent and received data. Useful for debugging to monitor I/O activity.
     * Optional, default is unset.
     *
     * @param echoOutput the output stream
     * @return this
     */
    public final ExpectBuilder withEchoOutput(Writer echoOutput) {
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
     * Sets a filter for the input. Optional, by default no filters are applied.
     * <p/>
     * Filters can be used to modify the input before performing expect operations. For example, to remove
     * non-printable characters.
     * <p/>
     * The given filters are applied one by one. The string returns by the
     * {@link net.sf.expectit.filter.Filter#filter(String, StringBuilder)} method of one filter is passed a parameter
     * to next one if not {@code null}. If it is {@code null}, then the filtering process stops and the latest
     * non-null result is appended to the expect internal buffer.
     *
     * @param filters the filters
     * @return this
     */
    public final ExpectBuilder withInputFilters(Filter... filters) {
        this.filters = filters;
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
     * Sets the line separator used by the {@link Expect#sendLine()} method. Optional, default is the system default.
     *
     * @param lineSeparator the line separator
     * @return this
     */
    public final ExpectBuilder withLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
        return this;
    }

    /**
     * Creates a ready to use {@link Expect} instance.
     * <p/>
     * This method creates an instance and starts background threads that receive input data through NIO pipes. The
     * created instance must be disposed after use by calling the {@link net.sf.expectit.Expect#close()}  method,
     * <p/>
     * The instance is not thread safe and intended to be used in a single thread.
     *
     * @return the instance
     * @throws IOException if I/O error occurs
     * @throws java.lang.IllegalStateException if the {@code inputs} are incorrect
     */
    public final Expect build() throws IOException {
        if (inputs == null || inputs.length == 0) {
            throw new IllegalStateException("Inputs are null or empty");
        }

        SingleInputExpect[] inputs = new SingleInputExpect[this.inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new SingleInputExpect(this.inputs[i], charset, echoOutput, filters);
        }

        ExpectImpl instance = new ExpectImpl(timeout, output, inputs, charset, echoOutput,
                errorOnTimeout, lineSeparator);
        instance.start();
        return instance;
    }

}
