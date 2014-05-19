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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * The main interface providing access to the expect operations.
 * <p/>
 * Input data for the operations is stored in the internal buffer growing while new data is being received from
 * the underlying input stream.
 * A successful match operation updates the buffer by removing a part from the begging until the end position of the
 * match.
 */
public interface Expect extends Closeable {
    /**
     * Sends the string to the output stream and flushes the output.
     * <p/>
     * The string is converted to bytes using {@code charset} set in the {@link net.sf.expectit.ExpectBuilder}.
     *
     * @param string the string to be sent
     * @return this
     * @throws IOException if I/O error occurs
     */
    Expect send(String string) throws IOException;

    /**
     * Appends the line separator character to the string and calls {@link #send(String)} method.
     *
     * @param string the string to be sent
     * @return this
     * @throws IOException if I/O error occurs
     * @see #send(String)
     * @see net.sf.expectit.ExpectBuilder#withLineSeparator(String)
     */
    Expect sendLine(String string) throws IOException;

    /**
     * Sends a new line to the output. Simply calls the {@link #sendLine(String)} method with empty string as a
     * parameter.
     *
     * @return this
     * @throws IOException if I/O error occurs
     */
    Expect sendLine() throws IOException;

    /**
     * Sends the given byte array and flushes the output.
     *
     * @param bytes the bytes to be sent
     * @return this
     * @throws IOException if I/O error occurs
     */
    Expect sendBytes(byte[] bytes) throws IOException;

    /**
     * Blocks until the given matcher matches against the first input stream using the default timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.sf.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     * <p/>
     * An instance of {@link java.io.EOFException} will be thrown if the underlying input steam, the internal buffer
     * is empty and the match operation is not successful.
     *
     * @param <R>     the matcher type
     * @param matcher the mather
     * @return the match result
     * @throws java.io.IOException                          if I/O error occurs
     * @throws java.nio.channels.ClosedByInterruptException if the thread calling this method has been interrupted
     */
    <R extends Result> R expect(Matcher<R> matcher) throws IOException;

    /**
     * Blocks until all the given matchers match against the first input stream using the default timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.sf.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     * <p/>
     * An instance of {@link java.io.EOFException} will be thrown if the underlying input steam, the internal buffer
     * is empty and the match operation is not successful.
     *
     * @param matchers the matchers
     * @return the match result from all the matchers
     * @throws java.io.IOException                          if I/O error occurs
     * @throws java.nio.channels.ClosedByInterruptException if the thread calling this method has been interrupted
     */
    MultiResult expect(Matcher<?>... matchers) throws IOException;

    /**
     * Blocks until the given matcher matches the first input stream using the given timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.sf.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     * <p/>
     * An instance of {@link java.io.EOFException} will be thrown if the underlying input stream, the internal buffer
     * is empty and the match operation is not successful.
     *
     * @param <R>       the matcher type
     * @param timeoutMs the timeout for the expect operation in milliseconds, infinite if -1 is passed
     * @param matcher   the mather
     * @return the match result
     * @throws java.io.IOException                          if I/O error occurs
     * @throws java.nio.channels.ClosedByInterruptException if the thread calling this method has been interrupted
     * @deprecated This method is deprecated and will be removed. Use
     * {@link #withTimeout(long, java.util.concurrent.TimeUnit)} to change the timeout for the expect operation.
     */
    @Deprecated
    <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException;

    /**
     * Blocks until all the given matchers match the first input stream using the given timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.sf.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     * <p/>
     * An instance of {@link java.io.EOFException} will be thrown if the underlying input steam, the internal buffer
     * is empty and the match operation is not successful.
     *
     * @param timeoutMs the timeout for the expect operation in milliseconds, infinite if -1 is passed
     * @param matcher   the matchers
     * @return the match result from all the matchers
     * @throws java.io.IOException                          if I/O error occurs
     * @throws java.nio.channels.ClosedByInterruptException if the thread calling this method has been interrupted
     * @deprecated This method is deprecated and will be removed. Use
     * {@link #withTimeout(long, java.util.concurrent.TimeUnit)} to change the timeout for the expect operation.
     */
    @Deprecated
    MultiResult expect(long timeoutMs, Matcher<?>... matcher) throws IOException;

    /**
     * Blocks until the given matcher matches against the given input stream using the default timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.sf.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     * <p/>
     * An instance of {@link java.io.EOFException} will be thrown if the underlying input steam, the internal buffer
     * is empty and the match operation is not successful.
     *
     * @param <R>     the matcher type
     * @param input   the index of the input. if the index is outside of the boundaries, a runtime exception will be
     *                thrown
     * @param matcher the matcher
     * @return the match result
     * @throws java.io.IOException                          if I/O error occurs
     * @throws java.nio.channels.ClosedByInterruptException if the thread calling this method has been interrupted
     */
    <R extends Result> R expectIn(int input, Matcher<R> matcher) throws IOException;

    /**
     * Blocks until the given matcher matches against the given input stream using the given timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.sf.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     *
     * @param <R>       the matcher type
     * @param input     the index of the input. if the index is outside of the boundaries, a runtime exception will be
     *                  thrown
     * @param timeoutMs the timeout for the expect operation in milliseconds, infinite if -1 is passed
     * @param matcher   the mather
     * @return the match result
     * @throws java.io.IOException                          if I/O error occurs
     * @throws java.nio.channels.ClosedByInterruptException if the thread calling this method has been interrupted
     * @deprecated This method is deprecated and will be removed. Use
     * {@link #withTimeout(long, java.util.concurrent.TimeUnit)} to change the timeout for the expect operation.
     */
    @Deprecated
    <R extends Result> R expectIn(int input, long timeoutMs, Matcher<R> matcher) throws IOException;

    /**
     * Blocks until all the given matchers matche against the given input stream using the given timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.sf.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     * <p/>
     * An instance of {@link java.io.EOFException} will be thrown if the underlying input steam, the internal buffer
     * is empty and the match operation is not successful.
     *
     * @param input     the index of the input. if the index is outside of the boundaries, a runtime exception will be
     *                  thrown
     * @param timeoutMs the timeout for the expect operation in milliseconds, infinite if -1 is passed
     * @param matchers  the matchers
     * @return the match result from all the matchers
     * @throws java.io.IOException                          if I/O error occurs
     * @throws java.nio.channels.ClosedByInterruptException if the thread calling this method has been interrupted
     * @deprecated This method is deprecated and will be removed. Use
     * {@link #withTimeout(long, java.util.concurrent.TimeUnit)} to change the timeout for the expect operation.
     */
    @Deprecated
    MultiResult expectIn(int input, long timeoutMs, Matcher<?>... matchers) throws IOException;

    /**
     * Sets the default timeout in the given unit for the expect operations for the returned instance. The timeout for
     * this instance will remain unchanged. Both instances, this and the returned one, will share the same configuration
     * except the default timeout value.
     *
     * @param duration the timeout value
     * @param unit     the time unit
     * @return an Expect instance with a new default timeout.
     * @throws java.lang.IllegalArgumentException if the timeout {@code <= 0}
     */
    Expect withTimeout(long duration, TimeUnit unit);

    /**
     * Sets the default timeout to infinity. The timeout for this instance will remain unchanged. Both instances, this
     * and the returned one, will share the same configuration except the default timeout value.
     *
     * @return an Expect instance with a new default timeout.
     */
    Expect withInfiniteTimeout();

    /**
     * Closes all resources associated with this instance.
     *
     * @throws IOException if I/O error occurs
     */
    @Override
    void close() throws IOException;
}
