package net.sf.expectit.ant.matcher;

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
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;
import org.apache.tools.ant.BuildException;

/**
 * An abstract generic matcher element.
 *
 * @param <R> the type result
 */
abstract class AbstractMatcherElement<R extends Result> extends AbstractTaskElement {
    private Long timeout;
    private Integer input;
    private String resultPrefix;

    /**
     * Default constructor.
     */
    protected AbstractMatcherElement() {
    }

    /**
     * Creates a matcher by calling {@link #createMatcher()} method and executes the expect
     * operation.
     */
    public final void execute() {
        try {
            Matcher<R> matcher = createMatcher();
            R result;
            if (input == null && timeout == null) {
                result = getExpect().expect(matcher);
            } else if (timeout == null) {
                result = getExpect().expectIn(input, matcher);
            } else if (input == null) {
                result = getExpect().expect(timeout, matcher);
            } else {
                result = getExpect().expectIn(input, timeout, matcher);
            }
            exportSuccessfulResult(resultPrefix, result);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Exports the successful result of the match as a set of properties with the given prefix.
     * The properties key/value
     * format is as follows:
     * <ul>
     * <li>{@code prefix + ".before" = result.getBefore()}</li>
     * <li>{@code prefix + ".group" = result.group()}</li>
     * <li>{@code prefix + ".success" = true}</li>
     * <li>{@code prefix + ".group." + &lt;number&gt; = result.group(&lt;number&gt;)},
     * where the {@code number}
     * is between 1 and {@code result.groupCount()}</li>
     * </ul>
     * If the {@code prefix} is {@code null}, or the {@code result} is not successful,
     * then this method does nothing.
     *
     * @param prefix the property prefix
     * @param result the result
     */
    protected void exportSuccessfulResult(String prefix, R result) {
        if (prefix == null) {
            return;
        }
        setPropertyIfNoNull(prefix, "input", result.getInput());
        if (!result.isSuccessful()) {
            return;
        }
        setPropertyIfNoNull(prefix, "input", result.getInput());
        setPropertyIfNoNull(prefix, "before", result.getBefore());
        setPropertyIfNoNull(prefix, "group", result.group());
        setPropertyIfNoNull(prefix, "success", "true");
        for (int i = 1; i <= result.groupCount(); i++) {
            setPropertyIfNoNull(prefix, "group." + i, result.group(i));
        }
    }

    private void setPropertyIfNoNull(String prefix, String name, String value) {
        if (value != null) {
            getProject().setProperty(prefix + "." + name, value);
        }
    }

    /**
     * Create a matcher corresponding to this element.
     *
     * @return the matcher instance
     */
    protected abstract Matcher<R> createMatcher();

    /**
     * Sets the timeout for the expect operation.
     *
     * @param timeout the timeout in milliseconds.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the input number. {@code 0} is default.
     *
     * @param input the input number
     */
    public void setInput(int input) {
        this.input = input;
    }

    /**
     * Sets the property result prefix used to export a match result.
     *
     * @param resultPrefix the property prefix
     */
    public void setResultPrefix(String resultPrefix) {
        this.resultPrefix = resultPrefix;
    }

    /**
     * Gets the property prefix.
     *
     * @return the property prefix
     */
    public String getResultPrefix() {
        return resultPrefix;
    }
}
