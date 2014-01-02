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

import net.sf.expectit.matcher.Matcher;

import java.io.Closeable;
import java.io.IOException;

/**
 * The interface to access expect operations on input data.
 */
public interface Expect extends Closeable {
    /**
     * Sends the string to the output stream and flushes the output.
     * <p/>
     * The string is converted to bytes using {@code charset} set in the {@link net.java.expectit.ExpectBuilder}.
     *
     * @param string the string to be sent
     * @return this
     * @throws IOException if I/O error occurs
     */
    Expect send(String string) throws IOException;

    /**
     * Appends a new line character and calls {@link #send(String)} method.
     *
     * @param string the string to be sent
     * @return this
     * @throws IOException if I/O error occurs
     * @see #send(String)
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
     * Awaits until the given matcher matches against the first input stream using the default timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.java.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     *
     * @param <R>     the matcher type
     * @param matcher the mather
     * @return the match result
     * @throws java.io.IOException if I/O error occurs, for example, the input is closed
     */
    <R extends Result> R expect(Matcher<R> matcher) throws IOException;

    /**
     * Awaits until the given matcher matches the first input stream using the given timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.java.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     *
     * @param <R>       the matcher type
     * @param timeoutMs the timeout for the expect operation in milliseconds
     * @param matcher   the mather
     * @return the match result
     * @throws java.io.IOException if I/O error occurs, for example, the input is closed
     */
    <R extends Result> R expect(long timeoutMs, Matcher<R> matcher) throws IOException;

    /**
     * Awaits until the given matcher matches against the given input stream using the default timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.java.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     *
     * @param <R>     the matcher type
     * @param input   the index of the input. if the index is outside of the boundaries, a runtime exception will be
     *                thrown
     * @param matcher the mather
     * @return the match result
     * @throws java.io.IOException if I/O error occurs, for example, the input is closed
     */
    <R extends Result> R expectIn(int input, Matcher<R> matcher) throws IOException;

    /**
     * Awaits until the given matcher matches against the given input stream using the given timeout.
     * <p/>
     * The method throws an {@link java.lang.AssertionError} if {@link net.java.expectit.ExpectBuilder#errorOnTimeout}
     * is set.
     *
     * @param <R>       the matcher type
     * @param input     the index of the input. if the index is outside of the boundaries, a runtime exception will be
     *                  thrown
     * @param timeoutMs the timeout for the expect operation in milliseconds
     * @param matcher   the mather
     * @return the match result
     * @throws java.io.IOException if I/O error occurs, for example, the input is closed
     */
    <R extends Result> R expectIn(int input, long timeoutMs, Matcher<R> matcher) throws IOException;
}
