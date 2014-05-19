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

import net.sf.expectit.echo.EchoAdapters;
import net.sf.expectit.echo.EchoOutput;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.filter.Filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private Filter filter;
    private OutputStream output;
    private long timeout = DEFAULT_TIMEOUT_MS;
    private Appendable[] echoOutput;
    private Appendable echoInput;
    private Charset charset = Charset.defaultCharset();
    private boolean errorOnTimeout;
    private String lineSeparator = System.getProperty("line.separator");
    private EchoOutput echoOutputOld;

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
     * @param unit     the time unit
     * @return this
     * @throws java.lang.IllegalArgumentException if the timeout {@code <= 0}
     */
    public final ExpectBuilder withTimeout(long duration, TimeUnit unit) {
        validateDuration(duration);
        this.timeout = unit.toMillis(duration);
        return this;
    }

    /**
     * Verifies that the duration is &gt;= 0.
     */
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
     * Sets the echo output to print all the sent and received data. Useful for debugging to monitor I/O activity.
     * Optional, by default is unset.
     *
     * @param echoOutput an instance of echo output. Refer to {@link net.sf.expectit.echo.EchoAdapters} for adapter
     *                   static methods for common use cases.
     * @return this
     */
    public final ExpectBuilder withEchoOutput(EchoOutput echoOutput) {
        this.echoOutputOld = echoOutput;
        return this;
    }

    public final ExpectBuilder withEchoOutput(Appendable ... echoOutput) {
        this.echoOutput = echoOutput;
        return this;
    }

    public final ExpectBuilder withEchoInput(Appendable echoInput) {
        this.echoInput = echoInput;
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
     * Sets a filter for the input. Optional, by default no filter is applied.
     * <p/>
     * Filters can be used to modify the input before performing expect operations. For example, to remove
     * non-printable characters. Filters can be switched on and off while working with the expect instance.
     *
     * @param filter      the filter
     * @param moreFilters more filter to apply. if specified then all the filters are combined using the
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
     * @throws IOException                     if I/O error occurs
     * @throws java.lang.IllegalStateException if the {@code inputs} are incorrect
     */
    public final Expect build() throws IOException {
        if (inputs == null || inputs.length == 0) {
            throw new IllegalStateException("Inputs are null or empty");
        }

        if (echoOutput != null && inputs.length != echoOutput.length) {
            throw new IllegalArgumentException("Number of the echo outputs must correspond to " +
                    "the number of inputs");
        }

        if (echoOutputOld != null) {
            echoOutput = new Appendable[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                echoOutput[i] = EchoAdapters.adaptInput(i, echoOutputOld);
            }
        }

        SingleInputExpect[] inputs = new SingleInputExpect[this.inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            Appendable appendable = echoOutput != null ? echoOutput[i] : null;
            inputs[i] = new SingleInputExpect(i, this.inputs[i], charset, appendable, filter);
        }
        if (echoOutputOld != null) {
            echoInput = EchoAdapters.adaptOutput(echoOutputOld);
        }
        ExpectImpl instance = new ExpectImpl(timeout, output, inputs, charset, echoInput,
                errorOnTimeout, lineSeparator);
        instance.start();
        return instance;
    }

}
