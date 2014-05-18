package net.sf.expectit;

import net.sf.expectit.matcher.Matcher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * An adapter for an expect instance which overrides the default timeout.
 */
class ExpectTimeoutAdapter extends AbstractExpectImpl {
    private final ExpectImpl delegate;

    ExpectTimeoutAdapter(ExpectImpl delegate, long timeout) {
        super(timeout);
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
