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

import static net.sf.expectit.TestUtils.LONG_TIMEOUT;
import static net.sf.expectit.TestUtils.SMALL_TIMEOUT;
import static net.sf.expectit.TestUtils.mockInputStream;
import static net.sf.expectit.echo.EchoAdapters.adapt;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import net.sf.expectit.echo.EchoOutput;
import net.sf.expectit.filter.Filter;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
        assertEquals(((ExpectImpl) expect).getTimeout(), ExpectBuilder.DEFAULT_TIMEOUT_MS);

        try {
            expect.sendBytes("".getBytes());
            fail();
        } catch (NullPointerException ok) {
        }
        builder.withInputs();
        expectIllegalState(builder);

        long[] invalidTimeouts = {0, -1, -2, -100};

        for (long timeout : invalidTimeouts) {
            try {
                builder.withTimeout(timeout, TimeUnit.SECONDS);
                fail("Should throw IllegalArgumentException for the timeout: " + timeout);
            } catch (IllegalArgumentException ok) {
            }
        }
    }

    @Test
    public void testTimeUnit() throws IOException {
        expect = new ExpectBuilder().withInputs(mock(InputStream.class))
                .withTimeout(3, TimeUnit.DAYS).build();
        assertEquals(((AbstractExpectImpl) expect).getTimeout(), TimeUnit.DAYS.toMillis(3));
        assertEquals(((AbstractExpectImpl) expect).getTimeout(), 259200000);
        expect.close();
        expect = new ExpectBuilder().withInputs(mock(InputStream.class))
                .withInfiniteTimeout().build();
        assertEquals(((AbstractExpectImpl) expect).getTimeout(), -1);
        Expect expect2 = expect.withTimeout(10, TimeUnit.SECONDS);
        assertEquals(((AbstractExpectImpl) expect2).getTimeout(), 10000);
        assertEquals(((AbstractExpectImpl) expect).getTimeout(), -1);

        assertEquals(
                ((AbstractExpectImpl) expect.withTimeout(
                        3,
                        TimeUnit.MILLISECONDS)).getTimeout(), 3);
        try {
            expect.withTimeout(-10, TimeUnit.DAYS);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
        }
        assertEquals(((AbstractExpectImpl) expect.withInfiniteTimeout()).getTimeout(), -1);
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

    // remove test when the deprecated method is removed
    @Deprecated
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
    public void testExceptionOnFailure() throws Exception {
        ExpectBuilder builder = new ExpectBuilder();
        MockInputStream input = mockInputStream("abc");
        builder.withInputs(input.getStream());
        builder.withOutput(mock(OutputStream.class));
        builder.withTimeout(SMALL_TIMEOUT, TimeUnit.MILLISECONDS);
        builder.withExceptionOnFailure();
        expect = builder.build();

        Matcher<?> mock = mock(Matcher.class);
        Result result = mock(Result.class);
        when(result.isSuccessful()).thenReturn(false);
        when(mock.matches(anyString(), eq(false))).thenReturn(result);
        try {
            expect.expect(mock);
            fail();
        } catch (final ExpectIOException ignore) {
            assertTrue(ignore.getMessage().contains("fail"));
            assertThat(ignore.getInputBuffer(), is("abc"));
        }
    }

    @Test
    public void testCharset() throws IOException, InterruptedException {
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
        //noinspection deprecation
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("hello")).isSuccessful());
    }

    private void configureMockInputStream(InputStream in, final byte[] bytes)
            throws IOException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        when(in.read(any(byte[].class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        latch.countDown();
                        if (mockInputReadCalled) {
                            return -1;
                        }
                        //noinspection SuspiciousSystemArraycopy
                        System.arraycopy(bytes, 0, invocation.getArguments()[0], 0, bytes.length);
                        mockInputReadCalled = true;
                        return bytes.length;
                    }
                });
        latch.await();
    }

    @Test
    public void testFilters() throws IOException, InterruptedException {
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

        when(filter2.afterAppend(any(StringBuilder.class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        ((StringBuilder) invocation.getArguments()[0]).append("01234");
                        return true;
                    }
                });
        expect = builder.build();

        String inputStr = "testFilter";
        configureMockInputStream(in, inputStr.getBytes());
        //noinspection deprecation
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("y")).isSuccessful());
        verify(filter1).beforeAppend(eq(inputStr), any(StringBuilder.class));
        verify(filter2).beforeAppend(eq("xxx"), any(StringBuilder.class));
        verify(filter3).beforeAppend(eq("yyy"), any(StringBuilder.class));

        verify(filter1).afterAppend(argThat(new StringBuilderArgumentMatcher("yy01234")));
        verify(filter2).afterAppend(any(StringBuilder.class));
        verify(filter3, never()).afterAppend(any(StringBuilder.class));
        // filters are not applied for the echo stream
        assertEquals("testFilter", echo.toString());
    }

    @SuppressWarnings("deprecation")
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
    public void expectEchoOutput() throws Exception {
        ExpectBuilder builder = new ExpectBuilder();
        final EchoOutput echoMock = mock(EchoOutput.class);
        final EchoOutput echo = new MockedSyncEchoOutput(echoMock);
        String inputText = "input";
        MockInputStream input = mockInputStream(inputText);
        String inputText2 = "number2";
        MockInputStream input2 = mockInputStream(inputText2);
        builder.withInputs(input.getStream(), input2.getStream());
        builder.withEchoOutput(echo);
        builder.withOutput(mock(OutputStream.class));
        expect = builder.build();
        input.waitUntilReady();
        input2.waitUntilReady();

        String sentText = "sentText";
        expect.sendLine(sentText);
        String sentTextLine = sentText + "\n";
        verify(echoMock).onSend(sentTextLine);
        input.push(inputText);
        input2.push(inputText2);
        input.waitUntilReady();
        input2.waitUntilReady();
        //noinspection deprecation
        assertTrue(expect.expect(LONG_TIMEOUT, times(2, contains(inputText))).isSuccessful());
        //noinspection deprecation
        assertTrue(expect.expectIn(1, SMALL_TIMEOUT, contains(inputText2)).isSuccessful());
        verify(echoMock, Mockito.timeout((int) SMALL_TIMEOUT).times(2)).onReceive(0, inputText);
        verify(echoMock, Mockito.timeout((int) SMALL_TIMEOUT).times(2)).onReceive(1, inputText2);
    }

    @Test
    public void expectEchoOutput2() throws Exception {
        ExpectBuilder builder = new ExpectBuilder();
        Appendable out = mock(Appendable.class);
        Appendable in1 = mock(Appendable.class);
        Appendable in2 = mock(Appendable.class);
        String inputText = "input";
        MockInputStream input = mockInputStream(inputText);
        String inputText2 = "number2";
        MockInputStream input2 = mockInputStream(inputText2);
        builder.withInputs(input.getStream(), input2.getStream());
        builder.withEchoOutput(out);
        builder.withOutput(mock(OutputStream.class));
        expect = builder.build();
        input.waitUntilReady();
        input2.waitUntilReady();

        String sentText = "sentText";
        expect.sendLine(sentText);
        String sentTextLine = sentText + "\n";
        verify(out).append(sentTextLine);

        builder.withEchoInput(in1);
        expect.close();
        input = mockInputStream(inputText);
        input2 = mockInputStream(inputText2);
        builder.withInputs(input.getStream(), input2.getStream());
        expect = builder.build();
        input.waitUntilReady();
        input2.waitUntilReady();

        input.push(inputText);
        input2.push(inputText2);
        //noinspection deprecation
        assertTrue(expect.expect(LONG_TIMEOUT, times(2, contains(inputText))).isSuccessful());
        //noinspection deprecation
        assertTrue(expect.expectIn(1, SMALL_TIMEOUT, contains(inputText2)).isSuccessful());

        try {
            verify(in1, Mockito.times(2)).append(inputText);
        } catch (AssertionError ignore) {
            verify(in1, Mockito.times(1)).append(inputText + inputText);
        }

        reset(in1);
        builder.withEchoInput(in1, in2);
        expect.close();

        input = mockInputStream(inputText);
        input2 = mockInputStream(inputText2);
        builder.withInputs(input.getStream(), input2.getStream());
        expect = builder.build();
        input.waitUntilReady();
        input2.waitUntilReady();
        //noinspection deprecation
        assertTrue(expect.expectIn(1, LONG_TIMEOUT, contains(inputText2)).isSuccessful());
        verify(in2).append(inputText2);
        verify(in1).append(inputText);

        try {
            builder.withEchoInput(in1, in2, in1);
            builder.build();
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testEchoOutputAutoFlush() throws Exception {
        ExpectBuilder builder = new ExpectBuilder();
        MockInputStream input1 = mockInputStream("abc");
        MockInputStream input2 = mockInputStream("def");
        builder.withInputs(input1.getStream(), input2.getStream());
        final StringWriter stringWriter1 = new StringWriter();
        final StringWriter stringWriter2 = new StringWriter();
        final BufferedWriter echo1 = new BufferedWriter(stringWriter1);
        final BufferedWriter echo2 = new BufferedWriter(stringWriter2);
        builder.withEchoInput(echo1, echo2);
        final StringWriter echoOutput = mock(StringWriter.class);
        builder.withEchoOutput(echoOutput);
        expect = builder.build();
        input1.waitUntilReady();
        input2.waitUntilReady();
        assertTrue(expect.expectIn(0, contains("ab")).isSuccessful());
        assertTrue(expect.expectIn(1, contains("def")).isSuccessful());
        echo1.flush();
        assertThat(stringWriter1.toString(), is("abc"));
        assertTrue(stringWriter2.toString().isEmpty());
        verify(echoOutput, never()).flush();
        expect.close();
        verify(echoOutput, never()).flush();
    }

    @Test
    public void testEchoOutputAutoFlush2() throws Exception {
        ExpectBuilder builder = new ExpectBuilder();
        builder.withAutoFlushEcho(true);
        MockInputStream input1 = mockInputStream("abc");
        builder.withInputs(input1.getStream());
        final StringWriter echoOutput = mock(StringWriter.class);
        builder.withEchoOutput(echoOutput);
        final StringWriter stringWriter1 = new StringWriter();
        final BufferedWriter echo1 = new BufferedWriter(stringWriter1);
        builder.withOutput(new ByteArrayOutputStream());
        builder.withEchoInput(echo1);
        expect = builder.build();
        expect.sendLine("xyz");
        input1.waitUntilReady();
        assertTrue(expect.expect(contains("ab")).isSuccessful());

        assertThat(stringWriter1.toString(), is("abc"));
        verify(echoOutput).flush();
        reset(echoOutput);
        expect.close();
        verify(echoOutput, only()).flush();
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 5000)
    public void testExpectMethods() throws Exception {
        ExpectBuilder builder = new ExpectBuilder();
        String inputText1 = "input1";
        String inputText2 = "input2";
        MockInputStream input1 = mockInputStream(inputText1);
        MockInputStream input2 = mockInputStream(inputText2);
        builder.withInputs(input1.getStream(), input2.getStream());
        expect = builder.build();
        input1.waitUntilReady();
        input2.waitUntilReady();

        assertFalse(expect.expectIn(1, SMALL_TIMEOUT, contains("input1")).isSuccessful());
        assertFalse(expect.expectIn(0, SMALL_TIMEOUT, contains("input2")).isSuccessful());
        assertTrue(expect.expect(SMALL_TIMEOUT, contains("input1")).isSuccessful());
        assertTrue(expect.expectIn(1, SMALL_TIMEOUT, contains("input2")).isSuccessful());
        assertFalse(
                expect.expect(SMALL_TIMEOUT, contains("input2"), contains("input1"))
                        .isSuccessful());
        input1.push(inputText1);
        assertTrue(
                expect.expect(SMALL_TIMEOUT, contains("input"), contains("input"))
                        .isSuccessful());
    }

    @Test(timeout = 10000)
    public void testClosedByInterruptExceptionIfInterrupted() throws Exception {
        MockInputStream input = mockInputStream("input");
        expect = new ExpectBuilder().withInputs(input.getStream()).build();
        input.waitUntilReady();
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch exceptionThrown = new CountDownLatch(1);
        final AtomicReference<IOException> exceptionRef = new AtomicReference<IOException>();

        Thread expectThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            started.countDown();
                            //noinspection deprecation
                            assertFalse(
                                    expect.expect(ExpectImpl.INFINITE_TIMEOUT, contains("a"))
                                            .isSuccessful());
                            fail();
                        } catch (IOException e) {
                            exceptionRef.set(e);
                            exceptionThrown.countDown();
                        }
                    }
                });

        try {
            expectThread.start();
            assertTrue(started.await(LONG_TIMEOUT, TimeUnit.MILLISECONDS));
            // make sure the thread is ready to receive interrupt
            Thread.sleep(SMALL_TIMEOUT);
            expectThread.interrupt();
            expectThread.join();
            assertTrue(exceptionThrown.await(LONG_TIMEOUT, TimeUnit.MILLISECONDS));
            //noinspection ThrowableResultOfMethodCallIgnored
            assertNotNull(exceptionRef.get() instanceof ClosedByInterruptException);
        } finally {
            expectThread.interrupt();
            expectThread.join();
        }
    }

    @Test(timeout = 10000)
    public void testExpectWithInfiniteTimeoutWaiting() throws Exception {
        MockInputStream input = mockInputStream("input");
        expect = new ExpectBuilder().withInputs(input.getStream()).build();

        Callable<Void> expectCallable = new Callable<Void>() {
            @Override
            public Void call() throws IOException {
                //noinspection deprecation
                assertFalse(
                        expect.expect(ExpectImpl.INFINITE_TIMEOUT, contains("a"))
                                .isSuccessful());
                fail();
                return null;
            }
        };
        input.waitUntilReady();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> result = executor.submit(expectCallable);
        try {
            try {
                result.get(LONG_TIMEOUT, TimeUnit.MILLISECONDS);
                fail();
            } catch (TimeoutException ok) {
            }

            Thread.sleep(SMALL_TIMEOUT * 2);
            assertFalse(result.isCancelled());
            assertFalse(result.isDone());
        } finally {
            result.cancel(true);
            executor.shutdownNow();
        }
    }

    @Test(timeout = 10000)
    public void testExpectWithInfiniteTimeoutConsequentlyPassing() throws Exception {
        final String inputString = "input";
        final MockInputStream mockInputStream = mockInputStream(inputString);
        expect = new ExpectBuilder().withInputs(mockInputStream.getStream()).build();
        mockInputStream.waitUntilReady();
        final int iterations = 5;
        final CountDownLatch successCounter = new CountDownLatch(iterations);

        Callable<Void> expectCallable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (int i = 0; i < iterations; i++) {
                    mockInputStream.push(inputString);
                    //noinspection deprecation
                    assertTrue(
                            expect.expect(ExpectImpl.INFINITE_TIMEOUT, contains(inputString))
                                    .isSuccessful());
                    successCounter.countDown();
                }
                return null;
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> result = executor.submit(expectCallable);
        try {
            assertTrue(successCounter.await(SMALL_TIMEOUT * iterations, TimeUnit.MILLISECONDS));
            assertFalse(result.isCancelled());
        } finally {
            result.cancel(true);
            executor.shutdownNow();
        }
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
        verify(mock).write("\n".getBytes());
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

    @Test
    public void testBufferSize() throws IOException {
        ExpectBuilder builder = new ExpectBuilder();
        InputStream mock = mock(InputStream.class);
        builder.withInputs(mock);
        builder.withOutput(mock(OutputStream.class));
        try {
            builder.withBufferSize(0);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {
        }
        builder.withBufferSize(20);
        expect = builder.build();
        verify(mock, timeout((int) LONG_TIMEOUT).atLeastOnce())
                .read(
                        argThat(
                                new ArgumentMatcher<byte[]>() {

                                    @Override
                                    public boolean matches(Object o) {
                                        return o instanceof byte[] && ((byte[]) o).length == 20;
                                    }
                                }));
    }

    @Test
    public void testCombineInputs() throws Exception {
        ExpectBuilder builder = new ExpectBuilder();
        String inputText1 = "abc";
        String inputText2 = "def";
        MockInputStream input1 = mockInputStream(inputText1);
        MockInputStream input2 = mockInputStream(inputText2);
        builder.withInputs(input1.getStream(), input2.getStream());
        builder.withCombineInputs(true);
        builder.withTimeout(SMALL_TIMEOUT, TimeUnit.MILLISECONDS);
        expect = builder.build();
        input1.waitUntilReady();
        input2.waitUntilReady();
        assertTrue(expect.expect(Matchers.allOf(contains("def"), contains("abc"))).isSuccessful());
        assertFalse(expect.expectIn(1, contains("def")).isSuccessful());
    }

    private static class MockedSyncEchoOutput implements EchoOutput {
        private final EchoOutput echoMock;

        public MockedSyncEchoOutput(final EchoOutput echoMock) {this.echoMock = echoMock;}

        @Override
        public void onReceive(final int input, final String string) throws
                IOException {
            synchronized (echoMock) {
                echoMock.onReceive(input, string);
            }
        }

        @Override
        public void onSend(final String string) throws IOException {
            synchronized (echoMock) {
                echoMock.onSend(string);
            }
        }
    }
}
