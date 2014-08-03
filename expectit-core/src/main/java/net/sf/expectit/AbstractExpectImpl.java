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

import net.sf.expectit.matcher.Matcher;

import java.io.IOException;

import static net.sf.expectit.matcher.Matchers.allOf;

/**
 * An abstract Expect class that implements the expect methods in a way to redirect the call to one abstract method.
 */
abstract class AbstractExpectImpl implements Expect {
    private final long timeout;

    /**
     * Constructor.
     *
     * @param timeoutMs timeout in milliseconds.
     */
    AbstractExpectImpl(long timeoutMs) {
        this.timeout = timeoutMs;
    }

    long getTimeout() {
        return timeout;
    }

    public abstract <R extends Result> R expectIn(int input, long timeoutMs, Matcher<R> matcher) throws IOException;

    public MultiResult expectIn(int input, long timeoutMs, Matcher<?>... matchers) throws IOException {
        return expectIn(input, timeoutMs, allOf(matchers));
    }

    public <R extends Result> R expectIn(int input, Matcher<R> matcher) throws IOException {
        return expectIn(input, timeout, matcher);
    }

    public <R extends Result> R expect(Matcher<R> matcher) throws IOException {
        return expectIn(0, matcher);
    }

    public MultiResult expect(Matcher<?>... matchers) throws IOException {
        return expect(0, matchers);
    }

    public <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException {
        return expectIn(0, timeoutMs, matcher);
    }

    public MultiResult expect(long timeoutMs, Matcher<?>... matchers) throws IOException {
        return expectIn(0, timeoutMs, matchers);
    }
}
