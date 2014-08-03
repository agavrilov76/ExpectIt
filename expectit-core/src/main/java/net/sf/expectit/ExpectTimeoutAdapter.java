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
import java.util.concurrent.TimeUnit;

/**
 * An adapter for an expect instance which overrides the default timeout.
 */
class ExpectTimeoutAdapter extends AbstractExpectImpl {
    private final ExpectImpl delegate;

    ExpectTimeoutAdapter(ExpectImpl delegate, long timeoutMs) {
        super(timeoutMs);
        this.delegate = delegate;
    }

    @Override
    public <R extends Result> R expectIn(int input, long timeoutMs, Matcher<R> matcher) throws IOException {
        return delegate.expectIn(input, timeoutMs, matcher);
    }

    @Override
    public Expect withTimeout(long duration, TimeUnit unit) {
        return delegate.withTimeout(duration, unit);
    }

    @Override
    public Expect withInfiniteTimeout() {
        return delegate.withInfiniteTimeout();
    }

    @Override
    public Expect send(String string) throws IOException {
        return delegate.send(string);
    }

    @Override
    public Expect sendLine() throws IOException {
        return delegate.sendLine();
    }

    @Override
    public Expect sendLine(String string) throws IOException {
        return delegate.sendLine(string);
    }

    @Override
    public Expect sendBytes(byte[] bytes) throws IOException {
        return delegate.sendBytes(bytes);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
