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

import static net.sf.expectit.ExpectBuilder.DEFAULT_BUFFER_SIZE;
import static net.sf.expectit.TestUtils.LONG_TIMEOUT;
import static net.sf.expectit.TestUtils.SMALL_TIMEOUT;
import static net.sf.expectit.matcher.Matchers.allOf;
import static net.sf.expectit.matcher.Matchers.anyOf;
import static net.sf.expectit.matcher.Matchers.anyString;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.eof;
import static net.sf.expectit.matcher.Matchers.exact;
import static net.sf.expectit.matcher.Matchers.matches;
import static net.sf.expectit.matcher.Matchers.regexp;
import static net.sf.expectit.matcher.Matchers.sequence;
import static net.sf.expectit.matcher.Matchers.startsWith;
import static net.sf.expectit.matcher.Matchers.times;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for basic single operation matchers.
 *
 * @author Alexey Gavrilov
 */
public class MatcherTest {
    private SingleInputExpect input;
    private ExecutorService executor;
    private MockInputStream mock;
    private final String text = "a1b2c3_";

    /**
     * Creates a mock input stream which send some data every SMALL_TIMEOUT ms.
     */
    @Before
    public void setup() throws Exception {
        mock = TestUtils.mockInputStream(text);
        final Pipe pipe = Pipe.open();
        input = new SingleInputExpect(
                pipe.source(),
                pipe.sink(),
                mock.getStream(),
                Charset.defaultCharset(),
                null,
                null,
                DEFAULT_BUFFER_SIZE,
                false);
        executor = Executors.newSingleThreadExecutor();
        input.start(executor);
        mock.waitUntilReady();
    }

    @After
    public void cleanup() throws IOException {
        input.stop();
        executor.shutdown();
    }

    /**
     * Basic expect string tests
     */
    @Test
    public void testExpectString() throws IOException, InterruptedException {
        Result result = input.expect(LONG_TIMEOUT, contains("b2"));
        assertTrue(result.isSuccessful());
        assertEquals(result.groupCount(), 0);
        assertEquals(result.end(), 4);
        assertEquals(result.start(), 2);
        assertEquals(result.group(), "b2");
        assertEquals(result.end(0), 4);
        assertEquals(result.start(0), 2);
        assertEquals(result.group(0), "b2");
        assertEquals(result.getBefore(), "a1");
        checkIndexOutOfBound(result, 1);

        StringBuilder buffer = input.getBuffer();
        result = input.expect(SMALL_TIMEOUT, contains("cx"));
        // no changes in the buffer if matching fails
        assertEquals(buffer.length(), input.getBuffer().length());
        assertFalse(result.isSuccessful());
        checkIndexOutOfBound(result, 1);
        checkIllegalState(result);
        checkIllegalState(result, 0);
        assertEquals(result.groupCount(), 0);
        mock.push(text);
        // expecting that mocking input stream goes circles
        assertTrue(input.expect(LONG_TIMEOUT, contains("c3_a")).isSuccessful());
        buffer = input.getBuffer();
        // check that buffer gets updated
        assertTrue(buffer.toString().startsWith("1b2c3"));
    }

    /**
     * Check for null, empty and invalid inputs.
     */
    @Test
    public void testEmptyStringAndNull() throws IOException {
        Result result = input.expect(LONG_TIMEOUT, contains(""));
        assertTrue(result.isSuccessful());
        try {
            input.expect(LONG_TIMEOUT, contains(null));
            fail();
        } catch (NullPointerException ok) {
        }

        result = input.expect(LONG_TIMEOUT, regexp(""));
        assertTrue(result.isSuccessful());

        result = input.expect(LONG_TIMEOUT, matches(""));
        assertTrue(result.isSuccessful());
        try {
            input.expect(LONG_TIMEOUT, regexp((String) null));
            fail();
        } catch (NullPointerException ok) {
        }
        try {
            input.expect(LONG_TIMEOUT, regexp((Pattern) null));
            fail();
        } catch (NullPointerException ok) {
        }
        try {
            input.expect(LONG_TIMEOUT, regexp("^(^^^"));
            fail();
        } catch (PatternSyntaxException ok) {
        }
    }

