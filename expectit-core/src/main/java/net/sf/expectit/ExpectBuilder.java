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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import net.sf.expectit.echo.EchoOutput;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.filter.Filters;

/**
 * A class used to construct {@link Expect} instances.
 */
public class ExpectBuilder {
    /**
     * The default timeout value of 30000 milliseconds.
     */
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    /**
     * The default buffer size in bytes.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    private InputStream[] inputs;
    private Filter filter;
    private OutputStream output;
    private long timeout = DEFAULT_TIMEOUT_MS;
    private EchoOutput echoOutputOld;
    private Charset charset = Charset.defaultCharset();
    @Deprecated
    private boolean errorOnTimeout;
    private String lineSeparator = "\n";
    private Appendable echoOutput;
    private Appendable echoInput;
    private Appendable[] echoInputs;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private boolean exceptionOnFailure;

    /**
     * Default constructor.
     */
    public ExpectBuilder() {
    }

    /**
     * Sets the output stream where {@link Expect} sends command to. Optional,
     * if not set then all the
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
     * Sets the default timeout in the given unit for expect operations. Optional,
     * the default value is 30 seconds.
     *
     * @param duration the timeout value
     * @param unit     the time unit
     * @return this
     * @throws java.lang.IllegalArgumentException if the timeout {@code <= 0}
     */
    public final ExpectBuilder withTimeout(long duration, TimeUnit unit) {
        validateDuration(duration);
        this.timeout = unit.toMillis(duration);
        return this;
    }

