package net.sf.expectit.ant;

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

import net.sf.expectit.ant.filter.FiltersElement;

/**
 * A useful interface which expect tasks should implement to implement to avoid forgetting method
 * delegations.
 */
public interface ExpectSupport {
    /**
     * Adds a task container element.
     *
     * @param sequential the task container
     */
    void add(SequentialElement sequential);

    /**
     * Sets the global timeout for expect operations.
     *
     * @param ms the timeout in milliseconds. -1 indicates that the timeout should be set to
     *           infinity.
     * @see net.sf.expectit.ExpectBuilder#withTimeout(long, java.util.concurrent.TimeUnit)
     */
    void setExpectTimeout(long ms);

    /**
     * Sets the expect charset.
     *
     * @param charset the charset name
     * @see net.sf.expectit.ExpectBuilder#withCharset(java.nio.charset.Charset)
     */
    void setCharset(String charset);

    /**
     * Adds the filter container element.
     *
     * @param filters the container element.
     */
    void add(FiltersElement filters);

    /**
     * Sets the {@code errorOnTimeout} flag. Default is false.
     *
     * @param errorOnTimeout - if {@code true} then an error occurs if any match operation times
     *                       out, {@code false}
     *                       otherwise.
     * @see net.sf.expectit.ExpectBuilder#withErrorOnTimeout(boolean)
     */
    @Deprecated
    void setErrorOnTimeout(boolean errorOnTimeout);

    /**
     * Sets the {@code exceptionOnFailure} flag. Default is false.
     *
     * @param exceptionOnFailure - if {@code true} then an error occurs if any match operation
     * fails or {@code false} otherwise.
     * @see net.sf.expectit.ExpectBuilder#withExceptionOnFailure()
     */
    void setExceptionOnFailure(boolean exceptionOnFailure);
    /**
     * Sets the line separator for the send element.
     *
     * @param lineSeparator the line separator
     * @see net.sf.expectit.ExpectBuilder#withLineSeparator(String)
     */
    void setLineSeparator(String lineSeparator);

    /**
     * Enables the echo output. If set to {@code true}, then the input and output data are logged
     * by the Ant's task
     * logger.
     *
     * @param echoOutput the echo output flag
     */
    void setEchoOutput(boolean echoOutput);
}