    /**
     * Tests for regexp matcher
     */
    @Test
    public void testExpectRegexp() throws IOException, InterruptedException {
        Result result = input.expect(LONG_TIMEOUT, regexp(".b.c"));
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "a");
        assertEquals(result.groupCount(), 0);
        assertEquals(result.end(), 5);
        assertEquals(result.start(), 1);
        assertEquals(result.group(), "1b2c");
        assertEquals(result.end(0), 5);
        assertEquals(result.start(0), 1);
        assertEquals(result.group(0), "1b2c");
        checkIndexOutOfBound(result, 1);
        assertTrue(input.getBuffer().toString().startsWith("3_"));
        assertEquals(result.getInput(), result.getBefore()
                + result.group(0)
                + input.getBuffer().toString());

        result = input.expect(SMALL_TIMEOUT, regexp("a.z.c"));
        assertFalse(result.isSuccessful());
        checkIllegalState(result);
        assertEquals(result.getInput(), "3_");

        mock.push(text);
        result = input.expect(LONG_TIMEOUT, regexp("3_([^_]*)c3"));
        assertEquals(result.group(0), "3_a1b2c3");
        assertEquals(result.group(1), "a1b2");

        mock.push(text);
        result = input.expect(LONG_TIMEOUT, regexp("a(.)b(.)c(.)"));
        assertEquals(result.group(1), "1");
        assertEquals(result.group(3), "3");
        assertEquals(result.groupCount(), 3);
        assertEquals(result.end(), 7);

