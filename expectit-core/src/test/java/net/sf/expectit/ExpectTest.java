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

import org.junit.Assert;
import net.sf.expectit.echo.EchoOutput;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.matcher.Matcher;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.Utils.*;
import static net.sf.expectit.echo.EchoAdapters.adapt;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for various expect builder parameters
 */
public class ExpectTest {
    private Expect expect;
    private boolean mockInputReadCalled;

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
        expectIllegalState(builder);

        builder.withInputs(mock(InputStream.class));
        expect = builder.build();
        assertEquals(((ExpectImpl) expect).getTimeout(), 30000);

        try {
            expect.sendBytes("".getBytes());
            fail();
        } catch (NullPointerException ok) {
        }
        builder.withInputs();
        expectIllegalState(builder);

        try {
            builder.withTimeout(-1, TimeUnit.SECONDS);
            fail();
        } catch (IllegalArgumentException ok) {
        }
    }

    @Test
    public void testTimeUnit() throws IOException {
        expect = new ExpectBuilder().withInputs(mock(InputStream.class))
                .withTimeout(3, TimeUnit.DAYS).build();
        assertEquals(((AbstractExpectImpl) expect).getTimeout(), 259200000);
        expect = new ExpectBuilder().withInputs(mock(InputStream.class))
                .withInfinitiveTimeout().build();
        assertEquals(((AbstractExpectImpl) expect).getTimeout(), -1);
        Expect expect2 = expect.withTimeout(10, TimeUnit.SECONDS);
        assertEquals(((AbstractExpectImpl) expect2).getTimeout(), 10000);
        assertEquals(((AbstractExpectImpl) expect).getTimeout(), -1);

        assertEquals(((AbstractExpectImpl) expect.withTimeout(3, TimeUnit.MILLISECONDS)).getTimeout(), 3);
        try {
            expect.withTimeout(-10, TimeUnit.DAYS);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
        }
        assertEquals(((AbstractExpectImpl) expect.withInfinitiveTimeout()).getTimeout(), -1);
    }

    private void expectIllegalState(ExpectBuilder builder) throws IOException {
        try {
            builder.build();
            fail();
        } catch (IllegalStateException ok) {
        }
    }

    @Test
    public void testNumberOfInputs() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream mock1 = mock(InputStream.class);
        InputStream mock2 = mock(InputStream.class);
        builder.withInputs(mock1, mock2);
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
        builder.withTimeout(SMALL_TIMEOUT, TimeUnit.MILLISECONDS);
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
        expect = builder.build();

        String testString = "hello";
        final byte[] bytes = testString.getBytes(charset);
        expect.send(testString);
        verify(out).write(bytes);
        configureMockInputStream(in, bytes);
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("hello")).isSuccessful());
    }

    private void configureMockInputStream(InputStream in, final byte[] bytes) throws IOException {
        when(in.read(any(byte[].class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (mockInputReadCalled) {
                    return -1;
                }
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(bytes, 0, invocation.getArguments()[0], 0, bytes.length);
                mockInputReadCalled = true;
                return bytes.length;
            }
        });
    }

    @Test
    public void testFilters() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream in = mock(InputStream.class);
        StringBuilder echo = new StringBuilder();
        builder.withInputs(in).withEchoOutput(adapt(echo));
        Filter filter1 = mock(Filter.class);
        Filter filter2 = mock(Filter.class);
        Filter filter3 = mock(Filter.class);
        builder.withInputFilters(filter1, filter2, filter3);
        when(filter1.beforeAppend(anyString(), any(StringBuilder.class))).thenReturn("xxx");
        when(filter2.beforeAppend(eq("xxx"), any(StringBuilder.class))).thenReturn("yyy");

        when(filter2.afterAppend(any(StringBuilder.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((StringBuilder) invocation.getArguments()[0]).append("01234");
                return true;
            }
        });
        expect = builder.build();

        String inputStr = "testFilter";
        configureMockInputStream(in, inputStr.getBytes());
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("y")).isSuccessful());
        verify(filter1).beforeAppend(eq(inputStr), any(StringBuilder.class));
        verify(filter2).beforeAppend(eq("xxx"), any(StringBuilder.class));
        verify(filter3).beforeAppend(eq("yyy"), any(StringBuilder.class));

        verify(filter1).afterAppend(argThat(new StringBuilderArgumentMatcher("yy01234")));
        verify(filter2).afterAppend(any(StringBuilder.class));
        verify(filter3, never()).afterAppend(any(StringBuilder.class));
        assertEquals("yyy", echo.toString());
    }

    @Test
    public void testIOException() throws IOException, InterruptedException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream in = mock(InputStream.class);
        when(in.read(any(byte[].class))).thenThrow(new EOFException(""));
        builder.withInputs(in);
        expect = builder.build();

        try {
            expect.expect(LONG_TIMEOUT, contains("test"));
            expect.expect(SMALL_TIMEOUT, contains("test"));
            fail();
        } catch (IOException ok) {
        }
    }

    @Test
    public void expectEchoOutput() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        EchoOutput echo = mock(EchoOutput.class);
        String inputText = "input";
        InputStream input = mockInputStream(SMALL_TIMEOUT, inputText);
        String inputText2 = "number2";
        InputStream input2 = mockInputStream(SMALL_TIMEOUT, inputText2);
        builder.withInputs(input, input2);
        builder.withEchoOutput(echo);
        builder.withOutput(mock(OutputStream.class));
        expect = builder.build();

        String sentText = "sentText";
        expect.sendLine(sentText);
        String sentTextLine = sentText + System.getProperty("line.separator");
        verify(echo).onSend(sentTextLine);

        reset(echo);
        assertTrue(expect.expect(LONG_TIMEOUT, times(2, contains(inputText))).isSuccessful());
        assertTrue(expect.expectIn(1, SMALL_TIMEOUT, contains(inputText2)).isSuccessful());
        verify(echo, Mockito.times(2)).onReceive(0, inputText);
        verify(echo).onReceive(eq(1), anyString());
    }

    @Test(timeout = 5000)
    public void testExpectMethods() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        String inputText1 = "input1";
        String inputText2 = "input2";
        InputStream input1 = mockInputStream(SMALL_TIMEOUT, inputText1);
        InputStream input2 = mockInputStream(SMALL_TIMEOUT, inputText2);
        builder.withInputs(input1, input2);
        expect = builder.build();

        assertFalse(expect.expectIn(1, SMALL_TIMEOUT, contains("input1")).isSuccessful());
        assertFalse(expect.expectIn(0, SMALL_TIMEOUT, contains("input2")).isSuccessful());
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("input1")).isSuccessful());
        assertTrue(expect.expectIn(1, SMALL_TIMEOUT, contains("input2")).isSuccessful());
        assertFalse(expect.expect(SMALL_TIMEOUT, contains("input2"), contains("input1")).isSuccessful());
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("input"), contains("input")).isSuccessful());
    }

    @Test
    public void testCustomLineSeparator() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        builder.withLineSeparator("XYZ");
        builder.withInputs(mock(InputStream.class));
        OutputStream mock = mock(OutputStream.class);
        builder.withOutput(mock);
        expect = builder.build();

        expect.sendLine("ABZ");
        verify(mock).write(("ABZ" + "XYZ").getBytes());
        expect.sendLine();
        verify(mock).write("XYZ".getBytes());
        expect.send("XXx");
        verify(mock).write(("XXx").getBytes());
        expect.sendBytes("fff".getBytes());
        verify(mock).write(("fff").getBytes());
    }

    @Test
    public void testLineSeparator() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        builder.withInputs(mock(InputStream.class));
        OutputStream mock = mock(OutputStream.class);
        builder.withOutput(mock);
        expect = builder.build();
        expect.sendLine();
        verify(mock).write((System.getProperty("line.separator")).getBytes());
    }

    private static class StringBuilderArgumentMatcher extends ArgumentMatcher<StringBuilder> {
        private final String contents;

        private StringBuilderArgumentMatcher(String contents) {
            this.contents = contents;
        }

        @Override
        public boolean matches(Object argument) {
            return argument.toString().equals(contents);
        }
    }
}