    static void validateDuration(long duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration <= 0");
        }
    }

    /**
     * Sets the default timeout to infinity.
     *
     * @return this
     */
    public final ExpectBuilder withInfiniteTimeout() {
        this.timeout = -1;
        return this;
    }

    /**
     * Sets the echo output to print all the sent and received data. Useful for debugging to
     * monitor I/O activity.
     * Optional, by default is unset.
     *
     * @param echoOutput an instance of echo output. Refer to {@link net.sf.expectit.echo
     *                   .EchoAdapters} for adapter
     *                   static methods for common use cases.
     * @return this
     */
    @Deprecated
    public final ExpectBuilder withEchoOutput(EchoOutput echoOutput) {
        this.echoOutputOld = echoOutput;
        return this;
    }

    /**
     * Enables printing of all the sent data. Useful for debugging to monitor I/O activity.
     * Optional, by default is unset.
     *
     * @param echoOutput where to echo the sent data.
     * @return this
     */
    public final ExpectBuilder withEchoOutput(Appendable echoOutput) {
        this.echoOutput = echoOutput;
        return this;
    }

    /**
     * Enables printing of all the received data. Useful for debugging to monitor I/O activity.
     * Optional, by default is unset.
     * <p/>
     * If the only {@code firstInput} is specified then the data received from all the inputs is
     * echoed to it.
     * <p/>
     * The build method throws an {@link java.lang.IllegalArgumentException} if the number of the
     * {@code otherInputs}
     * parameters does not correspond to the number of the input streams. That is,
     * if {@code otherInputs}
     * is specified, the number of them must be equal to the number of inputs minus 1.
     *
     * @param firstInput  where to echo the received data for the first input. If {@code
     *                    otherInputs} are empty, then
     *                    all the data will be echoed to it.
     * @param otherInputs where to echo the received data for other inputs.
     * @return this
     */
    public final ExpectBuilder withEchoInput(Appendable firstInput, Appendable... otherInputs) {
        this.echoInput = firstInput;
        this.echoInputs = otherInputs;
        return this;
    }

    /**
     * Sets the character encoding used to covert bytes when working with byte streams. Optional,
     * the default is
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
     * Sets a filter for the input. Optional, by default no filter is applied.
     * <p/>
     * Filters can be used to modify the input before performing expect operations. For example,
     * to remove
     * non-printable characters. Filters can be switched on and off while working with the expect
     * instance.
     *
     * @param filter      the filter
     * @param moreFilters more filter to apply. if specified then all the filters are combined
     *                    using the
     *                    {@link Filters#chain(Filter...)} method.s
     * @return this
     */
    public final ExpectBuilder withInputFilters(Filter filter, Filter... moreFilters) {
        if (moreFilters.length == 0) {
            this.filter = filter;
        } else {
            Filter[] filters = new Filter[moreFilters.length + 1];
            filters[0] = filter;
            System.arraycopy(moreFilters, 0, filters, 1, moreFilters.length);
            this.filter = Filters.chain(filters);
        }
        return this;
    }

    /**
     * Enables throwing an {@link java.lang.AssertionError} if any expect operation times out.
     * Optional, disabled by default.
     *
     * @param errorOnTimeout the error flag
     * @return this
     * @deprecated This method is deprecated and will be removed. Use
     * {@link #withExceptionOnFailure()} instead.
     */
    @Deprecated
    public final ExpectBuilder withErrorOnTimeout(boolean errorOnTimeout) {
        this.errorOnTimeout = errorOnTimeout;
        return this;
    }

    /**
     * Enables throwing an {@link ExpectIOException} if an expect operation
     * was not successful. Optional, disabled by default.
     *
     * @return this
     */
    public final ExpectBuilder withExceptionOnFailure() {
        this.exceptionOnFailure = true;
        return this;
    }

    /**
     * Sets the line separator used by the {@link Expect#sendLine()} method. Optional, default
     * is '\n'.
     * <p>
     * The default line separator was intentionally made non-system specific to avoid issues like
     * when the tool is running on Windows and sending {@code CRLF} to a Unix system which threats
     * them as two new lines.
     * </p>
     *
     * @param lineSeparator the line separator
     * @return this
     */
    public final ExpectBuilder withLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
        return this;
    }

    /**
     * Sets the size of the input buffer in bytes. Optional, default is 1024.
     *
     * @param bufferSize the buffer size
     * @return this
     * @throws java.lang.IllegalArgumentException if {@code bufferSize} is <= 0
     */
    public final ExpectBuilder withBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be > 0");
        }
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * Creates a ready to use {@link Expect} instance.
     * <p/>
     * This method creates an instance and starts background threads that receive input data
     * through NIO pipes. The
     * created instance must be disposed after use by calling the {@link net.sf.expectit
     * .Expect#close()}  method,
     * <p/>
     * The instance is not thread safe and intended to be used in a single thread.
     *
     * @return the instance
     * @throws java.io.IOException                     if I/O error occurs
     * @throws java.lang.IllegalStateException if the {@code inputs} are incorrect
     */
    public final Expect build() throws IOException {
        if (inputs == null || inputs.length == 0) {
            throw new IllegalStateException("Inputs are null or empty");
        }

        if (echoInputs != null && echoInputs.length != 0
                && echoInputs.length != inputs.length - 1) {
            throw new IllegalArgumentException(
                    "The number of echo input does not correspond to the total "
                            + "number of the input streams");
        }

        SingleInputExpect[] inputs = new SingleInputExpect[this.inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = new SingleInputExpect(
                    this.inputs[i], charset,
                    getEchoInputForIndex(i), filter, bufferSize);
        }

        if (echoOutputOld != null) {
            echoOutput = new AppendableAdapter() {
                @Override
                public Appendable append(CharSequence csq) throws IOException {
                    echoOutputOld.onSend(csq.toString());
                    return this;
                }
            };
        }
        ExpectImpl instance = new ExpectImpl(
                timeout,
                output,
                inputs,
                charset,
                echoOutput,
                errorOnTimeout,
                lineSeparator,
                exceptionOnFailure);
        instance.start();
        return instance;
    }

    private Appendable getEchoInputForIndex(final int i) {
        if (echoOutputOld != null) {
            return new AppendableAdapter() {
                @Override
                public Appendable append(CharSequence csq) throws IOException {
                    echoOutputOld.onReceive(i, csq.toString());
                    return this;
                }
            };
        }
        if (echoInput == null) {
            return null;
        } else if (echoInputs.length == 0 || i == 0) {
            return echoInput;
        } else {
            return echoInputs[i - 1];
        }
    }

}