        mock.push(text);
        // sanity check for Pattern instance
        result = input.expect(LONG_TIMEOUT, regexp(Pattern.compile("a(.*)")));
        assertTrue(result.isSuccessful());
        assertFalse(result.canStopMatching());
    }

    /**
     * Tests for exact match.
     */
    @Test
    public void testExpectMatch() throws IOException, InterruptedException {
        mock.push(text);
        Result result = input.expect(LONG_TIMEOUT, matches(".*a1b2.*"));
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "");
        checkIndexOutOfBound(result, 1);
        assertEquals(result.groupCount(), 0);
        assertEquals(input.getBuffer().length(), 0);
        mock.push(text);
        //await for some data
        input.expect(LONG_TIMEOUT, contains("_a"));
        mock.push(text);
        assertTrue(input.expect(LONG_TIMEOUT, contains("_a")).isSuccessful());

        result = input.expect(SMALL_TIMEOUT, matches("a(.)b(.)c(.)"));
        assertFalse(result.isSuccessful());
        checkIllegalState(result);
        checkIndexOutOfBound(result, 1);
        String buffer = input.getBuffer().toString();
        assertTrue(buffer, buffer.startsWith("1b2c3_"));

        result = input.expect(LONG_TIMEOUT, matches("(.*)c(.).*"));
        assertEquals(result.start(), 0);
        assertEquals(result.end(), buffer.length());
        assertEquals(result.group(), buffer);
        assertEquals(result.getBefore(), "");
        assertEquals(result.groupCount(), 2);
        assertEquals(result.group(2), "3");
        checkIndexOutOfBound(result, 3);

        mock.push(text);
        // sanity check for Pattern instance
        result = input.expect(LONG_TIMEOUT, matches(Pattern.compile("^a(.*)")));
        assertTrue(result.isSuccessful());
        assertEquals(result.group(1), "1b2c3_");
    }

    /**
     * Tests for the 'allOf' matcher
     */
    @Test
    public void testAllOf() throws IOException, InterruptedException {
        MultiResult result = input.expect(LONG_TIMEOUT, allOf(contains("b")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 2);
        assertEquals(result.end(), 3);
        mock.push(text);
        result = input.expect(LONG_TIMEOUT, allOf(contains("a"), contains("b"), regexp("a1")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 6);
        assertEquals(result.end(), 7);
        for (Result r : result.getResults()) {
            assertTrue(r.isSuccessful());
        }
        assertEquals(result.getResults().size(), 3);
        assertEquals(result.getResults().get(1).start(), 6);
        assertEquals(result.getResults().get(1).end(), 7);
        assertEquals(result.getResults().get(2).start(), 4);
        assertEquals(result.getResults().get(2).end(), 6);

        mock.push(text);
        // one negative
        result = input.expect(SMALL_TIMEOUT, allOf(contains("a"), contains("b"), regexp("XXX")));
        assertFalse(result.isSuccessful());
        assertEquals(result.getResults().size(), 3);
        assertEquals(result.getResults().get(0).start(), 4);
        assertEquals(result.getResults().get(0).end(), 5);
        assertTrue(result.getResults().get(1).isSuccessful());
        assertFalse(result.getResults().get(2).isSuccessful());

        // all negative
        result = input.expect(SMALL_TIMEOUT, allOf(matches("vv"), regexp("XXX")));
        for (Result r : result.getResults()) {
            assertFalse(r.isSuccessful());
        }

        // same end position: the first wins
        result = input.expect(LONG_TIMEOUT, allOf(contains("a"), contains("b"), regexp("a(.*)")));
        assertTrue(result.isSuccessful());
        assertEquals(result.groupCount(), 1);

        try {
            input.expect(LONG_TIMEOUT, allOf());
            fail();
        } catch (IllegalArgumentException ok) {
        }
        try {
            input.expect(LONG_TIMEOUT, allOf((Matcher<?>[]) null));
            fail();
        } catch (NullPointerException ok) {
        }
    }

    @Test
    public void testAnyOf() throws IOException, InterruptedException {
        try {
            input.expect(LONG_TIMEOUT, anyOf());
            fail();
        } catch (IllegalArgumentException ok) {
        }
        try {
            input.expect(LONG_TIMEOUT, anyOf((Matcher<?>[]) null));
            fail();
        } catch (NullPointerException ok) {
        }

        MultiResult result = input.expect(LONG_TIMEOUT, anyOf(contains("b")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 2);
        assertEquals(result.end(), 3);
        assertTrue(result.getInput().equals(result.getResults().get(0).getInput()));
        assertEquals(result.getInput(), "a1b2c3_");

        mock.push(text);
        result = input.expect(LONG_TIMEOUT, anyOf(contains("x"), contains("b"), contains("zzz")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 6);
        assertEquals(result.end(), 7);
        assertEquals(result.group(), "b");

        result = input.expect(SMALL_TIMEOUT, anyOf(contains("ttt"), contains("zzz")));
        assertFalse(result.isSuccessful());

        mock.push(text);
        // test for combination of matchers
        result = input.expect(
                LONG_TIMEOUT,
                anyOf(allOf(contains("1"), contains("2")), contains("FFFF")));
        assertTrue(result.isSuccessful());
        assertTrue(result.getResults().get(0) instanceof MultiResult);
        assertEquals(result.getResults().size(), 2);

        // test canStopMatching
        mock.push(text);
        result = input.expect(SMALL_TIMEOUT, anyOf(startsWith("a")));
        assertTrue(result.canStopMatching());
        result = input.expect(SMALL_TIMEOUT, anyOf(startsWith("a"), startsWith(text + text)));
        assertFalse(result.canStopMatching());
    }

    @Test
    public void testToString() {
        Matcher<Result> c = contains("xyz");
        assertEquals(c.toString(), "contains('xyz')");

        Matcher<Result> r = regexp("xyz");
        assertEquals(r.toString(), "regexp('xyz')");

        Matcher<Result> m = matches("xyz");
        assertEquals(m.toString(), "matches('xyz')");
        assertEquals(
                anyOf(c, r, m).toString(),
                "anyOf(contains('xyz'),regexp('xyz'),matches('xyz'))");
        assertEquals(allOf(c).toString(), "allOf(contains('xyz'))");

        Matcher<?> e = Matchers.eof();
        assertEquals(allOf(c, e).toString(), "allOf(contains('xyz'),eof)");
        assertEquals(startsWith("XXX").toString(), "startsWith('XXX')");
        assertEquals(
                sequence(startsWith("TTT"), eof()).toString(), "sequence(startsWith('TTT'),eof)");
    }

    @Test
    public void testEofMatcher() throws IOException, InterruptedException {
        assertTrue(input.expect(SMALL_TIMEOUT, contains("a1")).isSuccessful());
        assertTrue(input.expect(SMALL_TIMEOUT, contains("_")).isSuccessful());
        // make sure that all the data received
        input.expect(SMALL_TIMEOUT, times(3, contains("_")));
        // make sure the buffer is cleaned
        assertTrue(input.expect(SMALL_TIMEOUT, matches(".*")).isSuccessful());
        mock.push(TestUtils.EOF);
        try {
            // now the buffer is empty
            input.expect(SMALL_TIMEOUT, contains("xxx"));
            fail();
        } catch (EOFException ok) {
        }
        // should pass on successful match
        assertTrue(input.expect(SMALL_TIMEOUT, contains("")).isSuccessful());
        assertTrue(input.expect(SMALL_TIMEOUT, eof()).isSuccessful());
    }

    @Test
    public void testEofMultiMatcher1() throws IOException, InterruptedException {
        assertTrue(input.expect(SMALL_TIMEOUT, contains("a1")).isSuccessful());
        mock.push(TestUtils.EOF);
        MultiResult result = input.expect(SMALL_TIMEOUT, allOf(contains("b"), eof()));
        assertTrue(result.isSuccessful());
        assertFalse(result.getBefore().isEmpty());
        assertTrue(input.expect(SMALL_TIMEOUT, eof()).isSuccessful());
    }

    @Test
    public void testEofMultiMatcher2() throws IOException, InterruptedException {
        assertTrue(input.expect(SMALL_TIMEOUT, contains("a1")).isSuccessful());
        mock.push(TestUtils.EOF);
        MultiResult result = input.expect(SMALL_TIMEOUT, anyOf(contains("wrong"), eof()));
        assertTrue(result.isSuccessful());
        assertFalse(result.getBefore().isEmpty());
        assertTrue(input.expect(SMALL_TIMEOUT, eof()).isSuccessful());
    }

    @Test

    public void testTimes() throws IOException, InterruptedException {
        MultiResult result = input.expect(SMALL_TIMEOUT, times(1, contains("b")));
        assertEquals("a1", result.getBefore());
        assertEquals(result.group(), "b");

        result = times(2, contains("abc")).matches("ZabcXabcY", false);
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "ZabcX");
        assertEquals(result.getResults().get(0).getBefore(), "Z");
        assertEquals(result.getResults().get(1).getBefore(), "X");

        result = times(3, contains("abc")).matches("ZabcXabcY", false);
        assertFalse(result.isSuccessful());
        mock.push(text + text);
        result = input.expect(2 * SMALL_TIMEOUT, times(3, contains("_")));
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "2c3_a1b2c3_a1b2c3");
        assertEquals(result.group(), "_");
        for (Result r : result.getResults()) {
            assertTrue(r.isSuccessful());
            assertTrue(r.getBefore().endsWith("2c3"));
        }
        mock.push(text);
        result = input.expect(SMALL_TIMEOUT, times(10, contains("c")));
        assertTrue(result.getResults().get(0).isSuccessful());
        assertFalse(result.getResults().get(8).isSuccessful());
        assertFalse(result.getResults().get(9).isSuccessful());
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testSequence() throws IOException, InterruptedException {
        try {
            sequence();
            fail();
        } catch (IllegalArgumentException ignore) {

        }
        MultiResult result = input.expect(SMALL_TIMEOUT, sequence(contains("b")));
        assertEquals("a1", result.getBefore());
        mock.push(text);
        result = input.expect(SMALL_TIMEOUT, sequence(contains("c"), contains("b")));
        assertEquals("2c3_a1", result.getBefore());
        assertEquals(result.group(), "b");
        assertEquals(result.getResults().get(0).group(), "c");

        mock.push(text);
        final Matcher<MultiResult> sequence = sequence(contains("a"), contains("Z"), contains("c"));
        assertEquals(sequence.toString(), "sequence(contains('a'),contains('Z'),contains('c'))");
        result = input.expect(SMALL_TIMEOUT, sequence);
        assertFalse(result.isSuccessful());
        assertTrue(result.getResults().get(0).isSuccessful());
        assertEquals(result.getResults().get(0).getBefore(), "2c3_");


        result = input.expect(
                SMALL_TIMEOUT,
                sequence(contains("3"), anyOf(contains("Z"), contains("c"))));
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "2c3_a1b2");
        assertEquals(result.getResults().get(0).getBefore(), "2c");
        assertEquals(result.getResults().get(1).getBefore(), "_a1b2");
        assertEquals(result.getInput(), "2c3_a1b2c3_");
        assertEquals(result.getResults().get(1).getInput(), "_a1b2c3_");
    }

    @Test
    public void testAnyStringInfiniteTimeout() throws IOException {
        assertTrue(input.expect(ExpectImpl.INFINITE_TIMEOUT, anyString()).isSuccessful());
    }

    @Test
    public void testAnyString() throws IOException, InterruptedException {
        Result result = input.expect(SMALL_TIMEOUT, anyString());
        assertTrue(result.group().startsWith("a1"));
        assertTrue(result.group().endsWith("_"));
        assertEquals(result.getBefore(), "");
        mock.push(text);
        assertTrue(input.expect(SMALL_TIMEOUT, contains("a1b2")).getBefore().isEmpty());

        // stop producing new input
        reset(mock.getStream());
        when(mock.getStream().read(any(byte[].class))).thenReturn(0);

        // just to make sure that the buffer is clean
        input.expect(SMALL_TIMEOUT, anyString());
        input.expect(SMALL_TIMEOUT, anyString());
        assertTrue(input.getBuffer().length() == 0);
        result = input.expect(SMALL_TIMEOUT, anyString());
        assertFalse(result.isSuccessful());
    }

    /**
     * Verifies that all the result methods throw IllegalStateException.
     */
    private static void checkIllegalState(Result result) {
        try {
            result.end();
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.getBefore();
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.group();
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.start();
            fail();
        } catch (IllegalStateException ignore) {
        }
        assertNotNull(result.getInput());
    }

    /**
     * Verifies that all the result methods that take the index throws IndexOutOfBoundException
     * for the given index.
     */
    private static void checkIndexOutOfBound(Result result, int index) {
        try {
            result.end(index);
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
        try {
            result.group(index);
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
        try {
            result.start(index);
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    /**
     * Verifies that all the result methods throw IllegalStateException.
     */
    private static void checkIllegalState(Result result, int index) {
        try {
            result.end(index);
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.group(index);
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.start(index);
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test (timeout = 5000)
    public void testExact() throws IOException, InterruptedException {
        Result result = input.expect(SMALL_TIMEOUT, exact("a1"));
        assertFalse(result.isSuccessful());
        result = input.expect(SMALL_TIMEOUT, exact(text));
        assertTrue(result.isSuccessful());
        assertTrue(result.canStopMatching());
        assertEquals(result.getBefore(), "");
        assertEquals(result.groupCount(), 0);
        assertEquals(result.group(), text);
        assertFalse(input.expect(SMALL_TIMEOUT, exact(text)).isSuccessful());

        mock.push("XZY");
        result = input.expect(LONG_TIMEOUT * 100, exact("x"));
        assertFalse(result.isSuccessful());
        assertTrue(result.canStopMatching());
    }

    @Test
    public void testStartsWith() throws IOException, InterruptedException {
        Result result = input.expect(SMALL_TIMEOUT, startsWith("YZ"));
        assertFalse(result.isSuccessful());
        final String substring = text.substring(0, 2);
        result = input.expect(SMALL_TIMEOUT, startsWith(substring));
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "");
        assertEquals(result.groupCount(), 0);
        assertTrue(result.canStopMatching());
        assertEquals(result.group(), substring);
        final Result result2 = input.expect(SMALL_TIMEOUT, startsWith(text + text));
        assertFalse(result2.isSuccessful());
        assertFalse(result2.canStopMatching());
    }
}
