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

import net.sf.expectit.filter.Filter;
import net.sf.expectit.matcher.Matcher;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static net.sf.expectit.TestConstants.LONG_TIMEOUT;
import static net.sf.expectit.TestConstants.SMALL_TIMEOUT;
import static net.sf.expectit.matcher.Matchers.contains;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for various expect builder parameters
 */
public class ExpectBuilderTest {
    private Expect expect;

    @After
    public void cleanup() throws IOException {
        if (expect != null) {
            expect.close();
            expect = null;
        }
    }

    @Test
    public void testRequiredParameters() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        assertIllegalArgumentException(builder);
        InputStream mock = mock(InputStream.class);
        builder.withInputs(mock);
        assertIllegalArgumentException(builder);
        builder.withOutput(mock(OutputStream.class));
        expect = builder.build();
        assertNotNull(expect);
        builder.withInputs();
        assertIllegalArgumentException(builder);
        try {
            builder.withTimeout(-1);
            fail();
        } catch (IllegalArgumentException ok) {

        }
    }

    private void assertIllegalArgumentException(ExpectBuilder builder) throws IOException {
        try {
            builder.build();
            fail();
        } catch (IllegalArgumentException ok) {
        }
    }

    @Test
    public void testNumberOfInputs() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream mock1 = mock(InputStream.class);
        InputStream mock2 = mock(InputStream.class);
        builder.withInputs(mock1, mock2);
        builder.withOutput(mock(OutputStream.class));
        Matcher<?> mock = mock(Matcher.class);
        Result result = mock(Result.class);
        when(result.isSuccessful()).thenReturn(true);
        when(mock.matches(anyString(), eq(false))).thenReturn(result);
        expect = builder.build();
        assertTrue(expect.expectIn(0, mock).isSuccessful());
        assertTrue(expect.expectIn(1, mock).isSuccessful());
        try {
            expect.expectIn(2, mock);
            fail();
        } catch (ArrayIndexOutOfBoundsException ok) {
        }
    }

    @Test
    public void testErrorOnTimeout() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        builder.withInputs(mock(InputStream.class));
        builder.withOutput(mock(OutputStream.class));
        builder.withTimeout(SMALL_TIMEOUT);
        builder.withErrorOnTimeout(true);
        expect = builder.build();
        Matcher<?> mock = mock(Matcher.class);
        Result result = mock(Result.class);
        when(result.isSuccessful()).thenReturn(false);
        when(mock.matches(anyString(), eq(false))).thenReturn(result);
        try {
            expect.expect(mock);
            fail();
        } catch (AssertionError ok) {
        }
    }

    @Test
    public void testCharset() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream in = mock(InputStream.class);
        builder.withInputs(in);
        OutputStream out = mock(OutputStream.class);
        builder.withOutput(out);
        Charset charset = Charset.forName("UTF-16");
        builder.withCharset(charset);
        String testString = "hello";
        final byte[] bytes = testString.getBytes(charset);
        expect = builder.build();
        expect.send(testString);
        verify(out).write(bytes);
        configureMockInputStream(in, bytes);
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("hello")).isSuccessful());
    }

    private void configureMockInputStream(InputStream in, final byte[] bytes) throws IOException {
        when(in.read(any(byte[].class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(bytes, 0, invocation.getArguments()[0], 0, bytes.length);
                return bytes.length;
            }
        });
    }

    @Test
    public void testFilter() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream in = mock(InputStream.class);
        builder.withInputs(in);
        builder.withOutput(mock(OutputStream.class));
        Filter filter = mock(Filter.class);
        builder.withInputFilter(filter);
        when(filter.filter(anyString(), any(StringBuilder.class))).thenReturn("xxx");
        expect = builder.build();
        configureMockInputStream(in, "test".getBytes());
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("xxx")).isSuccessful());
    }

    @Test
    public void testIOException() throws IOException, InterruptedException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream in = mock(InputStream.class);
        when(in.read(any(byte[].class))).thenThrow(new EOFException(""));
        builder.withInputs(in);
        builder.withOutput(mock(OutputStream.class));
        expect = builder.build();
        try {
            expect.expect(LONG_TIMEOUT, contains("test"));
            expect.expect(SMALL_TIMEOUT, contains("test"));
            fail();
        } catch (IOException ok) {
        }
    }
    /*@Test
    public void expectEchoOutput() {

    }*/

}
