package net.sf.expectit.ant;

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

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.ant.filter.FiltersElement;
import org.apache.tools.ant.Task;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the export support.
 */
public class ExpectSupportImpl implements Closeable, ExpectSupport {
    private SequentialElement sequential;
    private Expect expect;
    private InputStream[] inputStreams = new InputStream[2];
    private FiltersElement filters;

    private final Task task;
    private final ExpectBuilder builder;

    ExpectSupportImpl(Task task, ExpectBuilder builder) {
        this.task = task;
        this.builder = builder;
    }

    /**
     * Creates an instance for the given task.
     *
     * @param task the task
     */
    public ExpectSupportImpl(Task task) {
        this(task, new ExpectBuilder());
    }

    @Override
    public void add(SequentialElement sequential) {
        this.sequential = sequential;
    }

    @Override
    public void setExpectTimeout(long ms) {
        builder.withTimeout(ms, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setCharset(String charset) {
        builder.withCharset(Charset.forName(charset));
    }

    @Override
    public void add(FiltersElement filters) {
        this.filters = filters;
    }

    @Override
    public void setErrorOnTimeout(boolean errorOnTimeout) {
        builder.withErrorOnTimeout(errorOnTimeout);
    }

    @Override
    public void setLineSeparator(String lineSeparator) {
        builder.withLineSeparator(lineSeparator);
    }

    /**
     * Constructs an instance of {@link Expect} and perform the sequential tasks on it.
     *
     * @throws IOException if an I/O error occurs
     */
    public void execute() throws IOException {
        if (sequential == null) {
            throw new IllegalArgumentException("Sequential element is missing");
        }
        builder.withInputs(inputStreams);
        if (filters != null) {
            builder.withInputFilters(filters.toFilter());
        }
        expect = builder.build();
        sequential.setExpect(expect);
        sequential.perform();
    }

    @Override
    public void close() throws IOException {
        if (expect != null) {
            expect.close();
        }
    }

    /**
     * Sets the output stream for the expect instance.
     *
     * @param output the output stream
     * @see net.sf.expectit.ExpectBuilder#withOutput(java.io.OutputStream)
     */
    public void setOutput(OutputStream output) {
        builder.withOutput(output);
    }

    /**
     * Sets the input streams for the expect instance.
     *
     * @param index the number of the input stream
     * @param is    the input stream
     * @see net.sf.expectit.ExpectBuilder#withInputs(java.io.InputStream...)
     */
    public void setInput(int index, InputStream is) {
        if (index >= inputStreams.length) {
            inputStreams = Arrays.copyOf(inputStreams, index + 1);
        }
        this.inputStreams[index] = is;
    }

    @Override
    public void setEchoOutput(boolean echoOutput) {
        if (echoOutput) {
            builder.withEchoOutput(new EchoOutputAdapter(task));
        }
    }

}
